package com.example.hammadifit;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

public class ProfileActivity extends AppCompatActivity {

    TextView username_tv, weight_tv, height_tv;
    ImageButton Image_back;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        username_tv = findViewById(R.id.textViewUsernameProfile);
        weight_tv = findViewById(R.id.textViewWeightProfile);
        height_tv = findViewById(R.id.textViewHeightProfile);
        Image_back = findViewById(R.id.imageButtonBackProfile);

        username_tv.setText(getIntent().getStringExtra("username"));
        weight_tv.setText(getIntent().getFloatExtra("weight", 0f)+"kg");
        height_tv.setText(""+getIntent().getFloatExtra("height", 0f)+"cm");

        Image_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}