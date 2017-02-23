package com.android.woojn.coursebookmarkapplication.adapter;

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
 * Created by wjn on 2017-02-18.
 */

public class ItemAdapter extends RealmRecyclerViewAdapter <Item, ItemAdapter.ItemViewHolder> {

    private final OnRecyclerViewClickListener mListener;

    public ItemAdapter(Context context, OrderedRealmCollection data, OnRecyclerViewClickListener listener) {
        super(context, data, true);
        mListener = listener;
    }

    public interface OnRecyclerViewClickListener {
        void onItemClick(int id);
        void onItemInItemClick(int id, View view);
    }

    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.item_list_item, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ItemViewHolder holder, int position) {
        if (getData() != null) {
            Item item = getData().get(position);

            if (item.isVisited()) {
                holder.itemView.setTag(item.getId());
                holder.textViewItemTitle.setText(item.getTitle());
                holder.textViewItemDesc.setText(item.getDesc());
                holder.textViewItemUrl.setText(item.getUrl());

                Glide.with(this.context)
                        .load(item.getImageUrl())
                        .listener(new RequestListener<String, GlideDrawable>() {
                            @Override
                            public boolean onException(Exception e, String model,
                                    Target<GlideDrawable> target, boolean isFirstResource) {
                                holder.progressBarItemPreview.setVisibility(View.INVISIBLE);
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(GlideDrawable resource, String model,
                                    Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                                holder.progressBarItemPreview.setVisibility(View.INVISIBLE);
                                return false;
                            }
                        })
                        .thumbnail(0.1f)
                        .centerCrop()
                        .into(holder.imageViewItemPreview);

                holder.progressBarItem.setVisibility(View.INVISIBLE);
                holder.linearLayoutItem.setVisibility(View.VISIBLE);
            } else {
                holder.linearLayoutItem.setVisibility(View.INVISIBLE);
                holder.progressBarItem.setVisibility(View.VISIBLE);
            }
        }
    }

    class ItemViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {

        @BindView(R.id.layout_item)
        LinearLayout linearLayoutItem;
        @BindView(R.id.pg_item)
        ProgressBar progressBarItem;
        @BindView(R.id.tv_item_title)
        TextView textViewItemTitle;
        @BindView(R.id.tv_item_desc)
        TextView textViewItemDesc;
        @BindView(R.id.tv_item_url)
        TextView textViewItemUrl;
        @BindView(R.id.iv_item_preview)
        ImageView imageViewItemPreview;
        @BindView(R.id.pg_item_preview)
        ProgressBar progressBarItemPreview;

        public ItemViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            mListener.onItemClick((int) itemView.getTag());
        }

        @OnClick(R.id.btn_item_overflow)
        public void onClickButtonItemOverflow(View view) {
            mListener.onItemInItemClick((int) itemView.getTag(), view);
        }
    }

}
