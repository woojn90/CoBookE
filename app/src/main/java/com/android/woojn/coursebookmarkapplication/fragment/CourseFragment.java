package com.android.woojn.coursebookmarkapplication.fragment;

import static com.android.woojn.coursebookmarkapplication.Constants.DEFAULT_VIEW_ID;
import static com.android.woojn.coursebookmarkapplication.Constants.FIELD_NAME_FAVORITE;
import static com.android.woojn.coursebookmarkapplication.Constants.FIELD_NAME_ID;
import static com.android.woojn.coursebookmarkapplication.Constants.FIELD_NAME_TITLE;
import static com.android.woojn.coursebookmarkapplication.Constants.KEY_COURSE_ID;
import static com.android.woojn.coursebookmarkapplication.util.RealmDbUtility.getNewIdByClass;
import static com.android.woojn.coursebookmarkapplication.util.RealmDbUtility.updateTextViewEmptyVisibility;
import static com.android.woojn.coursebookmarkapplication.util.SettingUtility.isDeleteWithConfirm;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.woojn.coursebookmarkapplication.R;
import com.android.woojn.coursebookmarkapplication.activity.CourseActivity;
import com.android.woojn.coursebookmarkapplication.adapter.CourseAdapter;
import com.android.woojn.coursebookmarkapplication.model.Course;
import com.android.woojn.coursebookmarkapplication.model.Item;
import com.android.woojn.coursebookmarkapplication.model.Section;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmResults;
import io.realm.Sort;

/**
 * Created by wjn on 2017-02-16.
 */

