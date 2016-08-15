package com.kara4k.traynotify;


import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import java.io.IOException;
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
            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context);
            mBuilder.setContentText(note.getText());
            mBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText(note.getText()));
            mBuilder.setContentTitle(note.getTitle());
            setVibroSound(context, mBuilder);
            mBuilder.setPriority(note.getPriority());
            mBuilder.setContentInfo(String.valueOf(note.getCheckId()));
            mBuilder.setAutoCancel(false);
            mBuilder.setContentIntent(PendingIntent.getActivities(context, note.getCheckId(), makeIntent(context), PendingIntent.FLAG_UPDATE_CURRENT));
            mBuilder.setOngoing(true);
            mBuilder.setSmallIcon(R.drawable.notify);

            if(note.getBirthday()!=0) {
                try {
                    Bitmap mBitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), Uri.parse("content://com.android.contacts/contacts/"+ note.getBirthday() + "/display_photo"));
                    mBuilder.setLargeIcon(mBitmap);
                } catch (IOException e) {
                    mBuilder.setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.user1));
                }
            } else {
                mBuilder.setLargeIcon(BitmapFactory.decodeResource(context.getResources(),R.drawable.user1 ));
            }


            nm.notify(note.getCheckId(), mBuilder.build());
        }

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if ((note.getRepeat() == 1) && (!note.getDays().equals("0;0;0;0;0;0;0;"))){
                Log.e("TAG", note.getRepeat() + "\n" + note.getDays() );
                AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                Intent alarmIntent = new Intent(context, AlarmReceiver.class);
                Bundle bundle = new Bundle();
                bundle.putInt("id", note.getCheckId());
                alarmIntent.putExtras(bundle);

                Calendar settedCal = Calendar.getInstance();
                settedCal.setTimeInMillis(note.getSetTime());
                Calendar now = Calendar.getInstance();
                now.add(Calendar.DAY_OF_MONTH, 1);
                settedCal.set(now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH));
                settedCal.set(Calendar.MILLISECOND,0000);


                PendingIntent pendingIntent = PendingIntent.getBroadcast(context, note.getCheckId(), alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, settedCal.getTimeInMillis(), pendingIntent);
            }

        }

    }



    {


    }

    private void setVibroSound(Context context, NotificationCompat.Builder mBuilder) {
        if ((note.getSound().equals("0")) && (note.getVibration().equals("1"))) {
            mBuilder.setDefaults(Notification.DEFAULT_ALL);
        } else if ((!(note.getSound().equals("0")) && (note.getVibration().equals("1")))) {
            mBuilder.setDefaults(Notification.DEFAULT_VIBRATE | Notification.DEFAULT_LIGHTS);
            setCustomSound(context, mBuilder);
        } else if (((note.getSound().equals("0")) && (!note.getVibration().equals("1")))) {
            mBuilder.setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_LIGHTS);
            setCustomVibration(mBuilder);
        } else if ((!note.getSound().equals("0")) && (!note.getVibration().equals("1"))) {
            mBuilder.setDefaults(Notification.DEFAULT_LIGHTS);
            setCustomSound(context, mBuilder);
            setCustomVibration(mBuilder);
        }
    }

    private void setCustomSound(Context context, NotificationCompat.Builder mBuilder) {
        try {
            mBuilder.setSound(Uri.parse(note.getSound()));
        } catch (Exception e) {

        }
    }

    private void setCustomVibration(NotificationCompat.Builder mBuilder) {
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
            note.setBirthday(current.getInt(11));

        }
        db.close();
    }

    private Intent[] makeIntent(Context context) {
        Intent main = new Intent(context, MainActivity.class);
        main.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        Intent delay = new Intent(context, CreateDelayedNote.class);
        delay.putExtra("id", note.getCheckId());
        delay.putExtra(Intent.EXTRA_SUBJECT, note.getTitle());
        delay.putExtra(Intent.EXTRA_TEXT, note.getText());
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
        if ((note.getRepeat() == 1) && (!note.getDays().equals("0;0;0;0;0;0;0;"))) {
            String[] split = note.getDays().split(";");
            if (split[getDayNum()].equals("1")) {
                return true;
            } else {
                return false;
            }
        } else {
            long now = Calendar.getInstance().getTimeInMillis();
            long set = note.getSetTime();
            if (now - 60000 < set) {
                return true;
            } else {
                return false;
            }
        }
    }
}
