package com.example.hammadifit;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.location.Location;
import android.os.Build;
import android.util.Log;
import android.util.Pair;

import androidx.annotation.RequiresApi;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
// code snippet taken from the slides & the run tracker app. (ill leave the comments as they are)
public class DatabaseConnector {
    // database name
    private static final String DATABASE_NAME = "FitnessApp";
    private SQLiteDatabase database; // TS: to run SQL commands
    private DatabaseOpenHelper databaseOpenHelper; // TS: create or open the database

    // public constructor for DatabaseConnector
    public DatabaseConnector(Context context) {
        // create a new DatabaseOpenHelper
        databaseOpenHelper =
                new DatabaseOpenHelper(context, DATABASE_NAME, null, 1);
    } // end DatabaseConnector constructor

    private void openReadableDB() {
        database = databaseOpenHelper.getReadableDatabase();
    }

    private void openWriteableDB() {
        database = databaseOpenHelper.getWritableDatabase();
    }
    // open the database connection
    public void open() throws SQLException
    {
        // create or open a database for reading/writing
        database = databaseOpenHelper.getWritableDatabase();
        //TS: at the first call, onCreate is called
    }

    // close the database connection
    public void close()
    {
        if (database != null)
            database.close(); // close the database connection
    }

    // inserts a new calorie in the database
    public void insertItem(String name, double calorie, String uid, long time) {
        ContentValues newItem = new ContentValues();
        newItem.put("name", name);
        newItem.put("calorie", calorie);
        newItem.put("time", time);
        newItem.put("uid", uid);

        open(); // open the database
        database.insert("calories", null, newItem);
        close(); // close the database
    }


    //String UID is used in each method signature for multi authentication

    // get all calories from the table that contains the given UID
    public Cursor getAllItems(String UID) {
        return database.rawQuery("SELECT _id, name, calorie, time, UID FROM calories WHERE UID="+"'"+UID+"'"+"ORDER BY time DESC;", null);
    }

    //gets all the items without the UID to use for the calorie history list view (one to one mapping)
    // id->list position
    // name, calorie, and timestamp
    public Cursor getAllItemsWithoutUID(String UID) {
        // https://stackoverflow.com/questions/16977230/how-to-convert-milliseconds-to-date-in-sqlite
        return database.rawQuery("SELECT _id, name, calorie, datetime(time/1000, 'unixepoch', 'localtime') AS time FROM calories WHERE UID="+"'"+UID+"'"+"ORDER BY time DESC;", null);
    }
    //@requiresapi is there because it was raising weird errors
    // method returns all the items of today ex now is 6:45 am, start of day is 00:00AM which is 12AM
    //thus it will keep the progress bar in the calorie activity active of tracking daily calories for 24hrs
    @RequiresApi(api = Build.VERSION_CODES.O)
    public Cursor getAllItemsToday(String UID) {
        LocalDate currentdate = LocalDate.now();
        Calendar c = Calendar.getInstance();
        c.set(Calendar.MONTH, currentdate.getMonthValue()-1); //january begins from 0
        c.set(Calendar.YEAR, currentdate.getYear());
        c.set(Calendar.DAY_OF_MONTH, currentdate.getDayOfMonth());
        c.set(Calendar.HOUR, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);

        Log.d("date", c.getTime().toString());

        long timeFrom12AM = c.getTimeInMillis(); //gets 12AM
        return database.rawQuery("SELECT _id, name, calorie, time, UID FROM calories WHERE UID="+"'"+UID+"'"+" AND time > " + timeFrom12AM + " ORDER BY time DESC;", null); //anything greater than today's 12am
    }


    public Cursor getAggregatedItemsWeekly(String UID)  // groups the sum of calories per date then restricts to last 7 days by ordering descending in terms of date
    {

        return database.rawQuery(
                "SELECT Date(datetime(time/1000, 'unixepoch', 'localtime')) AS timeAGG, sum(calorie) AS calorieSUM \n" +
                "FROM calories\n" +
                "WHERE UID= '"+UID+"' \n" +
                "GROUP BY timeAGG\n" +
                "ORDER BY timeAGG DESC\n" +
                "LIMIT 7\n" +
                "\n ", null);
    }

