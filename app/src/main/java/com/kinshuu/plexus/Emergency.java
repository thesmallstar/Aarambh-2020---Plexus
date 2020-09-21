package com.kinshuu.plexus;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.nio.charset.Charset;

import io.chirp.chirpsdk.ChirpSDK;
import io.chirp.chirpsdk.models.ChirpError;

import static com.kinshuu.plexus.MainActivity.mUsername;

public class Emergency extends AppCompatActivity {

    Button BTNsend;
    EditText ETmsg;

    String identifier="default";
    String TAG="MyLOGS";
    public static ChirpSDK chirp;
    private static final int RESULT_REQUEST_RECORD_AUDIO = 1;
    public static String CHIRP_APP_KEY = "a425Bff1fE83BaFD9e5F6d5A3";
    public static String CHIRP_APP_SECRET = "A0DFeB41c736196Fa82fBFCcAa36E334610Ea71bd128e7e9D5";
    public static String CHIRP_APP_CONFIG = "GpEu5S5FtjVzSmhBNpDrmLU9Ojw4c2xcsrhoPuerCsqSNfoicEuPCx0iX1lNFMYN2ekd4+HAT1wXVvvgqaTrm7FXO3EKt6aDCEOYcZtc8oPSMNk83Q/UMFfdgimH5AYSbx9yFiuvoKAuhXA31VsiEfdYSLD82zXTgEjgTYyzje1BMRIEqXKV3fG6pT14vftbJ1gc3qJR1RW4+/g1bVqKo6zG7gHkC+qwzSrqQ1/63lA2wMQ8Cvu3mmMzvgFVWlsBUg++sxGaztNCX0F7Ig96oi7PGeVGZGj5nnicfJsL3RHH2siNwoILh9E6SkejXNGq5uq35juxz1ySslDGTOr2y0yvKxjfgC5JI2+01TLlXGPTY8q7cDpASP9rbSwHWoEu7HIxHgu/g1ZZTfo21HxAkjHcxg0Zj+25HkTCalQ/jbrB33yYEUUI+05l+dP0OU29SMeZ1G2xTmrzy2nerEzTOW9CECAu/X0Vy6Wk+qYScuW64uboqeQnSfer5qmDK44jNYuwAg9ZklpzkTKaIRD/2bpsBElAwwS5UvTI5u2uQ/obYopGHC6VB88Ird1Q41FGGnIfMYwmvRJfPpBa4TGvU8S/9NoNZF891m0FYvy0FoN2kxus+Xi6z7O2lvcEGON+aiiKenCC+xdAimpNEGVJyQH36AG6KsIz3iAJT+Q/lpBwbWerYf5s7SlNZhlkxeWQfb+X7p/SSNndfrxsuX5g3RhMKXbXvG3bQueQHzguaWi5ykqMWzioEQA/xGeRhPu1gYaMDXYmonLfX/WWEuEpEqhmSlNh/ePnqM85CMkXBPz0AbleD5Xe0nz+f/hO2iX09br1ymNpJ2PnhEwpAPffoGNNYrxceUxCYYEMImRMaBjskKxoGl5jxknvY9G5jn6kW16/91NApoeul75yRFdzqF5fYT68uxNTzlF9stX6ukvsDoWPr8EFWl0OY82pgUfRiGdQwyy/0f9k8SKD41Ptcyac0VVeiZcoLeuRtm3EJPk9bgMk7FpQmkTz/dOSXVCb4upARtDmGY1+v5nbsxNEocmC9aVlHWXyYKqzKjZzf15XPLMUGP5/HPLA+8bNs7G9MT+vWi5EhR/QMKaDykLPdQCKrDJvXdPxeKVriDIqRGim24EmheFz4lb2Q2apYkc4JCD4gnXKuvrTdhtBtHZx3A==";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emergency);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setLogo(R.drawable.ic_navbar);
        actionBar.setDisplayUseLogoEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setBackgroundDrawable(new ColorDrawable(0));
        Button BTNsend= findViewById(R.id.BTNsend);

        BTNsend.setEnabled(false);
        final EditText ETmsg= findViewById(R.id.ETmsg);

        ETmsg.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable arg0) {
                enableSubmitIfReady();
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        });

        ChirpError error = chirp.setConfig(CHIRP_APP_CONFIG);
        if (error.getCode() == 0) {
            Log.v("ChirpSDK: ", "Configured ChirpSDK");
        } else {
            Log.e("ChirpError: ", error.getMessage());
        }

        BTNsend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String msg=ETmsg.getText().toString();
                if(!isNetworkAvailable()) {
                    //sending w/o signal
                    Toast.makeText(Emergency.this, "Network Unavailable, sending audio", Toast.LENGTH_SHORT).show();
                    identifier=msg;
                    byte[] payload = identifier.getBytes(Charset.forName("UTF-8"));
                    ChirpError error = chirp.send(payload);
                    Log.d(TAG, "onClick: identifier is "+identifier);
                    if (error.getCode() > 0) {
                        Log.d(TAG, error.getMessage());
                    } else {
                        Log.d(TAG, "Sent " + identifier);
                    }
                }
            }
        });
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    @Override
    protected void onResume() {
        super.onResume();
//        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.RECORD_AUDIO}, RESULT_REQUEST_RECORD_AUDIO);
//        }
        //else {
            // Start ChirpSDK sender and receiver, if no arguments are passed both sender and receiver are started
            ChirpError error = chirp.start(true, true);
            if (error.getCode() > 0) {
                Log.e("ChirpError: ", error.getMessage());
            } else {
                Log.v("ChirpSDK: ", "Started ChirpSDK");
            }
        //}
    }

    public void enableSubmitIfReady() {

        ETmsg=findViewById(R.id.ETmsg);
        BTNsend=findViewById(R.id.BTNsend);
        boolean isReady = ETmsg.getText().toString().length() > 0;
        BTNsend.setEnabled(isReady);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case RESULT_REQUEST_RECORD_AUDIO: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    ChirpError error = chirp.start();
                    if (error.getCode() > 0) {
                        Log.e("ChirpError: ", error.getMessage());
                    } else {
                        Log.v("ChirpSDK: ", "Started ChirpSDK");
                    }
                }
                return;
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        chirp.stop();
        try {
            chirp.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
