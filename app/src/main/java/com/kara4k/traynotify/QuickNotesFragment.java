package com.kara4k.traynotify;


import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

public class QuickNotesFragment extends Fragment {

    private List<Note> notes;
    private QuickAdapter adapter;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        inflater.inflate(R.layout.quick_notes_fragment, container);
        RecyclerView recyclerView = (RecyclerView) inflater.inflate(R.layout.quick_notes_fragment, container, false);
        adapter = QuickAdapter.getInstance();
        adapter.setGetNoteId(null);
        try {
            if (getArguments() != null) {
                SendObj sendObj = (SendObj) getArguments().getSerializable("notes");
                notes = sendObj.getNotes();
            } else {
                notes = getAllNotesFromDB(getContext());
            }
        } catch (Exception e) {
           notes = getAllNotesFromDB(getContext());
        }

        adapter.setList(notes);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

//        ItemTouchHelper.Callback callback = new QuickTouchHelper(adapter);
//        ItemTouchHelper helper = new ItemTouchHelper(callback);
//        helper.attachToRecyclerView(recyclerView);
        return recyclerView;
    }




    public static List<Note> getAllNotesFromDB(Context context) {
        DBQuick db = new DBQuick(context);
        db.open();
        List<Note> allnotes = new ArrayList<>();
        Cursor allData = db.getAllData();
        if (allData.moveToFirst()) {
            do {
                allnotes.add(new Note(allData.getInt(0), allData.getString(1), allData.getString(2), allData.getInt(3), allData.getLong(4), allData.getInt(5)));
            } while (allData.moveToNext());
        }
        db.close();
        return allnotes;
    }

    public List<Note> getNotes() {
        return notes;
    }

    public void setNotes(List<Note> notes) {
        this.notes = notes;
    }

}
