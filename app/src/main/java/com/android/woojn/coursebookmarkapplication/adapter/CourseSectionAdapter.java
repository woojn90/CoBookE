package com.android.woojn.coursebookmarkapplication.adapter;

import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.android.woojn.coursebookmarkapplication.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by wjn on 2017-02-07.
 */

public class CourseSectionAdapter extends RecyclerView.Adapter<CourseSectionAdapter.CourseSectionViewHolder> {

    private final Context mContext;
    private Cursor mCursor;
    private OnButtonInItemClickListener mListener;

    public CourseSectionAdapter(Context context, Cursor cursor, OnButtonInItemClickListener listener) {
        this.mContext = context;
        this.mCursor = cursor;
        this.mListener = listener;
    }

    public interface OnButtonInItemClickListener {
        void onButtonInItemClick(long id, int viewId);
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

        // TODO: DB 설계 후 수정
        long id = mCursor.getLong(mCursor.getColumnIndex("_ID"));
        String title = mCursor.getString(mCursor.getColumnIndex("title"));

        holder.itemView.setTag(id);
        holder.textViewSectionTitle.setText(title);

        // Adapter
        Cursor fakeCursor = getAllCourse();
        CourseSectionDetailAdapter adapter = new CourseSectionDetailAdapter(mContext, fakeCursor, holder);
        holder.recyclerViewCourseSectionDetail.setAdapter(adapter);
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
        // TODO: delete fake data & make db query

        // fake data
        String[] columns = new String[] { "_id", "title", "sub_title" };
        MatrixCursor matrixCursor = new MatrixCursor(columns);
        matrixCursor.addRow(new Object[] { 7, "blog1", "blog content1" });
        matrixCursor.addRow(new Object[] { 8, "blog2", "blog content2" });
        matrixCursor.addRow(new Object[] { 9, "blog3", "blog content3" });
        matrixCursor.addRow(new Object[] { 10, "blog4", "blog content is very very long so that should be ..." });

        Cursor cursor = matrixCursor;

        return cursor;
    }

    class CourseSectionViewHolder extends RecyclerView.ViewHolder
            implements CourseSectionDetailAdapter.OnButtonInItemClickListener {

        @BindView(R.id.tv_section_title)
        TextView textViewSectionTitle;
        @BindView(R.id.rv_course_section_detail_list)
        RecyclerView recyclerViewCourseSectionDetail;

        public CourseSectionViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            recyclerViewCourseSectionDetail.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false));
        }

        @OnClick({R.id.btn_share_section, R.id.btn_delete_section})
        public void onClick(View view) {
            long id = (long) itemView.getTag();
            int viewId = view.getId();

            mListener.onButtonInItemClick(id, viewId);
        }

        @Override
        public void onButtonInItemClick(long id, int viewId) {
            switch (viewId) {
                case -1:
                    Toast.makeText(mContext, "just item click / id : " + id, Toast.LENGTH_LONG).show();
                    break;
                case R.id.btn_share_section_detail:
                    Toast.makeText(mContext, "share / id : " + id, Toast.LENGTH_LONG).show();
                    break;
                case R.id.btn_delete_section_detail:
                    Toast.makeText(mContext, "delete / id : " + id, Toast.LENGTH_LONG).show();
                    break;
            }
        }
    }
}
