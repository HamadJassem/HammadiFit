package com.example.hammadifit;

import android.content.Context;

import java.io.IOException;
import java.io.InputStream;
//https://stackoverflow.com/questions/19945411/how-can-i-parse-a-local-json-file-from-assets-folder-into-a-listview

public class JsonReader {
    private Context ctx;
    JsonReader(Context ctx){this.ctx = ctx;}

    public String loadJSONFromAsset() {
        String json = null;
        try {
            InputStream is = ctx.getAssets().open("exercises.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }
}
