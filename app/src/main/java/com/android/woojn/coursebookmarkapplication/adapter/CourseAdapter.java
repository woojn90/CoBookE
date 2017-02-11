package com.android.woojn.coursebookmarkapplication.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

    private OnRecyclerViewClickListener mListener;

    public CourseAdapter(final Context context, OrderedRealmCollection<Course> data, OnRecyclerViewClickListener listener) {
        super(context, data, true);
        this.mListener = listener;
    }

    public interface OnRecyclerViewClickListener {
        void onItemClick(int id, int viewId);
        void onItemLongClick(int id, int viewId);
    }

    @Override
    public CourseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.course_list_item, parent, false);
        return new CourseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(CourseViewHolder holder, int position) {
        Course course = getData().get(position);

        holder.itemView.setTag(course.getId());
        holder.textViewCourseTitle.setText(course.getTitle());
        holder.textViewCourseDesc.setText(course.getDesc());
        holder.textViewCourseFavorite.setText(
                course.isFavorite() ? "Y" : "N");
    }

    class CourseViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

        @BindView(R.id.tv_course_title)
        TextView textViewCourseTitle;
        @BindView(R.id.tv_course_desc)
        TextView textViewCourseDesc;
        // TODO: 별 모양 Image로 변경 (로직도 적용)
        @BindView(R.id.tv_course_favorite)
        TextView textViewCourseFavorite;

        public CourseViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        @OnClick(R.id.tv_course_favorite)
        public void onClick(View view) {
            int id = (int) itemView.getTag();
            int viewId = view.getId();

            mListener.onItemClick(id, viewId);

            switch (viewId) {
                case R.id.tv_course_favorite:
                    // TODO: 이미지 변경
                    break;
            }
        }

        @Override
        public boolean onLongClick(View view) {
            int id = (int) itemView.getTag();
            int viewId = view.getId();

            mListener.onItemLongClick(id, viewId);
            return true;
        }
    }
}