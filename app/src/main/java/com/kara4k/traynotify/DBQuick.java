package com.kara4k.traynotify;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


class DBQuick {

    private static final String DB_NAME = "data";
    private static final int DB_VERSION = 1;

    private static final String TABLE_NAME = "data";
    private static final String KEY_ID = "_id";
    private static final String KEY_DATE = "DATE";
    private static final String KEY_DAYNAME = "DAYNAME";
    private static final String KEY_TEXT = "TEXT";
    private static final String KEY_IMG = "IMG_PATH";
    private static final String KEY_THUMB = "THUMB";

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
        return mDB.query(TABLE_NAME, null, null, null, null, null, null);
    }

    public Cursor getTodayEvent(String today) {
        return mDB.query(TABLE_NAME, new String[]{KEY_DATE, KEY_DAYNAME, KEY_TEXT, KEY_IMG, KEY_THUMB}, KEY_DATE + "= ?", new String[]{today}, null, null, null);
    }

    public void addRec(String date, String dayname, String text, String img, String thumb) {
        ContentValues cv = new ContentValues();
        cv.put(KEY_DATE, date);
        cv.put(KEY_DAYNAME, dayname);
        cv.put(KEY_TEXT, text);
        cv.put(KEY_IMG, img);
        cv.put(KEY_THUMB, thumb);
        mDB.insert(TABLE_NAME, null, cv);
    }

    public void updateRec(String text, String img, String thumb, String whereDate) {
        ContentValues cv = new ContentValues();
        cv.put(KEY_TEXT, text);
        cv.put(KEY_IMG, img);
        cv.put(KEY_THUMB, thumb);
        mDB.update(TABLE_NAME, cv, KEY_DATE + " = ?", new String[]{whereDate});
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
                    + KEY_DATE + " text,"
                    + KEY_DAYNAME + " text,"
                    + KEY_TEXT + " text,"
                    + KEY_IMG + " text,"
                    + KEY_THUMB + " text" + ");");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        }
    }

}



