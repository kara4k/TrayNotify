//package com.kara4k.traynotify;
//
//
//import android.support.v7.widget.RecyclerView;
//import android.support.v7.widget.helper.ItemTouchHelper;
//
//class DelayedTouchHelper extends ItemTouchHelper.SimpleCallback {
//    private final DelayedAdapter delayedAdapter;
//
//    public DelayedTouchHelper(DelayedAdapter delayedAdapter) {
//        super(0, ItemTouchHelper.RIGHT);
//        this.delayedAdapter = delayedAdapter;
//    }
//
//    @Override
//    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
//        return false;
//    }
//
//
//    @Override
//    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
//    }
//
//
//}