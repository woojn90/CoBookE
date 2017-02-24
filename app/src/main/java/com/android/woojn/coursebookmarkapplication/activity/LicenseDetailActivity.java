package com.android.woojn.coursebookmarkapplication.activity;

import static com.android.woojn.coursebookmarkapplication.Constants.KEY_DESC;
import static com.android.woojn.coursebookmarkapplication.Constants.KEY_TITLE;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.android.woojn.coursebookmarkapplication.R;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by wjn on 2017-02-24.
 */

public class LicenseDetailActivity extends AppCompatActivity {

    @BindView(R.id.tv_license_desc)
    protected TextView mTextViewLicenseDesc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String title = getIntent().getStringExtra(KEY_TITLE);
        setTitle(title);
        setContentView(R.layout.activity_license_detail);
        ButterKnife.bind(this);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        String desc = getIntent().getStringExtra(KEY_DESC);
        mTextViewLicenseDesc.setText(desc);
    }
}
