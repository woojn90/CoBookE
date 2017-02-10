package com.android.woojn.coursebookmarkapplication;

import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.woojn.coursebookmarkapplication.adapter.CourseSectionAdapter;
import com.android.woojn.coursebookmarkapplication.model.Course;
import com.android.woojn.coursebookmarkapplication.model.Section;
import com.android.woojn.coursebookmarkapplication.model.SectionDetail;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.realm.Realm;

import static com.android.woojn.coursebookmarkapplication.util.RealmDbUtility.getNewIdByClass;
import static com.android.woojn.coursebookmarkapplication.util.RealmDbUtility.setTextViewEmptyVisibility;

/**
 * Created by wjn on 2017-02-06.
 */

public class CourseActivity extends AppCompatActivity implements CourseSectionAdapter.OnRecyclerViewClickListener{

    @BindView(R.id.et_course_title)
    protected EditText mEditTextCourseTitle;
    @BindView(R.id.et_course_desc)
    protected EditText mEditTextCourseDesc;
    // TODO: star 이미지로 변경
    @BindView(R.id.tv_course_favorite)
    protected TextView mTextViewCourseFavorite;
    @BindView(R.id.tv_course_section_empty)
    protected TextView mTextViewCourseSectionEmpty;
    @BindView(R.id.rv_course_section_list)
    protected RecyclerView mRecyclerViewCourseSection;

    private Realm mRealm;
    private Toast mToast;
    private Course mCourse;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course);
        ButterKnife.bind(this);

        ActionBar actionBar = this.getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        int courseId = getIntent().getIntExtra("id", 0);
        Log.d("Debug", "CourseActivity : courseId = " + courseId);

        mRealm = Realm.getDefaultInstance();
        mCourse = mRealm.where(Course.class).equalTo("id", courseId).findFirst();

        mEditTextCourseTitle.setText(mCourse.getTitle());
        mEditTextCourseDesc.setText(mCourse.getDesc());
        mTextViewCourseFavorite.setText(mCourse.isFavorite() ? "Y" : "N");
        mRecyclerViewCourseSection.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerViewCourseSection.setAdapter(new CourseSectionAdapter(this, mCourse.getSections(), this));
        setTextViewEmptyVisibility(mRealm, Section.class, mCourse.getId(), mTextViewCourseSectionEmpty);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mRealm.close();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        updateCourseText();
    }

    @Override
    public void onItemClick(int id, int viewId) {
        switch (viewId) {
            case R.id.btn_search_section_detail:
                Log.d("Check", "search / section id : " + id);
                // TODO: 검색 intent로 이동 후 overlay button으로 추가되게 수정
//                int newSectionDetailId = getNewIdByClass(mRealm, SectionDetail.class);
//                insertSectionDetail(id, newSectionDetailId);

                Intent webIntent = new Intent(Intent.ACTION_WEB_SEARCH);
                webIntent.putExtra(SearchManager.QUERY, "이태원 맛집");
                startActivity(webIntent);

                break;
            case R.id.btn_share_section:
                Log.d("Check", "share / section id : " + id);
                break;
            case R.id.btn_delete_section:
                Log.d("Check", "delete / section id : " + id);
                // TODO: alert 띄우고 삭제하도록 수정
                deleteSection(id);
                break;
        }
    }

    @OnClick(R.id.tv_course_favorite)
    protected void onClickTextViewCourseFavorite() {
        Log.d("Check", "tv_course_favorite");

        // TODO: 이미지 적용 후 변경
        updateCourseFavorite();
    }

    @OnClick(R.id.btn_insert_section)
    protected void onClickButtonInsertSection() {
        Log.d("Check", "btn_insert_section");
        int newSectionId = getNewIdByClass(mRealm, Section.class);
        insertSection(newSectionId);
    }

    @OnClick(R.id.btn_share_course)
    protected void onClickButtonShareCourse() {
        Log.d("Check", "btn_share_course");
    }

    @OnClick(R.id.btn_delete_course)
    protected void onClickButtonDeleteCourse() {
        Log.d("Check", "btn_delete_course");
        deleteCourse();
    }

    private void makeToastAfterCancel(int resId) {
        if (mToast != null) {
            mToast.cancel();
        }
        mToast = Toast.makeText(this, resId, Toast.LENGTH_LONG);
        mToast.show();
    }

    private void updateCourseFavorite() {
        mRealm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                if (mCourse.isFavorite()) {
                    mCourse.setFavorite(false);
                    mTextViewCourseFavorite.setText("N");
                    makeToastAfterCancel(R.string.msg_favorite_n);
                } else {
                    mCourse.setFavorite(true);
                    makeToastAfterCancel(R.string.msg_favorite_y);
                    mTextViewCourseFavorite.setText("Y");
                }
            }
        });
    }

    private void updateCourseText() {
        mRealm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                String title = mEditTextCourseTitle.getText().toString();
                String desc = mEditTextCourseDesc.getText().toString();
                mCourse.setTitle(title);
                mCourse.setDesc(desc);
            }
        });
    }

    private void deleteCourse() {
        mRealm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                mCourse.deleteFromRealm();
                finish();
            }
        });
    }

    private void insertSection(final int sectionId) {
        mRealm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                // TODO: Dialog 띄우고 내용 작성 후 추가하게 수정
                Section section = realm.createObject(Section.class, sectionId);
                section.setTitle("Test Section");
                mCourse.getSections().add(section);
                setTextViewEmptyVisibility(realm, Section.class, mCourse.getId(), mTextViewCourseSectionEmpty);
            }
        });
    }

    private void deleteSection(final int sectionId) {
        mRealm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                Section section = realm.where(Section.class).equalTo("id", sectionId).findFirst();
                section.deleteFromRealm();
                setTextViewEmptyVisibility(realm, Section.class, mCourse.getId(), mTextViewCourseSectionEmpty);
            }
        });
    }

    private void insertSectionDetail(final int sectionId, final int sectionDetailId) {
        mRealm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                Section section = realm.where(Section.class).equalTo("id", sectionId).findFirst();

                SectionDetail sectionDetail = realm.createObject(SectionDetail.class, sectionDetailId);
                sectionDetail.setTitle("Test Title");
                sectionDetail.setDesc("Test Description");
                section.getSectionDetails().add(sectionDetail);
            }
        });
    }

}