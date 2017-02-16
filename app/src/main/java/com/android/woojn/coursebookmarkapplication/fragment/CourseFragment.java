package com.android.woojn.coursebookmarkapplication.fragment;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.android.woojn.coursebookmarkapplication.R;
import com.android.woojn.coursebookmarkapplication.activity.CourseActivity;
import com.android.woojn.coursebookmarkapplication.adapter.CourseAdapter;
import com.android.woojn.coursebookmarkapplication.model.Course;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Realm;

import static com.android.woojn.coursebookmarkapplication.activity.MainActivity.VIEW_ID_OF_ITEM_VIEW;
import static com.android.woojn.coursebookmarkapplication.util.RealmDbUtility.setTextViewEmptyVisibility;

/**
 * Created by wjn on 2017-02-16.
 */

public class CourseFragment extends Fragment implements CourseAdapter.OnRecyclerViewClickListener{

    @BindView(R.id.tv_course_empty)
    protected TextView mTextViewCourseEmpty;
    @BindView(R.id.rv_course_list)
    protected RecyclerView mRecyclerViewCourse;

    private Realm mRealm;
    private Toast mToast;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mRealm = Realm.getDefaultInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_course, null);
        ButterKnife.bind(this, rootView);

        mRecyclerViewCourse.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerViewCourse.setAdapter(new CourseAdapter(getContext(), mRealm.where(Course.class).findAllAsync(), this));
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        setTextViewEmptyVisibility(Course.class, 0, mTextViewCourseEmpty);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mRealm.close();
    }

    @Override
    public void onItemClick(int id, int viewId) {
        switch (viewId) {
            case VIEW_ID_OF_ITEM_VIEW:
                Intent updateIntent = new Intent(getContext(), CourseActivity.class);
                updateIntent.putExtra("id", id);
                startActivity(updateIntent);
                break;
            case R.id.iv_favorite_y_main:
            case R.id.iv_favorite_n_main:
                updateCourseFavoriteById(id);
                break;
            case R.id.btn_delete_course:
                deleteCourse(id);
                break;
        }
    }

    private void makeToastAfterCancel(int resId) {
        if (mToast != null) {
            mToast.cancel();
        }
        mToast = Toast.makeText(getContext(), resId, Toast.LENGTH_LONG);
        mToast.show();
    }

    private void updateCourseFavoriteById(final int courseId) {
        mRealm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                Course course = realm.where(Course.class).equalTo("id", courseId).findFirst();
                if (course.isFavorite()) {
                    course.setFavorite(false);
                    makeToastAfterCancel(R.string.msg_favorite_n);
                } else {
                    course.setFavorite(true);
                    makeToastAfterCancel(R.string.msg_favorite_y);
                }
            }
        });
    }

    private void deleteCourse(final int courseId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setMessage(R.string.msg_delete_confirm);
        builder.setNegativeButton(R.string.string_cancel, null);
        builder.setPositiveButton(R.string.string_delete, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mRealm.executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        Course course = realm.where(Course.class).equalTo("id", courseId).findFirst();
                        course.deleteFromRealm();
                        setTextViewEmptyVisibility(Course.class, 0, mTextViewCourseEmpty);
                    }
                });
            }
        });
        builder.show();
    }
}
