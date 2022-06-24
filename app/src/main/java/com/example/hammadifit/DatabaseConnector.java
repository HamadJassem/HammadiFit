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

public class DatabaseConnector {
    // database name
    private static final String DATABASE_NAME = "FitnessApp";
    private SQLiteDatabase database; // TS: to run SQL commands
    private DatabaseOpenHelper databaseOpenHelper; // TS: create or open the database
    private final long DAY = 86400000;

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

    // inserts a new contact in the database
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

    // inserts a new contact in the database
    public void updateContact(long id, String name, String email,
                              String phone, String state, String city) {
        ContentValues editContact = new ContentValues();
        editContact.put("name", name);
        editContact.put("email", email);
        editContact.put("phone", phone);
        editContact.put("street", state);
        editContact.put("city", city);

        open(); // open the database
        database.update("contacts", editContact, "_id=" + id, null);
        close(); // close the database
    }

    // return a Cursor with all contact information in the database
    public Cursor getAllItems() {
        return database.rawQuery("SELECT _id, name, calorie, time FROM calories ORDER BY time DESC;", null);
    }
    public Cursor getAllItems(String UID) {
        return database.rawQuery("SELECT _id, name, calorie, time, UID FROM calories WHERE UID="+"'"+UID+"'"+"ORDER BY time DESC;", null);
    }
    public Cursor getAllItemsWithoutUID(String UID) {
        // https://stackoverflow.com/questions/16977230/how-to-convert-milliseconds-to-date-in-sqlite
        return database.rawQuery("SELECT _id, name, calorie, datetime(time/1000, 'unixepoch', 'localtime') AS time FROM calories WHERE UID="+"'"+UID+"'"+"ORDER BY time DESC;", null);
    }
    @RequiresApi(api = Build.VERSION_CODES.O)
    public Cursor getAllItemsToday(String UID) {
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
        return database.rawQuery("SELECT _id, name, calorie, time, UID FROM calories WHERE UID="+"'"+UID+"'"+" AND time > " + timeFrom12AM + " ORDER BY time DESC;", null);
    }


    public Cursor getAggregatedItemsWeekly(String UID)
    {
        return database.rawQuery(
                "SELECT Date(datetime(time/1000, 'unixepoch')) AS timeAGG, sum(calorie) AS calorieSUM \n" +
                "FROM calories\n" +
                "WHERE UID= '"+UID+"' \n" +
                "GROUP BY timeAGG\n" +
                "ORDER BY timeAGG DESC\n" +
                "LIMIT 7\n" +
                "\n ", null);
    }

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

    public ArrayList<Calorie> getCalories(String UID)
    {
        ArrayList<Calorie> list = new ArrayList<Calorie>();
        this.openReadableDB();
        Cursor cursor = getAllItems(UID);
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




    // delete the Item specified by the given String name
    public void deleteItem(long id) {
        open();
        database.delete("calories", "_id=" + id, null);
        close();
    }


    private class DatabaseOpenHelper extends SQLiteOpenHelper {
        // public constructor
        public DatabaseOpenHelper(Context context, String name,
                                  CursorFactory factory, int version) {
            super(context, name, factory, version);
        }

        // creates the contacts table when the database is created
        // TS: this is called from  open()->getWritableDatabase(). Only if the database does not exist
        @Override
        public void onCreate(SQLiteDatabase db) {
            // query to create a new table named contacts
            String createQuery = "CREATE TABLE calories" +
                    "(_id integer primary key autoincrement," +
                    "name TEXT, calorie REAL, UID TEXT," +
                    "time INTEGER);";

            db.execSQL(createQuery); // execute the query
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion,
                              int newVersion) {
        }
    }
}