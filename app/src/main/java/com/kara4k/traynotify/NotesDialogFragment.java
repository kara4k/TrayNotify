package com.kara4k.traynotify;


import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

public class NotesDialogFragment extends DialogFragment implements QuickAdapter.GetNoteId {
    private List<Note> notes;
    private QuickAdapter adapter;
    private GetNoteWidget getNoteWidget;

    public interface GetNoteWidget {
        void getNoteData(int i, String title);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        RecyclerView recyclerView = (RecyclerView) inflater.inflate(R.layout.quick_notes_fragment, container, false);
        adapter = QuickAdapter.getInstance();
        getAllNotesFromDB();
        adapter.setList(notes);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        adapter.setGetNoteId(this);
        this.getDialog().setTitle("Choose note");
        return recyclerView;
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
        Log.e("TAG", String.valueOf(i));
        getNoteWidget.getNoteData(i, title);
        this.getDialog().dismiss();
    }
}
