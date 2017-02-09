package com.android.woojn.coursebookmarkapplication.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.woojn.coursebookmarkapplication.R;
import com.android.woojn.coursebookmarkapplication.model.SectionDetail;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.realm.OrderedRealmCollection;
import io.realm.RealmRecyclerViewAdapter;

/**
 * Created by wjn on 2017-02-07.
 */

public class CourseSectionDetailAdapter extends RealmRecyclerViewAdapter<SectionDetail, CourseSectionDetailAdapter.CourseSectionDetailViewHolder> {

    private OnRecyclerViewClickListener mListener;

    public CourseSectionDetailAdapter(final Context context, OrderedRealmCollection<SectionDetail> data, OnRecyclerViewClickListener listener) {
        super(context, data, true);
        this.mListener = listener;
    }

    public interface OnRecyclerViewClickListener {
        void onItemClick(int id, int viewId);
    }

    @Override
    public CourseSectionDetailViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.course_section_detail_list_item, parent, false);
        return new CourseSectionDetailViewHolder(view);
    }

    @Override
    public void onBindViewHolder(CourseSectionDetailViewHolder holder, int position) {
        SectionDetail sectionDetail = getData().get(position);

        holder.itemView.setTag(sectionDetail.getId());
        holder.sectionDetailTitleTextView.setText(sectionDetail.getTitle());
        holder.sectionDetailDescTextView.setText(sectionDetail.getDesc());
    }

    class CourseSectionDetailViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        @BindView(R.id.tv_section_detail_title)
        TextView sectionDetailTitleTextView;
        @BindView(R.id.tv_section_detail_desc)
        TextView sectionDetailDescTextView;

        public CourseSectionDetailViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            itemView.setOnClickListener(this);
        }

        @OnClick({R.id.btn_share_section_detail, R.id.btn_delete_section_detail})
        public void onClick(View view) {
            int id = (int) itemView.getTag();
            int viewId = view.getId();

            mListener.onItemClick(id, viewId);
        }
    }
}