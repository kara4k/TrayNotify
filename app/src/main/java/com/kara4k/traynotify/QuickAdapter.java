package com.kara4k.traynotify;


import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

public class QuickAdapter extends RecyclerView.Adapter<QuickAdapter.EventsViewHolder> {

    private static QuickAdapter quickAdapter;
    private List<Note> notes;

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
        return new EventsViewHolder(v);
    }

    @Override
    public void onBindViewHolder(EventsViewHolder eventsViewHolder, int i) {
        eventsViewHolder.title.setText(notes.get(i).getTitle());
        eventsViewHolder.text.setText(notes.get(i).getText());

    }

    @Override
    public int getItemCount() {
        return notes.size();
    }

    public static class EventsViewHolder extends RecyclerView.ViewHolder {

        final TextView title;
        final TextView text;

        EventsViewHolder(final View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.title);
            text = (TextView) itemView.findViewById(R.id.text);
        }
    }

}