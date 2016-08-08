package com.kara4k.traynotify;


import android.Manifest;
import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TimePicker;
import android.widget.Toast;

import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class CreateDelayedNote extends AppCompatActivity implements DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {

    private Intent intent;
    private MyView sound;
    private Uri soundUri;
    private Calendar mainCal;
    private SimpleDateFormat sDateFormat;
    private SimpleDateFormat sTimeFormat;
    private MyView setDate;
    private MyView setTime;
    private long[] vibration;
    private NotificationManager nm;
    private EditText textEdit;
    private EditText titleEdit;
    private AlarmManager alarmManager;
    private Intent alarmIntent;
    private PendingIntent pendingIntent;
    private LinearLayout daysLayout;
    private boolean[] days;
    private DBDelay db;
    private int checkThis;
    private DateFormatSymbols formatSymbols;
    private MyView repeat2;
    private String[] shortDays;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_notification);

        textEdit = (EditText) findViewById(R.id.textEdit);
        titleEdit = (EditText) findViewById(R.id.editTitle);

        db = new DBDelay(getApplicationContext());
        checkThis = db.getNoteCheckID();

        mainCal = Calendar.getInstance();
        sDateFormat = new SimpleDateFormat("dd.MM.yyyy");
        sTimeFormat = new SimpleDateFormat("HH:mm");

        if (getIntent().getExtras() != null) {
            checkThis = getIntent().getIntExtra("check", 0);
            Log.e("123", String.valueOf(checkThis));
        }

        setDate = (MyView) findViewById(R.id.setDate);
        setDate.getText().setText(sDateFormat.format(new Date(mainCal.getTimeInMillis())));
        setTime = (MyView) findViewById(R.id.setTime);
        setTime.getText().setText(sTimeFormat.format(new Date(mainCal.getTimeInMillis())));
        setDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pickDate(view);
            }
        });
        setTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pickTime();
            }
        });

        days = new boolean[]{true, true, true, true, true, true, true,};


        formatSymbols = DateFormatSymbols.getInstance();
        String[] shortWeekdays = formatSymbols.getShortWeekdays();
        shortDays = new String[] {shortWeekdays[2].substring(0,1).toUpperCase().concat(shortWeekdays[2].substring(1)).concat(", "),
                shortWeekdays[3].substring(0,1).toUpperCase().concat(shortWeekdays[3].substring(1)).concat(", "),
                shortWeekdays[4].substring(0,1).toUpperCase().concat(shortWeekdays[4].substring(1)).concat(", "),
                shortWeekdays[5].substring(0,1).toUpperCase().concat(shortWeekdays[5].substring(1)).concat(", "),
                shortWeekdays[6].substring(0,1).toUpperCase().concat(shortWeekdays[6].substring(1)).concat(", "),
                shortWeekdays[7].substring(0,1).toUpperCase().concat(shortWeekdays[7].substring(1)).concat(", "),
                shortWeekdays[1].substring(0,1).toUpperCase().concat(shortWeekdays[1].substring(1)).concat(", "),
                };


        nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);


        final Button repeat = (Button) findViewById(R.id.repeat);
        daysLayout = (LinearLayout) findViewById(R.id.days);
        repeat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (daysLayout.getVisibility() == view.GONE) {
                    daysLayout.setVisibility(view.VISIBLE);
                    repeat.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_check_white_24dp, 0);
                } else {
                    daysLayout.setVisibility(View.GONE);
                    repeat.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                }
            }
        });

        repeat2 = (MyView) findViewById(R.id.repeat2);
        repeat2.setSecondOnClickListener(new MyView.SecondOnClickListener() {
            @Override
            public void onClick() {
                testo();
            }
        });

        setRepeatDaysText();


        final MyView vibrate = (MyView) findViewById(R.id.vibrate);
        vibrate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                VibroDialogFragment vibroDialog = new VibroDialogFragment();
                if (vibration != null) {
                    Bundle bundle = new Bundle();
                    bundle.putInt("vibrate", (int) vibration[1] / 100);
                    bundle.putInt("pause", (int) vibration[2] / 100);
                    bundle.putInt("repeat", (vibration.length - 1) / 2);
                    vibroDialog.setArguments(bundle);
                }
                vibroDialog.show(getFragmentManager(), "Set pattern");
                vibroDialog.setmDialogInterface(new VibroDialogFragment.MDialogInterface() {
                    @Override
                    public void getResult(long[] vibroPattern, String v, String p, String r) {
                        vibration = vibroPattern;
                        vibrate.getText().setText(v + " x " + p + " x " + r);
                    }

                    @Override
                    public void clearVibro() {
                        vibration = null;
                        vibrate.getText().setText("Default");
                    }
                });

            }
        });

        Button test = (Button) findViewById(R.id.test);
        test.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                makeTest();
            }
        });


        sound = (MyView) findViewById(R.id.sound);
        sound.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                checkSDPermission();
            }
        });

        Button create = (Button) findViewById(R.id.create);
        create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createNotification();
            }
        });
    }

    private void setRepeatDaysText() {
        String selectedDays = "";
        for (int i = 0; i < days.length; i++) {
            if (days[i]) {
                selectedDays+=shortDays[i];
            }
        }
        if (!selectedDays.equals("")) {
           selectedDays = selectedDays.substring(0, selectedDays.length()-2);
        }
        repeat2.getText().setText(selectedDays);
    }

    private void testo() {
        String[] weekdays = formatSymbols.getWeekdays();
        String[] dialogItems = new String[]{weekdays[2].substring(0,1).toUpperCase().concat(weekdays[2].substring(1)),
                weekdays[3].substring(0,1).toUpperCase().concat(weekdays[3].substring(1)),
                weekdays[4].substring(0,1).toUpperCase().concat(weekdays[4].substring(1)),
                weekdays[5].substring(0,1).toUpperCase().concat(weekdays[5].substring(1)),
                weekdays[6].substring(0,1).toUpperCase().concat(weekdays[6].substring(1)),
                weekdays[7].substring(0,1).toUpperCase().concat(weekdays[7].substring(1)),
                weekdays[1].substring(0,1).toUpperCase().concat(weekdays[1].substring(1))
        };
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Days")
                .setMultiChoiceItems(dialogItems, days, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int indexSelected, boolean isChecked) {
                            days[indexSelected] = isChecked;

                    }
                }).setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        setRepeatDaysText();
                        repeat2.getCheckbox().setChecked(true);
                    }
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                    }
                }).create();
        dialog.show();
    }

    private String getNoteText() {
        return textEdit.getText().toString();
    }

    private String getNoteTitle() {
        String title;
        if (titleEdit.getText().toString().equals("")) {
            title = getString(R.string.app_name);
        } else {
            title = titleEdit.getText().toString();
        }
        return title;
    }

    private long getNoteCreateTime() {
        return Calendar.getInstance().getTimeInMillis();
    }

    private long getNoteSetTime() {
        return mainCal.getTimeInMillis();
    }

    private int getNoteRepeat() {
        int repeat = daysLayout.getVisibility() == View.VISIBLE ? 0 : 1;
        return repeat;
    }

    private String getNoteDays() {
        String stringDays = "";
        if ((repeat2.getCheckbox().isChecked()) && (!repeat2.getText().getText().equals(""))) {
            for (int i = 0; i < days.length; i++) {
                if (days[i]) {
                    stringDays += "1;";
                } else {
                    stringDays += "0;";
                }
            }
        } else {
            for (int i = 0; i < days.length; i++) {
                stringDays += "0;";
            }
        }
        return stringDays;
    }

    private String getNoteSound() {
        String sound;
        if (soundUri != null) {
            sound = soundUri.toString();
        } else {
            sound = "0";
        }
        return sound;
    }

    private String getNoteVibration() {
        String vibro;
        if (vibration != null) {
            vibro = String.valueOf(vibration[1]).concat(";")
                    + String.valueOf(vibration[2]).concat(";")
                    + String.valueOf((vibration.length - 1) / 2);
        } else {
            vibro = "1";
        }
        return vibro;
    }

    private int getNotePriority() {
        return 0;
    }

    private int getNoteCheckId() {
        return checkThis;
    }

    private void createNotification() {

        DelayedNote note = new DelayedNote();
        note.setText(getNoteText());
        note.setTitle(getNoteTitle());
        note.setCreateTime(getNoteCreateTime());
        note.setSetTime(getNoteSetTime());
        note.setRepeat(getNoteRepeat());
        note.setDays(getNoteDays());
        note.setSound(getNoteSound());
        note.setVibration(getNoteVibration());
        note.setPriority(getNotePriority());
        note.setCheckId(getNoteCheckId());


        db.addNote(note);


        Log.e("check", String.valueOf(note.getCheckId()));


        alarmIntent = new Intent(getApplicationContext(), AlarmReceiver.class);
        Bundle bundle = new Bundle();
        bundle.putInt("check", note.getCheckId());
        alarmIntent.putExtras(bundle);


        pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Calendar cl = Calendar.getInstance();
        cl.add(Calendar.SECOND, 3);
//        mainCal.add(Calendar.SECOND, 10);

        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, cl.getTimeInMillis(), 3 * 60 * 1000, pendingIntent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.notification_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.share:
                Log.e("Shit", "can happen");
                break;
            case R.id.clear_forms:
                Log.e("tag", String.valueOf(mainCal.get(Calendar.DAY_OF_WEEK)));
                break;
            case R.id.action_clear_notification:
                alarmIntent = new Intent(getApplicationContext(), AlarmReceiver.class);
                PendingIntent pi = PendingIntent.getBroadcast(getApplicationContext(), 0, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                alarmManager.cancel(pi);
                nm.cancel(checkThis);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void clearForms() {
        // TODO: 03.08.2016 clear
    }

    private void makeTest() {

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getApplicationContext());
        if (titleEdit.getText().toString().equals("")) {
            mBuilder.setContentTitle(titleEdit.getHint());
        } else {
            mBuilder.setContentTitle(titleEdit.getText().toString());
        }
        mBuilder.setContentText(textEdit.getText().toString());
        mBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText(textEdit.getText().toString()));
        mBuilder.setSmallIcon(R.drawable.notify);
        if (soundUri == null && vibration == null) {
            mBuilder.setDefaults(Notification.DEFAULT_ALL);
        } else if (soundUri != null && vibration == null) {
            mBuilder.setSound(soundUri);
            mBuilder.setDefaults(Notification.DEFAULT_VIBRATE | Notification.DEFAULT_LIGHTS);
        } else if (vibration != null && soundUri == null) {
            mBuilder.setVibrate(vibration);
            mBuilder.setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_LIGHTS);
        } else if (soundUri != null && vibration != null) {
            mBuilder.setSound(soundUri);
            mBuilder.setVibrate(vibration);
            mBuilder.setDefaults(Notification.DEFAULT_LIGHTS);
        }

        mBuilder.setSmallIcon(R.drawable.notify);
        nm.notify(0, mBuilder.build());
    }

    private void chooseSoundIntent() {
        intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALL);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "Select Tone");
        if (soundUri != null) {
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, soundUri);
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_DEFAULT_URI, soundUri);
        }

        startActivityForResult(intent, 5);
    }

    private void pickTime() {
        new TimePickerDialog(this, R.style.PickerStyle, this,
                mainCal.get(Calendar.HOUR_OF_DAY),
                mainCal.get(Calendar.MINUTE), true)
                .show();
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == -1 && requestCode == 5) {
            try {
                soundUri = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
                if (soundUri != null) {
                    Cursor mCursor = getContentResolver().query(soundUri, null, null, null, null);
                    if (mCursor.moveToFirst()) {
                        sound.getText().setText(mCursor.getString(8));
                        mCursor.close();
                    }

                }
            } catch (Exception e) {
                e.printStackTrace(); // TODO: 03.08.2016 toast smth wrong
            }
        }
    }

    public void pickDate(View v) {
        new DatePickerDialog(this, R.style.PickerStyle, this,
                mainCal.get(Calendar.YEAR),
                mainCal.get(Calendar.MONTH),
                mainCal.get(Calendar.DAY_OF_MONTH))
                .show();
    }

    @Override
    public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
        mainCal.set(i, i1, i2);
        setDate.getText().setText(sDateFormat.format(new Date(mainCal.getTimeInMillis())));
    }

    @Override
    public void onTimeSet(TimePicker timePicker, int i, int i1) {
        mainCal.set(Calendar.HOUR_OF_DAY, i);
        mainCal.set(Calendar.MINUTE, i1);
        mainCal.set(Calendar.SECOND, 0);
        setTime.getText().setText(sTimeFormat.format(new Date(mainCal.getTimeInMillis())));
    }

    private void checkSDPermission() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            int hasWriteSDPermission = checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE);
            if (hasWriteSDPermission == PackageManager.PERMISSION_DENIED) {
                Log.e("enter", "here");
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                return;
            } else {
                chooseSoundIntent();
            }
        } else {
            chooseSoundIntent();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                    Toast.makeText(this, getString(R.string.sd_permission_denied), Toast.LENGTH_SHORT).show();
                    chooseSoundIntent();
                } else {
                    chooseSoundIntent();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }


}

