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

public class ClipFragment extends Fragment {

    public static final String CLIP_SORT = "clip_sort";
    public static final int TEXT = 0;
    public static final int DATE = 1;

    private ClipAdapter adapter;
    private List<Clip> clipList;
    private List<Clip> clipListAll = new ArrayList<>();
    private SharedPreferences sp;

    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        RecyclerView recyclerView = (RecyclerView) inflater.inflate(R.layout.quick_notes_fragment, container, false);
        sp = PreferenceManager.getDefaultSharedPreferences(getContext());
        fillLists();

        adapter = ClipAdapter.getInstance();
        adapter.setNotes(clipList);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        SelectionMode selectionMode = (SelectionMode) getActivity();
        adapter.setSelectionMode(selectionMode);

        return recyclerView;
    }

    public void setSortOrder() {
        int sort = sp.getInt(CLIP_SORT, DATE);
        MainActivity main = (MainActivity) getActivity();
        if (sort == DATE) {
            sortByDate();
            main.mainMenu.findItem(R.id.sort_clip_date).setChecked(true);
        }
        if (sort == TEXT) {
            sortByText();
            main.mainMenu.findItem(R.id.sort_clip_text).setChecked(true);
        }

    }

    private void fillLists() {
        clipList = getClipListFromDB(getContext());
        setSortOrder();
        clipListAll.clear();
        clipListAll.addAll(clipList);
    }

    public void updateList() {
        fillLists();
        adapter.setNotes(clipList);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onStart() {
        fillLists();
        adapter.setNotes(clipList);
        adapter.notifyDataSetChanged();
        super.onStart();
    }

    private static List<Clip> getClipListFromDB(Context context) {
        try {
            DBClip db = new DBClip(context);
            db.open();
            ArrayList<Clip> list = new ArrayList<>();
            Cursor allData = db.getAllData();
            if (allData.moveToFirst()) {
                do {
                    list.add(new Clip(allData.getInt(0),allData.getString(1),allData.getLong(2),allData.getInt(3),allData.getInt(4)));
                } while (allData.moveToNext());
            }
            db.close();
            return list;
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    public List<Clip> getClipListAll() {
        return clipListAll;
    }

    public void setClipList(List<Clip> clipList) {
        this.clipList = clipList;
    }

    public List<Clip> getClipList() {
        return clipList;
    }

    public void sortByText() {
        if ((clipList != null) && (clipList.size() != 0)) {
            Collections.sort(clipList, new Comparator<Clip>() {
                @Override
                public int compare(Clip clip, Clip t1) {
                    return clip.getText().compareToIgnoreCase(t1.getText());
                }
            });
            ClipAdapter.getInstance().notifyDataSetChanged();

            sp.edit().putInt(CLIP_SORT, TEXT).apply();


        }
    }

    public void sortByDate() {
        if ((clipList != null) && (clipList.size() != 0)) {
            Collections.sort(clipList, new Comparator<Clip>() {
                @Override
                public int compare(Clip clip, Clip t1) {
                    if (clip.getDate() < t1.getDate())
                        return 1;
                    if (clip.getDate() > t1.getDate())
                        return -1;
                    return 0;
                }
            });
            ClipAdapter.getInstance().notifyDataSetChanged();

            sp.edit().putInt(CLIP_SORT, DATE).apply();


        }
    }
}
