package com.example.kenneth.examproject.Contracts;

import android.provider.BaseColumns;

/**
 * Created by Kenneth on 27-04-2017.
 */
//references to sources
//http://stackoverflow.com/questions/17451931/how-to-use-a-contract-class-in-android

public final class EventDatabaseContract {

    public static final  int    DATABASE_VERSION   = 1;
    public static final  String DATABASE_NAME      = "database.db";
    private static final String TEXT_TYPE          = " TEXT";
    private static final String LONG_TYPE          = " INT";
    private static final String IMAGE_TYPE          = " BLOB";
    private static final String COMMA_SEP          = ",";

    // To prevent someone from accidentally instantiating the contract class,
    // give it an empty constructor.
    private EventDatabaseContract() {}

    public static abstract class Table1 implements BaseColumns {
        public static final String TABLE_NAME       = "taskTable";
        public static final String COLUMN_NAME = "taskname";
        public static final String COLUMN_START_TIME = "time";
        public static final String COLUMN_ID = "_id";
        public static final String COLUMN_DESC = "desc";
        public static final String COLUMN_ADDRESS = "address";
        public static final String COLUMN_EVENT_ID = "eventid";
        public static final String COLUMN_LONGITUDE = "longitude";
        public static final String COLUMN_LATITUDE = "latitude";
        public static final String COLUMN_URL = "url";
        public static final String COLUMN_END_TIME = "end_time";



        public static final String CREATE_TABLE = "CREATE TABLE " +
                TABLE_NAME + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                COLUMN_NAME + TEXT_TYPE + COMMA_SEP +
                COLUMN_START_TIME + TEXT_TYPE + COMMA_SEP +
                COLUMN_END_TIME + TEXT_TYPE + COMMA_SEP +
                COLUMN_DESC + TEXT_TYPE + COMMA_SEP +
                COLUMN_ADDRESS + TEXT_TYPE + COMMA_SEP +
                COLUMN_EVENT_ID + TEXT_TYPE + COMMA_SEP +
                COLUMN_LONGITUDE + LONG_TYPE + COMMA_SEP +
                COLUMN_LATITUDE + LONG_TYPE + COMMA_SEP +
                COLUMN_URL + TEXT_TYPE + " )";

        public static final String DELETE_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;
    }
}
