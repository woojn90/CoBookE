package com.android.woojn.coursebookmarkapplication.activity;

import static com.android.woojn.coursebookmarkapplication.Constants.DEFAULT_FOLDER_ID;
import static com.android.woojn.coursebookmarkapplication.Constants.PAGE_COURSE;
import static com.android.woojn.coursebookmarkapplication.Constants.PAGE_ITEM;
import static com.android.woojn.coursebookmarkapplication.fragment.ItemFragment.currentFolderId;
import static com.android.woojn.coursebookmarkapplication.fragment.ItemFragment.isFabOpen;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.animation.AnimationUtils;

import com.android.woojn.coursebookmarkapplication.R;
import com.android.woojn.coursebookmarkapplication.adapter.PagerAdapter;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.tabs)
    protected TabLayout mTabLayout;
    @BindView(R.id.view_pager)
    protected ViewPager mViewPager;
    @BindView(R.id.fab_insert_course)
    protected FloatingActionButton mFabInsertCourse;
    @BindView(R.id.fab_expand_items)
    protected FloatingActionButton mFabExpandItems;
    @BindView(R.id.fab_toggle_grid)
    protected FloatingActionButton mFabToggleGrid;
    @BindView(R.id.fab_insert_folder)
    protected FloatingActionButton mFabInsertFolder;
    @BindView(R.id.fab_browser)
    protected FloatingActionButton mFabBrowser;

    private onKeyBackPressedListener mOnKeyBackPressedListener;

    public interface onKeyBackPressedListener {
        void onBackPressed();
    }

    public void setOnKeyBackPressedListener(onKeyBackPressedListener listener) {
        mOnKeyBackPressedListener = listener;
    }

    @Override
    public void onBackPressed() {
        if (mOnKeyBackPressedListener != null && currentFolderId != DEFAULT_FOLDER_ID) {
            mOnKeyBackPressedListener.onBackPressed();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        mTabLayout.addTab(mTabLayout.newTab());
        mTabLayout.addTab(mTabLayout.newTab());

        PagerAdapter pagerAdapter = new PagerAdapter(getSupportFragmentManager(), mTabLayout.getTabCount());
        mViewPager.setAdapter(pagerAdapter);
        mTabLayout.setupWithViewPager(mViewPager);
        mTabLayout.getTabAt(PAGE_COURSE).setText(R.string.string_course);
        mTabLayout.getTabAt(PAGE_ITEM).setText(R.string.string_item);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        int tabIndex = Integer.parseInt(sharedPreferences.getString(getString(R.string.pref_key_tab_index), PAGE_COURSE + ""));
        mTabLayout.getTabAt(tabIndex).select();
        selectFabByTabPosition(tabIndex);

        mTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                selectFabByTabPosition(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                // Do nothing
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                // Do nothing
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_setting) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void selectFabByTabPosition(int tabPosition) {
        switch (tabPosition) {
            case PAGE_COURSE:
                if (isFabOpen) {
                    animateFabItemsClose();
                }
                mFabInsertCourse.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_open));
                mFabInsertCourse.setClickable(true);
                mFabExpandItems.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_close));
                mFabExpandItems.setClickable(false);
                break;
            case PAGE_ITEM:
                mFabInsertCourse.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_close));
                mFabInsertCourse.setClickable(false);
                mFabExpandItems.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_open));
                mFabExpandItems.setClickable(true);
                break;
        }
    }

    private void animateFabItemsClose() {
        mFabExpandItems.startAnimation(AnimationUtils.loadAnimation(this, R.anim.rotate_backward));
        mFabToggleGrid.startAnimation(AnimationUtils.loadAnimation(this, R.anim.fab_close));
        mFabInsertFolder.startAnimation(AnimationUtils.loadAnimation(this, R.anim.fab_close));
        mFabBrowser.startAnimation(AnimationUtils.loadAnimation(this, R.anim.fab_close));
        mFabToggleGrid.setClickable(false);
        mFabInsertFolder.setClickable(false);
        mFabBrowser.setClickable(false);
        isFabOpen = false;
    }
}