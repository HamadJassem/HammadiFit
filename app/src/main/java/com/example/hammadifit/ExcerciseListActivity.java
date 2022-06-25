package com.example.hammadifit;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class ExcerciseListActivity extends AppCompatActivity {

    String excerciseType = null;
    ListView excerciseList;
    ArrayList<JSONObject> JSONObjects = null;
    ArrayList<HashMap<String, String>> formList = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_excercise_list);
        excerciseType = getIntent().getStringExtra("difficulty");
        excerciseList = findViewById(R.id.excerciseListView);

        try {
            JsonReader reader = new JsonReader(this);
            JSONArray arry = new JSONArray(reader.loadJSONFromAsset());
            formList = new ArrayList<HashMap<String, String>>();
            HashMap<String, String> map;
            JSONObjects = new ArrayList<>();

            for (int i = 0; i < arry.length(); i++) {

                JSONObject object = arry.getJSONObject(i);
                String key = object.keys().next();
                object = object.getJSONObject(key);

                if(object.getString("level").equals(excerciseType))
                {
                    JSONObjects.add(object);
                    map = new HashMap<>();
                    map.put("name", object.getString("name"));
                    map.put("link", key);
                    JSONArray instructions = object.getJSONArray("instructions");
                    String instructions_str = "";
                    for(int j = 0; j<instructions.length(); j++)
                    {
                        instructions_str += "\n" + instructions.getString(j) + "\n";
                    }
                    map.put("description", instructions_str);
                    formList.add(map);
                }
                String[] from = new String[]{"name"};
                int[] to = new int[]{R.id.textViewWorkoutName};
                SimpleAdapter adapter = new SimpleAdapter(this, formList, R.layout.excercise_item, from, to);
                excerciseList.setAdapter(adapter);

                (excerciseList).setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Intent intent = new Intent(ExcerciseListActivity.this, ExcerciseDetailActivity.class);
                        intent.putExtra("name", formList.get(position).get("name"));
                        intent.putExtra("link", formList.get(position).get("link"));
                        intent.putExtra("description", formList.get(position).get("description"));
                        startActivity(intent);
                    }
                });
            }
        } catch (JSONException e) {
            Log.d("hammadi", e.toString());
        }

    }
}