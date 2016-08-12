package com.kara4k.traynotify;


import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class DelayedAdapter extends RecyclerView.Adapter<DelayedAdapter.DelayedNotesViewHolder> {

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
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.delayed_item, viewGroup, false);
        context = viewGroup.getContext();
        return new DelayedNotesViewHolder(v);
    }

    @Override
    public void onBindViewHolder(DelayedNotesViewHolder notesViewHolder, int i) {
        notesViewHolder.title.setText(notes.get(i).getTitle());
        notesViewHolder.text.setText(notes.get(i).getText());
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");


        notesViewHolder.time.setText(timeFormat.format(new Date(notes.get(i).getSetTime())));
        notesViewHolder.numid.setText("#" + String.valueOf(notes.get(i).getCheckId()));

        highlightFinishedNotes(notesViewHolder, i);

        setRepeatOnceView(notesViewHolder, i);


    }

    private void highlightFinishedNotes(DelayedNotesViewHolder notesViewHolder, int i) {
        Calendar calendar = Calendar.getInstance();
        long now = calendar.getTimeInMillis();
        long setTime = notes.get(i).getSetTime();
        if (((notes.get(i).getRepeat() == 0) || (notes.get(i).getDays().equals("0;0;0;0;0;0;0;")))&&(now > setTime)) {
            notesViewHolder.numid.setTextColor(Color.RED);
        }
    }

    private void setRepeatOnceView(DelayedNotesViewHolder notesViewHolder, int i) {
        if ((notes.get(i).getRepeat() == 0) || (notes.get(i).getDays().equals("0;0;0;0;0;0;0;"))) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, dd.MM.yyyy");
            String date = dateFormat.format(new Date(notes.get(i).getSetTime()));
            date = date.substring(0, 1).toUpperCase() + date.substring(1);
            notesViewHolder.date.setText(date);

            for (int k = 0; k < notesViewHolder.days.length; k++) {
                notesViewHolder.days[k].setVisibility(View.INVISIBLE);
            }

        } else {
            notesViewHolder.date.setText("");
            notesViewHolder.date.setVisibility(View.INVISIBLE);
            setRepeatDaysView(notesViewHolder, i);
        }
    }

    private void setRepeatDaysView(DelayedNotesViewHolder notesViewHolder, int i) {
        DateFormatSymbols formatSymbols = DateFormatSymbols.getInstance();
        String[] shortWeekdays = formatSymbols.getShortWeekdays();
        String[] shortDays = new String[] {shortWeekdays[2].substring(0,1).toUpperCase().concat(shortWeekdays[2].substring(1)).concat(", "),
                shortWeekdays[3].substring(0,1).toUpperCase().concat(shortWeekdays[3].substring(1)).concat(", "),
                shortWeekdays[4].substring(0,1).toUpperCase().concat(shortWeekdays[4].substring(1)).concat(", "),
                shortWeekdays[5].substring(0,1).toUpperCase().concat(shortWeekdays[5].substring(1)).concat(", "),
                shortWeekdays[6].substring(0,1).toUpperCase().concat(shortWeekdays[6].substring(1)).concat(", "),
                shortWeekdays[7].substring(0,1).toUpperCase().concat(shortWeekdays[7].substring(1)).concat(", "),
                shortWeekdays[1].substring(0,1).toUpperCase().concat(shortWeekdays[1].substring(1))
        };

        for (int k = 0; k < notesViewHolder.days.length; k++) {
            notesViewHolder.days[k].setText(shortDays[k]);
        }

        String stringDays = notes.get(i).getDays();
        Log.e("tagddd", stringDays);
        String[] split = stringDays.split(";");
        for (int k = 0; k < 5; k++) {
            if (split[k].equals("1")) {
                notesViewHolder.days[k].setTextColor(Color.BLACK);
            }
        }
        if (split[5].equals("1")) {
            notesViewHolder.days[5].setTextColor(Color.RED);
        }
        if (split[6].equals("1")) {
            notesViewHolder.days[6].setTextColor(Color.RED);
        }
    }


    @Override
    public int getItemCount() {
        return notes.size();
    }

    public void remove(int position) {
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent alarmIntent = new Intent(context, AlarmReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(context, notes.get(position).getCheckId(), alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        am.cancel(pi);
        DBDelay db = new DBDelay(context);
        db.open();
        int id = notes.get(position).getCheckId();
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
        private final TextView[] days;


        DelayedNotesViewHolder(final View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.title);
            text = (TextView) itemView.findViewById(R.id.text);
            date = (TextView) itemView.findViewById(R.id.date);
            time = (TextView) itemView.findViewById(R.id.time);
            numid = (TextView) itemView.findViewById(R.id.numid);
            TextView mon = (TextView) itemView.findViewById(R.id.mon);
            TextView tue = (TextView) itemView.findViewById(R.id.tue);
            TextView wed = (TextView) itemView.findViewById(R.id.wed);
            TextView thu = (TextView) itemView.findViewById(R.id.thu);
            TextView fri = (TextView) itemView.findViewById(R.id.fri);
            TextView sat = (TextView) itemView.findViewById(R.id.sat);
            TextView sun = (TextView) itemView.findViewById(R.id.sun);
            days = new TextView[]{mon, tue, wed, thu, fri, sat, sun};
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
//            itemView.setOnLongClickListener(new View.OnLongClickListener() {
//                @Override
//                public boolean onLongClick(View view) {
//                    try {
//                        DelayedAdapter.getInstance().remove(getAdapterPosition());
//                    } catch (Exception e) {
//                        e.printStackTrace(); //
//                    }
//                    return false;
//                }
//            });
        }

    }

}