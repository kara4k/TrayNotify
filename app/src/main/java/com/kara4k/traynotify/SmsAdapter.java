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

public class SMSAdapter extends RecyclerView.Adapter<SMSAdapter.SMSViewHolder>{

    private List<SMS> smsList;

    private static SMSAdapter smsAdapter;

    private List<SMS> getSMS() {
        return smsList;
    }

    private SMSAdapter() {
    }

    public static SMSAdapter getInstance() {
        if (smsAdapter == null) {
            smsAdapter = new SMSAdapter();
        }
        return smsAdapter;
    }

    public void setSmsList(List<SMS> smsList) {
        this.smsList = smsList;
    }

    @Override
    public SMSViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.quick_item, parent, false);
        return new SMSViewHolder(v);
    }

    @Override
    public void onBindViewHolder(SMSViewHolder holder, int position) {
        holder.title.setText(smsList.get(position).getAddress());
        holder.text.setText(smsList.get(position).getBody());
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
        holder.date.setText(dateFormat.format(smsList.get(position).getDate()));
        holder.time.setText(timeFormat.format(new Date(smsList.get(position).getDate())));
        holder.numid.setVisibility(View.INVISIBLE);
    }

    @Override
    public int getItemCount() {
        return smsList.size();
    }

    public class SMSViewHolder extends RecyclerView.ViewHolder {

        private final TextView title;
        private final TextView text;
        private final TextView date;
        private final TextView time;
        private final TextView numid;

        public SMSViewHolder(View itemView) {
            super(itemView);

            title = (TextView) itemView.findViewById(R.id.name);
            text = (TextView) itemView.findViewById(R.id.text);
            date = (TextView) itemView.findViewById(R.id.date);
            time = (TextView) itemView.findViewById(R.id.day_of_week);
            numid = (TextView) itemView.findViewById(R.id.numid);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    try {
                        Context context = view.getContext();
                        SMS sms = SMSAdapter.getInstance().getSMS().get(getAdapterPosition());
                        Intent intent = new Intent(context, QuickNote.class);
                        intent.putExtra(Intent.EXTRA_SUBJECT, sms.getAddress());
                        intent.putExtra(Intent.EXTRA_TEXT, sms.getBody());
                        context.startActivity(intent);
                    } catch (Exception e) {
                    }
                }
            });
        }
    }
}
