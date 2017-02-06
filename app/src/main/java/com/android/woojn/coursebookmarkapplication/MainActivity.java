package com.android.woojn.coursebookmarkapplication;

import android.content.Intent;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import com.android.woojn.coursebookmarkapplication.adapter.CourseAdapter;

public class MainActivity extends AppCompatActivity {

    private TabHost tabHost;
    private TextView emptyCourseTextView;
    private RecyclerView courseRecyclerView;
    private CourseAdapter mCourseAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // View 설정
        emptyCourseTextView = (TextView) findViewById(R.id.tv_course_empty);
        courseRecyclerView = (RecyclerView) findViewById(R.id.rv_course_list);
        courseRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Tab 설정
        tabHost = (TabHost) findViewById(android.R.id.tabhost);
        tabHost.setup();
        TabHost.TabSpec spec1 = tabHost.newTabSpec("Tab1").setContent(R.id.tab_course)
                .setIndicator(getString(R.string.string_course));
        tabHost.addTab(spec1);
        TabHost.TabSpec spec2 = tabHost.newTabSpec("Tab2").setContent(R.id.tab_item)
                .setIndicator(getString(R.string.string_item));
        tabHost.addTab(spec2);

        // TODO Settings 값으로 default tab을 설정
        tabHost.setCurrentTab(0);

        // TODO DB 설정

        // Adapter 설정
        Cursor cursor = getAllCourse();
        mCourseAdapter = new CourseAdapter(this, cursor);
        courseRecyclerView.setAdapter(mCourseAdapter);

        courseRecyclerView.addOnItemTouchListener(new RecyclerItemClickListener(this, courseRecyclerView, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, long id) {
                // TODO 수정
                Toast.makeText(getApplicationContext(), "click / id = " + id, Toast.LENGTH_LONG).show();
            }
            @Override
            public void onItemLongClick(View view, long id) {
                // TODO 리스트 출력 후 선택(공유, 삭제 등)
                Toast.makeText(getApplicationContext(), "long click / id = " + id, Toast.LENGTH_LONG).show();
            }
        }));
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

    private Cursor getAllCourse() {
        // TODO delete fake data & make db query

        // fake data
        String[] columns = new String[] { "_id", "text", "description" };
        MatrixCursor matrixCursor= new MatrixCursor(columns);
        matrixCursor.addRow(new Object[] { 3, "test3", "desc4" });
        matrixCursor.addRow(new Object[] { 4, "test4", "desc4" });

        Cursor cursor = matrixCursor;

        // 코스 아이템이 없으면, 추가 권유 메세지가 나옴
        if (cursor != null && cursor.getCount() > 0) {
            emptyCourseTextView.setVisibility(View.GONE);
        } else {
            emptyCourseTextView.setVisibility(View.VISIBLE);
        }
        return cursor;
    }

}
