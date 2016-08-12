package com.kara4k.traynotify;


public class Birthday {


    private String id;
    private String name;
    private String photoUri;
    private String date;
    private int age;
    private int daysLeft;
    private int sign;
    private String dayOfWeek;

    public Birthday() {
    }

    public Birthday(String id, String name, String photoUri, String date, int age, int daysLeft, int sign, String dayOfWeek) {
        this.id = id;
        this.name = name;
        this.photoUri = photoUri;
        this.date = date;
        this.age = age;
        this.daysLeft = daysLeft;
        this.sign = sign;
        this.dayOfWeek = dayOfWeek;
    }

    public Birthday(String id, String name, String date, String cId) {
        this.id = id;
        this.name = name;
        this.date = date;
        this.photoUri = "content://com.android.contacts/contacts/"+ cId + "/display_photo";
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhotoUri() {
        return photoUri;
    }

    public void setPhotoUri(String photoUri) {
        this.photoUri = photoUri;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public int getDaysLeft() {
        return daysLeft;
    }

    public void setDaysLeft(int daysLeft) {
        this.daysLeft = daysLeft;
    }

    public int getSign() {
        return sign;
    }

    public void setSign(int sign) {
        this.sign = sign;
    }

    public String getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(String dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }
}
