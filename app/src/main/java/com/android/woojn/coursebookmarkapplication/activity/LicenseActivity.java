package com.android.woojn.coursebookmarkapplication.activity;

import static com.android.woojn.coursebookmarkapplication.Constants.FILE_NAME_LICENSE_APACHE;
import static com.android.woojn.coursebookmarkapplication.Constants.FILE_NAME_LICENSE_BUTTERKNIFE;
import static com.android.woojn.coursebookmarkapplication.Constants.FILE_NAME_LICENSE_GLIDE;
import static com.android.woojn.coursebookmarkapplication.Constants.FILE_NAME_LICENSE_JSOUP;
import static com.android.woojn.coursebookmarkapplication.Constants.FILE_NAME_LICENSE_ONBOARDER;
import static com.android.woojn.coursebookmarkapplication.Constants.FILE_NAME_LICENSE_REALM;
import static com.android.woojn.coursebookmarkapplication.Constants.KEY_FILE_NAME;
import static com.android.woojn.coursebookmarkapplication.Constants.KEY_TITLE;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.android.woojn.coursebookmarkapplication.R;
import com.android.woojn.coursebookmarkapplication.adapter.LicenseAdapter;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by wjn on 2017-02-24.
 */

public class LicenseActivity extends AppCompatActivity implements LicenseAdapter.OnRecyclerViewClickListener {

    @BindView(R.id.rv_license_list)
    protected RecyclerView mRecyclerViewLicenseList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_license);
        ButterKnife.bind(this);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        mRecyclerViewLicenseList.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerViewLicenseList.setAdapter(new LicenseAdapter(this, initAllLicense(), this));
    }

    @Override
    public void onItemClick(String[] titleAndFileName) {
        Intent intent = new Intent(this, LicenseDetailActivity.class);
        intent.putExtra(KEY_TITLE, titleAndFileName[0]);
        intent.putExtra(KEY_FILE_NAME, titleAndFileName[1]);
        startActivity(intent);
    }

    private ArrayList<String[]> initAllLicense() {
        ArrayList<String[]> arrayList = new ArrayList<>();
        arrayList.add(new String[] {"Android CardView Library", FILE_NAME_LICENSE_APACHE});
        arrayList.add(new String[] {"Android Compatibility Library v7", FILE_NAME_LICENSE_APACHE});
        arrayList.add(new String[] {"Android Design Library", FILE_NAME_LICENSE_APACHE});
        arrayList.add(new String[] {"Android Preference Library", FILE_NAME_LICENSE_APACHE});
        arrayList.add(new String[] {"Android RecyclerView Library", FILE_NAME_LICENSE_APACHE});
        arrayList.add(new String[] {"ButterKnife", FILE_NAME_LICENSE_BUTTERKNIFE});
        arrayList.add(new String[] {"Glide", FILE_NAME_LICENSE_GLIDE});
        arrayList.add(new String[] {"Jsoup", FILE_NAME_LICENSE_JSOUP});
        arrayList.add(new String[] {"Onboarder", FILE_NAME_LICENSE_ONBOARDER});
        arrayList.add(new String[] {"Realm", FILE_NAME_LICENSE_REALM});
        return arrayList;
    }
}
