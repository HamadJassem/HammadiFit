package com.example.hammadifit;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener {

    EditText email_et, password_et;
    Button btnRegister;
    TextView signin_tv;

    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        email_et = findViewById(R.id.editTextUsernameRegister);
        password_et = findViewById(R.id.editTextPasswordRegister);
        btnRegister = findViewById(R.id.buttonRegister);
        signin_tv = findViewById(R.id.textViewSignUp);

        mAuth = FirebaseAuth.getInstance();

        btnRegister.setOnClickListener(this);
        signin_tv.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.buttonRegister)
        {
            String email = email_et.getText().toString();
            String password = password_et.getText().toString();
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
            else
            {
                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {

                                    Toast.makeText(RegisterActivity.this, "Authentication Passed.",
                                            Toast.LENGTH_SHORT).show();
                                    Intent loginActivity = new Intent(getApplicationContext(), LoginActivity.class);
                                    startActivity(loginActivity);

                                } else {


                                    Toast.makeText(RegisterActivity.this, "Authentication failed.",
                                            Toast.LENGTH_SHORT).show();

                                }
                            }
                        });
            }
        }
        else if(v.getId() == R.id.textViewSignUp)
        {
            Intent login = new Intent(this, LoginActivity.class);
            startActivity(login);
        }
    }
}