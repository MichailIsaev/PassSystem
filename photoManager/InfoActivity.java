package com.example.michail.photomanager;

import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Активити для отображения информации о клиенте , полученная после запроса к серверу
 */

public class InfoActivity extends AppCompatActivity {
    private ImageButton imageButton, infoButton, exitButton;
    private EditText editText, commentText;
    private ImageView imageView;
    private byte[] photo;
    private String id;
    private Bitmap bmp;
    private boolean switchInfo = false;
    private Context appContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);
        editText = (EditText) findViewById(R.id.editText);
        commentText = (EditText) findViewById(R.id.editText2);
        imageView = (ImageView) findViewById(R.id.imageView);
        imageButton = (ImageButton) findViewById(R.id.goBack);
       // exitButton = (ImageButton) findViewById(R.id.imageButton5);
        infoButton = (ImageButton) findViewById(R.id.infoButton);
        final Intent intent = new Intent(InfoActivity.this, MainActivity.class);
        appContext = this.getApplicationContext();
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(intent);
            }
        });
        infoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (switchInfo == false) {
                    imageView.setVisibility(View.INVISIBLE);
                    switchInfo = true;
                    editText.setVisibility(View.VISIBLE);
                    commentText.setVisibility(View.VISIBLE);
                } else {
                    imageView.setVisibility(View.VISIBLE);
                    switchInfo = false;
                    editText.setVisibility(View.INVISIBLE);
                    commentText.setVisibility(View.INVISIBLE);
                }
            }
        });

        /*exitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentManager fragmentManager = getFragmentManager();
                ExitDialogFragment exit = new ExitDialogFragment();
                exit.show(fragmentManager, "dialog");
            }
        });*/

        Intent getIntent = getIntent();
        id = getIntent.getStringExtra("id");
        Log.i("TAG", id);
        new MyTask().execute();
    }

    Gson gson = new GsonBuilder().create();
    public Clients c;

    /**
     * Асинхронно (не в главном потоке приложения) делаем запрос к серверу , парсим полученную информацию и отображаем ее на нашем активити
     */

    public class MyTask extends AsyncTask<Void, Void, Void> {
        private String DATA = "";

        @Override
        protected Void doInBackground(Void... arg0) {

            OkHttpClient client = new OkHttpClient();
            Request.Builder builder = new Request.Builder();
            builder.url("http://10.60.3.146:8080/person?id=" + id);
            Request request = builder.build();
            Response response = null;
            try {
                response = client.newCall(request).execute();
                DATA = response.body().string();
            } catch (IOException e) {
                e.printStackTrace();
            }
            c = gson.fromJson(DATA, Clients.class);
            Log.i("TAG", String.valueOf(c.getName()));


            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            try {
                editText.setText(c.getName());
                commentText.setText(c.getComment());
                photo = Base64.decode(c.getPhoto(), 0);
                bmp = BitmapFactory.decodeByteArray(photo, 0, photo.length);
                imageView.setImageBitmap(bmp);
                if (Integer.parseInt(c.getId()) < 0) {
                    Log.i("hey", "i here");
                    imageButton.callOnClick();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(appContext, "Допуск запрещен", Toast.LENGTH_LONG);
                        }

                    });
                }
            } catch (Exception e) {
                Toast.makeText(getApplicationContext(), "Нет соединения с БД", Toast.LENGTH_LONG);
                imageButton.callOnClick();
            }
            super.onPostExecute(aVoid);
        }
    }

}
