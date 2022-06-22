package com.example.hammadifit;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase.CursorFactory;

import java.util.ArrayList;

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

    // open the database connection
    public void open() throws SQLException {
        // create or open a database for reading/writing
        database = databaseOpenHelper.getWritableDatabase();//TS: at the first call, onCreate is called
    }

    // close the database connection
    public void close() {
        if (database != null)
            database.close(); // close the database connection
    }

    // inserts a new contact in the database
    public void insertContact(String name, String email, String phone,
                              String state, String city) {
        ContentValues newContact = new ContentValues();
        newContact.put("name", name);
        newContact.put("email", email);
        newContact.put("phone", phone);
        newContact.put("street", state);
        newContact.put("city", city);

        open(); // open the database
        database.insert("contacts", null, newContact);
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
    public Cursor getAllItems(String UID) {
        return database.rawQuery("SELECT _id, name, calorie, time FROM calories WHERE UID="+"'"+UID+"'"+"ORDER BY time DESC;", null);
    }



    // delete the Item specified by the given String name
    public void deleteItem(long id) {
        open();
        database.delete("calories", "_id=" + id, null);
        /*OR*/ //database.delete("contacts", "_id = ?", new String[] {String.valueOf(id)});
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