package com.kara4k.traynotify;


import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import yuku.ambilwarna.AmbilWarnaDialog;


public class WidgetConfig extends AppCompatActivity implements DialogInterface.OnClickListener{

    private int textColor = Color.BLACK;
    private int textSize = 14;
    private int backgroundColor = Color.WHITE;
    private String wTitle;
    String wText;
    private int numId = 0;

    private int widgetID = AppWidgetManager.INVALID_APPWIDGET_ID;
    private Intent resultValue;

    public final static String WIDGET_CONF = "widget_conf";
    public final static String WIDGET_TEXT_COLOR = "text_color_";
    public final static String WIDGET_TEXT_SIZE = "text_size_";
    public final static String WIDGET_BACKGROUND = "background_";
    public final static String WIDGET_NOTE_ID = "note_";
    private SharedPreferences sp;
    private MyView noteView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sp = getSharedPreferences(WIDGET_CONF, MODE_PRIVATE);
        cancelOnInvalidId();
        setCancelResult();

        setContentView(R.layout.widget_config);

        final MyView textColorView = (MyView) findViewById(R.id.text_color);
        final MyView textSizeView = (MyView) findViewById(R.id.text_size);
        final MyView backgroundView = (MyView) findViewById(R.id.backkground);
        noteView = (MyView) findViewById(R.id.choose_note);
        Button createBtn = (Button) findViewById(R.id.create);
        Button editBtn = (Button) findViewById(R.id.edit);


        setNoteLabel(noteView);
        setColorLabel(textColorView, textColor);
        setColorLabel(backgroundView, backgroundColor);
        textSizeView.setText(String.valueOf(textSize));

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
                final int tempId = numId;
                NotesDialogFragment notes = new NotesDialogFragment();
                notes.setGetNoteWidget(new NotesDialogFragment.GetNoteWidget() {
                    @Override
                    public void getNoteData(int i, String title) {
                        noteView.setText(title);
                        numId = i;
                        wTitle = title;

                        if (numId != tempId) {
                            SharedPreferences.Editor editor = sp.edit();
                            editor.remove("#" + tempId);
                            editor.apply();

                        }

                    }
                });
                notes.show(getSupportFragmentManager(), "notes");
            }
        });

        createBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                writePrefsAndUpdate();
                setResult(RESULT_OK, resultValue);
                finish();
            }
        });

        editBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(WidgetConfig.this, MainActivity.class);
                WidgetConfig.this.startActivity(intent);
            }
        });
    }

    @Override
    public void onBackPressed() {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(getApplicationContext());
        Widget.updateWidget(this, appWidgetManager, sp, widgetID);
        super.onBackPressed();
    }

    @Override
    protected void onStart() {
        refreshNoteLabel();
        super.onStart();
    }

    private void setNoteLabel(MyView noteView) {
        if (numId != 0) {
            try {
                getNote(getApplicationContext(), numId);
                noteView.setText(wTitle);
            } catch (Exception e) {

            }
        } else {
            noteView.setText("");
        }
    }

    private void setCancelResult() {
        resultValue = new Intent();
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetID);
        setResult(RESULT_CANCELED, resultValue);
    }

    private void writePrefsAndUpdate() {

        SharedPreferences.Editor editor = sp.edit();
        editor.putInt(WIDGET_TEXT_COLOR + widgetID, textColor);
        editor.putInt(WIDGET_TEXT_SIZE + widgetID, textSize);
        editor.putInt(WIDGET_BACKGROUND + widgetID, backgroundColor);
        editor.putInt(WIDGET_NOTE_ID + widgetID, numId);
        editor.putInt("#" + numId, widgetID);
        editor.apply();
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(getApplicationContext());
        Widget.updateWidget(this, appWidgetManager, sp, widgetID);
    }

    private void cancelOnInvalidId() {
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            widgetID = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);
            textColor = extras.getInt(WIDGET_TEXT_COLOR, Color.BLACK);
            textSize = extras.getInt(WIDGET_TEXT_SIZE, 14);
            backgroundColor = extras.getInt(WIDGET_BACKGROUND, Color.WHITE);
            numId = extras.getInt(WIDGET_NOTE_ID, 0);
        }
        if (widgetID == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish();
        }
    }

    private void setColorLabel(MyView textColorView, int color) {
        textColorView.getImage().setBackgroundColor(color);
        textColorView.setText("#" + Integer.toHexString(color));
    }

    private void getNote(Context context, int noteId) {
        DBQuick dbQuick = new DBQuick(context);
        dbQuick.open();
        Cursor note = dbQuick.getCurrentNote(noteId);
        if (note.moveToFirst()) {
            wTitle = note.getString(1);
        } else {
            wTitle = "";
        }
        note.close();
        dbQuick.close();
    }

    @Override
    public void onClick(DialogInterface dialogInterface, int i) {
        switch (i) {
            case DialogInterface.BUTTON_NEUTRAL:
                SharedPreferences.Editor editor = sp.edit();
                editor.putInt(WIDGET_NOTE_ID + widgetID, 0);
                editor.putInt("#" + numId, 0);
                editor.apply();
                refreshNoteLabel();
                break;
        }
    }

    private void refreshNoteLabel() {
        numId = sp.getInt(WidgetConfig.WIDGET_NOTE_ID + widgetID, 0);
        setNoteLabel(noteView);
    }
}

