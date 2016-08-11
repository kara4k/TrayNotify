package com.kara4k.traynotify;


import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;

public class RebootReceiver extends BroadcastReceiver{


    @Override
    public void onReceive(Context context, Intent intent) {
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
                alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, allData.getLong(4), 24 * 60 * 60 * 1000, pendingIntent);
            } while (allData.moveToNext());

        }
        db.close();
    }
}
