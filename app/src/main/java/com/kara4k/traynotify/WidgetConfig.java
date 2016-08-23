package com.kara4k.traynotify;


import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import yuku.ambilwarna.AmbilWarnaDialog;


public class WidgetConfig extends AppCompatActivity{

    int textColor = Color.WHITE;
    int textSize = 14;
    int backgroundColor = Color.BLACK;
    String wTitle;
    String wText;
    int numId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.widget_config);

        final MyView textColorView = (MyView) findViewById(R.id.text_color);
        final MyView textSizeView = (MyView) findViewById(R.id.text_size);
        final MyView backgroundView = (MyView) findViewById(R.id.backkground);
        final MyView noteView = (MyView) findViewById(R.id.choose_note);

        setColorLabel(textColorView, textColor);
        setColorLabel(backgroundView, backgroundColor);

        final AmbilWarnaDialog.OnAmbilWarnaListener textColorListener = new AmbilWarnaDialog.OnAmbilWarnaListener() {
            @Override
            public void onCancel(AmbilWarnaDialog dialog) {

            }

            @Override
            public void onOk(AmbilWarnaDialog dialog, int color) {
                textColor = color;
                setColorLabel(textColorView, textColor);
            }
        };

        final AmbilWarnaDialog.OnAmbilWarnaListener backgroundColorListener = new AmbilWarnaDialog.OnAmbilWarnaListener() {
            @Override
            public void onCancel(AmbilWarnaDialog dialog) {

            }

            @Override
            public void onOk(AmbilWarnaDialog dialog, int color) {
                backgroundColor = color;
                setColorLabel(backgroundView, backgroundColor);
            }
        };

        textColorView.setSecondOnClickListener(new MyView.SecondOnClickListener() {
            @Override
            public void onClick() {
                AmbilWarnaDialog colorPicker = new AmbilWarnaDialog(WidgetConfig.this, textColor, true, textColorListener);
                colorPicker.show();
            }
        });


        textSizeView.setSecondOnClickListener(new MyView.SecondOnClickListener() {
            @Override
            public void onClick() {
                TextSizeDialogFragment sizeDialog = new TextSizeDialogFragment();
                sizeDialog.setOnSizeSelect(new TextSizeDialogFragment.OnSizeSelect() {
                    @Override
                    public void setSize(int i) {
                        textSize = i;
                        textSizeView.getText().setText(String.valueOf(textSize));
                    }
                });
                Bundle bundle = new Bundle();
                bundle.putInt("size", textSize);
                sizeDialog.setArguments(bundle);
                sizeDialog.show(getSupportFragmentManager(), "Tag");
            }
        });

        backgroundView.setSecondOnClickListener(new MyView.SecondOnClickListener() {
            @Override
            public void onClick() {
                AmbilWarnaDialog colorPicker = new AmbilWarnaDialog(WidgetConfig.this, backgroundColor, true, backgroundColorListener);
                colorPicker.show();
            }
        });

        noteView.setSecondOnClickListener(new MyView.SecondOnClickListener() {
            @Override
            public void onClick() {
                NotesDialogFragment notes = new NotesDialogFragment();
                notes.setGetNoteWidget(new NotesDialogFragment.GetNoteWidget() {
                    @Override
                    public void getNoteData(int i, String title, String text) {
                        noteView.setText(title);
                        numId = i;
                        wTitle = title;
                        wText = text;   // TODO: 23.08.2016  
                    }
                });
                notes.show(getSupportFragmentManager(), "notes");
            }
        });


    }

    private void setColorLabel(MyView textColorView, int color) {
        textColorView.getImage().setBackgroundColor(color);
        textColorView.setText("#" + Integer.toHexString(color));
    }


}

