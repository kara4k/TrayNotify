package com.kara4k.traynotify;


import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.NotificationCompat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.Calendar;

public class QuickNote extends AppCompatActivity {

    private EditText title;
    private EditText text;
    private Button create;
    private SeekBar seekbar;
    private Button delete;
    private NotificationManager nm;
    private TextView textId;

    private LinearLayout advancedLayout;
    private LinearLayout seekLayout;
    private MyView tray;
    private MyView ongoing;

    private int id;
    private SharedPreferences sp;
    private DBQuick dbQuick;
    private Calendar calendar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.quick_note);

        getSupportActionBar().setDefaultDisplayHomeAsUpEnabled(true);

        title = (EditText) findViewById(R.id.editTitle);
        text = (EditText) findViewById(R.id.textedit);
        tray = (MyView) findViewById(R.id.tray);
        ongoing = (MyView) findViewById(R.id.ongoing);
        create = (Button) findViewById(R.id.create);

        dbQuick = new DBQuick(getApplicationContext());
        id = dbQuick.getNoteCheckID();

        nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        calendar = Calendar.getInstance();
        intentChecks();
        tray.setSecondOnClickListener(new MyView.SecondOnClickListener() {
            @Override
            public void onClick() {
                if (!tray.getCheckbox().isChecked()) {
                    ongoing.getCheckbox().setChecked(false);
                    ongoing.getCheckbox().setEnabled(false);
                } else {
                    ongoing.getCheckbox().setEnabled(true);
                }
            }
        });

        create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                create();
            }
        });


    }

    private void intentChecks() {
        if (getIntent().getExtras() != null) {
            title.setText(getIntent().getStringExtra(Intent.EXTRA_SUBJECT));
            text.setText(getIntent().getStringExtra(Intent.EXTRA_TEXT));
            ongoing.getCheckbox().setChecked((getIntent().getBooleanExtra("ongoing", true)));
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
                nm.cancel(id);
                break;
        }
        return super.onOptionsItemSelected(item);
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
        ongoing.getCheckbox().setChecked(true);
        ongoing.getCheckbox().setEnabled(true);

    }

    public void create() {

        if (tray.getCheckbox().isChecked()) {
            createNote();
            writeToDB();
        } else {
            writeToDB();
            finish();
        }

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
        mBuilder.setOngoing(ongoing.getCheckbox().isChecked());
        mBuilder.setDefaults(Notification.DEFAULT_ALL);
        mBuilder.setContentIntent(PendingIntent.getActivities(getApplicationContext(), id, makeIntent(), PendingIntent.FLAG_UPDATE_CURRENT));
        mBuilder.setSmallIcon(R.drawable.notify);


        mBuilder.setLargeIcon(BitmapFactory.decodeResource(getResources(),R.drawable.user1 ));     // TODO: 12.08.2016
        nm.notify(id, mBuilder.build());
        finish();


    }

    private Intent[] makeIntent() {
        Intent main = new Intent(getApplicationContext(), MainActivity.class);
        main.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        Intent quick = new Intent(getApplicationContext(), QuickNote.class);

        quick.putExtra(Intent.EXTRA_SUBJECT, title.getText().toString());
        quick.putExtra(Intent.EXTRA_TEXT, text.getText().toString());
        quick.putExtra("ongoing", ongoing.getCheckbox().isChecked());
        quick.putExtra("id", id);
        return new Intent[]{main, quick};
    }

    private void writeToDB() {
        dbQuick.open();
        Cursor currentNote = dbQuick.getCurrentNote(id);
        if (currentNote.moveToFirst()) {
            if (title.getText().toString().equals("")) {
                dbQuick.updateRec("TrayNotify", text.getText().toString(), calendar.getTimeInMillis(), id);
            } else {
                dbQuick.updateRec(title.getText().toString(), text.getText().toString(), calendar.getTimeInMillis(), id);
            }
        } else {

            if (title.getText().toString().equals("")) {
                dbQuick.addNote("TrayNotify", text.getText().toString(), calendar.getTimeInMillis(), id);
            } else {
                dbQuick.addNote(title.getText().toString(), text.getText().toString(), calendar.getTimeInMillis(), id);
            }
            dbQuick.close();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        setResult(RESULT_OK);
    }
}
