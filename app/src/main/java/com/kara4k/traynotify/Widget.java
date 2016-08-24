package com.kara4k.traynotify;


import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.widget.RemoteViews;

public class Widget extends AppWidgetProvider {


    private static int noteId;

    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);

        SharedPreferences sp = context.getSharedPreferences(WidgetConfig.WIDGET_CONF, Context.MODE_PRIVATE);
        for (int id : appWidgetIds) {
            updateWidget(context, appWidgetManager, sp, id);
        }
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        super.onDeleted(context, appWidgetIds);
        SharedPreferences.Editor editor = context.getSharedPreferences(
                WidgetConfig.WIDGET_CONF, Context.MODE_PRIVATE).edit();
        for (int widgetID : appWidgetIds) {
            editor.remove(WidgetConfig.WIDGET_TEXT_COLOR + widgetID);
            editor.remove(WidgetConfig.WIDGET_TEXT_SIZE + widgetID);
            editor.remove(WidgetConfig.WIDGET_BACKGROUND + widgetID);
            editor.remove(WidgetConfig.WIDGET_NOTE_ID + widgetID);
            editor.remove("#" + noteId);
        }
        editor.apply();
    }

    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);
    }

    static void updateWidget(Context context, AppWidgetManager appWidgetManager,
                             SharedPreferences sp, int widgetID) {


        int textColor = sp.getInt(WidgetConfig.WIDGET_TEXT_COLOR + widgetID, 0);
        int textSize = sp.getInt(WidgetConfig.WIDGET_TEXT_SIZE + widgetID, 14);
        int backgroundColor = sp.getInt(WidgetConfig.WIDGET_BACKGROUND + widgetID, 0);
        noteId = sp.getInt(WidgetConfig.WIDGET_NOTE_ID + widgetID, 0);

        RemoteViews widgetView = new RemoteViews(context.getPackageName(), R.layout.widget);
        widgetView.setInt(R.id.text, "setBackgroundColor", backgroundColor);
        widgetView.setTextColor(R.id.text, textColor);
        widgetView.setFloat(R.id.text, "setTextSize", textSize);

        DBQuick dbQuick = new DBQuick(context);
        dbQuick.open();
        Cursor note = dbQuick.getCurrentNote(noteId);
        String text = "";
        if (note.moveToFirst()) {
            text = note.getString(1) + "\n" + note.getString(0);
        }
        note.close();
        dbQuick.close();
        widgetView.setTextViewText(R.id.text, text);

        Intent configIntent = new Intent(context,WidgetConfig.class);
        configIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_CONFIGURE);
        configIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetID);
        configIntent.putExtra(WidgetConfig.WIDGET_TEXT_COLOR, textColor);
        configIntent.putExtra(WidgetConfig.WIDGET_TEXT_SIZE, textSize);
        configIntent.putExtra(WidgetConfig.WIDGET_BACKGROUND, backgroundColor);
        configIntent.putExtra(WidgetConfig.WIDGET_NOTE_ID, noteId);
        PendingIntent pIntent = PendingIntent.getActivity(context, widgetID,configIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        widgetView.setOnClickPendingIntent(R.id.text, pIntent);


        // Обновляем виджет
        appWidgetManager.updateAppWidget(widgetID, widgetView);
    }

}
