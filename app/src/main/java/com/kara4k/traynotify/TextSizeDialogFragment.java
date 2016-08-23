package com.kara4k.traynotify;


import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

public class TextSizeDialogFragment extends DialogFragment implements DialogInterface.OnClickListener, SeekBar.OnSeekBarChangeListener {

    private TextView sampleView;
    private SeekBar seekBar;
    private TextView sizeView;
    private OnSizeSelect onSizeSelect;
    private int textSize = 14;

    public interface OnSizeSelect {
        void setSize(int i);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View form = getActivity().getLayoutInflater().inflate(R.layout.text_size_dialog, null);
        sampleView = (TextView) form.findViewById(R.id.sampleText);
        seekBar = (SeekBar) form.findViewById(R.id.seekbar);
        sizeView = (TextView) form.findViewById(R.id.text_size_view);
        seekBar.setOnSeekBarChangeListener(this);

        if (getArguments()!=null) {
            textSize = getArguments().getInt("size");
            seekBar.setProgress(textSize);
            sampleView.setTextSize((float) textSize);
            sizeView.setText(String.valueOf(seekBar.getProgress()));
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        return builder.setView(form).setPositiveButton(android.R.string.ok, this)
                .setNegativeButton(android.R.string.cancel, this).create();
    }

    @Override
    public void onClick(DialogInterface dialogInterface, int i) {
        switch (i) {
            case AlertDialog.BUTTON_POSITIVE:
                if (onSizeSelect != null) {
                    onSizeSelect.setSize(seekBar.getProgress());
                }
                break;
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
        sampleView.setTextSize((float) i);
        sizeView.setText(String.valueOf(seekBar.getProgress()));

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    public void setOnSizeSelect(OnSizeSelect onSizeSelect) {
        this.onSizeSelect = onSizeSelect;
    }
}
