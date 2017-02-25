package com.android.woojn.coursebookmarkapplication.activity;

import static com.android.woojn.coursebookmarkapplication.Constants.FIELD_NAME_ID;
import static com.android.woojn.coursebookmarkapplication.Constants.KEY_COURSE_ID;
import static com.android.woojn.coursebookmarkapplication.Constants.KEY_SECTION_ID;
import static com.android.woojn.coursebookmarkapplication.Constants.KEY_STRING_URL;
import static com.android.woojn.coursebookmarkapplication.util.RealmDbUtility.getNewIdByClass;
import static com.android.woojn.coursebookmarkapplication.util.RealmDbUtility.updateTextViewEmptyVisibility;

import static com.android.woojn.coursebookmarkapplication.util.SettingUtility.isDeleteWithConfirm;
import static com.android.woojn.coursebookmarkapplication.util.ShareUtility.shareTextByRealmObject;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.woojn.coursebookmarkapplication.R;
import com.android.woojn.coursebookmarkapplication.adapter.CourseSectionAdapter;
import com.android.woojn.coursebookmarkapplication.async.ParseAsyncTask;
import com.android.woojn.coursebookmarkapplication.model.Course;
import com.android.woojn.coursebookmarkapplication.model.Item;
import com.android.woojn.coursebookmarkapplication.model.Section;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmResults;

/**
 * Created by wjn on 2017-02-06.
 */

