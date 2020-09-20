package com.kinshuu.plexus;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import java.nio.charset.Charset;

import io.chirp.chirpsdk.interfaces.ChirpEventListener;
import io.chirp.chirpsdk.models.ChirpError;

import static com.kinshuu.plexus.Emergency.chirp;

public class Listener extends Service {

    String TAG="MyLOGS";
    String recieved="null";

    private static final int NOTIF_ID = 1;
    private static final String NOTIF_CHANNEL_ID = "Channel_Id";

    public Listener() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        ChirpError error = chirp.start(true, true);
        if (error.getCode() > 0) {
            Log.e("ChirpError: ", error.getMessage());
        } else {
            Log.v("ChirpSDK: ", "Started ChirpSDK");
        }
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        ChirpError error = chirp.start(true, true);
        if (error.getCode() > 0) {
            Log.e("ChirpError: ", error.getMessage());
        } else {
            Log.v(TAG, "Started ChirpSDK");
        }

        ChirpEventListener chirpEventListener = new ChirpEventListener() {
            @Override
            public void onReceived(byte[] data, int channel) {
                if (data != null) {
                    String identifier = new String(data);
                    Log.v(TAG, "Received " + identifier);
                    if(!isNetworkAvailable()) {
                        //sending w/o signal
                        Log.d(TAG, "onReceived: identifier is "+identifier);
                        if(identifier.equals(recieved)){
                            Toast.makeText(Listener.this, "Repeated Receive, not forwarding", Toast.LENGTH_SHORT).show();
                        }
                        else {
                            Toast.makeText(Listener.this, "Network Unavailable, forwarding Audio. " + identifier, Toast.LENGTH_SHORT).show();
                            byte[] payload = identifier.getBytes(Charset.forName("UTF-8"));
                            ChirpError error = chirp.send(payload);
                            if (error.getCode() > 0) {
                                Log.e("ChirpError: ", error.getMessage());
                            } else {
                                Log.v("ChirpSDK: ", "Sent " + identifier);
                            }
                        }
                        recieved=identifier;
                    }
                } else {
                    Log.e("ChirpError: ", "Decode failed");
                }
            }
            public void onSending(byte[] payload, int channel) {}
            public void onSent(byte[] payload, int channel) {}
            public void onReceiving(int channel) {}
            public void onStateChanged(int oldState, int newState) {}
            @Override
            public void onSystemVolumeChanged(float old, float current) {}
        };
        chirp.setListener(chirpEventListener);
        startForeground();
        return super.onStartCommand(intent, flags, startId);
    }

    private void startForeground() {
        Intent notificationIntent = new Intent(this, Emergency.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, 0);
        initChannels(getApplicationContext());
        startForeground(NOTIF_ID, new NotificationCompat.Builder(this,
                NOTIF_CHANNEL_ID) // don't forget create a notification channel first
                .setOngoing(true)
                .setContentTitle(getString(R.string.app_name))
                .setContentText("Listening in background")
                .setContentIntent(pendingIntent)
                .build());
    }

    public void initChannels(Context context) {
        if (Build.VERSION.SDK_INT < 26) {
            return;
        }
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationChannel channel = new NotificationChannel(NOTIF_CHANNEL_ID,
                "Background channel",
                NotificationManager.IMPORTANCE_DEFAULT);
        channel.setDescription("Channel description");
        notificationManager.createNotificationChannel(channel);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        ChirpError error = chirp.start(true, true);
        if (error.getCode() > 0) {
            Log.e("ChirpError: ", error.getMessage());
        } else {
            Log.v("ChirpSDK: ", "Started ChirpSDK");
        }
    }

    @Override
    public void onRebind(Intent intent) {
        // Start ChirpSDK sender and receiver, if no arguments are passed both sender and receiver are started
        ChirpError error = chirp.start(true, true);
        if (error.getCode() > 0) {
            Log.e("ChirpError: ", error.getMessage());
        } else {
            Log.v("ChirpSDK: ", "Started ChirpSDK");
        }
        super.onRebind(intent);
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}
