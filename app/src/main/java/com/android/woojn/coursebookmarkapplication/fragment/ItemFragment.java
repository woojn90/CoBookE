package com.android.woojn.coursebookmarkapplication.fragment;

import static com.android.woojn.coursebookmarkapplication.Constants.DEFAULT_FOLDER_ID;
import static com.android.woojn.coursebookmarkapplication.Constants.DEFAULT_SECTION_ID;
import static com.android.woojn.coursebookmarkapplication.Constants.FIELD_NAME_ID;
import static com.android.woojn.coursebookmarkapplication.Constants.KEY_FOLDER_ID;
import static com.android.woojn.coursebookmarkapplication.Constants.KEY_REQUEST_WEB_ACTIVITY;
import static com.android.woojn.coursebookmarkapplication.Constants.KEY_SECTION_ID;
import static com.android.woojn.coursebookmarkapplication.Constants.KEY_STRING_URL;
import static com.android.woojn.coursebookmarkapplication.Constants.REQUEST_WEB_ACTIVITY_WITH_SAVE;
import static com.android.woojn.coursebookmarkapplication.util.DisplayUtility.showPopupMenuIcon;
import static com.android.woojn.coursebookmarkapplication.util.RealmDbUtility.getNewIdByClass;
import static com.android.woojn.coursebookmarkapplication.util.RealmDbUtility.updateTextViewEmptyVisibilityByFolderId;
import static com.android.woojn.coursebookmarkapplication.util.ShareUtility.shareTextByRealmObject;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.woojn.coursebookmarkapplication.R;
import com.android.woojn.coursebookmarkapplication.activity.MainActivity;
import com.android.woojn.coursebookmarkapplication.activity.WebActivity;
import com.android.woojn.coursebookmarkapplication.adapter.DropdownAdapter;
import com.android.woojn.coursebookmarkapplication.adapter.FolderAdapter;
import com.android.woojn.coursebookmarkapplication.adapter.ItemAdapter;
import com.android.woojn.coursebookmarkapplication.async.ParseAsyncTask;
import com.android.woojn.coursebookmarkapplication.model.Folder;
import com.android.woojn.coursebookmarkapplication.model.Item;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.RealmResults;

/**
 * Created by wjn on 2017-02-16.
 */

