package com.example.hammadifit;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

public class CalorieActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calorie);
        /*
        * Mifflin-St Jeor Equation:
        *    For men:
        *    BMR = 10W + 6.25H - 5A + 5
        *    For women:
        *    BMR = 10W + 6.25H - 5A - 161
        *
        * */
        if(getIntent().getBooleanExtra("gender", true) == true)
        {
            int A = getIntent().getIntExtra("age", 0);
            float H = getIntent().getFloatExtra("height", 0f);
            float W = getIntent().getFloatExtra("weight", 0f);
            double BMR = 10 * W + 6.25 * H - 5*A + 5;
            ((TextView)(findViewById(R.id.textViewTotalCalories))).setText(""+BMR);
        }
        else
        {
            int A = getIntent().getIntExtra("age", 0);
            float H = getIntent().getFloatExtra("height", 0f);
            float W = getIntent().getFloatExtra("weight", 0f);
            double BMR = 10 * W + 6.25 * H - 5*A - 161;
            ((TextView)(findViewById(R.id.textViewTotalCalories))).setText(""+BMR);
        }
    }
}