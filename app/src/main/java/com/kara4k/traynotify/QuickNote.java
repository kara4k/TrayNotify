package com.kara4k.traynotify;


import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.NotificationCompat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.util.Calendar;

public class QuickNote extends AppCompatActivity {

    private EditText title;
    private EditText text;
    private ToggleButton ongoing;
    private Button create;
    private SeekBar seekbar;
    private Button delete;
    private NotificationManager nm;
    private TextView textId;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.quick_note);

        getSupportActionBar().setDefaultDisplayHomeAsUpEnabled(true);

        title = (EditText) findViewById(R.id.editTitle);
        text = (EditText) findViewById(R.id.textedit);
        ongoing = (ToggleButton) findViewById(R.id.toggle_ongoing);
        create = (Button) findViewById(R.id.create);
        seekbar = (SeekBar) findViewById(R.id.seekBar);
        delete = (Button) findViewById(R.id.delete);
        textId = (TextView) findViewById(R.id.text_id);

        nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        if (getIntent().getExtras() != null) {
            title.setText(getIntent().getStringExtra(Intent.EXTRA_SUBJECT));
            text.setText(getIntent().getStringExtra(Intent.EXTRA_TEXT));
            ongoing.setChecked(getIntent().getBooleanExtra("ongoing", true));
            seekbar.setProgress(getIntent().getIntExtra("id", 0));
            textId.setText("#" + getIntent().getIntExtra("id", 0));
        }
        create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                test();
            }
        });
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                delete();
            }
        });
        seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                textId.setText("#" + i);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

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
            case R.id.action_clear_all:
                nm.cancelAll();
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
        ongoing.setChecked(true);
        seekbar.setProgress(0);
        textId.setText("#0");
    }

    private void delete() {
        nm.cancel(seekbar.getProgress());
    }

    public void test() {

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getApplicationContext());
        if (title.getText().toString().equals("")) {
            mBuilder.setContentTitle(title.getHint());
        } else {
            mBuilder.setContentTitle(title.getText().toString());
        }
        mBuilder.setContentText(text.getText().toString());
        mBuilder.setContentInfo("#" + seekbar.getProgress());
        mBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText(text.getText().toString()));

        if (ongoing.isChecked()) {
            mBuilder.setOngoing(true);
        }
//        mBuilder.setDefaults(Notification.DEFAULT_ALL);
        mBuilder.setContentIntent(PendingIntent.getActivities(getApplicationContext(), seekbar.getProgress(), makeIntent(), PendingIntent.FLAG_UPDATE_CURRENT));
        mBuilder.setSmallIcon(R.drawable.notify);
//        mBuilder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.test));
        nm.notify(seekbar.getProgress(), mBuilder.build());
        writeToDB();
    }

    private Intent[] makeIntent() {
        Intent main = new Intent(getApplicationContext(), MainActivity.class);
        main.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        Intent quick = new Intent(getApplicationContext(), QuickNote.class);

        quick.putExtra(Intent.EXTRA_SUBJECT, title.getText().toString());
        quick.putExtra(Intent.EXTRA_TEXT, text.getText().toString());
        quick.putExtra("ongoing", ongoing.isChecked());
        quick.putExtra("id", seekbar.getProgress());
        return new Intent[]{main, quick};
    }

    private void writeToDB() {
        DBQuick dbQuick = new DBQuick(getApplicationContext());
        dbQuick.open();
        Calendar calendar = Calendar.getInstance();
        if (title.getText().toString().equals("")) {
            dbQuick.addNote("TrayNotify", text.getText().toString(), calendar.getTimeInMillis(), seekbar.getProgress());
        } else {
            dbQuick.addNote(title.getText().toString(), text.getText().toString(), calendar.getTimeInMillis(), seekbar.getProgress());
        }
        dbQuick.close();
    }
}