public class ItemFragment extends Fragment
        implements ItemAdapter.OnRecyclerViewClickListener, FolderAdapter.OnRecyclerViewClickListener
        , MainActivity.onKeyBackPressedListener {

    @BindView(R.id.layout_explorer_bar)
    protected LinearLayout mLinearLayoutExplorerBar;
    @BindView(R.id.iv_up_arrow_enable)
    protected ImageView mImageViewUpArrowEnable;
    @BindView(R.id.iv_up_arrow_disable)
    protected ImageView mImageViewUpArrowDisable;
    @BindView(R.id.tv_item_empty)
    protected TextView mTextViewItemEmpty;
    @BindView(R.id.rv_folder_list)
    protected RecyclerView mRecyclerViewFolder;
    @BindView(R.id.rv_item_list)
    protected RecyclerView mRecyclerViewItem;

    private FloatingActionButton mFabExpandItems;
    private FloatingActionButton mFabToggleGrid;
    private FloatingActionButton mFabInsertFolder;
    private FloatingActionButton mFabBrowser;

    private Realm mRealm;
    private SharedPreferences mSharedPreferences;
    private RealmResults<Folder> mFolders;
    private RealmResults<Item> mItems;

    private int mFolderIdWantToChange;
    public static ArrayList<Integer> folderIds;
    public static boolean isFabOpen;
    public static int currentFolderId;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mRealm = Realm.getDefaultInstance();
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_item, container, false);
        ButterKnife.bind(this, rootView);

        setRecyclerViewLayoutManager();
        folderIds = new ArrayList<>();
        folderIds.add(DEFAULT_FOLDER_ID);
        currentFolderId = DEFAULT_FOLDER_ID;
        setRecyclerViewAdapter();

        mItems.addChangeListener(new RealmChangeListener<RealmResults<Item>>() {
            @Override
            public void onChange(RealmResults<Item> element) {
                updateTextViewEmptyVisibilityByFolderId(currentFolderId, mTextViewItemEmpty);
            }
        });
        mFolders.addChangeListener(new RealmChangeListener<RealmResults<Folder>>() {
            @Override
            public void onChange(RealmResults<Folder> element) {
                updateTextViewEmptyVisibilityByFolderId(currentFolderId, mTextViewItemEmpty);
            }
        });

        mFabExpandItems = (FloatingActionButton) getActivity().findViewById(R.id.fab_expand_items);
        mFabExpandItems.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                animateFabInItemTab();
            }
        });

        mFabToggleGrid = (FloatingActionButton) getActivity().findViewById(R.id.fab_toggle_grid);
        mFabToggleGrid.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                animateFabInItemTab();
                toggleGridColumn();
            }
        });

        mFabInsertFolder = (FloatingActionButton) getActivity().findViewById(R.id.fab_insert_folder);
        mFabInsertFolder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                animateFabInItemTab();
                insertFolder();
            }
        });

        mFabBrowser = (FloatingActionButton) getActivity().findViewById(R.id.fab_browser);
        mFabBrowser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDefaultWebPage();
            }
        });
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        setRecyclerViewAdapter();
        RealmResults<Item> items = mRealm.where(Item.class).findAll();
        for (Item item : items) {
            if (!item.isVisited()) {
                retrieveItemById(item.getId());
            }
        }
    }

    @Override
    public void onDestroy() {
        mRealm.close();
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if (currentFolderId != DEFAULT_FOLDER_ID) {
            onClickImageViewUpArrowEnable();
        } else {
            getActivity().onBackPressed();
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        ((MainActivity) getActivity()).setOnKeyBackPressedListener(this);
    }

    @OnClick(R.id.iv_up_arrow_enable)
    public void onClickImageViewUpArrowEnable() {
        if (isFabOpen) animateFabInItemTab();
        int childCount = mLinearLayoutExplorerBar.getChildCount();
        mLinearLayoutExplorerBar.removeViewAt(childCount - 1);
        mLinearLayoutExplorerBar.removeViewAt(childCount - 2);

        folderIds.remove(folderIds.indexOf(currentFolderId));
        currentFolderId = folderIds.get(folderIds.size() - 1);
        setRecyclerViewAdapter();
    }

    @OnClick(R.id.iv_home)
    public void onClickImageViewHome() {
        if (isFabOpen) animateFabInItemTab();
        if (folderIds.size() > 1) {
            int childCount = mLinearLayoutExplorerBar.getChildCount();
            for (int i = childCount - 1; i > 1; i--) {
                mLinearLayoutExplorerBar.removeViewAt(i);
            }

            folderIds.clear();
            folderIds.add(DEFAULT_FOLDER_ID);
            currentFolderId = DEFAULT_FOLDER_ID;
            setRecyclerViewAdapter();
        }
    }

    @Override
    public void onFolderClick(int id) {
        if (isFabOpen) animateFabInItemTab();
        currentFolderId = id;
        folderIds.add(currentFolderId);
        Folder folder = mRealm.where(Folder.class).equalTo(FIELD_NAME_ID, currentFolderId).findFirst();

        TextView textViewInside = new TextView(getContext());
        textViewInside.setText(">");
        textViewInside.setTextSize(17);
        textViewInside.setPadding(4, 4, 4, 4);
        textViewInside.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        mLinearLayoutExplorerBar.addView(textViewInside);

        TextView textViewFolder = new TextView(getContext());
        textViewFolder.setText(folder.getTitle());
        textViewFolder.setTextSize(17);
        textViewFolder.setPadding(4, 4, 4, 4);
        textViewFolder.setTag(id);
        textViewFolder.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        textViewFolder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                moveToSelectedFolder((int) view.getTag());
            }
        });
        mLinearLayoutExplorerBar.addView(textViewFolder);

        setRecyclerViewAdapter();
    }

    @Override
    public void onFolderDoubleTap(int id) {
        Folder folder = mRealm.where(Folder.class).equalTo(FIELD_NAME_ID, id).findFirst();
        showFolderDialog(id, folder.getTitle());
    }

    @Override
    public void onItemInFolderClick(final int id, View view) {
        if (isFabOpen) animateFabInItemTab();
        switch (view.getId()) {
            case R.id.btn_folder_overflow:
                PopupMenu popupMenu = new PopupMenu(getContext(), view);
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        Folder folder = mRealm.where(Folder.class).equalTo(FIELD_NAME_ID, id).findFirst();
                        switch (menuItem.getItemId()) {
                            case R.id.item_delete_folder:
                                deleteFolder(folder);
                                return true;
                            case R.id.item_change_folder:
                                showChangeFolderDialog(folder);
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
        webIntent.putExtra(KEY_FOLDER_ID, currentFolderId);
        webIntent.putExtra(KEY_SECTION_ID, DEFAULT_SECTION_ID);
        startActivity(webIntent);
    }

    @Override
    public void onItemInItemClick(final int id, View view) {
        if (isFabOpen) animateFabInItemTab();
        switch (view.getId()) {
            case R.id.btn_item_overflow:
                PopupMenu popupMenu = new PopupMenu(getContext(), view);
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        Item item = mRealm.where(Item.class).equalTo(FIELD_NAME_ID, id).findFirst();
                        switch (menuItem.getItemId()) {
                            case R.id.item_delete_item:
                                deleteItem(item);
                                return true;
                            case R.id.item_share_item:
                                shareTextByRealmObject(getContext(), item);
                                return true;
                            case R.id.item_change_item:
                                showChangeFolderDialog(item);
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

    private void setRecyclerViewLayoutManager() {
        int numberOfColumn = mSharedPreferences.getInt(getString(R.string.pref_key_item_number_of_grid_column), 2);
        mRecyclerViewFolder.setLayoutManager(new GridLayoutManager(getActivity(), numberOfColumn));
        mRecyclerViewItem.setLayoutManager(new GridLayoutManager(getActivity(), numberOfColumn));
    }

    private void setRecyclerViewAdapter() {
        toggleUpArrowImageView();

        Folder currentFolder = mRealm.where(Folder.class).equalTo(FIELD_NAME_ID, currentFolderId).findFirst();
        mFolders = currentFolder.getFolders().sort(FIELD_NAME_ID);
        mItems = currentFolder.getItems().sort(FIELD_NAME_ID);
        mRecyclerViewFolder.setAdapter(new FolderAdapter(getContext(), mFolders, this));
        mRecyclerViewItem.setAdapter(new ItemAdapter(getContext(), mItems, this));

        updateTextViewEmptyVisibilityByFolderId(currentFolderId, mTextViewItemEmpty);
    }

    private void animateFabInItemTab() {
        if (isFabOpen) {
            mFabExpandItems.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.rotate_backward));
            mFabToggleGrid.hide();
            mFabInsertFolder.hide();
            mFabBrowser.hide();
            isFabOpen = false;
        } else {
            mFabExpandItems.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.rotate_forward));
            mFabToggleGrid.show();
            mFabInsertFolder.show();
            mFabBrowser.show();
            isFabOpen = true;
        }
    }

    private void toggleGridColumn() {
        int beforeNumberOfColumn = mSharedPreferences.getInt(getString(R.string.pref_key_item_number_of_grid_column), 2);
        SharedPreferences.Editor editor = mSharedPreferences.edit();

        if (beforeNumberOfColumn == 2) {
            editor.putInt(getString(R.string.pref_key_item_number_of_grid_column), 3);
            editor.apply();
            mFabToggleGrid.setImageResource(R.drawable.ic_fab_grid_2x2);
        } else {
            editor.putInt(getString(R.string.pref_key_item_number_of_grid_column), 2);
            editor.apply();
            mFabToggleGrid.setImageResource(R.drawable.ic_fab_grid_3x3);
        }
        setRecyclerViewLayoutManager();
        mRecyclerViewFolder.setAdapter(new FolderAdapter(getContext(), mFolders, ItemFragment.this));
        mRecyclerViewItem.setAdapter(new ItemAdapter(getContext(), mItems, ItemFragment.this));
    }

    private void toggleUpArrowImageView() {
        if (folderIds.size() > 1) {
            mImageViewUpArrowDisable.setVisibility(View.GONE);
            mImageViewUpArrowEnable.setVisibility(View.VISIBLE);
        } else {
            mImageViewUpArrowEnable.setVisibility(View.GONE);
            mImageViewUpArrowDisable.setVisibility(View.VISIBLE);
        }
    }

    private void moveToSelectedFolder(int folderId) {
        if (isFabOpen) animateFabInItemTab();

        if (folderId == currentFolderId) {
            return;
        }

        int index = folderIds.indexOf(folderId);
        int childCount = mLinearLayoutExplorerBar.getChildCount();
        for (int i = childCount - 1; i > index * 2 + 1; i--) {
            mLinearLayoutExplorerBar.removeViewAt(i);
        }

        for (int i = folderIds.size() - 1; i > index; i--) {
            folderIds.remove(i);
        }
        currentFolderId = folderId;
        setRecyclerViewAdapter();
    }

    private void deleteItem(Item item) {
        mRealm.beginTransaction();
        item.deleteFromRealm();
        mRealm.commitTransaction();
    }

    private void insertFolder() {
        Folder parentFolder = mRealm.where(Folder.class).equalTo(FIELD_NAME_ID, currentFolderId).findFirst();
        int newFolderId = getNewIdByClass(Folder.class);
        mRealm.beginTransaction();
        Folder folder = mRealm.createObject(Folder.class, newFolderId);
        folder.setTitle("New Folder");
        parentFolder.getFolders().add(folder);
        mRealm.commitTransaction();
    }

    private void deleteFolder(Folder folder) {
        mRealm.beginTransaction();
        deleteItemsAndFoldersInsideFolder(folder);
        mRealm.commitTransaction();
    }

    /**
     * realm.beginTransaction(); 이후에 호출됨 / 재귀 함수
     */
    private void deleteItemsAndFoldersInsideFolder(Folder parentFolder) {
        deleteItemsInsideFolder(parentFolder);
        RealmList<Folder> folders = parentFolder.getFolders();
        for (int i = folders.size() - 1; i >= 0; i--) {
            deleteItemsInsideFolder(folders.get(i));
        }
        parentFolder.deleteFromRealm();
    }

    /**
     * realm.beginTransaction(); 이후에 호출됨
     */
    private void deleteItemsInsideFolder(Folder parentFolder) {
        RealmList<Item> items = parentFolder.getItems();
        for (int i = items.size() - 1; i >= 0; i--) {
            items.get(i).deleteFromRealm();
        }
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
        editTextTitle.setImeOptions(EditorInfo.IME_ACTION_DONE);
        editTextTitle.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE){
                    if (alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled()) {
                        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).performClick();
                        return true;
                    }
                }
                return false;
            }
        });

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

    private void showChangeFolderDialog(final RealmObject realmObject) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        final View dialogView = getActivity().getLayoutInflater().inflate(R.layout.dialog_folder_change, null);
        builder.setView(dialogView);
        final Spinner spinnerFolder = (Spinner) dialogView.findViewById(R.id.sp_folder_change_dropdown);
        setSpinnerData(spinnerFolder);

        builder.setTitle(R.string.string_folder_change);
        builder.setNegativeButton(R.string.string_cancel, null);
        builder.setPositiveButton(R.string.string_change, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Folder currentFolder = mRealm.where(Folder.class).equalTo(FIELD_NAME_ID, currentFolderId).findFirst();
                Folder changeFolder = (Folder) spinnerFolder.getSelectedItem();

                if (realmObject instanceof Item) {
                    Item item = (Item) realmObject;
                    mRealm.beginTransaction();
                    currentFolder.getItems().remove(item);
                    changeFolder.getItems().add(item);
                    mRealm.commitTransaction();
                } else {
                    Folder folder = (Folder) realmObject;
                    mRealm.beginTransaction();
                    currentFolder.getItems().remove(folder);
                    changeFolder.getFolders().add(folder);
                    mRealm.commitTransaction();
                }
                Toast.makeText(getContext(), changeFolder.getTitle() +
                        getString(R.string.msg_postfix_folder_move), Toast.LENGTH_SHORT).show();
            }
        });

        final AlertDialog alertDialog = builder.create();
        alertDialog.show();
        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
        spinnerFolder.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Folder folder = (Folder) parent.getSelectedItem();
                if (folder.getId() != currentFolderId) {
                    alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                } else {
                    alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });
    }

    private void setSpinnerData(Spinner spinner) {
        ArrayList<Folder> foldersToDisplay = new ArrayList<>();
        RealmResults<Folder> folders = mRealm.where(Folder.class).findAll().sort(FIELD_NAME_ID);

        for (Folder folder : folders) {
            foldersToDisplay.add(folder);
        }

        DropdownAdapter adapter = new DropdownAdapter(getContext(), R.layout.dropdown_folder, R.id.tv_dropdown_folder,
                foldersToDisplay, getActivity().getLayoutInflater());
        spinner.setAdapter(adapter);
        spinner.setSelection(0);
    }

    private void retrieveItemById(int itemId) {
        new ParseAsyncTask().execute(itemId, null, null);
    }

    private void showDefaultWebPage() {
        Intent webIntent = new Intent(getContext(), WebActivity.class);
        webIntent.putExtra(KEY_REQUEST_WEB_ACTIVITY, REQUEST_WEB_ACTIVITY_WITH_SAVE);
        String stringUrl = mSharedPreferences.getString(getString(R.string.pref_key_home_page)
                , getString(R.string.pref_value_home_page_naver));
        webIntent.putExtra(KEY_STRING_URL, stringUrl);
        webIntent.putExtra(KEY_SECTION_ID, DEFAULT_SECTION_ID);
        webIntent.putExtra(KEY_FOLDER_ID, currentFolderId);
        startActivity(webIntent);
    }
}
