package com.kara4k.traynotify;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

public class NActionReceiver extends BroadcastReceiver {

    public static final String TYPE = "type";
    public static final String ID = "id";
    public static final String ACTION = "action";

    @Override
    public void onReceive(Context context, Intent intent) {

        int action = intent.getIntExtra(ACTION, 0);
        Log.e("NActionReceiver", "onReceive: " + action);

        if (action == 3) {
            try {
                removeTrayNotification(context, intent);
            } catch (Exception e) {
            }
        }
    }



    private void removeTrayNotification(Context context, Intent intent) {
        NotificationManagerCompat nm = NotificationManagerCompat.from(context);
        int id = intent.getIntExtra("id", 0);
        int type = intent.getIntExtra("type", 0);
        nm.cancel(id);
        if (type == 1) {
            setTrayIconDB(context, id);
            refreshRecyclerIfOpen(context, id);
        }
    }

    private void setTrayIconDB(Context context, int id) {
        try {
            DBQuick db = new DBQuick(context);
            db.open();
            db.setQuickTrayInDB(id, 0);
            db.close();
        } catch (Exception e) {
        }
    }

    private void refreshRecyclerIfOpen(Context context, int id) {
        try {
            MainActivity.refreshQuickTrayIcon(context, id);
        } catch (Exception e) {
        }
    }
}
