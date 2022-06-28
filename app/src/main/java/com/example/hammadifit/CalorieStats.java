package com.example.hammadifit;

import static java.util.Calendar.DAY_OF_MONTH;
import static java.util.Calendar.DAY_OF_WEEK;
import static java.util.TimeZone.LONG;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Pair;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.DefaultValueFormatter;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Locale;

// https://www.youtube.com/watch?v=S3zqxVoIUig
// Activity for displaying charts (pie chart and bar chart for weekly and daily intake)
public class CalorieStats extends AppCompatActivity {

    PieChart FoodPie;
    DatabaseConnector db;
    BarChart FoodBar;
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calorie_stats);

        FoodPie = findViewById(R.id.calorieStatsPieChart);
        FoodBar = findViewById(R.id.calorieStatsBarChart);

        db = new DatabaseConnector(this);
        // creates three arraylists, one for pie chart values, barchart y values, and barchart x values
        ArrayList<PieEntry> entries = new ArrayList<>();
        ArrayList<BarEntry> barEntries = new ArrayList<>();
        ArrayList<String> BarXValues = new ArrayList<>();
        //populating Pie Chart Entries
        ArrayList<Pair<String, Double>> items = db.getAggregatedCaloriesToday(getIntent().getStringExtra("UID"));
        double sum = 0;
        for(Pair<String, Double> item : items)
        {
            sum += item.second; //calculating sum
        }
        for(Pair<String, Double> item : items)
        {
            entries.add(new PieEntry((float)(item.second / sum), item.first)); //calculating proportion from sum
        }
        // populating bar chart entries
        items = db.getAggregatedCaloriesWeekly(getIntent().getStringExtra("UID"));


        //reading from reverse because the given order is descending, but we want it to show from beginning to end.
        Collections.reverse(items);
        int i = 0;
        for(Pair<String, Double> item : items)
        {
            barEntries.add(new BarEntry(i++, Math.round(item.second)));
            BarXValues.add(LocalDate.parse(item.first).getDayOfWeek().toString());
        }
        // colors for pie chart
        ArrayList<Integer> colors = new ArrayList<>();
        for(int color : ColorTemplate.MATERIAL_COLORS)
            colors.add(color);
        for(int color : ColorTemplate.VORDIPLOM_COLORS)
            colors.add(color);
        //giving title for pie chart and setting formatting for the pie chart
        PieDataSet dataSet = new PieDataSet(entries, "Food Category");
        dataSet.setColors(colors);
        PieData data = new PieData(dataSet);
        data.setDrawValues(true);
        data.setValueFormatter(new PercentFormatter(FoodPie));
        data.setValueTextSize(18f);
        data.setValueTextColor(Color.BLACK);

        //populating bar chart
        BarDataSet barset = new BarDataSet(barEntries, "Weekly Calories");
        BarData bardata = new BarData();
        bardata.addDataSet(barset);

        //populating pie chart by setting the foodpie view dataset to the one we created above (data)
        FoodPie.setData(data);
        Description pieDescr = new Description();
        pieDescr.setText("Daily Intake Category");
        FoodPie.setDescription(pieDescr);
        FoodPie.invalidate();
        FoodPie.animateY(1500, Easing.EaseInQuad);

        //.... similar as above
        FoodBar.setData(bardata);
        FoodBar.invalidate();
        FoodBar.getXAxis().setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) { //formatting x values for bar chart, a bit buggy because of the library
                return BarXValues.get((int) value);
            }
        });

        Description barDescr = new Description();
        pieDescr.setText("Weekly Intake Distribution"); //for some reasons it doesnt show (BUG)
        FoodPie.setDescription(barDescr);
        FoodBar.animateY(1500, Easing.EaseInQuad);


    }
}