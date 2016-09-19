package com.kara4k.traynotify;


import android.Manifest;
import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.NotificationCompat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RemoteViews;
import android.widget.TimePicker;
import android.widget.Toast;

import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class CreateDelayedNote extends AppCompatActivity implements DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener, DialogInterface.OnClickListener {

    private MyView sound;
    private Uri soundUri;
    private Calendar mainCal;
    private SimpleDateFormat sDateFormat;
    private SimpleDateFormat sTimeFormat;
    private MyView setDate;
    private MyView setTime;
    private long[] vibration;
    //    private NotificationManagerCompat nm;
    private EditText textEdit;
    private EditText titleEdit;
    private AlarmManager alarmManager;
    private boolean[] days;
    private DBDelay db;
    private int checkThis;
    private DateFormatSymbols formatSymbols;
    private MyView repeatWeek;
    private MyView repeatMonth;
    private MyView repeatYear;
    private String[] shortDays;
    private DelayedNote note;
    private MyView vibrate;
    private int birthday = 0;
    private SharedPreferences sp;
    private MyView priority;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_notification);

        note = new DelayedNote();

        textEdit = (EditText) findViewById(R.id.textEdit);
        titleEdit = (EditText) findViewById(R.id.editTitle);

        sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
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


//        nm = NotificationManagerCompat.from(this);
        alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);


        repeatWeek = (MyView) findViewById(R.id.repeat_week);
        repeatMonth = (MyView) findViewById(R.id.repeat_month);
        repeatYear = (MyView) findViewById(R.id.repeat_year);

        repeatWeek.setmCheckToggle(false);
        repeatWeek.setSecondOnClickListener(new MyView.SecondOnClickListener() {
            @Override
            public void onClick() {
                chooseDays();
            }
        });

        setRepeatWeekCheckListener();

        setRepeatMonthCheckListener();

        setRepeatYearCheckListener();


        setRepeatDaysText();


        vibrate = (MyView) findViewById(R.id.vibrate);
        setDefaultVibroPattern();
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


        sound = (MyView) findViewById(R.id.sound);
        setDefaultSoundText();
        setDefaultSoundUri();
        this.sound.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                showSoundDialog();
