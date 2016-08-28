package com.kara4k.traynotify;


import java.io.Serializable;
import java.util.List;

public class SendObj implements Serializable{

    List<Note> notes;
    List<DelayedNote> delayedNotes;

    SendObj() {

    }

    public SendObj(List<Note> notes) {
        this.notes = notes;
    }

    public SendObj(List<DelayedNote> delayedNotes, int i) {
        this.delayedNotes = delayedNotes;
    }

    public List<Note> getNotes() {
        return notes;
    }

    public void setNotes(List<Note> notes) {
        this.notes = notes;
    }

    public List<DelayedNote> getDelayedNotes() {
        return delayedNotes;
    }

    public void setDelayedNotes(List<DelayedNote> delayedNotes) {
        this.delayedNotes = delayedNotes;
    }
}
