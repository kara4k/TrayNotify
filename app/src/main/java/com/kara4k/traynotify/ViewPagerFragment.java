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

public class ViewPagerFragment extends Fragment  {

    private TabLayout tabs;
    private ViewPager viewPager;
    private QuickNotesFragment quickNotes;
    private DelayedNotesFragment delayedNotes;
    private Adapter adapter;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        LinearLayout linearLayout = (LinearLayout) inflater.inflate(R.layout.viewpager_fragment, container, false);
        viewPager = (ViewPager) linearLayout.findViewById(R.id.viewpager);
        adapter = new Adapter(getFragmentManager());
        quickNotes = new QuickNotesFragment();
        delayedNotes = new DelayedNotesFragment();
        adapter.addFragment(quickNotes, getString(R.string.notes));
        adapter.addFragment(delayedNotes, getString(R.string.notifications));
        viewPager.setAdapter(adapter);
        tabs = (TabLayout) getActivity().findViewById(R.id.tabs);
        tabs.setVisibility(View.VISIBLE);
        tabs.setupWithViewPager(viewPager);

        if (getArguments()!= null) {
            int item = getArguments().getInt("item", 0);
            viewPager.setCurrentItem(item);
        }


        return linearLayout;
    }

    public ViewPager getViewPager() {
        return viewPager;
    }

    public void updateQuick() {
        try {
            adapter = new Adapter(getFragmentManager());
            Bundle quickNotesBundle = new Bundle();
            SendObj sendObj = new SendObj(quickNotes.getNotes());
            quickNotesBundle.putSerializable("notes", sendObj);
            quickNotes = new QuickNotesFragment();
            quickNotes.setArguments(quickNotesBundle);
            adapter.addFragment(quickNotes, getString(R.string.notes));
            adapter.addFragment(delayedNotes, getString(R.string.notifications));
            viewPager.setAdapter(adapter);
            viewPager.setCurrentItem(0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateTrayRemoved(int item) {
        try {
            int index = quickNotes.getRecyclerPosition();
            int top = quickNotes.getPadding();
            adapter = new Adapter(getFragmentManager());
            quickNotes = new QuickNotesFragment();
            adapter.addFragment(quickNotes, getString(R.string.notes));
            adapter.addFragment(delayedNotes, getString(R.string.notifications));
            viewPager.setAdapter(adapter);
            viewPager.setCurrentItem(item);
            quickNotes.scrollTo(index, top);
        } catch (Exception e) {

        }
    }

    public void updateDelayed () {
        try {
            adapter = new Adapter(getFragmentManager());
            Bundle delayedNotesBundle = new Bundle();
            SendObj sendObj = new SendObj(delayedNotes.getNotes(), 0);
            delayedNotesBundle.putSerializable("delayed_notes", sendObj);

            delayedNotes = new DelayedNotesFragment();
            delayedNotes.setArguments(delayedNotesBundle);

            adapter.addFragment(quickNotes, getString(R.string.notes));
            adapter.addFragment(delayedNotes, getString(R.string.notifications));
            viewPager.setAdapter(adapter);
            viewPager.setCurrentItem(1);
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
        public int getItemPosition(Object object){
            return POSITION_NONE;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }
}
