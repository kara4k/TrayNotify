package com.kara4k.traynotify;


import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

        NotesViewHolder(final View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.name);
            text = (TextView) itemView.findViewById(R.id.text);
            date = (TextView) itemView.findViewById(R.id.date);
            time = (TextView) itemView.findViewById(R.id.day_of_week);
            numid = (TextView) itemView.findViewById(R.id.numid);
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

    }

}