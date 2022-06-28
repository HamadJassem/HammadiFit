package com.example.hammadifit;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Pair;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;

public class ProfileActivity extends AppCompatActivity {

    TextView username_tv, weight_tv, height_tv;
    ImageButton Image_back;
    DatabaseConnector db;
    BarChart distanceBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        db = new DatabaseConnector(this);


        username_tv = findViewById(R.id.textViewUsernameProfile);
        weight_tv = findViewById(R.id.textViewWeightProfile);
        height_tv = findViewById(R.id.textViewHeightProfile);
        Image_back = findViewById(R.id.imageButtonBackProfile);
        distanceBar = findViewById(R.id.StepsBarChart);

        username_tv.setText(getIntent().getStringExtra("username"));
        weight_tv.setText(getIntent().getFloatExtra("weight", 0f)+"kg");
        height_tv.setText(""+getIntent().getFloatExtra("height", 0f)+"cm");

        ArrayList<BarEntry> barEntries = new ArrayList<>();
        ArrayList<String> BarXValues = new ArrayList<>();

        ArrayList<Pair<String, Integer>> items = db.getDistanceWeeklyInArrayList(getIntent().getStringExtra("UID"));


        //reading from reverse because the given order is descending
        Collections.reverse(items);
        int i = 0;
        for(Pair<String, Integer> item : items)
        {
            barEntries.add(new BarEntry(i++, item.second));
            BarXValues.add(LocalDate.parse(item.first).getDayOfWeek().toString());
        }

        // code snippet for bar chart
        BarDataSet barset = new BarDataSet(barEntries, "Weekly Steps");
        BarData bardata = new BarData();
        bardata.addDataSet(barset);

        distanceBar.setData(bardata);
        distanceBar.invalidate();
        distanceBar.getXAxis().setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return BarXValues.get((int) value);
            }
        });

        distanceBar.animateY(1500, Easing.EaseInQuad);

        Image_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}