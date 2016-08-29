package com.kara4k.traynotify;


import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.support.v7.app.NotificationCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class QuickAdapter extends RecyclerView.Adapter<QuickAdapter.NotesViewHolder> {

    private static QuickAdapter quickAdapter;

    private List<Note> getNotes() {
        return notes;
    }

    private List<Note> notes;
    private Context context;
    private GetNoteId getNoteId;

    private QuickAdapter() {
    }

    public static QuickAdapter getInstance() {
        if (quickAdapter == null) {
            quickAdapter = new QuickAdapter();
        }
        return quickAdapter;
    }

    public interface GetNoteId {
        void getId(int i, String title);
    }

    public void setGetNoteId(GetNoteId getNoteId) {
        this.getNoteId = getNoteId;
    }

    public void setList(List<Note> notes) {
        this.notes = notes;
    }

    @Override
    public NotesViewHolder onCreateViewHolder(ViewGroup viewGroup, final int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.quick_item, viewGroup, false);
        context = viewGroup.getContext();
        return new NotesViewHolder(v);
    }

    @Override
    public void onBindViewHolder(NotesViewHolder notesViewHolder, int i) {
        notesViewHolder.title.setText(notes.get(i).getTitle());
        notesViewHolder.text.setText(notes.get(i).getText());
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, dd.MM.yyyy");
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
        String date = dateFormat.format(new Date(notes.get(i).getDate()));
        date = date.substring(0, 1).toUpperCase() + date.substring(1);
        notesViewHolder.date.setText(date);
        notesViewHolder.time.setText(timeFormat.format(new Date(notes.get(i).getDate())));
        notesViewHolder.numid.setText("#" + String.valueOf(notes.get(i).getNumid()).substring(1));
        setTray(notesViewHolder, i);

    }

    private void setTray(NotesViewHolder notesViewHolder, int i) {
        if (notes.get(i).getIcon() == 1) {
            notesViewHolder.tray.setImageResource(R.drawable.ic_speaker_notes_red_24dp);
        } else {
            notesViewHolder.tray.setImageResource(R.drawable.ic_speaker_notes_off_grey_24dp);
        }
    }


    @Override
    public int getItemCount() {
        return notes.size();
    }

    public void remove(int position) {
        DBQuick db = new DBQuick(context);
        db.open();
        int id = notes.get(position).getId();
        int numID = notes.get(position).getNumid();
        db.removeNote(id);
        db.close();
        notes.remove(position);
        notifyItemRemoved(position);
        updateWidget(numID);
        removeTray(numID);

    }

    private void removeTray(int numID) {
        NotificationManager nm = (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);
        nm.cancel(numID);
    }

    private void updateWidget(int numID) {
        SharedPreferences sp = context.getSharedPreferences(WidgetConfig.WIDGET_CONF, Context.MODE_PRIVATE);
        int widgetID = sp.getInt("#" + numID, -1);
        if (widgetID != -1) {
            SharedPreferences.Editor edit = sp.edit();
            edit.putInt(WidgetConfig.WIDGET_NOTE_ID + widgetID, 0);
            edit.remove("#" + numID);
            edit.apply();
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            Widget.updateWidget(context, appWidgetManager, sp, widgetID);
        }
    }

    public static class NotesViewHolder extends RecyclerView.ViewHolder {

        private final TextView title;
        private final TextView text;
        private final TextView date;
        private final TextView time;
        private final TextView numid;
        private final ImageView tray;
        private final LinearLayout dateLayout;

        NotesViewHolder(final View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.name);
            text = (TextView) itemView.findViewById(R.id.text);
            date = (TextView) itemView.findViewById(R.id.date);
            time = (TextView) itemView.findViewById(R.id.day_of_week);
            numid = (TextView) itemView.findViewById(R.id.numid);
            tray = (ImageView) itemView.findViewById(R.id.isShown);
            dateLayout = (LinearLayout) itemView.findViewById(R.id.date_layout);

            dateLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onTrayImageClick(view);
                }
            });

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    try {
                        if (QuickAdapter.getInstance().getNoteId != null) {
                            int numid = QuickAdapter.getInstance().getNotes().get(getAdapterPosition()).getNumid();
                            String title = QuickAdapter.getInstance().getNotes().get(getAdapterPosition()).getTitle();
                            QuickAdapter.getInstance().getNoteId.getId(numid, title);
                        } else {

                            Context context = view.getContext();
                            Note note = QuickAdapter.getInstance().getNotes().get(getAdapterPosition());
                            Intent intent = new Intent(context, QuickNote.class);
                            intent.putExtra(Intent.EXTRA_SUBJECT, note.getTitle());
                            intent.putExtra(Intent.EXTRA_TEXT, note.getText());
                            intent.putExtra("id", note.getNumid());
                            context.startActivity(intent);
                        }
                    } catch (Exception e) {
                    }
                }
            });
            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    try {
                        if (QuickAdapter.getInstance().getNoteId == null) {
                            QuickAdapter.getInstance().remove(getAdapterPosition());
                        }
                    } catch (Exception e) {
                    }
                    return false;
                }
            });
        }

        private void onTrayImageClick(View view) {
            try {
                Context context = view.getContext();
                Note note = QuickAdapter.getInstance().getNotes().get(getAdapterPosition());
                NotificationManager nm = (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);
                if (note.getIcon() != 1) {
                    nm.notify(note.getNumid(), createNotification(context, note));
                    tray.setImageResource(R.drawable.ic_speaker_notes_red_24dp);
                    writeTrayToDB(context, note, 1);
                    note.setIcon(1);
                } else {
                    nm.cancel(note.getNumid());
                    tray.setImageResource(R.drawable.ic_speaker_notes_off_grey_24dp);
                    writeTrayToDB(context, note, 0);
                    note.setIcon(0);
                }
            } catch (Exception e) {

            }
        }

        private void writeTrayToDB(Context context, Note note, int tray) {
            DBQuick db = new DBQuick(context);
            db.open();
            db.setQuickTrayInDB(note.getNumid(), tray);
            db.close();
        }

        private  Notification createNotification(Context context, Note note) {
            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context);
            mBuilder.setContentTitle(note.getTitle());
            mBuilder.setContentText(note.getText());
            mBuilder.setContentInfo("#" + String.valueOf(note.getNumid()).substring(1));
            mBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText(note.getText()));
            mBuilder.setOngoing(true);
            mBuilder.setContentIntent(PendingIntent.getActivities(context, note.getNumid(), makeIntent(context, note), PendingIntent.FLAG_UPDATE_CURRENT));
            mBuilder.setSmallIcon(R.drawable.notify);
            mBuilder.setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.user1));
            return mBuilder.build();


        }

        private Intent[] makeIntent(Context context, Note note) {
            Intent main = new Intent(context, MainActivity.class);
            main.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            Intent quick = new Intent(context, QuickNote.class);

            quick.putExtra(Intent.EXTRA_SUBJECT, note.getTitle());
            quick.putExtra(Intent.EXTRA_TEXT, note.getText());
            quick.putExtra("id", note.getNumid());
            return new Intent[]{main, quick};
        }


    }

}