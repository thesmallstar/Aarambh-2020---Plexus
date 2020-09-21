package com.kinshuu.plexus;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.UiThread;
import androidx.annotation.WorkerThread;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;

import android.Manifest;
import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.text.SpannableString;
import android.text.format.DateFormat;
import android.text.method.ScrollingMovementMethod;
import android.text.style.ForegroundColorSpan;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.nearby.connection.ConnectionInfo;
import com.google.android.gms.nearby.connection.Payload;
import com.google.android.gms.nearby.connection.Strategy;

import java.io.IOException;
import java.util.Random;

public class WalkiTalkie extends ConnectionsActivity {

    private static final boolean DEBUG = true;


    private static final Strategy STRATEGY = Strategy.P2P_STAR;


    private static final long ANIMATION_DURATION = 600;


    @ColorInt
    private static final int[] COLORS =
            new int[] {
                    0xFFF44336 /* red */,
                    0xFF9C27B0 /* deep purple */,
                    0xFF00BCD4 /* teal */,
                    0xFF4CAF50 /* green */,
                    0xFFFFAB00 /* amber */,
                    0xFFFF9800 /* orange */,
                    0xFF795548 /* brown */
            };


    private static final String SERVICE_ID =
            "com.google.location.nearby.apps.walkietalkie.automatic.SERVICE_ID";


    private State mState = State.UNKNOWN;


    private String mName;


    @ColorInt private int mConnectedColor = COLORS[0];


    private TextView mPreviousStateView;

    private TextView mCurrentStateView;


    @Nullable
    private Animator mCurrentAnimator;


    private TextView mDebugLogView;


    private final GestureDetector mGestureDetector =
            new GestureDetector(KeyEvent.KEYCODE_VOLUME_DOWN, KeyEvent.KEYCODE_VOLUME_UP) {
                @Override
                protected void onHold() {
                    logV("onHold");
                    startRecording();
                }

                @Override
                protected void onRelease() {
                    logV("onRelease");
                    stopRecording();
                }
            };


    @Nullable private AudioRecorder mRecorder;


    @Nullable private AudioPlayer mAudioPlayer;

