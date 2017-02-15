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

import static com.android.woojn.coursebookmarkapplication.MainActivity.VIEW_ID_OF_ITEM_VIEW;

/**
 * Created by wjn on 2017-02-07.
 */

public class CourseSectionDetailAdapter extends RealmRecyclerViewAdapter<SectionDetail, CourseSectionDetailAdapter.CourseSectionDetailViewHolder> {

    private final OnRecyclerViewClickListener mListener;

    public CourseSectionDetailAdapter(Context context, OrderedRealmCollection<SectionDetail> data, OnRecyclerViewClickListener listener) {
        super(context, data, true);
        this.mListener = listener;
    }

    public interface OnRecyclerViewClickListener {
        void onItemClick(int id, int viewId);
    }

    @Override
    public CourseSectionDetailViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.course_section_detail_list_item, parent, false);
        return new CourseSectionDetailViewHolder(view);
    }

    @Override
    public void onBindViewHolder(CourseSectionDetailViewHolder holder, int position) {
        if (getData() != null) {
            SectionDetail sectionDetail = getData().get(position);
            holder.itemView.setTag(sectionDetail.getId());
            holder.textViewSectionDetailTitle.setText(sectionDetail.getTitle());
            holder.textViewSectionDetailDesc.setText(sectionDetail.getDesc());
            holder.textViewSectionDetailUrl.setText(sectionDetail.getUrl());
        }
    }

    class CourseSectionDetailViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {

        @BindView(R.id.tv_section_detail_title)
        TextView textViewSectionDetailTitle;
        @BindView(R.id.tv_section_detail_desc)
        TextView textViewSectionDetailDesc;
        @BindView(R.id.tv_section_detail_url)
        TextView textViewSectionDetailUrl;

        public CourseSectionDetailViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            itemView.setOnClickListener(this);
        }

        @OnClick({R.id.btn_share_section_detail, R.id.btn_delete_section_detail})
        public void onClick(View view) {
            int id = (int) itemView.getTag();
            int viewId = view.getId();

            switch (viewId) {
                case R.id.btn_share_section_detail:
                case R.id.btn_delete_section_detail:
                    break;
                default:
                    viewId = VIEW_ID_OF_ITEM_VIEW;
                    break;
            }
            mListener.onItemClick(id, viewId);
        }
    }
}