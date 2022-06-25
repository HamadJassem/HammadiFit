package com.example.hammadifit;

import static com.example.hammadifit.FitMapService.FASTEST_UPDATE_INTERVAL;
import static com.example.hammadifit.FitMapService.UPDATE_INTERVAL;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import android.location.Location;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.FusedLocationProviderApi;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;

import java.security.Permission;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

// code snippet used for calculating distance between two points
// https://www.geeksforgeeks.org/program-distance-two-points-earth/
//
// multiple sources for the step counter
//
public class WalkActivity extends AppCompatActivity implements View.OnClickListener, SensorEventListener, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener
{

    Button startWalking, stopWalking;
    TextView distance_tv, steps_tv, timer_tv;
    DatabaseConnector db;
    String UID;
    boolean SessionRunning = false;
    SensorManager sensorManager = null;
    Sensor stepCounter = null;
    SharedPreferences sp_counter;
    private GoogleApiClient googleApiClient;
    private LocationRequest locationRequest;
    private Timer timer = null;

    @Override
    protected void onStart()
    {
        super.onStart();
        googleApiClient.connect();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_walk);
        UID = getIntent().getStringExtra("UID");
        startWalking = findViewById(R.id.buttonStartWalking);
        stopWalking = findViewById(R.id.buttonStopWalking);

        distance_tv = findViewById(R.id.textViewDistance);
        steps_tv = findViewById(R.id.textViewSteps);
        timer_tv = findViewById(R.id.textViewTimer);

        startWalking.setOnClickListener(this);
        stopWalking.setOnClickListener(this);
        startWalking.setEnabled(false);
        stopWalking.setEnabled(false);


        sp_counter = getSharedPreferences("stepcounter", MODE_PRIVATE);

        googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(UPDATE_INTERVAL)
                .setFastestInterval(FASTEST_UPDATE_INTERVAL);

        db = new DatabaseConnector(this);



    }

    @Override
    protected void onResume() {

        super.onResume();
        checkActivityRecognition();
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        stepCounter = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        if(stepCounter != null) // check if step counter exist in the device
        {
            sensorManager.registerListener(this, stepCounter,SensorManager.SENSOR_DELAY_FASTEST);
        }
    }

    private void checkActivityRecognition() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACTIVITY_RECOGNITION) != (int) PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this, new String[] { Manifest.permission.ACTIVITY_RECOGNITION }, 354);
        }
    }

    public static double distance(double lat1,
                                  double lat2, double lon1,
                                  double lon2)
    {

        // The math module contains a function
        // named toRadians which converts from
        // degrees to radians.
        lon1 = Math.toRadians(lon1);
        lon2 = Math.toRadians(lon2);
        lat1 = Math.toRadians(lat1);
        lat2 = Math.toRadians(lat2);

        // Haversine formula
        double dlon = lon2 - lon1;
        double dlat = lat2 - lat1;
        double a = Math.pow(Math.sin(dlat / 2), 2)
                + Math.cos(lat1) * Math.cos(lat2)
                * Math.pow(Math.sin(dlon / 2),2);

        double c = 2 * Math.asin(Math.sqrt(a));

        double r = 6371;

        // calculate the result
        return(c * r);
    }
    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.buttonStartWalking)
        {
            SessionRunning = true;
            Intent fitService = new Intent(this, FitMapService.class);
            ContextCompat.startForegroundService(this, fitService);
            startTimer();
            startWalking.setEnabled(false);
            stopWalking.setEnabled(true);
        }
        else if(v.getId() == R.id.buttonStopWalking)
        {
            SessionRunning = false;

            //get all locations within the walk first
            ArrayList<Location> locs = db.getLocations(UID);
            //get today's date in yyyy-mm-dd
            LocalDate dateObj = LocalDate.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            String date = dateObj.format(formatter);
            // save steps and distance of the day...
            double totalDistance = 0;
            for(int i = 0; i<locs.size()-1; i++) //classic O(n) algorithm, compares all adjacent points in one pass
            {
                LatLng thisPoint = new LatLng(locs.get(i).getLatitude(), locs.get(i).getLongitude());
                LatLng nextPoint = new LatLng(locs.get(i+1).getLatitude(), locs.get(i+1).getLongitude());
                double distance = distance(thisPoint.latitude, nextPoint.latitude, thisPoint.longitude, nextPoint.longitude); //O(1)
                totalDistance += distance;
            }
            //check if data of the day already exists
            StepDistance step = db.getStepsByDate(date, UID);
            if(step != null)
            {
                //get the value
                double prevDistance = step.distance;
                // add steps and distances
                double newDistance = totalDistance + prevDistance;
                //save back again (update)
                step.distance = newDistance;
                db.open();
                db.updateStep(step);
                db.close();
            }
            else //if it doesnt exist
            {
                //save directly
                step = new StepDistance(0, totalDistance, sp_counter.getInt("counter", 0), date, UID);
                db.insertStep(step);
            }
            //stop service to clear the walk session
            Intent fitService = new Intent(this, FitMapService.class);
            stopService(fitService);
            stopTimer();
            stopWalking.setEnabled(false);
            startWalking.setEnabled(true);

        }
    }

    private void startTimer() {
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                updateUI();
            }
        };
        timer = new Timer(true);
        timer.schedule(task, 1000, 1000);
    }

    private void stopTimer() {
        if(timer != null)
            timer.cancel();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {


        if(event.sensor.getType() == Sensor.TYPE_STEP_COUNTER && SessionRunning)
        {
            int totalSteps = sp_counter.getInt("counter", 0) + 1;
            SharedPreferences.Editor e = sp_counter.edit();
            e.putInt("counter", totalSteps);
            e.commit();
            updateUI();
        }
    }

    private void updateUI()
    {
        try
        {
            long second = ((System.currentTimeMillis() - ((FitApplication) getApplication()).getStartTime()) / 1000L) ;
            long sec = second % 60;
            long min = (second / 60) % 60;
            long hour = (second / (60 * 60));
            timer_tv.setText(""+hour+":"+min+":"+sec);
            steps_tv.setText("Steps: "+ sp_counter.getInt("counter", 0));
            ArrayList<Location> locs = db.getLocations(UID);
            double totalDistance = 0;
            for(int i = 0; i<locs.size()-1; i++) //classic O(n) algorithm, compares all adjacent points in one pass
            {
                LatLng thisPoint = new LatLng(locs.get(i).getLatitude(), locs.get(i).getLongitude());
                LatLng nextPoint = new LatLng(locs.get(i+1).getLatitude(), locs.get(i+1).getLongitude());
                double distance = distance(thisPoint.latitude, nextPoint.latitude, thisPoint.longitude, nextPoint.longitude); //O(1)
                totalDistance += distance;
            }
            distance_tv.setText(totalDistance + "km");




        }catch (Exception e)
        {
            //pass
        }
    }



    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onConnected(Bundle dataBundle){
        // Check Permissions Now
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},123);
        }

        try {
            Location location = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
            if (location != null) {
                startWalking.setEnabled(true);
            }
            LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
        }    catch (SecurityException s){
            Log.d("TS","Not able to run location services...");
        }}//-----------------------------------------------------------------------------
     public void onRequestPermissionsResult(int requestCode,String[] permissions,int[] grantResults) {
         super.onRequestPermissionsResult(requestCode, permissions, grantResults);
         if (requestCode == 123)
             if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                 onConnected(new Bundle());
         else if(requestCode == 354)
                 if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                     checkActivityRecognition();
     }


    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {

    }
}