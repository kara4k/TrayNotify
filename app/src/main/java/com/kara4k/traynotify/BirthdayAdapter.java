package com.kara4k.traynotify;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class BirthdayAdapter extends RecyclerView.Adapter<BirthdayAdapter.BirthdayViewHolder> {

    private static BirthdayAdapter birthdayAdapter;
    private Context context;
    private List<Birthday> birthdays;


    private SparseBooleanArray selectedItems = new SparseBooleanArray();
    private boolean select;
    private SelectionMode selectionMode;
    private int selectedCount = 0;


    private BirthdayAdapter() {
    }

    public static BirthdayAdapter getInstance() {
        if (birthdayAdapter == null) {
            birthdayAdapter = new BirthdayAdapter();
        }
        return birthdayAdapter;
    }

    public void setSelectionMode(SelectionMode selectionMode) {
        this.selectionMode = selectionMode;
    }

    private List<Birthday> getBirthdays() {
        return birthdays;
    }

    public void setBirthdays(List<Birthday> birthdays) {
        this.birthdays = birthdays;
    }

    @Override
    public BirthdayViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.birthday_item, parent, false);
        context = parent.getContext();
        return new BirthdayViewHolder(v);
    }

    @Override
    public void onBindViewHolder(BirthdayViewHolder holder, int position) {
        holder.nameView.setText(birthdays.get(position).getName());

        setBirthdayDate(holder, position);

        setPhoto(holder.photoView, position);
        setDaysLeft(holder, position);
        holder.ageView.setText(context.getString(R.string.age) + birthdays.get(position).getAge());
        setZodiacMonthSign(holder, position);
        setZodiacYearSign(holder, position);

        holder.itemView.setSelected(selectedItems.get(position, false));

        if (select) {
            holder.checkBox.setVisibility(View.VISIBLE);
            holder.checkBox.setChecked(selectedItems.get(position, false));
        } else {
            holder.checkBox.setVisibility(View.GONE);
        }


    }

    private void setBirthdayDate(BirthdayViewHolder holder, int position) {
        try {
            String date[] = birthdays.get(position).getDate().split(" ");
            if (Integer.parseInt(date[2]) == 0) {
                holder.dateView.setText(date[0] + " " + date[1]);
                holder.ageView.setVisibility(View.INVISIBLE);
            } else {
                holder.ageView.setVisibility(View.VISIBLE);
                holder.dateView.setText(birthdays.get(position).getDate());
            }
        } catch (NumberFormatException e) {
            holder.ageView.setVisibility(View.VISIBLE);
            holder.dateView.setText(birthdays.get(position).getDate());
        }
    }

    private void setZodiacYearSign(BirthdayViewHolder holder, int position) {
        try {
            holder.zodiacYear.setImageResource(BirthdayFragment.getYaarSign(birthdays.get(position).getDate()));
        } catch (Exception e) {
        }
    }

    private void setZodiacMonthSign(BirthdayViewHolder holder, int position) {
        try {
            holder.zodiacMonth.setImageResource(birthdays.get(position).getSign());
        } catch (Exception e) {

        }
    }

    private void setDaysLeft(BirthdayViewHolder holder, int position) {
        if (birthdays.get(position).getDaysLeft() == 0) {
            holder.daysLeftView.setText(R.string.today);
            holder.daysLeftView.setTextColor(Color.RED);
        } else {
            holder.daysLeftView.setText(context.getString(R.string.left) + birthdays.get(position).getDaysLeft() + context.getString(R.string.days));
            holder.daysLeftView.setTextColor(Color.BLACK);
        }
    }

    private void setPhoto(ImageView imageView, int position) {
        try {
            Bitmap mBitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), Uri.parse(birthdays.get(position).getPhotoUri()));
            imageView.setImageBitmap(mBitmap);
        } catch (IOException e) {
            imageView.setImageResource(R.drawable.unnamed);

        }
    }


    public void startSelection() {
        select = true;
        refreshAll();
    }

    private void refreshAll() {
        for (int i = 0; i < birthdays.size(); i++) {
            notifyItemChanged(i);
        }
    }

    public void deleteSelected() {
        try {
            ArrayList<String> list = getSelectedId();
            removeFromList(list);
            removeFromDB(list);
        } catch (Exception e) {
        }
    }

    private void removeFromDB(ArrayList<String> list) {
        try {
            DBBirthday db = new DBBirthday(context);
            db.open();
            for (String x : list) {
                db.remove(x);
            }
            db.close();
        } catch (Exception e) {
        }
    }

    private void removeFromList(ArrayList<String> list) {
        for (int i = 0; i < birthdays.size(); i++) {
            for (String x : list) {
                if (birthdays.get(i).getId().equals(x)) {
                    birthdays.remove(i);
                }
            }
        }
        notifyDataSetChanged();
    }

    private ArrayList<String> getSelectedId() {
        ArrayList<String> list = new ArrayList<>();
        for (int i = 0; i < selectedItems.size(); i++) {
            if (selectedItems.valueAt(i)) {
                Birthday birthday = birthdays.get(selectedItems.keyAt(i));
                list.add(birthday.getId());
            }
        }
        return list;
    }

    private ArrayList<Birthday> getSelected() {
        ArrayList<Birthday> list = new ArrayList<>();
        for (int i = 0; i < selectedItems.size(); i++) {
            if (selectedItems.valueAt(i)) {
                Birthday birthday = birthdays.get(selectedItems.keyAt(i));
                list.add(birthday);
            }
        }
        return list;
    }

    public void selectAll() {
        for (int i = 0; i < birthdays.size(); i++) {
            selectedItems.put(i, true);
        }
        refreshAll();
        selectedCount = birthdays.size();
        selectionMode.selectedItemsCount(birthdays.size());
    }

    public void endSelectionMode() {
        select = false;
        selectedItems = new SparseBooleanArray();
        selectedCount = 0;
        refreshAll();

    }

    public void setSelectedAlarms() {
        DBDelay db = new DBDelay(context);
        ArrayList<Birthday> list = getSelected();
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        for (Birthday x : list) {
            int id = db.getNoteCheckID();
                DelayedNote note = new DelayedNote();
                note.setCheckId(id);
                note.setText(getAlarmText(x));
                note.setTitle(context.getString(R.string.birthday_title));
                note.setCreateTime(getCurrentTime());
                note.setRepeat(3);
                note.setDays("0;0;0;0;0;0;0;");

                note.setSetTime(getSetBirthdayTime(sp, x));

                String sound = sp.getString(Settings.BIRTHDAY_SOUND, "0");
                note.setSound(sound);

                String vibration = sp.getString(Settings.BIRTHDAY_VIBRATION, "1");
                note.setVibration(vibration);

                boolean importnat = sp.getBoolean(Settings.BIRTHDAY_IMPORTANT, false);
                if (importnat) {
                    note.setPriority(2);
                } else {
                    note.setPriority(0);
                }

                note.setBirthday(Integer.parseInt(x.getId()));
                db.addNote(note);
        }


    }

    private long getSetBirthdayTime(SharedPreferences sp, Birthday birthday) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(birthday.getSetTime());
        int day = Integer.parseInt(sp.getString(Settings.BIRTHDAY_DAY, "0"));
        calendar.add(Calendar.DAY_OF_MONTH, day);
        String time = sp.getString(Settings.BIRTHDAY_TIME, "09:00");
        String hourMinute[] = time.split(":");
        int hour = Integer.parseInt(hourMinute[0]);
        int minute = Integer.parseInt(hourMinute[1]);
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        return calendar.getTimeInMillis();
    }

    private long getCurrentTime() {
        return Calendar.getInstance().getTimeInMillis();
    }

    private String getAlarmText (Birthday birthday) {
        String bDate[] = birthday.getDate().split(" ");
        String msgText = "";
        if (Integer.parseInt(bDate[2]) == 0) {
             msgText = birthday.getName() + ",\n" + bDate[0].concat(" ").concat(bDate[1]).concat(".");
        } else {
             msgText = birthday.getName() + ",\n" + birthday.getDate() + ", " + birthday.getAge() + context.getString(R.string.years);

        }
        return msgText;
    }


    @Override
    public int getItemCount() {
        return birthdays.size();
    }

    public class BirthdayViewHolder extends RecyclerView.ViewHolder {

        private final ImageView photoView;
        private final TextView nameView;
        private final TextView dateView;
        private final TextView daysLeftView;
        private final TextView ageView;
        private final ImageView zodiacMonth;
        private final ImageView zodiacYear;
        private final CheckBox checkBox;


        public BirthdayViewHolder(final View itemView) {
            super(itemView);
            photoView = (ImageView) itemView.findViewById(R.id.photo);
            nameView = (TextView) itemView.findViewById(R.id.name);
            dateView = (TextView) itemView.findViewById(R.id.date);
            daysLeftView = (TextView) itemView.findViewById(R.id.days_left);
            ageView = (TextView) itemView.findViewById(R.id.age);
            zodiacMonth = (ImageView) itemView.findViewById(R.id.sign_month);
            zodiacYear = (ImageView) itemView.findViewById(R.id.sign_year);
            checkBox = (CheckBox) itemView.findViewById(R.id.check_box_birthday);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (select) {
                        toggleSelectItem();
                    } else {

                        Intent delayed = new Intent(context, CreateDelayedNote.class);
                        delayed.putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.birthday_title));
                        String name = BirthdayAdapter.getInstance().getBirthdays().get(getAdapterPosition()).getName();
                        int age = BirthdayAdapter.getInstance().getBirthdays().get(getAdapterPosition()).getAge();
                        String date = BirthdayAdapter.getInstance().getBirthdays().get(getAdapterPosition()).getDate();
                        long setTimer = BirthdayAdapter.getInstance().getBirthdays().get(getAdapterPosition()).getSetTime();
                        int id = Integer.parseInt(BirthdayAdapter.getInstance().getBirthdays().get(getAdapterPosition()).getId());
                        delayed.putExtra(Intent.EXTRA_TEXT, getMsgText(name, age, date));
                        delayed.putExtra("time", setTimer);
                        delayed.putExtra("birthday", id);
                        context.startActivity(delayed);
                    }
                }
            });

            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    if (selectionMode != null) {
                        selectionMode.startSelection(1);
                        if (select) {
                            itemView.setSelected(true);
                            selectedItems.put(getAdapterPosition(), true);
                            selectedCount++;
                            notifyItemChanged(getAdapterPosition());
                        }
                    }


                    return true;
                }
            });

        }

        private void toggleSelectItem() {
            selectionModeClick(itemView);
            checkForEndSelect();
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

        @NonNull
        public String getMsgText(String name, int age, String date) {
            String bDate[] = birthdays.get(getAdapterPosition()).getDate().split(" ");
            String msgText = name + ",\n" + date + ", " + age + context.getString(R.string.years);
            if (Integer.parseInt(bDate[2]) == 0) {
                msgText = name + ",\n" + bDate[0].concat(" ").concat(bDate[1]).concat(".");
            }
            return msgText;
        }
    }
}
