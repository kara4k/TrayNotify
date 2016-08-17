package com.kara4k.traynotify;


import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class VibroDialogFragment extends DialogFragment implements DialogInterface.OnClickListener, SeekBar.OnSeekBarChangeListener {


    private TextView vibrateText;
    private TextView pauseText;
    private TextView repeatText;

    private MDialogInterface mDialogInterface;
    private SeekBar vibrate;
    private SeekBar pause;
    private SeekBar repeat;

    public void setmDialogInterface(MDialogInterface mDialogInterface) {
        this.mDialogInterface = mDialogInterface;
    }

    interface MDialogInterface {
        void getResult(long[] vibroPattern, String vibration, String pause, String repeat);
        void clearVibro();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        View form = getActivity().getLayoutInflater().inflate(R.layout.set_vibro_style, null);
        vibrate = (SeekBar) form.findViewById(R.id.vibrate);
        pause = (SeekBar) form.findViewById(R.id.pause);
        repeat = (SeekBar) form.findViewById(R.id.repeat);
        vibrate.setOnSeekBarChangeListener(this);
        pause.setOnSeekBarChangeListener(this);
        repeat.setOnSeekBarChangeListener(this);
        vibrateText = (TextView) form.findViewById(R.id.vibrate_text);
        pauseText = (TextView) form.findViewById(R.id.pause_text);
        repeatText = (TextView) form.findViewById(R.id.repeat_text);

        if (getArguments() != null) {
            vibrate.setProgress(getArguments().getInt("vibrate", 0));
            pause.setProgress(getArguments().getInt("pause", 0));
            repeat.setProgress(getArguments().getInt("repeat", 1) - 1);
        } else {
            vibrateText.setText("0.0" + getString(R.string.sec));
            pauseText.setText("0.0" + getString(R.string.sec));
            repeatText.setText("1" + getString(R.string.times));
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        return builder.setView(form).setNeutralButton(getString(R.string.text_default), this).setPositiveButton(android.R.string.ok, this)
                .setNegativeButton(android.R.string.cancel, this).create();

    }


    @Override
    public void onClick(DialogInterface dialogInterface, int i) {
        switch (i) {
            case DialogInterface.BUTTON_POSITIVE:
                List<Long> vibratePattern = new ArrayList<Long>();
                vibratePattern.add(0,(long)0);
                for (int k = 0; k < repeat.getProgress() + 1; k++) {
                    vibratePattern.add(Long.parseLong(String.valueOf(vibrate.getProgress() * 100)));
                    vibratePattern.add(Long.parseLong(String.valueOf(pause.getProgress() * 100)));
                }

                long[] vibration = new long[vibratePattern.size()];
                for (int j = 0; j < vibratePattern.size(); j++) {
                    vibration[j] = vibratePattern.get(j);
                }


                if (mDialogInterface != null) {
                    mDialogInterface.getResult(vibration,
                            vibrateText.getText().toString().split(" ")[0],
                            pauseText.getText().toString().split(" ")[0],
                            repeatText.getText().toString().split(" ")[0]);
                }
                break;
            case DialogInterface.BUTTON_NEUTRAL:
                if (mDialogInterface != null) {
                    mDialogInterface.clearVibro();
                }
                break;
        }

    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
        switch (seekBar.getId()) {
            case R.id.vibrate:
                if (i < 10) {
                    vibrateText.setText("0." + i + getString(R.string.sec));
                } else {
                    vibrateText.setText(String.valueOf(i)
                            .substring(0, 1)
                            .concat(".")
                            .concat(String.valueOf(i).substring(1))
                            .concat(getString(R.string.sec)));
                }
                break;
            case R.id.pause:
                if (i < 10) {
                    pauseText.setText("0." + i + getString(R.string.sec));
                } else {
                    pauseText.setText(String.valueOf(i)
                            .substring(0, 1)
                            .concat(".")
                            .concat(String.valueOf(i).substring(1))
                            .concat(getString(R.string.sec)));
                }
                break;
            case R.id.repeat:
                repeatText.setText(i + 1 + getString(R.string.times));
                break;
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }
}
