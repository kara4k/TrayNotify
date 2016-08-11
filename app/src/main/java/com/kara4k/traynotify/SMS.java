package com.kara4k.traynotify;


public class SMS {

    private int id;
    private int person;
    private String address;
    private String body;
    private long date;

    public SMS() {
    }

    public SMS(int id, int person, String address, String body, long date) {
        this.id = id;
        this.person = person;
        this.address = address;
        this.body = body;
        this.date = date;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getPerson() {
        return person;
    }

    public void setPerson(int person) {
        this.person = person;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }
}
