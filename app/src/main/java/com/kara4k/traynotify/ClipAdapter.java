package com.kara4k.traynotify;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class ClipAdapter extends RecyclerView.Adapter<ClipAdapter.ClipHolder> {

    private static ClipAdapter clipAdapter;

    private List<Clip> notes;
    private Context context;

    private ClipAdapter() {
    }

    public static ClipAdapter getInstance() {
        if (clipAdapter == null) {
            clipAdapter = new ClipAdapter();
        }
        return clipAdapter;
    }

    public void setNotes(List<Clip> notes) {
        this.notes = notes;
    }

    @Override
    public ClipHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.quick_item, parent, false);
        context = parent.getContext();
        return new ClipHolder(v);
    }

    @Override
    public void onBindViewHolder(ClipHolder holder, int i) {
        holder.title.setVisibility(View.GONE);
        holder.numid.setVisibility(View.GONE);
        holder.text.setText(notes.get(i).getText());
        holder.text.setTextColor(Color.BLACK);

        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, dd.MM.yyyy");
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
        String date = dateFormat.format(new Date(notes.get(i).getDate()));
        date = date.substring(0, 1).toUpperCase() + date.substring(1);
        holder.date.setText(date);
        holder.time.setText(timeFormat.format(new Date(notes.get(i).getDate())));

        holder.tray.setImageResource(R.drawable.ic_content_paste_black_24dp);

        Log.e("ClipAdapter", "onBindViewHolder: " + notes.get(i).getText());
    }

    @Override
    public int getItemCount() {
        return notes.size();
    }

    public class ClipHolder extends RecyclerView.ViewHolder {

        private final TextView title;
        private final TextView text;
        private final TextView date;
        private final TextView time;
        private final TextView numid;
        private final ImageView tray;
        private final CheckBox checkBox;
        private final LinearLayout dateLayout;

        public ClipHolder(View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.name);
            text = (TextView) itemView.findViewById(R.id.text);
            date = (TextView) itemView.findViewById(R.id.date);
            time = (TextView) itemView.findViewById(R.id.day_of_week);
            numid = (TextView) itemView.findViewById(R.id.numid);
            tray = (ImageView) itemView.findViewById(R.id.isShown);
            checkBox = (CheckBox) itemView.findViewById(R.id.checkbox_select);
            dateLayout = (LinearLayout) itemView.findViewById(R.id.date_layout);
        }
    }
}