    //gets items for today to use in generating the pie chart
    @RequiresApi(api = Build.VERSION_CODES.O)
    public Cursor getAggregatedItemsToday(String UID) {
        LocalDate currentdate = LocalDate.now();
        Calendar c = Calendar.getInstance();
        c.set(Calendar.MONTH, currentdate.getMonthValue()-1);
        c.set(Calendar.YEAR, currentdate.getYear());
        c.set(Calendar.DAY_OF_MONTH, currentdate.getDayOfMonth());
        c.set(Calendar.HOUR, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);

        Log.d("date", c.getTime().toString());

        long timeFrom12AM = c.getTimeInMillis();
        return database.rawQuery("SELECT name, sum(calorie) AS calorieSum FROM (SELECT _id, name, calorie, time, UID FROM calories WHERE UID="+"'"+UID+"'"+" AND time > " + timeFrom12AM + ") GROUP BY name", null);
    }

    //gets calories for today in an arraylist for ease of use
    @RequiresApi(api = Build.VERSION_CODES.O)
    public ArrayList<Calorie> getCaloriesToday(String UID)
    {
        ArrayList<Calorie> list = new ArrayList<Calorie>();
        this.openReadableDB();
        Cursor cursor = getAllItemsToday(UID);
        while (cursor.moveToNext())
        {
            int id = cursor.getInt(0);
            String name = cursor.getString(1);
            double calorie = cursor.getDouble(2);
            long time = cursor.getLong(3);

            list.add(new Calorie(id, time, name, calorie, UID));


        }
        if (cursor != null){
            cursor.close();
        }
        close(); //close the database
        return list;
    }
    //gets the aggregated calories today for piechart
    @RequiresApi(api = Build.VERSION_CODES.O)
    public ArrayList<Pair<String, Double>> getAggregatedCaloriesToday(String UID)
    {
        ArrayList<Pair<String, Double>> list = new ArrayList<>();
        this.openReadableDB();
        Cursor cursor = getAggregatedItemsToday(UID);
        while (cursor.moveToNext())
        {
            String name = cursor.getString(0);
            double calorie = cursor.getDouble(1);
            list.add(new Pair<>(name, calorie));
        }
        if (cursor != null){
            cursor.close();
        }
        close(); //close the database
        return list;
    }
    //gets aggregated calories for the week for bar chart
    public ArrayList<Pair<String, Double>> getAggregatedCaloriesWeekly(String UID)
    {
        ArrayList<Pair<String, Double>> list = new ArrayList<>();
        this.openReadableDB();
        Cursor cursor = getAggregatedItemsWeekly(UID);
        while (cursor.moveToNext())
        {
            String name = cursor.getString(0);
            double calorie = cursor.getDouble(1);
            list.add(new Pair<>(name, calorie));
        }
        if (cursor != null){
            cursor.close();
        }
        close(); //close the database
        return list;
    }

    // gets weekly steps for bar chart in the profile page
    public Cursor getStepsWeekly(String UID)
    {
        return database.rawQuery("SELECT * FROM distances WHERE UID = '"+ UID + "' ORDER BY _id DESC LIMIT 7;", null);
    }
    // gets weekly steps in an arraylist for bar chart
    public ArrayList<Pair<String, Integer>> getDistanceWeeklyInArrayList(String UID)
    {
        ArrayList<Pair<String, Integer>> list = new ArrayList<>();
        this.openReadableDB();
        Cursor cursor = getStepsWeekly(UID);
        while (cursor.moveToNext())
        {
            String date = cursor.getString(3);
            int distance = cursor.getInt(2);
            list.add(new Pair<>(date, distance));
        }
        if (cursor != null){
            cursor.close();
        }
        close(); //close the database
        return list;
    }




    // delete the Item specified by the given String name
    public void deleteItem(long id) {
        open();
        database.delete("calories", "_id=" + id, null);
        close();
    }


