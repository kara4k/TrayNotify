package com.kara4k.traynotify;


import android.Manifest;
import android.app.AlertDialog;
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


    private Uri soundUri;
    private SharedPreferences sp;
    private Preference soundPref;
    private Preference vibrationPref;
    private long[] vibration;

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
        initVibroPattern();
        vibrationPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                setVibrationByDefault();
                return true;
            }
        });

        SwitchPreference trackCBPref = (SwitchPreference) findPreference(TRACK_CLIPBOARD);
        trackCBPref.setOnPreferenceChangeListener(this);

        SwitchPreference remActions = (SwitchPreference) findPreference(REM_SHOW_ACTIONS);
        remActions.setOnPreferenceChangeListener(this);
        SwitchPreference quickActions = (SwitchPreference) findPreference(QUICK_SHOW_ACTIONS);
        quickActions.setOnPreferenceChangeListener(this);

        ifRemoveImportantDefault();
        ifRemovePushSettings();


    }

    public void ifRemoveImportantDefault() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            PreferenceCategory delayedDefaultsCategory = (PreferenceCategory) findPreference(DELAYED_DEFAULTS_CATEGORY);
            SwitchPreference delayedPriority = (SwitchPreference) findPreference(IMPORTANT);

            delayedDefaultsCategory.removePreference(delayedPriority);
        }
    }

    public void ifRemovePushSettings() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            PreferenceScreen delayedAppearanceScreen = (PreferenceScreen) findPreference(DELAYED_APPEARANCE_SCREEN);
            PreferenceCategory pushCategory = (PreferenceCategory) findPreference(DELAYED_PUSH_CATEGORY);
            Preference pushBackground =  findPreference(REM_PUSH_BACKGROUND);
            Preference pushTextColor =  findPreference(REM_PUSH_TEXT_COLOR);
            delayedAppearanceScreen.removePreference(pushCategory);
            delayedAppearanceScreen.removePreference(pushBackground);
            delayedAppearanceScreen.removePreference(pushTextColor);
        }
    }

    private void initVibroPattern() {
        try {
            String vPattern = sp.getString(VIBRATION, "1");
            if (vPattern.equals("1")) {
                vibrationPref.setSummary(getString(R.string.text_default));
            } else {
                vibration = CreateDelayedNote.parseVibrationFromString(vPattern);
                vibrationPref.setSummary(CreateDelayedNote.parseVibroTitle(vibration));
            }
        } catch (Exception e) {
        }
    }

    private void setVibrationByDefault() {
        VibroDialogFragment vibroDialog = new VibroDialogFragment();
        vibroDialog.setmDialogInterface(this);
        setDialogArguments(vibroDialog);
        vibroDialog.show(getFragmentManager(), getString(R.string.pattern));
    }

    private void setDialogArguments(VibroDialogFragment vibroDialog) {
        if (vibration != null) {
            Bundle bundle = new Bundle();
            bundle.putInt("vibrate", (int) vibration[1] / 100);
            bundle.putInt("pause", (int) vibration[2] / 100);
            bundle.putInt("repeat", (vibration.length - 1) / 2);
            vibroDialog.setArguments(bundle);
        }
    }

    private void setSoundPrefSummary() {
        String trackName = sp.getString(TRACK_NAME, "0");
        if (!trackName.equals("0")) {
            soundPref.setSummary(trackName);
        }
    }

    private void setSoundByDefault() {
        String trackName = sp.getString(TRACK_NAME, "0");
        if (trackName.equals("0")) {
            trackName = getString(R.string.text_default);
        }

        showSoundDialog(Settings.this, trackName);
    }

    private void showSoundDialog(Context context, String trackName) {
        new AlertDialog.Builder(Settings.this).setTitle(trackName)
                .setPositiveButton(R.string.choose, this)
                .setNegativeButton(getString(R.string.cancel), this)
                .setNeutralButton(getString(R.string.text_default), this)
                .create().show();
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

    private void chooseSoundIntent() {
        soundUri = Uri.parse(sp.getString(SOUND, "0"));
        Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALL);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, getString(R.string.melody));
        if (soundUri.toString() != "0") {
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, soundUri);
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_DEFAULT_URI, soundUri);
        }
        startActivityForResult(intent, 1);
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
        vibrationPref.setSummary(v + " x " + p + " x " + r);
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
            if (!(boolean)newValue) {
                remShowActionText.setChecked(false);
            }
        }
        if (preference.getKey().equals(QUICK_SHOW_ACTIONS)) {
            SwitchPreference quickShowActionText = (SwitchPreference) findPreference(QUICK_SHOW_ACTIONS_TEXT);
            if (!(boolean)newValue) {
                quickShowActionText.setChecked(false);
            }
        }
        return true;
    }

    private void toggleServiceRun(boolean newValue, Intent clipService) {
        if (newValue) {
            startService(clipService);
        } else {
            stopService(clipService);
        }
    }
}
