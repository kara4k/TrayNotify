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
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

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

//    private void showTrayNotification(Context context) {
//        NotificationManagerCompat nm = NotificationManagerCompat.from(context);
//        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context);
//        mBuilder.setContentText(note.getText());
//        mBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText(note.getText()));
//        mBuilder.setContentTitle(note.getTitle());
//        setVibroSound(mBuilder);
//        mBuilder.setPriority(note.getPriority());
//        mBuilder.setContentInfo(String.valueOf(note.getCheckId()));
//        mBuilder.setAutoCancel(false);
//        mBuilder.setContentIntent(PendingIntent.getActivities(context, note.getCheckId(), makeIntent(context), PendingIntent.FLAG_UPDATE_CURRENT));
//        mBuilder.setOngoing(true);
//        mBuilder.setSmallIcon(R.drawable.notify);
//
//        if (note.getBirthday() != 0) {
//            setLargeIcon(context, mBuilder);
//        }
//
//        PendingIntent removePI = PendingIntent.getBroadcast(context, note.getCheckId(), actionRemoveIntent(context, note.getCheckId()), PendingIntent.FLAG_UPDATE_CURRENT);
//        mBuilder.addAction(R.drawable.ic_delete_sweep_white_24dp, context.getString(R.string.remove), removePI);
//
//        nm.notify(note.getCheckId(), mBuilder.build());
//    }

    private void showTrayNotification(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);

        NotificationManagerCompat nm = NotificationManagerCompat.from(context);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context);
        mBuilder.setSmallIcon(R.drawable.ic_alarm_on_white_24dp); // TODO: 19.09.2016
        setVibroSound(mBuilder);
        mBuilder.setPriority(note.getPriority());
        mBuilder.setContentIntent(getMainPI(context));
        mBuilder.setOngoing(true);



        RemoteViews smallView = getSmallViews(context, sp);
        mBuilder.setContent(smallView);


        RemoteViews bigView = getBigViews(context, sp);
        mBuilder.setCustomBigContentView(bigView);


        RemoteViews pushView = getPushViews(context, sp);
        mBuilder.setCustomHeadsUpContentView(pushView);
