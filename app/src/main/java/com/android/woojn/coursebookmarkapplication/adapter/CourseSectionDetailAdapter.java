package com.android.woojn.coursebookmarkapplication.adapter;

import static com.android.woojn.coursebookmarkapplication.ConstantClass.VIEW_ID_OF_ITEM_VIEW;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.woojn.coursebookmarkapplication.R;
import com.android.woojn.coursebookmarkapplication.model.SectionDetail;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.realm.OrderedRealmCollection;
import io.realm.RealmRecyclerViewAdapter;

/**
 * Created by wjn on 2017-02-07.
 */

public class CourseSectionDetailAdapter extends RealmRecyclerViewAdapter<SectionDetail, CourseSectionDetailAdapter.CourseSectionDetailViewHolder> {

    private final OnRecyclerViewClickListener mListener;

    public CourseSectionDetailAdapter(Context context, OrderedRealmCollection<SectionDetail> data, OnRecyclerViewClickListener listener) {
        super(context, data, true);
        mListener = listener;
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
    public void onBindViewHolder(final CourseSectionDetailViewHolder holder, int position) {
        if (getData() != null) {
            SectionDetail sectionDetail = getData().get(position);
            holder.itemView.setTag(sectionDetail.getId());
            holder.textViewSectionDetailTitle.setText(sectionDetail.getTitle());
            holder.textViewSectionDetailDesc.setText(sectionDetail.getDesc());
            holder.textViewSectionDetailUrl.setText(sectionDetail.getUrl());

            Glide.with(this.context)
                    .load(sectionDetail.getImageUrl())
                    .listener(new RequestListener<String, GlideDrawable>() {
                        @Override
                        public boolean onException(Exception e, String model,
                                Target<GlideDrawable> target, boolean isFirstResource) {
                            holder.progressBarCourseImageLoading.setVisibility(View.GONE);
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(GlideDrawable resource, String model,
                                Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                            holder.progressBarCourseImageLoading.setVisibility(View.GONE);
                            return false;
                        }
                    })
                    .thumbnail(0.1f)
                    .centerCrop()
                    .into(holder.imageViewSectionDetailPreview);
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
        @BindView(R.id.iv_section_detail_preview)
        ImageView imageViewSectionDetailPreview;
        @BindView(R.id.pg_course_image_loading)
        ProgressBar progressBarCourseImageLoading;

        public CourseSectionDetailViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            mListener.onItemClick((int) itemView.getTag(), VIEW_ID_OF_ITEM_VIEW);
        }

        @OnClick(R.id.btn_share_section_detail)
        public void onClickButtonShareSectionDetail(View view) {
            mListener.onItemClick((int) itemView.getTag(), view.getId());
        }

        @OnClick(R.id.btn_delete_section_detail)
        public void onClickButtonDeleteSectionDetail(View view) {
            mListener.onItemClick((int) itemView.getTag(), view.getId());
        }
    }
}