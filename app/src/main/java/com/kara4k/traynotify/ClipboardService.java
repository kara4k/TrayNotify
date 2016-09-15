package com.kara4k.traynotify;

import android.app.Service;
import android.content.ClipDescription;
import android.content.ClipboardManager;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import java.util.Calendar;

public class ClipboardService extends Service implements ClipboardManager.OnPrimaryClipChangedListener {

    ClipboardManager cm;
    NotificationManagerCompat nm;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e("ClipboardService", "onStartCommand: " + "onStart");

        cm = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        nm = NotificationManagerCompat.from(getApplicationContext());

        cm.addPrimaryClipChangedListener(this);


        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {

        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onPrimaryClipChanged() {
        if ((isTextPlain(cm)) || (isTextHTML(cm))) {
            if (ifNotSpace()) {
                String s = cm.getPrimaryClip().getItemAt(0).getText().toString();
                writeClipToDB(s);


            }
        }


    }

    private boolean isTextPlain(ClipboardManager cm) {
        return cm.getPrimaryClip().getDescription().getMimeType(0).equals(ClipDescription.MIMETYPE_TEXT_PLAIN);
    }

    private boolean isTextHTML(ClipboardManager cm) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            return cm.getPrimaryClip().getDescription().getMimeType(0).equals(ClipDescription.MIMETYPE_TEXT_HTML);
        } else {
            return false;
        }

    }

    private boolean ifNotSpace() {
        return !cm.getPrimaryClip().getItemAt(0).getText().toString().trim().equals("");
    }

    private long getCurrentTime() {
        return Calendar.getInstance().getTimeInMillis();
    }

    private void writeClipToDB(String text) {
        DBClip db = new DBClip(getApplicationContext());
        db.open();
        int id = db.getClipNumID();
        Log.e("ClipboardService", "writeClipToDB: " + id);
        if (!db.isExist(text)) {
            db.addClip(text, getCurrentTime(), id);
            Log.e("ClipboardService", "writeClipToDB: " + "writing to db!");
        }
        db.close();
    }
}
