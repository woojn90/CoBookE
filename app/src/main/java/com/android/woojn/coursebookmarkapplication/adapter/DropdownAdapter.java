package com.android.woojn.coursebookmarkapplication.adapter;

import static com.android.woojn.coursebookmarkapplication.Constants.DEFAULT_FOLDER_ID;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.woojn.coursebookmarkapplication.R;
import com.android.woojn.coursebookmarkapplication.model.Folder;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by wjn on 2017-02-23.
 */

public class DropdownAdapter extends ArrayAdapter<Folder> {

    @BindView(R.id.tv_dropdown_folder)
    protected TextView mTextViewDropdownFolder;
    @BindView(R.id.iv_dropdown_folder)
    protected ImageView mImageViewDropdownFolder;

    private LayoutInflater mLayoutInflater;
    private ArrayList<Folder> mFolders;

    public DropdownAdapter(Context context, int resource, int textViewResourceId, ArrayList<Folder> objects,
            LayoutInflater layoutInflater) {
        super(context, resource, textViewResourceId, objects);
        mLayoutInflater = layoutInflater;
        mFolders = objects;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return getCustomView(position, parent);
    }

    private View getCustomView(int position, ViewGroup parent) {
        View view = mLayoutInflater.inflate(R.layout.dropdown_folder, parent, false);
        ButterKnife.bind(this, view);

        mTextViewDropdownFolder.setText((mFolders.get(position)).toString());
        if (mFolders.get(position).getId() != DEFAULT_FOLDER_ID) {
            mImageViewDropdownFolder.setBackgroundResource(R.drawable.ic_folder);
        }
        return view;
    }

}
