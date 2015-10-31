package corp.seedling.news.wordy;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import corp.seedling.news.wordy.sync.MySyncAdapter;


    public class MainActivity extends AppCompatActivity implements MainActivityFragment.Callback, ClearableEditText.EditTextCallback{

    @Bind (R.id.my_awesome_toolbar) Toolbar toolbar;
    @Bind (R.id.drawer_layout) DrawerLayout mDrawerLayout;
    @Bind (R.id.left_drawer) ListView mDrawerList;
    @Bind (R.id.ad_view) AdView mAdView;

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String DETAILFRAGMENT_TAG = "DFTAG";
    String mKeyword = null;
    public final static int REQUEST_CODE = 7;
    public final static int RESULT_CODE_BACK_PRESSED = 8;
    private ActionBarDrawerToggle mDrawerToggle;
    // slide menu items
    private String[] navMenuTitles;
    private TypedArray navMenuIcons;
    private ArrayList<NavDrawerItem> navDrawerItems;
    private NavDrawerListAdapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.e(TAG, "********* oncreate enter *********");
        super.onCreate(savedInstanceState);

        if (getIntent().hasExtra("keyword")){ //This cud come only frm fav screen OR widget :)
            mKeyword = getIntent().getStringExtra("keyword");
            Log.e(TAG, "fav word RXD = " + mKeyword);
        }
        else if (savedInstanceState != null){
            mKeyword = savedInstanceState.getString("keyword" , "Welcome");
        }
        else{
            mKeyword = "Welcome";

        }

        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        //Nav Drawer
        mDrawerLayout.setDrawerShadow(R.mipmap.drawer_shadow, GravityCompat.START);
        // load slide menu items
        navMenuTitles = getResources().getStringArray(R.array.nav_drawer_items);

        // nav drawer icons from resources
        navMenuIcons = getResources().obtainTypedArray(R.array.nav_drawer_icons);
        navDrawerItems = new ArrayList<NavDrawerItem>();
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[0], navMenuIcons.getResourceId(0, -1)));
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[1], navMenuIcons.getResourceId(1, -1)));

        // Recycle the typed array
        navMenuIcons.recycle();

        // setting the nav drawer list adapter
        adapter = new NavDrawerListAdapter(getApplicationContext(),
                navDrawerItems);

        // Set the adapter for the list view
        mDrawerList.setAdapter(adapter);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

        mDrawerToggle = new ActionBarDrawerToggle(
                this,
                mDrawerLayout,
                R.string.drawer_open,
                R.string.drawer_close
        ) {
            public void onDrawerClosed(View view) {
            }
            public void onDrawerOpened(View drawerView) {
                hideKeyboard();
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);


        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice("BE0B283D9DB35079AECC55005262BED2")
                .build();

        mAdView.loadAd(adRequest);


        MySyncAdapter.initializeSyncAdapter(this);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        Log.e(TAG, "********* onSaveInstanceState ENTER*********");
        super.onSaveInstanceState(outState);
        outState.putString("keyword", mKeyword);
    }

    @Override
    public void onItemSelected(String webUrl) {


        if (findViewById(R.id.fragment_detail) == null) {

            Log.e(TAG, "********* onItemSelected: mtwopane FALSE:launch detail act*********");
            Intent intent = new Intent(this, DetailActivity.class)
                    .putExtra("url", webUrl)
                    .putExtra("keyword", mKeyword)
                    ;
            startActivity(intent);

        } else {
            DetailFragment detailFragment = (DetailFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.fragment_detail);
            // DisplayFragment (Fragment B) is in the layout (tablet layout),
            // so tell the fragment to update
            Log.e(TAG, "********* onItemSelected: mtwopane TRUE:update detail frag*********");
            detailFragment.updateContent(webUrl, mKeyword);
        }
    }

    /* The click listner for ListView in the navigation drawer */
    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            selectItem(position);
        }
    }

    public void hideKeyboard() {
        Log.e(TAG, "HIDE KEYPAD");
        try {
            InputMethodManager in = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            View view = findViewById(android.R.id.content);
            in.hideSoftInputFromWindow(view.getWindowToken(),
                    InputMethodManager.HIDE_NOT_ALWAYS);
        } catch (Throwable e) {
            // handle any un-expected UI issue if happened
        }

    }

    private void selectItem(int position) {
        switch (position){
            case 0:
                startActivityForResult(new Intent(this, FavActivity.class), REQUEST_CODE, null);
                break;
            case 1:
                startActivity(new Intent(this, SettingsActivity.class));
                break;
        }
        mDrawerList.setItemChecked(position, true);
        mDrawerLayout.closeDrawer(mDrawerList);
    }

    /**
     * When using the ActionBarDrawerToggle, you must call it during
     * onPostCreate() and onConfigurationChanged()...
     */

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggls
        mDrawerToggle.onConfigurationChanged(newConfig);
    }


    @Override
    protected void onPause() {
        super.onPause();
        Log.e(TAG, "********* onpause enter *********");
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.e(TAG, "********* onstart enter *********");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.e(TAG, "********* onresumeenter *********");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.e(TAG, "********* onstop enter *********");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.e(TAG, "********* ondestroy enter *********");
    }

    public String getKeyword(){
        return mKeyword;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == REQUEST_CODE) {

            if (resultCode == RESULT_OK) {
                this.finish();
            }
            else if (resultCode == RESULT_CODE_BACK_PRESSED) {
                ((MainActivityFragment)getSupportFragmentManager().findFragmentById(R.id.fragment_main)).putFavFeature();
            }
        }
    }

    @Override
    public void onWordEntered(String word) {
        this.mKeyword = word;
        ((MainActivityFragment)getSupportFragmentManager().findFragmentById(R.id.fragment_main)).startNewAsyncTask(word);
    }

}
