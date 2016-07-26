package com.kara4k.traynotify;


import java.io.Serializable;

public class Note implements Serializable{

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

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public int getIcon() {
        return icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public int getNumid() {
        return numid;
    }

    public void setNumid(int numid) {
        this.numid = numid;
    }
}
