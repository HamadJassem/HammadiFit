package com.example.hammadifit;

public class StepDistance {
    public int _id;
    public double distance;
    public int steps;
    public String date;

    public StepDistance(int _id, double distance, int steps, String date, String UID) {
        this._id = _id;
        this.distance = distance;
        this.steps = steps;
        this.date = date;
        this.UID = UID;
    }
    StepDistance(){}
    public String UID;


}