//                checkSDPermission();
            }
        });

        priority = (MyView) findViewById(R.id.max_priority);
        checkIfShowPriorityView();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createNotification();
            }
        });


        onIntentReceive(tempId, vibrate);
    }

    private void checkIfShowPriorityView() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            priority.setVisibility(View.GONE);
        } else {
            priority.setVisibility(View.VISIBLE);
        }
    }

    private void showSoundDialog() {
        new android.app.AlertDialog.Builder(CreateDelayedNote.this).setTitle(sound.getText().getText())
                .setPositiveButton(R.string.choose, this)
                .setNegativeButton(getString(R.string.cancel), this)
                .setNeutralButton(getString(R.string.text_default), this)
                .create().show();
    }

    private void setDefaultVibroPattern() {
        String vPattern = sp.getString(Settings.VIBRATION, "1");
        if (!vPattern.equals("1")) {
            vibration = parseVibrationFromString(vPattern);
            vibrate.getText().setText(parseVibroTitle(vibration));
        } else {
            vibration = null;
            vibrate.getText().setText(getString(R.string.text_default));
        }
    }

    private void setDefaultSoundUri() {
        String sound = sp.getString(Settings.SOUND, "0");
        if (!sound.equals("0")) {
            soundUri = Uri.parse(sound);
        } else {
            soundUri = null;
        }
    }

    private void setDefaultSoundText() {
        String trackName = sp.getString(Settings.TRACK_NAME, "0");
        if (trackName.equals("0")) {
            trackName = getString(R.string.text_default);
        }
        sound.getText().setText(trackName);
    }

    private void setRepeatYearCheckListener() {
        repeatYear.getCheckbox().setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    repeatYearChecked();
                } else {
                    repeatYearUnchecked();
                }
            }
        });
    }

    private void repeatYearUnchecked() {
        repeatWeek.getCheckbox().setEnabled(true);
        repeatMonth.getCheckbox().setEnabled(true);
    }

    private void repeatYearChecked() {
        repeatWeek.getCheckbox().setEnabled(false);
        repeatWeek.getCheckbox().setChecked(false);
        repeatMonth.getCheckbox().setEnabled(false);
        repeatMonth.getCheckbox().setChecked(false);
    }

    private void setRepeatMonthCheckListener() {
        repeatMonth.getCheckbox().setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    repeatMonthChecked();
                } else {
                    repeatMonthUnchecked();
                }
            }
        });
    }

    private void repeatMonthUnchecked() {
        repeatWeek.getCheckbox().setEnabled(true);
        repeatYear.getCheckbox().setEnabled(true);
    }

    private void repeatMonthChecked() {
        repeatWeek.getCheckbox().setEnabled(false);
        repeatWeek.getCheckbox().setChecked(false);
        repeatYear.getCheckbox().setEnabled(false);
        repeatYear.getCheckbox().setChecked(false);
    }

    private void setRepeatWeekCheckListener() {
        repeatWeek.getCheckbox().setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    repeatWeekChecked();
                } else {
                    repeatWeekUnchecked();
                }
            }
        });
    }

    private void repeatWeekUnchecked() {
        repeatMonth.getCheckbox().setEnabled(true);
        repeatYear.getCheckbox().setEnabled(true);
    }

    private void repeatWeekChecked() {
        repeatMonth.getCheckbox().setEnabled(false);
        repeatMonth.getCheckbox().setChecked(false);
        repeatYear.getCheckbox().setEnabled(false);
        repeatYear.getCheckbox().setChecked(false);
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
                    repeatYearChecked();
                    repeatYear.getCheckbox().setChecked(true);

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
        } else {
            setDefaultSound();
        }

        if (!note.getVibration().equals("1")) {
            parseVibration(vibrate);
        } else {
            vibration = null;
            vibrate.setText(getString(R.string.text_default));
        }

        if (note.getPriority() != 0) {
            priority.getCheckbox().setChecked(true);
        } else {
            priority.getCheckbox().setChecked(false);
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
            this.repeatWeek.getCheckbox().setChecked(true);
            String[] split = note.getDays().split(";");
            for (int i = 0; i < split.length; i++) {
                if (split[i].equals("0")) {
                    days[i] = false;
                } else {
                    days[i] = true;
                }
            }
            setRepeatDaysText();
        }
        if (note.getRepeat() == 2) {
            repeatMonthChecked();
            repeatMonth.getCheckbox().setChecked(true);
        }
        if (note.getRepeat() == 3) {
            repeatYearChecked();
            repeatYear.getCheckbox().setChecked(true);
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

        vibration = parseVibrationFromString(note.getVibration());
        vibrate.setText(parseVibroTitle(vibration));
    }

    public static long[] parseVibrationFromString(String pattern) {
        String[] strings = pattern.split(";");
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

        return vibration;

    }


    public static String parseVibroTitle(long[] vibration) {
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

        return vText.concat(" x ").concat(pText).concat(" x ").concat(rText);
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
        repeatWeek.getText().setText(selectedDays);
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
        if ((repeatWeek.getCheckbox().isChecked()) && (!repeatWeek.getText().getText().equals(""))) {
            return 1;
        } else if (repeatMonth.getCheckbox().isChecked()) {
            return 2;
        } else if (repeatYear.getCheckbox().isChecked()) {
            return 3;
        } else return 0;
    }

    private String getNoteDays() {
        String stringDays = "";
        if ((repeatWeek.getCheckbox().isChecked()) && (!repeatWeek.getText().getText().equals(""))) {
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

    public static String getNoteVibration(long[] vibration) {
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
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            return 0;
        } else {
            return checkIfImportant();
        }

    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private int checkIfImportant() {
        if (priority.getCheckbox().isChecked()) {
            return Notification.PRIORITY_HIGH;
        } else {
            return Notification.PRIORITY_DEFAULT;
        }
    }

    private int getNoteCheckId() {
        return checkThis;
    }

    private void createNotification() {
        fillNoteFromViews();
        writeNoteToDb();
        setMainCallZeroSecs();
        setAlarm(getPendingIntent());
        finish();
    }

    private void setAlarm(PendingIntent pendingIntent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            setAfterKITKATAlarm(pendingIntent);
        } else {
            setBeforeKITKATAlarm(pendingIntent);
        }
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void setAfterKITKATAlarm(PendingIntent pendingIntent) {
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, mainCal.getTimeInMillis(), pendingIntent);
    }

    private void setBeforeKITKATAlarm(PendingIntent pendingIntent) {
        int repeat = note.getRepeat();
        if (repeat != 1) {
            alarmManager.set(AlarmManager.RTC_WAKEUP, mainCal.getTimeInMillis(), pendingIntent);
        } else {
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, mainCal.getTimeInMillis(), 24 * 60 * 60 * 1000, pendingIntent);
        }
    }

    private PendingIntent getPendingIntent() {
        return PendingIntent.getBroadcast(getApplicationContext(), checkThis, getAlarmIntent(), PendingIntent.FLAG_UPDATE_CURRENT);
    }

    @NonNull
    private Intent getAlarmIntent() {
        Intent alarmIntent = new Intent(getApplicationContext(), AlarmReceiver.class);
        Bundle bundle = new Bundle();
        bundle.putInt("id", note.getCheckId());
        alarmIntent.putExtras(bundle);
        return alarmIntent;
    }

    private void setMainCallZeroSecs() {
        mainCal.set(Calendar.SECOND, 00);
        mainCal.set(Calendar.MILLISECOND, 0000);
    }

    private void fillNoteFromViews() {
        note.setText(getNoteText());
        note.setTitle(getNoteTitle());
        note.setCreateTime(getNoteCreateTime());
        note.setSetTime(getNoteSetTime());
        note.setRepeat(getNoteRepeat());
        note.setDays(getNoteDays());
        note.setSound(getNoteSound());
        note.setVibration(getNoteVibration(vibration));
        note.setPriority(getNotePriority());
        note.setCheckId(getNoteCheckId());
        note.setBirthday(getNoteBirthday());
    }

    private void writeNoteToDb() {
        db.open();
        Cursor alarmNote = db.getAlarmNote(checkThis);
        if (alarmNote.moveToFirst()) {
            db.editNote(note, checkThis);
        } else {
            db.addNote(note);
        }
    }

    private int getNoteBirthday() {
        return birthday;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.quick_menu, menu);
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
            case R.id.copy:
                makeTest();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void makeTest() {

        NotificationManagerCompat nm = NotificationManagerCompat.from(getApplicationContext());
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getApplicationContext());
//        if (titleEdit.getText().toString().equals("")) {
//            mBuilder.setContentTitle(getString(R.string.app_name));
//        } else {
//            mBuilder.setContentTitle(titleEdit.getText().toString());
//        }
//        mBuilder.setContentText(textEdit.getText().toString());
//        mBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText(textEdit.getText().toString()));
//        mBuilder.setSmallIcon(R.drawable.notify);
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
//        Intent intent = new Intent(getApplicationContext(), QuickNote.class);
//        PendingIntent p = PendingIntent.getActivity(getApplicationContext(), note.getCheckId(), getPackageManager().getLaunchIntentForPackage(getPackageName())
//                ,PendingIntent.FLAG_UPDATE_CURRENT);
//        mBuilder.setFullScreenIntent(p,true);

        if (titleEdit.getText().toString().toLowerCase().contains("ongoing")) {
            mBuilder.setOngoing(true);
        }
        if (titleEdit.getText().toString().toLowerCase().contains("push")) {

            mBuilder.setPriority(Notification.PRIORITY_HIGH);
        }


