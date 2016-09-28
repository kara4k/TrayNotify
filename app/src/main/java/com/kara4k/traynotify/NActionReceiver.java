package com.kara4k.traynotify;

import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationManagerCompat;

import java.util.Calendar;

public class NActionReceiver extends BroadcastReceiver {

    public static final String TYPE = "type";
    public static final String ID = "id";
    public static final String ACTION = "action";

    Context context;
    private AlarmManager alarmManager;
    private NotificationManagerCompat nm;

    @Override
    public void onReceive(Context context, Intent intent) {

        this.context = context;
        nm = NotificationManagerCompat.from(context);

        int action = intent.getIntExtra(ACTION, 0);
        int type = intent.getIntExtra(TYPE, 0);
        int id = intent.getIntExtra(ID, 0);


        if (action == 1) {
            sendText(type, id);
        }

        if (action == 2) {
            putToClip(context, type, id);
        }

        if (action == 3) {
            removeTrayNotification(intent);
        }

        if (action == 4 || action == 5 || action == 6) {
            createFastDelayedAlarm(context, action, id);
        }
    }

    private void createFastDelayedAlarm(Context context, int action, int id) {
        alarmManager = (AlarmManager) context.getSystemService(context.ALARM_SERVICE);
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        String delayTime = getDelayTime(action, sp);
        String[] timeAdd = delayTime.split(":");
        long setTime = getTime(timeAdd);
        setAlarm(getPendingIntent(id), setTime);
        nm.cancel(id);
    }

    @NonNull
    private long getTime(String[] timeAdd) {
        Calendar now = Calendar.getInstance();
        now.add(Calendar.HOUR_OF_DAY, Integer.parseInt(timeAdd[0]));
        now.add(Calendar.MINUTE, Integer.parseInt(timeAdd[1]));
        return now.getTimeInMillis();
    }

    @NonNull
    private String getDelayTime(int action, SharedPreferences sp) {
        String delayTime = "00:05";
        switch (action) {
            case 4:
                delayTime = sp.getString(Settings.FAST_REM_DELAY_1, "00:05");
                break;
            case 5:
                delayTime = sp.getString(Settings.FAST_REM_DELAY_2, "00:20");
                break;
            case 6:
                delayTime = sp.getString(Settings.FAST_REM_DELAY_3, "01:00");
                break;
        }
        return delayTime;
    }

    private void setAlarm(PendingIntent pendingIntent, long time) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            setAfterKITKATAlarm(pendingIntent, time);
        } else {
            setBeforeKITKATAlarm(pendingIntent, time);
        }
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void setAfterKITKATAlarm(PendingIntent pendingIntent, long time) {
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, time, pendingIntent);
    }

    private void setBeforeKITKATAlarm(PendingIntent pendingIntent, long time) {
        alarmManager.set(AlarmManager.RTC_WAKEUP, time, pendingIntent);

    }

    private PendingIntent getPendingIntent(int id) {
        return PendingIntent.getBroadcast(context, 5000 - id, getAlarmIntent(id), PendingIntent.FLAG_UPDATE_CURRENT);
    }

    @NonNull
    private Intent getAlarmIntent(int id) {
        Intent alarmIntent = new Intent(context, AlarmReceiver.class);
        Bundle bundle = new Bundle();
        bundle.putInt("id", id);
        bundle.putInt("fast", 1);
        alarmIntent.putExtras(bundle);
        return alarmIntent;
    }

    public void sendText(int type, int id) {
        if (type == 1) {
            sendIntent(getQuickNoteTitle(id), getQuickNoteText(id));
        } else if (type == 2) {
            sendIntent(getDelayedTitle(id), getDelayedText(id));
        }
    }

    private void sendIntent(String title, String text) {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_SUBJECT, title);
        sendIntent.putExtra(Intent.EXTRA_TEXT, text);
        sendIntent.setType("text/plain");
        sendIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(sendIntent);
    }

    public void putToClip(Context context, int type, int id) {
        if (type == 1) {
            setQuickToClip(id);
        }
        if (type == 2) {
            setDelayedToClip(id);
        }
        MainActivity.notifyClipData(context);
    }

    public void setDelayedToClip(int id) {
        ClipboardManager cm = (ClipboardManager) context.getSystemService(context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("", getDelayedText(id));
        cm.setPrimaryClip(clip);
    }

    private String getDelayedText(int id) {
        try {
            String text = "";
            if (id != 0) {
                DBDelay db = new DBDelay(context);
                db.open();
                Cursor note = db.getAlarmNote(id);
                if (note.moveToFirst()) {
                    text = note.getString(1);
                }
                note.close();
                db.close();
            }
            return text;
        } catch (Exception e) {
            return "";
        }
    }

    private String getDelayedTitle(int id) {
        try {
            String title = "";
            if (id != 0) {
                DBDelay db = new DBDelay(context);
                db.open();
                Cursor note = db.getAlarmNote(id);
                if (note.moveToFirst()) {
                    title = note.getString(2);
                }
                note.close();
                db.close();
            }
            return title;
        } catch (Exception e) {
            return "";
        }
    }

    public void setQuickToClip(int id) {
        ClipboardManager cm = (ClipboardManager) context.getSystemService(context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("", getQuickNoteText(id));
        cm.setPrimaryClip(clip);
    }

    public String getQuickNoteText(int id) {
        try {
            String clipText = "";
            if (id != 0) {
                DBQuick db = new DBQuick(context);
                db.open();
                Cursor currentNote = db.getCurrentNote(id);
                if (currentNote.moveToFirst()) {
                    clipText = currentNote.getString(0);
                }
                currentNote.close();
                db.close();
            }
            return clipText;
        } catch (Exception e) {
            return "";
        }
    }

    public String getQuickNoteTitle(int id) {
        try {
            String title = "";
            if (id != 0) {
                DBQuick db = new DBQuick(context);
                db.open();
                Cursor currentNote = db.getCurrentNote(id);
                if (currentNote.moveToFirst()) {
                    title = currentNote.getString(1);
                }
                currentNote.close();
                db.close();
            }
            return title;
        } catch (Exception e) {
            return "";
        }
    }


    private void removeTrayNotification(Intent intent) {
        try {

            int id = intent.getIntExtra("id", 0);
            int type = intent.getIntExtra("type", 0);
            nm.cancel(id);
            if (type == 1) {
                setTrayIconDB(id);
                refreshRecyclerIfOpen(id);
            }
        } catch (Exception e) {
        }
    }

    private void setTrayIconDB(int id) {
        try {
            DBQuick db = new DBQuick(context);
            db.open();
            db.setQuickTrayInDB(id, 0);
            db.close();
        } catch (Exception e) {
        }
    }

    private void refreshRecyclerIfOpen(int id) {
        try {
            MainActivity.refreshQuickTrayIcon(context, id);
        } catch (Exception e) {
        }
    }
}
