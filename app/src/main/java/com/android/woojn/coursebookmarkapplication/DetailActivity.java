package com.android.woojn.coursebookmarkapplication;

import android.database.Cursor;
import android.database.MatrixCursor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.android.woojn.coursebookmarkapplication.adapter.CourseSectionAdapter;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by wjn on 2017-02-06.
 */

public class DetailActivity extends AppCompatActivity implements CourseSectionAdapter.OnButtonInItemClickListener{

    @BindView(R.id.tv_course_section_empty)
    protected TextView mTextViewCourseSectionEmpty;
    @BindView(R.id.rv_course_section_list)
    protected RecyclerView mRecyclerViewCourseSection;

    private CourseSectionAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        ButterKnife.bind(this);

        // TODO: DB 설정

        // RecyclerView 설정
        mRecyclerViewCourseSection.setLayoutManager(new LinearLayoutManager(this));

        Cursor fakeCursor = getAllCourse();
        mAdapter = new CourseSectionAdapter(this, fakeCursor, this);
        mRecyclerViewCourseSection.setAdapter(mAdapter);
    }

    @Override
    public void onButtonInItemClick(long id, int viewId) {
        switch (viewId) {
            case R.id.btn_share_section:
                Toast.makeText(this, "share / id : " + id, Toast.LENGTH_LONG).show();
                break;
            case R.id.btn_delete_section:
                Toast.makeText(this, "delete / id : " + id, Toast.LENGTH_LONG).show();
                break;
        }
    }

    private Cursor getAllCourse() {
        // TODO: delete fake data & make db query

        // fake data
        String[] columns = new String[] { "_id", "title" };
        MatrixCursor matrixCursor = new MatrixCursor(columns);
        matrixCursor.addRow(new Object[] { 5, "section1" });
        matrixCursor.addRow(new Object[] { 6, "section2" });

        Cursor cursor = matrixCursor;

        // 코스 아이템이 없으면, 추가 권유 메세지가 나옴
        if (cursor != null && cursor.getCount() > 0) {
            mTextViewCourseSectionEmpty.setVisibility(View.GONE);
        } else {
            mTextViewCourseSectionEmpty.setVisibility(View.VISIBLE);
        }
        return cursor;
    }

}
