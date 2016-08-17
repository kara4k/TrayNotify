package com.kara4k.traynotify;


class SMS {

    private int id;
    private int person;
    private String address;
    private String body;
    private long date;

    public SMS(int id, int person, String address, String body, long date) {
        this.id = id;
        this.person = person;
        this.address = address;
        this.body = body;
        this.date = date;
    }

    public String getAddress() {
        return address;
    }

    public String getBody() {
        return body;
    }

    public long getDate() {
        return date;
    }

}
