package com.android.woojn.coursebookmarkapplication.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
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

public class SectionItemAdapter extends RealmRecyclerViewAdapter<Item, SectionItemAdapter.SectionItemViewHolder> {

    private final OnRecyclerViewClickListener mListener;

    public SectionItemAdapter(Context context, OrderedRealmCollection<Item> data, OnRecyclerViewClickListener listener) {
        super(context, data, true);
        mListener = listener;
    }

    public interface OnRecyclerViewClickListener {
        void onItemClick(int id);
        void onItemLongClick(int id);
        void onItemDoubleTap(int id);
        void onItemInItemClick(int id, View view);
    }

    @Override
    public SectionItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.list_item_of_section_item, parent, false);
        return new SectionItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final SectionItemViewHolder holder, int position) {
        if (getData() != null) {
            Item item = getData().get(position);

            if (item.isVisited()) {
                holder.itemView.setTag(item.getId());
                holder.textViewSectionItemTitle.setText(item.getTitle());
                holder.textViewSectionItemDesc.setText(item.getDesc());
                holder.textViewSectionItemUrl.setText(item.getUrl());

                Glide.with(this.context)
                        .load(item.getImageUrl())
                        .listener(new RequestListener<String, GlideDrawable>() {
                            @Override
                            public boolean onException(Exception e, String model,
                                    Target<GlideDrawable> target, boolean isFirstResource) {
                                holder.progressBarSectionItemPreview.setVisibility(View.INVISIBLE);
                                holder.textViewSectionItemPreview.setVisibility(View.VISIBLE);
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(GlideDrawable resource, String model,
                                    Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                                holder.progressBarSectionItemPreview.setVisibility(View.INVISIBLE);
                                holder.textViewSectionItemPreview.setVisibility(View.VISIBLE);
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

    class SectionItemViewHolder extends RecyclerView.ViewHolder {

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
        @BindView(R.id.tv_section_item_preview)
        TextView textViewSectionItemPreview;
        @BindView(R.id.iv_section_item_preview)
        ImageView imageViewSectionItemPreview;
        @BindView(R.id.pg_section_item_preview)
        ProgressBar progressBarSectionItemPreview;

        public SectionItemViewHolder(final View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            final GestureDetector gd = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onSingleTapConfirmed(MotionEvent e) {
                    mListener.onItemClick((int) itemView.getTag());
                    return true;
                }

                @Override
                public void onLongPress(MotionEvent e) {
                    mListener.onItemLongClick((int) itemView.getTag());
                    return;
                }

                @Override
                public boolean onDoubleTap(MotionEvent e) {
                    mListener.onItemDoubleTap((int) itemView.getTag());
                    return true;
                }
            });

            itemView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    gd.onTouchEvent(event);
                    return true;
                }
            });
        }

        @OnClick(R.id.btn_section_item_overflow)
        public void onClickButtonSectionItemOverflow(View view) {
            mListener.onItemInItemClick((int) itemView.getTag(), view);
        }
    }
}