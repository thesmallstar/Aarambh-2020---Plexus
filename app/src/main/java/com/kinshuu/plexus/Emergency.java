package com.kinshuu.plexus;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.nio.charset.Charset;

import io.chirp.chirpsdk.ChirpSDK;
import io.chirp.chirpsdk.models.ChirpError;

import static com.kinshuu.plexus.Listener.PhDirectory;
import static com.kinshuu.plexus.MainActivity.mUsername;

public class Emergency extends AppCompatActivity {

    Button BTNsend;
    EditText ETmsg;

    String identifier = "default";
    String TAG = "MyLOGS";
    Spinner spinnerEmergency;
    String select, mlat, mlong, msg;
    private FusedLocationProviderClient fusedLocationClient;


    public static ChirpSDK chirp;
    private static final int RESULT_REQUEST_RECORD_AUDIO = 1;
    public static String CHIRP_APP_KEY = "a425Bff1fE83BaFD9e5F6d5A3";
    public static String CHIRP_APP_SECRET = "A0DFeB41c736196Fa82fBFCcAa36E334610Ea71bd128e7e9D5";
    public static String CHIRP_APP_CONFIG = "GpEu5S5FtjVzSmhBNpDrmLU9Ojw4c2xcsrhoPuerCsqSNfoicEuPCx0iX1lNFMYN2ekd4+HAT1wXVvvgqaTrm7FXO3EKt6aDCEOYcZtc8oPSMNk83Q/UMFfdgimH5AYSbx9yFiuvoKAuhXA31VsiEfdYSLD82zXTgEjgTYyzje1BMRIEqXKV3fG6pT14vftbJ1gc3qJR1RW4+/g1bVqKo6zG7gHkC+qwzSrqQ1/63lA2wMQ8Cvu3mmMzvgFVWlsBUg++sxGaztNCX0F7Ig96oi7PGeVGZGj5nnicfJsL3RHH2siNwoILh9E6SkejXNGq5uq35juxz1ySslDGTOr2y0yvKxjfgC5JI2+01TLlXGPTY8q7cDpASP9rbSwHWoEu7HIxHgu/g1ZZTfo21HxAkjHcxg0Zj+25HkTCalQ/jbrB33yYEUUI+05l+dP0OU29SMeZ1G2xTmrzy2nerEzTOW9CECAu/X0Vy6Wk+qYScuW64uboqeQnSfer5qmDK44jNYuwAg9ZklpzkTKaIRD/2bpsBElAwwS5UvTI5u2uQ/obYopGHC6VB88Ird1Q41FGGnIfMYwmvRJfPpBa4TGvU8S/9NoNZF891m0FYvy0FoN2kxus+Xi6z7O2lvcEGON+aiiKenCC+xdAimpNEGVJyQH36AG6KsIz3iAJT+Q/lpBwbWerYf5s7SlNZhlkxeWQfb+X7p/SSNndfrxsuX5g3RhMKXbXvG3bQueQHzguaWi5ykqMWzioEQA/xGeRhPu1gYaMDXYmonLfX/WWEuEpEqhmSlNh/ePnqM85CMkXBPz0AbleD5Xe0nz+f/hO2iX09br1ymNpJ2PnhEwpAPffoGNNYrxceUxCYYEMImRMaBjskKxoGl5jxknvY9G5jn6kW16/91NApoeul75yRFdzqF5fYT68uxNTzlF9stX6ukvsDoWPr8EFWl0OY82pgUfRiGdQwyy/0f9k8SKD41Ptcyac0VVeiZcoLeuRtm3EJPk9bgMk7FpQmkTz/dOSXVCb4upARtDmGY1+v5nbsxNEocmC9aVlHWXyYKqzKjZzf15XPLMUGP5/HPLA+8bNs7G9MT+vWi5EhR/QMKaDykLPdQCKrDJvXdPxeKVriDIqRGim24EmheFz4lb2Q2apYkc4JCD4gnXKuvrTdhtBtHZx3A==";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emergency);

        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.SEND_SMS}, 101);
        }

        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 101);
        }

        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 101);
        }

        spinnerEmergency = findViewById(R.id.spinnerEmergency);
        String[] spinnerEmergencylist = {"Other", "Medical", "Fire", "Police"};
        ArrayAdapter<String> spinnerEmergencyAdapter = new ArrayAdapter<>(Emergency.this, android.R.layout.simple_list_item_1, spinnerEmergencylist);
        spinnerEmergencyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerEmergency.setAdapter(spinnerEmergencyAdapter);
        spinnerEmergency.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (parent.getItemAtPosition(position).toString().equals("Other"))
                    select = "z";
                else {
                    Log.d(TAG, "onItemSelected: in else");
                    select = parent.getItemAtPosition(position).toString().charAt(0) + "";
                    Log.d(TAG, "onItemSelected: Select is "+select);
                    fusedLocationClient.getLastLocation().addOnSuccessListener(Emergency.this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            Log.d(TAG, "onSuccess: got location");
                            // Got last known location. In some rare situations this can be null.
                            if (location != null) {
                                // Logic to handle location object
                                mlat = location.getLatitude() + "";
                                mlong = location.getLongitude() + "";
//                              BTNsend.setEnabled(true);
                                Toast.makeText(Emergency.this, "Selected is " + select, Toast.LENGTH_SHORT).show();
                                Log.d(TAG, "onSuccess: Location found");
                                Log.d(TAG, "onSuccess: Lat is "+mlat+"Long is "+mlong);
                            }
                        }
                    });
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                Toast.makeText(Emergency.this, "Nothing selectde", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "onNothingSelected: nothing selected");

            }
        });

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        Button BTNsend = findViewById(R.id.BTNsend);

