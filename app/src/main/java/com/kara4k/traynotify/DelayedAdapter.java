package com.kara4k.traynotify;


import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.widget.RecyclerView;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class DelayedAdapter extends RecyclerView.Adapter<DelayedAdapter.DelayedNotesViewHolder> {

    private static DelayedAdapter quickAdapter;

    private List<DelayedNote> getNotes() {
        return notes;
    }

    private List<DelayedNote> notes;
    private Context context;


    private SparseBooleanArray selectedItems = new SparseBooleanArray();
    private boolean select;
    private SelectionMode selectionMode;
    private int selectedCount = 0;

    private DelayedAdapter() {
    }

    public static DelayedAdapter getInstance() {
        if (quickAdapter == null) {
            quickAdapter = new DelayedAdapter();
        }
        return quickAdapter;
    }

    public void setSelectionMode(SelectionMode selectionMode) {
        this.selectionMode = selectionMode;
    }

    public void setSelect(boolean select) {
        this.select = select;
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

        setRepeatView(notesViewHolder, i);

        notesViewHolder.itemView.setSelected(selectedItems.get(i, false));

        if (select) {
            notesViewHolder.checkBox.setVisibility(View.VISIBLE);
            notesViewHolder.checkBox.setChecked(selectedItems.get(i, false));
        } else {
            notesViewHolder.checkBox.setVisibility(View.GONE);
        }


    }

    private void highlightFinishedNotes(DelayedNotesViewHolder notesViewHolder, int i) {
        Calendar calendar = Calendar.getInstance();
        long now = calendar.getTimeInMillis();
        long setTime = notes.get(i).getSetTime();
        if ((notes.get(i).getRepeat() == 0) && (now > setTime)) {
            notesViewHolder.numid.setTextColor(Color.RED);
        }
    }

    private void setRepeatView(DelayedNotesViewHolder notesViewHolder, int i) {
        if ((notes.get(i).getRepeat() == 0)) {
            setNoRepeat(notesViewHolder, i);
        } else if (notes.get(i).getRepeat() == 1) {
            setRepeatWeekView(notesViewHolder, i);
        } else if (notes.get(i).getRepeat() == 2) {
            setRepeatMonthView(notesViewHolder, i);
        } else if (notes.get(i).getRepeat() == 3) {
            setRepeatYearView(notesViewHolder, i);
        }
    }

    private void setRepeatYearView(DelayedNotesViewHolder notesViewHolder, int i) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("d MMMM");
        String date = dateFormat.format(new Date(notes.get(i).getSetTime()));
        notesViewHolder.date.setText(date);
        hideDaysOfWeekViews(notesViewHolder);
    }

    private void setRepeatMonthView(DelayedNotesViewHolder notesViewHolder, int i) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("d ");
        String date = dateFormat.format(new Date(notes.get(i).getSetTime())).concat(context.getString(R.string.day_of_month));
        notesViewHolder.date.setText(date);
        hideDaysOfWeekViews(notesViewHolder);
    }

    private void setNoRepeat(DelayedNotesViewHolder notesViewHolder, int i) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, dd.MM.yyyy");
        String date = dateFormat.format(new Date(notes.get(i).getSetTime()));
        date = date.substring(0, 1).toUpperCase() + date.substring(1);
        notesViewHolder.date.setText(date);
        notesViewHolder.repeat.setVisibility(View.GONE);
        hideDaysOfWeekViews(notesViewHolder);
    }

    private void hideDaysOfWeekViews(DelayedNotesViewHolder notesViewHolder) {
        for (int k = 0; k < notesViewHolder.days.length; k++) {
            notesViewHolder.days[k].setVisibility(View.GONE);
        }
    }

    private void setRepeatWeekView(DelayedNotesViewHolder notesViewHolder, int i) {
        DateFormatSymbols formatSymbols = DateFormatSymbols.getInstance();
        String[] shortWeekdays = formatSymbols.getShortWeekdays();
        String[] shortDays = new String[]{shortWeekdays[2].substring(0, 1).toUpperCase().concat(shortWeekdays[2].substring(1)).concat(", "),
                shortWeekdays[3].substring(0, 1).toUpperCase().concat(shortWeekdays[3].substring(1)).concat(", "),
                shortWeekdays[4].substring(0, 1).toUpperCase().concat(shortWeekdays[4].substring(1)).concat(", "),
                shortWeekdays[5].substring(0, 1).toUpperCase().concat(shortWeekdays[5].substring(1)).concat(", "),
                shortWeekdays[6].substring(0, 1).toUpperCase().concat(shortWeekdays[6].substring(1)).concat(", "),
                shortWeekdays[7].substring(0, 1).toUpperCase().concat(shortWeekdays[7].substring(1)).concat(", "),
                shortWeekdays[1].substring(0, 1).toUpperCase().concat(shortWeekdays[1].substring(1))
        };

        notesViewHolder.date.setVisibility(View.GONE);

        for (int k = 0; k < notesViewHolder.days.length; k++) {
            notesViewHolder.days[k].setText(shortDays[k]);
            notesViewHolder.days[k].setVisibility(View.VISIBLE);
        }

        String stringDays = notes.get(i).getDays();
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

    public void startSelection() {
        select = true;
        refreshAll();
    }

    public void endSelectionMode() {
        select = false;
        selectedItems = new SparseBooleanArray();
        selectedCount = 0;
        refreshAll();

    }

    private void refreshAll() {
        for (int i = 0; i < notes.size(); i++) {
            notifyItemChanged(i);
        }
    }

    public void selectAll() {
        for (int i = 0; i < notes.size(); i++) {
            selectedItems.put(i, true);
        }
        refreshAll();
        selectedCount = notes.size();
        selectionMode.selectedItemsCount(notes.size());
    }


    public void deleteSelected() {
        try {
            ArrayList<Integer> list = getSelectedId();

            removeFromList(list);
            removeFromDB(list);
            cancelAlarmEvent(list);
            removeFromTray(list);

        } catch (Exception e) {
        }
    }

    private void removeFromTray(ArrayList<Integer> list) {
        NotificationManagerCompat nm = NotificationManagerCompat.from(context);
        for (int x : list) {
            nm.cancel(x);
        }
    }

    private void removeFromList(ArrayList<Integer> list) {
        for (int i = 0; i < notes.size(); i++) {
            for (int x : list) {
                if (notes.get(i).getCheckId() == x) {
                    notes.remove(i);
//                    notifyItemRemoved(i);
                }
            }
        }
        notifyDataSetChanged();
    }

    @NonNull
    private ArrayList<Integer> getSelectedId() {
        ArrayList<Integer> list = new ArrayList<>();
        for (int i = 0; i < selectedItems.size(); i++) {
            if (selectedItems.valueAt(i)) {
                DelayedNote note = notes.get(selectedItems.keyAt(i));
                list.add(note.getCheckId());
            }
        }
        return list;
    }

    private void removeFromDB(ArrayList<Integer> list) {
        DBDelay db = new DBDelay(context);
        db.open();
        for (int x : list) {
            db.removeNote(x);
        }
        db.close();
    }

    private void cancelAlarmEvent(ArrayList<Integer> list) {
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent alarmIntent = new Intent(context, AlarmReceiver.class);
        for (int x : list) {
            PendingIntent pi = PendingIntent.getBroadcast(context, x, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            am.cancel(pi);
        }
    }

    public class DelayedNotesViewHolder extends RecyclerView.ViewHolder {

        private final TextView title;
        private final TextView text;
        private final TextView date;
        private final TextView time;
        private final TextView numid;
        private final TextView[] days;
        private final CheckBox checkBox;
        private final ImageView repeat;


        DelayedNotesViewHolder(final View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.name);
            text = (TextView) itemView.findViewById(R.id.text);
            date = (TextView) itemView.findViewById(R.id.date);
            time = (TextView) itemView.findViewById(R.id.day_of_week);
            numid = (TextView) itemView.findViewById(R.id.numid);
            checkBox = (CheckBox) itemView.findViewById(R.id.checkbox_select_d);
            repeat = (ImageView) itemView.findViewById(R.id.repeat_img);
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
                        if (select) {
                            selectionModeClick(itemView);
                            checkForEndSelect();
                        } else {
                            goToEditActivity(view);
                        }

                    } catch (Exception e) {
                    }
                }
            });

            checkBox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (select) {
                        selectionModeClick(itemView);
                        checkForEndSelect();
                    }
                }
            });

            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    try {
                        startSelection(itemView);
                    } catch (Exception e) {
                        return false;
                    }
                    return true;
                }
            });
        }

        private void startSelection(View itemView) {
            if (selectionMode != null) {
                selectionMode.startSelection(1);
                if (select) {
                    itemView.setSelected(true);
                    selectedItems.put(getAdapterPosition(), true);
                    selectedCount++;
                    notifyItemChanged(getAdapterPosition());
                }
            }
        }

        private void selectionModeClick(View itemView) {
            if (!selectedItems.get(getAdapterPosition(), false)) {
                selectedItems.put(getAdapterPosition(), true);
                itemView.setSelected(true);
                checkBox.setChecked(true);
                selectedCount++;
                selectionMode.selectedItemsCount(selectedCount);
            } else {
                selectedItems.put(getAdapterPosition(), false);
                itemView.setSelected(false);
                checkBox.setChecked(false);
                selectedCount--;
                selectionMode.selectedItemsCount(selectedCount);
            }
        }

        private void checkForEndSelect() {
            if (selectedCount == 0) {
                selectionMode.startSelection(1);
            }
        }

        private void goToEditActivity(View view) {
            Context context = view.getContext();
            DelayedNote note = DelayedAdapter.getInstance().getNotes().get(getAdapterPosition());
            Intent intent = new Intent(context, CreateDelayedNote.class);
            intent.putExtra(Intent.EXTRA_SUBJECT, note.getTitle());
            intent.putExtra(Intent.EXTRA_TEXT, note.getText());
            intent.putExtra("id", note.getCheckId());
            context.startActivity(intent);
        }

    }

}