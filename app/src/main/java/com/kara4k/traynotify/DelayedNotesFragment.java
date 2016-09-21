package com.kara4k.traynotify;


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

public class DelayedNotesFragment extends Fragment {

    private List<DelayedNote> notes;
    private RecyclerView recyclerView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        recyclerView = (RecyclerView) inflater.inflate(R.layout.quick_notes_fragment, container, false);
        DelayedAdapter adapter = DelayedAdapter.getInstance();

        notes = getAllNotesFromDB();
        adapter.setList(notes);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        SelectionMode selectionMode = (SelectionMode) getActivity();
        adapter.setSelectionMode(selectionMode);


//        ItemTouchHelper.Callback callback = new DelayedTouchHelper(adapter);
//        ItemTouchHelper helper = new ItemTouchHelper(callback);
//        helper.attachToRecyclerView(recyclerView);
        return recyclerView;
    }

    public List<DelayedNote> getNotes() {
        return notes;
    }

    public void setNotes(List<DelayedNote> notes) {
        this.notes = notes;
    }

    public List<DelayedNote> getAllNotesFromDB() {
        try {
            DBDelay db = new DBDelay(getActivity());
            db.open();
            List<DelayedNote> allnotes = new ArrayList<>();
            Cursor allData = db.getAllData();
            if (allData.moveToFirst()) {
                do {
                    allnotes.add(new DelayedNote(allData.getInt(0),
                            allData.getString(1),
                            allData.getString(2),
                            allData.getLong(3),
                            allData.getLong(4),
                            allData.getInt(5),
                            allData.getString(6),
                            allData.getString(7),
                            allData.getString(8),
                            allData.getInt(9),
                            allData.getInt(10)
                    ));
                } while (allData.moveToNext());
            }
            db.close();
            return allnotes;
        } catch (Exception e) {
            return new ArrayList<DelayedNote>();
        }
    }
}
