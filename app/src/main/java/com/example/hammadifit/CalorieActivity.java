package com.example.hammadifit;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.sql.Time;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

//inspired from https://www.calculator.net/bmr-calculator.html
public class CalorieActivity extends AppCompatActivity implements View.OnClickListener {

    Button btnAddCalorie;
    DatabaseConnector db;
    EditText fat_et, protein_et, carbs_et, food_name_et;
    ProgressBar pb;
    TextView consumed_tv;
    String UID;
    SharedPreferences formValues_sp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calorie);

        consumed_tv = findViewById(R.id.textViewCaloriesConsumed);

        btnAddCalorie = findViewById(R.id.buttonAddDish);
        btnAddCalorie.setOnClickListener(this);

        pb = findViewById(R.id.progressBarCaloriesFull);

        fat_et = findViewById(R.id.editTextFat);
        protein_et = findViewById(R.id.editTextProteins);
        carbs_et = findViewById(R.id.editTextCarbs);
        food_name_et = findViewById(R.id.editTextDishName);

        UID = getIntent().getStringExtra("UID");

        db = new DatabaseConnector(this);

        formValues_sp = getSharedPreferences("formvalues", MODE_PRIVATE);


    }

    protected void onStart() {
        super.onStart();
        updateUI(); //incase user backs off from history after deleting an item...
    }
    @Override
    protected void onResume() {
        // load fat, protein, carb and food name values from a shared preference
        super.onResume();
        fat_et.setText(formValues_sp.getString("fat", ""));
        protein_et.setText(formValues_sp.getString("protein", ""));
        carbs_et.setText(formValues_sp.getString("carbs", ""));
        food_name_et.setText(formValues_sp.getString("food", ""));
    }

    @Override
    protected void onPause() {
        // save fat, protein, carb and food name values in a shared preference
        super.onPause();
        SharedPreferences.Editor editor = formValues_sp.edit();
        editor.putString("fat", fat_et.getText().toString());
        editor.putString("protein", protein_et.getText().toString());
        editor.putString("carbs", carbs_et.getText().toString());
        editor.putString("food", food_name_et.getText().toString());
        editor.commit();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.buttonAddDish) {
            try {
                //check if all fields are working fine without errors in a try catch block for implicit handling
                float fatV = Float.parseFloat(fat_et.getText().toString());
                float proteinV = Float.parseFloat(protein_et.getText().toString());
                float carbsV = Float.parseFloat(carbs_et.getText().toString());
                String foodName = food_name_et.getText().toString();
                double calories = 9*fatV + 4*proteinV + 4*carbsV; // calculation of the calories
                //insert to database if successfully calculated
                db.insertItem(foodName, calories, getIntent().getStringExtra("UID"), System.currentTimeMillis());
                updateUI();
            }
            catch (Exception e)
            {
                Toast.makeText(this, "Issue While Adding Meal... Check Inputs", Toast.LENGTH_SHORT).show();
            }

        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.calorie_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId())
        {
            case R.id.CalorieItemHistory:
                Intent history = new Intent(this, CalorieHistory.class);
                history.putExtra("UID", UID);
                startActivity(history);
                return true;
            case R.id.CalorieItemInfo:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Information About This Page");
                String message = "This Page Allows you to calculate the calories of any dish from fats, proteins, and carbohydrates. \n The formula for getting any calorie is 4 * Proteins + 4 * Carbohydrates + 9 * Fats, considering That all units are in grams... \n\n Each Day You Have To Eat Around " + getBMR() + " Calories";
                builder.setMessage(message);
                builder.setPositiveButton("Got It!", null);
                builder.show();
                return true;
            case R.id.CalorieItemStats:
                Intent stats = new Intent(this, CalorieStats.class);
                stats.putExtra("UID", UID);
                startActivity(stats);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    public double getBMR()
    {
        double BMR;
        //calculates the required calories to eat per day for both genders
        // gender = true for male
        // gender = false for female
        if(getIntent().getBooleanExtra("gender", true) == true)
        {
            int A = getIntent().getIntExtra("age", 0);
            float H = getIntent().getFloatExtra("height", 0f);
            float W = getIntent().getFloatExtra("weight", 0f);
            BMR = 10 * W + 6.25 * H - 5*A + 5;

        }
        else
        {
            int A = getIntent().getIntExtra("age", 0);
            float H = getIntent().getFloatExtra("height", 0f);
            float W = getIntent().getFloatExtra("weight", 0f);
            BMR = 10 * W + 6.25 * H - 5*A - 161;
        }
        return BMR;
    }

    private void updateUI()
    {
        //gets calories for individual users by UID
        ArrayList<Calorie> arr = db.getCaloriesToday(getIntent().getStringExtra("UID"));
        double sum = 0;
        for(Calorie i : arr)
        {
            sum += i.getCalorie();
        }
        consumed_tv.setText(""+sum + " Calories Consumed");

        /*
         * Mifflin-St Jeor Equation:
         *    For men:
         *    BMR = 10W + 6.25H - 5A + 5
         *    For women:
         *    BMR = 10W + 6.25H - 5A - 161
         *
         * */
        double BMR = getBMR();


        //calculates proportion left and displays it in progress bar
        // if it exceeds the maximum intake, it will change the progress bar color to red
        ((TextView)(findViewById(R.id.textViewTotalCalories))).setText(""+( (BMR-sum>=0) ? (BMR-sum) : 0 ) + " Calories Needed");

        pb.setProgress((int)((sum/BMR)*100));
        if(sum > BMR)
        {
            pb.getProgressDrawable().setColorFilter(Color.RED, android.graphics.PorterDuff.Mode.SRC_IN);
        }
        else
        {
            pb.getProgressDrawable().setColorFilter(Color.GREEN, android.graphics.PorterDuff.Mode.SRC_IN);
        }

    }
}