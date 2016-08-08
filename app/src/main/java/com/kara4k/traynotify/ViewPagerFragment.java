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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ViewPagerFragment extends Fragment implements Serializable {

    private TabLayout tabs;
    private Adapter adapter;
    private ViewPager viewPager;
    private QuickNotesFragment quickNotes;
    private DelayedNotesFragment delayedNotes;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        LinearLayout linearLayout = (LinearLayout) inflater.inflate(R.layout.viewpager_fragment, container, false);
        viewPager = (ViewPager) linearLayout.findViewById(R.id.viewpager);
        adapter = new Adapter(getFragmentManager());
        quickNotes = new QuickNotesFragment();
        delayedNotes = new DelayedNotesFragment();
        adapter.addFragment(quickNotes, "Quick Notes");
        adapter.addFragment(delayedNotes, "Quick Notes2");
        viewPager.setAdapter(adapter);

        tabs = (TabLayout) getActivity().findViewById(R.id.tabs);
        tabs.setVisibility(View.VISIBLE);
        tabs.setupWithViewPager(viewPager);
        return linearLayout;
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
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }
}
