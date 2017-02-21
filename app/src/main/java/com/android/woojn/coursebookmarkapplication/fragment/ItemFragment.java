package com.android.woojn.coursebookmarkapplication.fragment;

import static com.android.woojn.coursebookmarkapplication.Constants.DEFAULT_SECTION_ID;
import static com.android.woojn.coursebookmarkapplication.Constants.FIELD_NAME_ID;
import static com.android.woojn.coursebookmarkapplication.Constants.KEY_REQUEST_WEB_ACTIVITY;
import static com.android.woojn.coursebookmarkapplication.Constants.KEY_SECTION_ID;
import static com.android.woojn.coursebookmarkapplication.Constants.KEY_STRING_URL;
import static com.android.woojn.coursebookmarkapplication.Constants.REQUEST_WEB_ACTIVITY_WITH_SAVE;
import static com.android.woojn.coursebookmarkapplication.util.DisplayUtility.showPopupMenuIcon;
import static com.android.woojn.coursebookmarkapplication.util.RealmDbUtility.setTextViewEmptyVisibility;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

import com.android.woojn.coursebookmarkapplication.R;
import com.android.woojn.coursebookmarkapplication.activity.WebActivity;
import com.android.woojn.coursebookmarkapplication.adapter.FolderAdapter;
import com.android.woojn.coursebookmarkapplication.adapter.ItemAdapter;
import com.android.woojn.coursebookmarkapplication.model.Folder;
import com.android.woojn.coursebookmarkapplication.model.Item;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Created by wjn on 2017-02-16.
 */

