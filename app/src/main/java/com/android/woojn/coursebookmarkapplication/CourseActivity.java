package com.android.woojn.coursebookmarkapplication;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuInflater;
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

public class CourseActivity extends AppCompatActivity
        implements SharedPreferences.OnSharedPreferenceChangeListener,
        CourseSectionAdapter.OnRecyclerViewClickListener{

    @BindView(R.id.tv_course_title)
    protected TextView mTextViewCourseTitle;
    @BindView(R.id.tv_course_search_word)
    protected TextView mTextViewCourseSearchWord;
    @BindView(R.id.tv_course_desc)
    protected TextView mTextViewCourseDesc;
    @BindView(R.id.iv_favorite_y_course)
    protected ImageView mImageViewFavoriteY;
    @BindView(R.id.iv_favorite_n_course)
    protected ImageView mImageViewFavoriteN;
    @BindView(R.id.tv_course_section_empty)
    protected TextView mTextViewCourseSectionEmpty;
    @BindView(R.id.rv_course_section_list)
    protected RecyclerView mRecyclerViewCourseSection;

    private SharedPreferences mSharedPreferences;
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
        boolean isInsertedCourse = getIntent().getBooleanExtra("isInsertedCourse", false);

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        mSharedPreferences.registerOnSharedPreferenceChangeListener(this);

        mRealm = Realm.getDefaultInstance();
        mCourse = mRealm.where(Course.class).equalTo("id", courseId).findFirst();

        setFavoriteImageView(mCourse.isFavorite());
        mRecyclerViewCourseSection.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerViewCourseSection.setAdapter(new CourseSectionAdapter(this, mCourse.getSections(), this));
        setTextViewEmptyVisibility(Section.class, mCourse.getId(), mTextViewCourseSectionEmpty);

        if (isInsertedCourse) {
            showCourseDialog(false, "", "", "");
        } else {
            setAllTextView();
        }
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
        menu.findItem(R.id.action_setting).setVisible(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
            case R.id.action_register_section:
                int newSectionId = getNewIdByClass(mRealm, Section.class);
                insertSection(newSectionId);
                break;
            case R.id.action_update:
                showCourseDialog(true, mCourse.getTitle(), mCourse.getSearchWord(), mCourse.getDesc());
                break;
            case R.id.action_delete:
                deleteCourse();
                break;
            case R.id.action_share:
                // TODO: 코스 공유
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        // Do nothing
    }

    @Override
    public void onItemClick(final int id, View view) {
        switch (view.getId()) {
            case R.id.btn_search_section_detail:
            Section section = mRealm.where(Section.class).equalTo("id", id).findFirst();

            String searchEngine = mSharedPreferences.getString(getString(R.string.pref_key_search_engine),
                        getString(R.string.pref_value_search_engine_naver_total));
            String query = mCourse.getSearchWord() + " " + section.getSearchWord();

            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(searchEngine + query));
            startActivity(intent);

            // TODO: overlay button으로 추가되게 수정 + insertSectionDetail method 삭제
                int newSectionDetailId = getNewIdByClass(mRealm, SectionDetail.class);
                insertSectionDetail(id, newSectionDetailId);
            break;
            case R.id.btn_delete_section:
                deleteSection(id);
                break;
            case R.id.btn_section_overflow:
                PopupMenu popupMenu = new PopupMenu(this, view);
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.item_section_update:
                                updateSection(id);
                                return true;
                            case R.id.item_section_share:
                                // TODO: 섹션 공유
                                return true;
                        }
                        return false;
                    }
                });
                popupMenu.inflate(R.menu.menu_section);
                popupMenu.show();
                break;
        }
    }

    @OnClick({R.id.iv_favorite_y_course, R.id.iv_favorite_n_course})
    protected void onClickTextViewCourseFavorite() {
        updateCourseFavorite();
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

    private void setAllTextView() {
        mTextViewCourseTitle.setText(mCourse.getTitle());
        mTextViewCourseSearchWord.setText("(" + mCourse.getSearchWord() + ")");
        mTextViewCourseDesc.setText(mCourse.getDesc());
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

    private void deleteCourse() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.msg_delete_confirm);
        builder.setNegativeButton(R.string.string_cancel, null);
        builder.setPositiveButton(R.string.string_delete, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mRealm.executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        mCourse.deleteFromRealm();
                        finish();
                    }
                });
            }
        });
        builder.show();
    }

    private void insertSection(int sectionId) {
        showSectionDialog(sectionId, false, "", "");
    }

    private void updateSection(int sectionId) {
        Section section = mRealm.where(Section.class).equalTo("id", sectionId).findFirst();
        String beforeTitle = section.getTitle();
        String beforeSearchWord = section.getSearchWord();
        showSectionDialog(sectionId, true, beforeTitle, beforeSearchWord);
    }

    private void deleteSection(final int sectionId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.msg_delete_confirm);
        builder.setNegativeButton(R.string.string_cancel, null);
        builder.setPositiveButton(R.string.string_delete, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mRealm.executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        Section section = realm.where(Section.class).equalTo("id", sectionId).findFirst();
                        section.deleteFromRealm();
                        setTextViewEmptyVisibility(Section.class, mCourse.getId(), mTextViewCourseSectionEmpty);
                    }
                });
            }
        });
        builder.show();
    }

    private void insertSectionDetail(final int sectionId, final int sectionDetailId) {
        mRealm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                Section section = realm.where(Section.class).equalTo("id", sectionId).findFirst();

                SectionDetail sectionDetail = realm.createObject(SectionDetail.class, sectionDetailId);
                sectionDetail.setTitle("Test Title");
                sectionDetail.setDesc("Test Description");
                sectionDetail.setUrl("https://www.google.com");
                section.getSectionDetails().add(sectionDetail);
            }
        });
    }

    private void showCourseDialog(final boolean isCreated, final String beforeTitle, final String beforeSearchWord, final String beforeDesc) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final View dialogView = getLayoutInflater().inflate(R.layout.dialog_course, null);
        builder.setView(dialogView);
        final EditText editTextTitle = (EditText) dialogView.findViewById(R.id.et_course_title);
        final EditText editTextSearchWord = (EditText) dialogView.findViewById(R.id.et_course_search_word);
        final EditText editTextDesc = (EditText) dialogView.findViewById(R.id.et_course_desc);

        if (isCreated) {
            editTextTitle.setText(beforeTitle);
            editTextSearchWord.setText(beforeSearchWord);
            editTextDesc.setText(beforeDesc);
        }

        builder.setTitle(R.string.string_course_info);
        builder.setNegativeButton(R.string.string_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (!isCreated) {
                    mRealm.beginTransaction();
                    mCourse.deleteFromRealm();
                    mRealm.commitTransaction();
                    finish();
                }
            }
        });
        builder.setPositiveButton(isCreated ? R.string.string_update : R.string.string_register, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String title = editTextTitle.getText().toString();
                String searchWord = editTextSearchWord.getText().toString();
                String Desc = editTextDesc.getText().toString();

                mRealm.beginTransaction();
                mCourse.setTitle(title);
                mCourse.setSearchWord(searchWord);
                mCourse.setDesc(Desc);
                mRealm.commitTransaction();
                setAllTextView();
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
                String afterTitle = editTextTitle.getText().toString();
                String afterSearchWord = editTextSearchWord.getText().toString();
                String afterDesc = editTextDesc.getText().toString();

                if (!afterTitle.trim().isEmpty() && !afterSearchWord.trim().isEmpty()
                        && (!beforeTitle.equals(afterTitle) || !beforeSearchWord.equals(afterSearchWord)) || !beforeDesc.equals(afterDesc)) {
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
        editTextDesc.addTextChangedListener(textWatcher);
    }

    private void showSectionDialog(final int sectionId, final boolean isCreated, final String beforeTitle, final String beforeSearchWord) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final View dialogView = getLayoutInflater().inflate(R.layout.dialog_section, null);
        builder.setView(dialogView);
        final EditText editTextTitle = (EditText) dialogView.findViewById(R.id.et_section_title);
        final EditText editTextSearchWord = (EditText) dialogView.findViewById(R.id.et_section_search_word);

        if (isCreated) {
            editTextTitle.setText(beforeTitle);
            editTextSearchWord.setText(beforeSearchWord);
        }

        builder.setTitle(R.string.string_section_info);
        builder.setNegativeButton(R.string.string_cancel, null);
        builder.setPositiveButton(isCreated ? R.string.string_update : R.string.string_register, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String title = editTextTitle.getText().toString();
                String searchWord = editTextSearchWord.getText().toString();

                Section section;
                mRealm.beginTransaction();
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
                String afterTitle = editTextTitle.getText().toString();
                String afterSearchWord = editTextSearchWord.getText().toString();

                if (!afterTitle.trim().isEmpty() && !afterSearchWord.trim().isEmpty()
                        && (!beforeTitle.equals(afterTitle) || !beforeSearchWord.equals(afterSearchWord))) {
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