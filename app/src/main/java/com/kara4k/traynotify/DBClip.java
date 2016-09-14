package com.kara4k.traynotify;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

class DBClip {

    private static final String DB_NAME = "Clip";
    private static final int DB_VERSION = 1;

    private static final String TABLE_NAME = "Clip";
    private static final String KEY_ID = "_id";
    private static final String KEY_TEXT = "TEXT";
    private static final String KEY_DATE = "DATE";
    private static final String KEY_NUMID = "NUMID";
    private static final String KEY_CHECKED = "CHECKED";


    private final Context mContext;

    private DBManager dbManager;
    private SQLiteDatabase mDB;

    public DBClip(Context ctx) {
        mContext = ctx;
    }

    public void open() {
        dbManager = new DBManager(mContext);
        mDB = dbManager.getWritableDatabase();
    }

    public Cursor getAllData() {
        Log.e("DBClip", "getAllData: " + "here");
        return mDB.query(TABLE_NAME, null, null, null, null, null, KEY_ID + " DESC");
    }

    public void removeClip(int numId) {
        mDB.delete(TABLE_NAME, KEY_NUMID + "=?", new String[]{String.valueOf(numId)});
    }

    public boolean isExist(String text) {
        Cursor clips = mDB.query(TABLE_NAME, null, KEY_TEXT + "=?", new String[]{text}, null, null, null);
        if (clips.moveToFirst()) {
            Log.e("DBClip", "isExist: " + "exist");
            return true;
        } else {
            Log.e("DBClip", "isExist: " + "notExist");
            return false;
        }
    }

    public int getClipNumID() {
        int numId;
        Cursor cursor = mDB.query(TABLE_NAME, null, null, null, null, null, KEY_NUMID + " DESC");
        if (cursor.moveToFirst()) {
            numId = cursor.getInt(3) + 1;
        } else {
            numId = 1;
        }
        return numId;
    }

    public void addClip(String text, long date, int numid) {
        ContentValues cv = new ContentValues();
        cv.put(KEY_TEXT, text);
        cv.put(KEY_DATE, date);
        cv.put(KEY_NUMID, numid);
        mDB.insert(TABLE_NAME, null, cv);
    }




    public void close() {
        if (dbManager != null) dbManager.close();
    }


    private class DBManager extends SQLiteOpenHelper {

        final Context context;

        public DBManager(Context context) {
            super(context, DBClip.DB_NAME, null, DBClip.DB_VERSION);
            this.context = context;
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("create table " + TABLE_NAME + " ("
                    + KEY_ID + " integer primary key autoincrement,"
                    + KEY_TEXT + " text,"
                    + KEY_DATE + " integer,"
                    + KEY_NUMID + " integer,"
                    + KEY_CHECKED + " integer" + ");");

        }


        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        }
    }

}
