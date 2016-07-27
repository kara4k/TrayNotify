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

public class QuickAdapter extends RecyclerView.Adapter<QuickAdapter.EventsViewHolder>{

    private static QuickAdapter quickAdapter;

    public List<Note> getNotes() {
        return notes;
    }

    private List<Note> notes;
    private Context context;

    QuickAdapter() {
    }

    public static QuickAdapter getInstance() {
        if (quickAdapter == null) {
            quickAdapter = new QuickAdapter();
        }
        return quickAdapter;
    }




    public void setList(List<Note> notes) {
        this.notes = notes;
    }

    @Override
    public EventsViewHolder onCreateViewHolder(ViewGroup viewGroup, final int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.quick_item, viewGroup, false);
        context = viewGroup.getContext();
        return new EventsViewHolder(v);
    }

    @Override
    public void onBindViewHolder(EventsViewHolder eventsViewHolder, int i) {
        eventsViewHolder.title.setText(notes.get(i).getTitle());
        eventsViewHolder.text.setText(notes.get(i).getText());
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, dd.MM.yyyy");
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
        String date = dateFormat.format(new Date(notes.get(i).getDate()));
        date = date.substring(0, 1).toUpperCase() + date.substring(1);
        eventsViewHolder.date.setText(date);
        eventsViewHolder.time.setText(timeFormat.format(new Date(notes.get(i).getDate())));

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

    public static class EventsViewHolder extends RecyclerView.ViewHolder {

        private final TextView title;
        private final TextView text;
        private final TextView date;
        private final TextView time;

        EventsViewHolder(final View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.title);
            text = (TextView) itemView.findViewById(R.id.text);
            date = (TextView) itemView.findViewById(R.id.date);
            time = (TextView) itemView.findViewById(R.id.time);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Context context = view.getContext();
                    Note note = QuickAdapter.getInstance().getNotes().get(getAdapterPosition());
                    Intent intent = new Intent(context, QuickNote.class);
                    intent.putExtra(Intent.EXTRA_SUBJECT, note.getTitle());
                    intent.putExtra(Intent.EXTRA_TEXT, note.getText());
                    intent.putExtra("id", note.getNumid());
                    context.startActivity(intent);
                }
            });
        }
    }

}