package com.android.woojn.coursebookmarkapplication.adapter;

import static com.android.woojn.coursebookmarkapplication.Constants.DEFAULT_VIEW_ID;
import static com.android.woojn.coursebookmarkapplication.util.DisplayUtility.getUnderlineText;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.woojn.coursebookmarkapplication.R;
import com.android.woojn.coursebookmarkapplication.model.Item;

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

public class CourseSectionItemAdapter extends RealmRecyclerViewAdapter<Item, CourseSectionItemAdapter.CourseSectionItemViewHolder> {

    private final OnRecyclerViewClickListener mListener;

    public CourseSectionItemAdapter(Context context, OrderedRealmCollection<Item> data, OnRecyclerViewClickListener listener) {
        super(context, data, true);
        mListener = listener;
    }

    public interface OnRecyclerViewClickListener {
        void onItemClick(int id, int viewId);
    }

    @Override
    public CourseSectionItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.course_section_item_list_item, parent, false);
        return new CourseSectionItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final CourseSectionItemViewHolder holder, int position) {
        if (getData() != null) {
            Item item = getData().get(position);

            if (item.isVisited()) {
                holder.itemView.setTag(item.getId());
                holder.textViewSectionItemTitle.setText(item.getTitle());
                holder.textViewSectionItemDesc.setText(item.getDesc());
                holder.textViewSectionItemUrl.setText(getUnderlineText(item.getUrl()));

                Glide.with(this.context)
                        .load(item.getImageUrl())
                        .listener(new RequestListener<String, GlideDrawable>() {
                            @Override
                            public boolean onException(Exception e, String model,
                                    Target<GlideDrawable> target, boolean isFirstResource) {
                                holder.progressBarSectionItemPreview.setVisibility(View.INVISIBLE);
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(GlideDrawable resource, String model,
                                    Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                                holder.progressBarSectionItemPreview.setVisibility(View.INVISIBLE);
                                return false;
                            }
                        })
                        .thumbnail(0.1f)
                        .centerCrop()
                        .into(holder.imageViewSectionItemPreview);

                holder.progressBarSectionItem.setVisibility(View.INVISIBLE);
                holder.linearLayoutSectionItem.setVisibility(View.VISIBLE);
            } else {
                holder.linearLayoutSectionItem.setVisibility(View.INVISIBLE);
                holder.progressBarSectionItem.setVisibility(View.VISIBLE);
            }
        }
    }

    class CourseSectionItemViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {

        @BindView(R.id.layout_section_item)
        LinearLayout linearLayoutSectionItem;
        @BindView(R.id.pg_section_item)
        ProgressBar progressBarSectionItem;
        @BindView(R.id.tv_section_item_title)
        TextView textViewSectionItemTitle;
        @BindView(R.id.tv_section_item_desc)
        TextView textViewSectionItemDesc;
        @BindView(R.id.tv_section_item_url)
        TextView textViewSectionItemUrl;
        @BindView(R.id.iv_section_item_preview)
        ImageView imageViewSectionItemPreview;
        @BindView(R.id.pg_section_item_preview)
        ProgressBar progressBarSectionItemPreview;

        public CourseSectionItemViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            mListener.onItemClick((int) itemView.getTag(), DEFAULT_VIEW_ID);
        }

        @OnClick(R.id.btn_share_section_item)
        public void onClickButtonShareSectionDetail(View view) {
            mListener.onItemClick((int) itemView.getTag(), view.getId());
        }

        @OnClick(R.id.btn_delete_section_item)
        public void onClickButtonDeleteSectionDetail(View view) {
            mListener.onItemClick((int) itemView.getTag(), view.getId());
        }
    }
}