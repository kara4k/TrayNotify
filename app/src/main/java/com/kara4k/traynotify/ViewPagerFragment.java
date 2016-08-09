package com.kara4k.traynotify;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.List;

public class ViewPagerFragment extends Fragment  {

    private TabLayout tabs;
    private Adapter adapter;
    private ViewPager viewPager;
    private QuickNotesFragment quickNotes;
    private DelayedNotesFragment delayedNotes;
    private getPagerItem getPagerItem;

    public interface getPagerItem {
        void getItem(int i);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        LinearLayout linearLayout = (LinearLayout) inflater.inflate(R.layout.viewpager_fragment, container, false);
        viewPager = (ViewPager) linearLayout.findViewById(R.id.viewpager);
        adapter = new Adapter(getFragmentManager());
        quickNotes = new QuickNotesFragment();
        delayedNotes = new DelayedNotesFragment();
        adapter.addFragment(quickNotes, "Quick Notes");
        adapter.addFragment(delayedNotes, "Notifications");
        viewPager.setAdapter(adapter);
        tabs = (TabLayout) getActivity().findViewById(R.id.tabs);
        tabs.setVisibility(View.VISIBLE);
        tabs.setupWithViewPager(viewPager);
        Log.e("fragge", String.valueOf(getArguments().getInt("item", 0)));
        viewPager.setCurrentItem(getArguments().getInt("item", 0));

        return linearLayout;
    }


    @Override
    public void onDetach() {
        super.onDetach();
        tabs.setVisibility(View.GONE);
        if (getPagerItem!=null) {
            getPagerItem.getItem(viewPager.getCurrentItem());
        }
    }

    public void setGetPagerItem(ViewPagerFragment.getPagerItem getPagerItem) {
        this.getPagerItem = getPagerItem;
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
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }
}
