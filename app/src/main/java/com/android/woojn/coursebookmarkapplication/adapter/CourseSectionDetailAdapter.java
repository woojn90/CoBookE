package com.android.woojn.coursebookmarkapplication.adapter;

import android.content.Context;
import android.database.Cursor;
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

public class CourseSectionDetailAdapter extends RecyclerView.Adapter<CourseSectionDetailAdapter.CourseSectionDetailViewHolder> {

    private Context mContext;
    private Cursor mCursor;

    public CourseSectionDetailAdapter(Context context, Cursor cursor) {
        this.mContext = context;
        this.mCursor = cursor;
    }

    @Override
    public CourseSectionDetailViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.course_section_detail_list_item, parent, false);
        return new CourseSectionDetailViewHolder(view);
    }

    @Override
    public void onBindViewHolder(CourseSectionDetailViewHolder holder, int position) {
        if (!mCursor.moveToPosition(position)) return;

        // TODO DB 설계 후 수정
        long id = mCursor.getLong(mCursor.getColumnIndex("_ID"));
        String title = mCursor.getString(mCursor.getColumnIndex("title"));
        String subTitle = mCursor.getString(mCursor.getColumnIndex("sub_title"));

        holder.itemView.setTag(id);
        holder.sectionDetailTitleTextView.setText(title);
        holder.sectionDetailSubTitleTextView.setText(subTitle);
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

    class CourseSectionDetailViewHolder extends RecyclerView.ViewHolder {

        TextView sectionDetailTitleTextView;
        TextView sectionDetailSubTitleTextView;
        Button sectionDetailShareButton;
        Button sectionDetailDeleteButton;

        public CourseSectionDetailViewHolder(View itemView) {
            super(itemView);
            sectionDetailTitleTextView = (TextView) itemView.findViewById(R.id.tv_section_detail_title);
            sectionDetailSubTitleTextView = (TextView) itemView.findViewById(R.id.tv_section_detail_subtitle);
            sectionDetailShareButton = (Button) itemView.findViewById(R.id.btn_share_section_detail);
            sectionDetailDeleteButton = (Button) itemView.findViewById(R.id.btn_delete_section_detail);
        }
    }
}
