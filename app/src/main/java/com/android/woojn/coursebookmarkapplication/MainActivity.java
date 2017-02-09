package com.android.woojn.coursebookmarkapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import com.android.woojn.coursebookmarkapplication.adapter.CourseAdapter;
import com.android.woojn.coursebookmarkapplication.model.Course;
import com.android.woojn.coursebookmarkapplication.model.Section;
import com.android.woojn.coursebookmarkapplication.model.SectionDetail;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.realm.Realm;
import io.realm.RealmResults;

import static com.android.woojn.coursebookmarkapplication.util.RealmDbUtility.getNewIdByClass;

public class MainActivity extends AppCompatActivity
        implements SharedPreferences.OnSharedPreferenceChangeListener,
        CourseAdapter.OnRecyclerViewClickListener {

    @BindView(android.R.id.tabhost)
    protected TabHost tabHost;
    @BindView(R.id.tv_course_empty)
    protected TextView mTextViewCourseEmpty;
    @BindView(R.id.rv_course_list)
    protected RecyclerView mRecyclerViewCourse;

    private Realm mRealm;
    private SharedPreferences mSharedPreferences;
    private Toast mToast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        // Tab 설정
        tabHost.setup();
        TabHost.TabSpec spec1 = tabHost.newTabSpec("Tab1").setContent(R.id.tab_course)
                .setIndicator(getString(R.string.string_course));
        tabHost.addTab(spec1);
        TabHost.TabSpec spec2 = tabHost.newTabSpec("Tab2").setContent(R.id.tab_item)
                .setIndicator(getString(R.string.string_item));
        tabHost.addTab(spec2);

        // Settings 적용 (최초 tab 설정)
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        int tabIndex = Integer.parseInt(mSharedPreferences.getString(getString(R.string.pref_tab_index_key), "0"));
        tabHost.setCurrentTab(tabIndex);
        mSharedPreferences.registerOnSharedPreferenceChangeListener(this);

        // DB 설정
        mRealm = Realm.getDefaultInstance();

        // RecyclerView 설정
        mRecyclerViewCourse.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerViewCourse.setAdapter(new CourseAdapter(this, getAllCourse(), this));
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
            case -1:
                // TODO: 항목 수정
                Log.d("Check", "click / id = " + id);
                Intent updateIntent = new Intent(getApplicationContext(), CourseActivity.class);
                updateIntent.putExtra("id", id);
                startActivity(updateIntent);
                break;
            case R.id.tv_course_favorite:
                mRealm.beginTransaction();
                Course course = mRealm.where(Course.class).equalTo("id", id).findFirst();
                if (course.isFavorite()) {
                    course.setFavorite(false);
                    makeToastMsg(getString(R.string.msg_favorite_n));
                } else {
                    course.setFavorite(true);
                    makeToastMsg(getString(R.string.msg_favorite_y));
                }
                mRealm.commitTransaction();
                break;
        }
    }

    @Override
    public void onItemLongClick(int id, int viewId) {
        // TODO: 리스트 출력 후 선택 (공유, 삭제, 즐겨찾기 등)
        Log.d("Check", "long click / id = " + id);

        mRealm.beginTransaction();
        Course course = mRealm.where(Course.class).equalTo("id", id).findFirst();
        course.deleteFromRealm();
        mRealm.commitTransaction();
    }

    @OnClick(R.id.fab_insert_course)
    protected void onClick(View view) {
        // TODO: delete below fake data

        mRealm.beginTransaction();
        int newCourseId = getNewIdByClass(mRealm, Course.class);
        Course course = mRealm.createObject(Course.class, newCourseId);
        course.setTitle("Test Course");
        course.setDesc("Test for realm DB");
        course.setFavorite(false);

        int newSectionId1 = getNewIdByClass(mRealm, Section.class);
        Section section1 = mRealm.createObject(Section.class, newSectionId1);
        section1.setTitle("밥집");
        int newSectionId2 = getNewIdByClass(mRealm, Section.class);
        Section section2 = mRealm.createObject(Section.class, newSectionId2);
        section2.setTitle("카페");
        int newSectionId3 = getNewIdByClass(mRealm, Section.class);
        Section section3 = mRealm.createObject(Section.class, newSectionId3);
        section3.setTitle("관광");

        course.getSections().add(section1);
        course.getSections().add(section2);
        course.getSections().add(section3);

        int newSectionDetailId1 = getNewIdByClass(mRealm, SectionDetail.class);
        SectionDetail sectionDetail1 = mRealm.createObject(SectionDetail.class, newSectionDetailId1);
        sectionDetail1.setTitle("맛집1");
        sectionDetail1.setDesc("맛있고 친절한 맛집");
        int newSectionDetailId2 = getNewIdByClass(mRealm, SectionDetail.class);
        SectionDetail sectionDetail2 = mRealm.createObject(SectionDetail.class, newSectionDetailId2);
        sectionDetail2.setTitle("맛집2");
        sectionDetail2.setDesc("친절하고 좋은 집");
        int newSectionDetailId3 = getNewIdByClass(mRealm, SectionDetail.class);
        SectionDetail sectionDetail3 = mRealm.createObject(SectionDetail.class, newSectionDetailId3);
        sectionDetail3.setTitle("카페1");
        sectionDetail3.setDesc("가격 대비 훌륭함");
        int newSectionDetailId4 = getNewIdByClass(mRealm, SectionDetail.class);
        SectionDetail sectionDetail4 = mRealm.createObject(SectionDetail.class, newSectionDetailId4);
        sectionDetail4.setTitle("카페2");
        sectionDetail4.setDesc("커피가 아주 맛있다");
        int newSectionDetailId5 = getNewIdByClass(mRealm, SectionDetail.class);
        SectionDetail sectionDetail5 = mRealm.createObject(SectionDetail.class, newSectionDetailId5);
        sectionDetail5.setTitle("카페2");
        sectionDetail5.setDesc("커피가 아주 맛있다");

        section1.getSectionDetails().add(sectionDetail1);
        section1.getSectionDetails().add(sectionDetail2);
        section2.getSectionDetails().add(sectionDetail3);
        section2.getSectionDetails().add(sectionDetail4);
        section3.getSectionDetails().add(sectionDetail5);

        mRealm.copyToRealmOrUpdate(course);
        mRealm.commitTransaction();

        Intent insertIntent = new Intent(this, CourseActivity.class);
        insertIntent.putExtra("id", newCourseId);
        startActivity(insertIntent);
    }

    private RealmResults<Course> getAllCourse() {
        // 코스가 없으면, 추가 권유 메세지가 나옴
        if (mRealm.where(Course.class).count() > 0) {
            mTextViewCourseEmpty.setVisibility(View.GONE);
        } else {
            mTextViewCourseEmpty.setVisibility(View.VISIBLE);
        }
        return mRealm.where(Course.class).findAllAsync();
    }

    private void makeToastMsg(String msg) {
        if (mToast != null) {
            mToast.cancel();
        }
        mToast = Toast.makeText(this, msg, Toast.LENGTH_LONG);
        mToast.show();
    }
}