package com.kara4k.traynotify;


import android.Manifest;
import android.content.ContentResolver;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class SMSFragment extends Fragment {

    SMSAdapter adapter;
    List<SMS> smsList;
    private Map<Integer, String> names;

    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        RecyclerView recyclerView = (RecyclerView) inflater.inflate(R.layout.quick_notes_fragment, container, false);

        adapter = SMSAdapter.getInstance();
        smsList = new ArrayList<>();
        adapter.setSmsList(smsList);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        checkPermissions();
        return recyclerView;
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

    public void getSMS() {
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
        adapter.setSmsList(smsList);


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

    public void getContactNames() {
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

    void checkReadContactsPermissions() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            int hasReadContactsPermission = ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_CONTACTS);
            if (hasReadContactsPermission == PackageManager.PERMISSION_DENIED) {
                requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, 1);
                return;
            } else {
                getContactNames();
                checkReadSMSPermissions();
            }
        } else {
            getContactNames();
            checkReadSMSPermissions();
        }
    }

    void checkReadSMSPermissions() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            int hasReadContactsPermission = ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_SMS);
            if (hasReadContactsPermission == PackageManager.PERMISSION_DENIED) {
                requestPermissions(new String[]{Manifest.permission.READ_SMS}, 2);
                return;
            } else {
                getSMS();
                adapter.notifyDataSetChanged();
            }
        } else {
            getSMS();
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                    Toast.makeText(getContext(), "Contacts access denied", Toast.LENGTH_SHORT).show();
                    checkReadSMSPermissions();
                } else {
                    getContactNames();
                    checkReadSMSPermissions();
                }
                break;
            case 2:
                if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                    Toast.makeText(getContext(), "SMS access denied", Toast.LENGTH_SHORT).show();
                } else {
                    getSMS();
                    adapter.notifyDataSetChanged();
                }

            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    class GetSmsListTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            getContactNames();
            getSMS();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            try {
                adapter.setSmsList(smsList);
                adapter.notifyDataSetChanged();
                super.onPostExecute(aVoid);
            } catch (Exception e) {

            }
        }
    }
}

