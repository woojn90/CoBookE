package com.android.woojn.coursebookmarkapplication.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
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
        void onFolderLongClick(int id);
        void onFolderDoubleTap(int id);
        void onItemInFolderClick(int id, View view);
    }

    @Override
    public FolderViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.list_item_of_folder, parent, false);
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

    class FolderViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.tv_folder_title)
        TextView textViewFolderTitle;

        public FolderViewHolder(final View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            final GestureDetector gd = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onSingleTapConfirmed(MotionEvent e) {
                    mListener.onFolderClick((int) itemView.getTag());
                    return true;
                }

                @Override
                public void onLongPress(MotionEvent e) {
                    mListener.onFolderLongClick((int) itemView.getTag());
                    return;
                }

                @Override
                public boolean onDoubleTap(MotionEvent e) {
                    mListener.onFolderDoubleTap((int) itemView.getTag());
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

        @OnClick(R.id.btn_folder_overflow)
        public void onClickButtonFolderOverflow(View view) {
            mListener.onItemInFolderClick((int) itemView.getTag(), view);
        }
    }
}
