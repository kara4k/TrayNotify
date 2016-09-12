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
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

public class Settings extends PreferenceActivity implements DialogInterface.OnClickListener, VibroDialogFragment.MDialogInterface
{

    public static final String SOUND = "sound";
    public static final String TRACK_NAME = "track_name";
    public static final String VIBRATION = "vibration";


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
        vibrationPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                setVibrationByDefault();
                return true;
            }
        });

    }

    private void setVibrationByDefault() {
        VibroDialogFragment vibroDialog = new VibroDialogFragment();
        vibroDialog.setmDialogInterface(this);
//        if (vibration != null) {
//            Bundle bundle = new Bundle();
//            bundle.putInt("vibrate", (int) vibration[1] / 100);
//            bundle.putInt("pause", (int) vibration[2] / 100);
//            bundle.putInt("repeat", (vibration.length - 1) / 2);
//            vibroDialog.setArguments(bundle);
//        }
        vibroDialog.show(getFragmentManager(), getString(R.string.pattern));
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
                        Log.e("TAG", "onActivityResult: " + trackName);
                        Log.e("TAG", "onActivityResult: " + soundUri.toString());
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
    }

    @Override
    public void clearVibro() {
        vibration = null;
        vibrationPref.setSummary(getString(R.string.text_default));
    }
}
