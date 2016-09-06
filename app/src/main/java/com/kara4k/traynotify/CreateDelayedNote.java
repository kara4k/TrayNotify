package com.kara4k.traynotify;


import android.Manifest;
import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.NotificationCompat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import java.io.IOException;
import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class CreateDelayedNote extends AppCompatActivity implements DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {

    private MyView sound;
    private Uri soundUri;
    private Calendar mainCal;
    private SimpleDateFormat sDateFormat;
    private SimpleDateFormat sTimeFormat;
    private MyView setDate;
    private MyView setTime;
    private long[] vibration;
    private NotificationManagerCompat nm;
    private EditText textEdit;
    private EditText titleEdit;
    private AlarmManager alarmManager;
    private boolean[] days;
    private DBDelay db;
    private int checkThis;
    private DateFormatSymbols formatSymbols;
    private MyView repeat;
    private String[] shortDays;
    private DelayedNote note;
    private MyView vibrate;
    private int birthday = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_notification);

        note = new DelayedNote();

        textEdit = (EditText) findViewById(R.id.textEdit);
        titleEdit = (EditText) findViewById(R.id.editTitle);

        db = new DBDelay(getApplicationContext());
        checkThis = db.getNoteCheckID();
        int tempId = checkThis;

        mainCal = Calendar.getInstance();
        sDateFormat = new SimpleDateFormat("dd.MM.yyyy");
        sTimeFormat = new SimpleDateFormat("HH:mm");


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
        shortDays = new String[]{shortWeekdays[2].substring(0, 1).toUpperCase().concat(shortWeekdays[2].substring(1)).concat(", "),
                shortWeekdays[3].substring(0, 1).toUpperCase().concat(shortWeekdays[3].substring(1)).concat(", "),
                shortWeekdays[4].substring(0, 1).toUpperCase().concat(shortWeekdays[4].substring(1)).concat(", "),
                shortWeekdays[5].substring(0, 1).toUpperCase().concat(shortWeekdays[5].substring(1)).concat(", "),
                shortWeekdays[6].substring(0, 1).toUpperCase().concat(shortWeekdays[6].substring(1)).concat(", "),
                shortWeekdays[7].substring(0, 1).toUpperCase().concat(shortWeekdays[7].substring(1)).concat(", "),
                shortWeekdays[1].substring(0, 1).toUpperCase().concat(shortWeekdays[1].substring(1)).concat(", "),
        };


        nm = NotificationManagerCompat.from(this);
        alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);


        repeat = (MyView) findViewById(R.id.repeat);
        repeat.setSecondOnClickListener(new MyView.SecondOnClickListener() {
            @Override
            public void onClick() {
                chooseDays();
            }
        });

        setRepeatDaysText();


        vibrate = (MyView) findViewById(R.id.vibrate);
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
                vibroDialog.show(getFragmentManager(), getString(R.string.pattern));
                vibroDialog.setmDialogInterface(new VibroDialogFragment.MDialogInterface() {
                    @Override
                    public void getResult(long[] vibroPattern, String v, String p, String r) {
                        vibration = vibroPattern;
                        vibrate.getText().setText(v + " x " + p + " x " + r);
                    }

                    @Override
                    public void clearVibro() {
                        vibration = null;
                        vibrate.getText().setText(getString(R.string.text_default));
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

        onIntentReceive(tempId, vibrate);
    }

    private void onIntentReceive(int tempId, MyView vibrate) {
        if (getIntent().getExtras() != null) {
            checkThis = getIntent().getIntExtra("id", 0);
            if (checkThis != 0) {
                db.open();
                Cursor alarmNote = db.getAlarmNote(checkThis);
                if (alarmNote.moveToFirst()) {
                    fillNoteFromDb(alarmNote);
                    fillFormsFromNote(vibrate);
                } else {
                    titleEdit.setText(getIntent().getStringExtra(Intent.EXTRA_SUBJECT));
                    textEdit.setText(getIntent().getStringExtra(Intent.EXTRA_TEXT));
                }
                db.close();
            } else if (checkThis == 0) {
                birthday = getIntent().getIntExtra("birthday", 0);
                if (birthday != 0) {
                    mainCal.setTimeInMillis(getIntent().getLongExtra("time", mainCal.getTimeInMillis()));
                    setDate.setText(sDateFormat.format(mainCal.getTimeInMillis()));
                    setTime.setText(sTimeFormat.format(mainCal.getTimeInMillis()));

                }
                titleEdit.setText(getIntent().getStringExtra(Intent.EXTRA_SUBJECT));
                textEdit.setText(getIntent().getStringExtra(Intent.EXTRA_TEXT));
                checkThis = tempId;
            }


        }
    }

    private void fillNoteFromDb(Cursor alarmNote) {
        note.setText(alarmNote.getString(1));
        note.setTitle(alarmNote.getString(2));
        note.setCreateTime(alarmNote.getLong(3));
        note.setSetTime(alarmNote.getLong(4));
        note.setRepeat(alarmNote.getInt(5));
        note.setDays(alarmNote.getString(6));
        note.setSound(alarmNote.getString(7));
        note.setVibration(alarmNote.getString(8));
        note.setPriority(alarmNote.getInt(9));
        note.setCheckId(alarmNote.getInt(10));
        note.setBirthday(alarmNote.getInt(11));
    }

    private void fillFormsFromNote(MyView vibrate) {
        parseTextTitleDate();
        parseRepeat();
        if (!note.getSound().equals("0")) {
            parseSound();
        }
        if (!note.getVibration().equals("1")) {
            parseVibration(vibrate);
        }
        birthday = note.getBirthday();
    }

    private void parseTextTitleDate() {
        titleEdit.setText(note.getTitle());
        textEdit.setText(note.getText());
        setDate.setText(sDateFormat.format(note.getSetTime()));
        setTime.setText(sTimeFormat.format(note.getSetTime()));
        mainCal.setTimeInMillis(note.getSetTime());
    }

    private void parseRepeat() {
        if (note.getRepeat() == 1) {
            this.repeat.getCheckbox().setChecked(true);
            String[] split = note.getDays().split(";");
            for (int i = 0; i < split.length; i++) {
                if (split[i].equals("0")) {
                    days[i] = false;
                } else {
                    days[i] = true;
                }
            }
            setRepeatDaysText();
        } else {
            this.repeat.getCheckbox().setChecked(false);
        }
    }

    private void parseSound() {
        try {
            soundUri = Uri.parse(note.getSound());
            Cursor mCursor = getContentResolver().query(soundUri, null, null, null, null);
            if (mCursor.moveToFirst()) {
                sound.getText().setText(mCursor.getString(8));
                mCursor.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void parseVibration(MyView vibrate) {
        String[] strings = note.getVibration().split(";");
        int repeatTime = Integer.parseInt(strings[2]);
        List<Long> vibratePattern = new ArrayList<Long>();
        vibratePattern.add(0, (long) 0);

        for (int k = 0; k < repeatTime; k++) {
            vibratePattern.add(Long.parseLong(strings[0]));
            vibratePattern.add(Long.parseLong(strings[1]));
        }

        long[] vibration = new long[vibratePattern.size()];
        for (int j = 0; j < vibratePattern.size(); j++) {
            vibration[j] = vibratePattern.get(j);
        }

        setVibrationTextFromString(vibrate, vibration);
    }

    private void setVibrationTextFromString(MyView vibrate, long[] vibration) {
        long v = vibration[1] / 100;
        long p = vibration[2] / 100;

        String vText = "";
        String pText = "";
        String rText = String.valueOf((vibration.length - 1) / 2);

        if (v < 10) {
            vText = (String.format("0.%d", v));
        } else {
            vText = (String.valueOf(v)
                    .substring(0, 1)
                    .concat(".")
                    .concat(String.valueOf(v).substring(1)));
        }


        if (p < 10) {
            pText = (String.format("0.%d", p));
        } else {
            pText = (String.valueOf(p)
                    .substring(0, 1)
                    .concat(".")
                    .concat(String.valueOf(p).substring(1)));
        }

        vibrate.setText(vText.concat(" x ").concat(pText).concat(" x ").concat(rText));
    }


    private void setRepeatDaysText() {
        String selectedDays = "";
        for (int i = 0; i < days.length; i++) {
            if (days[i]) {
                selectedDays += shortDays[i];
            }
        }
        if (!selectedDays.equals("")) {
            selectedDays = selectedDays.substring(0, selectedDays.length() - 2);
        }
        repeat.getText().setText(selectedDays);
    }

    private void chooseDays() {
        String[] weekdays = formatSymbols.getWeekdays();
        String[] dialogItems = new String[]{weekdays[2].substring(0, 1).toUpperCase().concat(weekdays[2].substring(1)),
                weekdays[3].substring(0, 1).toUpperCase().concat(weekdays[3].substring(1)),
                weekdays[4].substring(0, 1).toUpperCase().concat(weekdays[4].substring(1)),
                weekdays[5].substring(0, 1).toUpperCase().concat(weekdays[5].substring(1)),
                weekdays[6].substring(0, 1).toUpperCase().concat(weekdays[6].substring(1)),
                weekdays[7].substring(0, 1).toUpperCase().concat(weekdays[7].substring(1)),
                weekdays[1].substring(0, 1).toUpperCase().concat(weekdays[1].substring(1))
        };
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(R.string.days_capital)
                .setMultiChoiceItems(dialogItems, days, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int indexSelected, boolean isChecked) {
                        days[indexSelected] = isChecked;

                    }
                }).setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        setRepeatDaysText();
                        repeat.getCheckbox().setChecked(true);
                    }
                }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
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
        return this.repeat.getCheckbox().isChecked() ? 1 : 0;
    }

    private String getNoteDays() {
        String stringDays = "";
        if ((repeat.getCheckbox().isChecked()) && (!repeat.getText().getText().equals(""))) {
            for (boolean day : days) {
                if (day) {
                    stringDays += "1;";
                } else {
                    stringDays += "0;";
                }
            }
        } else {
            for (boolean day : days) {
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
        note.setBirthday(getNoteBirthday());

        db.open();
        Cursor alarmNote = db.getAlarmNote(checkThis);
        if (alarmNote.moveToFirst()) {
            db.editNote(note, checkThis);
        } else {
            db.addNote(note);
        }


        Intent alarmIntent = new Intent(getApplicationContext(), AlarmReceiver.class);
        Bundle bundle = new Bundle();
        bundle.putInt("id", note.getCheckId());


        alarmIntent.putExtras(bundle);


        mainCal.set(Calendar.SECOND, 00);
        mainCal.set(Calendar.MILLISECOND, 0000);

        SimpleDateFormat sdf = new SimpleDateFormat("EEE SSSS, dd.MM.yyyy; HH:mm:ss:SSSSS ");


        PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), checkThis, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, mainCal.getTimeInMillis(), 24 * 60 * 60 * 1000, pendingIntent);

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, mainCal.getTimeInMillis(), pendingIntent);
        } else {
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, mainCal.getTimeInMillis(), 24 * 60 * 60 * 1000, pendingIntent);
        }

        setResult(RESULT_OK);
        finish();
    }

    private int getNoteBirthday() {
        return birthday;
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
                sendIntent();
                break;
            case R.id.clear_forms:
                clearForms();
                break;
            case R.id.action_clear_notification:
                nm.cancel(checkThis);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void sendIntent() {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_SUBJECT, titleEdit.getText().toString());
        sendIntent.putExtra(Intent.EXTRA_TEXT, textEdit.getText().toString());
        sendIntent.setType("text/plain");
        startActivity(sendIntent);
    }

    private void clearForms() {
        note = new DelayedNote();
        textEdit.setText("");
        titleEdit.setText("");
        Calendar cal = Calendar.getInstance();
        setDate.setText(sDateFormat.format(cal.getTimeInMillis()));
        setTime.setText(sTimeFormat.format(cal.getTimeInMillis()));
        days = new boolean[]{true, true, true, true, true, true, true};
        setRepeatDaysText();
        repeat.getCheckbox().setChecked(false);
        soundUri = null;
        sound.setText(getString(R.string.text_default));
        vibration = null;
        vibrate.setText(getString(R.string.text_default));
        birthday = 0;

    }

    private void makeTest() {

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getApplicationContext());
        if (titleEdit.getText().toString().equals("")) {
            mBuilder.setContentTitle(getString(R.string.app_name));
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

        if (birthday != 0) {
            try {
                Bitmap mBitmap = MediaStore.Images.Media.getBitmap(getApplicationContext().getContentResolver(), Uri.parse("content://com.android.contacts/contacts/" + birthday + "/display_photo"));
                mBuilder.setLargeIcon(mBitmap);
            } catch (IOException e) {
                mBuilder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.user1));
            }
        } else {
            mBuilder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.user1));
        }

        nm.notify(0, mBuilder.build());
    }

    private void chooseSoundIntent() {
        Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALL);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, getString(R.string.melody));
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

            }
        }
    }

    private void pickDate(View v) {
        new DatePickerDialog(this, R.style.PickerStyle, this,
                mainCal.get(Calendar.YEAR),
                mainCal.get(Calendar.MONTH),
                mainCal.get(Calendar.DAY_OF_MONTH)).show();
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
            int hasWriteSDPermission = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE);
            if (hasWriteSDPermission == PackageManager.PERMISSION_DENIED) {
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
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
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




    @Override
    public void onBackPressed() {
        super.onBackPressed();
        setResult(RESULT_CANCELED);
    }
}

