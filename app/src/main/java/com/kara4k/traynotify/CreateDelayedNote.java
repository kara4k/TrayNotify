package com.kara4k.traynotify;


import android.content.Intent;
import android.database.Cursor;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import java.text.DateFormatSymbols;

public class CreateDelayedNote extends AppCompatActivity {

    private Intent intent;
    private Button sound;
    private Uri soundUri;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_notification);

        CheckButton mon = (CheckButton) findViewById(R.id.monday);
        CheckButton tue = (CheckButton) findViewById(R.id.tuesday);
        CheckButton wed = (CheckButton) findViewById(R.id.wednesday);
        CheckButton thu = (CheckButton) findViewById(R.id.thursday);
        CheckButton fri = (CheckButton) findViewById(R.id.friday);
        CheckButton sat = (CheckButton) findViewById(R.id.saturday);
        CheckButton sun = (CheckButton) findViewById(R.id.sunday);

        DateFormatSymbols ddd = DateFormatSymbols.getInstance();
        String[] shortWeekdays = ddd.getShortWeekdays();
        mon.setText(shortWeekdays[2]);
        tue.setText(shortWeekdays[3]);
        wed.setText(shortWeekdays[4]);
        thu.setText(shortWeekdays[5]);
        fri.setText(shortWeekdays[6]);
        sat.setText(shortWeekdays[7]);
        sun.setText(shortWeekdays[1]);

        sound = (Button) findViewById(R.id.sound);
        sound.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALL);
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "Select Tone");
                if (soundUri != null) {
                    intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, soundUri);
                    intent.putExtra(RingtoneManager.EXTRA_RINGTONE_DEFAULT_URI, soundUri);
                }

                startActivityForResult(intent, 5);
            }
        });
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == -1 && requestCode == 5) {
            soundUri = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
            if (soundUri != null) {
                try {
                    Cursor mCursor = getContentResolver().query(soundUri, null, null, null, null); // TODO: 01.08.2016 permissions
                    if (mCursor.moveToFirst()) {
                        sound.setText(mCursor.getString(8));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}

