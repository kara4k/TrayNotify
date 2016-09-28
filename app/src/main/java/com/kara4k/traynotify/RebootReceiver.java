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
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.NotificationCompat;
import android.view.View;
import android.widget.RemoteViews;

import java.util.List;

public class RebootReceiver extends BroadcastReceiver {


    private SharedPreferences sp;

    @Override
    public void onReceive(Context context, Intent intent) {

        sp = PreferenceManager.getDefaultSharedPreferences(context);

        setReminders(context);
        showNotes(context);
        startClipTracking(context);

    }

    private void startClipTracking(Context context) {

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
//                nm.notify(x.getNumid(), createNotification(context, x));
                nm.notify(x.getNumid(), makeNotification(context, x));
            }
        }
    }

    public static void setReminders(Context context) {
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
    public static void startAfterKitKatAlarm(Cursor allData, AlarmManager alarmManager, PendingIntent pendingIntent) {
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, allData.getLong(4), pendingIntent);
    }

    public static void startBeforeKITKATAlarm(Cursor allData, AlarmManager alarmManager, PendingIntent pendingIntent) {
        int repeat = allData.getInt(5);
        if (repeat == 1) {
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, allData.getLong(4), 24 * 60 * 60 * 1000, pendingIntent);
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, allData.getLong(4), pendingIntent);
        }
    }

    @NonNull
    public static Intent getAlarmIntent(Context context, Cursor allData) {
        Intent alarmIntent = new Intent(context, AlarmReceiver.class);
        Bundle bundle = new Bundle();
        bundle.putInt("id", allData.getInt(10));
        alarmIntent.putExtras(bundle);
        return alarmIntent;
    }


    public static Notification makeNotification(Context context, Note note) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context);
        mBuilder.setSmallIcon(R.drawable.ic_description_white_24dp);
        mBuilder.setContentIntent(getMainPI(context, note));
        mBuilder.setOngoing(true);


        RemoteViews smallView = getSmallViews(context, note, sp);
        mBuilder.setContent(smallView);

        RemoteViews bigView = getBigViews(context, note, sp);

        mBuilder.setCustomBigContentView(bigView);

        return mBuilder.build();
    }

    @NonNull
    public static RemoteViews getBigViews(Context context, Note note, SharedPreferences sp) {
        RemoteViews bigView = new RemoteViews(context.getPackageName(), R.layout.notification_big);
        bigView.setTextViewText(R.id.n_big_title, note.getTitle());
        bigView.setTextViewText(R.id.n_big_text, note.getText());
        bigView.setOnClickPendingIntent(R.id.n_big_actions, getMainPI(context,note));

        int background = sp.getInt(Settings.QUICK_BACKGROUND, Color.WHITE);
        bigView.setInt(R.id.n_big_layout, "setBackgroundColor", background);

        int textColor = sp.getInt(Settings.QUICK_TEXT, Color.BLACK);
        bigView.setInt(R.id.n_big_title, "setTextColor", textColor);
        bigView.setInt(R.id.n_big_text, "setTextColor", textColor);





        boolean showActions = sp.getBoolean(Settings.QUICK_SHOW_ACTIONS, true);
        if (showActions) {

            boolean showText = sp.getBoolean(Settings.QUICK_SHOW_ACTIONS_TEXT, true);
            if (!showText) {



                bigView.setViewVisibility(R.id.n_big_actions2, View.VISIBLE);
                int iconColor = sp.getInt(Settings.QUICK_ACTIONS_ICON_COLOR, Color.BLACK);
                bigView.setInt(R.id.n_big_share_icon2, "setColorFilter", iconColor);
                bigView.setInt(R.id.n_big_copy_icon2, "setColorFilter", iconColor);
                bigView.setInt(R.id.n_big_close_icon2, "setColorFilter", iconColor);
                bigView.setOnClickPendingIntent(R.id.n_big_share_icon2,getActionPI(context, note,1));
                bigView.setOnClickPendingIntent(R.id.n_big_copy_icon2,getActionPI(context, note,2));
                bigView.setOnClickPendingIntent(R.id.n_big_close_icon2,getActionPI(context, note,3));



            } else {
                bigView.setViewVisibility(R.id.n_big_actions, View.VISIBLE);

                int actionsTextColor = sp.getInt(Settings.QUICK_ACTIONS_TEXT_COLOR, Color.BLACK);
                bigView.setInt(R.id.n_big_share_text, "setTextColor", actionsTextColor);
                bigView.setInt(R.id.n_big_copy_text, "setTextColor", actionsTextColor);
                bigView.setInt(R.id.n_big_close_text, "setTextColor", actionsTextColor);

                int iconColor = sp.getInt(Settings.QUICK_ACTIONS_ICON_COLOR, Color.BLACK);
                bigView.setInt(R.id.n_big_share_icon, "setColorFilter", iconColor);
                bigView.setInt(R.id.n_big_copy_icon, "setColorFilter", iconColor);
                bigView.setInt(R.id.n_big_close_icon, "setColorFilter", iconColor);

                bigView.setOnClickPendingIntent(R.id.n_big_share,getActionPI(context, note,1));
                bigView.setOnClickPendingIntent(R.id.n_big_copy,getActionPI(context, note,2));
                bigView.setOnClickPendingIntent(R.id.n_big_close,getActionPI(context, note,3));
            }

        }

        return bigView;
    }

    @NonNull
    public static RemoteViews getSmallViews(Context context, Note note, SharedPreferences sp) {
        RemoteViews smallView = new RemoteViews(context.getPackageName(), R.layout.notification);
        smallView.setTextViewText(R.id.n_text, getNText(context, note));

        int background = sp.getInt(Settings.QUICK_BACKGROUND, Color.WHITE);
        smallView.setInt(R.id.n_layout, "setBackgroundColor", background);

        int textColor = sp.getInt(Settings.QUICK_TEXT, Color.BLACK);
        smallView.setInt(R.id.n_text, "setTextColor", textColor);

        return smallView;
    }

    public static String getNText(Context context,Note note) {
        if (!note.getText().equals("")) {
            return note.getText();
        } else if (!note.getTitle().equals("")) {
            return note.getTitle();
        } else return context.getString(R.string.app_name);
    }


    public static PendingIntent getActionPI(Context context, Note note, int action) {
        int pIid = Integer.parseInt(String.valueOf(note.getNumid()).concat(String.valueOf(action)));
        return PendingIntent.getBroadcast(context, pIid, getActionIntent(context,note, action), PendingIntent.FLAG_UPDATE_CURRENT);
    }

    public static Intent getActionIntent(Context context, Note note, int action) {
        Intent intent = new Intent(context, NActionReceiver.class);
        intent.putExtra(NActionReceiver.TYPE, 1);
        intent.putExtra(NActionReceiver.ID, note.getNumid());
        intent.putExtra(NActionReceiver.ACTION, action);
        return intent;
    }

    public static PendingIntent getMainPI(Context context, Note note) {
        return PendingIntent.getActivities(context, note.getNumid(), makeIntent(context, note), PendingIntent.FLAG_UPDATE_CURRENT);
    }


    public static Intent[] makeIntent(Context context, Note note) {
        Intent main = new Intent(context, MainActivity.class);
        main.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        Intent quick = new Intent(context, QuickNote.class);

        quick.putExtra(Intent.EXTRA_SUBJECT, note.getTitle());
        quick.putExtra(Intent.EXTRA_TEXT, note.getText());
        quick.putExtra("id", note.getNumid());
        quick.putExtra("inTray", true);
        return new Intent[]{main, quick};
    }

}
