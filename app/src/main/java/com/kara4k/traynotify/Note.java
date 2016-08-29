package com.kara4k.traynotify;


import java.io.Serializable;

class Note implements Serializable{

    private int id;
    private String title;
    private String text;
    private int icon;
    private long date;
    private int numid;

    public Note(int id, String title, String text, int icon, long date, int numid) {
        this.id = id;
        this.title = title;
        this.text = text;
        this.icon = icon;
        this.date = date;
        this.numid = numid;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getText() {
        return text;
    }

    public long getDate() {
        return date;
    }

    public int getNumid() {
        return numid;
    }

    public int getIcon() {
        return icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }
}
