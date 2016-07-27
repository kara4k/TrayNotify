package com.kara4k.traynotify;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


class DBQuick {

    private static final String DB_NAME = "Quick";
    private static final int DB_VERSION = 1;

    private static final String TABLE_NAME = "Notes";
    private static final String KEY_ID = "_id";
    private static final String KEY_TITLE = "TITLE";
    private static final String KEY_TEXT = "TEXT";
    private static final String KEY_ICON = "ICON";
    private static final String KEY_DATE = "DATE";
    private static final String KEY_NUMID = "NUMID";


    private final Context mContext;

    private DBManager dbManager;
    private SQLiteDatabase mDB;

    public DBQuick(Context ctx) {
        mContext = ctx;
    }

    public void open() {
        dbManager = new DBManager(mContext);
        mDB = dbManager.getWritableDatabase();
    }

    public Cursor getAllData() {
        return mDB.query(TABLE_NAME, null, null, null, null, null, KEY_ID + " DESC");
    }

    public void removeNote(int id) {
        mDB.delete(TABLE_NAME,KEY_ID + "=?",new String[]{String.valueOf(id)});
    }

//    public Cursor getTodayEvent(String today) {
//        return mDB.query(TABLE_NAME, new String[]{KEY_DATE, KEY_DAYNAME, KEY_TEXT, KEY_IMG, KEY_THUMB}, KEY_DATE + "= ?", new String[]{today}, null, null, null);
//    }

    public void addNote(String title, String text, long date, int numid) {
        ContentValues cv = new ContentValues();
        cv.put(KEY_TITLE, title);
        cv.put(KEY_TEXT, text);
        cv.put(KEY_DATE, date);
        cv.put(KEY_NUMID, numid);
        mDB.insert(TABLE_NAME, null, cv);
    }

    public void updateRec(String title, String text, String date, String numid, String id) {
        ContentValues cv = new ContentValues();
        cv.put(KEY_TITLE, title);
        cv.put(KEY_TEXT, text);
        cv.put(KEY_DATE, date);
        cv.put(KEY_NUMID, numid);
        mDB.update(TABLE_NAME, cv, KEY_ID + " = ?", new String[]{id});
    }

    public void close() {
        if (dbManager != null) dbManager.close();
    }


    private class DBManager extends SQLiteOpenHelper {

        public DBManager(Context context) {
            super(context, DBQuick.DB_NAME, null, DBQuick.DB_VERSION);

        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("create table " + TABLE_NAME + " ("
                    + KEY_ID + " integer primary key autoincrement,"
                    + KEY_TITLE + " text,"
                    + KEY_TEXT + " text,"
                    + KEY_ICON + " integer,"
                    + KEY_DATE + " integer,"
                    + KEY_NUMID + " integer" + ");");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        }
    }

}



