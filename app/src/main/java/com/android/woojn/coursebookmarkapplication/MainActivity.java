package com.android.woojn.coursebookmarkapplication;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import com.android.woojn.coursebookmarkapplication.adapter.CourseAdapter;
import com.android.woojn.coursebookmarkapplication.model.Course;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.realm.Realm;

import static com.android.woojn.coursebookmarkapplication.util.RealmDbUtility.getNewIdByClass;
import static com.android.woojn.coursebookmarkapplication.util.RealmDbUtility.setTextViewEmptyVisibility;

public class MainActivity extends AppCompatActivity
        implements SharedPreferences.OnSharedPreferenceChangeListener,
        CourseAdapter.OnRecyclerViewClickListener {

    public static final int VIEW_ID_OF_ITEM_VIEW = 0;
    public static final boolean IS_INSERTED_COURSE = true;

    @BindView(android.R.id.tabhost)
    protected TabHost mTabHost;
    @BindView(R.id.tv_course_empty)
    protected TextView mTextViewCourseEmpty;
    @BindView(R.id.rv_course_list)
    protected RecyclerView mRecyclerViewCourse;

    private SharedPreferences mSharedPreferences;
    private Realm mRealm;
    private Toast mToast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        mTabHost.setup();
        TabHost.TabSpec spec1 = mTabHost.newTabSpec("Tab1").setContent(R.id.tab_course)
                .setIndicator(getString(R.string.string_course));
        mTabHost.addTab(spec1);
        TabHost.TabSpec spec2 = mTabHost.newTabSpec("Tab2").setContent(R.id.tab_item)
                .setIndicator(getString(R.string.string_item));
        mTabHost.addTab(spec2);

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        int tabIndex = Integer.parseInt(mSharedPreferences.getString(getString(R.string.pref_key_tab_index), "0"));
        mTabHost.setCurrentTab(tabIndex);
        mSharedPreferences.registerOnSharedPreferenceChangeListener(this);

        mRealm = Realm.getDefaultInstance();

        mRecyclerViewCourse.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerViewCourse.setAdapter(new CourseAdapter(this, mRealm.where(Course.class).findAllAsync(), this));
    }

    @Override
    protected void onResume() {
        super.onResume();
        setTextViewEmptyVisibility(Course.class, 0, mTextViewCourseEmpty);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mSharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
        mRealm.close();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        menu.findItem(R.id.action_overflow).setVisible(false);
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

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        // Do nothing
    }

    @Override
    public void onItemClick(int id, int viewId) {
        switch (viewId) {
            case VIEW_ID_OF_ITEM_VIEW:
                Intent updateIntent = new Intent(getApplicationContext(), CourseActivity.class);
                updateIntent.putExtra("id", id);
                startActivity(updateIntent);
                break;
            case R.id.iv_favorite_y_main:
            case R.id.iv_favorite_n_main:
                updateCourseFavoriteById(id);
                break;
        }
    }

    @Override
    public void onItemLongClick(int id) {
        // TODO: 리스트 출력 후 선택 (공유, 삭제 등)
        deleteCourse(id);
    }

    @OnClick(R.id.fab_insert_course)
    protected void onClick() {
        int newCourseId = getNewIdByClass(mRealm, Course.class);
        insertCourse(newCourseId);

        Intent insertIntent = new Intent(this, CourseActivity.class);
        insertIntent.putExtra("id", newCourseId);
        insertIntent.putExtra("isInsertedCourse", IS_INSERTED_COURSE);
        startActivity(insertIntent);
    }

    private void makeToastAfterCancel(int resId) {
        if (mToast != null) {
            mToast.cancel();
        }
        mToast = Toast.makeText(this, resId, Toast.LENGTH_LONG);
        mToast.show();
    }

    private void insertCourse(final int courseId) {
        mRealm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                Course course = realm.createObject(Course.class, courseId);
                course.setFavorite(false);
                setTextViewEmptyVisibility(Course.class, 0, mTextViewCourseEmpty);
            }
        });
    }

    private void updateCourseFavoriteById(final int courseId) {
        mRealm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                Course course = realm.where(Course.class).equalTo("id", courseId).findFirst();
                if (course.isFavorite()) {
                    course.setFavorite(false);
                    makeToastAfterCancel(R.string.msg_favorite_n);
                } else {
                    course.setFavorite(true);
                    makeToastAfterCancel(R.string.msg_favorite_y);
                }
            }
        });
    }

    private void deleteCourse(final int courseId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.msg_delete_confirm);
        builder.setNegativeButton(R.string.string_cancel, null);
        builder.setPositiveButton(R.string.string_delete, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mRealm.executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        Course course = realm.where(Course.class).equalTo("id", courseId).findFirst();
                        course.deleteFromRealm();
                        setTextViewEmptyVisibility(Course.class, 0, mTextViewCourseEmpty);
                    }
                });
            }
        });
        builder.show();
    }

}