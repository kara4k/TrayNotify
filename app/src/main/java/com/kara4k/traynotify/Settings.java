package com.kara4k.traynotify;


import android.Manifest;
import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.widget.TimePicker;
import android.widget.Toast;

public class Settings extends PreferenceActivity implements DialogInterface.OnClickListener, VibroDialogFragment.MDialogInterface, Preference.OnPreferenceChangeListener {

    public static final String SOUND = "sound";
    public static final String TRACK_NAME = "track_name";
    public static final String VIBRATION = "vibration";
    public static final String IMPORTANT = "important";
    public static final String TRACK_CLIPBOARD = "track_clipboard";
    public static final String DELAYED_DEFAULTS_CATEGORY = "delayed_defaults";
    public static final String DELAYED_APPEARANCE_SCREEN = "delayed_appearance";
    public static final String DELAYED_PUSH_CATEGORY = "delayed_push_category";
    public static final String SHOW_BIRTHDAYS = "show_birthdays";
    public static final String BIRTHDAY_DEFAULTS_CATEGORY = "birthday_defaults";

    public static final String VERSION_CODE = "version_code";
    public static final String QUICK_BACKGROUND = "n_q_background";
    public static final String QUICK_TEXT = "n_q_text";
    public static final String QUICK_SHOW_ACTIONS = "n_q_show_actions";
    public static final String QUICK_SHOW_ACTIONS_TEXT = "n_q_show_actions_text";
    public static final String QUICK_ACTIONS_TEXT_COLOR = "n_q_actions_text_color";
    public static final String QUICK_ACTIONS_ICON_COLOR = "n_q_actions_icon_color";

    public static final String QUICK_DEFAULT_IN_TRAY = "n_q_default_in_tray";
    public static final String REM_BACKGROUND = "n_r_background";
    public static final String REM_TEXT = "n_r_text";
    public static final String REM_SHOW_ACTIONS = "n_r_show_actions";
    public static final String REM_SHOW_ACTIONS_TEXT = "n_r_show_actions_text";
    public static final String REM_ACTIONS_TEXT_COLOR = "n_r_actions_text_color";
    public static final String REM_ACTIONS_ICON_COLOR = "n_r_actions_icon_color";
    public static final String REM_PUSH_BACKGROUND = "n_r_push_background";
    public static final String REM_PUSH_TEXT_COLOR = "n_r_push_text_color";

    public static final String BIRTHDAY_SOUND = "b_sound";
    public static final String BIRTHDAY_TRACK_NAME = "b_track_name";
    public static final String BIRTHDAY_VIBRATION = "b_vibration";
    public static final String BIRTHDAY_IMPORTANT = "b_important";
    public static final String BIRTHDAY_DAY = "b_day";
    public static final String BIRTHDAY_TIME = "b_time";

    public static final String FAST_REM_DELAY_1 = "fast_rem_delay_1";
    public static final String FAST_REM_DELAY_2 = "fast_rem_delay_2";
    public static final String FAST_REM_DELAY_3 = "fast_rem_delay_3";


    private Uri soundUri;
    private Uri birthdaySoundUri;
    private SharedPreferences sp;
    private Preference soundPref;
    private Preference vibrationPref;
    private long[] vibration;
    private Preference birthdaySoundPref;
    private Preference birthdayVibrationPref;
    private long[] birthdayVibration;
    private SwitchPreference birthdayPriority;
    private Preference nDelay1;
    private Preference nDelay2;
    private Preference nDelay3;
    private Preference bTime;
    private Preference bDay;

