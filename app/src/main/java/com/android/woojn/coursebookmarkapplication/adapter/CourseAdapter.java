package com.android.woojn.coursebookmarkapplication.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
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

import static com.android.woojn.coursebookmarkapplication.MainActivity.VIEW_ID_OF_ITEM_VIEW;

/**
 * Created by wjn on 2017-02-06.
 */

public class CourseAdapter extends RealmRecyclerViewAdapter<Course, CourseAdapter.CourseViewHolder> {

    private OnRecyclerViewClickListener mListener;

    public CourseAdapter(Context context, OrderedRealmCollection<Course> data, OnRecyclerViewClickListener listener) {
        super(context, data, true);
        this.mListener = listener;
    }

    public interface OnRecyclerViewClickListener {
        void onItemClick(int id, int viewId);
        void onItemLongClick(int id);
    }

    @Override
    public CourseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.course_list_item, parent, false);
        return new CourseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(CourseViewHolder holder, int position) {
        Course course = getData().get(position);

        holder.itemView.setTag(course.getId());
        holder.textViewCourseTitle.setText(course.getTitle());
        holder.textViewCourseDesc.setText(course.getDesc());
        if (course.isFavorite()) {
            holder.imageViewFavoriteN.setVisibility(View.GONE);
            holder.imageViewFavoriteY.setVisibility(View.VISIBLE);
        } else {
            holder.imageViewFavoriteY.setVisibility(View.GONE);
            holder.imageViewFavoriteN.setVisibility(View.VISIBLE);
        }
    }

    class CourseViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener, View.OnLongClickListener {

        @BindView(R.id.tv_course_title)
        TextView textViewCourseTitle;
        @BindView(R.id.tv_course_desc)
        TextView textViewCourseDesc;
        @BindView(R.id.iv_favorite_y_main)
        ImageView imageViewFavoriteY;
        @BindView(R.id.iv_favorite_n_main)
        ImageView imageViewFavoriteN;

        public CourseViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        @OnClick({R.id.iv_favorite_y_main, R.id.iv_favorite_n_main})
        public void onClick(View view) {
            int id = (int) itemView.getTag();
            int viewId = view.getId();

            switch (viewId) {
                case R.id.iv_favorite_y_main:
                    imageViewFavoriteY.setVisibility(View.GONE);
                    imageViewFavoriteN.setVisibility(View.VISIBLE);
                    break;
                case R.id.iv_favorite_n_main:
                    imageViewFavoriteN.setVisibility(View.GONE);
                    imageViewFavoriteY.setVisibility(View.VISIBLE);
                    break;
                default:
                    viewId = VIEW_ID_OF_ITEM_VIEW;
                    break;
            }
            mListener.onItemClick(id, viewId);
        }

        @Override
        public boolean onLongClick(View view) {
            int id = (int) itemView.getTag();
            mListener.onItemLongClick(id);
            return true;
        }
    }
}