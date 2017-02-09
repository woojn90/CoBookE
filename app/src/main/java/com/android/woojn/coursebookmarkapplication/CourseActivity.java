package com.android.woojn.coursebookmarkapplication;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.woojn.coursebookmarkapplication.adapter.CourseSectionAdapter;
import com.android.woojn.coursebookmarkapplication.model.Course;
import com.android.woojn.coursebookmarkapplication.model.Section;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnFocusChange;
import io.realm.Realm;

import static com.android.woojn.coursebookmarkapplication.util.RealmDbUtility.getNewIdByClass;

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

        // Intent
        int courseId = getIntent().getIntExtra("id", 0);
        Log.d("Debug", "CourseActivity : courseId = " + courseId);

        // DB 설정
        mRealm = Realm.getDefaultInstance();

        // UI & RecyclerView 설정
        mCourse = mRealm.where(Course.class).equalTo("id", courseId).findFirst();
        mEditTextCourseTitle.setText(mCourse.getTitle());
        mEditTextCourseDesc.setText(mCourse.getDesc());
        mTextViewCourseFavorite.setText(mCourse.isFavorite() ? "Y" : "N");
        mRecyclerViewCourseSection.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerViewCourseSection.setAdapter(new CourseSectionAdapter(this, mCourse.getSections(), this, mRealm));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mEditTextCourseTitle.clearFocus();
        mEditTextCourseDesc.clearFocus();
        mRealm.close();
    }

    @Override
    public void onItemClick(int id, int viewId) {
        switch (viewId) {
            case R.id.btn_share_section:
                Log.d("Check", "share / id : " + id);
                break;
            case R.id.btn_delete_section:
                Log.d("Check", "delete / id : " + id);
                // TODO: alert 띄우고 삭제하도록 수정
                mRealm.beginTransaction();
                Section section = mRealm.where(Section.class).equalTo("id", id).findFirst();
                section.deleteFromRealm();
                mRealm.commitTransaction();
                break;
        }
    }

    @OnClick({R.id.tv_course_favorite,
            R.id.btn_insert_section, R.id.btn_share_course,
            R.id.btn_save_course, R.id.btn_cancel_course})
    protected void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_course_favorite:
                Log.d("Check", "tv_course_favorite");

                // TODO: 이미지 적용 후 변경
                mRealm.beginTransaction();
                if (mCourse.isFavorite()) {
                    mCourse.setFavorite(false);
                    mTextViewCourseFavorite.setText("N");
                    makeToastMsg(getString(R.string.msg_favorite_n));
                } else {
                    mCourse.setFavorite(true);
                    mTextViewCourseFavorite.setText("Y");
                    makeToastMsg(getString(R.string.msg_favorite_y));
                }
                mRealm.commitTransaction();
                break;

            case R.id.btn_insert_section:
                Log.d("Check", "btn_insert_section");
                // TODO: Dialog 띄우고 내용 작성 후 추가하게 수정
                mRealm.beginTransaction();
                int newSectionId = getNewIdByClass(mRealm, Section.class);
                Section section = mRealm.createObject(Section.class, newSectionId);
                section.setTitle("Test Section");
                mCourse.getSections().add(section);
                mRealm.commitTransaction();
                break;

            case R.id.btn_share_course:
                Log.d("Check", "btn_share_course");
                break;
            case R.id.btn_save_course:
                Log.d("Check", "btn_save_section");
                break;
            case R.id.btn_cancel_course:
                Log.d("Check", "btn_cancel_course");
                break;
        }
    }

    @OnFocusChange({R.id.et_course_title, R.id.et_course_desc})
    protected void onFocusChange(View view, boolean hasFocus) {
        // TODO: 저장 버튼 누를 때 저장되게 변경
        if (!hasFocus) {
            String text = ((EditText) view).getText().toString();
            mRealm.beginTransaction();
            switch (view.getId()) {
                case R.id.et_course_title:
                    mCourse.setTitle(text);
                    break;
                case R.id.et_course_desc:
                    mCourse.setDesc(text);
                    break;
            }
            mRealm.commitTransaction();
        }
    }

    private void makeToastMsg(String msg) {
        if (mToast != null) {
            mToast.cancel();
        }
        mToast = Toast.makeText(this, msg, Toast.LENGTH_LONG);
        mToast.show();
    }

}