package com.kara4k.traynotify;


import android.Manifest;
import android.content.ContentResolver;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;


public class BirthdayFragment extends Fragment {

    private BirthdayAdapter adapter;
    private List<Birthday> birthdaysList;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        RecyclerView recyclerView = (RecyclerView) inflater.inflate(R.layout.quick_notes_fragment, container, false);
        TabLayout tabs = (TabLayout) getActivity().findViewById(R.id.tabs);
        tabs.setVisibility(View.GONE);
        adapter = BirthdayAdapter.getInstance();
        birthdaysList = new ArrayList<>();
        adapter.setBirthdays(birthdaysList);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        new GetInfo().execute();
//        checkReadContactsPermissions();
        return recyclerView;
    }

    void checkReadContactsPermissions() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            int hasReadContactsPermission = ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_CONTACTS);
            if (hasReadContactsPermission == PackageManager.PERMISSION_DENIED) {
                requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, 1);
                return;
            } else {
                getContactsInfo();
            }
        } else {
            getContactsInfo();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                    Toast.makeText(getContext(), "Contacts access denied", Toast.LENGTH_SHORT).show();
                } else {
                    getContactsInfo();
                }
                break;

            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void getContactsInfo() {

        List<Birthday> list = new ArrayList<>();
        ContentResolver cr = getContext().getContentResolver();
        Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
        if (cur.moveToFirst()) {
            do {
                String id = cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID));
                String name = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                ContentResolver bd = getContext().getContentResolver();
                Cursor bdc = bd.query(android.provider.ContactsContract.Data.CONTENT_URI,
                        new String[]{ContactsContract.CommonDataKinds.Event.DATA},
                        android.provider.ContactsContract.Data.CONTACT_ID + " = " + id + " AND " + ContactsContract.Contacts.Data.MIMETYPE + " = '" + ContactsContract.CommonDataKinds.Event.CONTENT_ITEM_TYPE + "' AND " + ContactsContract.CommonDataKinds.Event.TYPE + " = "
                                + ContactsContract.CommonDataKinds.Event.TYPE_BIRTHDAY, null, android.provider.ContactsContract.Data.DISPLAY_NAME);
                if (bdc.moveToFirst()) {
                    String birthday = bdc.getString(0);
                    list.add(new Birthday(id, name, getStringDate(birthday), id, daysLeft(birthday), getAge(birthday), getZodiacSign(birthday), getNotificationTime(birthday)));
                }
            } while (cur.moveToNext());
        }
        cur.close();
        Collections.sort(list);
        birthdaysList = list;
        adapter.setBirthdays(birthdaysList);

    }

    private int getAge(String birthday) {
        Calendar birthdayDate = Calendar.getInstance();
        String[] yearMonthDay = birthday.split("-");
        int year = Integer.parseInt(yearMonthDay[0]);
        int month = Integer.parseInt(yearMonthDay[1]) - 1;
        int day = Integer.parseInt(yearMonthDay[2]);
        birthdayDate.set(year, month, day, 00, 00);
        birthdayDate.set(Calendar.SECOND, 00);
        birthdayDate.set(Calendar.MILLISECOND, 0000);


        Calendar now = Calendar.getInstance();
        now.set(Calendar.HOUR_OF_DAY, 00);
        now.set(Calendar.MINUTE, 00);
        now.set(Calendar.SECOND, 00);
        now.set(Calendar.MILLISECOND, 0000);

        Calendar temp = Calendar.getInstance();
        temp.setTimeInMillis(birthdayDate.getTimeInMillis());
        temp.set(Calendar.YEAR, now.get(Calendar.YEAR));

        int age;

        if (temp.getTimeInMillis() >= now.getTimeInMillis()) {
            age = now.get(Calendar.YEAR) - birthdayDate.get(Calendar.YEAR);
        } else {
            age = ((now.get(Calendar.YEAR) - birthdayDate.get(Calendar.YEAR)) + 1);
        }
        return age;
    }

    private int getZodiacSign(String birthday) {
        Calendar birthdayDate = Calendar.getInstance();
        String[] yearMonthDay = birthday.split("-");
        int month = Integer.parseInt(yearMonthDay[1]);
        int day = Integer.parseInt(yearMonthDay[2]);

        int sign = R.drawable.capricorn;
        switch (month) {

            case 1:
                if (day <= 20) {
                    sign = R.drawable.capricorn;
                } else {
                    sign = R.drawable.aquarius;
                }
                break;
            case 2:
                if (day <= 20) {
                    sign = R.drawable.aquarius;
                } else {
                    sign = R.drawable.pisces;
                }
                break;
            case 3:
                if (day <= 20) {
                    sign = R.drawable.pisces;
                } else {
                    sign = R.drawable.aries;
                }
                break;
            case 4:
                if (day <= 20) {
                    sign = R.drawable.aries;
                } else {
                    sign = R.drawable.taurus;
                }
                break;
            case 5:
                if (day <= 20) {
                    sign = R.drawable.taurus;
                } else {
                    sign = R.drawable.gemini;
                }
                break;
            case 6:
                if (day <= 21) {
                    sign = R.drawable.gemini;
                } else {
                    sign = R.drawable.cancer;
                }
                break;
            case 7:
                if (day <= 22) {
                    sign = R.drawable.cancer;
                } else {
                    sign = R.drawable.leo;
                }
                break;
            case 8:
                if (day <= 23) {
                    sign = R.drawable.leo;
                } else {
                    sign = R.drawable.virgo;
                }
                break;
            case 9:
                if (day <= 23) {
                    sign = R.drawable.virgo;
                } else {
                    sign = R.drawable.libra;
                }
                break;
            case 10:
                if (day <= 23) {
                    sign = R.drawable.libra;
                } else {
                    sign = R.drawable.scorpio;
                }
                break;
            case 11:
                if (day <= 22) {
                    sign = R.drawable.scorpio;
                } else {
                    sign = R.drawable.sagittarius;
                }
                break;
            case 12:
                if (day <= 21) {
                    sign = R.drawable.sagittarius;
                } else {
                    sign = R.drawable.capricorn;
                }
                break;
        }

        return sign;
    }

    private int daysLeft(String birthday) {
        Calendar birthdayDate = Calendar.getInstance();
        String[] yearMonthDay = birthday.split("-");
        int year = Integer.parseInt(yearMonthDay[0]);
        int month = Integer.parseInt(yearMonthDay[1]) - 1;
        int day = Integer.parseInt(yearMonthDay[2]);

        Calendar now = Calendar.getInstance();
        birthdayDate.set(now.get(Calendar.YEAR), month, day, 00, 00);
        birthdayDate.set(Calendar.SECOND, 00);
        birthdayDate.set(Calendar.MILLISECOND, 0000);

        if (today(birthdayDate, now)) {
            return 0;
        }

        if (now.getTimeInMillis() > birthdayDate.getTimeInMillis()) {
            birthdayDate.add(Calendar.YEAR, 1);
        }

        now.set(Calendar.HOUR_OF_DAY, 00);
        now.set(Calendar.MINUTE, 00);
        now.set(Calendar.SECOND, 00);
        now.set(Calendar.MILLISECOND, 0000);
        long difference = birthdayDate.getTimeInMillis() - now.getTimeInMillis();
        long days = difference / (24 * 60 * 60 * 1000);
        return (int) days;
    }

    private boolean today(Calendar birthdayDate, Calendar now) {
        if ((birthdayDate.get(Calendar.MONTH) == now.get(Calendar.MONTH))
                && ((birthdayDate.get(Calendar.DAY_OF_MONTH) == now.get(Calendar.DAY_OF_MONTH)))) {
            return true;
        } else {
            return false;
        }

    }

    private String getStringDate(String birthday) {
        String[] yearMonthDay = birthday.split("-");
        return yearMonthDay[2] + " " + getStringMonth(yearMonthDay[1]) + " " + yearMonthDay[0];

    }

    private String getStringMonth(String month) {
        DateFormatSymbols dfs = DateFormatSymbols.getInstance();
        String[] months = dfs.getMonths();
        return months[Integer.parseInt(month) - 1];

    }

    private long getNotificationTime(String birthday) {
        Calendar birthdayDate = Calendar.getInstance();
        String[] yearMonthDay = birthday.split("-");
        int year = Integer.parseInt(yearMonthDay[0]);
        int month = Integer.parseInt(yearMonthDay[1]) - 1;
        int day = Integer.parseInt(yearMonthDay[2]);
        birthdayDate.set(year, month, day, 00, 00);
        birthdayDate.set(Calendar.SECOND, 00);
        birthdayDate.set(Calendar.MILLISECOND, 0000);

        Calendar now = Calendar.getInstance();
        now.set(Calendar.HOUR_OF_DAY, 00);
        now.set(Calendar.MINUTE, 00);
        now.set(Calendar.SECOND, 00);
        now.set(Calendar.MILLISECOND, 0000);

        birthdayDate.set(Calendar.YEAR, now.get(Calendar.YEAR));

        if (birthdayDate.getTimeInMillis() <= now.getTimeInMillis()) {
            birthdayDate.add(Calendar.YEAR, 1);
        }
        birthdayDate.set(Calendar.HOUR_OF_DAY, 9);

        long time = birthdayDate.getTimeInMillis();
        return time;
    }

    class GetInfo extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                getContactsInfo();
            } catch (Exception e) {
                Log.e("tag", "testoooo");
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            try {
                adapter.notifyDataSetChanged();
            } catch (Exception e) {
                Log.e("TAG", "onPostExecute: shit ");
            }
            super.onPostExecute(aVoid);
        }
    }

}
