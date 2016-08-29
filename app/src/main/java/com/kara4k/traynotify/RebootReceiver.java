package com.kara4k.traynotify;


import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.NotificationCompat;

import java.util.List;

public class RebootReceiver extends BroadcastReceiver {


    @Override
    public void onReceive(Context context, Intent intent) {
        setReminders(context);
        showNotes(context);
    }

    private void showNotes(Context context) {
        NotificationManager nm = (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);
        List<Note> notes = QuickNotesFragment.getAllNotesFromDB(context);
        for (Note x : notes) {
            if (x.getIcon() == 1) {
                nm.notify(x.getNumid(),createNotification(context, x));
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
                Intent alarmIntent = new Intent(context, AlarmReceiver.class);
                Bundle bundle = new Bundle();
                bundle.putInt("id", allData.getInt(10));
                alarmIntent.putExtras(bundle);
                PendingIntent pendingIntent = PendingIntent.getBroadcast(context, allData.getInt(10), alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, allData.getLong(4), pendingIntent);
                } else {
                    alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, allData.getLong(4), 24 * 60 * 60 * 1000, pendingIntent);
                }
            } while (allData.moveToNext());

        }
        db.close();
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
        mBuilder.setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.user1));
        return mBuilder.build();
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
