package com.kara4k.traynotify;


import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.NotificationCompat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.RemoteViews;

import java.util.Calendar;

public class QuickNote extends AppCompatActivity {

    private EditText title;
    private EditText text;
    private NotificationManagerCompat nm;
    private SharedPreferences sp;

    private MyView tray;

    private int id;
    private DBQuick dbQuick;
    private Calendar calendar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.quick_note);

        trySetDefaultAsHomeEnabled();

        sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        title = (EditText) findViewById(R.id.editTitle);
        text = (EditText) findViewById(R.id.textedit);
        tray = (MyView) findViewById(R.id.tray);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);

        dbQuick = new DBQuick(getApplicationContext());
        id = dbQuick.getNoteCheckID();

        nm = NotificationManagerCompat.from(this);

        calendar = Calendar.getInstance();

        setDefaultsTrayChecked();

        intentChecks();



        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                create();
            }
        });


    }

    public void setDefaultsTrayChecked() {
        boolean trayChecked = sp.getBoolean(Settings.QUICK_DEFAULT_IN_TRAY, false);
        if (trayChecked) {
            tray.getCheckbox().setChecked(true);
        } else {
            tray.getCheckbox().setChecked(false);
        }
    }

    private void trySetDefaultAsHomeEnabled() {
        try {
            getSupportActionBar().setDefaultDisplayHomeAsUpEnabled(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void intentChecks() {
        if (getIntent().getExtras() != null) {
            title.setText(getIntent().getStringExtra(Intent.EXTRA_SUBJECT));
            text.setText(getIntent().getStringExtra(Intent.EXTRA_TEXT));
            id = getIntent().getIntExtra("id", id);
            boolean trayChecked = sp.getBoolean(Settings.QUICK_DEFAULT_IN_TRAY, false);
            tray.getCheckbox().setChecked(getIntent().getBooleanExtra("inTray", trayChecked));
        }
    }




    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.quick_menu, menu);
        ifShowCloseIcon(menu);
        return true;
    }

    public void ifShowCloseIcon(Menu menu) {
        boolean showActions = sp.getBoolean(Settings.QUICK_SHOW_ACTIONS, true);
        MenuItem removeCurrent = menu.findItem(R.id.remove_current_n);
        if (showActions) {
            removeCurrent.setVisible(false);
        } else {
            removeCurrent.setVisible(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.share:
                sendIntent();
                break;
            case R.id.clear_forms:
                clearForms();
                break;
            case R.id.remove_current_n:
                removeFromTray();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void removeFromTray() {
        nm.cancel(id);
        DBQuick db = new DBQuick(getApplicationContext());
        db.open();
        db.setQuickTrayInDB(id, 0);
        db.close();
    }


    private void sendIntent() {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_SUBJECT, title.getText().toString());
        sendIntent.putExtra(Intent.EXTRA_TEXT, text.getText().toString());
        sendIntent.setType("text/plain");
        startActivity(sendIntent);
    }

    private void clearForms() {
        title.setText("");
        text.setText("");
        setDefaultsTrayChecked();

    }

    private void create() {

        if (tray.getCheckbox().isChecked()) {
            createTray();
            writeToDB();
        } else {
            writeToDB();
            nm.cancel(id);
        }

        updateWidgetIfExist();

        finish();

    }

    private void updateWidgetIfExist() {
        SharedPreferences sp = getSharedPreferences(WidgetConfig.WIDGET_CONF, Context.MODE_PRIVATE);
        int widgetID = sp.getInt("#" + id, -1);
        if (widgetID != -1) {
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(getApplicationContext());
            Widget.updateWidget(this, appWidgetManager, sp, widgetID);
        }
    }

//    private void createNote() {
//        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getApplicationContext());
//        if (title.getText().toString().equals("")) {
//            mBuilder.setContentTitle(getString(R.string.app_name));
//        } else {
//            mBuilder.setContentTitle(title.getText().toString());
//        }
//        mBuilder.setContentText(text.getText().toString());
//        mBuilder.setContentInfo("#" + String.valueOf(id).substring(1));
//        mBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText(text.getText().toString()));
//        mBuilder.setOngoing(true);
//        mBuilder.setContentIntent(getMainPI());
//        mBuilder.setSmallIcon(R.drawable.notify);
//
//
//        PendingIntent removePI = PendingIntent.getBroadcast(getApplicationContext(), id, actionRemoveIntent(), PendingIntent.FLAG_UPDATE_CURRENT);
//        mBuilder.addAction(R.drawable.ic_delete_sweep_white_24dp, getString(R.string.remove), removePI);
//
//
////        mBuilder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.main_icon));
//        nm.notify(id, mBuilder.build());
//
//    }

    private void createTray() {
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getApplicationContext());
        mBuilder.setSmallIcon(R.drawable.ic_description_white_24dp); // TODO: 19.09.2016
        mBuilder.setContentIntent(getMainPI());
        mBuilder.setOngoing(true);


        RemoteViews smallView = getSmallViews();
        mBuilder.setContent(smallView);

        RemoteViews bigView = getBigViews();
        mBuilder.setCustomBigContentView(bigView);



        nm.notify(id, mBuilder.build());
    }

    @NonNull
    private RemoteViews getBigViews() {
        RemoteViews bigView = new RemoteViews(getPackageName(), R.layout.notification_big);
        bigView.setTextViewText(R.id.n_big_title, getTitleName());
        bigView.setTextViewText(R.id.n_big_text, text.getText().toString());
        bigView.setOnClickPendingIntent(R.id.n_big_actions,getMainPI());

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
                bigView.setOnClickPendingIntent(R.id.n_big_share_icon2,getActionPI(1));
                bigView.setOnClickPendingIntent(R.id.n_big_copy_icon2,getActionPI(2));
                bigView.setOnClickPendingIntent(R.id.n_big_close_icon2,getActionPI(3));



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

                bigView.setOnClickPendingIntent(R.id.n_big_share,getActionPI(1));
                bigView.setOnClickPendingIntent(R.id.n_big_copy,getActionPI(2));
                bigView.setOnClickPendingIntent(R.id.n_big_close,getActionPI(3));
            }

        }



        return bigView;
    }

    @NonNull
    private RemoteViews getSmallViews() {
        RemoteViews smallView = new RemoteViews(getPackageName(), R.layout.notification);
        smallView.setTextViewText(R.id.n_text, getSmallViewText());

        int background = sp.getInt(Settings.QUICK_BACKGROUND, Color.WHITE);
        smallView.setInt(R.id.n_layout, "setBackgroundColor", background);

        int textColor = sp.getInt(Settings.QUICK_TEXT, Color.BLACK);
        smallView.setInt(R.id.n_text, "setTextColor", textColor);

        return smallView;
    }

    private String getSmallViewText() {
        if (!text.getText().toString().equals("")) {
            return text.getText().toString();
        } else if (!title.getText().toString().equals("")) {
            return title.getText().toString();
        } else {
            return getString(R.string.app_name);
        }
    }

    private PendingIntent getActionPI(int action) {
        int pIid= Integer.parseInt(String.valueOf(id).concat(String.valueOf(action)));
        return PendingIntent.getBroadcast(getApplicationContext(),pIid, getActionIntent(action), PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private Intent getActionIntent(int action) {
        Intent intent = new Intent(getApplicationContext(), NActionReceiver.class);
        intent.putExtra(NActionReceiver.TYPE, 1);
        intent.putExtra(NActionReceiver.ID, id);
        intent.putExtra(NActionReceiver.ACTION, action);
        return intent;
    }

    private PendingIntent getMainPI() {
        return PendingIntent.getActivities(getApplicationContext(), id, makeIntent(), PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private String getTitleName() {
        if (!title.getText().toString().equals("")) {
            return title.getText().toString();
        } else {
            return getString(R.string.app_name);
        }
    }


    private Intent[] makeIntent() {
        Intent main = new Intent(getApplicationContext(), MainActivity.class);
        main.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        Intent quick = new Intent(getApplicationContext(), QuickNote.class);

        quick.putExtra(Intent.EXTRA_SUBJECT, title.getText().toString());
        quick.putExtra(Intent.EXTRA_TEXT, text.getText().toString());
        quick.putExtra("id", id);
        quick.putExtra("inTray", true);
        return new Intent[]{main, quick};
    }

    private void writeToDB() {
        dbQuick.open();
        Cursor currentNote = dbQuick.getCurrentNote(id);
        if (currentNote.moveToFirst()) {
            dbQuick.updateRec(getTitleText(), text.getText().toString(), getTray(), calendar.getTimeInMillis(), id);
        } else {
            dbQuick.addNote(getTitleText(), text.getText().toString(), getTray(), calendar.getTimeInMillis(), id);
            dbQuick.close();
        }

    }

    private String getTitleText() {
        if (title.getText().toString().equals("")) {
            return getString(R.string.app_name);
        } else {
            return title.getText().toString();
        }
    }

    private int getTray() {
        if (tray.getCheckbox().isChecked()) {
            return 1;
        } else {
            return 0;
        }
    }


}
