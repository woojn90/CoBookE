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
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
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
    @BindView(R.id.btn_web_back)
    protected Button mButtonWebBack;
    @BindView(R.id.btn_web_forward)
    protected Button mButtonWebForward;
    @BindView(R.id.et_web_address)
    protected EditText mEditTextWebAddress;

    private SharedPreferences mSharedPreferences;
    private Toast mToast;

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

        mEditTextWebAddress.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
        mEditTextWebAddress.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    String stringUrl = mEditTextWebAddress.getText().toString();
                    mWebView.loadUrl(stringUrl);
                    setButtonsEnable();
                    mEditTextWebAddress.clearFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(mEditTextWebAddress.getWindowToken(), 0);
                    return true;
                }
                return false;
            }
        });

        mSectionId = getIntent().getIntExtra(KEY_SECTION_ID, DEFAULT_SECTION_ID);
        mFolderId = getIntent().getIntExtra(KEY_FOLDER_ID, DEFAULT_FOLDER_ID);
        String stringUrl = getIntent().getStringExtra(KEY_STRING_URL);

        if (stringUrl == null || stringUrl.length() == 0) {
            showToastByForce(R.string.msg_invalid_url_home);
            stringUrl = mSharedPreferences.getString(getString(R.string.pref_key_home_page)
                    , getString(R.string.pref_value_home_page_naver));
        }

        mWebView.getSettings().setJavaScriptEnabled(true);
        Log.d("Check", "loadUrl before");
        mWebView.loadUrl(stringUrl);
        setButtonsEnable();
        Log.d("Check", "loadUrl after");

        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                mEditTextWebAddress.setText(url);
                super.onPageStarted(view, url, favicon);
            }

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
                mEditTextWebAddress.setText(mWebView.getUrl());
                return true;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                mEditTextWebAddress.setText(url);
                setButtonsEnable();
                super.onPageFinished(view, url);
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

    @OnClick(R.id.btn_web_back)
    public void onClickButtonWebBack() {
        if (mWebView.canGoBack()) {
            mWebView.goBack();
        }
    }

    @OnClick(R.id.btn_web_forward)
    public void onClickButtonWebFront() {
        if (mWebView.canGoForward()) {
            mWebView.goForward();
        }
    }

    @OnClick(R.id.btn_web_home)
    public void onClickButtonWebHome() {
        String homePageUrl = mSharedPreferences.getString(getString(R.string.pref_key_home_page)
                , getString(R.string.pref_value_home_page_naver));
        mWebView.loadUrl(homePageUrl);
    }

    @OnClick(R.id.btn_web_refresh)
    public void onClickButtonWebRefresh() {
        mWebView.reload();
    }

    private void showToastByForce(int resId) {
        if (mToast != null) {
            mToast.cancel();
        }
        mToast = Toast.makeText(this, resId, Toast.LENGTH_LONG);
        mToast.show();
    }

    private void setButtonsEnable() {
        if (mWebView.canGoBack()) {
            mButtonWebBack.setEnabled(true);
            mButtonWebBack.setAlpha(1);
        } else {
            mButtonWebBack.setEnabled(false);
            mButtonWebBack.setAlpha(0.3f);
        }

        if (mWebView.canGoForward()) {
            mButtonWebForward.setEnabled(true);
            mButtonWebForward.setAlpha(1);
        } else {
            mButtonWebForward.setEnabled(false);
            mButtonWebForward.setAlpha(0.3f);
        }
    }

    private void saveThisPage() {
        Realm realm = Realm.getDefaultInstance();
        int newItemId = getNewIdByClass(Item.class);

        realm.beginTransaction();
        Item item = realm.createObject(Item.class, newItemId);
        item.setUrl(mWebView.getUrl());
        mWebView.getTitle();
        item.setTitle(getString(R.string.string_default_title));
        item.setDesc(getString(R.string.string_default_desc));
        item.setVisited(false);
        realm.commitTransaction();

        if (mSectionId != DEFAULT_SECTION_ID) {
            Section section = realm.where(Section.class).equalTo(FIELD_NAME_ID, mSectionId).findFirst();
            realm.beginTransaction();
            section.getItems().add(item);
            realm.commitTransaction();
        } else {
            Folder parentFolder = realm.where(Folder.class).equalTo(FIELD_NAME_ID, mFolderId).findFirst();
            realm.beginTransaction();
            parentFolder.getItems().add(item);
            realm.commitTransaction();
        }
        realm.close();
        showToastByForce(R.string.msg_save);
    }
}
