package com.android.woojn.coursebookmarkapplication;

import android.database.Cursor;
import android.database.MatrixCursor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.android.woojn.coursebookmarkapplication.adapter.CourseSectionAdapter;

/**
 * Created by wjn on 2017-02-06.
 */

public class DetailActivity extends AppCompatActivity {

    private TextView emptyCourseSectionTextView;
    private RecyclerView courseSectionRecyclerView;
    private CourseSectionAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        // View 설정
        emptyCourseSectionTextView = (TextView) findViewById(R.id.tv_course_section_empty);
        courseSectionRecyclerView = (RecyclerView) findViewById(R.id.rv_course_section_list);
        courseSectionRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // TODO DB 설정

        // Adapter 설정
        Cursor fakeCursor = getAllCourse();
        mAdapter = new CourseSectionAdapter(this, fakeCursor);
        courseSectionRecyclerView.setAdapter(mAdapter);
    }

    private Cursor getAllCourse() {
        // TODO delete fake data & make db query

        // fake data
        String[] columns = new String[] { "_id", "title"};
        MatrixCursor matrixCursor= new MatrixCursor(columns);
        matrixCursor.addRow(new Object[] { 5, "section1"});
        matrixCursor.addRow(new Object[] { 6, "section2"});

        Cursor cursor = matrixCursor;

        // 코스 아이템이 없으면, 추가 권유 메세지가 나옴
        if (cursor != null && cursor.getCount() > 0) {
            emptyCourseSectionTextView.setVisibility(View.GONE);
        } else {
            emptyCourseSectionTextView.setVisibility(View.VISIBLE);
        }
        return cursor;
    }
}
