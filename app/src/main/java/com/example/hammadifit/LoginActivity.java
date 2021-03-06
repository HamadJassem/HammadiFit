package com.example.hammadifit;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener{

    EditText email_et, password_et;
    Button btnLogin;
    TextView register_tv;

    FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        email_et = findViewById(R.id.editTextUsername);
        password_et = findViewById(R.id.editTextPassword);
        btnLogin = findViewById(R.id.buttonLogIn);
        register_tv = findViewById(R.id.textViewRegister);

        mAuth = FirebaseAuth.getInstance();

        btnLogin.setOnClickListener(this);
        register_tv.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId())
        {
            case R.id.buttonLogIn:
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
                    //uses fire base authentication to sign in
                    mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful())
                            {
                                Toast.makeText(getApplicationContext(), "successfully login", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                            }
                            else
                            {
                                AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);

                                builder.setTitle("Failed To Sign In");
                                builder.setMessage("Invalid Email or Password");
                                builder.setPositiveButton("OK", null);
                                builder.show();
                            }
                        }
                    });
                }
                break;
            case R.id.textViewRegister:
                startActivity(new Intent(this, RegisterActivity.class));
                break;
        }
    }
}