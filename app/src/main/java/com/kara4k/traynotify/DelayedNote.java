package com.kara4k.traynotify;


class DelayedNote {

    private int id;
    private String text;
    private String title;
    private long createTime;
    private long setTime;
    private int repeat;
    private String days;
    private String sound;
    private String vibration;
    private int priority;
    private int checkId;
    private int birthday;

    DelayedNote() {

    }

    public DelayedNote(int id, String text, String title, long createTime, long setTime, int repeat, String days, String sound, String vibration, int priority, int checkId) {
        this.id = id;
        this.text = text;
        this.title = title;
        this.createTime = createTime;
        this.setTime = setTime;
        this.repeat = repeat;
        this.days = days;
        this.sound = sound;
        this.vibration = vibration;
        this.priority = priority;
        this.checkId = checkId;
    }

    private int getId() {
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


    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public long getSetTime() {
        return setTime;
    }

    public void setSetTime(long setTime) {
        this.setTime = setTime;
    }

    public int getRepeat() {
        return repeat;
    }

    public void setRepeat(int repeat) {
        this.repeat = repeat;
    }

    public String getDays() {
        return days;
    }

    public void setDays(String days) {
        this.days = days;
    }

    public String getSound() {
        return sound;
    }

    public void setSound(String sound) {
        this.sound = sound;
    }

    public String getVibration() {
        return vibration;
    }

    public void setVibration(String vibration) {
        this.vibration = vibration;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public int getCheckId() {
        return checkId;
    }

    public void setCheckId(int checkId) {
        this.checkId = checkId;
    }

    public int getBirthday() {
        return birthday;
    }

    public void setBirthday(int birthday) {
        this.birthday = birthday;
    }

    @Override
    public String toString() {
        String note = "";
        note = getId() + "\n"
                + getText() + "\n"
                + getTitle() + "\n"
                + getCreateTime() + "\n"
                + getSetTime() + "\n"
                + getRepeat() + "\n"
                + getDays() + "\n"
                + getSound() + "\n"
                + getVibration() + "\n"
                + getPriority() + "\n"
                + getCheckId();
        return note;
    }
}
