package com.android.woojn.coursebookmarkapplication.activity;

import static com.android.woojn.coursebookmarkapplication.Constants.DEFAULT_FOLDER_ID;
import static com.android.woojn.coursebookmarkapplication.Constants.KEY_FOLDER_ID;
import static com.android.woojn.coursebookmarkapplication.Constants.PAGE_COURSE;
import static com.android.woojn.coursebookmarkapplication.Constants.PAGE_ITEM;
import static com.android.woojn.coursebookmarkapplication.fragment.ItemFragment.currentFolderId;
import static com.android.woojn.coursebookmarkapplication.fragment.ItemFragment.isFabOpen;
import static com.android.woojn.coursebookmarkapplication.util.RealmDbUtility.insertDefaultFolderIfNeeded;
import static com.android.woojn.coursebookmarkapplication.util.SettingUtility.toggleGridColumn;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import com.android.woojn.coursebookmarkapplication.R;
import com.android.woojn.coursebookmarkapplication.adapter.PagerAdapter;

import com.jrejaud.onboarder.OnboardingActivity;
import com.jrejaud.onboarder.OnboardingPage;

import java.util.LinkedList;
import java.util.List;

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
    @BindView(R.id.fab_insert_folder)
    protected FloatingActionButton mFabInsertFolder;
    @BindView(R.id.fab_browser)
    protected FloatingActionButton mFabBrowser;

    public static Menu menuMain;

    private SharedPreferences mSharedPreferences;
    private onKeyBackPressedListener mOnKeyBackPressedListener;
    private boolean isBackToExitPressedOnce = false;

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
            if (isBackToExitPressedOnce) {
                super.onBackPressed();
                return;
            }
            isBackToExitPressedOnce = true;
            Toast.makeText(this, R.string.msg_exit_twice, Toast.LENGTH_SHORT).show();

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    isBackToExitPressedOnce = false;
                }
            }, 2000);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        insertDefaultFolderIfNeeded(this);

        mTabLayout.addTab(mTabLayout.newTab());
        mTabLayout.addTab(mTabLayout.newTab());

        PagerAdapter pagerAdapter = new PagerAdapter(getSupportFragmentManager(), mTabLayout.getTabCount());
        mViewPager.setAdapter(pagerAdapter);
        mTabLayout.setupWithViewPager(mViewPager);
        mTabLayout.getTabAt(PAGE_COURSE).setText(R.string.string_course);
        mTabLayout.getTabAt(PAGE_ITEM).setText(R.string.string_item);

        mTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                toggleFabAndMenuItemByTabPosition(tab.getPosition());
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

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        if (getIntent() != null && getIntent().hasExtra(KEY_FOLDER_ID)) {
            mTabLayout.getTabAt(PAGE_ITEM).select();
            // TODO: 해당 폴더로 이동하게 (ArrayList<Integer> foldedIds 처리가 복잡하여 보류)
        } else {
            int tabIndex = Integer.parseInt(mSharedPreferences.getString(getString(R.string.pref_key_tab_index), PAGE_COURSE + ""));
            mTabLayout.getTabAt(tabIndex).select();
        }
        initializeFabByTabPosition(mTabLayout.getSelectedTabPosition());

        int firstRun = mSharedPreferences.getInt(getString(R.string.pref_key_zero_means_first_run), 0);
        if (firstRun == 0) {
            showOnboarderActivity();
            SharedPreferences.Editor editor = mSharedPreferences.edit();
            editor.putInt(getString(R.string.pref_key_zero_means_first_run), 1);
            editor.apply();
        }
    }

    @Override
    protected void onPause() {
        if (isFabOpen) {
            animateFabItemsClose();
        }
        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        menuMain = menu;
        toggleFabAndMenuItemByTabPosition(mTabLayout.getSelectedTabPosition());
        initGridColumnIcon();
        return true;
    }

    private void initGridColumnIcon() {
        int beforeNumberOfColumn = mSharedPreferences.getInt(getString(R.string.pref_key_item_number_of_grid_column), 2);

        if (beforeNumberOfColumn == 2) {
            menuMain.findItem(R.id.action_change_grid).setIcon(R.drawable.ic_fab_grid_3x3);
        } else {
            menuMain.findItem(R.id.action_change_grid).setIcon(R.drawable.ic_fab_grid_2x2);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_change_grid:
                toggleGridColumn(this, item);
                break;
            case R.id.action_help:
                showOnboarderActivity();
                return true;
            case R.id.action_setting:
                Intent settingIntent = new Intent(this, SettingsActivity.class);
                startActivity(settingIntent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showOnboarderActivity() {
        List<OnboardingPage> pages = new LinkedList<>();
        OnboardingPage page1 = new OnboardingPage(null, "desc", R.drawable.splash);
        OnboardingPage page2 = new OnboardingPage(null, "desc2", R.drawable.splash);
        OnboardingPage page3 = new OnboardingPage(null, "desc3", R.drawable.ic_ui_home, "시작하기");
        pages.add(page1);
        pages.add(page2);
        pages.add(page3);

        Bundle bundle = OnboardingActivity.newBundleColorBackground(R.color.colorAccent, pages);
        Intent intent = new Intent(this, GuideActivity.class);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    private void initializeFabByTabPosition(int tabPosition) {
        switch (tabPosition) {
            case PAGE_COURSE:
                mFabExpandItems.hide();
                mFabExpandItems.setClickable(false);
                break;
            case PAGE_ITEM:
                mFabInsertCourse.hide();
                mFabInsertCourse.setClickable(false);
                break;
        }
    }

    private void toggleFabAndMenuItemByTabPosition(int tabPosition) {
        switch (tabPosition) {
            case PAGE_COURSE:
                if (isFabOpen) {
                    animateFabItemsClose();
                }
                mFabInsertCourse.show();
                mFabInsertCourse.setClickable(true);
                mFabExpandItems.hide();
                mFabExpandItems.setClickable(false);
                if (menuMain != null) {
                    menuMain.findItem(R.id.action_change_grid).setVisible(false);
                }
                break;
            case PAGE_ITEM:
                mFabExpandItems.show();
                mFabExpandItems.setClickable(true);
                mFabInsertCourse.hide();
                mFabInsertCourse.setClickable(false);
                if (menuMain != null) {
                    menuMain.findItem(R.id.action_change_grid).setVisible(true);
                }
                break;
        }
    }

    private void animateFabItemsClose() {
        mFabExpandItems.startAnimation(AnimationUtils.loadAnimation(this, R.anim.rotate_backward));
        mFabInsertFolder.hide();
        mFabBrowser.hide();
        isFabOpen = false;
    }
}