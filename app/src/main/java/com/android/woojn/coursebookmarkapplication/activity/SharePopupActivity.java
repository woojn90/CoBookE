package com.android.woojn.coursebookmarkapplication.activity;

import static com.android.woojn.coursebookmarkapplication.Constants.DEFAULT_FOLDER_ID;
import static com.android.woojn.coursebookmarkapplication.Constants.FIELD_NAME_ID;
import static com.android.woojn.coursebookmarkapplication.util.DisplayUtility.getUnderlineText;
import static com.android.woojn.coursebookmarkapplication.util.RealmDbUtility.getNewIdByClass;
import static com.android.woojn.coursebookmarkapplication.util.RealmDbUtility.insertDefaultFolderIfNeeded;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.woojn.coursebookmarkapplication.R;
import com.android.woojn.coursebookmarkapplication.model.Folder;
import com.android.woojn.coursebookmarkapplication.model.Item;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmModel;
import io.realm.RealmResults;

/**
 * Created by wjn on 2017-02-22.
 */

public class SharePopupActivity extends AppCompatActivity {

    @BindView(R.id.tv_share_url)
    protected TextView mTextViewShareUrl;
    @BindView(R.id.layout_share)
    protected LinearLayout mLinearLayoutShare;
    @BindView(R.id.pg_share)
    protected ProgressBar mProgressBarShare;
    @BindView(R.id.tv_share_title)
    protected TextView mTextViewShareTitle;
    @BindView(R.id.tv_share_desc)
    protected TextView mTextViewShareDesc;
    @BindView(R.id.iv_share_preview)
    protected ImageView mImageViewSharePreview;
    @BindView(R.id.pg_share_preview)
    protected ProgressBar mProgressBarSharePreview;
    @BindView(R.id.sp_share_dropdown)
    protected Spinner mSpinnerShareDropdown;

    private Realm mRealm;

    private int mItemId = getNewIdByClass(Item.class);
    private int mFolderId = DEFAULT_FOLDER_ID;
    private Item mItem;
    private boolean mIsSaved;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        layoutParams.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        layoutParams.dimAmount = 0.5f;
        getWindow().setAttributes(layoutParams);
        setContentView(R.layout.activity_share_popup);
        ButterKnife.bind(this);
        insertDefaultFolderIfNeeded(this);

        mRealm = Realm.getDefaultInstance();
        setSpinnerData();

        Intent shareIntent = getIntent();
        if (shareIntent != null && Intent.ACTION_SEND.equals(shareIntent.getAction())
                && "text/plain".equals(shareIntent.getType())) {
            String sharedText = shareIntent.getStringExtra(Intent.EXTRA_TEXT);
            String stringUrl = sharedText.substring(sharedText.indexOf("http"));
            if (stringUrl == null || stringUrl.length() == 0) {
                Toast.makeText(this, R.string.msg_invalid_url_not_save, Toast.LENGTH_LONG).show();
                finish();
            }
            mTextViewShareUrl.setText(getUnderlineText(stringUrl));

            mRealm.beginTransaction();
            mItem = mRealm.createObject(Item.class, mItemId);
            mItem.setUrl(stringUrl);
            mRealm.commitTransaction();
            ParseShareAsyncTask parseShareAsyncTask = new ParseShareAsyncTask();
            parseShareAsyncTask.execute(null, null, null);
        }

        mItem.addChangeListener(new RealmChangeListener<RealmModel>() {
            @Override
            public void onChange(RealmModel element) {
                if (!mIsSaved && mItem.isVisited()) {
                    mLinearLayoutShare.setVisibility(View.VISIBLE);
                    mProgressBarShare.setVisibility(View.INVISIBLE);
                    mTextViewShareTitle.setText(mItem.getTitle());
                    mTextViewShareDesc.setText(mItem.getDesc());
                    Glide.with(SharePopupActivity.this)
                            .load(mItem.getImageUrl())
                            .listener(new RequestListener<String, GlideDrawable>() {
                                @Override
                                public boolean onException(Exception e, String model,
                                        Target<GlideDrawable> target, boolean isFirstResource) {
                                    mProgressBarSharePreview.setVisibility(View.INVISIBLE);
                                    return false;
                                }

                                @Override
                                public boolean onResourceReady(GlideDrawable resource, String model,
                                        Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                                    mProgressBarSharePreview.setVisibility(View.INVISIBLE);
                                    return false;
                                }
                            })
                            .thumbnail(0.1f)
                            .centerCrop()
                            .into(mImageViewSharePreview);
                    mItem.removeChangeListener(this);
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        if (mItem != null && !mIsSaved) {
            mRealm.beginTransaction();
            mItem.deleteFromRealm();
            mRealm.commitTransaction();
        }
        mRealm.close();
        super.onDestroy();
    }

    @OnClick(R.id.btn_share_save)
    public void onClickButtonShareSave(View view) {
        Folder folder = mRealm.where(Folder.class).equalTo(FIELD_NAME_ID, mFolderId).findFirst();
        mRealm.beginTransaction();
        folder.getItems().add(mItem);
        mRealm.commitTransaction();

        Toast.makeText(this, R.string.msg_save, Toast.LENGTH_LONG).show();
        // TODO: Save and Run 버튼 만들거나 Toast 등에서 처리
//        Snackbar.make(getWindow().getDecorView().getRootView(), R.string.msg_save, Snackbar.LENGTH_LONG)
//                .setAction(R.string.msg_move_to_app, new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        Intent intent = new Intent(SharePopupActivity.this, MainActivity.class);
//                        startActivity(intent);
//                    }
//                }).show();
        mIsSaved = true;
        finish();
    }

    private void setSpinnerData() {

        ArrayList<Folder> foldersToDisplay = new ArrayList<>();
        RealmResults<Folder> folders = mRealm.where(Folder.class).findAll().sort(FIELD_NAME_ID);

        for (Folder folder : folders) {
            foldersToDisplay.add(folder);
        }

        ArrayAdapter<Folder> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, foldersToDisplay);
        mSpinnerShareDropdown.setAdapter(adapter);
        mSpinnerShareDropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Folder folder = (Folder) parent.getSelectedItem();
                mFolderId = folder.getId();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });
        mSpinnerShareDropdown.setSelection(0);
    }

    private class ParseShareAsyncTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void ... params) {
            try {
                Realm realm = Realm.getDefaultInstance();
                Item item = realm.where(Item.class).equalTo(FIELD_NAME_ID, mItemId).findFirst();

                Document doc = Jsoup.connect(item.getUrl()).get();

                Elements ogTags = doc.select("meta[property^=og:]");
                if (ogTags.size() <= 0) {
                    // TODO: og: 태그 없으면 title 등 다른 tag로 찾기
                    realm.beginTransaction();
                    item.setVisited(true);
                    realm.commitTransaction();
                    return null;
                }

                realm.beginTransaction();
                for (Element tag : ogTags) {
                    String property = tag.attr("property");
                    String content = tag.attr("content");

                    if ("og:title".equals(property)) {
                        item.setTitle(content);
                    } else if ("og:description".equals(property)) {
                        item.setDesc(content);
                    } else if ("og:image".equals(property)) {
                        item.setImageUrl(content);
                    }
                }
                // TODO: 저장된 값이 없어도 방문한 것으로 처리할 지 확인
                item.setVisited(true);
                realm.commitTransaction();
                realm.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

}
