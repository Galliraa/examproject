package com.example.kenneth.examproject.DatabaseHelpers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.kenneth.examproject.Models.Event;

import java.util.List;
import java.util.Vector;

import static com.example.kenneth.examproject.Contracts.EventDatabaseContract.DATABASE_NAME;
import static com.example.kenneth.examproject.Contracts.EventDatabaseContract.DATABASE_VERSION;
import static com.example.kenneth.examproject.Contracts.EventDatabaseContract.Table1.COLUMN_END_TIME;
import static com.example.kenneth.examproject.Contracts.EventDatabaseContract.Table1.COLUMN_LATITUDE;
import static com.example.kenneth.examproject.Contracts.EventDatabaseContract.Table1.COLUMN_LONGITUDE;
import static com.example.kenneth.examproject.Contracts.EventDatabaseContract.Table1.COLUMN_START_TIME;
import static com.example.kenneth.examproject.Contracts.EventDatabaseContract.Table1.COLUMN_DESC;
import static com.example.kenneth.examproject.Contracts.EventDatabaseContract.Table1.COLUMN_URL;
import static com.example.kenneth.examproject.Contracts.EventDatabaseContract.Table1.COLUMN_NAME;
import static com.example.kenneth.examproject.Contracts.EventDatabaseContract.Table1.COLUMN_ADDRESS;
import static com.example.kenneth.examproject.Contracts.EventDatabaseContract.Table1.CREATE_TABLE;
import static com.example.kenneth.examproject.Contracts.EventDatabaseContract.Table1.DELETE_TABLE;
import static com.example.kenneth.examproject.Contracts.EventDatabaseContract.Table1.TABLE_NAME;


//references to sources
//http://www.androiddesignpatterns.com/2012/05/correctly-managing-your-sqlite-database.html

public class DatabaseHelper extends SQLiteOpenHelper {

    private static DatabaseHelper sInstance;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        sqLiteDatabase.execSQL(DELETE_TABLE);
        onCreate(sqLiteDatabase);
    }

    public void deleteAll()
    {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("delete from "+ TABLE_NAME);
        db.close();
    }

    public static synchronized DatabaseHelper getInstance(Context context)
    {
        if (sInstance == null) {
            sInstance = new DatabaseHelper(context.getApplicationContext());
        }
        return sInstance;
    }

    //add new row to database
    public void addEvent(Event event){

        ContentValues values = new ContentValues();

        values.put(COLUMN_START_TIME, event.getStartTime());
        values.put(COLUMN_URL, event.getEventImage());
        values.put(COLUMN_END_TIME, event.getEndTime());
        values.put(COLUMN_DESC, event.getDescrition());
        values.put(COLUMN_NAME, event.getName());
        values.put(COLUMN_ADDRESS, event.getAddress());
        values.put(COLUMN_LONGITUDE, event.getLongitude());
        values.put(COLUMN_LATITUDE, event.getLatitude());

        SQLiteDatabase db = sInstance.getWritableDatabase();
        db.insert(TABLE_NAME, null, values);

        db.close();
    }

    public List<Event> getAllEvents(){
        List<Event> events = new Vector<>();
        SQLiteDatabase db = sInstance.getWritableDatabase();

        //cursor point to a location in your results
        Cursor c = db.query(TABLE_NAME, null, null, null, null, null, COLUMN_START_TIME+" DESC");
        //move to taskId row
        c.moveToLast();

        while(!c.isBeforeFirst()) {
            if(c.getString(c.getColumnIndex(COLUMN_NAME))!=null)
            {
                Event temp = new Event();

                temp.setAddress(c.getString(c.getColumnIndex(COLUMN_ADDRESS)));
                temp.setDescrition(c.getString(c.getColumnIndex(COLUMN_DESC)));
                temp.setName(c.getString(c.getColumnIndex(COLUMN_NAME)));
                temp.setStartTime(c.getString(c.getColumnIndex(COLUMN_START_TIME)));
                temp.setLongitude(c.getFloat(c.getColumnIndex(COLUMN_LONGITUDE)));
                temp.setLatitude(c.getFloat(c.getColumnIndex(COLUMN_LATITUDE)));
                temp.setEndTime(c.getString(c.getColumnIndex(COLUMN_END_TIME)));
                temp.setEventImage(c.getString(c.getColumnIndex(COLUMN_URL)));

                events.add(events.size(), temp);
                c.moveToPrevious();
            }
        }
        c.close();
        db.close();
        return events;
    }

}
