package com.kara4k.traynotify;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.NotificationCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ToggleButton;


public class AddNotify extends Fragment {

    private static AddNotify instance;
    private EditText title;
    private EditText text;
    private ToggleButton ongoing;
    private Button create;
    private SeekBar seekbar;
    private Button delete;
    private NotificationManager nm;
    private TextView textId;


//    public AddNotify() {
//    }
//
//    public static AddNotify getInstance() {
//        if (instance == null) {
//            instance = new AddNotify();
//        }
//        return instance;
//    }
//
//    private EditText ed;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.add, container, false);
        title = (EditText) view.findViewById(R.id.editTitle);
        text = (EditText) view.findViewById(R.id.textedit);
        ongoing = (ToggleButton) view.findViewById(R.id.toggle_ongoing);
        create = (Button) view.findViewById(R.id.create);
        seekbar = (SeekBar) view.findViewById(R.id.seekBar);
        delete = (Button) view.findViewById(R.id.delete);
        textId = (TextView) view.findViewById(R.id.text_id);

        nm = (NotificationManager) getActivity().getSystemService(getActivity().NOTIFICATION_SERVICE);


        if (getActivity().getIntent() != null){
            text.setText(getActivity().getIntent().getStringExtra(Intent.EXTRA_TEXT));
            title.setText(getActivity().getIntent().getStringExtra(Intent.EXTRA_SUBJECT));
            ongoing.setChecked(getActivity().getIntent().getBooleanExtra("ongoing", false));
            seekbar.setProgress(getActivity().getIntent().getIntExtra("id", 0));
            textId.setText("ID: " + getActivity().getIntent().getIntExtra("id", 0));
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
                textId.setText("ID: " + i);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        return view;
    }

    private void delete() {
        nm.cancel(seekbar.getProgress());
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

        super.onSaveInstanceState(outState);
    }

    public void test() {

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getActivity());
        if (title.getText().toString().equals("")) {
            mBuilder.setContentTitle(title.getHint());
        } else {
            mBuilder.setContentTitle(title.getText().toString());
        }
        mBuilder.setContentText(text.getText().toString());
        mBuilder.setContentInfo("ID: " + seekbar.getProgress());
        mBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText(text.getText().toString()));

        if (ongoing.isChecked()) {
            mBuilder.setOngoing(true);
        }
        mBuilder.setDefaults(Notification.DEFAULT_ALL);

        Intent intent = new Intent(getContext(), MainActivity.class);
        intent.putExtra(Intent.EXTRA_SUBJECT, title.getText().toString());
        intent.putExtra(Intent.EXTRA_TEXT, text.getText().toString());
        intent.putExtra("ongoing", ongoing.isChecked());
        intent.putExtra("id", seekbar.getProgress());

        mBuilder.setContentIntent(PendingIntent.getActivity(getContext(), 0, intent, 0));
        mBuilder.setSmallIcon(R.drawable.notify);
//        mBuilder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.test));
        nm.notify(seekbar.getProgress(), mBuilder.build());
    }


}
