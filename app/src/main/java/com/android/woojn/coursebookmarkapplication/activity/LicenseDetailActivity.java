package com.android.woojn.coursebookmarkapplication.activity;

import static com.android.woojn.coursebookmarkapplication.Constants.KEY_FILE_NAME;
import static com.android.woojn.coursebookmarkapplication.Constants.KEY_TITLE;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.android.woojn.coursebookmarkapplication.R;

import java.io.IOException;
import java.io.InputStream;

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

        String fileName = getIntent().getStringExtra(KEY_FILE_NAME);
        String desc;
        try {
            desc = readTextByFileName(fileName);
        } catch (IOException e) {
            desc = getString(R.string.msg_license_asset_problem);
            e.printStackTrace();
        }
        mTextViewLicenseDesc.setText(desc);
    }

    private String readTextByFileName(String fileName) throws IOException {
        InputStream is = getAssets().open(fileName);
        int size = is.available();
        byte[] buffer = new byte[size];
        is.read(buffer);
        is.close();

        return new String(buffer);
    }
}
