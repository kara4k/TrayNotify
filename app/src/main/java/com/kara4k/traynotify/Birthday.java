package com.kara4k.traynotify;


import android.support.annotation.NonNull;

class Birthday implements Comparable<Birthday> {


    private String id;
    private String name;
    private String photoUri;
    private String date;
    private int age;
    private int daysLeft;
    private int sign;
    private long setTime;


    public Birthday(String id, String name, String date, String cId, int daysLeft, int age, int sign, long setTime) {
        this.id = id;
        this.name = name;
        this.date = date;
        this.photoUri = "content://com.android.contacts/contacts/" + cId + "/display_photo";
        this.daysLeft = daysLeft;
        this.age = age;
        this.sign = sign;
        this.setTime = setTime;
    }

    public Birthday(String contId, String name, String date) {
        this.id = contId;
        this.name = name;
        this.date = date;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getPhotoUri() {
        return photoUri;
    }

    public String getDate() {
        return date;
    }

    public int getAge() {
        return age;
    }

    public int getDaysLeft() {
        return daysLeft;
    }

    public int getSign() {
        return sign;
    }

    public long getSetTime() {
        return setTime;
    }

    @Override
    public int compareTo(@NonNull Birthday birthday) {

        if (this.daysLeft > birthday.daysLeft)
            return 1;
        if (this.daysLeft < birthday.daysLeft)
            return -1;
        return 0;
    }

}
