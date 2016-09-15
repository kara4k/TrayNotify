package com.kara4k.traynotify;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
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
    protected ClipboardManager cm;

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
        cm = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        return new ClipHolder(v);
    }

    @Override
    public void onBindViewHolder(ClipHolder holder, int i) {
        holder.title.setVisibility(View.GONE);
        holder.numid.setVisibility(View.GONE);
        holder.text.setText(notes.get(i).getText());

        setDateTime(holder, i);
        IfInClipNowIcon(holder, i);



    }

    private void IfInClipNowIcon(ClipHolder holder, int i) {
        String textInClipNow = cm.getPrimaryClip().getItemAt(0).getText().toString();
        if (textInClipNow.equals(notes.get(i).getText())) {
            holder.inClip.setImageResource(R.drawable.ic_content_paste_red_24dp);
            notes.get(i).setChecked(1);
        } else {
            holder.inClip.setImageResource(R.drawable.ic_content_paste_grey_24dp);
            notes.get(i).setChecked(0);
        }
    }

    private void setDateTime(ClipHolder holder, int i) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, dd.MM.yyyy");
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
        String date = dateFormat.format(new Date(notes.get(i).getDate()));
        date = date.substring(0, 1).toUpperCase() + date.substring(1);
        holder.date.setText(date);
        holder.time.setText(timeFormat.format(new Date(notes.get(i).getDate())));
    }

    @Override
    public int getItemCount() {
        return notes.size();
    }

    public void refreshAll() {
        for (int i = 0; i < notes.size(); i++) {
            notifyItemChanged(i);
        }
    }

    public void clearListAll() {
        try {
            DBClip db = new DBClip(context);
            db.open();
            db.clearDB();
            db.close();
            notes.clear();
            notifyDataSetChanged();
        } catch (Exception e) {
        }
    }

    public class ClipHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

        private final TextView title;
        private final TextView text;
        private final TextView date;
        private final TextView time;
        private final TextView numid;
        private final ImageView inClip;
        private final CheckBox checkBox;
        private final LinearLayout dateLayout;

        public ClipHolder(View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.name);
            text = (TextView) itemView.findViewById(R.id.text);
            date = (TextView) itemView.findViewById(R.id.date);
            time = (TextView) itemView.findViewById(R.id.day_of_week);
            numid = (TextView) itemView.findViewById(R.id.numid);
            inClip = (ImageView) itemView.findViewById(R.id.isShown);
            checkBox = (CheckBox) itemView.findViewById(R.id.checkbox_select);
            dateLayout = (LinearLayout) itemView.findViewById(R.id.date_layout);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
            dateLayout.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.quick_holder:
                    Intent quick = new Intent(context, QuickNote.class);
                    quick.putExtra(Intent.EXTRA_TEXT, notes.get(getAdapterPosition()).getText());
                    context.startActivity(quick);
                    break;
                case R.id.date_layout:
                    inClip.setImageResource(R.drawable.ic_content_paste_red_24dp);
                    putToClipboard();
                    changeList();
                    break;
            }

        }

        private void changeList() {
            for (Clip x :notes) {
                if (x.getChecked() != 0) {
                    x.setChecked(0);
                    notifyItemChanged(notes.indexOf(x));
                }
            }
            notes.get(getAdapterPosition()).setChecked(1);
        }

        private void changeDatabase() {
            DBClip db = new DBClip(context);
            db.clearAndCheckSingle(notes.get(getAdapterPosition()).getNumId(), 1);
        }

        private void putToClipboard() {

            ClipData clip = ClipData.newPlainText("", notes.get(getAdapterPosition()).getText());
            cm.setPrimaryClip(clip);
        }

        @Override
        public boolean onLongClick(View view) {
            switch (view.getId()) {
                case R.id.quick_holder:
                    DBClip db = new DBClip(context);
                    db.open();
                    db.removeClip(notes.get(getAdapterPosition()).getNumId());
                    db.close();
                    notes.remove(getAdapterPosition());
                    notifyItemRemoved(getAdapterPosition());
                    break;
            }
            return true;
        }
    }
}
