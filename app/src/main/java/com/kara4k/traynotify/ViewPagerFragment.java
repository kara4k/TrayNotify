package com.kara4k.traynotify;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.List;

public class ViewPagerFragment extends Fragment {

    private TabLayout tabs;
    private LockableViewPager viewPager;
    private QuickNotesFragment quickNotes;
    private DelayedNotesFragment delayedNotes;
    private Adapter adapter;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        LinearLayout linearLayout = (LinearLayout) inflater.inflate(R.layout.viewpager_fragment, container, false);
        viewPager = (LockableViewPager) linearLayout.findViewById(R.id.viewpager);
        adapter = new Adapter(getFragmentManager());
        quickNotes = new QuickNotesFragment();
        delayedNotes = new DelayedNotesFragment();
        adapter.addFragment(quickNotes, getString(R.string.notes));
        adapter.addFragment(delayedNotes, getString(R.string.notifications));
        viewPager.setAdapter(adapter);
        ViewPager.OnPageChangeListener pageChangeListener = (ViewPager.OnPageChangeListener) getActivity();
        viewPager.addOnPageChangeListener(pageChangeListener);
        tabs = (TabLayout) getActivity().findViewById(R.id.tabs);
        tabs.setVisibility(View.VISIBLE);
        tabs.setupWithViewPager(viewPager);

        if (getArguments() != null) {
            int item = getArguments().getInt("item", 0);
            viewPager.setCurrentItem(item);
        }

//        viewPager.setSwipeLocked(true);

        return linearLayout;
    }


    public LockableViewPager getViewPager() {
        return viewPager;
    }

    public TabLayout getTabs() {
        return tabs;
    }

    public void refreshQuick(List<Note> notes) {
        try {
            quickNotes.setNotes(notes);
            QuickAdapter.getInstance().notifyDataSetChanged();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void refreshDelayed(List<DelayedNote> notes) {
        try {
            delayedNotes.setNotes(notes);
            DelayedAdapter.getInstance().notifyDataSetChanged();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public QuickNotesFragment getQuickNotes() {
        return quickNotes;
    }

    public DelayedNotesFragment getDelayedNotes() {
        return delayedNotes;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        tabs.setVisibility(View.GONE);

    }

    static class Adapter extends FragmentStatePagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public Adapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }


        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }


        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }
}
