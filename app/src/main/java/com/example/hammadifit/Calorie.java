package com.example.hammadifit;

public class Calorie
{
    private int id;
    private long time;
    private String name;
    private double calorie;
    private String UID;

    public Calorie(int id, long time, String name, double calorie, String UID) {
        this.id = id;
        this.time = time;
        this.name = name;
        this.calorie = calorie;
        this.UID = UID;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getCalorie() {
        return calorie;
    }

    public void setCalorie(double calorie) {
        this.calorie = calorie;
    }

    public String getUID() {
        return UID;
    }

    public void setUID(String UID) {
        this.UID = UID;
    }

    public Calorie()
    {

    }
}
