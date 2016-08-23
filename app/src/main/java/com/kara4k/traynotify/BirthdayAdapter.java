package com.kara4k.traynotify;


import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;
import java.util.List;

public class BirthdayAdapter extends RecyclerView.Adapter<BirthdayAdapter.BirthdayViewHolder> {

    private static BirthdayAdapter birthdayAdapter;
    private Context context;
    private List<Birthday> birthdays;


    private BirthdayAdapter() {
    }

    public static BirthdayAdapter getInstance() {
        if (birthdayAdapter == null) {
            birthdayAdapter = new BirthdayAdapter();
        }
        return birthdayAdapter;
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
        holder.dateView.setText(birthdays.get(position).getDate());
        setPhoto(holder.photoView, position);
        setDaysLeft(holder, position);
        holder.ageView.setText(context.getString(R.string.age) + birthdays.get(position).getAge());
        setZodiacSign(holder, position);
    }

    private void setZodiacSign(BirthdayViewHolder holder, int position) {
        try {
            holder.zodiac.setImageResource(birthdays.get(position).getSign());
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
            imageView.setImageResource(R.drawable.user1);

        }
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
        private final ImageView zodiac;


        public BirthdayViewHolder(final View itemView) {
            super(itemView);
            photoView = (ImageView) itemView.findViewById(R.id.photo);
            nameView = (TextView) itemView.findViewById(R.id.name);
            dateView = (TextView) itemView.findViewById(R.id.date);
            daysLeftView = (TextView) itemView.findViewById(R.id.days_left);
            ageView = (TextView) itemView.findViewById(R.id.age);
            zodiac = (ImageView) itemView.findViewById(R.id.sign);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent delayed = new Intent(context, CreateDelayedNote.class);
                    delayed.putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.birthday_title));
                    String name = BirthdayAdapter.getInstance().getBirthdays().get(getAdapterPosition()).getName();
                    int age = BirthdayAdapter.getInstance().getBirthdays().get(getAdapterPosition()).getAge();
                    String date = BirthdayAdapter.getInstance().getBirthdays().get(getAdapterPosition()).getDate();
                    long setTimer = BirthdayAdapter.getInstance().getBirthdays().get(getAdapterPosition()).getSetTime();
                    int id = Integer.parseInt(BirthdayAdapter.getInstance().getBirthdays().get(getAdapterPosition()).getId());


                    delayed.putExtra(Intent.EXTRA_TEXT, name + ",\n" + date +",\n" +  age + context.getString(R.string.years));
                    delayed.putExtra("time", setTimer);
                    delayed.putExtra("birthday", id);
                    context.startActivity(delayed);
                }
            });

        }
    }
}
