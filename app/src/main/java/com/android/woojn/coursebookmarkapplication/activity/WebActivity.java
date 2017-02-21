package com.android.woojn.coursebookmarkapplication.activity;

import static com.android.woojn.coursebookmarkapplication.Constants.DEFAULT_FOLDER_ID;
import static com.android.woojn.coursebookmarkapplication.Constants.DEFAULT_SECTION_ID;
import static com.android.woojn.coursebookmarkapplication.Constants.FIELD_NAME_ID;
import static com.android.woojn.coursebookmarkapplication.Constants.KEY_FOLDER_ID;
import static com.android.woojn.coursebookmarkapplication.Constants.KEY_REQUEST_WEB_ACTIVITY;
import static com.android.woojn.coursebookmarkapplication.Constants.KEY_SECTION_ID;
import static com.android.woojn.coursebookmarkapplication.Constants.KEY_STRING_URL;
import static com.android.woojn.coursebookmarkapplication.Constants.REQUEST_WEB_ACTIVITY_WITHOUT_SAVE;
import static com.android.woojn.coursebookmarkapplication.Constants.REQUEST_WEB_ACTIVITY_WITH_SAVE;
import static com.android.woojn.coursebookmarkapplication.util.RealmDbUtility.getNewIdByClass;

import android.annotation.TargetApi;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.woojn.coursebookmarkapplication.R;
import com.android.woojn.coursebookmarkapplication.model.Folder;
import com.android.woojn.coursebookmarkapplication.model.Item;
import com.android.woojn.coursebookmarkapplication.model.Section;
import com.android.woojn.coursebookmarkapplication.model.SectionDetail;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.realm.Realm;

/**
 * Created by wjn on 2017-02-16.
 */

public class WebActivity extends AppCompatActivity {

    @BindView(R.id.pb_web_loading)
    protected ProgressBar mProgressBarWebLoading;
    @BindView(R.id.web_view)
    protected WebView mWebView;
    @BindView(R.id.btn_page_back)
    protected Button mButtonPageBack;
    @BindView(R.id.btn_page_forward)
    protected Button mButtonPageForward;
    @BindView(R.id.et_url)
    protected EditText mEditTextUrl;

    private SharedPreferences mSharedPreferences;
    private int mSectionId;
    private int mFolderId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web);
        ButterKnife.bind(this);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        mSectionId = getIntent().getIntExtra(KEY_SECTION_ID, DEFAULT_SECTION_ID);
        mFolderId = getIntent().getIntExtra(KEY_FOLDER_ID, DEFAULT_FOLDER_ID);
        String stringUrl = getIntent().getStringExtra(KEY_STRING_URL);
        if (stringUrl == null || stringUrl.length() == 0) {
            Toast.makeText(this, R.string.msg_invalid_url, Toast.LENGTH_LONG).show();
            stringUrl = mSharedPreferences.getString(getString(R.string.pref_key_home_page)
                    , getString(R.string.pref_value_home_page_naver));
        }

        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.loadUrl(stringUrl);
        mWebView.setWebViewClient(new WebViewClient() {
            @SuppressWarnings("deprecation")
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                mEditTextUrl.setText(mWebView.getUrl());
                setButtonsEnable();
                return true;
            }

            @TargetApi(Build.VERSION_CODES.N)
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                view.loadUrl(request.getUrl().toString());
                mEditTextUrl.setText(mWebView.getUrl());
                return true;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                setButtonsEnable();
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

        mEditTextUrl.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                int result = actionId & EditorInfo.IME_MASK_ACTION;
                if (result == EditorInfo.IME_ACTION_DONE) {
                    String stringUrl = mEditTextUrl.getText().toString();
                    mWebView.loadUrl(stringUrl);
                    setButtonsEnable();
                    return true;
                }
                return false;
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_web, menu);

        int requestCode = getIntent().getIntExtra(KEY_REQUEST_WEB_ACTIVITY, REQUEST_WEB_ACTIVITY_WITH_SAVE);
        if (requestCode == REQUEST_WEB_ACTIVITY_WITHOUT_SAVE) {
            menu.findItem(R.id.action_save).setVisible(false);
            // TODO: 목록 띄워서 저장 가능하게
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
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

    @OnClick(R.id.btn_page_back)
    public void onClickButtonPageBack() {
        if (mWebView.canGoBack()) {
            mWebView.goBack();
        }
    }

    @OnClick(R.id.btn_page_forward)
    public void onClickButtonPageFront() {
        if (mWebView.canGoForward()) {
            mWebView.goForward();
        }
    }

    @OnClick(R.id.btn_page_home)
    public void onClickButtonPageHome() {
        String homePageUrl = mSharedPreferences.getString(getString(R.string.pref_key_home_page)
                , getString(R.string.pref_value_home_page_naver));
        mWebView.loadUrl(homePageUrl);
    }

    @OnClick(R.id.btn_refresh)
    public void onClickButtonRefresh() {
        mWebView.reload();
    }

    private void setButtonsEnable() {
        if (mWebView.canGoBack()) {
            mButtonPageBack.setEnabled(true);
            mButtonPageBack.setAlpha(1);
        } else {
            mButtonPageBack.setEnabled(false);
            mButtonPageBack.setAlpha(0.5f);
        }

        if (mWebView.canGoForward()) {
            mButtonPageForward.setEnabled(true);
            mButtonPageForward.setAlpha(1);
        } else {
            mButtonPageForward.setEnabled(false);
            mButtonPageForward.setAlpha(0.5f);
        }
    }

    private void saveThisPage() {
        Realm realm = Realm.getDefaultInstance();
        if (mSectionId != DEFAULT_SECTION_ID) {
            int newSectionDetailId = getNewIdByClass(SectionDetail.class);
            Section section = realm.where(Section.class).equalTo(FIELD_NAME_ID, mSectionId).findFirst();
            realm.beginTransaction();
            SectionDetail sectionDetail = realm.createObject(SectionDetail.class, newSectionDetailId);
            sectionDetail.setUrl(mWebView.getUrl());
            sectionDetail.setVisited(false);
            section.getSectionDetails().add(sectionDetail);
            realm.commitTransaction();
        } else {
            Folder parentFolder = realm.where(Folder.class).equalTo(FIELD_NAME_ID, mFolderId).findFirst();
            int newItemId = getNewIdByClass(Item.class);
            realm.beginTransaction();
            Item item = realm.createObject(Item.class, newItemId);
            item.setUrl(mWebView.getUrl());
            item.setVisited(false);
            parentFolder.getItems().add(item);
            realm.commitTransaction();
        }
        realm.close();
        Toast.makeText(this, R.string.msg_save, Toast.LENGTH_LONG).show();
    }
}
