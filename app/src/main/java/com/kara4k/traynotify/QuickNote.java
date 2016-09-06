package com.kara4k.traynotify;


import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.util.Calendar;

public class QuickNote extends AppCompatActivity {

    private EditText title;
    private EditText text;
    private NotificationManagerCompat nm;

    private MyView tray;

    private int id;
    private DBQuick dbQuick;
    private Calendar calendar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.quick_note);

        trySetDefaultAsHomeEnabled();


        title = (EditText) findViewById(R.id.editTitle);
        text = (EditText) findViewById(R.id.textedit);
        tray = (MyView) findViewById(R.id.tray);
        Button create = (Button) findViewById(R.id.create);

        dbQuick = new DBQuick(getApplicationContext());
        id = dbQuick.getNoteCheckID();

        nm = NotificationManagerCompat.from(this);

        calendar = Calendar.getInstance();
        intentChecks();


        create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                create();
            }
        });


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
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.quick_menu, menu);
        return true;
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
            case R.id.action_clear_note:
                clearTrayCurrent();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void clearTrayCurrent() {
        try {
            nm.cancel(id);
            Log.e("TAG", "onOptionsItemSelected:" + id);
            DBQuick db = new DBQuick(getApplicationContext());
            db.open();
            db.setQuickTrayInDB(id, 0);
            db.close();
        } catch (Exception e) {
        }
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
        tray.getCheckbox().setChecked(true);

    }

    private void create() {

        if (tray.getCheckbox().isChecked()) {
            createNote();
            writeToDB();
        } else {
            writeToDB();
            nm.cancel(id);
        }

        SharedPreferences sp = getSharedPreferences(WidgetConfig.WIDGET_CONF, Context.MODE_PRIVATE);
        int widgetID = sp.getInt("#" + id, -1);
        if (widgetID != -1) {
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(getApplicationContext());
            Widget.updateWidget(this, appWidgetManager, sp, widgetID);
        }

        setResult(RESULT_OK);
        finish();

    }

    private void createNote() {
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getApplicationContext());
        if (title.getText().toString().equals("")) {
            mBuilder.setContentTitle(getString(R.string.app_name));
        } else {
            mBuilder.setContentTitle(title.getText().toString());
        }
        mBuilder.setContentText(text.getText().toString());
        mBuilder.setContentInfo("#" + String.valueOf(id).substring(1));
        mBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText(text.getText().toString()));
        mBuilder.setOngoing(true);
        mBuilder.setContentIntent(PendingIntent.getActivities(getApplicationContext(), id, makeIntent(), PendingIntent.FLAG_UPDATE_CURRENT));
        mBuilder.setSmallIcon(R.drawable.notify);


        mBuilder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.user1));
        nm.notify(id, mBuilder.build());

    }

    private Intent[] makeIntent() {
        Intent main = new Intent(getApplicationContext(), MainActivity.class);
        main.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        Intent quick = new Intent(getApplicationContext(), QuickNote.class);

        quick.putExtra(Intent.EXTRA_SUBJECT, title.getText().toString());
        quick.putExtra(Intent.EXTRA_TEXT, text.getText().toString());
        quick.putExtra("id", id);
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


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        setResult(RESULT_CANCELED);
    }
}
