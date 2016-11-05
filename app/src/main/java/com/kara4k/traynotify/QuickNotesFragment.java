package com.kara4k.traynotify;


import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class QuickNotesFragment extends Fragment {

    public static final String QUICK_SORT = "quick_sort";
    public static final int TITLE = 0;
    public static final int TEXT = 1;
    public static final int DATE = 2;

    private List<Note> notes;
    private QuickAdapter adapter;
    private RecyclerView recyclerView;
    private SharedPreferences sp;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        recyclerView = (RecyclerView) inflater.inflate(R.layout.quick_notes_fragment, container, false);
        adapter = QuickAdapter.getInstance();
        adapter.setGetNoteId(null);
        notes = getAllNotesFromDB(getContext());

        adapter.setList(notes);
        recyclerView.setAdapter(adapter);

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));


        SelectionMode selectionMode = (SelectionMode) getActivity();
        adapter.setSelectionMode(selectionMode);

        sp = PreferenceManager.getDefaultSharedPreferences(getContext());
//        ItemTouchHelper.Callback callback = new QuickTouchHelper(adapter);
//        ItemTouchHelper helper = new ItemTouchHelper(callback);
//        helper.attachToRecyclerView(recyclerView);
        return recyclerView;
    }

    @Override
    public void onStart() {
        setSortOrder();
        super.onStart();
    }

    public void setSortOrder() {
        int sort = sp.getInt(QUICK_SORT, DATE);
        if (sort == DATE) {
            sortByDate();
        }
        if (sort == TITLE) {
            sortByTitle();
        }
        if (sort == TEXT) {
            sortByText();
        }

    }


    public static List<Note> getAllNotesFromDB(Context context) {
        try {
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
        } catch (Exception e) {
            return new ArrayList<Note>();
        }
    }

    public void sortByTitle() {
        if ((notes != null) && (notes.size() != 0)) {
            Collections.sort(notes, new Comparator<Note>() {
                @Override
                public int compare(Note note, Note t1) {
                    return note.getTitle().compareToIgnoreCase(t1.getTitle());
                }
            });

            QuickAdapter.getInstance().notifyDataSetChanged();

            sp.edit().putInt(QUICK_SORT, TITLE).apply();

        }


    }
    public void sortByText() {
        if ((notes != null) && (notes.size() != 0)) {
            Collections.sort(notes, new Comparator<Note>() {
                @Override
                public int compare(Note note, Note t1) {
                    return note.getText().compareToIgnoreCase(t1.getText());
                }
            });

            QuickAdapter.getInstance().notifyDataSetChanged();

            sp.edit().putInt(QUICK_SORT, TEXT).apply();

        }
    }
    public void sortByDate() {
        if ((notes != null) && (notes.size() != 0)) {
            Collections.sort(notes, new Comparator<Note>() {
                @Override
                public int compare(Note note, Note t1) {
                    if (note.getDate() < t1.getDate())
                        return 1;
                    if (note.getDate() > t1.getDate())
                        return -1;
                    return 0;

                }
            });

            QuickAdapter.getInstance().notifyDataSetChanged();

            sp.edit().putInt(QUICK_SORT, DATE).apply();

        }
    }

    public List<Note> getNotes() {
        return notes;
    }

    public void setNotes(List<Note> notes) {
        this.notes = notes;
    }



}
