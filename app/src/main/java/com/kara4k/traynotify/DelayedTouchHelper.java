package com.kara4k.traynotify;


import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;

public class DelayedTouchHelper extends ItemTouchHelper.SimpleCallback {
    private DelayedAdapter delayedAdapter;

    public DelayedTouchHelper(DelayedAdapter delayedAdapter) {
        super(0, ItemTouchHelper.RIGHT);
        this.delayedAdapter = delayedAdapter;
    }

    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
        //TODO: Not implemented here
        return false;
    }


    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
        delayedAdapter.remove(viewHolder.getAdapterPosition());
    }


}