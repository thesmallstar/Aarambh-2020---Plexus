package com.kinshuu.plexus;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.telecom.Call;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;


import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class PublicEmergency extends AppCompatActivity {

    EditText title,description;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        int SDK_INT = android.os.Build.VERSION.SDK_INT;
        if (SDK_INT > 8)
        {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                    .permitAll().build();
            StrictMode.setThreadPolicy(policy);
            //your codes here

        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_public_emergency);
        title = (EditText)findViewById(R.id.title);
        Intent intent = getIntent();
        description = (EditText)findViewById(R.id.des);
        Button submitButton = (Button) findViewById(R.id.submit);
        Toast t = Toast.makeText(this.getApplicationContext(), "Public Emergency", Toast.LENGTH_SHORT);
        t.show();
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tryToPost();
            }
        });

    }
    boolean isEmpty(EditText text) {
        CharSequence str = text.getText().toString();
        return TextUtils.isEmpty(str);
    }
    void tryToPost() {

        if (isEmpty(title)) {
            title.setError("You must enter description");
            return;
        }

        if (isEmpty(description)) {
            description.setError("You must enter description");
            return;
        }

        tryPost();

    }

    void tryPost(){

        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        //   String rname = name.getText().toString();
        String titles = title.getText().toString();
        String des = description.getText().toString();
        String url = "http://172.27.49.95/emergency/emergency/public/addPost";


        RequestBody body = new FormBody.Builder()
                .add("title", titles)
                .add("description", des)
                .add("name",MainActivity.mUsername)
                .add("email",MainActivity.mUserEmail)
                .build();

        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        Toast.makeText(getApplicationContext(), "Trying to Send Public Emergency", Toast.LENGTH_LONG).show();
        //Call call = client.newCall(request);

        client.newCall(request).enqueue(new Callback(){

            @Override
            public void onResponse(@NotNull okhttp3.Call call, @NotNull final Response response) throws IOException {


                PublicEmergency.this.runOnUiThread(new Runnable() {

                    @Override
                    public void run() {


                        try {
                            carryPostahead(response.body().string());
                        } catch (IOException e) {

                            e.printStackTrace();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });

            }
            @Override
            public void onFailure(@NotNull okhttp3.Call call, @NotNull IOException e) {

                e.printStackTrace();
            }
        });


    }
    private void carryPostahead(String r) throws JSONException {

        Toast t = Toast.makeText(this.getApplicationContext(), r, Toast.LENGTH_LONG);
        t.show();
       return;
    }

}
