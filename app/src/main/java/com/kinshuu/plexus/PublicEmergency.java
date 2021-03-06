//
package com.kinshuu.plexus;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.telecom.Call;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;


import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class PublicEmergency extends AppCompatActivity {

    EditText title,description;
    Bitmap bitmap=null;
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //Detects request codes
        if(requestCode==1 && resultCode == Activity.RESULT_OK) {
            Uri selectedImage = data.getData();
             bitmap = null;
            try {
                bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImage);
                Toast t = Toast.makeText(this.getApplicationContext(), "Image Has been added", Toast.LENGTH_SHORT);
                t.show();
            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setLogo(R.drawable.ic_navbar);
        Intent intent = new Intent();




        actionBar.setDisplayUseLogoEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setBackgroundDrawable(new ColorDrawable(0));
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
        Intent intents = getIntent();
        description = (EditText)findViewById(R.id.des);
        Button submitButton = (Button) findViewById(R.id.submit);
        Button addPhoto = (Button) findViewById(R.id.addPhoto);
//        Toast t = Toast.makeText(this.getApplicationContext(), "Public Emergency", Toast.LENGTH_SHORT);
//        t.show();

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tryToPost();
            }
        });

        addPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI), 1);
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
    private static final MediaType MEDIA_TYPE_PNG = MediaType.parse("image/png");
    void tryPost(){

        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        //   String rname = name.getText().toString();
        String titles = title.getText().toString();
        String des = description.getText().toString();
        String url = "http://172.27.49.95/emergency/emergency/public/addPost";


//        RequestBody body = new FormBody.Builder()
//                .add("title", titles)
//                .add("description", des)
//                .add("name",MainActivity.mUsername)
//                .add("email",MainActivity.mUserEmail)
//                .build();

        RequestBody body;

if(bitmap==null){
     body = new MultipartBody.Builder().setType(MultipartBody.FORM)
            .addFormDataPart("title", titles)
            .addFormDataPart("description", des)
            .addFormDataPart("name", MainActivity.mUsername)
            .addFormDataPart("email", MainActivity.mUserEmail)
            .build();
}else {
    ByteArrayOutputStream stream = new ByteArrayOutputStream();
    bitmap.compress(Bitmap.CompressFormat.JPEG, 1, stream);
    final byte[] bitmapdata = stream.toByteArray();

     body = new MultipartBody.Builder().setType(MultipartBody.FORM)
            .addFormDataPart("image", "filename.jpeg",
                    RequestBody.create(MediaType.parse("image/*jpeg"), bitmapdata))
            .addFormDataPart("title", titles)
            .addFormDataPart("description", des)
            .addFormDataPart("name", MainActivity.mUsername)
            .addFormDataPart("email", MainActivity.mUserEmail)
            .build();
}
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
