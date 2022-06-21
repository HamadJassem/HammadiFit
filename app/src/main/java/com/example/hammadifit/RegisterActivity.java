package com.example.hammadifit;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

// //  https://firebase.google.com/docs/database/android/read-and-write
// https://stackoverflow.com/questions/61836337/how-to-check-if-a-key-exists-in-firebase-database

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener {

    EditText email_et, password_et, username_et, height_et, weight_et, age_et;
    Button btnRegister;
    TextView signin_tv;
    RadioButton rbmale, rbfemale;

    private DatabaseReference mDatabase;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        email_et = findViewById(R.id.editTextEmailRegister);
        password_et = findViewById(R.id.editTextPasswordRegister);
        btnRegister = findViewById(R.id.buttonRegister);
        signin_tv = findViewById(R.id.textViewSignUp);
        username_et = findViewById(R.id.editTextUsernameRegister);
        height_et = findViewById(R.id.editTextHeightRegister);
        weight_et = findViewById(R.id.editTextWeightRegister);
        rbmale = findViewById(R.id.radioButtonMaleRegister);
        rbfemale = findViewById(R.id.radioButtonFemaleRegister);
        age_et = findViewById(R.id.editTextAgeRegister);


        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        btnRegister.setOnClickListener(this);
        signin_tv.setOnClickListener(this);
    }
    public void writeNewUser(String email, String username, boolean gender, float height, float weight, int age) {
        User user = new User(email, username, gender, height, weight, age);
        mDatabase.child("users").child(username).setValue(user);
    }
    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.buttonRegister)
        {
            String email = email_et.getText().toString();
            String password = password_et.getText().toString();
            String username = username_et.getText().toString();
            String height = height_et.getText().toString();
            String weight = weight_et.getText().toString();
            String age = age_et.getText().toString();
            boolean gender = (rbmale.isChecked() == true ? true : false);
            if(email.trim().isEmpty())
            {
                email_et.setError("Fill this field please");
                email_et.requestFocus();
            }
            else if(password.trim().isEmpty())
            {
                password_et.setError("Fill this field please");
                password_et.requestFocus();
            }
            else if(username.trim().isEmpty())
            {
                username_et.setError("Fill this field please");
                username_et.requestFocus();
            }
            else if(height.trim().isEmpty())
            {
                height_et.setError("Fill this field please");
                height_et.requestFocus();
            }
            else if(weight.trim().isEmpty())
            {
                weight_et.setError("Fill this field please");
                weight_et.requestFocus();
            }
            else if(age.trim().isEmpty())
            {
                age_et.setError("Fill this field please");
                age_et.requestFocus();
            }
            else
            {
                try
                { //TODO add validation for Existing user and email
                    float heightF = Float.parseFloat(height);
                    float weightF = Float.parseFloat(weight);
                    int ageI = Integer.parseInt(age);
                    if(ageI < 0 || heightF < 0 || weightF < 0)
                    {
                        throw new Exception();
                    }
                    mDatabase.child("users").child(username).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if(!snapshot.exists()) // check if username does not exist
                            {
                                mAuth.createUserWithEmailAndPassword(email, password)
                                        .addOnCompleteListener(RegisterActivity.this, new OnCompleteListener<AuthResult>() {
                                            @Override
                                            public void onComplete(@NonNull Task<AuthResult> task) {
                                                if (task.isSuccessful()) {

                                                    Toast.makeText(RegisterActivity.this, "Authentication Passed.",
                                                            Toast.LENGTH_SHORT).show();

                                                    writeNewUser(email, username, gender, heightF, weightF, ageI);

                                                    mDatabase.child("uids").child(mAuth.getCurrentUser().getUid()).setValue(username);


                                                    Intent loginActivity = new Intent(getApplicationContext(), LoginActivity.class);
                                                    startActivity(loginActivity);

                                                } else {


                                                    Toast.makeText(RegisterActivity.this, "Authentication failed.",
                                                            Toast.LENGTH_SHORT).show();

                                                }
                                            }
                                        });
                            }
                            else
                            {
                                username_et.setError("Username Exists");
                                username_et.requestFocus();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });


                }
                catch (Exception e)
                {
                    Toast.makeText(getApplicationContext(), "Invalid Input in either of Height, Weight, Age..", Toast.LENGTH_SHORT).show();
                }

            }
        }
        else if(v.getId() == R.id.textViewSignUp)
        {
            Intent login = new Intent(this, LoginActivity.class);
            startActivity(login);
        }
    }
}