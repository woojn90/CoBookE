package com.android.woojn.coursebookmarkapplication.activity;

import static com.android.woojn.coursebookmarkapplication.Constants.DEFAULT_FOLDER_ID;
import static com.android.woojn.coursebookmarkapplication.Constants.FIELD_NAME_ID;
import static com.android.woojn.coursebookmarkapplication.Constants.KEY_FOLDER_ID;
import static com.android.woojn.coursebookmarkapplication.util.RealmDbUtility.getNewIdByClass;
import static com.android.woojn.coursebookmarkapplication.util.RealmDbUtility.insertDefaultFolderIfNeeded;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.woojn.coursebookmarkapplication.R;
import com.android.woojn.coursebookmarkapplication.adapter.DropdownAdapter;
import com.android.woojn.coursebookmarkapplication.async.ParseAsyncTask;
import com.android.woojn.coursebookmarkapplication.model.Folder;
import com.android.woojn.coursebookmarkapplication.model.Item;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;

/**
 * Created by wjn on 2017-02-22.
 */

public class SharePopupActivity extends AppCompatActivity implements RealmChangeListener<Item> {

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
    @BindView(R.id.tv_share_preview)
    protected TextView mTextViewSharePreview;
    @BindView(R.id.iv_share_preview)
    protected ImageView mImageViewSharePreview;
    @BindView(R.id.pg_share_preview)
    protected ProgressBar mProgressBarSharePreview;
    @BindView(R.id.sp_share_dropdown)
    protected Spinner mSpinnerShareDropdown;

    private Realm mRealm;
    private Toast mToast;

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
            if (sharedText != null) {
                String stringUrl = sharedText.substring(sharedText.indexOf("http"));
                if (stringUrl.length() > 0) {
                    mTextViewShareUrl.setText(stringUrl);

                    mRealm.beginTransaction();
                    mItem = mRealm.createObject(Item.class, mItemId);
                    mItem.setUrl(stringUrl);
                    mRealm.commitTransaction();

                    mItem.addChangeListener(this);

                    new ParseAsyncTask().execute(mItemId, null, null);
                    return;
                }
            }
            showToastByForce(R.string.msg_invalid_url_not_save);
            finishAndRemoveTask();
        }
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

    @Override
    public void onChange(Item element) {
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
                            mTextViewSharePreview.setVisibility(View.VISIBLE);
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(GlideDrawable resource, String model,
                                Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                            mProgressBarSharePreview.setVisibility(View.INVISIBLE);
                            mTextViewSharePreview.setVisibility(View.VISIBLE);
                            return false;
                        }
                    })
                    .thumbnail(0.1f)
                    .centerCrop()
                    .into(mImageViewSharePreview);
            mItem.removeChangeListener(this);
        }
    }

    @OnClick(R.id.btn_share_save)
    public void onClickButtonShareSave() {
        saveItemAndFinish();
    }

    @OnClick(R.id.btn_share_save_and_run)
    public void onClickButtonShareSaveAndRun() {
        saveItemAndFinish();

        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra(KEY_FOLDER_ID, mFolderId);
        startActivity(intent);
    }

    private void showToastByForce(int resId) {
        if (mToast != null) {
            mToast.cancel();
        }
        mToast = Toast.makeText(this, resId, Toast.LENGTH_LONG);
        mToast.show();
    }

    private void setSpinnerData() {
        ArrayList<Folder> foldersToDisplay = new ArrayList<>();
        RealmResults<Folder> folders = mRealm.where(Folder.class).findAll().sort(FIELD_NAME_ID);

        for (Folder folder : folders) {
            foldersToDisplay.add(folder);
        }

        DropdownAdapter adapter = new DropdownAdapter(this, R.layout.dropdown_folder, R.id.tv_dropdown_folder,
                foldersToDisplay, getLayoutInflater());
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

    private void saveItemAndFinish() {
        Folder folder = mRealm.where(Folder.class).equalTo(FIELD_NAME_ID, mFolderId).findFirst();
        mRealm.beginTransaction();
        folder.getItems().add(mItem);
        mRealm.commitTransaction();

        showToastByForce(R.string.msg_save);
        mIsSaved = true;
        finishAndRemoveTask();
    }
}