    //reference runtracker app
    public void insertLocation(Location location, String UID) {
        ContentValues newItem = new ContentValues();
        newItem.put("latitude", location.getLatitude());
        newItem.put("longitude", location.getLongitude());
        newItem.put("time", location.getTime());
        newItem.put("uid", UID);
        open(); // open the database
        database.insert("locations", null, newItem);
        close(); // close the database
    }

    // for bar chart
    public StepDistance getStepsByDate(String date, String UID)
    {
        StepDistance step = null;
        this.openReadableDB();
        Cursor cursor = database.rawQuery("SELECT * FROM distances WHERE date = '"+date+"' AND UID = '"+UID + "';", null);
        if(cursor.moveToFirst())
        {
            step = new StepDistance(cursor.getInt(0), cursor.getDouble(1), cursor.getInt(2), cursor.getString(3), cursor.getString(4));
        }
        if(cursor != null)
        {
            cursor.close();
        }
        this.close();
        return step;
    }
    // use all locations to find distance between all adjacent points -> add and get total distance
    public ArrayList<Location> getLocations(String UID)
    {
        ArrayList<Location> list = new ArrayList<Location>();
        this.openReadableDB();
        Cursor cursor = database.rawQuery("SELECT * FROM LOCATIONS WHERE UID = '" + UID + "';", null);

        while (cursor.moveToNext())
        {
            double latitude = cursor.getDouble(1);
            double longitude = cursor.getDouble(2);
            long time = cursor.getLong(3);

            Location loc = new Location("GPS");
            loc.setLatitude(latitude);
            loc.setLongitude(longitude);
            loc.setTime(time);

            list.add(loc);
        }
        if (cursor != null){
            cursor.close();
        }
        close(); //close the database
        return list;
    }
    // when ending the walking session, all locations will be deleted
    public void deleteLocations()
    {
        this.openWriteableDB();
        database.delete("LOCATIONS", null, null);
        this.close();
    }
    //updates the step for each location change & sensor change
    public void updateStep(StepDistance stepdistance)
    {
        ContentValues content = new ContentValues();
        content.put("distance", stepdistance.distance);
        content.put("steps", stepdistance.steps);
        content.put("date", stepdistance.date);
        content.put("UID", stepdistance.UID);
        database.update("distances", content, "_id="+stepdistance._id, null);
    }
    // inserts fresh steps if session started and user didnt walk today.
    public void insertStep(StepDistance stepdistance)
    {
        ContentValues content = new ContentValues();
        content.put("distance", stepdistance.distance);
        content.put("steps", stepdistance.steps);
        content.put("date", stepdistance.date);
        content.put("UID", stepdistance.UID);
        open();
        database.insert("distances", null, content);
        close();
    }


    private class DatabaseOpenHelper extends SQLiteOpenHelper {
        // public constructor
        public DatabaseOpenHelper(Context context, String name,
                                  CursorFactory factory, int version) {
            super(context, name, factory, version);
        }

        // creates calories, locations, distances tables when the database is created
        // TS: this is called from  open()->getWritableDatabase(). Only if the database does not exist

        @Override
        public void onCreate(SQLiteDatabase db) {
            // query to create a new table named contacts
            String createQuery = "CREATE TABLE calories" +
                    "(_id integer primary key autoincrement," +
                    "name TEXT, calorie REAL, UID TEXT," +
                    "time INTEGER);";

            db.execSQL(createQuery); // execute the query
            // uses runtracker's template for calculating distance walked
            String createQuery2 = "CREATE TABLE locations(_id integer primary key autoincrement, latitude REAL, longitude REAL, time INTEGER, UID TEXT);";
            db.execSQL(createQuery2);
            // track users daily distance and steps covered
            String createQuery3 = "CREATE TABLE distances(_id integer primary key autoincrement, distance REAL, steps INTEGER, date TEXT, UID TEXT);";
            db.execSQL(createQuery3);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion,
                              int newVersion) {
        }
    }
}