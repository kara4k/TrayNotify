package com.kara4k.traynotify;


import android.Manifest;
import android.content.ContentResolver;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class SMSFragment extends Fragment {

    public static final String SMS_SORT = "sms_sort";
    public static final int NAME = 0;
    public static final int DATE = 1;

    private SMSAdapter adapter;
    private List<SMS> smsList;
    private List<SMS> smsListAll = new ArrayList<>();
    private Map<Integer, String> names;
    private SharedPreferences sp;

    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        RecyclerView recyclerView = (RecyclerView) inflater.inflate(R.layout.quick_notes_fragment, container, false);
        hideVPTabs();
        sp = PreferenceManager.getDefaultSharedPreferences(getContext());
        adapter = SMSAdapter.getInstance();
        smsList = new ArrayList<>();
        adapter.setSmsList(smsList);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        checkPermissions();



        return recyclerView;
    }


    public void setSortOrder() {
        int sort = sp.getInt(SMS_SORT, 1);
        MainActivity main = (MainActivity) getActivity();
        if (sort == DATE) {
            sortByDate();
            main.mainMenu.findItem(R.id.sort_msg_date).setChecked(true);
        }
        if (sort == NAME) {
            sortByName();
            main.mainMenu.findItem(R.id.sort_msg_title).setChecked(true);
        }
        smsListAll.clear();
        smsListAll.addAll(smsList);

    }

    private void hideVPTabs() {
        TabLayout tabs = (TabLayout) getActivity().findViewById(R.id.tabs);
        tabs.setVisibility(View.GONE);
    }

    private void checkPermissions() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            int hasReadContactsPermission = ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_CONTACTS);
            int hasReadSMSPermission = ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_SMS);
            if ((hasReadContactsPermission == PackageManager.PERMISSION_GRANTED)
                    && (hasReadSMSPermission == PackageManager.PERMISSION_GRANTED)) {
                new GetSmsListTask().execute();
            } else {
                checkReadContactsPermissions();
            }
        } else {
            new GetSmsListTask().execute();
        }
    }

    private void tryGetSMS() {
        try {
            getSMS();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<SMS> getSmsListAll() {
        return smsListAll;
    }

    public void setSmsList(List<SMS> smsList) {
        this.smsList = smsList;
    }

    private void getSMS() {
        String[] reqCols = new String[]{"_id", "person", "address", "body", "date"};
        Cursor cursor = getContext().getContentResolver().query(Uri.parse("content://sms/inbox"), reqCols, null, null, null);
        List<SMS> allSms = new ArrayList<>();
        if (cursor.moveToFirst()) {
            do {
                String name = "";
                try {
                    name = getName(cursor);
                } catch (Exception e) {
                    name = cursor.getString(2);
                }
                allSms.add(new SMS(cursor.getInt(0), cursor.getInt(1), name, cursor.getString(3), cursor.getLong(4)));
            } while (cursor.moveToNext());

        }
        cursor.close();
        smsList = allSms;
//        adapter.setSmsList(smsList);
//        smsListAll.addAll(smsList);


    }

    private String getName(Cursor cursor) {
        String name = "";
        if (cursor.getInt(1) != 0) {
            name = names.get(cursor.getInt(1));
        } else {
            name = cursor.getString(2);
        }
        return name;
    }

    private void getContactNames() {
        ContentResolver cr = getContext().getContentResolver();
        String[] projection = new String[]{ContactsContract.Contacts._ID, ContactsContract.Contacts.DISPLAY_NAME};
        Uri uri = Uri.parse("content://contacts/people");
        Cursor cur = cr.query(uri, projection, null, null, null);
        names = new HashMap<>();
        if (cur.moveToFirst()) {
            do {
                names.put(cur.getInt(0), cur.getString(1));
            } while (cur.moveToNext());

        }
        cur.close();
    }

    private void checkReadContactsPermissions() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            int hasReadContactsPermission = ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_CONTACTS);
            if (hasReadContactsPermission == PackageManager.PERMISSION_DENIED) {
                requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, 1);
                return;
            } else {
                try {
                    getContactNames();
                } catch (Exception e) {

                }
                checkReadSMSPermissions();
            }
        } else {
            try {
                getContactNames();
            } catch (Exception e) {

            }
            checkReadSMSPermissions();
        }
    }

    private void checkReadSMSPermissions() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            int hasReadContactsPermission = ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_SMS);
            if (hasReadContactsPermission == PackageManager.PERMISSION_DENIED) {
                requestPermissions(new String[]{Manifest.permission.READ_SMS}, 2);
                return;
            } else {
                tryGetSMS();
                adapter.notifyDataSetChanged();
            }
        } else {
            tryGetSMS();
            adapter.notifyDataSetChanged();
        }
    }

    public List<SMS> getSmsList() {
        return smsList;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                    Toast.makeText(getContext(), getString(R.string.contacts_access_denied), Toast.LENGTH_SHORT).show();
                    checkReadSMSPermissions();
                } else {
                    try {
                        getContactNames();
                    } catch (Exception e) {

                    }
                    checkReadSMSPermissions();
                }
                break;
            case 2:
                if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                    Toast.makeText(getContext(), R.string.sms_access_denied, Toast.LENGTH_SHORT).show();
                } else {
                   tryGetSMS();
                    adapter.notifyDataSetChanged();
                }

            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    public void sortByName() {
        if ((smsList != null) && (smsList.size() != 0)) {
           Collections.sort(smsList, new Comparator<SMS>() {
               @Override
               public int compare(SMS sms, SMS t1) {
                   return sms.getAddress().compareToIgnoreCase(t1.getAddress());
               }
           });

            SMSAdapter.getInstance().notifyDataSetChanged();

            sp.edit().putInt(SMS_SORT, NAME).apply();

        }
    }
    public void sortByDate() {
        if ((smsList != null) && (smsList.size() != 0)) {
            Collections.sort(smsList, new Comparator<SMS>() {
                @Override
                public int compare(SMS sms, SMS t1) {
                    if (sms.getDate() < t1.getDate())
                        return 1;
                    if (sms.getDate() > t1.getDate())
                        return -1;
                    return 0;
                }
            });

            SMSAdapter.getInstance().notifyDataSetChanged();

            sp.edit().putInt(SMS_SORT, DATE).apply();

        }
    }

    private class GetSmsListTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                getContactNames();
            } catch (Exception e) {

            }
            tryGetSMS();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            try {
                setSortOrder();

                adapter.setSmsList(smsList);
                adapter.notifyDataSetChanged();
                super.onPostExecute(aVoid);
            } catch (Exception e) {

            }
        }
    }
}