    private int mOriginalVolume;
    private Button bt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_walki_talkie);
        bt = (Button)findViewById(R.id.starter);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setLogo(R.drawable.ic_navbar);
        actionBar.setDisplayUseLogoEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setBackgroundDrawable(new ColorDrawable(0));
        mPreviousStateView = (TextView) findViewById(R.id.previous_state);
        mCurrentStateView = (TextView) findViewById(R.id.current_state);

        mDebugLogView = (TextView) findViewById(R.id.debug_log);
        mDebugLogView.setVisibility(DEBUG ? View.VISIBLE : View.GONE);
        mDebugLogView.setMovementMethod(new ScrollingMovementMethod());

        mName = generateRandomName();

        ((TextView) findViewById(R.id.name)).setText(mName);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (mState == State.CONNECTED && mGestureDetector.onKeyEvent(event)) {
            return true;
        }
        return super.dispatchKeyEvent(event);
    }

    @Override
    protected void onStart() {
        super.onStart();

        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        mOriginalVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        audioManager.setStreamVolume(
                AudioManager.STREAM_MUSIC, audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC), 0);


    }

    public void handleStart(View view){

        setState(State.SEARCHING);
    }

    @Override
    protected void onStop() {

        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, mOriginalVolume, 0);
        setVolumeControlStream(AudioManager.USE_DEFAULT_STREAM_TYPE);


        if (isRecording()) {
            stopRecording();
        }
        if (isPlaying()) {
            stopPlaying();
        }

        setState(State.UNKNOWN);

        if (mCurrentAnimator != null && mCurrentAnimator.isRunning()) {
            mCurrentAnimator.cancel();
        }

        super.onStop();
    }

    @Override
    public void onBackPressed() {
        if (getState() == State.CONNECTED) {
            setState(State.SEARCHING);
            return;
        }
        super.onBackPressed();
    }

    @Override
    protected void onEndpointDiscovered(Endpoint endpoint) {
        stopDiscovering();
        connectToEndpoint(endpoint);
    }

    @Override
    protected void onConnectionInitiated(Endpoint endpoint, ConnectionInfo connectionInfo) {
        mConnectedColor = COLORS[connectionInfo.getAuthenticationToken().hashCode() % COLORS.length];

        acceptConnection(endpoint);
    }

    @Override
    protected void onEndpointConnected(Endpoint endpoint) {
        Toast.makeText(
                this, getString(R.string.toast_connected, endpoint.getName()), Toast.LENGTH_SHORT)
                .show();
        setState(State.CONNECTED);
    }

    @Override
    protected void onEndpointDisconnected(Endpoint endpoint) {
        Toast.makeText(
                this, getString(R.string.toast_disconnected, endpoint.getName()), Toast.LENGTH_SHORT)
                .show();
        setState(State.SEARCHING);
    }

    @Override
    protected void onConnectionFailed(Endpoint endpoint) {
        // Let's try someone else.
        if (getState() == State.SEARCHING) {
            startDiscovering();
        }
    }


    private void setState(State state) {
        if (mState == state) {
            logW("State set to " + state + " but already in that state");
            return;
        }

        logD("State set to " + state);
        State oldState = mState;
        mState = state;
        onStateChanged(oldState, state);
    }


    private State getState() {
        return mState;
    }


    private void onStateChanged(State oldState, State newState) {
        if (mCurrentAnimator != null && mCurrentAnimator.isRunning()) {
            mCurrentAnimator.cancel();
        }


        switch (newState) {
            case SEARCHING:
                disconnectFromAllEndpoints();
                startDiscovering();
                startAdvertising();
                break;
            case CONNECTED:
                stopDiscovering();
                stopAdvertising();
                break;
            case UNKNOWN:
                stopAllEndpoints();
                break;
            default:
                break;
        }

        switch (oldState) {
            case UNKNOWN:

                transitionForward(oldState, newState);
                break;
            case SEARCHING:
                switch (newState) {
                    case UNKNOWN:
                        transitionBackward(oldState, newState);
                        break;
                    case CONNECTED:
                        transitionForward(oldState, newState);
                        break;
                    default:
                        // no-op
                        break;
                }
                break;
            case CONNECTED:
                transitionBackward(oldState, newState);
                break;
        }
    }


    @UiThread
    private void transitionForward(State oldState, final State newState) {
        mPreviousStateView.setVisibility(View.VISIBLE);
        mCurrentStateView.setVisibility(View.VISIBLE);

        updateTextView(mPreviousStateView, oldState);
        updateTextView(mCurrentStateView, newState);

        if (ViewCompat.isLaidOut(mCurrentStateView)) {
            mCurrentAnimator = createAnimator(false /* reverse */);
            mCurrentAnimator.addListener(
                    new AnimatorListener() {
                        @Override
                        public void onAnimationEnd(Animator animator) {
                            updateTextView(mCurrentStateView, newState);
                        }
                    });
            mCurrentAnimator.start();
        }
    }


    @UiThread
    private void transitionBackward(State oldState, final State newState) {
        mPreviousStateView.setVisibility(View.VISIBLE);
        mCurrentStateView.setVisibility(View.VISIBLE);

        updateTextView(mCurrentStateView, oldState);
        updateTextView(mPreviousStateView, newState);

        if (ViewCompat.isLaidOut(mCurrentStateView)) {
            mCurrentAnimator = createAnimator(true /* reverse */);
            mCurrentAnimator.addListener(
                    new AnimatorListener() {
                        @Override
                        public void onAnimationEnd(Animator animator) {
                            updateTextView(mCurrentStateView, newState);
                        }
                    });
            mCurrentAnimator.start();
        }
    }

    @NonNull
    private Animator createAnimator(boolean reverse) {
        Animator animator;
        if (Build.VERSION.SDK_INT >= 21) {
            int cx = mCurrentStateView.getMeasuredWidth() / 2;
            int cy = mCurrentStateView.getMeasuredHeight() / 2;
            int initialRadius = 0;
            int finalRadius = Math.max(mCurrentStateView.getWidth(), mCurrentStateView.getHeight());
            if (reverse) {
                int temp = initialRadius;
                initialRadius = finalRadius;
                finalRadius = temp;
            }
            animator =
                    ViewAnimationUtils.createCircularReveal(
                            mCurrentStateView, cx, cy, initialRadius, finalRadius);
        } else {
            float initialAlpha = 0f;
            float finalAlpha = 1f;
            if (reverse) {
                float temp = initialAlpha;
                initialAlpha = finalAlpha;
                finalAlpha = temp;
            }
            mCurrentStateView.setAlpha(initialAlpha);
            animator = ObjectAnimator.ofFloat(mCurrentStateView, "alpha", finalAlpha);
        }
        animator.addListener(
                new AnimatorListener() {
                    @Override
                    public void onAnimationCancel(Animator animator) {
                        mPreviousStateView.setVisibility(View.GONE);
                        mCurrentStateView.setAlpha(1);
                    }

                    @Override
                    public void onAnimationEnd(Animator animator) {
                        mPreviousStateView.setVisibility(View.GONE);
                        mCurrentStateView.setAlpha(1);
                    }
                });
        animator.setDuration(ANIMATION_DURATION);
        return animator;
    }

    /** Updates the {@link TextView} with the correct color/text for the given {@link State}. */
    @UiThread
    private void updateTextView(TextView textView, State state) {
        switch (state) {
            case SEARCHING:
                textView.setBackgroundResource(R.color.state_searching);
                textView.setText(R.string.status_searching);
                break;
            case CONNECTED:
                textView.setBackgroundColor(mConnectedColor);
                textView.setText(R.string.status_connected);
                break;
            default:
                textView.setBackgroundResource(R.color.state_unknown);
                textView.setText(R.string.status_unknown);
                break;
        }
    }

    @Override
    protected void onReceive(Endpoint endpoint, Payload payload) {
        if (payload.getType() == Payload.Type.STREAM) {
            if (mAudioPlayer != null) {
                mAudioPlayer.stop();
                mAudioPlayer = null;
            }

            AudioPlayer player =
                    new AudioPlayer(payload.asStream().asInputStream()) {
                        @WorkerThread
                        @Override
                        protected void onFinish() {
                            runOnUiThread(
                                    new Runnable() {
                                        @UiThread
                                        @Override
                                        public void run() {
                                            mAudioPlayer = null;
                                        }
                                    });
                        }
                    };
            mAudioPlayer = player;
            player.start();
        }
    }


    private void stopPlaying() {
        logV("stopPlaying()");
        if (mAudioPlayer != null) {
            mAudioPlayer.stop();
            mAudioPlayer = null;
        }
    }


    private boolean isPlaying() {
        return mAudioPlayer != null;
    }


    private void startRecording() {
        logV("startRecording()");
        try {
            ParcelFileDescriptor[] payloadPipe = ParcelFileDescriptor.createPipe();

            // Send the first half of the payload (the read side) to Nearby Connections.
            send(Payload.fromStream(payloadPipe[0]));

            // Use the second half of the payload (the write side) in AudioRecorder.
            mRecorder = new AudioRecorder(payloadPipe[1]);
            mRecorder.start();
        } catch (IOException e) {
            logE("startRecording() failed", e);
        }
    }


    private void stopRecording() {
        logV("stopRecording()");
        if (mRecorder != null) {
            mRecorder.stop();
            mRecorder = null;
        }
    }


    private boolean isRecording() {
        return mRecorder != null && mRecorder.isRecording();
    }

    private static String[] join(String[] a, String... b) {
        String[] join = new String[a.length + b.length];
        System.arraycopy(a, 0, join, 0, a.length);
        System.arraycopy(b, 0, join, a.length, b.length);
        return join;
    }


    @Override
    protected String getName() {
        return mName;
    }


    @Override
    public String getServiceId() {
        return SERVICE_ID;
    }


    @Override
    public Strategy getStrategy() {
        return STRATEGY;
    }

    @Override
    protected void logV(String msg) {
        super.logV(msg);
        appendToLogs(toColor(msg, getResources().getColor(R.color.log_verbose)));
    }

    @Override
    protected void logD(String msg) {
        super.logD(msg);
        appendToLogs(toColor(msg, getResources().getColor(R.color.log_debug)));
    }

    @Override
    protected void logW(String msg) {
        super.logW(msg);
        appendToLogs(toColor(msg, getResources().getColor(R.color.log_warning)));
    }

    @Override
    protected void logW(String msg, Throwable e) {
        super.logW(msg, e);
        appendToLogs(toColor(msg, getResources().getColor(R.color.log_warning)));
    }

    @Override
    protected void logE(String msg, Throwable e) {
        super.logE(msg, e);
        appendToLogs(toColor(msg, getResources().getColor(R.color.log_error)));
    }

    private void appendToLogs(CharSequence msg) {
        mDebugLogView.append("\n");
        mDebugLogView.append(DateFormat.format("hh:mm", System.currentTimeMillis()) + ": ");
        mDebugLogView.append(msg);
    }

    private static CharSequence toColor(String msg, int color) {
        SpannableString spannable = new SpannableString(msg);
        spannable.setSpan(new ForegroundColorSpan(color), 0, msg.length(), 0);
        return spannable;
    }

    private static String generateRandomName() {
        String name = "";
        Random random = new Random();
        for (int i = 0; i < 5; i++) {
            name += random.nextInt(10);
        }
        return name;
    }


    private abstract static class AnimatorListener implements Animator.AnimatorListener {
        @Override
        public void onAnimationStart(Animator animator) {}

        @Override
        public void onAnimationEnd(Animator animator) {}

        @Override
        public void onAnimationCancel(Animator animator) {}

        @Override
        public void onAnimationRepeat(Animator animator) {}
    }


    public enum State {
        UNKNOWN,
        SEARCHING,
        CONNECTED
    }
}
