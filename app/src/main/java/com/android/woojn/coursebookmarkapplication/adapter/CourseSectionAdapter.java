package com.android.woojn.coursebookmarkapplication.adapter;

import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.android.woojn.coursebookmarkapplication.R;

/**
 * Created by wjn on 2017-02-07.
 */

public class CourseSectionAdapter extends RecyclerView.Adapter<CourseSectionAdapter.CourseSectionViewHolder> {

    private Context mContext;
    private Cursor mCursor;

    public CourseSectionAdapter(Context context, Cursor cursor) {
        this.mContext = context;
        this.mCursor = cursor;
    }

    @Override
    public CourseSectionViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.course_section_list_item, parent, false);
        return new CourseSectionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(CourseSectionViewHolder holder, int position) {
        if (!mCursor.moveToPosition(position)) return;

        // TODO DB 설계 후 수정
        long id = mCursor.getLong(mCursor.getColumnIndex("_ID"));
        String title = mCursor.getString(mCursor.getColumnIndex("title"));

        holder.itemView.setTag(id);
        holder.sectionTitleTextView.setText(title);

        // Adapter
        Cursor fakeCursor = getAllCourse();
        CourseSectionDetailAdapter adapter = new CourseSectionDetailAdapter(mContext, fakeCursor);
        holder.sectionDetailRecyclerView.setAdapter(adapter);
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

    private Cursor getAllCourse() {
        // TODO delete fake data & make db query

        // fake data
        String[] columns = new String[] { "_id", "title", "sub_title"};
        MatrixCursor matrixCursor= new MatrixCursor(columns);
        matrixCursor.addRow(new Object[] { 7, "blog1", "blog content1"});
        matrixCursor.addRow(new Object[] { 8, "blog2", "blog content2"});
        matrixCursor.addRow(new Object[] { 9, "blog3", "blog content3"});
        matrixCursor.addRow(new Object[] { 10, "blog4", "blog content is very very long so that should be ..."});

        Cursor cursor = matrixCursor;

        return cursor;
    }

    class CourseSectionViewHolder extends RecyclerView.ViewHolder {

        TextView sectionTitleTextView;
        Button sectionShareButton;
        Button sectionDeleteButton;
        RecyclerView sectionDetailRecyclerView;

        public CourseSectionViewHolder(View itemView) {
            super(itemView);
            sectionTitleTextView = (TextView) itemView.findViewById(R.id.tv_section_title);
            sectionShareButton = (Button) itemView.findViewById(R.id.btn_share_section);
            sectionDeleteButton = (Button) itemView.findViewById(R.id.btn_delete_section);
            sectionDetailRecyclerView = (RecyclerView) itemView.findViewById(R.id.rv_course_section_detail_list);
            sectionDetailRecyclerView.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false));
        }
    }
}
