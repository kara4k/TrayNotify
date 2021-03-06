package com.kara4k.traynotify;


import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


public class BirthdayFragment extends Fragment {

    public static final String BIRTHDAY_SORT = "birthday_sort";

    public static final int DAYS = 0;
    public static final int NAME = 1;
    public static final int AGE = 2;

    private BirthdayAdapter adapter;
    private List<Birthday> birthdaysList;
    private List<Birthday> birthdaysListAll;
    private SharedPreferences sp;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        RecyclerView recyclerView = (RecyclerView) inflater.inflate(R.layout.quick_notes_fragment, container, false);
        hideVPTabs();
        sp = PreferenceManager.getDefaultSharedPreferences(getContext());
        adapter = BirthdayAdapter.getInstance();
        birthdaysList = new ArrayList<>();
        birthdaysListAll = new ArrayList<>();
        adapter.setBirthdays(birthdaysList);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        SelectionMode selectionMode = (SelectionMode) getActivity();
        adapter.setSelectionMode(selectionMode);
        setSortedListFromMDB();


        return recyclerView;
    }

    public void setSortOrder() {
        int sort = sp.getInt(BIRTHDAY_SORT, DAYS);
        MainActivity main = (MainActivity) getActivity();
        if (sort == DAYS) {
            sortByDaysLeft();
            main.mainMenu.findItem(R.id.sortDaysLeft).setChecked(true);
        }
        if (sort == NAME) {
            sortByNames();
            main.mainMenu.findItem(R.id.sortNames).setChecked(true);
        }
        if (sort == AGE) {
            sortByAge();
            main.mainMenu.findItem(R.id.sortAge).setChecked(true);
        }

    }

    private void setSortedListFromMDB() {
        List<Birthday> list = getBirthdaysFromMDB();
//        birthdaysList = list;
        birthdaysList.clear();
        birthdaysList.addAll(list);
        setSortOrder();
        adapter.setBirthdays(birthdaysList);
        adapter.notifyDataSetChanged();
        birthdaysListAll.clear();
        birthdaysListAll.addAll(birthdaysList);
    }

    @NonNull
    private List<Birthday> getBirthdaysFromMDB() {
        try {
            DBBirthday db = new DBBirthday(getContext());
            db.open();
            Cursor allData = db.getAllData();
            List<Birthday> list = new ArrayList<>();
            if (allData.moveToFirst()) {
                do {
                    String id = allData.getString(1);
                    String name = allData.getString(2);
                    String birthday = allData.getString(3);
                    list.add(new Birthday(id, name, getStringDate(birthday), id, daysLeft(birthday), getAge(birthday), getZodiacSign(birthday), getNotificationTime(birthday)));
                } while (allData.moveToNext());
            }

            allData.close();
            db.close();
            return list;
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }


    private void hideVPTabs() {
        TabLayout tabs = (TabLayout) getActivity().findViewById(R.id.tabs);
        tabs.setVisibility(View.GONE);
    }

    public void checkReadContactsPermissions() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            int hasReadContactsPermission = ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_CONTACTS);
            if (hasReadContactsPermission == PackageManager.PERMISSION_DENIED) {
                requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, 1);
                return;
            } else {
                new GetInfo(getContext()).execute();
            }
        } else {
            new GetInfo(getContext()).execute();
        }
    }

    private void tryGetContactsInfo() {
        try {
            getContactsInfo();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                    Toast.makeText(getContext(), R.string.contacts_access_denied, Toast.LENGTH_SHORT).show();
                } else {
                    new GetInfo(getContext()).execute();
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
                    list.add(new Birthday(id, name, birthday));
                }
                bdc.close();
            } while (cur.moveToNext());
        }
        cur.close();

        writeBirthdaysToMDB(list);
