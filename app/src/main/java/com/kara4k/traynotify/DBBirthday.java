package com.kara4k.traynotify;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

class DBBirthday {

    private static final String DB_NAME = "Birthdays";
    private static final int DB_VERSION = 1;

    private static final String TABLE_NAME = "Birthdays";
    private static final String KEY_ID = "_id";
    private static final String KEY_CONT_ID = "CONT_ID";
    private static final String KEY_NAME = "NAME";
    private static final String KEY_DATE = "DATE";


    private final Context mContext;

    private DBManager dbManager;
    private SQLiteDatabase mDB;

    public DBBirthday(Context ctx) {
        mContext = ctx;
    }

    public void open() {
        dbManager = new DBManager(mContext);
        mDB = dbManager.getWritableDatabase();
    }

    public Cursor getAllData() {
        return mDB.query(TABLE_NAME, null, null, null, null, null, KEY_ID + " DESC");
    }

    public void clearDB() {
        mDB.delete(TABLE_NAME, null, null);
    }

    public void addNote(String contId, String name, String date) {
        ContentValues cv = new ContentValues();
        cv.put(KEY_CONT_ID, contId);
        cv.put(KEY_NAME, name);
        cv.put(KEY_DATE, date);
        mDB.insert(TABLE_NAME, null, cv);
    }



    public void close() {
        if (dbManager != null) dbManager.close();
    }


    private class DBManager extends SQLiteOpenHelper {

        final Context context;

        public DBManager(Context context) {
            super(context, DBBirthday.DB_NAME, null, DBBirthday.DB_VERSION);
            this.context = context;
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("create table " + TABLE_NAME + " ("
                    + KEY_ID + " integer primary key autoincrement,"
                    + KEY_CONT_ID + " text,"
                    + KEY_NAME + " text,"
                    + KEY_DATE + " text" + ");");


        }


        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        }
    }

}