public class CourseFragment extends Fragment implements CourseAdapter.OnRecyclerViewClickListener,
        SharedPreferences.OnSharedPreferenceChangeListener {

    @BindView(R.id.tv_course_empty)
    protected TextView mTextViewCourseEmpty;
    @BindView(R.id.rv_course_list)
    protected RecyclerView mRecyclerViewCourse;

    private Realm mRealm;
    private SharedPreferences mSharedPreferences;
    private Toast mToast;
    private RealmResults<Course> mCourses;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mRealm = Realm.getDefaultInstance();
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        mSharedPreferences.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_course, container, false);
        ButterKnife.bind(this, rootView);

        mRecyclerViewCourse.setLayoutManager(new LinearLayoutManager(getActivity()));
        setRecyclerViewAdapter();

        FloatingActionButton fabInsertCourse = (FloatingActionButton) getActivity().findViewById(R.id.fab_insert_course);
        fabInsertCourse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int newCourseId = getNewIdByClass(Course.class);
                showCourseDialog(newCourseId, false, "", "", "");
            }
        });
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        updateTextViewEmptyVisibility(Course.class, 0, mTextViewCourseEmpty);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mRealm.close();
        mSharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onItemClick(int id, int viewId) {
        Course course = mRealm.where(Course.class).equalTo(FIELD_NAME_ID, id).findFirst();
        switch (viewId) {
            case DEFAULT_VIEW_ID:
                Intent updateIntent = new Intent(getContext(), CourseActivity.class);
                updateIntent.putExtra(KEY_COURSE_ID, id);
                startActivity(updateIntent);
                break;
            case R.id.iv_favorite_y_main:
            case R.id.iv_favorite_n_main:
                toggleCourseFavorite(course);
                break;
            case R.id.btn_course_delete:
                deleteCourse(course);
                break;
        }
    }

    @Override
    public void onItemDoubleTap(int id) {
        Course course = mRealm.where(Course.class).equalTo(FIELD_NAME_ID, id).findFirst();
        showCourseDialog(course.getId(), true, course.getTitle(), course.getDesc(), course.getSearchWord());
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (getContext().getString(R.string.pref_key_sort_course).equals(key)) {
            setRecyclerViewAdapter();
        }
    }

    private void showToastByForce(int resId) {
        if (mToast != null) {
            mToast.cancel();
        }
        mToast = Toast.makeText(getContext(), resId, Toast.LENGTH_LONG);
        mToast.show();
    }

    private void setRecyclerViewAdapter() {
        String sort = mSharedPreferences.getString(getString(R.string.pref_key_sort_course), getString(R.string.string_sort_latest));

        if (getString(R.string.string_sort_abc).equals(sort)) {
            mCourses = mRealm.where(Course.class).findAllSorted(FIELD_NAME_TITLE)
                    .sort(FIELD_NAME_FAVORITE, Sort.DESCENDING);
        } else if (getString(R.string.string_sort_cba).equals(sort)) {
            mCourses = mRealm.where(Course.class).findAllSorted(FIELD_NAME_TITLE, Sort.DESCENDING)
                    .sort(FIELD_NAME_FAVORITE, Sort.DESCENDING);
        } else if (getString(R.string.string_sort_latest).equals(sort)) {
            mCourses = mRealm.where(Course.class).findAllSorted(FIELD_NAME_ID)
                    .sort(FIELD_NAME_FAVORITE, Sort.DESCENDING);
        } else if (getString(R.string.string_sort_earliest).equals(sort)) {
            mCourses = mRealm.where(Course.class).findAllSorted(FIELD_NAME_ID, Sort.DESCENDING)
                    .sort(FIELD_NAME_FAVORITE, Sort.DESCENDING);
        }
        mRecyclerViewCourse.setAdapter(new CourseAdapter(getContext(), mCourses, this));
    }

    private void toggleCourseFavorite(final Course course) {
        mRealm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                if (course.isFavorite()) {
                    course.setFavorite(false);
                    showToastByForce(R.string.msg_favorite_n);
                } else {
                    course.setFavorite(true);
                    showToastByForce(R.string.msg_favorite_y);
                }
            }
        });
    }

    private void deleteCourse(final Course course) {
        if (isDeleteWithConfirm(getContext())) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setMessage(R.string.msg_delete_confirm);
            builder.setNegativeButton(R.string.string_cancel, null);
            builder.setPositiveButton(R.string.string_delete, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    deleteCourseConfirm(course);
                }
            });
            builder.show();
        } else {
            deleteCourseConfirm(course);
        }
    }

    private void deleteCourseConfirm(Course course) {
        mRealm.beginTransaction();
        RealmList<Section> sections = course.getSections();
        for (int i = sections.size() - 1; i >= 0; i--) {

            RealmList<Item> sectionItems = sections.get(i).getItems();
            for (int j = sectionItems.size() - 1; j >= 0; j--) {
                sectionItems.get(j).deleteFromRealm();
            }
            sections.get(i).deleteFromRealm();
        }
        course.deleteFromRealm();
        mRealm.commitTransaction();
        showToastByForce(R.string.msg_delete);
        updateTextViewEmptyVisibility(Course.class, 0, mTextViewCourseEmpty);
    }

    private void showCourseDialog(final int courseId, final boolean isCreated, final String beforeTitle, final String beforeDesc, final String beforeSearchWord) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        final View dialogView = getActivity().getLayoutInflater().inflate(R.layout.dialog_course, null);
        builder.setView(dialogView);
        final EditText editTextTitle = (EditText) dialogView.findViewById(R.id.et_course_title);
        final EditText editTextDesc = (EditText) dialogView.findViewById(R.id.et_course_desc);
        final EditText editTextSearchWord = (EditText) dialogView.findViewById(R.id.et_course_search_word);

        if (isCreated) {
            editTextTitle.setText(beforeTitle);
            editTextDesc.setText(beforeDesc);
            editTextSearchWord.setText(beforeSearchWord);
        }

        builder.setTitle(R.string.string_course_info);
        builder.setNeutralButton(R.string.string_cancel, null);
        builder.setPositiveButton(isCreated ? R.string.string_update : R.string.string_register, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String title = editTextTitle.getText().toString().trim();
                String Desc = editTextDesc.getText().toString().trim();
                String searchWord = editTextSearchWord.getText().toString().trim();

                Course course;
                mRealm.beginTransaction();
                if (isCreated) {
                    course = mRealm.where(Course.class).equalTo(FIELD_NAME_ID, courseId).findFirst();
                    course.setTitle(title);
                    course.setDesc(Desc);
                    course.setSearchWord(searchWord);
                }
                else {
                    course = mRealm.createObject(Course.class, courseId);
                    course.setTitle(title);
                    course.setDesc(Desc);
                    course.setSearchWord(searchWord);

                    Intent insertIntent = new Intent(getContext(), CourseActivity.class);
                    insertIntent.putExtra(KEY_COURSE_ID, courseId);
                    startActivity(insertIntent);
                    updateTextViewEmptyVisibility(Course.class, 0, mTextViewCourseEmpty);
                }
                mRealm.commitTransaction();
            }
        });

        final AlertDialog alertDialog = builder.create();
        alertDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        alertDialog.show();
        if (!isCreated) alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
        editTextTitle.requestFocus();
        editTextTitle.setImeOptions(EditorInfo.IME_ACTION_NEXT);
        editTextDesc.setImeOptions(EditorInfo.IME_ACTION_NEXT);
        editTextSearchWord.setImeOptions(EditorInfo.IME_ACTION_DONE);
        editTextSearchWord.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE){
                    if (alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled()) {
                        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).performClick();
                        return true;
                    }
                }
                return false;
            }
        });

        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Do nothing
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String afterTitle = editTextTitle.getText().toString();

                if (!afterTitle.trim().isEmpty()) {
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
        editTextDesc.addTextChangedListener(textWatcher);
        editTextSearchWord.addTextChangedListener(textWatcher);
    }
}
