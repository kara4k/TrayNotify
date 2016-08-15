package com.kara4k.traynotify;


public class Birthday implements Comparable<Birthday> {


    private String id;
    private String name;
    private String photoUri;
    private String date;
    private int age;
    private int daysLeft;
    private int sign;
    private long setTime;

    public Birthday() {
    }




    public Birthday(String id, String name, String date, String cId, int daysLeft, int age, int sign, long setTime) {
        this.id = id;
        this.name = name;
        this.date = date;
        this.photoUri = "content://com.android.contacts/contacts/"+ cId + "/display_photo";
        this.daysLeft = daysLeft;
        this.age = age;
        this.sign = sign;
        this.setTime = setTime;
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

    public long getSetTime() {
        return setTime;
    }

    public void setSetTime(long setTime) {
        this.setTime = setTime;
    }

    @Override
    public int compareTo(Birthday birthday) {

            if(this.daysLeft > birthday.daysLeft)
                return 1;
            if(this.daysLeft < birthday.daysLeft)
                return -1;
            return 0;
        }

}