public class CourseActivity extends AppCompatActivity
        implements CourseSectionAdapter.OnRecyclerViewClickListener {

    @BindView(R.id.layout_course_info)
    protected LinearLayout mLinearLayoutCourseInfo;
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
    @BindView(R.id.tv_section_empty)
    protected TextView mTextViewSectionEmpty;
    @BindView(R.id.rv_section_list)
    protected RecyclerView mRecyclerViewSection;

    private Realm mRealm;
    private SharedPreferences mSharedPreferences;
    private Toast mToast;

    public static Course currentCourse;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course);
        ButterKnife.bind(this);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        int courseId = getIntent().getIntExtra(KEY_COURSE_ID, 0);

        mRealm = Realm.getDefaultInstance();
        currentCourse = mRealm.where(Course.class).equalTo(FIELD_NAME_ID, courseId).findFirst();

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        setAllTextView();
        toggleFavoriteImageView(currentCourse.isFavorite());

        RealmResults<Section> sections = currentCourse.getSections().sort(FIELD_NAME_ID);
        mRecyclerViewSection.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerViewSection.setAdapter(new CourseSectionAdapter(this, sections, this));
        updateTextViewEmptyVisibility(Section.class, currentCourse.getId(), mTextViewSectionEmpty);

        final GestureDetector gd = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDoubleTap(MotionEvent e) {
                showCourseDialog(currentCourse.getTitle(), currentCourse.getSearchWord(), currentCourse.getDesc());
                return true;
            }
        });

        mLinearLayoutCourseInfo.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                gd.onTouchEvent(event);
                return true;
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        for (Section section : currentCourse.getSections()) {
            for (Item item : section.getItems()) {
                if (!item.isVisited()) {
                    retrieveSectionItemById(item.getId());
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mRealm.close();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_course, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_share:
                shareTextByRealmObject(this, currentCourse);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemClick(int id, View view) {
        final Section section = mRealm.where(Section.class).equalTo(FIELD_NAME_ID, id).findFirst();
        switch (view.getId()) {
            case R.id.btn_section_search_page:
                searchAndShowResults(section);
                break;
            case R.id.btn_section_share:
                shareTextByRealmObject(CourseActivity.this, section);
                break;
        }
    }

    @Override
    public void onItemLongClick(int id) {
        Section section = mRealm.where(Section.class).equalTo(FIELD_NAME_ID, id).findFirst();
        deleteSection(section);
    }

    @Override
    public void onItemDoubleTap(int id) {
        Section section = mRealm.where(Section.class).equalTo(FIELD_NAME_ID, id).findFirst();
        showSectionDialog(section.getId(), true, section.getTitle(), section.getSearchWord());
    }

    @OnClick({R.id.iv_favorite_y_course, R.id.iv_favorite_n_course})
    protected void onClickTextViewCourseFavorite() {
        toggleCourseFavorite();
    }

    @OnClick(R.id.fab_insert_section)
    protected void onClickFloatingActionButton() {
        int newSectionId = getNewIdByClass(Section.class);
        showSectionDialog(newSectionId, false, "", "");
    }

    private void showToastByForce(int resId) {
        if (mToast != null) {
            mToast.cancel();
        }
        mToast = Toast.makeText(this, resId, Toast.LENGTH_LONG);
        mToast.show();
    }

    private void toggleFavoriteImageView(boolean isFavorite) {
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
        mTextViewCourseTitle.setText(currentCourse.getTitle());
        mTextViewCourseSearchWord.setText("(" + currentCourse.getSearchWord() + ")");
        String desc = currentCourse.getDesc();
        if (desc != null && !desc.isEmpty()) {
            mTextViewCourseDesc.setVisibility(View.VISIBLE);
            mTextViewCourseDesc.setText(currentCourse.getDesc());
        } else {
            mTextViewCourseDesc.setVisibility(View.GONE);
        }
    }

    private void toggleCourseFavorite() {
        mRealm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                if (currentCourse.isFavorite()) {
                    currentCourse.setFavorite(false);
                    showToastByForce(R.string.msg_favorite_n);
                } else {
                    currentCourse.setFavorite(true);
                    showToastByForce(R.string.msg_favorite_y);
                }
                toggleFavoriteImageView(currentCourse.isFavorite());
            }
        });
    }

    private void deleteSection(final Section section) {
        if (isDeleteWithConfirm(this)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(R.string.msg_delete_confirm);
            builder.setNegativeButton(R.string.string_cancel, null);
            builder.setPositiveButton(R.string.string_delete, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    deleteSectionConfirm(section);
                }
            });
            builder.show();
        } else {
            deleteSectionConfirm(section);
        }
    }

    private void deleteSectionConfirm(Section section) {
        RealmList<Item> sectionItems = section.getItems();
        mRealm.beginTransaction();
        for (int i = sectionItems.size() - 1; i >= 0; i--) {
            sectionItems.get(i).deleteFromRealm();
        }
        section.deleteFromRealm();
        mRealm.commitTransaction();
        showToastByForce(R.string.msg_delete);
        updateTextViewEmptyVisibility(Section.class, currentCourse.getId(), mTextViewSectionEmpty);
    }

    private void searchAndShowResults(Section section) {
        String searchEngine = mSharedPreferences.getString(getString(R.string.pref_key_target_of_auto_search),
                getString(R.string.pref_value_target_of_auto_search_naver_total));
        String query = currentCourse.getSearchWord() + " " + section.getSearchWord();

        if (getString(R.string.pref_value_target_of_auto_search_instagram).equals(searchEngine)) {
            query = query.replace(" ", "");
        }

        String queryEncoded;
        try {
            queryEncoded = URLEncoder.encode(query, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            queryEncoded = query;
            e.printStackTrace();
        }

        Intent webIntent = new Intent(this, WebActivity.class);
        webIntent.putExtra(KEY_STRING_URL, searchEngine + queryEncoded);
        webIntent.putExtra(KEY_SECTION_ID, section.getId());
        startActivity(webIntent);
    }

    private void showCourseDialog(final String beforeTitle, final String beforeSearchWord, final String beforeDesc) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final View dialogView = getLayoutInflater().inflate(R.layout.dialog_course, null);
        builder.setView(dialogView);
        final EditText editTextTitle = (EditText) dialogView.findViewById(R.id.et_course_title);
        final EditText editTextSearchWord = (EditText) dialogView.findViewById(R.id.et_course_search_word);
        final EditText editTextDesc = (EditText) dialogView.findViewById(R.id.et_course_desc);

        editTextTitle.setText(beforeTitle);
        editTextSearchWord.setText(beforeSearchWord);
        editTextDesc.setText(beforeDesc);

        builder.setTitle(R.string.string_course_info);
        builder.setNegativeButton(R.string.string_cancel, null);
        builder.setPositiveButton(R.string.string_update, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String title = editTextTitle.getText().toString();
                String searchWord = editTextSearchWord.getText().toString();
                String Desc = editTextDesc.getText().toString();

                mRealm.beginTransaction();
                currentCourse.setTitle(title);
                currentCourse.setSearchWord(searchWord);
                currentCourse.setDesc(Desc);
                mRealm.commitTransaction();
                setAllTextView();
            }
        });

        final AlertDialog alertDialog = builder.create();
        alertDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        alertDialog.show();
        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
        editTextTitle.requestFocus();
        editTextTitle.setImeOptions(EditorInfo.IME_ACTION_NEXT);
        editTextSearchWord.setImeOptions(EditorInfo.IME_ACTION_NEXT);
        editTextDesc.setImeOptions(EditorInfo.IME_ACTION_DONE);
        editTextDesc.setOnEditorActionListener(new TextView.OnEditorActionListener() {
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
                String afterSearchWord = editTextSearchWord.getText().toString();
                String afterDesc = editTextDesc.getText().toString();

                if (!afterTitle.trim().isEmpty() && !afterSearchWord.trim().isEmpty()
                        && (!beforeTitle.equals(afterTitle) || !beforeSearchWord.equals(afterSearchWord) || !beforeDesc.equals(afterDesc))) {
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
                    section = mRealm.where(Section.class).equalTo(FIELD_NAME_ID, sectionId).findFirst();
                    section.setTitle(title);
                    section.setSearchWord(searchWord);
                }
                else {
                    section = mRealm.createObject(Section.class, sectionId);
                    section.setTitle(title);
                    section.setSearchWord(searchWord);
                    currentCourse.getSections().add(section);
                    updateTextViewEmptyVisibility(Section.class, currentCourse.getId(), mTextViewSectionEmpty);
                }
                mRealm.commitTransaction();
            }
        });

        final AlertDialog alertDialog = builder.create();
        alertDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        alertDialog.show();
        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
        editTextTitle.requestFocus();
        editTextTitle.setImeOptions(EditorInfo.IME_ACTION_NEXT);
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

    private void retrieveSectionItemById(int itemId) {
        new ParseAsyncTask().execute(itemId, null, null);
    }
}