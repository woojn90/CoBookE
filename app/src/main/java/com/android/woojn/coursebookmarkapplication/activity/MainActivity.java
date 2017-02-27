package com.android.woojn.coursebookmarkapplication.activity;

import static com.android.woojn.coursebookmarkapplication.Constants.DEFAULT_FOLDER_ID;
import static com.android.woojn.coursebookmarkapplication.Constants.KEY_FOLDER_ID;
import static com.android.woojn.coursebookmarkapplication.Constants.PAGE_COURSE;
import static com.android.woojn.coursebookmarkapplication.Constants.PAGE_ITEM;
import static com.android.woojn.coursebookmarkapplication.fragment.ItemFragment.currentFolderId;
import static com.android.woojn.coursebookmarkapplication.fragment.ItemFragment.isFabOpen;
import static com.android.woojn.coursebookmarkapplication.util.RealmDbUtility.insertDefaultFolderIfNeeded;
import static com.android.woojn.coursebookmarkapplication.util.RealmDbUtility.insertInitialData;
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

        int tabIndex = Integer.parseInt(mSharedPreferences.getString(getString(R.string.pref_key_tab_index), PAGE_COURSE + ""));
        mTabLayout.getTabAt(tabIndex).select();
        initializeFabByTabPosition(mTabLayout.getSelectedTabPosition());

        int firstRun = mSharedPreferences.getInt(getString(R.string.pref_key_zero_means_first_run), 0);
        if (firstRun == 0) {
            showOnboarderActivity();
            SharedPreferences.Editor editor = mSharedPreferences.edit();
            editor.putInt(getString(R.string.pref_key_zero_means_first_run), 1);
            editor.apply();
            insertInitialData(this);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (getIntent() != null && getIntent().hasExtra(KEY_FOLDER_ID)) {
            mTabLayout.getTabAt(PAGE_ITEM).select();
        }
    }

    @Override
    protected void onPause() {
        if (isFabOpen) animateFabItemsClose();
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
        OnboardingPage page1 = new OnboardingPage("<기능 소개>\n검색어 지정", "코스 별, 그룹 별로\n검색어를 지정할 수 있습니다.", R.drawable.onboarder1);
        OnboardingPage page2 = new OnboardingPage("<기능 소개>\n자동 검색 및 저장", "1. 지정해둔 검색어로 자동 검색합니다.\n2. 원하는 페이지를 저장합니다.", R.drawable.onboarder2);
        OnboardingPage page3 = new OnboardingPage("<기능 소개>\n공유 받기", "다른 어플에서 보던 내용도\n공유 기능을 사용하여 손쉽게 저장합니다.", R.drawable.onboarder3);
        OnboardingPage page4 = new OnboardingPage("<기능 소개>\n공유 하기", "저장해둔 항목들은\n깔끔하게 정리하여 공유할 수 있습니다.", R.drawable.onboarder4);
        OnboardingPage page5 = new OnboardingPage("[사용법]\n항목 수정", "연달아 터치하여\n항목을 수정합니다.", R.drawable.onboarder5);
        OnboardingPage page6 = new OnboardingPage("[사용법]\n항목 이동", "길게 터치하여\n항목을 이동합니다.", R.drawable.onboarder6);
        OnboardingPage page7 = new OnboardingPage(null, "Course Bookmark with Efficiency\n효율적인 북마크 관리", R.drawable.icon, getString(R.string.string_start));

        pages.add(page1);
        pages.add(page2);
        pages.add(page3);
        pages.add(page4);
        pages.add(page5);
        pages.add(page6);
        pages.add(page7);

        for (OnboardingPage page : pages) {
            page.setTitleTextColor(android.R.color.black);
            page.setBodyTextColor(android.R.color.black);
        }

        Bundle bundle = OnboardingActivity.newBundleColorBackground(R.color.colorBackgroundOnboarder, pages);
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