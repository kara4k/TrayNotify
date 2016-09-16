package com.kara4k.traynotify;


import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.NotificationCompat;

import java.util.List;

public class RebootReceiver extends BroadcastReceiver {


    @Override
    public void onReceive(Context context, Intent intent) {

        setReminders(context);
        showNotes(context);
        startClipTracking(context);

    }

    private void startClipTracking(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        boolean isTrack = sp.getBoolean(Settings.TRACK_CLIPBOARD, false);

        if (isTrack) {
            try {
                Intent trackClip = new Intent(context, ClipboardService.class);
                context.startService(trackClip);
            } catch (Exception e) {
            }
        }
    }

    private void showNotes(Context context) {
        NotificationManagerCompat nm = NotificationManagerCompat.from(context);
        List<Note> notes = QuickNotesFragment.getAllNotesFromDB(context);
        for (Note x : notes) {
            if (x.getIcon() == 1) {
                nm.notify(x.getNumid(), createNotification(context, x));
            }
        }
    }

    private void setReminders(Context context) {
        DBDelay db = new DBDelay(context);
        db.open();
        Cursor allData = db.getAllData();
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        if (allData.moveToFirst()) {
            do {
                PendingIntent pendingIntent = PendingIntent.getBroadcast(context, allData.getInt(10), getAlarmIntent(context, allData), PendingIntent.FLAG_UPDATE_CURRENT);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    startAfterKitKatAlarm(allData, alarmManager, pendingIntent);
                } else {
                    startBeforeKITKATAlarm(allData, alarmManager, pendingIntent);
                }
            } while (allData.moveToNext());

        }
        db.close();
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void startAfterKitKatAlarm(Cursor allData, AlarmManager alarmManager, PendingIntent pendingIntent) {
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, allData.getLong(4), pendingIntent);
    }

    private void startBeforeKITKATAlarm(Cursor allData, AlarmManager alarmManager, PendingIntent pendingIntent) {
        int repeat = allData.getInt(5);
        if (repeat == 1) {
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, allData.getLong(4), 24 * 60 * 60 * 1000, pendingIntent);
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, allData.getLong(4), pendingIntent);
        }
    }

    @NonNull
    private Intent getAlarmIntent(Context context, Cursor allData) {
        Intent alarmIntent = new Intent(context, AlarmReceiver.class);
        Bundle bundle = new Bundle();
        bundle.putInt("id", allData.getInt(10));
        alarmIntent.putExtras(bundle);
        return alarmIntent;
    }

    private Notification createNotification(Context context, Note note) {
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context);
        mBuilder.setContentTitle(note.getTitle());
        mBuilder.setContentText(note.getText());
        mBuilder.setContentInfo("#" + String.valueOf(note.getNumid()).substring(1));
        mBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText(note.getText()));
        mBuilder.setOngoing(true);
        mBuilder.setContentIntent(PendingIntent.getActivities(context, note.getNumid(), makeIntent(context, note), PendingIntent.FLAG_UPDATE_CURRENT));
        mBuilder.setSmallIcon(R.drawable.notify);


//        mBuilder.setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.main_icon));

        PendingIntent removePI = PendingIntent.getBroadcast(context, note.getNumid(), actionRemoveIntent(context, note.getNumid()), PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.addAction(R.drawable.ic_delete_sweep_white_24dp, context.getString(R.string.remove), removePI);

        return mBuilder.build();
    }

    private Intent actionRemoveIntent(Context context, int id) {
        Intent intent = new Intent(context, NActionReceiver.class);
        intent.putExtra("type", 1);
        intent.putExtra("id", id);
        return intent;
    }

    private Intent[] makeIntent(Context context, Note note) {
        Intent main = new Intent(context, MainActivity.class);
        main.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        Intent quick = new Intent(context, QuickNote.class);

        quick.putExtra(Intent.EXTRA_SUBJECT, note.getTitle());
        quick.putExtra(Intent.EXTRA_TEXT, note.getText());
        quick.putExtra("id", note.getNumid());
        return new Intent[]{main, quick};
    }

}
