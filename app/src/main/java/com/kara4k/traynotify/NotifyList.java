package com.kara4k.traynotify;


import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class NotifyList extends Fragment {

    private String msgData = "";
    private TextView sms;
    String birthdays = "";
    private ImageView photoView;
    private Bitmap photo;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
//        ScrollView scrollView = (ScrollView) inflater.inflate(R.layout.test, container, false);
//        sms = (TextView) scrollView.findViewById(R.id.sms);
//        photoView = (ImageView) scrollView.findViewById(R.id.image);


        RelativeLayout rl = (RelativeLayout) inflater.inflate(R.layout.birthday_item, container, false);
        ImageView imageView = (ImageView) rl.findViewById(R.id.photo);

//        new MyTask().execute();
//        photo = openPhoto(121, getContext());


//        InputStream is = openDisplayPhoto(93, getContext());
//        photo = BitmapFactory.decodeStream(is);
//        Log.e("TAG", String.valueOf(photo.getHeight()) + "\n" + String.valueOf(photo.getWidth()));
//        Uri photoUri = getPhotoUri(83, getContext());
//        Log.e("TAG",photoUri.toString() );
//        Uri photoUr2 = getPhotoUri(93, getContext());
//        Log.e("TAG",photoUr2.toString() );



        try {
            Bitmap mBitmap = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), Uri.parse("content://com.android.contacts/contacts/121/display_photo"));
            imageView.setImageBitmap(mBitmap);
        } catch (IOException e) {
            imageView.setImageResource(R.drawable.user3);

        }
//        new MyTask().execute();
//        photoView.setImageBitmap(m);
//        getBirthdays();


        return rl;
    }

    public Bitmap openPhoto(long contactId, Context context) {
        Uri contactUri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactId);
        Uri photoUri = Uri.withAppendedPath(contactUri, ContactsContract.Contacts.Photo.CONTENT_DIRECTORY);
        Cursor cursor = context.getContentResolver().query(photoUri,
                new String[]{ContactsContract.Contacts.Photo.PHOTO}, null, null, null);
        if (cursor == null) {
            return null;
        }
        try {
            if (cursor.moveToFirst()) {
                byte[] data = cursor.getBlob(0);
                if (data != null) {
                    return BitmapFactory.decodeStream(new ByteArrayInputStream(data));
                }
            }
        } finally {
            cursor.close();
        }
        return null;

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
                    birthdays += contactId + "\n" + displayName + "\n" + birthday + "\n\n";
                    Log.e("TAG", contactId + "\n" + displayName + "\n" + birthday + "\n\n");
                }
            }
            birthdayCur.close();

        }

        cur.close();
    }

    public Uri getPhotoUri(long contactId, Context context) {
        Uri contactUri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactId);
        Uri displayPhotoUri = Uri.withAppendedPath(contactUri, ContactsContract.Contacts.Photo.DISPLAY_PHOTO);
        return displayPhotoUri;
    }

    public InputStream openDisplayPhoto(long contactId, Context context) {
        Uri contactUri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactId);
        Uri displayPhotoUri = Uri.withAppendedPath(contactUri, ContactsContract.Contacts.Photo.DISPLAY_PHOTO);
        try {
            AssetFileDescriptor fd =
                    context.getContentResolver().openAssetFileDescriptor(displayPhotoUri, "r");
            return fd.createInputStream();
        } catch (IOException e) {
            return null;
        }
    }

    class MyTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            getBirthday();

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
//            sms.setText(birthdays);
//            photoView.setImageBitmap(photo);
            super.onPostExecute(aVoid);

        }
    }

    public void getBirthday() {
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
                    Log.e("TAG", id + "\n" + name + "\n" + birthday);
                }
            } while (cur.moveToNext());
        }
        cur.close();
    }


}
