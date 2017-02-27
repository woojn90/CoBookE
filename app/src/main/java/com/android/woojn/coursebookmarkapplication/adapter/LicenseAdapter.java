package com.android.woojn.coursebookmarkapplication.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.woojn.coursebookmarkapplication.R;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by wjn on 2017-02-24.
 */

public class LicenseAdapter extends RecyclerView.Adapter<LicenseAdapter.LicenseViewHolder> {

    private Context mContext;
    private ArrayList<String[]> mArrayList;
    private final OnRecyclerViewClickListener mListener;

    public LicenseAdapter(Context context, ArrayList<String[]> arrayList, OnRecyclerViewClickListener listener) {
        mContext = context;
        mArrayList = arrayList;
        mListener = listener;
    }

    public interface OnRecyclerViewClickListener {
        void onItemClick(String[] titleAndDesc);
    }

    @Override
    public LicenseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.list_item_of_license, parent, false);
        return new LicenseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(LicenseViewHolder holder, int position) {
        if (mArrayList.get(position) != null) {
            String title = mArrayList.get(position)[0];

            holder.textViewLicenseTitle.setText(title);
            holder.itemView.setTag(mArrayList.get(position));
        }
    }

    @Override
    public int getItemCount() {
        return mArrayList.size();
    }

    class LicenseViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        @BindView(R.id.tv_license_title)
        TextView textViewLicenseTitle;

        public LicenseViewHolder(final View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            mListener.onItemClick((String[]) itemView.getTag());
        }
    }
}