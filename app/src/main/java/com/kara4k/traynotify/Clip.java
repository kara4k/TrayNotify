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

    public String getText() {
        return text;
    }

    public long getDate() {
        return date;
    }

    public int getNumId() {
        return numId;
    }

    public int getChecked() {
        return checked;
    }

    public void setChecked(int checked) {
        this.checked = checked;
    }
}


