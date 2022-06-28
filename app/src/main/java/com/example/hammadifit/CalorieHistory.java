package com.example.hammadifit;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ListActivity;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

// simple activity to remove and check calories
public class CalorieHistory extends AppCompatActivity {


    private ListView foodListView;
    private CursorAdapter foodAdapter;
    String UID;
    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calorie_history);
        foodListView = findViewById(R.id.FoodCalorieListView);
        foodListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                AlertDialog.Builder builder = new AlertDialog.Builder(CalorieHistory.this);
                builder.setTitle("Are you sure?");
                builder.setMessage("Do You Want To Delete this entry?");
                builder.setNegativeButton("No", null);
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        new AsyncTask<Long, Object, Object>(){
                            DatabaseConnector connector = new DatabaseConnector(CalorieHistory.this);
                            @Override
                            protected Object doInBackground(Long... params) {
                                connector.deleteItem(params[0]);
                                return null;
                            }
                            protected void onPostExecute(Object param)
                            {
                                connector.open();
                                Cursor c = connector.getAllItemsWithoutUID(UID);
                                foodAdapter.changeCursor(c);
                                foodAdapter.notifyDataSetChanged();
                                connector.close();
                            }
                        }.execute(id);
                    }
                });
                builder.show();
            }
        });

        String[] from = new String[] {"name", "calorie", "time"};
        int[] to = new int[] {R.id.textViewFoodNameHistory, R.id.textViewCaloriesHistory, R.id.textViewDateHistory};
        foodAdapter = new SimpleCursorAdapter(this, R.layout.calorie_item, null, from, to);
        UID = getIntent().getStringExtra("UID");

        foodListView.setAdapter(foodAdapter);


    }

    @Override
    protected void onResume()
    {
        super.onResume();
        (new HistoryTask()).execute((Object[])null);
    }

    @Override
    protected void onStop()
    {
        Cursor cursor = foodAdapter.getCursor();
        foodAdapter.changeCursor(null);
        if(cursor != null)
            cursor.close();
        super.onStop();
    }

    class HistoryTask extends AsyncTask<Object, Object, Cursor>
    {
        DatabaseConnector connector = new DatabaseConnector(CalorieHistory.this);
        @Override
        protected Cursor doInBackground(Object... objects) {
            connector.open();
            return connector.getAllItemsWithoutUID(UID);
        }

        @Override
        protected void onPostExecute(Cursor cursor) {
            foodAdapter.changeCursor(cursor);
            connector.close();
        }
    }
}