    @Override
    public void onCreate(Bundle paramBundle) {
        super.onCreate(paramBundle);
        addPreferencesFromResource(R.xml.settings);

        sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        soundPref = findPreference(SOUND);
        setSoundPrefSummary();
        this.soundPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                setSoundByDefault();
                return true;
            }
        });

        vibrationPref = findPreference(VIBRATION);
        initVibroPattern(1);
        vibrationPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                setVibrationByDefault(1);
                return true;
            }
        });

        SwitchPreference trackCBPref = (SwitchPreference) findPreference(TRACK_CLIPBOARD);
        trackCBPref.setOnPreferenceChangeListener(this);

        SwitchPreference remActions = (SwitchPreference) findPreference(REM_SHOW_ACTIONS);
        remActions.setOnPreferenceChangeListener(this);
        SwitchPreference quickActions = (SwitchPreference) findPreference(QUICK_SHOW_ACTIONS);
        quickActions.setOnPreferenceChangeListener(this);

        birthdaySoundPref = findPreference(BIRTHDAY_SOUND);
        setBirthdaySoundPrefSummary();
        birthdaySoundPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                setBirthdaySoundByDefault();
                return true;
            }
        });

        birthdayVibrationPref = findPreference(BIRTHDAY_VIBRATION);
        initVibroPattern(2);
        birthdayVibrationPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                setVibrationByDefault(2);
                return true;
            }
        });

        birthdayPriority = (SwitchPreference) findPreference(BIRTHDAY_IMPORTANT);

        nDelay1 = findPreference(FAST_REM_DELAY_1);
        setDelaySummary(nDelay1, FAST_REM_DELAY_1, "00:05");
        nDelay1.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                showTimePicker(nDelay1, FAST_REM_DELAY_1, "00:05");
                return true;
            }
        });

        nDelay2 = findPreference(FAST_REM_DELAY_2);
        setDelaySummary(nDelay2, FAST_REM_DELAY_2, "00:20");
        nDelay2.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                showTimePicker(nDelay2, FAST_REM_DELAY_2, "00:20");
                return true;
            }
        });

        nDelay3 = findPreference(FAST_REM_DELAY_3);
        setDelaySummary(nDelay3, FAST_REM_DELAY_3, "01:00");
        nDelay3.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                showTimePicker(nDelay3, FAST_REM_DELAY_3, "01:00");
                return true;
            }
        });

        bTime = findPreference(BIRTHDAY_TIME);
        setDelaySummary(bTime, BIRTHDAY_TIME, "09:00");
        bTime.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                showTimePicker(bTime, BIRTHDAY_TIME, "09:00");
                return true;
            }
        });

        bDay = findPreference(BIRTHDAY_DAY);
        setBNotifyDaySummary(sp.getString(BIRTHDAY_DAY, "0"));
        bDay.setOnPreferenceChangeListener(this);


        ifRemoveImportantDefault();
        ifRemovePushSettings();


    }

    private void showTimePicker(Preference preference, String key, String s) {
        String time = sp.getString(key, s);
        String[] hourMin = time.split(":");
        new TimePickerDialog(Settings.this, R.style.PickerStyle, getFirstTimeListener(preference, key),
                Integer.parseInt(hourMin[0]),
                Integer.parseInt(hourMin[1]), true)
                .show();
    }

    private void setDelaySummary(Preference pref, String key, String t) {
        String time = sp.getString(key, t);
        pref.setSummary(time);
    }

    public void ifRemoveImportantDefault() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            PreferenceCategory delayedDefaultsCategory = (PreferenceCategory) findPreference(DELAYED_DEFAULTS_CATEGORY);
            SwitchPreference delayedPriority = (SwitchPreference) findPreference(IMPORTANT);
            PreferenceCategory birthdayDefaultsCategory = (PreferenceCategory) findPreference(BIRTHDAY_DEFAULTS_CATEGORY);


            delayedDefaultsCategory.removePreference(delayedPriority);
            birthdayDefaultsCategory.removePreference(birthdayPriority);
        }
    }

    public void ifRemovePushSettings() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            PreferenceScreen delayedAppearanceScreen = (PreferenceScreen) findPreference(DELAYED_APPEARANCE_SCREEN);
            PreferenceCategory pushCategory = (PreferenceCategory) findPreference(DELAYED_PUSH_CATEGORY);
            Preference pushBackground = findPreference(REM_PUSH_BACKGROUND);
            Preference pushTextColor = findPreference(REM_PUSH_TEXT_COLOR);
            delayedAppearanceScreen.removePreference(pushCategory);
            delayedAppearanceScreen.removePreference(pushBackground);
            delayedAppearanceScreen.removePreference(pushTextColor);
        }
    }

    private void initVibroPattern(int i) {
        try {
            if (i == 1) {
                String vPattern = sp.getString(VIBRATION, "1");
                if (vPattern.equals("1")) {
                    vibrationPref.setSummary(getString(R.string.text_default));
                } else {
                    vibration = CreateDelayedNote.parseVibrationFromString(vPattern);
                    vibrationPref.setSummary(CreateDelayedNote.parseVibroTitle(getApplicationContext(), vibration));
                }
            } else if (i == 2) {
                String vPattern = sp.getString(BIRTHDAY_VIBRATION, "1");
                if (vPattern.equals("1")) {
                    birthdayVibrationPref.setSummary(getString(R.string.text_default));
                } else {
                    birthdayVibration = CreateDelayedNote.parseVibrationFromString(vPattern);
                    birthdayVibrationPref.setSummary(CreateDelayedNote.parseVibroTitle(getApplicationContext(), birthdayVibration));
                }
            }
        } catch (Exception e) {
        }
    }

    private TimePickerDialog.OnTimeSetListener getFirstTimeListener(final Preference preference, final String key) {
        TimePickerDialog.OnTimeSetListener listener = new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int i, int i1) {
                String hour = (i < 10) ? "0" + String.valueOf(i) : String.valueOf(i);
                String minute = (i1 < 10) ? "0" + String.valueOf(i1) : String.valueOf(i1);
                String time = hour.concat(":").concat(minute);
                sp.edit().putString(key, time).commit();
                preference.setSummary(time);
            }
        };
        return listener;
    }

    private VibroDialogFragment.MDialogInterface getBVibroListener() {
        VibroDialogFragment.MDialogInterface listener = new VibroDialogFragment.MDialogInterface() {
            @Override
            public void getResult(long[] vibroPattern, String v, String p, String r) {
                birthdayVibration = vibroPattern;
                if (v.equals("0.0")) {
                    birthdayVibrationPref.setSummary(getString(R.string.no_vibration_summary));
                } else {
                    birthdayVibrationPref.setSummary(v + " x " + p + " x " + r);
                }
                SharedPreferences.Editor editor = sp.edit();
                editor.putString(BIRTHDAY_VIBRATION, CreateDelayedNote.getNoteVibration(birthdayVibration)).apply();
            }

            @Override
            public void clearVibro() {
                birthdayVibration = null;
                birthdayVibrationPref.setSummary(getString(R.string.text_default));
                SharedPreferences.Editor editor = sp.edit();
                editor.putString(BIRTHDAY_VIBRATION, "1").apply();
            }
        };
        return listener;

    }

    private void setVibrationByDefault(int i) {
        VibroDialogFragment vibroDialog = new VibroDialogFragment();
        if (i == 1) {
            vibroDialog.setmDialogInterface(this);
        } else if (i == 2) {
            vibroDialog.setmDialogInterface(getBVibroListener());
        }
        setDialogArguments(vibroDialog, i);
        vibroDialog.show(getFragmentManager(), getString(R.string.pattern));
    }

    private void setDialogArguments(VibroDialogFragment vibroDialog, int i) {
        if (i == 1) {
            if (vibration != null) {
                Bundle bundle = new Bundle();
                bundle.putInt("vibrate", (int) vibration[1] / 100);
                bundle.putInt("pause", (int) vibration[2] / 100);
                bundle.putInt("repeat", (vibration.length - 1) / 2);
                vibroDialog.setArguments(bundle);
            }
        } else if (i == 2) {
            if (birthdayVibration != null) {
                Bundle bundle = new Bundle();
                bundle.putInt("vibrate", (int) birthdayVibration[1] / 100);
                bundle.putInt("pause", (int) birthdayVibration[2] / 100);
                bundle.putInt("repeat", (birthdayVibration.length - 1) / 2);
                vibroDialog.setArguments(bundle);
            }
        }
    }

    private void setSoundPrefSummary() {
        String trackName = sp.getString(TRACK_NAME, "0");
        if (!trackName.equals("0")) {
            soundPref.setSummary(trackName);
        }
    }

    private void setBirthdaySoundPrefSummary() {
        String trackName = sp.getString(BIRTHDAY_TRACK_NAME, "0");
        if (!trackName.equals("0")) {
            soundPref.setSummary(trackName);
        }
    }

    private void setSoundByDefault() {
        String trackName = sp.getString(TRACK_NAME, "0");
        if (trackName.equals("0")) {
            trackName = getString(R.string.text_default);
        }

        showSoundDialog(Settings.this, trackName, this);
    }

    private void setBirthdaySoundByDefault() {
        String trackName = sp.getString(BIRTHDAY_TRACK_NAME, "0");
        if (trackName.equals("0")) {
            trackName = getString(R.string.text_default);
        }

        showSoundDialog(Settings.this, trackName, getBirthdaySoundListener());
    }

    private DialogInterface.OnClickListener getBirthdaySoundListener() {
        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                switch (i) {
                    case DialogInterface.BUTTON_POSITIVE:
                        checkSDPermission(2);
                        break;
                    case DialogInterface.BUTTON_NEGATIVE:
                        SharedPreferences.Editor edit = sp.edit();
                        edit.putString(BIRTHDAY_SOUND, "-1");
                        edit.putString(BIRTHDAY_TRACK_NAME, getString(R.string.no_sound_summary)).apply();
                        birthdaySoundPref.setSummary(getString(R.string.no_sound_summary));
                        break;
                    case DialogInterface.BUTTON_NEUTRAL:
                        SharedPreferences.Editor editor = sp.edit();
                        editor.putString(BIRTHDAY_SOUND, "0");
                        editor.putString(BIRTHDAY_TRACK_NAME, "0").apply();
                        birthdaySoundPref.setSummary(R.string.text_default);
                        break;
                }
            }
        };
        return listener;
    }

    private void showSoundDialog(Context context, String trackName, DialogInterface.OnClickListener listener) {
        new AlertDialog.Builder(Settings.this).setTitle(trackName)
                .setPositiveButton(R.string.choose, listener)
                .setNegativeButton(getString(R.string.no_sound_summary), listener)
                .setNeutralButton(getString(R.string.standart_dialog_button), listener)
                .create().show();
    }

    private void checkSDPermission(int i) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            int hasWriteSDPermission = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE);
            if (hasWriteSDPermission == PackageManager.PERMISSION_DENIED) {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, i);
                return;
            } else {
                chooseSoundIntent(i);
            }
        } else {
            chooseSoundIntent(i);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                    Toast.makeText(this, getString(R.string.sd_permission_denied), Toast.LENGTH_SHORT).show();
                    chooseSoundIntent(1);
                } else {
                    chooseSoundIntent(1);
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void chooseSoundIntent(int i) {
        if (i == 1) {
            soundUri = Uri.parse(sp.getString(SOUND, "0"));
            Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALL);
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, getString(R.string.melody));
            if (soundUri.toString() != "0") {
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, soundUri);
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_DEFAULT_URI, soundUri);
            }
            startActivityForResult(intent, i);
        } else if (i == 2) {
            birthdaySoundUri = Uri.parse(sp.getString(BIRTHDAY_SOUND, "0"));
            Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALL);
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, getString(R.string.melody));
            if (birthdaySoundUri.toString() != "0") {
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, birthdaySoundUri);
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_DEFAULT_URI, birthdaySoundUri);
            }
            startActivityForResult(intent, i);
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == -1 && requestCode == 1) {
            try {
                soundUri = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
                if (soundUri != null) {
                    Cursor mCursor = getContentResolver().query(soundUri, null, null, null, null);
                    if (mCursor.moveToFirst()) {
                        String trackName = mCursor.getString(8);
                        soundPref.setSummary(trackName);
                        SharedPreferences.Editor editor = sp.edit();
                        editor.putString(SOUND, soundUri.toString());
                        editor.putString(TRACK_NAME, trackName).apply();
                        mCursor.close();
                    }

                }
            } catch (Exception e) {

            }
        } else if (resultCode == -1 && requestCode == 2) {
            try {
                birthdaySoundUri = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
                if (birthdaySoundUri != null) {
                    Cursor mCursor = getContentResolver().query(birthdaySoundUri, null, null, null, null);
                    if (mCursor.moveToFirst()) {
                        String trackName = mCursor.getString(8);
                        birthdaySoundPref.setSummary(trackName);
                        SharedPreferences.Editor editor = sp.edit();
                        editor.putString(BIRTHDAY_SOUND, birthdaySoundUri.toString());
                        editor.putString(BIRTHDAY_TRACK_NAME, trackName).apply();
                        mCursor.close();
                    }

                }
            } catch (Exception e) {

            }
        }
    }

    @Override
    public void onClick(DialogInterface dialogInterface, int i) {
        switch (i) {
            case DialogInterface.BUTTON_POSITIVE:
                checkSDPermission(1);
                break;
            case DialogInterface.BUTTON_NEGATIVE:
                SharedPreferences.Editor edit = sp.edit();
                edit.putString(SOUND, "-1");
                edit.putString(TRACK_NAME, getString(R.string.no_sound_summary)).apply();
                soundPref.setSummary(getString(R.string.no_sound_summary));
                break;
            case DialogInterface.BUTTON_NEUTRAL:
                SharedPreferences.Editor editor = sp.edit();
                editor.putString(SOUND, "0");
                editor.putString(TRACK_NAME, "0").apply();
                soundPref.setSummary(R.string.text_default);
                break;
        }
    }

    @Override
    public void getResult(long[] vibroPattern, String v, String p, String r) {
        vibration = vibroPattern;
        if (v.equals("0.0")) {
            vibrationPref.setSummary(getString(R.string.no_vibration_summary));
        } else {
            vibrationPref.setSummary(v + " x " + p + " x " + r);
        }
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(VIBRATION, CreateDelayedNote.getNoteVibration(vibration)).apply();
    }

    @Override
    public void clearVibro() {
        vibration = null;
        vibrationPref.setSummary(getString(R.string.text_default));
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(VIBRATION, "1").apply();
    }


    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference.getKey().equals(TRACK_CLIPBOARD)) {
            try {
                Intent clipService = new Intent(getApplicationContext(), ClipboardService.class);
                toggleServiceRun((boolean) newValue, clipService);
            } catch (Exception e) {
            }
        }
        if (preference.getKey().equals(REM_SHOW_ACTIONS)) {
            SwitchPreference remShowActionText = (SwitchPreference) findPreference(REM_SHOW_ACTIONS_TEXT);
            if (!(boolean) newValue) {
                remShowActionText.setChecked(false);
            }
        }
        if (preference.getKey().equals(QUICK_SHOW_ACTIONS)) {
            SwitchPreference quickShowActionText = (SwitchPreference) findPreference(QUICK_SHOW_ACTIONS_TEXT);
            if (!(boolean) newValue) {
                quickShowActionText.setChecked(false);
            }
        }
        if (preference.getKey().equals(BIRTHDAY_DAY)) {
            setBNotifyDaySummary(newValue);
        }
        return true;
    }

    private void setBNotifyDaySummary(Object newValue) {
        String summary;
        switch (newValue.toString()) {
            case "0":
                summary = getString(R.string.current_day);
                break;
            case "-1":
                summary = getString(R.string.minus_one);
                break;
            case "-2":
                summary = getString(R.string.minus_two);
                break;
            case "-7":
                summary = getString(R.string.minus_three);
                break;
            default:
                summary = getString(R.string.current_day);
                break;
        }
        bDay.setSummary(summary);
    }

    private void toggleServiceRun(boolean newValue, Intent clipService) {
        if (newValue) {
            startService(clipService);
        } else {
            stopService(clipService);
        }
    }
}