//        setSortedListFromMDB();
//        setSortOrder();

    }

    private void writeBirthdaysToMDB(List<Birthday> list) {
        try {
            if (list.size() > 0) {
                DBBirthday db = new DBBirthday(getContext());
                db.open();
                db.clearDB();
                for (Birthday x : list) {
                    db.addNote(x.getId(), x.getName(), x.getDate());
                }
                db.close();
            }
        } catch (Exception e) {
        }
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

    public static int getYaarSign(String date) {
        String[] dateArray = date.split(" ");
        int year = Integer.parseInt(dateArray[2]);
        int sign = 0;
        switch (year % 12) {
            case 4:
                sign = R.drawable.rat;
                break;
            case 5:
                sign = R.drawable.bull;
                break;
            case 6:
                sign = R.drawable.tiger;
                break;
            case 7:
                sign = R.drawable.rabbit;
                break;
            case 8:
                sign = R.drawable.dragon;
                break;
            case 9:
                sign = R.drawable.snake;
                break;
            case 10:
                sign = R.drawable.horse;
                break;
            case 11:
                sign = R.drawable.sheep;
                break;
            case 0:
                sign = R.drawable.monkey;
                break;
            case 1:
                sign = R.drawable.chicken;
                break;
            case 2:
                sign = R.drawable.dog;
                break;
            case 3:
                sign = R.drawable.pig;
                break;
        }
        return sign;
    }


    public static int getZodiacSign(String birthday) {
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
        return (birthdayDate.get(Calendar.MONTH) == now.get(Calendar.MONTH))
                && ((birthdayDate.get(Calendar.DAY_OF_MONTH) == now.get(Calendar.DAY_OF_MONTH)));

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

        return birthdayDate.getTimeInMillis();
    }

    public void sortByAge() {
        trySortByAge();
        adapter.notifyDataSetChanged();
        sp.edit().putInt(BIRTHDAY_SORT, AGE).apply();
    }

    private void trySortByAge() {
        if ((birthdaysList != null) && (birthdaysList.size() != 0)) {
            try {
                Collections.sort(birthdaysList, new Comparator<Birthday>() {
                    @Override
                    public int compare(Birthday birthday, Birthday t1) {
                        if (birthday.getAge() > t1.getAge())
                            return 1;
                        if (birthday.getAge() < t1.getAge())
                            return -1;
                        return 0;
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void sortByDaysLeft() {
        trySortByDaysLeft();
        adapter.notifyDataSetChanged();
        sp.edit().putInt(BIRTHDAY_SORT, DAYS).apply();
    }

    private void trySortByDaysLeft() {
        if ((birthdaysList != null) && (birthdaysList.size() != 0)) {
            try {
                Collections.sort(birthdaysList, new Comparator<Birthday>() {
                    @Override
                    public int compare(Birthday birthday, Birthday t1) {
                        if (birthday.getDaysLeft() > t1.getDaysLeft())
                            return 1;
                        if (birthday.getDaysLeft() < t1.getDaysLeft())
                            return -1;
                        return 0;
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void sortByNames() {
        trySortByNames();
        adapter.notifyDataSetChanged();
        sp.edit().putInt(BIRTHDAY_SORT, NAME).apply();
    }

    private void trySortByNames() {
        if ((birthdaysList != null) && (birthdaysList.size() != 0)) {
            try {
                Collections.sort(birthdaysList, new Comparator<Birthday>() {
                    @Override
                    public int compare(Birthday birthday, Birthday t1) {
                        return birthday.getName().compareToIgnoreCase(t1.getName());
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public List<Birthday> getBirthdaysList() {
        return birthdaysList;
    }

    public void setBirthdaysList(List<Birthday> birthdaysList) {
        this.birthdaysList = birthdaysList;
    }

    public List<Birthday> getBirthdaysListAll() {
        return birthdaysListAll;
    }

    private class GetInfo extends AsyncTask<Void, Void, Void> {

        private final ProgressDialog dialog;

        GetInfo(Context context) {
            dialog = new ProgressDialog(context);
            dialog.setMessage(context.getString(R.string.loading));
        }

        @Override
        protected void onPreExecute() {
            dialog.show();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                tryGetContactsInfo();
            } catch (Exception e) {
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            try {
                setSortedListFromMDB();
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
            } catch (Exception e) {
            }
            super.onPostExecute(aVoid);
        }
    }

}
