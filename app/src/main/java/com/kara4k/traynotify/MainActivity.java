package com.kara4k.traynotify;

import android.app.NotificationManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    final private int DELAYED = 1;
    final private int QUICK = 2;
    private DrawerLayout mDrawerLayout;
    private NotificationManager nm;
    private int pagerItem = 0;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setHomeAsUpIndicator(R.drawable.ic_menu_white_24dp);
            supportActionBar.setDisplayHomeAsUpEnabled(true);
        }

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer);

        nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    // This method will trigger on item Click of navigation menu
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        Fragment fragment;
                        switch (menuItem.getItemId()) {
                            case R.id.add_note:
                                fragment = new ViewPagerFragment();
                                Bundle bundle = new Bundle();
                                Log.e("MAIN", String.valueOf(pagerItem));
                                bundle.putInt("item", pagerItem);
                                fragment.setArguments(bundle);
                                break;
                            case R.id.new_delayed:
                                fragment = new QuickNotesFragment();
                                break;
                            default:
                                fragment = new NotifyList();
                        }
                        menuItem.setChecked(true);
                        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                        ft.replace(R.id.container, fragment);
                        ft.commit();
                        // TODO: handle navigation
                        // Closing drawer on item click
                        mDrawerLayout.closeDrawers();
                        return true;
                    }
                });




        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent notification = new Intent(getApplicationContext(), CreateDelayedNote.class);
                startActivityForResult(notification,DELAYED);
            }
        });
    }

    @Override
    protected void onStart() {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ViewPagerFragment vp = new ViewPagerFragment();
        Bundle bundle = new Bundle();
        bundle.putInt("item", pagerItem);
        vp.setArguments(bundle);
        ft.replace(R.id.container, vp);
        ft.commitAllowingStateLoss();
        super.onStart();

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_clear_all) {
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
                pagerItem = 1;
                break;
            case DELAYED:
                pagerItem = 0;
                break;
        }
    }

    public void clear() {
        nm.cancelAll();
    }


}