//        NotificationCompat.BigPictureStyle notiStyle = new
//                NotificationCompat.BigPictureStyle();
//        notiStyle.setBigContentTitle("Big Picture Expanded");
//        notiStyle.setSummaryText("Nice big picture.");


        Intent intent = new Intent(getApplicationContext(), QuickNote.class);
        PendingIntent p = PendingIntent.getActivity(getApplicationContext(), note.getCheckId(), getPackageManager().getLaunchIntentForPackage(getPackageName())
                , PendingIntent.FLAG_UPDATE_CURRENT);

        RemoteViews smallView = new RemoteViews(getPackageName(), R.layout.notification);
//        Drawable drawable = getApplicationInfo().loadIcon(getPackageManager());
//        Bitmap bitmap = ((BitmapDrawable)drawable).getBitmap();

//        remoteView.setImageViewBitmap(R.id.n_app_icon,bitmap);
//        smallView.setTextViewText(R.id.n_title, titleEdit.getText().toString());
        smallView.setTextViewText(R.id.n_text, textEdit.getText().toString());
        smallView.setViewVisibility(R.id.n_main_icon, View.VISIBLE);
//        remoteView.setTextViewText(R.id.n_time, "#1");

        RemoteViews bigView = new RemoteViews(getPackageName(), R.layout.notification_big);
