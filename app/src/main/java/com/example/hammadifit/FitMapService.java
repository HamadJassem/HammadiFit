package com.example.hammadifit;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.location.Location;


import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.util.Timer;
import java.util.TimerTask;


public class FitMapService extends Service implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    private String UID;
    public static final int UPDATE_INTERVAL = 5000;         // 5 seconds
    public static final int FASTEST_UPDATE_INTERVAL = 2000; // 2 seconds
    private GoogleApiClient googleApiClient;
    private LocationRequest locationRequest;
    private DatabaseConnector db;


    public FitMapService() {
    }

    @Override
    public void onCreate()
    {
        super.onCreate();
        db = new DatabaseConnector(getApplicationContext());

        googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        // get location request and set it up
        locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(UPDATE_INTERVAL)
                .setFastestInterval(FASTEST_UPDATE_INTERVAL);

    }



    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //creates foreground service once the service starts
        NotificationChannel notificationChannel = new NotificationChannel("Channel_ID", "My Notifications", NotificationManager.IMPORTANCE_DEFAULT);
        NotificationManager manager = (NotificationManager) getSystemService(this.NOTIFICATION_SERVICE);
        manager.createNotificationChannel(notificationChannel);
        int icon = R.drawable.steps_icon;
        Notification notification = new NotificationCompat
                .Builder(this, "Channel_ID")
                .setTicker("Step Tracker Started")
                .setContentTitle("Step Tracker")
                .setContentText("Tracks steps & distance")
                .setSmallIcon(icon)
                .setAutoCancel(true)
                .setChannelId("Channel_ID")
                .build();
        manager.notify(1, notification);
        startForeground(1, notification);

        FitApplication app = (FitApplication) getApplication();
        app.setStartTime(System.currentTimeMillis());
        googleApiClient.connect();

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
       return null;
    }

    @Override
    public void onLocationChanged(Location location)
    {
        if (location != null){
            //inserts location corresponding to the user
            db.insertLocation(location, ((FitApplication)getApplication()).getUID());
        }
    }

    @Override
    public void onConnected(Bundle dataBundle) {
        // Check Permissions Now
        try {
            Location location = LocationServices.FusedLocationApi
                    .getLastLocation(googleApiClient);
            if (location != null){
                db.insertLocation(location, ((FitApplication)getApplication()).getUID());
            }
            LocationServices.FusedLocationApi
                    .requestLocationUpdates(
                            googleApiClient, locationRequest, this);
        }
        catch (SecurityException s){
            Log.d("hammadi","services not working");

        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        if (googleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.
                    removeLocationUpdates(googleApiClient, this);
        }
    }
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult)
    {
        Toast.makeText(this, "Connection failed! " +
                        "Please check your settings and try again.",
                Toast.LENGTH_SHORT).show();
    }
    @Override
    public void onDestroy() {
        if (googleApiClient.isConnected()) {
            googleApiClient.disconnect();
        }
        db.deleteLocations();
        super.onDestroy();
    }
}