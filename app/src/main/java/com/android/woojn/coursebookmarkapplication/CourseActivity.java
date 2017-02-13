package com.android.woojn.coursebookmarkapplication;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
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
    @BindView(R.id.et_course_search_word)
    protected EditText mEditTextCourseSearchWord;
    @BindView(R.id.iv_favorite_y_course)
    protected ImageView mImageViewFavoriteY;
    @BindView(R.id.iv_favorite_n_course)
    protected ImageView mImageViewFavoriteN;
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

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        int courseId = getIntent().getIntExtra("id", 0);
        Log.d("Debug", "CourseActivity : courseId = " + courseId);

        mRealm = Realm.getDefaultInstance();
        mCourse = mRealm.where(Course.class).equalTo("id", courseId).findFirst();

        mEditTextCourseTitle.setText(mCourse.getTitle());
        mEditTextCourseDesc.setText(mCourse.getDesc());
        mEditTextCourseSearchWord.setText(mCourse.getSearchWord());
        setFavoriteImageView(mCourse.isFavorite());
        mRecyclerViewCourseSection.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerViewCourseSection.setAdapter(new CourseSectionAdapter(this, mCourse.getSections(), this));
        setTextViewEmptyVisibility(Section.class, mCourse.getId(), mTextViewCourseSectionEmpty);
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
                // TODO: 검색 intent로 이동 후 overlay button으로 추가되게 수정 + insertSectionDetail method 삭제
                int newSectionDetailId = getNewIdByClass(mRealm, SectionDetail.class);
                insertSectionDetail(id, newSectionDetailId);
                break;
            case R.id.btn_share_section:
                break;
            case R.id.btn_delete_section:
                // TODO: alert 띄우고 삭제하도록 수정
                deleteSection(id);
                break;
        }
    }

    @Override
    public void onItemLongClick(int id) {
        // TODO: 리스트 출력 후 수정, 삭제 등을 선택하게 (현재는 수정만)
        showSectionDialog(id, true);
    }

    @OnClick({R.id.iv_favorite_y_course, R.id.iv_favorite_n_course})
    protected void onClickTextViewCourseFavorite() {
        updateCourseFavorite();
    }

    @OnClick(R.id.btn_insert_section)
    protected void onClickButtonInsertSection() {
        int newSectionId = getNewIdByClass(mRealm, Section.class);
        insertSection(newSectionId);
    }

    @OnClick(R.id.btn_share_course)
    protected void onClickButtonShareCourse() {
        Log.d("Check", "btn_share_course");
    }

    @OnClick(R.id.btn_delete_course)
    protected void onClickButtonDeleteCourse() {
        deleteCourse();
    }

    private void makeToastAfterCancel(int resId) {
        if (mToast != null) {
            mToast.cancel();
        }
        mToast = Toast.makeText(this, resId, Toast.LENGTH_LONG);
        mToast.show();
    }

    private void setFavoriteImageView(boolean isFavorite) {
        if (isFavorite) {
            mImageViewFavoriteN.setVisibility(View.GONE);
            mImageViewFavoriteY.setVisibility(View.VISIBLE);
        }
        else {
            mImageViewFavoriteY.setVisibility(View.GONE);
            mImageViewFavoriteN.setVisibility(View.VISIBLE);
        }
    }

    private void updateCourseFavorite() {
        mRealm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                if (mCourse.isFavorite()) {
                    mCourse.setFavorite(false);
                    makeToastAfterCancel(R.string.msg_favorite_n);
                } else {
                    mCourse.setFavorite(true);
                    makeToastAfterCancel(R.string.msg_favorite_y);
                }
                setFavoriteImageView(mCourse.isFavorite());
            }
        });
    }

    private void updateCourseText() {
        mRealm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                String title = mEditTextCourseTitle.getText().toString();
                String desc = mEditTextCourseDesc.getText().toString();
                String searchWord = mEditTextCourseSearchWord.getText().toString();
                mCourse.setTitle(title);
                mCourse.setDesc(desc);
                mCourse.setSearchWord(searchWord);
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

    private void insertSection(int sectionId) {
        showSectionDialog(sectionId, false);
    }

    private void deleteSection(final int sectionId) {
        mRealm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                Section section = realm.where(Section.class).equalTo("id", sectionId).findFirst();
                section.deleteFromRealm();
                setTextViewEmptyVisibility(Section.class, mCourse.getId(), mTextViewCourseSectionEmpty);
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

    private void showSectionDialog(final int sectionId, final boolean isCreated) {
        final View dialogView = getLayoutInflater().inflate(R.layout.dialog_section, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(isCreated ? R.string.string_update_section : R.string.string_register_section);
        builder.setView(dialogView);
        final EditText editTextTitle = (EditText) dialogView.findViewById(R.id.et_section_title);
        final EditText editTextSearchWord = (EditText) dialogView.findViewById(R.id.et_section_search_word);

        builder.setNegativeButton(R.string.string_cancel, null);
        builder.setPositiveButton(isCreated ? R.string.string_update : R.string.string_register, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String title = editTextTitle.getText().toString();
                String searchWord = editTextSearchWord.getText().toString();

                mRealm.beginTransaction();
                Section section;
                if (isCreated) {
                    section = mRealm.where(Section.class).equalTo("id", sectionId).findFirst();
                    section.setTitle(title);
                    section.setSearchWord(searchWord);
                }
                else {
                    section = mRealm.createObject(Section.class, sectionId);
                    section.setTitle(title);
                    section.setSearchWord(searchWord);
                    mCourse.getSections().add(section);
                }
                mRealm.commitTransaction();
                setTextViewEmptyVisibility(Section.class, mCourse.getId(), mTextViewCourseSectionEmpty);
            }
        });

        final AlertDialog alertDialog = builder.create();
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.show();
        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);

        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Do nothing
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!editTextTitle.getText().toString().trim().isEmpty() &&
                        !editTextSearchWord.getText().toString().trim().isEmpty()) {
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

}