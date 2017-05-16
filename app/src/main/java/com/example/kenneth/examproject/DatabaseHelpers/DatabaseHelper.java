package com.example.kenneth.examproject.DatabaseHelpers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;


import com.example.kenneth.examproject.Models.Event;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Vector;

import static com.example.kenneth.examproject.Contracts.EventDatabaseContract.DATABASE_NAME;
import static com.example.kenneth.examproject.Contracts.EventDatabaseContract.DATABASE_VERSION;
import static com.example.kenneth.examproject.Contracts.EventDatabaseContract.Table1.COLUMN_EVENT_ID;
import static com.example.kenneth.examproject.Contracts.EventDatabaseContract.Table1.COLUMN_LATITUDE;
import static com.example.kenneth.examproject.Contracts.EventDatabaseContract.Table1.COLUMN_LONGITUDE;
import static com.example.kenneth.examproject.Contracts.EventDatabaseContract.Table1.COLUMN_START_TIME;
import static com.example.kenneth.examproject.Contracts.EventDatabaseContract.Table1.COLUMN_DESC;
import static com.example.kenneth.examproject.Contracts.EventDatabaseContract.Table1.COLUMN_ID;
import static com.example.kenneth.examproject.Contracts.EventDatabaseContract.Table1.COLUMN_IMAGE;
import static com.example.kenneth.examproject.Contracts.EventDatabaseContract.Table1.COLUMN_NAME;
import static com.example.kenneth.examproject.Contracts.EventDatabaseContract.Table1.COLUMN_ADDRESS;
import static com.example.kenneth.examproject.Contracts.EventDatabaseContract.Table1.CREATE_TABLE;
import static com.example.kenneth.examproject.Contracts.EventDatabaseContract.Table1.DELETE_TABLE;
import static com.example.kenneth.examproject.Contracts.EventDatabaseContract.Table1.TABLE_NAME;

/**
 * Created by Kenneth on 27-04-2017.
 */
//references to sources
//http://www.androiddesignpatterns.com/2012/05/correctly-managing-your-sqlite-database.html
//http://stackoverflow.com/questions/4989182/converting-java-bitmap-to-byte-array

public class DatabaseHelper extends SQLiteOpenHelper {

    private static DatabaseHelper sInstance;

    /**
     * Constructor should be private to prevent direct instantiation.
     * make call to static method "getInstance()" instead.
     */

    private DatabaseHelper(Context context) {
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

    public static synchronized DatabaseHelper getInstance(Context context) {

        // Use the application context, which will ensure that you
        // don't accidentally leak an Activity's context.
        // See this article for more information: http://bit.ly/6LRzfx
        if (sInstance == null) {
            sInstance = new DatabaseHelper(context.getApplicationContext());
        }
        return sInstance;
    }

    //add new row to database
    public void addEvent(Event event){

        Bitmap bmp = event.getEventImage();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] byteArray = stream.toByteArray();

        ContentValues values = new ContentValues();
        values.put(COLUMN_START_TIME, event.getStartTime());
        values.put(COLUMN_DESC, event.getDescrition());
        values.put(COLUMN_NAME, event.getName());
        values.put(COLUMN_ADDRESS, event.getAddress());
        values.put(COLUMN_EVENT_ID, event.getId());
        values.put(COLUMN_LONGITUDE, event.getLongitude());
        values.put(COLUMN_LATITUDE, event.getLatitude());
        values.put(COLUMN_IMAGE, byteArray);

        SQLiteDatabase db = sInstance.getWritableDatabase();
        db.insert(TABLE_NAME, null, values);
        String query = "SELECT * FROM " + TABLE_NAME + " WHERE 1";

        //cursor point to a location in your results
        Cursor c = db.rawQuery(query,null);

        if(c.getCount() > 48) {
            c.moveToFirst();
            deleteEventFromDatabase(c.getInt(c.getColumnIndex(COLUMN_ID)));
        }
        db.close();
    }

    //delete row from database
    public void deleteEventFromDatabase(long eventId){
        SQLiteDatabase db = sInstance.getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_NAME + " WHERE " + COLUMN_ID + " IN " + "( " + eventId + " )");
        db.close();
    }

    public List<Event> getAllEvents(){
        List<Event> events = new Vector<Event>();
        SQLiteDatabase db = sInstance.getWritableDatabase();
        String query = "SELECT * FROM " + TABLE_NAME + " WHERE 1";

        //cursor point to a location in your results
        Cursor c = db.rawQuery(query,null);

        //move to taskId row
        c.moveToLast();

        while(!c.isBeforeFirst()) {
            if(c.getString(c.getColumnIndex(COLUMN_NAME))!=null)
            {
                Event temp = new Event();
                temp.setId(c.getString(c.getColumnIndex(COLUMN_EVENT_ID)));
                temp.setAddress(c.getString(c.getColumnIndex(COLUMN_ADDRESS)));
                temp.setDescrition(c.getString(c.getColumnIndex(COLUMN_DESC)));
                temp.setName(c.getString(c.getColumnIndex(COLUMN_NAME)));
                temp.setStartTime(c.getString(c.getColumnIndex(COLUMN_START_TIME)));
                temp.setLongitude(c.getFloat(c.getColumnIndex(COLUMN_LONGITUDE)));
                temp.setLatitude(c.getFloat(c.getColumnIndex(COLUMN_LATITUDE)));

                byte[] byteArray = c.getBlob(c.getColumnIndex(COLUMN_IMAGE));
                Bitmap bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
                temp.setEventImage(bitmap);
                events.add(events.size(), temp);
                c.moveToPrevious();
            }
        }
        c.close();
        db.close();
        return events;
    }

}