//        bigView.setInt(R.id.n_big_layout, "setBackgroundColor", Color.BLACK);
//        bigView.setInt(R.id.n_big_text, "setTextColor", Color.WHITE);


        bigView.setTextViewText(R.id.n_big_title, titleEdit.getText().toString());
        bigView.setTextViewText(R.id.n_big_text, textEdit.getText().toString());

        Intent quickIntent = new Intent(getApplicationContext(), QuickNote.class);
        PendingIntent actionPI = PendingIntent.getActivity(getApplicationContext(), note.getCheckId(), quickIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        bigView.setOnClickPendingIntent(R.id.n_big_actions,actionPI);
        bigView.setViewVisibility(R.id.n_big_main_icon, View.GONE);

//        bigView.setViewVisibility(R.id.n_big_notetype, View.GONE);
//        bigView.setViewVisibility(R.id.n_big_numid, View.GONE);



        mBuilder.setContent(smallView);
        mBuilder.setCustomBigContentView(bigView);

        RemoteViews pushView = new RemoteViews(getPackageName(), R.layout.notification_big);
        pushView.setTextViewText(R.id.n_big_title, titleEdit.getText().toString());
        pushView.setTextViewText(R.id.n_big_text, textEdit.getText().toString());
        pushView.setViewVisibility(R.id.n_big_actions, View.GONE);
        pushView.setViewVisibility(R.id.n_big_main_icon, View.VISIBLE);


        mBuilder.setCustomHeadsUpContentView(pushView);

        mBuilder.setContentIntent(p);

        nm.notify(0, mBuilder.build());
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
        mainCal = Calendar.getInstance();
        setDate.setText(sDateFormat.format(mainCal.getTimeInMillis()));
        setTime.setText(sTimeFormat.format(mainCal.getTimeInMillis()));
        days = new boolean[]{true, true, true, true, true, true, true};
        setRepeatDaysText();
        repeatWeek.getCheckbox().setChecked(false);
        repeatMonth.getCheckbox().setChecked(false);
        repeatYear.getCheckbox().setChecked(false);
        setDefaultSoundText();
        setDefaultSoundUri();
        setDefaultVibroPattern();
        priority.getCheckbox().setChecked(false);
        birthday = 0;

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
    public void onClick(DialogInterface dialogInterface, int i) {
        switch (i) {
            case DialogInterface.BUTTON_POSITIVE:
                checkSDPermission();
                break;
            case DialogInterface.BUTTON_NEGATIVE:
                break;
            case DialogInterface.BUTTON_NEUTRAL:
                setDefaultSound();
                break;
        }
    }

    private void setDefaultSound() {
        soundUri = null;
        sound.setText(getString(R.string.text_default));
    }
}

