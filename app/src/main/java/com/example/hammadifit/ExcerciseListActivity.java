package com.example.hammadifit;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.view.MenuItemCompat;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

//json parsing code snippet was taken from stack overflow and modified to work on my dataset, but i lost the link (there are numerous)

public class ExcerciseListActivity extends AppCompatActivity implements SearchView.OnQueryTextListener {

    String excerciseType = null;
    ListView excerciseList;

    ArrayList<JSONObject> JSONObjects = null;
    // the idea for having two arraylists is: form list for having all excercises, queried lists is the filtered one based on keyword search
    ArrayList<HashMap<String, String>> formList = null;
    ArrayList<HashMap<String, String>> queriedList = null;
    SearchView excercise_sv;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_excercise_list);
        excerciseType = getIntent().getStringExtra("difficulty");
        excerciseList = findViewById(R.id.excerciseListView);
        excercise_sv = findViewById(R.id.workOutSearchView);
        excercise_sv.setOnQueryTextListener(this);

        try {
            // creates jsonreader object
            JsonReader reader = new JsonReader(this);
            //the json object starts with an array since its a concatenated list of excercises
            JSONArray arry = new JSONArray(reader.loadJSONFromAsset());
            //creates an array list of hashmaps to populate the listview
            formList = new ArrayList<HashMap<String, String>>();
            HashMap<String, String> map;
            JSONObjects = new ArrayList<>();

            // iterate through all json objects inside the json array
            for (int i = 0; i < arry.length(); i++) {

                JSONObject object = arry.getJSONObject(i); // gets the object
                String key = object.keys().next(); //gets the key
                object = object.getJSONObject(key); //accesses the json object of size 1 through the key.

                // checks if the level of the excercise is equivelant to the one that was choosen in the previous page
                if(object.getString("level").equals(excerciseType))
                {
                    // adds relevant fields for the details page
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

            }
            queriedList = formList; //assigns a reference of form list to the query list
            String[] from = new String[]{"name"}; //shows name of each excercise
            int[] to = new int[]{R.id.textViewWorkoutName};
            SimpleAdapter adapter = new SimpleAdapter(this, formList, R.layout.excercise_item, from, to);
            excerciseList.setAdapter(adapter);

            //  goes to the details page
            (excerciseList).setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Intent intent = new Intent(ExcerciseListActivity.this, ExcerciseDetailActivity.class);
                    intent.putExtra("name", queriedList.get(position).get("name"));
                    intent.putExtra("link", queriedList.get(position).get("link"));
                    intent.putExtra("description", queriedList.get(position).get("description"));
                    startActivity(intent);
                }
            });
        } catch (JSONException e) {
            Log.d("hammadi", e.toString());
        }

    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        String[] from = new String[]{"name"};
        int[] to = new int[]{R.id.textViewWorkoutName};
        if(newText.equals("")) // if text is empty set the adapter to form list
        {
            queriedList = formList;
            SimpleAdapter adapter = new SimpleAdapter(this, queriedList, R.layout.excercise_item, from, to);
            excerciseList.setAdapter(adapter);
        }
        else
        {
            queriedList = new ArrayList<>();
            for(HashMap<String, String> mp : formList)
            {
                if(mp.get("name").toLowerCase().contains(newText.toLowerCase()))
                {
                    queriedList.add(mp);
                }
            }
            SimpleAdapter adapter = new SimpleAdapter(this, queriedList, R.layout.excercise_item, from, to);
            excerciseList.setAdapter(adapter);
        }
        return false;
    }
}