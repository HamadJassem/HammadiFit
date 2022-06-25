package com.example.hammadifit;

import android.app.Application;

public class FitApplication extends Application {
    public String getUID() {
        return UID;
    }

    public void setUID(String UID) {
        this.UID = UID;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    private long startTime = -1;
    private String UID = "";
}