//        BTNsend.setEnabled(false);
        final EditText ETmsg = findViewById(R.id.ETmsg);

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
                if (select.equals("z"))
                    msg = ETmsg.getText().toString();
                else {
                    msg = select + mlat.substring(5, 8) + mlong.substring(5, 8);
                    Log.d(TAG, "onSuccess: Lat is "+mlat.substring(5, 8)+"Long is "+mlong.substring(5, 8));
                    Log.d(TAG, "onClick: msg is "+msg);
                }
                identifier = msg;
                if (!isNetworkAvailable()) {
                    //sending w/o signal
                    Toast.makeText(Emergency.this, "Network Unavailable, sending audio", Toast.LENGTH_SHORT).show();
                    byte[] payload = identifier.getBytes(Charset.forName("UTF-8"));
                    ChirpError error = chirp.send(payload);
                    Log.d(TAG, "onClick: identifier is " + identifier);
                    if (error.getCode() > 0) {
                        Log.d(TAG, error.getMessage());
                    } else {
                        Log.d(TAG, "Sent " + identifier);
                    }
                } else {
                    if (!select.equals("z")) {
                        Log.d(TAG, "onClick: identifier is "+identifier);
                        Toast.makeText(Emergency.this, "Network Available, sending text and Firebase.", Toast.LENGTH_SHORT).show();
                        SmsManager smsManager = SmsManager.getDefault();
                        String number = PhDirectory.get(identifier.charAt(0));
                        mlat = identifier.substring(1, 4);
                        mlong = identifier.substring(4, 7);
                        Toast.makeText(Emergency.this, "lat is " + mlat + "long is " + mlong, Toast.LENGTH_SHORT).show();
                        smsManager.sendTextMessage(number, null, "I am sending my location for precaution for my safety. lat = 23.17" + mlat + " and long= 80.02" + mLong, null, null);
//                        Log.d(TAG, "I am sending my location for precaution for my safety. lat = 23.17" + mlat + " and long= 80.02"+ mlong );
                    }
                }
            }
        });
    }

//    @Override
//    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//        switch (parent.getId()) {
//            case R.id.spinnerEmergency: {
//                if (parent.getItemAtPosition(position).toString().equals("Other"))
//                    select = "z";
//                else {
//                    select = parent.getItemAtPosition(position).toString().charAt(0) + "";
//                    fusedLocationClient.getLastLocation()
//                            .addOnSuccessListener(this, new OnSuccessListener<Location>() {
//                                @Override
//                                public void onSuccess(Location location) {
//                                    // Got last known location. In some rare situations this can be null.
//                                    if (location != null) {
//                                        // Logic to handle location object
//                                        mlat=location.getLatitude()+"";
//                                        mlong=location.getLongitude()+"";
//                                    }
//                                }
//                            });
//                }
//                break;
//            }
//        }
//    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, RESULT_REQUEST_RECORD_AUDIO);
        } else {
            // Start ChirpSDK sender and receiver, if no arguments are passed both sender and receiver are started
            ChirpError error = chirp.start(true, true);
            if (error.getCode() > 0) {
                Log.e("ChirpError: ", error.getMessage());
            } else {
                Log.v("ChirpSDK: ", "Started ChirpSDK");
            }
        }
    }

    public void enableSubmitIfReady() {
        ETmsg = findViewById(R.id.ETmsg);
        BTNsend = findViewById(R.id.BTNsend);
        boolean isReady = ETmsg.getText().toString().length() > 0;
        if (!select.equals("z"))
            isReady = true;
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
