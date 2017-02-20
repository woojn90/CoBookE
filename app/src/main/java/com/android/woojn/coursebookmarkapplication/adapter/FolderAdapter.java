package com.android.woojn.coursebookmarkapplication.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.woojn.coursebookmarkapplication.R;
import com.android.woojn.coursebookmarkapplication.model.Folder;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.realm.OrderedRealmCollection;
import io.realm.RealmRecyclerViewAdapter;

/**
 * Created by wjn on 2017-02-18.
 */

public class FolderAdapter extends RealmRecyclerViewAdapter <Folder, FolderAdapter.FolderViewHolder> {

    private final OnRecyclerViewClickListener mListener;

    public FolderAdapter(Context context, OrderedRealmCollection data, OnRecyclerViewClickListener listener) {
        super(context, data, true);
        mListener = listener;
    }

    public interface OnRecyclerViewClickListener {
        void onFolderClick(int id);
        void onItemInFolderClick(int id, View view);
    }

    @Override
    public FolderViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.folder_list_item, parent, false);
        return new FolderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final FolderViewHolder holder, int position) {
        if (getData() != null) {
            Folder folder = getData().get(position);

            holder.itemView.setTag(folder.getId());
            holder.textViewFolderTitle.setText(folder.getTitle());
        }
    }

    class FolderViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {

        @BindView(R.id.tv_folder_title)
        TextView textViewFolderTitle;

        public FolderViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            mListener.onFolderClick((int) itemView.getTag());
        }

        @OnClick(R.id.btn_folder_overflow)
        public void onClickButtonFolderOverflow(View view) {
            mListener.onItemInFolderClick((int) itemView.getTag(), view);
        }
    }

}
