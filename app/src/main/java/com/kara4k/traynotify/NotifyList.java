package com.kara4k.traynotify;


import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Map;

public class NotifyList extends Fragment {

    private String msgData = "";
    private TextView sms;
    String birthdays = "";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ScrollView scrollView = (ScrollView) inflater.inflate(R.layout.test, container, false);
        sms = (TextView) scrollView.findViewById(R.id.sms);
        String[] reqCols = new String[]{"_id", "address", "body", "person", "date"};
        Cursor cursor = getContext().getContentResolver().query(Uri.parse("content://sms/inbox"), reqCols, null, null, null);

        if (cursor.moveToFirst()) {

            for (int i = 0; i < cursor.getColumnCount(); i++) {
                Log.e("Tag", cursor.getColumnName(i));
            }
            do {
                Log.e("taggge", cursor.getString(0) + "\n" +
                        cursor.getString(1) + "\n" +
                        cursor.getString(2) + "\n" +
                        cursor.getInt(3) + "\n" +
                        cursor.getLong(4) + "\n\n");
            } while (cursor.moveToNext());


//            do {
//                msgData += cursor.getInt(0) + "\n" + cursor.getString(1) + "\n" + cursor.getString(2) + "\n\n\n";
//            } while (cursor.moveToNext());

        }

        sms.setText(msgData);
//
//
//        Cursor phones = getContext().getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,null,null, null);
//        while (phones.moveToNext())
//        {
//            String name=phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
//            String phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
//            Log.e("Tag", name + "\n" + phoneNumber + "\n\n"  );
//
//        }
//        phones.close();


//        ContentResolver cr = getContext().getContentResolver();
//        Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
//        if (cur.moveToFirst()) {
//           int count = cur.getColumnCount();
//            Log.e("TAG", String.valueOf(count));
//            for (int i = 0; i < count-1; i++) {
//                Log.e("TAG", cur.getColumnName(i));
//            }
//        }
//        cur.close();

//        new MyTask().execute(); !!!!!!!!!!!!!!!

//        getBirthdays();


        return scrollView;
    }

    private void getBirthdays() {
        ContentResolver cr = getContext().getContentResolver(); //getContnetResolver()
        String[] projection = new String[]{ContactsContract.Contacts._ID, ContactsContract.Contacts.DISPLAY_NAME};

        Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI, projection, null, null,
                ContactsContract.Contacts.DISPLAY_NAME + " COLLATE LOCALIZED ASC");

        while (cur.moveToNext()) {

            Map<String, String> contactInfoMap = new HashMap<String, String>();
            String contactId = cur.getString(cur.getColumnIndex(ContactsContract.Data._ID));
            String displayName = cur.getString(cur.getColumnIndex(ContactsContract.Data.DISPLAY_NAME));

            String columns[] = {
                    ContactsContract.CommonDataKinds.Event.START_DATE,
                    ContactsContract.CommonDataKinds.Event.TYPE,
                    ContactsContract.CommonDataKinds.Event.MIMETYPE,
            };

            String where = ContactsContract.CommonDataKinds.Event.TYPE
                    + "="
                    + ContactsContract.CommonDataKinds.Event.TYPE_BIRTHDAY
                    + " and "
                    + ContactsContract.CommonDataKinds.Event.MIMETYPE
                    + " = '"
                    + ContactsContract.CommonDataKinds.Event.CONTENT_ITEM_TYPE
                    + "' and "
                    + ContactsContract.Data.CONTACT_ID
                    + " = "
                    + contactId;

            String[] selectionArgs = null;
            String sortOrder = ContactsContract.Contacts.DISPLAY_NAME;

            Cursor birthdayCur = cr.query(ContactsContract.Data.CONTENT_URI, columns, where, selectionArgs, sortOrder);
            if (birthdayCur.getCount() > 0) {
                while (birthdayCur.moveToNext()) {
                    String birthday = birthdayCur.getString(birthdayCur.getColumnIndex(ContactsContract.CommonDataKinds.Event.START_DATE));
                    Log.e("TAG", "onCreateView: " + displayName + "\n" + birthday);
                    birthdays += displayName + "\n" + birthday + "\n\n";
                }
            }
            birthdayCur.close();

        }

        cur.close();
    }

    class MyTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            getBirthdays();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            sms.setText(birthdays);
            super.onPostExecute(aVoid);

        }
    }


}
