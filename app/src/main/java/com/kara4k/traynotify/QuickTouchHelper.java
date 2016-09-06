package com.kara4k.traynotify;

import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;

class QuickTouchHelper extends ItemTouchHelper.SimpleCallback {
    private final QuickAdapter quickAdapter;

    public QuickTouchHelper(QuickAdapter quickAdapter) {
        super(0 , ItemTouchHelper.LEFT);
        this.quickAdapter = quickAdapter;
    }

    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
        return false;
    }


    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {

    }


}
