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
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    final private int QUICK = 1;
    final private int DELAYED = 2;
    private DrawerLayout mDrawerLayout;
    private NotificationManager nm;
    private int pagerItem = 0;
    private ViewPagerFragment vp;
    private NavigationView navigationView;
    private FloatingActionButton fab;
    private BirthdayFragment birthdayFragment;
    private Menu mainMenu;


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
                                mainMenu.findItem(R.id.sort).setVisible(false);
                                break;
                            case R.id.messages:
                                showSecondaryFragment(new SMSFragment());
                                setBarTitle(supportActionBar, getString(R.string.messages));
                                mainMenu.findItem(R.id.sort).setVisible(false);
                                break;
                            case R.id.birthdays:
                                birthdayFragment = new BirthdayFragment();
                                showSecondaryFragment(birthdayFragment);
                                setBarTitle(supportActionBar, getString(R.string.birthdays));
                                mainMenu.findItem(R.id.sort).setVisible(true);
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
        int item = vp.getViewPager().getCurrentItem();
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
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.container);
        if (currentFragment instanceof SMSFragment) {
            showFirstFragment();
            mainMenu.findItem(R.id.sort).setVisible(false);
        } else if (currentFragment instanceof BirthdayFragment) {
            showFirstFragment();
            mainMenu.findItem(R.id.sort).setVisible(false);
        } else if (currentFragment instanceof ViewPagerFragment) {
            super.onBackPressed();
        }

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
        vp = new ViewPagerFragment();
        Bundle bundle = createFragmentBundle();
        vp.setArguments(bundle);
        ft.replace(R.id.container, vp);
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
        if (id == R.id.sortDaysLeft) {
            birthdayFragment.sortByDaysLeft();
            item.setChecked(true);
            return true;
        } else if (id == R.id.sortNames) {
            birthdayFragment.sortByNames();
            item.setChecked(true);
            return true;
        } else if (id == R.id.sortAge) {
            birthdayFragment.sortByAge();
            item.setChecked(true);
            return true;
        } else if (id == R.id.action_clear_all) {
            clear();
            return true;
        } else if (id == R.id.quick_note) {
            Intent quick = new Intent(this, QuickNote.class);
            startActivityForResult(quick, QUICK);
        } else if (id == android.R.id.home) {
            mDrawerLayout.openDrawer(GravityCompat.START);
        }
        return super.onOptionsItemSelected(item);
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

    private void clear() {
        nm.cancelAll();
    }


}
