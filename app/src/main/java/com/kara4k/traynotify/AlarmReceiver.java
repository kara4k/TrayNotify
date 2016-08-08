package com.kara4k.traynotify;


import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

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

        if (isNotify()) {

        }

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context);
        mBuilder.setSmallIcon(R.drawable.notify);

        mBuilder.setContentText(note.getText());
        mBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText(note.getText()));
        mBuilder.setContentTitle(note.getTitle());
        Log.e("tage",note.toString());

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

        Log.e("6767", note.toString());
        isNotify();

        mBuilder.setPriority(note.getPriority());
        mBuilder.setContentInfo(String.valueOf(note.getCheckId()));

        mBuilder.setAutoCancel(false);
        mBuilder.setContentIntent(PendingIntent.getActivities(context, 0, makeIntent(context), PendingIntent.FLAG_UPDATE_CURRENT));
        mBuilder.setOngoing(true);
        nm.notify(note.getCheckId(), mBuilder.build());
    }

    private void fillNote(Intent intent, DBDelay db) {
        note = new DelayedNote();
        Cursor current = db.getAlarmNote(intent.getIntExtra("id", 0));
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
        delay.putExtra("id", note.getCheckId());
        return new Intent[]{main, delay};
    }

    private int getDayNum() {
        Calendar calendar = Calendar.getInstance();
        int day = calendar.get(Calendar.DAY_OF_WEEK);
        int today;
        switch (day) {
            case 1:
                today = 6;
                break;
            case 2:
                today = 0;
                break;
            case 3:
                today = 1;
                break;
            case 4:
                today = 2;
                break;
            case 5:
                today = 3;
                break;
            case 6:
                today = 4;
                break;
            case 7:
                today = 5;
                break;
            default:
                today = 0;
        }
        return today;
    }

    private boolean isNotify() {
        if (note.getRepeat() == 1) {
            String[] split = note.getDays().split(";");
            if (split[getDayNum()].equals("1")) {
                return true;
            } else {
                return false;
            }
        } else {
            return true;
        }
    }
}
