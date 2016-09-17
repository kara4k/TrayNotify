package com.kara4k.traynotify;

class Clip {
    private int id;
    private String text;
    private long date;
    private int numId;
    private int checked;

    public Clip(int id, String text, long date, int numId, int checked) {
        this.id = id;
        this.text = text;
        this.date = date;
        this.numId = numId;
        this.checked = checked;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public int getNumId() {
        return numId;
    }

    public void setNumId(int numId) {
        this.numId = numId;
    }

    public int getChecked() {
        return checked;
    }

    public void setChecked(int checked) {
        this.checked = checked;
    }
}


