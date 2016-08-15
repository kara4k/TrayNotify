package com.kara4k.traynotify;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBDelay {

    private static final String DB_NAME = "Delay";
    private static final int DB_VERSION = 1;

    private static final String TABLE_NAME = "Notes";
    private static final String KEY_ID = "_id";
    private static final String KEY_TEXT = "TEXT";
    private static final String KEY_TITLE = "TITLE";
    private static final String KEY_CREATE_TIME = "CREATE_TIME";
    private static final String KEY_SET_TIME = "SET_TIME";
    private static final String KEY_REPEAT = "REPEAT";
    private static final String KEY_DAYS = "DAYS";
    private static final String KEY_SOUND = "SOUND";
    private static final String KEY_VIBRATION = "VIBRATION";
    private static final String KEY_PRIORITY = "PRIORITY";
    private static final String KEY_CHECKID = "CHECKID";
    private static final String KEY_BIRTHDAY = "BIRTHDAY";


    private final Context mContext;

    private DBManager dbManager;
    private SQLiteDatabase mDB;

    public DBDelay(Context ctx) {
        mContext = ctx;
    }

    public void open() {
        dbManager = new DBManager(mContext);
        mDB = dbManager.getWritableDatabase();
    }

    public Cursor getAllData() {
        return mDB.query(TABLE_NAME, null, null, null, null, null, KEY_ID + " DESC");
    }

    public int getNoteCheckID() {
        open();
        int checkId;
       Cursor cursor = mDB.query(TABLE_NAME, null, null, null, null, null, KEY_CHECKID + " DESC");
        if (cursor.moveToFirst()) {
            checkId = cursor.getInt(10) + 1;
        } else {
            checkId = 1;
        }
        close();
        return checkId;
    }

    public void removeNote(int id) {
        mDB.delete(TABLE_NAME, KEY_CHECKID + "=?", new String[]{String.valueOf(id)});
    }

    public Cursor getAlarmNote(int check) {
        return mDB.query(TABLE_NAME,
                new String[]{KEY_ID, KEY_TEXT, KEY_TITLE, KEY_CREATE_TIME, KEY_SET_TIME, KEY_REPEAT, KEY_DAYS, KEY_SOUND, KEY_VIBRATION,KEY_PRIORITY, KEY_CHECKID, KEY_BIRTHDAY},
                KEY_CHECKID + "= ?", new String[]{String.valueOf(check)}, null, null, null);
    }

    public void addNote(DelayedNote note) {
        ContentValues cv = new ContentValues();
        cv.put(KEY_TEXT, note.getText());
        cv.put(KEY_TITLE, note.getTitle());
        cv.put(KEY_CREATE_TIME, note.getCreateTime());
        cv.put(KEY_SET_TIME, note.getSetTime());
        cv.put(KEY_REPEAT, note.getRepeat());
        cv.put(KEY_DAYS, note.getDays());
        cv.put(KEY_SOUND, note.getSound());
        cv.put(KEY_VIBRATION, note.getVibration());
        cv.put(KEY_PRIORITY, note.getPriority());
        cv.put(KEY_CHECKID, note.getCheckId());
        cv.put(KEY_BIRTHDAY, note.getBirthday());
        open();
        mDB.insert(TABLE_NAME, null, cv);
        close();
    }



    public void editNote(DelayedNote note, int id) {
        ContentValues cv = new ContentValues();
        cv.put(KEY_TEXT, note.getText());
        cv.put(KEY_TITLE, note.getTitle());
        cv.put(KEY_CREATE_TIME, note.getCreateTime());
        cv.put(KEY_SET_TIME, note.getSetTime());
        cv.put(KEY_REPEAT, note.getRepeat());
        cv.put(KEY_DAYS, note.getDays());
        cv.put(KEY_SOUND, note.getSound());
        cv.put(KEY_VIBRATION, note.getVibration());
        cv.put(KEY_PRIORITY, note.getPriority());
        cv.put(KEY_CHECKID, note.getCheckId());
        cv.put(KEY_BIRTHDAY, note.getBirthday());
        open();
        mDB.update(TABLE_NAME, cv, KEY_CHECKID + "=?", new String[]{String.valueOf(id)} );
        close();
    }

    public void close() {
        if (dbManager != null) dbManager.close();
    }


    private class DBManager extends SQLiteOpenHelper {

        public DBManager(Context context) {
            super(context, DBDelay.DB_NAME, null, DBDelay.DB_VERSION);

        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("create table " + TABLE_NAME + " ("
                    + KEY_ID + " integer primary key autoincrement,"
                    + KEY_TEXT + " text,"
                    + KEY_TITLE + " text,"
                    + KEY_CREATE_TIME + " integer,"
                    + KEY_SET_TIME + " integer,"
                    + KEY_REPEAT + " integer,"
                    + KEY_DAYS + " text,"
                    + KEY_SOUND + " text,"
                    + KEY_VIBRATION + " text,"
                    + KEY_PRIORITY + " integer,"
                    + KEY_CHECKID + " integer,"
                    + KEY_BIRTHDAY + " integer" + ");");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        }
    }

}



