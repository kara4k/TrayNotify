package com.kara4k.traynotify;


import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class DelayedAdapter extends RecyclerView.Adapter<DelayedAdapter.DelayedNotesViewHolder>{

    private static DelayedAdapter quickAdapter;

    public List<DelayedNote> getNotes() {
        return notes;
    }

    private List<DelayedNote> notes;
    private Context context;

    DelayedAdapter() {
    }

    public static DelayedAdapter getInstance() {
        if (quickAdapter == null) {
            quickAdapter = new DelayedAdapter();
        }
        return quickAdapter;
    }




    public void setList(List<DelayedNote> notes) {
        this.notes = notes;
    }

    @Override
    public DelayedNotesViewHolder onCreateViewHolder(ViewGroup viewGroup, final int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.quick_item, viewGroup, false);
        context = viewGroup.getContext();
        return new DelayedNotesViewHolder(v);
    }

    @Override
    public void onBindViewHolder(DelayedNotesViewHolder notesViewHolder, int i) {
        notesViewHolder.title.setText(notes.get(i).getTitle());
        notesViewHolder.text.setText(notes.get(i).getText());
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, dd.MM.yyyy");
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
        String date = dateFormat.format(new Date(notes.get(i).getSetTime()));
        date = date.substring(0, 1).toUpperCase() + date.substring(1);
        notesViewHolder.date.setText(date);
        notesViewHolder.time.setText(timeFormat.format(new Date(notes.get(i).getSetTime())));
        notesViewHolder.numid.setText("#" + String.valueOf(notes.get(i).getCheckId()).substring(1));

    }



    @Override
    public int getItemCount() {
        return notes.size();
    }

    public void remove(int position) {
        DBQuick db = new DBQuick(context);
        db.open();
        int id = notes.get(position).getId();
        db.removeNote(id);
        db.close();
        notes.remove(position);
        notifyItemRemoved(position);

    }

    public static class DelayedNotesViewHolder extends RecyclerView.ViewHolder {

        private final TextView title;
        private final TextView text;
        private final TextView date;
        private final TextView time;
        private final TextView numid;

        DelayedNotesViewHolder(final View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.title);
            text = (TextView) itemView.findViewById(R.id.text);
            date = (TextView) itemView.findViewById(R.id.date);
            time = (TextView) itemView.findViewById(R.id.time);
            numid = (TextView) itemView.findViewById(R.id.numid);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    try {
                        Context context = view.getContext();
                        DelayedNote note = DelayedAdapter.getInstance().getNotes().get(getAdapterPosition());
                        Intent intent = new Intent(context, CreateDelayedNote.class);
                        intent.putExtra(Intent.EXTRA_SUBJECT, note.getTitle());
                        intent.putExtra(Intent.EXTRA_TEXT, note.getText());
                        intent.putExtra("id", note.getCheckId());
                        context.startActivity(intent);
                    } catch (Exception e) {
                        e.printStackTrace(); // TODO: 29.07.2016
                    }
                }
            });
            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    try {
                        DelayedAdapter.getInstance().remove(getAdapterPosition());
                    } catch (Exception e) {
                        e.printStackTrace(); // TODO: 29.07.2016
                    }
                    return false;
                }
            });
        }

    }

}