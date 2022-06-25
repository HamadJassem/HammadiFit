package com.example.hammadifit;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class WorkoutActivity extends AppCompatActivity {

    CardView cardBeginner, cardIntermediate, cardExpert;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workout);

        cardBeginner = findViewById(R.id.CardBeginner);
        cardIntermediate = findViewById(R.id.CardIntermediate);
        cardExpert = findViewById(R.id.CardExpert);

        cardBeginner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(WorkoutActivity.this, ExcerciseListActivity.class);
                intent.putExtra("difficulty", "beginner");
                startActivity(intent);
            }
        });
        cardIntermediate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(WorkoutActivity.this, ExcerciseListActivity.class);
                intent.putExtra("difficulty", "intermediate");
                startActivity(intent);
            }
        });
        cardExpert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(WorkoutActivity.this, ExcerciseListActivity.class);
                intent.putExtra("difficulty", "expert");
                startActivity(intent);
            }
        });




    }

}