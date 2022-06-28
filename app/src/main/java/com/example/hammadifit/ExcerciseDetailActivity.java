package com.example.hammadifit;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

// sources for workouts : https://github.com/wrkout/exercises.json

public class ExcerciseDetailActivity extends AppCompatActivity {

    ImageView im1, im2;
    TextView title_tv, description_tv;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_excercise_detail);

        String link = "https://raw.githubusercontent.com/wrkout/exercises.json/master/exercises/"+getIntent().getStringExtra("link")+"/images/";
        String pic0 = link+"0.jpg";
        String pic1 = link+"1.jpg";

        im1 = findViewById(R.id.imageViewFirstExcercise);
        im2 = findViewById(R.id.imageViewSecondExcercise);
        title_tv = findViewById(R.id.TextViewExcerciseTitle);
        description_tv = findViewById(R.id.textViewExcerciseDescription);

        title_tv.setText(getIntent().getStringExtra("name"));
        description_tv.setText(getIntent().getStringExtra("description"));

        // runs two threads to download the pictures on parallel
        new  picDownloadTask(getIntent().getStringExtra("link")+"0.jpg", im1).execute(pic0);
        new  picDownloadTask(getIntent().getStringExtra("link")+"1.jpg", im2).execute(pic1);

    }

    // reference from rss feed FILEIO class, but modified to download pictures
    class picDownloadTask extends AsyncTask<String, Void, String>
    {
        picDownloadTask(String FILENAME, ImageView im){this.filename = FILENAME; this.im = im;}
        String filename;
        Bitmap pic;
        ImageView im;
        @Override
        protected String doInBackground(String... strings) {
            try{
                // get the URL
                try {
                    InputStream check = getApplicationContext().openFileInput(filename);
                } catch(FileNotFoundException e)
                {
                    URL url = new URL(strings[0]);

                    // get the input stream
                    InputStream in = url.openStream();

                    // get the output stream
                    FileOutputStream out =
                            getApplicationContext().openFileOutput(filename, Context.MODE_PRIVATE);

                    // read input and write output
                    byte[] buffer = new byte[1024];
                    int bytesRead = in.read(buffer);
                    while (bytesRead != -1)
                    {
                        out.write(buffer, 0, bytesRead);
                        bytesRead = in.read(buffer);
                    }
                    out.close();
                    in.close();
                }
            }
            catch (IOException e) {

            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            try {
                //gets stream, converts to bitmap, then assigns image view the picture
                InputStream in = getApplicationContext().openFileInput(filename);
                pic = BitmapFactory.decodeStream(in);
                im.setImageBitmap(pic);
                im.setVisibility(View.VISIBLE);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

        }
    }
}