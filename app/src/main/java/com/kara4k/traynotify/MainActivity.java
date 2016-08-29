package com.kara4k.traynotify;

import android.app.NotificationManager;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.util.List;

public class MainActivity extends AppCompatActivity implements SearchView.OnQueryTextListener {

    final private int QUICK = 1;
    final private int DELAYED = 2;
    private DrawerLayout mDrawerLayout;
    private NotificationManager nm;
    private int pagerItem = 0;
    private ViewPagerFragment vpFragment;
    private NavigationView navigationView;
    private FloatingActionButton fab;
    private BirthdayFragment birthdayFragment;
    private Menu mainMenu;
    private SMSFragment smsFragment;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
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
                            case R.id.birthdays:
                                birthdayFragment = new BirthdayFragment();
                                showSecondaryFragment(birthdayFragment);
                                setBarTitle(supportActionBar, getString(R.string.birthdays));
                                setBirthdaysMenu();
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
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent notification = new Intent(getApplicationContext(), CreateDelayedNote.class);
                startActivityForResult(notification, DELAYED);
            }
        });

        showFirstFragment();
    }

    private void setBirthdaysMenu() {
        mainMenu.findItem(R.id.sort).setVisible(true);
        mainMenu.findItem(R.id.sortDaysLeft).setChecked(true);
        mainMenu.findItem(R.id.quick_note).setVisible(false);
        mainMenu.findItem(R.id.action_clear_all).setVisible(false);
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
        fab.setVisibility(View.INVISIBLE);
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
        super.onStart();

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
            if (currentFragment instanceof SMSFragment) {
                showFirstFragment();
                setVPFragmentMenu();
            } else if (currentFragment instanceof BirthdayFragment) {
                showFirstFragment();
                setVPFragmentMenu();
            } else if (currentFragment instanceof ViewPagerFragment) {
                super.onBackPressed();
            }
        }
    }

    private void setVPFragmentMenu() {
        mainMenu.findItem(R.id.sort).setVisible(false);
        mainMenu.findItem(R.id.quick_note).setVisible(true);
        mainMenu.findItem(R.id.action_clear_all).setVisible(true);
    }

    private void showFirstFragment() {
        firstFragmentTransaction();
        fab.setVisibility(View.VISIBLE);
        navigationView.getMenu().getItem(0).setChecked(true);
        try {
            getSupportActionBar().setTitle(getString(R.string.app_name));
        } catch (Exception e) {
            e.printStackTrace();
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
        } else if (id == R.id.quick_note) {
            callQuickNoteActivity();
        } else if (id == android.R.id.home) {
            mDrawerLayout.openDrawer(GravityCompat.START);
        }
        return super.onOptionsItemSelected(item);
    }

    private void callQuickNoteActivity() {
        Intent quick = new Intent(this, QuickNote.class);
        startActivityForResult(quick, QUICK);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case QUICK:
                pagerItem = 0;
                break;
            case DELAYED:
                pagerItem = 1;
                break;
        }
    }

    private void clearTray() {
        nm.cancelAll();

        DBQuick db = new DBQuick(this);
        db.open();
        db.clearQuickTrayAll();
        db.close();
        vpFragment.updateTrayRemoved();
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
            vpFragment.updateDelayed();
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
            vpFragment.updateQuick();
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
                    if (x.getAddress().toLowerCase().contains(newText.toLowerCase()) || (x.getBody().toLowerCase().contains(newText.toLowerCase()))) {   // TODO: 03.07.2016 gettext!=null
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
}
