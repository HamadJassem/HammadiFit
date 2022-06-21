package com.example.hammadifit;

//  References:
//  https://firebase.google.com/docs/database/android/read-and-write

public class User {
    public String email;
    public String username;
    public boolean gender; //true = male, false = female
    public float height;
    public float weight;
    public int age;

    public User()
    {

    }
    public User(String email, String username, boolean gender, float height, float weight, int age) {
        this.email = email;
        this.username = username;
        this.gender = gender;
        this.height = height;
        this.weight = weight;
        this.age = age;
    }
}
