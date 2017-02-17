package com.android.woojn.coursebookmarkapplication.activity;

import static com.android.woojn.coursebookmarkapplication.ConstantClass.ID;
import static com.android.woojn.coursebookmarkapplication.ConstantClass.REQUEST_WEB_ACTIVITY;
import static com.android.woojn.coursebookmarkapplication.ConstantClass.REQUEST_WEB_ACTIVITY_WITHOUT_SAVE;
import static com.android.woojn.coursebookmarkapplication.ConstantClass.REQUEST_WEB_ACTIVITY_WITH_SAVE;
import static com.android.woojn.coursebookmarkapplication.ConstantClass.SECTION_ID;
import static com.android.woojn.coursebookmarkapplication.ConstantClass.STRING_URL;
import static com.android.woojn.coursebookmarkapplication.util.RealmDbUtility.getNewIdByClass;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.woojn.coursebookmarkapplication.R;
import com.android.woojn.coursebookmarkapplication.model.Section;
import com.android.woojn.coursebookmarkapplication.model.SectionDetail;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Realm;

/**
 * Created by wjn on 2017-02-16.
 */

public class WebActivity extends AppCompatActivity {

    @BindView(R.id.pb_web_loading)
    protected ProgressBar mProgressBarWebLoading;
    @BindView(R.id.web_view)
    protected WebView mWebView;

    private int mSectionId;
    private Toast mToast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web);
        ButterKnife.bind(this);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        mSectionId = getIntent().getIntExtra(SECTION_ID, 0);
        String stringUrl = getIntent().getStringExtra(STRING_URL);
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.loadUrl(stringUrl);
        mWebView.setWebViewClient(new WebViewClient() {
            @SuppressWarnings("deprecation")
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }

            @TargetApi(Build.VERSION_CODES.N)
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                view.loadUrl(request.getUrl().toString());
                return true;
            }
        });
        mWebView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                mProgressBarWebLoading.setProgress(newProgress);

                if (newProgress >= 100) {
                    mProgressBarWebLoading.setVisibility(View.GONE);
                } else {
                    mProgressBarWebLoading.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_web, menu);

        int requestCode = getIntent().getIntExtra(REQUEST_WEB_ACTIVITY, REQUEST_WEB_ACTIVITY_WITH_SAVE);
        if (requestCode == REQUEST_WEB_ACTIVITY_WITHOUT_SAVE) {
            menu.findItem(R.id.action_save).setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                break;
            case R.id.action_refresh:
                mWebView.reload();
                break;
            case R.id.action_save:
                saveThisPage();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (mWebView.canGoBack()) {
            mWebView.goBack();
            return;
        }
        super.onBackPressed();
    }

    private void makeToastAfterCancel(int resId) {
        if (mToast != null) {
            mToast.cancel();
        }
        mToast = Toast.makeText(this, resId, Toast.LENGTH_LONG);
        mToast.show();
    }

    private void saveThisPage() {
        int newSectionDetailId = getNewIdByClass(SectionDetail.class);
        Realm realm = Realm.getDefaultInstance();
        Section section = realm.where(Section.class).equalTo(ID, mSectionId).findFirst();
        realm.beginTransaction();
        SectionDetail sectionDetail = realm.createObject(SectionDetail.class, newSectionDetailId);
        sectionDetail.setUrl(mWebView.getUrl());
        section.getSectionDetails().add(sectionDetail);
        realm.commitTransaction();

        makeToastAfterCancel(R.string.msg_save);
    }
}
