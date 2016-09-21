package com.kara4k.traynotify;


import java.io.Serializable;

class Note implements Serializable{

    private final int id;
    private final String title;
    private final String text;
    private int icon;
    private final long date;
    private final int numid;

    public Note(int id, String title, String text, int icon, long date, int numid) {
        this.id = id;
        this.title = title;
        this.text = text;
        this.icon = icon;
        this.date = date;
        this.numid = numid;
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
