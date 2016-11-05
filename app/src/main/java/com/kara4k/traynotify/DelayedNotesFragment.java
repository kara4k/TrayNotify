package com.kara4k.traynotify;


import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class DelayedNotesFragment extends Fragment {

    public static final String DELAYED_SORT = "delayed_sort";
    public static final int TITLE = 0;
    public static final int TEXT = 1;
    public static final int DATE = 2;
    public static final int REPEAT = 3;
    public static final int BIRTHDAY = 4;

    private List<DelayedNote> notes;
    private RecyclerView recyclerView;
    private SharedPreferences sp;
    private SharedPreferences sp1;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        recyclerView = (RecyclerView) inflater.inflate(R.layout.quick_notes_fragment, container, false);
        sp = PreferenceManager.getDefaultSharedPreferences(getContext());

        DelayedAdapter adapter = DelayedAdapter.getInstance();

        notes = getAllNotesFromDB();
        adapter.setList(notes);
//        setSortOrder();
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        SelectionMode selectionMode = (SelectionMode) getActivity();
        adapter.setSelectionMode(selectionMode);

        sp1 = PreferenceManager.getDefaultSharedPreferences(getContext());

//        ItemTouchHelper.Callback callback = new DelayedTouchHelper(adapter);
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
        int sort = sp.getInt(DELAYED_SORT, DATE);
        Log.e("DelayedNotesFragment", "setSortOrder: " + sort);
        if (sort == DATE) {
            sortByDate();
        }
        if (sort == TITLE) {
            sortByTitle();
        }
        if (sort == TEXT) {
            sortByText();
        }
        if (sort == REPEAT) {
            sortByRepeat();
        }
        if (sort == BIRTHDAY) {
            sortByBirth();
        }

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
            Cursor allData = getAllData(db);
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
                            allData.getInt(10),
                            allData.getInt(11)
                    ));
                } while (allData.moveToNext());
            }
            db.close();
            return allnotes;
        } catch (Exception e) {
            return new ArrayList<DelayedNote>();
        }
    }

    private Cursor getAllData(DBDelay db) {
        Cursor allData;
        boolean showBirthdays = sp.getBoolean(Settings.SHOW_BIRTHDAYS, true);
        if (showBirthdays) {
             allData = db.getAllData();
        } else {
            allData = db.getAllDataWithoughtBirthdays();
        }
        return allData;
    }

    public void sortByTitle() {
        if ((notes != null) && (notes.size() != 0)) {
            Collections.sort(notes, new Comparator<DelayedNote>() {
                @Override
                public int compare(DelayedNote delayedNote, DelayedNote t1) {
                    return delayedNote.getTitle().compareToIgnoreCase(t1.getTitle());
                }
            });
            DelayedAdapter.getInstance().notifyDataSetChanged();
            DelayedAdapter.getInstance().refreshAll();

            sp.edit().putInt(DELAYED_SORT, TITLE).apply();

        }
    }

    public void sortByText() {
        if ((notes != null) && (notes.size() != 0)) {
            Collections.sort(notes, new Comparator<DelayedNote>() {
                @Override
                public int compare(DelayedNote delayedNote, DelayedNote t1) {
                    return delayedNote.getText().compareToIgnoreCase(t1.getText());
                }
            });
            DelayedAdapter.getInstance().notifyDataSetChanged();
            DelayedAdapter.getInstance().refreshAll();

            sp.edit().putInt(DELAYED_SORT, TEXT).apply();

        }
    }

    public void sortByDate() {
        if ((notes != null) && (notes.size() != 0)) {
            Collections.sort(notes, new Comparator<DelayedNote>() {
                @Override
                public int compare(DelayedNote delayedNote, DelayedNote t1) {
                    if (delayedNote.getCreateTime() < t1.getCreateTime())
                        return 1;
                    if (delayedNote.getCreateTime() > t1.getCreateTime())
                        return -1;
                    return 0;
                }
            });
            DelayedAdapter.getInstance().notifyDataSetChanged();
            DelayedAdapter.getInstance().refreshAll();


            sp.edit().putInt(DELAYED_SORT, DATE).apply();

        }
    }

    public void sortByRepeat() {
        if ((notes != null) && (notes.size() != 0)) {
            Collections.sort(notes, new Comparator<DelayedNote>() {
                @Override
                public int compare(DelayedNote delayedNote, DelayedNote t1) {
                    if (delayedNote.getRepeat() > t1.getRepeat())
                        return 1;
                    if (delayedNote.getRepeat() < t1.getRepeat())
                        return -1;
                    return 0;
                }
            });
            DelayedAdapter.getInstance().notifyDataSetChanged();
            DelayedAdapter.getInstance().refreshAll();

            sp.edit().putInt(DELAYED_SORT, REPEAT).apply();

        }
    }

    public void sortByBirth() {
        if ((notes != null) && (notes.size() != 0)) {
            Collections.sort(notes, new Comparator<DelayedNote>() {
                @Override
                public int compare(DelayedNote delayedNote, DelayedNote t1) {
                    if (delayedNote.getBirthday() < t1.getBirthday())
                        return 1;
                    if (delayedNote.getBirthday() > t1.getBirthday())
                        return -1;
                    if (delayedNote.getBirthday() == t1.getBirthday()) {
                        return delayedNote.getText().compareToIgnoreCase(t1.getText());
                    }
                    return 0;
                }
            });
            DelayedAdapter.getInstance().notifyDataSetChanged();
            DelayedAdapter.getInstance().refreshAll();

            sp.edit().putInt(DELAYED_SORT, BIRTHDAY).apply();

        }
    }
}
