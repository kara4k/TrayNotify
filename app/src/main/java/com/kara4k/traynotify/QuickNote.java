package com.kara4k.traynotify;


import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.NotificationCompat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.Calendar;

public class QuickNote extends AppCompatActivity {

    private boolean isCreateTray = true;
    private boolean isOngoing = true;
    private boolean isNotify = true;

    private EditText title;
    private EditText text;
    private Button create;
    private SeekBar seekbar;
    private Button delete;
    private NotificationManager nm;
    private TextView textId;
    private ImageButton createTrayButton;
    private ImageButton ongoingButton;
    private ImageButton notifyButton;
    private LinearLayout advancedLayout;
    private LinearLayout seekLayout;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.quick_note);

        getSupportActionBar().setDefaultDisplayHomeAsUpEnabled(true);

        title = (EditText) findViewById(R.id.editTitle);
        text = (EditText) findViewById(R.id.textedit);
        createTrayButton = (ImageButton) findViewById(R.id.create_tray);
        ongoingButton = (ImageButton) findViewById(R.id.ongoing);
        notifyButton = (ImageButton) findViewById(R.id.notify);
        advancedLayout = (LinearLayout) findViewById(R.id.advanced_layout);
        seekLayout = (LinearLayout) findViewById(R.id.seek_layout);
        create = (Button) findViewById(R.id.create);
        seekbar = (SeekBar) findViewById(R.id.seekBar);
        delete = (Button) findViewById(R.id.delete);
        textId = (TextView) findViewById(R.id.text_id);
        final Button advancedButton = (Button) findViewById(R.id.advanced);


        nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);


        intentChecks();


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
        createTrayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isCreateTray) {
                    createTrayButtonDisabled();
                    isCreateTray = false;
                    ongoingButtonDisabled();
                    notifyButtonDisabled();
                    isOngoing = false;
                    isNotify = false;
                } else {
                    createTrayButtonEnabled();
                    isCreateTray = true;

                }
            }
        });

        ongoingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isCreateTray) {
                    if (isOngoing) {
                        ongoingButtonDisabled();
                        isOngoing = false;
                    } else {
                        ongoingButtonEnabled();
                        isOngoing = true;
                    }
                }
            }
        });

        notifyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isCreateTray) {
                    if (isNotify) {
                        notifyButtonDisabled();
                        isNotify = false;
                    } else {
                        notifyButtonEnabled();
                        isNotify = true;
                    }
                }
            }
        });

        advancedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (advancedLayout.getVisibility() == view.GONE) {
                    advancedLayout.setVisibility(view.VISIBLE);
                    seekLayout.setVisibility(View.VISIBLE);
                    advancedButton.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_drop_up_white_24dp, 0);
                } else {
                    advancedLayout.setVisibility(View.GONE);
                    seekLayout.setVisibility(View.GONE);
                    advancedButton.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_drop_down_white_24dp, 0);
                }
            }
        });

    }

    private void intentChecks() {
        if (getIntent().getExtras() != null) {
            title.setText(getIntent().getStringExtra(Intent.EXTRA_SUBJECT));
            text.setText(getIntent().getStringExtra(Intent.EXTRA_TEXT));
            isOngoing = (getIntent().getBooleanExtra("ongoing", true));
            if (!isOngoing) {
                ongoingButtonDisabled();
            }
            seekbar.setProgress(getIntent().getIntExtra("id", 0));
            textId.setText("#" + getIntent().getIntExtra("id", 0));
        }
    }

    private void notifyButtonEnabled() {
        notifyButton.setBackgroundColor(Color.parseColor("#676d83"));
        notifyButton.setImageResource(R.drawable.ic_notifications_active_white_24dp);
    }

    private void notifyButtonDisabled() {
        notifyButton.setBackgroundColor(Color.WHITE);
        notifyButton.setImageResource(R.drawable.ic_notifications_off_black_24dp);
    }

    private void createTrayButtonDisabled() {
        createTrayButton.setBackgroundColor(Color.WHITE);
        createTrayButton.setImageResource(R.drawable.ic_speaker_notes_off_black_24dp);
    }

    private void createTrayButtonEnabled() {
        createTrayButton.setBackgroundColor(Color.parseColor("#676d83"));
        createTrayButton.setImageResource(R.drawable.ic_speaker_notes_white_24dp);
    }

    private void ongoingButtonEnabled() {
        ongoingButton.setBackgroundColor(Color.parseColor("#676d83"));
        ongoingButton.setImageResource(R.drawable.ic_delete_forever_white_24dp);
    }

    private void ongoingButtonDisabled() {
        ongoingButton.setBackgroundColor(Color.WHITE);
        ongoingButton.setImageResource(R.drawable.ic_delete_sweep_black_24dp);
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
        seekbar.setProgress(0);
        textId.setText("#0");
        isCreateTray = true;
        isOngoing = true;
        isNotify = true;
        createTrayButtonEnabled();
        ongoingButtonEnabled();
        notifyButtonEnabled();
    }

    private void delete() {
        nm.cancel(seekbar.getProgress());
    }

    public void test() {

        if (isCreateTray) {
            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getApplicationContext());
            if (title.getText().toString().equals("")) {
                mBuilder.setContentTitle(title.getHint());
            } else {
                mBuilder.setContentTitle(title.getText().toString());
            }
            mBuilder.setContentText(text.getText().toString());
            mBuilder.setContentInfo("#" + seekbar.getProgress());
            mBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText(text.getText().toString()));
            mBuilder.setOngoing(isOngoing);
            if (isNotify) {
                mBuilder.setDefaults(Notification.DEFAULT_ALL);
            }
            mBuilder.setContentIntent(PendingIntent.getActivities(getApplicationContext(), seekbar.getProgress(), makeIntent(), PendingIntent.FLAG_UPDATE_CURRENT));
            mBuilder.setSmallIcon(R.drawable.notify);
            nm.notify(seekbar.getProgress(), mBuilder.build());
        }
        writeToDB();
    }

    private Intent[] makeIntent() {
        Intent main = new Intent(getApplicationContext(), MainActivity.class);
        main.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        Intent quick = new Intent(getApplicationContext(), QuickNote.class);

        quick.putExtra(Intent.EXTRA_SUBJECT, title.getText().toString());
        quick.putExtra(Intent.EXTRA_TEXT, text.getText().toString());
        quick.putExtra("ongoing", isOngoing);
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
