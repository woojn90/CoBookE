package com.android.woojn.coursebookmarkapplication.adapter;

import static com.android.woojn.coursebookmarkapplication.Constants.DEFAULT_VIEW_ID;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.woojn.coursebookmarkapplication.R;
import com.android.woojn.coursebookmarkapplication.model.Course;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.realm.OrderedRealmCollection;
import io.realm.RealmRecyclerViewAdapter;

/**
 * Created by wjn on 2017-02-06.
 */

public class CourseAdapter extends RealmRecyclerViewAdapter<Course, CourseAdapter.CourseViewHolder> {

    private final OnRecyclerViewClickListener mListener;

    public CourseAdapter(Context context, OrderedRealmCollection<Course> data, OnRecyclerViewClickListener listener) {
        super(context, data, true);
        mListener = listener;
    }

    public interface OnRecyclerViewClickListener {
        void onItemClick(int id, int viewId);
        void onItemDoubleTap(int id);
    }

    @Override
    public CourseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.course_list_item, parent, false);
        return new CourseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(CourseViewHolder holder, int position) {
        if (getData() != null) {
            Course course = getData().get(position);
            holder.itemView.setTag(course.getId());
            holder.textViewCourseTitle.setText(course.getTitle());
            String desc = course.getDesc();
            if (desc != null && !desc.isEmpty()) {
                holder.textViewCourseDesc.setVisibility(View.VISIBLE);
                holder.textViewCourseDesc.setText(course.getDesc());
            } else {
                holder.textViewCourseDesc.setVisibility(View.GONE);
            }
            if (course.isFavorite()) {
                holder.imageViewFavoriteN.setVisibility(View.GONE);
                holder.imageViewFavoriteY.setVisibility(View.VISIBLE);
            } else {
                holder.imageViewFavoriteY.setVisibility(View.GONE);
                holder.imageViewFavoriteN.setVisibility(View.VISIBLE);
            }
        }
    }

    class CourseViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.tv_course_title)
        TextView textViewCourseTitle;
        @BindView(R.id.tv_course_desc)
        TextView textViewCourseDesc;
        @BindView(R.id.iv_favorite_y_main)
        ImageView imageViewFavoriteY;
        @BindView(R.id.iv_favorite_n_main)
        ImageView imageViewFavoriteN;

        public CourseViewHolder(final View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            final GestureDetector gd = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onSingleTapConfirmed(MotionEvent e) {
                    mListener.onItemClick((int) itemView.getTag(), DEFAULT_VIEW_ID);
                    return true;
                }

                @Override
                public boolean onDoubleTap(MotionEvent e) {
                    mListener.onItemDoubleTap((int) itemView.getTag());
                    return true;
                }
            });

            itemView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    gd.onTouchEvent(event);
                    return true;
                }
            });
        }

        @OnClick(R.id.iv_favorite_y_main)
        public void onClickImageViewFavoriteY(View view) {
            imageViewFavoriteY.setVisibility(View.GONE);
            imageViewFavoriteN.setVisibility(View.VISIBLE);
            mListener.onItemClick((int) itemView.getTag(), view.getId());
        }

        @OnClick(R.id.iv_favorite_n_main)
        public void onClickImageViewFavoriteN(View view) {
            imageViewFavoriteN.setVisibility(View.GONE);
            imageViewFavoriteY.setVisibility(View.VISIBLE);
            mListener.onItemClick((int) itemView.getTag(), view.getId());
        }

        @OnClick(R.id.btn_delete_course)
        public void onClickButtonDeleteCourse(View view) {
            mListener.onItemClick((int) itemView.getTag(), view.getId());
        }
    }
}