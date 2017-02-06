package com.android.woojn.coursebookmarkapplication.adapter;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.woojn.coursebookmarkapplication.R;

/**
 * Created by wjn on 2017-02-06.
 */

public class CourseAdapter extends RecyclerView.Adapter<CourseAdapter.CourseViewHolder> {

    private Context mContext;
    private Cursor mCursor;

    public CourseAdapter(Context context, Cursor cursor) {
        this.mContext = context;
        this.mCursor = cursor;
    }

    @Override
    public CourseAdapter.CourseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.course_list_item, parent, false);
        return new CourseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(CourseAdapter.CourseViewHolder holder, int position) {
        if (!mCursor.moveToPosition(position)) return;

        // TODO DB 설계 후 수정
        long id = mCursor.getLong(mCursor.getColumnIndex("_ID"));
        String text = mCursor.getString(mCursor.getColumnIndex("text"));
        String description = mCursor.getString(mCursor.getColumnIndex("description"));

        holder.itemView.setTag(id);
        holder.textTextView.setText(text);
        holder.descriptionTextView.setText(description);
    }

    @Override
    public int getItemCount() {
        return mCursor.getCount();
    }

    public void swapCursor(Cursor newCursor) {
        if (mCursor != null) mCursor.close();
        mCursor = newCursor;
        if (newCursor != null) {
            this.notifyDataSetChanged();
        }
    }

    class CourseViewHolder extends RecyclerView.ViewHolder {

        TextView textTextView;
        TextView descriptionTextView;

        public CourseViewHolder(View itemView) {
            super(itemView);
            textTextView = (TextView) itemView.findViewById(R.id.tv_text_test1);
            descriptionTextView = (TextView) itemView.findViewById(R.id.tv_text_test2);
        }
    }
}
