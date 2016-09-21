package com.kara4k.traynotify;


class SMS {

    private final String address;
    private final String body;
    private final long date;

    public SMS(int id, int person, String address, String body, long date) {
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
