package com.kara4k.traynotify;


import android.content.Context;
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


    BirthdayAdapter() {
    }

    public static BirthdayAdapter getInstance() {
        if (birthdayAdapter == null) {
            birthdayAdapter = new BirthdayAdapter();
        }
        return birthdayAdapter;
    }

    public List<Birthday> getBirthdays() {
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
        holder.ageView.setText("Age: " + birthdays.get(position).getAge());
    }

    private void setDaysLeft(BirthdayViewHolder holder, int position) {
        if (birthdays.get(position).getDaysLeft() == 0) {
            holder.daysLeftView.setText("Today");
            holder.daysLeftView.setTextColor(Color.RED);
        } else {
            holder.daysLeftView.setText("Left: " + birthdays.get(position).getDaysLeft() + " days");
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

        private ImageView photoView;
        private TextView nameView;
        private TextView dateView;
        private TextView daysLeftView;
        private TextView ageView;
        private ImageView zodiac;
        private TextView dayOfWeek;


        public BirthdayViewHolder(View itemView) {
            super(itemView);
            photoView = (ImageView) itemView.findViewById(R.id.photo);
            nameView = (TextView) itemView.findViewById(R.id.name);
            dateView = (TextView) itemView.findViewById(R.id.date);
            daysLeftView = (TextView) itemView.findViewById(R.id.days_left);
            ageView = (TextView) itemView.findViewById(R.id.age);
            zodiac = (ImageView) itemView.findViewById(R.id.sign);
            dayOfWeek = (TextView) itemView.findViewById(R.id.day_of_week);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                }
            });

        }
    }
}
