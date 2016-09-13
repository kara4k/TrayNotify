package com.kara4k.traynotify;


import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.Notification;
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
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.NotificationCompat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class AlarmReceiver extends BroadcastReceiver {


    private DelayedNote note;

    @Override
    public void onReceive(Context context, Intent intent) {

        fillNote(intent, context);

        checkIfShowTrayNotification(context);
        repeatAlarm(context);
    }

    private void checkIfShowTrayNotification(Context context) {
        if (ifNotify()) {
            showTrayNotification(context);
        }
    }


    private void repeatAlarm(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, note.getCheckId(), createAlarmIntent(context), PendingIntent.FLAG_UPDATE_CURRENT);
        int repeat = note.getRepeat();
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            setBeforeKITKATAlarm(alarmManager, pendingIntent, repeat);
        } else {
            setAfterKITKATAlarm(alarmManager, pendingIntent, repeat);
        }
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void setAfterKITKATAlarm(AlarmManager alarmManager, PendingIntent pendingIntent, int repeat) {
        if (repeat == 1) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, getWeekRepeatTime(), pendingIntent);
        }
        if (repeat == 2) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, getMonthRepeatTime(), pendingIntent);
        }
        if (repeat == 3) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, getYearRepeatTime(), pendingIntent);
        }
    }

    private void setBeforeKITKATAlarm(AlarmManager alarmManager, PendingIntent pendingIntent, int repeat) {
        if (repeat == 2) {
            alarmManager.set(AlarmManager.RTC_WAKEUP, getMonthRepeatTime(), pendingIntent);
        }
        if (repeat == 3) {
            alarmManager.set(AlarmManager.RTC_WAKEUP, getYearRepeatTime(), pendingIntent);
        }
    }

    @NonNull
    private Intent createAlarmIntent(Context context) {
        Intent alarmIntent = new Intent(context, AlarmReceiver.class);
        Bundle bundle = new Bundle();
        bundle.putInt("id", note.getCheckId());
        alarmIntent.putExtras(bundle);
        return alarmIntent;
    }


    private long getWeekRepeatTime() {
        Calendar setCal = Calendar.getInstance();
        setCal.setTimeInMillis(note.getSetTime());
        Calendar now = Calendar.getInstance();
        int nowDay = now.get(Calendar.DAY_OF_MONTH);
        return nowDayEqualsSetDay(setCal, now, nowDay, Calendar.DAY_OF_MONTH);
    }


    private long getYearRepeatTime() {
        Calendar setCal = Calendar.getInstance();
        setCal.setTimeInMillis(note.getSetTime());
        Calendar now = Calendar.getInstance();
        setCal.set(Calendar.YEAR, now.get(Calendar.YEAR));
        if (setCal.get(Calendar.MONTH) < now.get(Calendar.MONTH)) {
            setCal.add(Calendar.YEAR, 1);
            setZeroSeconds(setCal);
            return setCal.getTimeInMillis();
        } else if (setCal.get(Calendar.MONTH) > now.get(Calendar.MONTH)) {
            return setCal.getTimeInMillis();
        } else if (setCal.get(Calendar.MONTH) == now.get(Calendar.MONTH)) {
            int setDay = setCal.get(Calendar.DAY_OF_MONTH);
            int nowDay = now.get(Calendar.DAY_OF_MONTH);
            if (nowDay > setDay) {
                return nowDayMoreThenSetDay(setCal, now, Calendar.YEAR);
            } else if (nowDay < setDay) {
                return nowDayLessTHenSetDay(setCal, now);
            } else if (nowDay == setDay) {
                return nowDayEqualsSetDay(setCal, now, nowDay, Calendar.YEAR);
            }
        }
        return 0;
    }


    private long getMonthRepeatTime() {
        Calendar setCal = Calendar.getInstance();
        setCal.setTimeInMillis(note.getSetTime());
        Calendar now = Calendar.getInstance();
        int setDay = setCal.get(Calendar.DAY_OF_MONTH);
        int nowDay = now.get(Calendar.DAY_OF_MONTH);
        if (nowDay > setDay) {
            return nowDayMoreThenSetDay(setCal, now, Calendar.MONTH);
        } else if (nowDay == setDay) {
            return nowDayEqualsSetDay(setCal, now, nowDay, Calendar.MONTH);
        } else if (nowDay < setDay) {
            return nowDayLessTHenSetDay(setCal, now);
        }
        return 0;

    }

    private long nowDayEqualsSetDay(Calendar setCal, Calendar now, int nowDay, int field) {
        setCal.set(now.get(Calendar.YEAR), now.get(Calendar.MONTH), nowDay);
        if (setCal.get(Calendar.HOUR_OF_DAY) < now.get(Calendar.HOUR_OF_DAY)) {
            setCal.add(field, 1);
            setZeroSeconds(setCal);
            return setCal.getTimeInMillis();
        } else if (setCal.get(Calendar.HOUR_OF_DAY) > now.get(Calendar.HOUR_OF_DAY)) {
            setZeroSeconds(setCal);
            return setCal.getTimeInMillis();
        } else if (setCal.get(Calendar.HOUR_OF_DAY) == now.get(Calendar.HOUR_OF_DAY)) {
            if (setCal.get(Calendar.MINUTE) <= now.get(Calendar.MINUTE)) {
                setCal.add(field, 1);
                setZeroSeconds(setCal);
                return setCal.getTimeInMillis();
            } else if (setCal.get(Calendar.MINUTE) > now.get(Calendar.MINUTE)) {
                setZeroSeconds(setCal);
                return setCal.getTimeInMillis();
            }
        }
        return 0;
    }

    private long nowDayLessTHenSetDay(Calendar setCal, Calendar now) {
        setCal.set(Calendar.MONTH, now.get(Calendar.MONTH));
        setCal.set(Calendar.YEAR, now.get(Calendar.YEAR));
        setZeroSeconds(setCal);
        return setCal.getTimeInMillis();
    }

    private long nowDayMoreThenSetDay(Calendar setCal, Calendar now, int field) {
        now.add(field, 1);
        setCal.set(Calendar.MONTH, now.get(Calendar.MONTH));
        setCal.set(Calendar.YEAR, now.get(Calendar.YEAR));
        setZeroSeconds(setCal);
        return setCal.getTimeInMillis();
    }

    private void setZeroSeconds(Calendar setCal) {
        setCal.set(Calendar.SECOND, 00);
        setCal.set(Calendar.MILLISECOND, 0000);
    }

    private void showTrayNotification(Context context) {
        NotificationManagerCompat nm = NotificationManagerCompat.from(context);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context);
        mBuilder.setContentText(note.getText());
        mBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText(note.getText()));
        mBuilder.setContentTitle(note.getTitle());
        setVibroSound(mBuilder);
        mBuilder.setPriority(note.getPriority());
        mBuilder.setContentInfo(String.valueOf(note.getCheckId()));
        mBuilder.setAutoCancel(false);
        mBuilder.setContentIntent(PendingIntent.getActivities(context, note.getCheckId(), makeIntent(context), PendingIntent.FLAG_UPDATE_CURRENT));
        mBuilder.setOngoing(true);
        mBuilder.setSmallIcon(R.drawable.notify);

        if (note.getBirthday() != 0) {
            setLargeIcon(context, mBuilder);
        }

        PendingIntent removePI = PendingIntent.getBroadcast(context, note.getCheckId(), actionRemoveIntent(context, note.getCheckId()), PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.addAction(R.drawable.ic_delete_sweep_white_24dp, context.getString(R.string.remove), removePI);

        nm.notify(note.getCheckId(), mBuilder.build());
    }

    private Intent actionRemoveIntent(Context context, int id) {
        Intent intent = new Intent(context, NActionReceiver.class);
        intent.putExtra("type", 2);
        intent.putExtra("id", id);
        return intent;
    }

    private void setLargeIcon(Context context, NotificationCompat.Builder mBuilder) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            mBuilder.setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.main_icon));
        } else {
            tryBirthdayPhoto(context, mBuilder);
        }
    }

    private void tryBirthdayPhoto(Context context, NotificationCompat.Builder mBuilder) {
        if (note.getBirthday() != 0) {
            try {
                Bitmap mBitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), Uri.parse("content://com.android.contacts/contacts/" + note.getBirthday() + "/display_photo"));
                mBuilder.setLargeIcon(mBitmap);
            } catch (IOException e) {
                mBuilder.setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.main_icon));
            }
        } else {
            mBuilder.setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.main_icon));
        }
    }


    private void setVibroSound(NotificationCompat.Builder mBuilder) {
        if ((note.getSound().equals("0")) && (note.getVibration().equals("1"))) {
            mBuilder.setDefaults(Notification.DEFAULT_ALL);
        } else if ((!(note.getSound().equals("0")) && (note.getVibration().equals("1")))) {
            mBuilder.setDefaults(Notification.DEFAULT_VIBRATE | Notification.DEFAULT_LIGHTS);
            setCustomSound(mBuilder);
        } else if (((note.getSound().equals("0")) && (!note.getVibration().equals("1")))) {
            mBuilder.setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_LIGHTS);
            setCustomVibration(mBuilder);
        } else if ((!note.getSound().equals("0")) && (!note.getVibration().equals("1"))) {
            mBuilder.setDefaults(Notification.DEFAULT_LIGHTS);
            setCustomSound(mBuilder);
            setCustomVibration(mBuilder);
        }
    }

    private void setCustomSound(NotificationCompat.Builder mBuilder) {
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

    private void fillNote(Intent intent, Context context) {
        try {
            DBDelay db = new DBDelay(context);
            db.open();
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
        } catch (Exception e) {
        }
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


    private boolean ifNotify() {
        int repeat = note.getRepeat();
        if (repeat == 1) {
            return checkWeekRepeat();
        } else if (repeat == 0) {
            return singleCheck();
        } else if (repeat == 2) {
            return checkMonthRepeat();
        } else if (repeat == 3) {
            return checkYearRepeat();
        }
        return true;
    }

    private boolean checkYearRepeat() {
        Calendar setCal = Calendar.getInstance();
        setCal.setTimeInMillis(note.getSetTime());
        Calendar now = Calendar.getInstance();
        if ((setCal.get(Calendar.MONTH) == now.get(Calendar.MONTH))
                && (setCal.get(Calendar.DAY_OF_MONTH) == now.get(Calendar.DAY_OF_MONTH))) {
            return timeCheck(setCal, now);
        } else return false;
    }

    private boolean singleCheck() {
        Calendar setCal = Calendar.getInstance();
        setCal.setTimeInMillis(note.getSetTime());
        Calendar now = Calendar.getInstance();
        if ((setCal.get(Calendar.YEAR) == now.get(Calendar.YEAR)) &&
                (setCal.get(Calendar.MONTH) == now.get(Calendar.MONTH)) &&
                (setCal.get(Calendar.DAY_OF_MONTH) == now.get(Calendar.DAY_OF_MONTH))) {
            return timeCheck(setCal, now);
        } else return false;

    }

    private boolean checkWeekRepeat() {
        String[] split = note.getDays().split(";");
        if (split[getDayNum()].equals("1")) {
            Calendar setCal = Calendar.getInstance();
            setCal.setTimeInMillis(note.getSetTime());
            Calendar now = Calendar.getInstance();
            return timeCheck(setCal, now);
        } else {
            return false;
        }
    }

    private boolean timeCheck(Calendar setCal, Calendar now) {

        if ((setCal.get(Calendar.HOUR) == now.get(Calendar.HOUR))
                && (setCal.get(Calendar.MINUTE) == now.get(Calendar.MINUTE))) {
            return true;
        } else
            return false;
    }

    private boolean checkMonthRepeat() {
        Calendar setCal = Calendar.getInstance();
        setCal.setTimeInMillis(note.getSetTime());
        Calendar now = Calendar.getInstance();
        if ((setCal.get(Calendar.DAY_OF_MONTH) == now.get(Calendar.DAY_OF_MONTH))) {
            return timeCheck(setCal, now);
        } else {
            return false;
        }
    }
}
