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

    public void removeNote(int numId) {
        mDB.delete(TABLE_NAME,KEY_NUMID + "=?",new String[]{String.valueOf(numId)});
    }

    public int getNoteCheckID() {
        open();
        int numId;
        Cursor cursor = mDB.query(TABLE_NAME, null, null, null, null, null, KEY_NUMID + " ASC");
        if (cursor.moveToFirst()) {
            numId = cursor.getInt(5) - 1;
        } else {
            numId = -1;
        }
        close();
        return numId;
    }

    public Cursor getCurrentNote(int id) {
        return mDB.query(TABLE_NAME, new String[]{KEY_TEXT, KEY_TITLE, KEY_DATE, KEY_NUMID}, KEY_NUMID + "= ?", new String[]{String.valueOf(id)}, null, null, null);
    }

    public void addNote(String title, String text,int inTray, long date, int numid) {
        ContentValues cv = new ContentValues();
        cv.put(KEY_TITLE, title);
        cv.put(KEY_TEXT, text);
        cv.put(KEY_ICON, inTray);
        cv.put(KEY_DATE, date);
        cv.put(KEY_NUMID, numid);
        mDB.insert(TABLE_NAME, null, cv);
    }

    public void updateRec(String title, String text, int inTray, long date, int numid) {
        ContentValues cv = new ContentValues();
        cv.put(KEY_TITLE, title);
        cv.put(KEY_TEXT, text);
        cv.put(KEY_ICON, inTray);
        cv.put(KEY_DATE, date);
        cv.put(KEY_NUMID, numid);
        mDB.update(TABLE_NAME, cv, KEY_NUMID + " = ?", new String[]{String.valueOf(numid)});
    }

    public void clearQuickTrayAll() {
        ContentValues cv = new ContentValues();
        cv.put(KEY_ICON, 0);
        mDB.update(TABLE_NAME, cv, null, null);
    }

    public void setQuickTrayInDB(int numId, int tray) {
        ContentValues cv = new ContentValues();
        cv.put(KEY_ICON, tray);
        mDB.update(TABLE_NAME, cv, KEY_NUMID + " = ?", new String[]{String.valueOf(numId)});
    }

    public void close() {
        if (dbManager != null) dbManager.close();
    }


    private class DBManager extends SQLiteOpenHelper {

        Context context;

        public DBManager(Context context) {
            super(context, DBQuick.DB_NAME, null, DBQuick.DB_VERSION);
            this.context = context;
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


            ContentValues cv = new ContentValues();
            cv.put(KEY_TITLE, context.getString(R.string.first_message));
            cv.put(KEY_TEXT, context.getString(R.string.first_message_text));
            cv.put(KEY_ICON, 0);
            cv.put(KEY_NUMID, -1);
            db.insert(TABLE_NAME, null, cv);

            ContentValues cv2 = new ContentValues();
            cv2.put(KEY_TITLE, context.getString(R.string.second_message));
            cv2.put(KEY_TEXT, "");
            cv.put(KEY_ICON, 0);
            cv2.put(KEY_NUMID, -2);
            db.insert(TABLE_NAME, null, cv2);

            ContentValues cv3 = new ContentValues();
            cv3.put(KEY_TITLE, context.getString(R.string.third_message_title));
            cv3.put(KEY_TEXT, context.getString(R.string.third_message_text));
            cv.put(KEY_ICON, 0);
            cv3.put(KEY_NUMID, -3);
            db.insert(TABLE_NAME, null, cv3);
        }








        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        }
    }

}