public class ItemFragment extends Fragment
        implements ItemAdapter.OnRecyclerViewClickListener, FolderAdapter.OnRecyclerViewClickListener,
        SharedPreferences.OnSharedPreferenceChangeListener{

    @BindView(R.id.tv_item_empty)
    protected TextView mTextViewItemEmpty;
    @BindView(R.id.rv_folder_list)
    protected RecyclerView mRecyclerViewFolder;
    @BindView(R.id.rv_item_list)
    protected RecyclerView mRecyclerViewItem;

    private Realm mRealm;
    private SharedPreferences mSharedPreferences;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mRealm = Realm.getDefaultInstance();
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        mSharedPreferences.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_item, container, false);
        ButterKnife.bind(this, rootView);

        int numberOfColumn = mSharedPreferences.getInt(
                getString(R.string.pref_key_item_number_of_grid_column), 2);

        mRecyclerViewFolder.setLayoutManager(new GridLayoutManager(getActivity(), numberOfColumn));
        mRecyclerViewFolder.setAdapter(new FolderAdapter(getContext(), mRealm.where(Folder.class).findAllAsync(), this));
        mRecyclerViewItem.setLayoutManager(new GridLayoutManager(getActivity(), numberOfColumn));
        mRecyclerViewItem.setAdapter(new ItemAdapter(getContext(), mRealm.where(Item.class).findAllAsync(), this));
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        setTextViewEmptyVisibility(Item.class, 0, mTextViewItemEmpty);
        RealmResults<Item> items = mRealm.where(Item.class).findAll();
        for (Item item : items) {
            if (!item.isVisited()) {
                retrieveItemById(item.getId());
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mRealm.close();
        mSharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        int numberOfColumn = mSharedPreferences.getInt(getString(R.string.pref_key_item_number_of_grid_column), 0);
        mRecyclerViewFolder.setLayoutManager(new GridLayoutManager(getActivity(), numberOfColumn));
        mRecyclerViewFolder.setAdapter(new FolderAdapter(getContext(), mRealm.where(Folder.class).findAllAsync(), this));
        mRecyclerViewItem.setLayoutManager(new GridLayoutManager(getActivity(), numberOfColumn));
        mRecyclerViewItem.setAdapter(new ItemAdapter(getContext(), mRealm.where(Item.class).findAllAsync(), this));

    }

    @Override
    public void onFolderClick(int id) {
        Folder folder = mRealm.where(Folder.class).equalTo(FIELD_NAME_ID, id).findFirst();
        // TODO: folder 진입
    }

    @Override
    public void onItemInFolderClick(final int id, View view) {
        switch (view.getId()) {
            case R.id.btn_folder_overflow:
                PopupMenu popupMenu = new PopupMenu(getContext(), view);
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        Folder folder = mRealm.where(Folder.class).equalTo(FIELD_NAME_ID, id).findFirst();
                        switch (menuItem.getItemId()) {
                            case R.id.item_update_folder:
                                showFolderDialog(id, folder.getTitle());
                                return true;
                            case R.id.item_delete_folder:
                                deleteFolder(folder);
                                return true;
                        }
                        return false;
                    }
                });
                popupMenu.inflate(R.menu.menu_in_folder_view);
                popupMenu.show();
                showPopupMenuIcon(popupMenu);
                break;
        }
    }

    @Override
    public void onItemClick(int id) {
        Item item = mRealm.where(Item.class).equalTo(FIELD_NAME_ID, id).findFirst();
        String stringUrl = item.getUrl();

        Intent webIntent = new Intent(getContext(), WebActivity.class);
        webIntent.putExtra(KEY_REQUEST_WEB_ACTIVITY, REQUEST_WEB_ACTIVITY_WITH_SAVE);
        webIntent.putExtra(KEY_STRING_URL, stringUrl);
        webIntent.putExtra(KEY_SECTION_ID, DEFAULT_SECTION_ID);
        startActivity(webIntent);
    }

    @Override
    public void onItemInItemClick(final int id, View view) {
        switch (view.getId()) {
            case R.id.btn_item_overflow:
                PopupMenu popupMenu = new PopupMenu(getContext(), view);
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        Item item = mRealm.where(Item.class).equalTo(FIELD_NAME_ID, id).findFirst();
                        switch (menuItem.getItemId()) {
                            case R.id.item_share_item:
                                // TODO: share
                                return true;
                            case R.id.item_delete_item:
                                deleteItem(item);
                                return true;
                        }
                        return false;
                    }
                });
                popupMenu.inflate(R.menu.menu_in_item_view);
                popupMenu.show();
                showPopupMenuIcon(popupMenu);
                break;
        }
    }

    private void deleteItem(Item item) {
        mRealm.beginTransaction();
        item.deleteFromRealm();
        mRealm.commitTransaction();
        setTextViewEmptyVisibility(Item.class, 0, mTextViewItemEmpty);
    }

    private void deleteFolder(Folder folder) {
        mRealm.beginTransaction();
        folder.deleteFromRealm();
        mRealm.commitTransaction();
        setTextViewEmptyVisibility(Item.class, 0, mTextViewItemEmpty);
    }

    private void showFolderDialog(final int folderId, final String beforeTitle) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        final View dialogView = getActivity().getLayoutInflater().inflate(R.layout.dialog_folder, null);
        builder.setView(dialogView);
        final EditText editTextTitle = (EditText) dialogView.findViewById(R.id.et_folder_title);

        editTextTitle.setText(beforeTitle);

        builder.setTitle(R.string.string_folder_info);
        builder.setNegativeButton(R.string.string_cancel, null);
        builder.setPositiveButton(R.string.string_update, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String title = editTextTitle.getText().toString();

                Folder folder = mRealm.where(Folder.class).equalTo(FIELD_NAME_ID, folderId).findFirst();
                mRealm.beginTransaction();
                folder.setTitle(title);
                mRealm.commitTransaction();
            }
        });

        final AlertDialog alertDialog = builder.create();
        alertDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        alertDialog.show();
        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
        editTextTitle.requestFocus();

        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Do nothing
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String afterTitle = editTextTitle.getText().toString();

                if (!afterTitle.trim().isEmpty() && !beforeTitle.equals(afterTitle)) {
                    alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                } else {
                    alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                }
            }
            @Override
            public void afterTextChanged(Editable s) {
                // Do nothing
            }
        };
        editTextTitle.addTextChangedListener(textWatcher);
    }

    private void retrieveItemById(int itemId) {
        ParseAsyncTask parseAsyncTask = new ParseAsyncTask();
        parseAsyncTask.execute(itemId, null, null);
    }

    private class ParseAsyncTask extends AsyncTask<Integer, Void, Void> {
        @Override
        protected Void doInBackground(Integer... params) {
            try {
                Realm realm = Realm.getDefaultInstance();
                Item item = realm.where(Item.class).equalTo(FIELD_NAME_ID, params[0]).findFirst();

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

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
        }
    }

}
