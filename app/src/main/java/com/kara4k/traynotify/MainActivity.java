package com.kara4k.traynotify;

import android.app.NotificationManager;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.util.List;

public class MainActivity extends AppCompatActivity implements SearchView.OnQueryTextListener,
        SelectionMode, ViewPager.OnPageChangeListener {

    private DrawerLayout mDrawerLayout;
    private NotificationManager nm;
    private int pagerItem = 0;
    private ViewPagerFragment vpFragment;
    private NavigationView navigationView;
    private FloatingActionButton fab;
    private BirthdayFragment birthdayFragment;
    private Menu mainMenu;
    private SMSFragment smsFragment;
    private ActionMode actionMode;
    private Toolbar toolbar;
    private ClipFragment clipFragment;
    private SharedPreferences sp;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        final ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setHomeAsUpIndicator(R.drawable.ic_menu_white_24dp);
            supportActionBar.setDisplayHomeAsUpEnabled(true);
        }

        if (savedInstanceState != null) {
            pagerItem = savedInstanceState.getInt("item", 0);
        }

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer);

        nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);


        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    // This method will trigger on item Click of navigation menu
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        switch (menuItem.getItemId()) {
                            case R.id.add_note:
                                showFirstFragment();
                                setVPFragmentMenu();
                                break;
                            case R.id.messages:
                                smsFragment = new SMSFragment();
                                showSecondaryFragment(smsFragment);
                                setBarTitle(supportActionBar, getString(R.string.messages));
                                setVPFragmentMenu();
                                break;
                            case R.id.clipboard:
                                clipFragment = new ClipFragment();
                                showSecondaryFragment(clipFragment);
                                setBarTitle(supportActionBar, getString(R.string.clipboard));
                                setClipFragmentMenu();
                                break;
                            case R.id.birthdays:
                                birthdayFragment = new BirthdayFragment();
                                showSecondaryFragment(birthdayFragment);
                                setBarTitle(supportActionBar, getString(R.string.birthdays));
                                setBirthdaysMenu();
                                break;
                            case R.id.settings:
                                Intent settings = new Intent(MainActivity.this, Settings.class);
                                startActivity(settings);
                                break;
                            case R.id.rate:
                                rateApp();
                                break;
                            case R.id.feedback:
                                sendEmail(new String[]{"kara4k@gmail.com"});
                                break;
                        }
                        mDrawerLayout.closeDrawers();
                        return true;
                    }
                });


        fab = (FloatingActionButton) findViewById(R.id.fab);


        setFabNotes();
        showFirstFragment();

        getApplicationContext().registerReceiver(removeTrayReceiver, new IntentFilter("refreshTrayIcons"));




        restartAlarmsOnUpdate();
    }

    private void restartAlarmsOnUpdate() {
        String versionPrev = sp.getString(Settings.VERSION_CODE, "0");
        int versionCode = BuildConfig.VERSION_CODE;
        if (!String.valueOf(versionCode).equals(versionPrev)) {
            RebootReceiver.setReminders(getApplicationContext());
            sp.edit().putString(Settings.VERSION_CODE, String.valueOf(versionCode)).apply();
        }
    }


    private void setFabNotes() {
        fab.setImageResource(R.drawable.ic_note_add_white_24dp);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                callQuickNoteActivity();
            }
        });
    }

    private void setFabReminders() {
        fab.setImageResource(R.drawable.ic_alarm_add_white_24dp);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent notification = new Intent(getApplicationContext(), CreateDelayedNote.class);
                startActivity(notification);
            }
        });
    }

    private void setBirthdaysMenu() {
        mainMenu.findItem(R.id.sort).setVisible(true);
        mainMenu.findItem(R.id.sortDaysLeft).setChecked(true);
        mainMenu.findItem(R.id.action_clear_all).setVisible(false);
        mainMenu.findItem(R.id.sync_birthdays).setVisible(true);
        mainMenu.findItem(R.id.clear_all_clips).setVisible(false);
    }

    private void sendEmail(String[] addresses) {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:"));
        intent.putExtra(Intent.EXTRA_EMAIL, addresses);
        intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name));
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }

    private void setBarTitle(ActionBar bar, String title) {
        try {
            bar.setTitle(title);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void showSecondaryFragment(Fragment fragment) {
        FragmentTransaction fta = getSupportFragmentManager().beginTransaction();
        fta.replace(R.id.container, fragment);
        fta.commitAllowingStateLoss();
        if (fab.getVisibility() == View.VISIBLE) {
            fab.setVisibility(View.INVISIBLE);
        }
    }

    private void rateApp() {
        Uri uri = Uri.parse("market://details?id=" + getApplicationContext().getPackageName());
        Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
        goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET |
                Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        try {
            startActivity(goToMarket);
        } catch (ActivityNotFoundException e) {
            startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("http://play.google.com/store/apps/details?id=" + getApplicationContext().getPackageName())));
        }

    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        int item = vpFragment.getViewPager().getCurrentItem();
        outState.putInt("item", item);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onStart() {

        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.container);
        if (currentFragment instanceof ViewPagerFragment) {
            showFirstFragment();
        }

        setCurrentNavigationMenuItemChecked(currentFragment);
        super.onStart();

    }

    private void setCurrentNavigationMenuItemChecked(Fragment currentFragment) {
        if (currentFragment instanceof SMSFragment) {
            navigationView.getMenu().getItem(1).setChecked(true);
        } else if (currentFragment instanceof ClipFragment) {
            navigationView.getMenu().getItem(2).setChecked(true);
        } else if (currentFragment instanceof BirthdayFragment) {
            navigationView.getMenu().getItem(3).setChecked(true);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchItemView = (SearchView) searchItem.getActionView();
        searchItemView.setOnQueryTextListener(this);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        mainMenu = menu;
        menu.findItem(R.id.sort).setVisible(false);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawers();
        } else {
            Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.container);
            if (currentFragment instanceof ViewPagerFragment) {
                super.onBackPressed();
            } else {
                showFirstFragment();
                setVPFragmentMenu();
            }
        }
    }

    private void setVPFragmentMenu() {
        mainMenu.findItem(R.id.sort).setVisible(false);
        mainMenu.findItem(R.id.action_clear_all).setVisible(true);
        mainMenu.findItem(R.id.sync_birthdays).setVisible(false);
        mainMenu.findItem(R.id.clear_all_clips).setVisible(false);
    }

    private void setClipFragmentMenu() {
        mainMenu.findItem(R.id.sort).setVisible(false);
        mainMenu.findItem(R.id.action_clear_all).setVisible(false);
        mainMenu.findItem(R.id.sync_birthdays).setVisible(false);
        mainMenu.findItem(R.id.clear_all_clips).setVisible(true);
    }

    private void showFirstFragment() {
        firstFragmentTransaction();
        fab.setVisibility(View.VISIBLE);
        navigationView.getMenu().getItem(0).setChecked(true);
        try {
            getSupportActionBar().setTitle(getString(R.string.app_name));
        } catch (Exception e) {
        }
    }

    private void firstFragmentTransaction() {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        vpFragment = new ViewPagerFragment();
        Bundle bundle = createFragmentBundle();
        vpFragment.setArguments(bundle);
        ft.replace(R.id.container, vpFragment);
        ft.commitAllowingStateLoss();
    }

    @NonNull
    private Bundle createFragmentBundle() {
        Bundle bundle = new Bundle();
        bundle.putInt("item", pagerItem);
        return bundle;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_search) {

            return true;
        } else if (id == R.id.sync_birthdays) {
            syncBirthdayInfo();
            return true;
        } else if (id == R.id.sortDaysLeft) {
            sortByDaysLeft(item);
            return true;
        } else if (id == R.id.sortNames) {
            sortByNames(item);
            return true;
        } else if (id == R.id.sortAge) {
            sortByAge(item);
            return true;
        } else if (id == R.id.action_clear_all) {
            clearTray();
            return true;
        } else if (id == R.id.clear_all_clips) {
            showClearClipDialog();
            return true;
        } else if (id == android.R.id.home) {
            mDrawerLayout.openDrawer(GravityCompat.START);
        }
        return super.onOptionsItemSelected(item);
    }

    private void showClearClipDialog() {
        new AlertDialog.Builder(MainActivity.this).setTitle(R.string.ask_clear_clip_history)
                .setPositiveButton(R.string.clear, getClearClipDialogListener())
                .setNegativeButton(getString(R.string.cancel), null)
                .create()
                .show();
    }

    @NonNull
    private DialogInterface.OnClickListener getClearClipDialogListener() {
        return new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                try {
                    clearClipHistory();
                } catch (Exception e) {
                }
            }
        };
    }

    private void clearClipHistory() {
        ClipAdapter.getInstance().clearListAll();
    }

    private void syncBirthdayInfo() {
        birthdayFragment.checkReadContactsPermissions();
        mainMenu.findItem(R.id.sortDaysLeft).setChecked(true);
    }

    private void callQuickNoteActivity() {
        Intent quick = new Intent(this, QuickNote.class);
        startActivity(quick);
    }

    private void sortByAge(MenuItem item) {
        birthdayFragment.sortByAge();
        item.setChecked(true);
    }

    private void sortByNames(MenuItem item) {
        birthdayFragment.sortByNames();
        item.setChecked(true);
    }

    private void sortByDaysLeft(MenuItem item) {
        birthdayFragment.sortByDaysLeft();
        item.setChecked(true);
    }


    private void clearTray() {
        nm.cancelAll();

        DBQuick db = new DBQuick(this);
        db.open();
        db.clearQuickTrayAll();
        db.close();
        QuickAdapter.getInstance().clearTrayAll();

    }

    private Fragment getCurrentFragment() {
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.container);
        if (currentFragment != null) {
            return currentFragment;
        }
        return null;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        Fragment fragment = getCurrentFragment();
        if ((fragment != null) && (fragment instanceof SMSFragment)) {
            return smsFragmentSearch(newText);
        } else if ((fragment != null) && (fragment instanceof BirthdayFragment)) {
            return birthdayFragmentSearch(newText);
        } else if ((fragment != null) && (fragment instanceof ClipFragment)) {
            return clipFragmentSearch(newText);
        } else if ((fragment != null) && (fragment instanceof ViewPagerFragment)) {
            int pagerItem = vpFragment.getViewPager().getCurrentItem();
            if (pagerItem == 0) {
                return quickNotesSearch(newText);
            } else if (pagerItem == 1) {
                return delayedNotesSearch(newText);
            }
        }
        return true;
    }

    private boolean clipFragmentSearch(String newText) {
        try {
            List<Clip> clipsAll = clipFragment.getClipListAll();
            List<Clip> clipsFiltered = clipFragment.getClipList();
            clipsFiltered.clear();
            if (newText.length() == 0) {
                clipsFiltered.addAll(clipsAll);
            } else {
                for (Clip x : clipsAll) {
                    if (x.getText().toLowerCase().contains(newText.toLowerCase())) {
                        clipsFiltered.add(x);
                    }
                }
            }
            clipFragment.setClipList(clipsFiltered);
            ClipAdapter.getInstance().notifyDataSetChanged();
            return true;
        } catch (Exception e) {
            return true;
        }
    }

    private boolean delayedNotesSearch(String newText) {
        try {
            List<DelayedNote> notesAll = vpFragment.getDelayedNotes().getAllNotesFromDB();
            List<DelayedNote> notesFiltered = vpFragment.getDelayedNotes().getNotes();
            notesFiltered.clear();
            if (newText.length() == 0) {
                notesFiltered.addAll(notesAll);
            } else {
                for (DelayedNote x : notesAll) {
                    if (x.getTitle().toLowerCase().contains(newText.toLowerCase()) || (x.getText().toLowerCase().contains(newText.toLowerCase()))) {
                        notesFiltered.add(x);
                    }
                }
            }
            vpFragment.refreshDelayed(notesFiltered);
            return true;
        } catch (Exception e) {
            return true;
        }
    }

    private boolean quickNotesSearch(String newText) {
        try {
            List<Note> notesAll = QuickNotesFragment.getAllNotesFromDB(getApplicationContext());
            List<Note> notesFiltered = vpFragment.getQuickNotes().getNotes();
            notesFiltered.clear();
            if (newText.length() == 0) {
                notesFiltered.addAll(notesAll);
            } else {
                for (Note x : notesAll) {
                    if (x.getTitle().toLowerCase().contains(newText.toLowerCase()) || (x.getText().toLowerCase().contains(newText.toLowerCase()))) {
                        notesFiltered.add(x);
                    }
                }
            }
            vpFragment.refreshQuick(notesFiltered);
            return true;
        } catch (Exception e) {
            return true;
        }
    }

    private boolean birthdayFragmentSearch(String newText) {
        try {
            List<Birthday> birthdaysListAll = birthdayFragment.getBirthdaysListAll();
            List<Birthday> birthdaysListFiltered = birthdayFragment.getBirthdaysList();
            birthdaysListFiltered.clear();
            if (newText.length() == 0) {
                birthdaysListFiltered.addAll(birthdaysListAll);
            } else {
                for (Birthday x : birthdaysListAll) {
                    if (x.getName().toLowerCase().contains(newText.toLowerCase())) {
                        birthdaysListFiltered.add(x);
                    }
                }
            }
            birthdayFragment.setBirthdaysList(birthdaysListFiltered);
            BirthdayAdapter.getInstance().notifyDataSetChanged();
            return true;
        } catch (Exception e) {
            return true;
        }
    }

    private boolean smsFragmentSearch(String newText) {
        try {
            List<SMS> smsListAll = smsFragment.getSmsListAll();
            List<SMS> smsListFiltered = smsFragment.getSmsList();
            smsListFiltered.clear();
            if (newText.length() == 0) {
                smsListFiltered.addAll(smsListAll);
            } else {
                for (SMS x : smsListAll) {
                    if (x.getAddress().toLowerCase().contains(newText.toLowerCase()) || (x.getBody().toLowerCase().contains(newText.toLowerCase()))) {
                        smsListFiltered.add(x);
                    }
                }
            }
            smsFragment.setSmsList(smsListFiltered);
            SMSAdapter.getInstance().notifyDataSetChanged();
            return true;
        } catch (Exception e) {
            return true;
        }
    }

    private final ActionMode.Callback callback = new ActionMode.Callback() {

        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            mode.getMenuInflater().inflate(R.menu.action_mode, menu);
            return true;
        }

        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.action_delete:
                    showConfirmDeleteDialog();
                    break;
                case R.id.action_selectAll:
                    selectAll();
                    break;
            }
            return false;
        }

        public void onDestroyActionMode(ActionMode mode) {
            endActionMode();
        }

    };

    private void showConfirmDeleteDialog() {
        try {
            DialogInterface.OnClickListener deleteDialog = getOnDeleteDialogClickListener();
            new AlertDialog.Builder(MainActivity.this).setTitle(R.string.delete_ask)
                    .setPositiveButton(R.string.delete, deleteDialog)
                    .setNegativeButton(R.string.cancel, null)
                    .create().show();
        } catch (Exception e) {
        }
    }

    @NonNull
    private DialogInterface.OnClickListener getOnDeleteDialogClickListener() {
        return new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                try {
                    deleteSelectedOnConfirm();
                } catch (Exception e) {
                }
            }
        };
    }

    private void deleteSelectedOnConfirm() {
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.container);
        if (currentFragment instanceof ViewPagerFragment) {
            if (pagerItem == 0) {
                QuickAdapter.getInstance().deleteSelected();
                actionMode.finish();
            } else if (pagerItem == 1) {
                DelayedAdapter.getInstance().deleteSelected();
                actionMode.finish();
            }
        } else if (currentFragment instanceof ClipFragment) {
            ClipAdapter.getInstance().deleteSelected();
            actionMode.finish();
        }
    }

    private void endActionMode() {
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.container);
        if (currentFragment instanceof ViewPagerFragment) {
            endSelectionForCurrent();
        } else if (currentFragment instanceof ClipFragment) {
            ClipAdapter.getInstance().endSelectionMode();
        }
        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNDEFINED);
        actionMode = null;
    }

    private void endSelectionForCurrent() {
        fab.setVisibility(View.VISIBLE);
        vpFragment.getViewPager().setSwipeLocked(false);
        vpFragment.getTabs().setVisibility(View.VISIBLE);
        if (pagerItem == 0) {
            QuickAdapter.getInstance().endSelectionMode();
        } else if (pagerItem == 1) {
            DelayedAdapter.getInstance().endSelectionMode();
        }
    }

    private void selectAll() {
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.container);
        if (currentFragment instanceof ViewPagerFragment) {
            if (pagerItem == 0) {
                QuickAdapter.getInstance().selectAll();
            } else if (pagerItem == 1) {
                DelayedAdapter.getInstance().selectAll();
            }
        } else if (currentFragment instanceof ClipFragment) {
            ClipAdapter.getInstance().selectAll();
        }
    }

    @Override
    public void startSelection(int i) {
        try {
            selection(i);
        } catch (Exception e) {
        }

    }


    private void selection(int i) {
        if (actionMode == null) {
            Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.container);
            mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
            if (currentFragment instanceof ViewPagerFragment) {
                vpSelection(i);
            } else if (currentFragment instanceof ClipFragment) {
                clipSelection();
            }
        } else {
            actionMode.finish();
        }
    }

    private void clipSelection() {
        if (actionMode == null) {
            actionMode = startSupportActionMode(callback);
            actionMode.setTitle("1");
            ClipAdapter.getInstance().startSelection();
        }
    }

    private void vpSelection(int i) {
        vpFragment.getViewPager().setSwipeLocked(true);
        vpFragment.getTabs().setVisibility(View.GONE);
        pagerItem = i;
        fab.setVisibility(View.GONE);
        if (pagerItem == 0) {
            quickSelection();
        } else if (pagerItem == 1) {
            delayedSelection();
        }
    }


    private void delayedSelection() {
        if (actionMode == null) {
            DelayedAdapter.getInstance().startSelection();
            actionMode = startSupportActionMode(callback);
            actionMode.setTitle("1");
        }
    }

    private void quickSelection() {
        if (actionMode == null) {
            QuickAdapter.getInstance().startSelection();
            actionMode = startSupportActionMode(callback);
            actionMode.setTitle("1");
        }
    }


    @Override
    public void selectedItemsCount(int i) {
        if (actionMode != null) {
            actionMode.setTitle(String.valueOf(i));
        }
    }


    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        pagerItem = position;
        switch (position) {
            case 0:
                setFabNotes();
                break;
            case 1:
                setFabReminders();
                break;
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    private BroadcastReceiver removeTrayReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                int id = intent.getIntExtra("id", 0);
                QuickAdapter.getInstance().refreshSingle(id);
            } catch (Exception e) {
            }
        }

    };

    static void refreshQuickTrayIcon(Context context, int id) {
        Intent intent = new Intent("refreshTrayIcons");
        intent.putExtra("id", id);
        context.sendBroadcast(intent);
    }

}
