package com.example.hammadifit;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    String userId = "";
    private User loggedUser = null;
    FirebaseUser currentUser = null;
    TextView msg_tv;
    ProgressBar pbHome;



    CardView buttonWorkout, buttonCalorie, buttonSteps;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        msg_tv = findViewById(R.id.TextViewNameUser);
        pbHome = findViewById(R.id.progressBarHome);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        buttonCalorie = findViewById(R.id.CardCalorie);
        buttonWorkout = findViewById(R.id.CardWorkout);
        buttonSteps = findViewById(R.id.CardWalk);

        buttonCalorie.setOnClickListener(this);
        buttonWorkout.setOnClickListener(this);
        buttonSteps.setOnClickListener(this);







    }

    @Override
    protected void onStart() {
        super.onStart();
         currentUser = mAuth.getCurrentUser();

        if(currentUser == null)
        {
            Intent login = new Intent(this, LoginActivity.class);
            startActivity(login);
        }
        else
        {
            mDatabase.child("uids").child(currentUser.getUid()).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DataSnapshot> task) {
                    if (!task.isSuccessful()) {
                        Log.e("firebase", "Error getting data", task.getException());
                    }
                    else {
                        userId = task.getResult().getValue().toString();
                    }
                }
            });
            mDatabase.child("users").child(userId).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DataSnapshot> task) {
                    if (!task.isSuccessful()) {
                        Log.e("firebase", "Error getting data", task.getException());
                    }
                    else {
                       // loggedUser = task.getResult().getValue(User.class);
                        loggedUser = task.getResult().child(userId).getValue(User.class);
                        if(loggedUser == null) //weird bug when backing off from a page to home.
                            loggedUser = task.getResult().getValue(User.class);

                      updateUI();
                    }
                }
            });


        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(
                R.menu.home_menu, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.ItemLogout:
                if(currentUser != null)
                {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);

                    builder.setTitle("Are You Sure?");
                    builder.setMessage("Do You Want to Sign Out Of Your Account?");
                    builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            mAuth.signOut();
                            Intent login = new Intent(MainActivity.this, LoginActivity.class);
                            startActivity(login);
                        }
                    });
                    builder.setNegativeButton("No", null);
                    builder.show();


                }
                return true;
            case R.id.ItemProfile:
                if(loggedUser != null)
                {
                    Intent profile = new Intent(this, ProfileActivity.class);
                    profile.putExtra("username", loggedUser.username);
                    profile.putExtra("gender", loggedUser.gender);
                    profile.putExtra("height", loggedUser.height);
                    profile.putExtra("weight", loggedUser.weight);
                    startActivity(profile);
                }
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    private void updateUI() {
        try { //try catch needed in case loggedUser is null.
            msg_tv.setText("Hello, " + loggedUser.username);
            pbHome.setVisibility(View.GONE);

        }
        catch (Exception e)
        {
            // do nothing;
        }

    }

    @Override
    public void onClick(View v) {
        switch(v.getId())
        {
            case R.id.CardWalk:
                break;
            case R.id.CardWorkout:
                break;
            case R.id.CardCalorie:
                if(loggedUser != null)
                {
                    Intent CalorieAct = new Intent(this, CalorieActivity.class);
                    CalorieAct.putExtra("username", loggedUser.username);
                    CalorieAct.putExtra("gender", loggedUser.gender);
                    CalorieAct.putExtra("height", loggedUser.height);
                    CalorieAct.putExtra("weight", loggedUser.weight);
                    CalorieAct.putExtra("age", loggedUser.age);
                    CalorieAct.putExtra("UID", mAuth.getUid());
                    startActivity(CalorieAct);
                }
                else
                {
                    Toast.makeText(this, "wait", Toast.LENGTH_SHORT).show();
                }


                break;
        }
    }
}