//        pushView.setViewVisibility(R.id.n_big_main_icon, View.VISIBLE);
//        pushView.setInt(R.id.n_big_layout, "setBackgroundColor", Color.BLACK);
//        pushView.setInt(R.id.n_big_text, "setTextColor", Color.WHITE);
//        pushView.setInt(R.id.n_big_title, "setTextColor", Color.WHITE);


        nm.notify(note.getCheckId(), mBuilder.build());
    }

    @NonNull
    private RemoteViews getPushViews(Context context, SharedPreferences sp) {
        RemoteViews pushView = new RemoteViews(context.getPackageName(), R.layout.notification_big);
        pushView.setTextViewText(R.id.n_big_title, note.getTitle());
        pushView.setTextViewText(R.id.n_big_text, note.getText());
        pushView.setViewVisibility(R.id.n_big_actions, View.GONE);

        int pushBackground = sp.getInt(Settings.REM_PUSH_BACKGROUND, Color.WHITE);
        pushView.setInt(R.id.n_big_layout, "setBackgroundColor", pushBackground);

        int pushTextColor = sp.getInt(Settings.REM_PUSH_TEXT_COLOR, Color.BLACK);
        pushView.setInt(R.id.n_big_title, "setTextColor", pushTextColor);
        pushView.setInt(R.id.n_big_text, "setTextColor", pushTextColor);

        pushView.setOnClickPendingIntent(R.id.n_big_layout, null);


        trySetPhotoIfBirthday(context, pushView, R.id.n_big_main_icon, Color.RED);
        return pushView;
    }

    @NonNull
    private RemoteViews getBigViews(Context context,SharedPreferences sp) {
        RemoteViews bigView = new RemoteViews(context.getPackageName(), R.layout.notification_big);
        bigView.setTextViewText(R.id.n_big_title, note.getTitle());
        bigView.setTextViewText(R.id.n_big_text, note.getText());
        bigView.setOnClickPendingIntent(R.id.n_big_actions, getMainPI(context));

        int background = sp.getInt(Settings.REM_BACKGROUND, Color.WHITE);
        Log.e("AlarmReceiver", "getSmallViews: " + background);
        bigView.setInt(R.id.n_big_layout, "setBackgroundColor", background);

        int textColor = sp.getInt(Settings.REM_TEXT, Color.BLACK);
        Log.e("AlarmReceiver", "getSmallViews: " + textColor);
        bigView.setInt(R.id.n_big_title, "setTextColor", textColor);
        bigView.setInt(R.id.n_big_text, "setTextColor", textColor);


        boolean showActions = sp.getBoolean(Settings.REM_SHOW_ACTIONS, true);

        if (showActions) {
            boolean showText = sp.getBoolean(Settings.REM_SHOW_ACTIONS_TEXT, true);
            if (!showText) {
                bigView.setViewVisibility(R.id.n_big_share_text, View.GONE);
                bigView.setViewVisibility(R.id.n_big_copy_text, View.GONE);
                bigView.setViewVisibility(R.id.n_big_close_text, View.GONE);

                int iconColor = sp.getInt(Settings.REM_ACTIONS_ICON_COLOR, Color.BLACK);
                bigView.setInt(R.id.n_big_share_icon, "setColorFilter", iconColor);
                bigView.setInt(R.id.n_big_copy_icon, "setColorFilter", iconColor);
                bigView.setInt(R.id.n_big_close_icon, "setColorFilter", iconColor);
            } else {
                int actionsTextColor = sp.getInt(Settings.REM_ACTIONS_TEXT_COLOR, Color.BLACK);
                bigView.setInt(R.id.n_big_share_text, "setTextColor", actionsTextColor);
                bigView.setInt(R.id.n_big_copy_text, "setTextColor", actionsTextColor);
                bigView.setInt(R.id.n_big_close_text, "setTextColor", actionsTextColor);

                int iconColor = sp.getInt(Settings.REM_ACTIONS_ICON_COLOR, Color.BLACK);
                bigView.setInt(R.id.n_big_share_icon, "setColorFilter", iconColor);
                bigView.setInt(R.id.n_big_copy_icon, "setColorFilter", iconColor);
                bigView.setInt(R.id.n_big_close_icon, "setColorFilter", iconColor);
            }


        } else {
            bigView.setViewVisibility(R.id.n_big_actions, View.GONE);
        }


        trySetPhotoIfBirthday(context, bigView, R.id.n_big_main_icon, textColor);


        bigView.setOnClickPendingIntent(R.id.n_big_share, getActionPI(context, note, 1));
        bigView.setOnClickPendingIntent(R.id.n_big_copy, getActionPI(context, note, 2));
        bigView.setOnClickPendingIntent(R.id.n_big_close, getActionPI(context, note, 3));
        return bigView;
    }



    @NonNull
    private RemoteViews getSmallViews(Context context, SharedPreferences sp) {
        RemoteViews smallView = new RemoteViews(context.getPackageName(), R.layout.notification);
        smallView.setTextViewText(R.id.n_text, getNText(context, note));

        int background = sp.getInt(Settings.REM_BACKGROUND, Color.WHITE);
        Log.e("AlarmReceiver", "getSmallViews: " + background);
        smallView.setInt(R.id.n_layout, "setBackgroundColor", background);

        int textColor = sp.getInt(Settings.REM_TEXT, Color.BLACK);
        Log.e("AlarmReceiver", "getSmallViews: " + textColor);
        smallView.setInt(R.id.n_text, "setTextColor", textColor);

        trySetPhotoIfBirthday(context, smallView, R.id.n_main_icon, textColor);
        return smallView;
    }



    private void trySetPhotoIfBirthday(Context context, RemoteViews views, int imageView, int color) {
        try {
            setPhotoIfBirthday(context, views, imageView, color);
        } catch (Exception e) {
            views.setViewVisibility(imageView, View.GONE);
        }
    }



    private void setPhotoIfBirthday(Context context, RemoteViews views, int imageView, int color) {
        if (ifBirthday()) {
            views.setViewVisibility(imageView, View.VISIBLE);
            try {
                Bitmap mBitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), Uri.parse("content://com.android.contacts/contacts/" + note.getBirthday() + "/display_photo"));
                views.setImageViewBitmap(imageView, mBitmap);
            } catch (IOException e) {
                views.setImageViewResource(imageView, BirthdayFragment.getZodiacSign(parseDate()));
                views.setInt(imageView, "setColorFilter", color);
            }
        }
    }

    private String parseDate() {
        Calendar birthday = Calendar.getInstance();
        birthday.setTimeInMillis(note.getSetTime());
        int year = birthday.get(Calendar.YEAR);
        int month = birthday.get(Calendar.MONTH);
        int day = birthday.get(Calendar.DAY_OF_MONTH);
        return String.valueOf(year) + "-" + String.valueOf(month) + "-" + String.valueOf(day);
    }

    private boolean ifBirthday() {
        if (note.getBirthday() != 0) {
            return true;
        } else {
            return false;
        }
    }

    public static String getNText(Context context, DelayedNote note) {
        if (!note.getText().equals("")) {
            return note.getText();
        } else if (!note.getTitle().equals("")) {
            return note.getTitle();
        } else return context.getString(R.string.app_name);
    }

    private PendingIntent getActionPI(Context context, DelayedNote note, int action) {
        int pIid = Integer.parseInt(String.valueOf(note.getCheckId()).concat(String.valueOf(action)));
        Log.e("QuickNote", "getActionPI: " + pIid);
        return PendingIntent.getBroadcast(context, pIid, getActionIntent(context, note, action), PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private Intent getActionIntent(Context context, DelayedNote note, int action) {
        Intent intent = new Intent(context, NActionReceiver.class);
        intent.putExtra(NActionReceiver.TYPE, 2);
        intent.putExtra(NActionReceiver.ID, note.getCheckId());
        intent.putExtra(NActionReceiver.ACTION, action);
        return intent;
    }

    private PendingIntent getMainPI(Context context) {
        return PendingIntent.getActivities(context, note.getCheckId(), makeIntent(context), PendingIntent.FLAG_UPDATE_CURRENT);
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
