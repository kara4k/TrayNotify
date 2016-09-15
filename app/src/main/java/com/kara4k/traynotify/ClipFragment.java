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

public class ClipFragment extends Fragment {

    ClipAdapter adapter;
    List<Clip> clipList;
    List<Clip> clipListAll = new ArrayList<>();

    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        RecyclerView recyclerView = (RecyclerView) inflater.inflate(R.layout.quick_notes_fragment, container, false);

        fillLists();

        adapter = ClipAdapter.getInstance();
        adapter.setNotes(clipList);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        return recyclerView;
    }

    private void fillLists() {
        clipList = getClipListFromDB();
        clipListAll.clear();
        clipListAll.addAll(clipList);
    }

    @Override
    public void onStart() {
        fillLists();
        adapter.setNotes(clipList);
        adapter.notifyDataSetChanged();
        super.onStart();
    }

    private List<Clip> getClipListFromDB() {
        try {
            DBClip db = new DBClip(getContext());
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
}
