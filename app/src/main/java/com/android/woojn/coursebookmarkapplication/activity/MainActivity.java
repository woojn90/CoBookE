package com.android.woojn.coursebookmarkapplication.activity;

import static com.android.woojn.coursebookmarkapplication.Constants.DEFAULT_SECTION_ID;
import static com.android.woojn.coursebookmarkapplication.Constants.KEY_COURSE_ID;
import static com.android.woojn.coursebookmarkapplication.Constants.KEY_REQUEST_WEB_ACTIVITY;
import static com.android.woojn.coursebookmarkapplication.Constants.KEY_SECTION_ID;
import static com.android.woojn.coursebookmarkapplication.Constants.KEY_STRING_URL;
import static com.android.woojn.coursebookmarkapplication.Constants.PAGE_COURSE;
import static com.android.woojn.coursebookmarkapplication.Constants.PAGE_ITEM;
import static com.android.woojn.coursebookmarkapplication.Constants.REQUEST_WEB_ACTIVITY_WITH_SAVE;
import static com.android.woojn.coursebookmarkapplication.util.RealmDbUtility.getNewIdByClass;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.EditText;

import com.android.woojn.coursebookmarkapplication.R;
import com.android.woojn.coursebookmarkapplication.adapter.PagerAdapter;
import com.android.woojn.coursebookmarkapplication.model.Course;
import com.android.woojn.coursebookmarkapplication.model.Folder;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.realm.Realm;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.tabs)
    protected TabLayout mTabLayout;
    @BindView(R.id.view_pager)
    protected ViewPager mViewPager;
    @BindView(R.id.fab_for_course_fragment)
    protected FloatingActionButton mFabForCourseFragment;
    @BindView(R.id.fab_for_item_fragment)
    protected FloatingActionButton mFabForItemFragment;
    @BindView(R.id.fab_toggle_grid)
    protected FloatingActionButton mFabToggleGrid;
    @BindView(R.id.fab_insert_folder)
    protected FloatingActionButton mFabInsertFolder;
    @BindView(R.id.fab_search_browser)
    protected FloatingActionButton mFabSearchBrowser;

    private Realm mRealm;
    private SharedPreferences mSharedPreferences;

    private boolean mIsFabOpen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        mTabLayout.addTab(mTabLayout.newTab().setText(R.string.string_course));
        mTabLayout.addTab(mTabLayout.newTab().setText(R.string.string_item));

        PagerAdapter pagerAdapter = new PagerAdapter(getSupportFragmentManager(), mTabLayout.getTabCount());
        mViewPager.setAdapter(pagerAdapter);
        mTabLayout.setupWithViewPager(mViewPager);
        mTabLayout.getTabAt(PAGE_COURSE).setText(R.string.string_course);
        mTabLayout.getTabAt(PAGE_ITEM).setText(R.string.string_item);

        mRealm = Realm.getDefaultInstance();

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        int tabIndex = Integer.parseInt(mSharedPreferences.getString(getString(R.string.pref_key_tab_index), PAGE_COURSE + ""));
        mTabLayout.getTabAt(tabIndex).select();

        mTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch (tab.getPosition()) {
                    case PAGE_COURSE:
                        mFabForCourseFragment.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_open));
                        mFabForCourseFragment.setClickable(true);
                        mFabForItemFragment.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_close));
                        mFabForItemFragment.setClickable(false);
                        if (mIsFabOpen) {
                            animateFabInItemTab();
                        }
                        break;
                    case PAGE_ITEM:
                        mFabForCourseFragment.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_close));
                        mFabForCourseFragment.setClickable(false);
                        mFabForItemFragment.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_open));
                        mFabForItemFragment.setClickable(true);
                        break;
                }
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

    @OnClick(R.id.fab_for_course_fragment)
    protected void onClickFabForCourseFragment() {
        showCourseInsertDialog();
    }

    @OnClick(R.id.fab_for_item_fragment)
    protected void onClickFabForItemFragment() {
        animateFabInItemTab();
    }

    @OnClick(R.id.fab_insert_folder)
    protected void onClickFabInsertFolder() {
        int newFolderId = getNewIdByClass(Folder.class);
        mRealm.beginTransaction();
        Folder folder = mRealm.createObject(Folder.class, newFolderId);
        folder.setTitle("New Folder");
        mRealm.commitTransaction();
        animateFabInItemTab();
    }

    @OnClick(R.id.fab_search_browser)
    protected void onClickFabSearchBrowser() {
        showDefaultWebPage();
        animateFabInItemTab();
    }

    @OnClick(R.id.fab_toggle_grid)
    protected void onClickFabToggleGrid() {
        int currentNumberOfColumn = mSharedPreferences.getInt(getString(R.string.pref_key_item_number_of_grid_column), 2);

        if (currentNumberOfColumn == 2) {
            SharedPreferences.Editor editor = mSharedPreferences.edit();
            editor.putInt(getString(R.string.pref_key_item_number_of_grid_column), 3);
            editor.apply();
        } else {
            SharedPreferences.Editor editor = mSharedPreferences.edit();
            editor.putInt(getString(R.string.pref_key_item_number_of_grid_column), 2);
            editor.apply();
        }
        animateFabInItemTab();
    }

    private void animateFabInItemTab() {
        if (mIsFabOpen) {
            mFabForItemFragment.startAnimation(AnimationUtils.loadAnimation(this, R.anim.rotate_backward));
            mFabToggleGrid.startAnimation(AnimationUtils.loadAnimation(this, R.anim.fab_close));
            mFabInsertFolder.startAnimation(AnimationUtils.loadAnimation(this, R.anim.fab_close));
            mFabSearchBrowser.startAnimation(AnimationUtils.loadAnimation(this, R.anim.fab_close));
            mFabToggleGrid.setClickable(false);
            mFabInsertFolder.setClickable(false);
            mFabSearchBrowser.setClickable(false);
            mIsFabOpen = false;
        } else {
            mFabForItemFragment.startAnimation(AnimationUtils.loadAnimation(this, R.anim.rotate_forward));
            mFabToggleGrid.startAnimation(AnimationUtils.loadAnimation(this, R.anim.fab_open));
            mFabInsertFolder.startAnimation(AnimationUtils.loadAnimation(this, R.anim.fab_open));
            mFabSearchBrowser.startAnimation(AnimationUtils.loadAnimation(this, R.anim.fab_open));
            mFabToggleGrid.setClickable(true);
            mFabInsertFolder.setClickable(true);
            mFabSearchBrowser.setClickable(true);
            mIsFabOpen = true;
        }
    }

    private void showCourseInsertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final View dialogView = getLayoutInflater().inflate(R.layout.dialog_course, null);
        builder.setView(dialogView);
        final EditText editTextTitle = (EditText) dialogView.findViewById(R.id.et_course_title);
        final EditText editTextSearchWord = (EditText) dialogView.findViewById(R.id.et_course_search_word);
        final EditText editTextDesc = (EditText) dialogView.findViewById(R.id.et_course_desc);

        builder.setTitle(R.string.string_course_info);
        builder.setNegativeButton(R.string.string_cancel, null);
        builder.setPositiveButton(R.string.string_register, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String title = editTextTitle.getText().toString();
                String searchWord = editTextSearchWord.getText().toString();
                String Desc = editTextDesc.getText().toString();

                int newCourseId = getNewIdByClass(Course.class);

                Intent insertIntent = new Intent(MainActivity.this, CourseActivity.class);
                insertIntent.putExtra(KEY_COURSE_ID, newCourseId);
                startActivity(insertIntent);

                mRealm.beginTransaction();
                Course course = mRealm.createObject(Course.class, newCourseId);
                course.setTitle(title);
                course.setSearchWord(searchWord);
                course.setDesc(Desc);
                mRealm.commitTransaction();
            }
        });

        final AlertDialog alertDialog = builder.create();
        alertDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        alertDialog.show();
        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
        editTextTitle.requestFocus();

        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Do nothing
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String afterTitle = editTextTitle.getText().toString();
                String afterSearchWord = editTextSearchWord.getText().toString();

                if (!afterTitle.trim().isEmpty() && !afterSearchWord.trim().isEmpty()) {
                    alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                } else {
                    alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                }
            }
            @Override
            public void afterTextChanged(Editable s) {
                // Do nothing
            }
        };
        editTextTitle.addTextChangedListener(textWatcher);
        editTextSearchWord.addTextChangedListener(textWatcher);
    }

    private void showDefaultWebPage() {
        Intent webIntent = new Intent(this, WebActivity.class);
        webIntent.putExtra(KEY_REQUEST_WEB_ACTIVITY, REQUEST_WEB_ACTIVITY_WITH_SAVE);
        String stringUrl = mSharedPreferences.getString(getString(R.string.pref_key_home_page)
                , getString(R.string.pref_value_home_page_naver));
        webIntent.putExtra(KEY_STRING_URL, stringUrl);
        webIntent.putExtra(KEY_SECTION_ID, DEFAULT_SECTION_ID);
        startActivity(webIntent);
    }
}