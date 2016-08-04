package com.kara4k.traynotify;


import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.v7.app.NotificationCompat;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class AlarmReceiver extends BroadcastReceiver {


    DelayedNote note;

    @Override
    public void onReceive(Context context, Intent intent) {
        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        DBDelay db = new DBDelay(context);
        db.open();

        fillNote(intent, db);
        Calendar calendar = Calendar.getInstance();
        calendar.get(Calendar.DAY_OF_WEEK);






        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context);
        mBuilder.setSmallIcon(R.drawable.notify);

        mBuilder.setContentText(note.getText());
        mBuilder.setContentTitle(note.getTitle());


        if (!note.getSound().equals("0")) {
            try {
                mBuilder.setSound(Uri.parse(note.getSound()));
            } catch (Exception e) {
                // TODO: 04.08.2016 defaults
            }
        }

        if (!note.getVibration().equals("1")) {
            String[] strings = note.getVibration().split(";");
            int repeat = Integer.parseInt(strings[2]);
            List<Long> vibratePattern = new ArrayList<Long>();
            vibratePattern.add(0, (long) 0);

            for (int k = 0; k < repeat; k++) {
                vibratePattern.add(Long.parseLong(strings[0]));
                vibratePattern.add(Long.parseLong(strings[1]));
            }

            long[] vibration = new long[vibratePattern.size()];
            for (int j = 0; j < vibratePattern.size(); j++) {
                vibration[j] = vibratePattern.get(j);
            }

            mBuilder.setVibrate(vibration);
        }

        mBuilder.setPriority(note.getPriority());
        mBuilder.setContentInfo(String.valueOf(note.getCheckId()));

        mBuilder.setAutoCancel(false);
        mBuilder.setContentIntent(PendingIntent.getActivities(context, 0, makeIntent(context), PendingIntent.FLAG_UPDATE_CURRENT));

        nm.notify(note.getCheckId(), mBuilder.build());
    }

    private void fillNote(Intent intent, DBDelay db) {
        note = new DelayedNote();
        Cursor current = db.getAlarmNote(intent.getIntExtra("check", 0));
        if (current.moveToFirst()) {
            note.setText(current.getString(1));
            note.setTitle(current.getString(2));
            note.setCreateTime(current.getLong(3));
            note.setSetTime(current.getLong(4));
            note.setRepeat(current.getInt(5));
            note.setDays(current.getString(6));
            note.setSound(current.getString(7));
            note.setVibration(current.getString(8));
            note.setPriority(current.getInt(9));
            note.setCheckId(current.getInt(10));

        }
        db.close();
    }

    private Intent[] makeIntent(Context context) {
        Intent main = new Intent(context, MainActivity.class);
        main.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        Intent delay = new Intent(context, CreateDelayedNote.class);
        delay.putExtra("check", note.getCheckId());
        return new Intent[]{main, delay};
    }
}
