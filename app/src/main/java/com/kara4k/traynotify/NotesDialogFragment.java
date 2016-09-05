package com.kara4k.traynotify;


import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class NotesDialogFragment extends DialogFragment implements QuickAdapter.GetNoteId{
    private List<Note> notes;
    private QuickAdapter adapter;
    private GetNoteWidget getNoteWidget;



    public interface GetNoteWidget {
        void getNoteData(int i, String title);
    }



    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        RecyclerView recyclerView = (RecyclerView) getActivity().getLayoutInflater().inflate(R.layout.quick_notes_fragment, null);
        adapter = QuickAdapter.getInstance();
        getAllNotesFromDB();
        adapter.setList(notes);
        makeSingleInstance();
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        adapter.setGetNoteId(this);
        adapter.setSelectionMode(null);
        DialogInterface.OnClickListener neutralListener = (DialogInterface.OnClickListener) getActivity();
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        return builder.setView(recyclerView).setNeutralButton(R.string.deselect, neutralListener)
                .setNegativeButton(android.R.string.cancel, neutralListener).create();
    }

    private void getAllNotesFromDB() {
        DBQuick db = new DBQuick(getActivity());
        db.open();
        List<Note> allnotes = new ArrayList<>();
        Cursor allData = db.getAllData();
        if (allData.moveToFirst()) {
            do {
                allnotes.add(new Note(allData.getInt(0), allData.getString(1), allData.getString(2), allData.getInt(3), allData.getLong(4), allData.getInt(5)));
            } while (allData.moveToNext());
        }
        db.close();
        notes = allnotes;
    }

    public void setGetNoteWidget(GetNoteWidget getNoteWidget) {
        this.getNoteWidget = getNoteWidget;
    }

    @Override
    public void getId(int i, String title) {
        getNoteWidget.getNoteData(i, title);
        this.getDialog().dismiss();
    }

    private void makeSingleInstance() {
        SharedPreferences sp = getActivity().getSharedPreferences(WidgetConfig.WIDGET_CONF, Context.MODE_PRIVATE);
        try {
            removeExistedNotes(sp);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void removeExistedNotes(SharedPreferences sp) {
        Iterator<Note> iter = notes.listIterator();
        while (iter.hasNext()) {
            Note x = iter.next();
            if (sp.getInt("#" + x.getNumid(), 0) != 0) {
                iter.remove();
            }
        }
    }


}
