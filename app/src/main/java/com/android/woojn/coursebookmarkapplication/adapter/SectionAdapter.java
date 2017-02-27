package com.android.woojn.coursebookmarkapplication.adapter;

import static com.android.woojn.coursebookmarkapplication.Constants.FIELD_NAME_ID;
import static com.android.woojn.coursebookmarkapplication.Constants.KEY_SECTION_ID;
import static com.android.woojn.coursebookmarkapplication.Constants.KEY_STRING_URL;
import static com.android.woojn.coursebookmarkapplication.activity.CourseActivity.currentCourse;
import static com.android.woojn.coursebookmarkapplication.util.DisplayUtility.showItemDialog;
import static com.android.woojn.coursebookmarkapplication.util.DisplayUtility.showPopupMenuIcon;
import static com.android.woojn.coursebookmarkapplication.util.RealmDbUtility.getNewIdByClass;
import static com.android.woojn.coursebookmarkapplication.util.RealmDbUtility.updateTextViewEmptyVisibility;
import static com.android.woojn.coursebookmarkapplication.util.SettingUtility.isDeleteWithConfirm;
import static com.android.woojn.coursebookmarkapplication.util.ShareUtility.shareTextByRealmObject;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.woojn.coursebookmarkapplication.R;
import com.android.woojn.coursebookmarkapplication.activity.WebActivity;
import com.android.woojn.coursebookmarkapplication.model.Item;
import com.android.woojn.coursebookmarkapplication.model.Section;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.realm.OrderedRealmCollection;
import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmRecyclerViewAdapter;
import io.realm.RealmResults;

/**
 * Created by wjn on 2017-02-07.
 */

public class SectionAdapter extends RealmRecyclerViewAdapter<Section, SectionAdapter.SectionViewHolder> {

    private final Context mContext;
    private final OnRecyclerViewClickListener mListener;
    private Toast mToast;

    public SectionAdapter(Context context, OrderedRealmCollection<Section> data, OnRecyclerViewClickListener listener) {
        super(context, data, true);
        mListener = listener;
        mContext = context;
    }

    public interface OnRecyclerViewClickListener {
        void onItemClick(int id, View view);
        void onItemDoubleTap(int id);
    }

    @Override
    public SectionViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.list_item_of_section, parent, false);
        return new SectionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final SectionViewHolder holder, int position) {
        if (getData() != null) {
            final Section section = getData().get(position);
            holder.itemView.setTag(section.getId());
            holder.textViewSectionTitle.setText(section.getTitle());
            holder.textViewSectionSearchWord.setText("(" + section.getSearchWord() + ")");

            RealmResults<Item> sectionItems = section.getItems().sort(FIELD_NAME_ID);
            holder.recyclerViewCourseSectionDetail.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
            holder.recyclerViewCourseSectionDetail.setAdapter(new SectionItemAdapter(mContext, sectionItems, holder));
            updateTextViewEmptyVisibility(Item.class, section.getId(), holder.textViewCourseSectionDetailEmpty);
        }
    }

    class SectionViewHolder extends RecyclerView.ViewHolder
        implements SectionItemAdapter.OnRecyclerViewClickListener {

        @BindView(R.id.tv_section_title)
        TextView textViewSectionTitle;
        @BindView(R.id.tv_section_search_word)
        TextView textViewSectionSearchWord;
        @BindView(R.id.tv_section_item_empty)
        TextView textViewCourseSectionDetailEmpty;
        @BindView(R.id.rv_section_item_list)
        RecyclerView recyclerViewCourseSectionDetail;

        public SectionViewHolder(final View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            final GestureDetector gd = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onDoubleTap(MotionEvent e) {
                    mListener.onItemDoubleTap((int) itemView.getTag());
                    return true;
                }
            });

            itemView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    gd.onTouchEvent(event);
                    return true;
                }
            });
        }

        @OnClick({R.id.btn_section_search_page, R.id.btn_section_share, R.id.btn_section_delete})
        public void onClick(View view) {
            mListener.onItemClick((int) itemView.getTag(), view);
        }

        @Override
        public void onItemClick(int id) {
            Realm realm = Realm.getDefaultInstance();
            Section section = realm.where(Section.class).equalTo(FIELD_NAME_ID, (int) itemView.getTag()).findFirst();
            Item item = realm.where(Item.class).equalTo(FIELD_NAME_ID, id).findFirst();
            int sectionId = section.getId();
            String stringUrl = item.getUrl();
            realm.close();

            Intent webIntent = new Intent(mContext, WebActivity.class);
            webIntent.putExtra(KEY_STRING_URL, stringUrl);
            webIntent.putExtra(KEY_SECTION_ID, sectionId);
            mContext.startActivity(webIntent);
            showToastByForce(mContext.getString(R.string.msg_move_to_web_page));
        }

        @Override
        public void onItemLongClick(int id) {
            Realm realm = Realm.getDefaultInstance();
            Item item = realm.where(Item.class).equalTo(FIELD_NAME_ID, id).findFirst();
            showSectionChangeDialog(item);
            realm.close();
        }

        @Override
        public void onItemDoubleTap(int id) {
            Realm realm = Realm.getDefaultInstance();
            Item item = realm.where(Item.class).equalTo(FIELD_NAME_ID, id).findFirst();
            showItemDialog(mContext, id, item.getTitle(), item.getDesc());
            realm.close();
        }

        @Override
        public void onItemInItemClick(final int id, View view) {
            switch (view.getId()) {
                case R.id.btn_section_item_overflow:
                    PopupMenu popupMenu = new PopupMenu(mContext, view);
                    popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem menuItem) {
                            Realm realm = Realm.getDefaultInstance();
                            Item item = realm.where(Item.class).equalTo(FIELD_NAME_ID, id).findFirst();
                            switch (menuItem.getItemId()) {
                                case R.id.item_share_item_or_folder:
                                    shareTextByRealmObject(mContext, item);
                                    realm.close();
                                    return true;
                                case R.id.item_delete:
                                    deleteSectionItem(item);
                                    realm.close();
                                    return true;
                            }
                            realm.close();
                            return false;
                        }
                    });
                    popupMenu.inflate(R.menu.menu_in_item_view);
                    popupMenu.show();
                    showPopupMenuIcon(popupMenu);
                    break;
            }
        }

        private void showSectionChangeDialog(final Item item) {
            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
            final View dialogView = ((LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.dialog_dropdown, null);
            builder.setView(dialogView);
            final Spinner spinnerSection = (Spinner) dialogView.findViewById(R.id.sp_dropdown);

            final int sectionId = (int) itemView.getTag();
            boolean hasData = setSpinnerData(spinnerSection, sectionId);

            builder.setTitle(R.string.string_section_select);
            builder.setNeutralButton(R.string.string_cancel, null);
            builder.setNegativeButton(R.string.string_copy, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Realm realm = Realm.getDefaultInstance();
                    Section selectSection = (Section) spinnerSection.getSelectedItem();
                    realm.beginTransaction();
                    Item copyItem = realm.createObject(Item.class, getNewIdByClass(Item.class));
                    copyItem.setTitle(item.getTitle());
                    copyItem.setDesc(item.getDesc());
                    copyItem.setImageUrl(item.getImageUrl());
                    copyItem.setUrl(item.getImageUrl());
                    copyItem.setVisited(item.isVisited());
                    selectSection.getItems().add(copyItem);
                    realm.commitTransaction();
                    realm.close();

                    showToastByForce(mContext.getString(R.string.msg_prefix_move_or_copy) + selectSection.getTitle() +
                            mContext.getString(R.string.msg_postfix_copy_section));
                }
            });
            builder.setPositiveButton(R.string.string_move, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Realm realm = Realm.getDefaultInstance();
                    Section currentSection = realm.where(Section.class).equalTo(FIELD_NAME_ID, sectionId).findFirst();
                    Section selectSection = (Section) spinnerSection.getSelectedItem();

                    realm.beginTransaction();
                    currentSection.getItems().remove(item);
                    selectSection.getItems().add(item);
                    realm.commitTransaction();
                    realm.close();
                    showToastByForce(mContext.getString(R.string.msg_prefix_move_or_copy) + selectSection.getTitle() +
                            mContext.getString(R.string.msg_postfix_move_section));
                }
            });

            final AlertDialog alertDialog = builder.create();
            alertDialog.show();

            if (!hasData) {
                alertDialog.getButton(alertDialog.BUTTON_NEGATIVE).setEnabled(false);
                alertDialog.getButton(alertDialog.BUTTON_POSITIVE).setEnabled(false);
            }
        }

        private boolean setSpinnerData(Spinner spinner, int selectedSectionId) {
            ArrayList<Section> sectionsToDisplay = new ArrayList<>();
            Realm realm = Realm.getDefaultInstance();
            RealmList<Section> sections = currentCourse.getSections();
            realm.close();

            for (Section section : sections) {
                int sectionId = section.getId();
                if (sectionId != selectedSectionId) {
                    sectionsToDisplay.add(section);
                }
            }

            ArrayAdapter<Section> adapter = new ArrayAdapter<>(mContext, android.R.layout.simple_spinner_dropdown_item, sectionsToDisplay);
            spinner.setAdapter(adapter);
            spinner.setSelection(0);

            if (sectionsToDisplay.size() > 0) {
                return true;
            }
            return false;
        }

        private void deleteSectionItem(final Item item) {
            if (isDeleteWithConfirm(mContext)) {
                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                builder.setMessage(R.string.msg_delete_confirm);
                builder.setNegativeButton(R.string.string_cancel, null);
                builder.setPositiveButton(R.string.string_delete, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteSectionItemConfirm(item);
                    }
                });
                builder.show();
            } else {
                deleteSectionItemConfirm(item);
            }
        }

        private void deleteSectionItemConfirm(Item item) {
            Realm realm = Realm.getDefaultInstance();
            realm.beginTransaction();
            item.deleteFromRealm();
            realm.commitTransaction();
            realm.close();
            showToastByForce(R.string.msg_delete);
            updateTextViewEmptyVisibility(Item.class, (int) itemView.getTag(), textViewCourseSectionDetailEmpty);
        }

        private void showToastByForce(int resId) {
            if (mToast != null) {
                mToast.cancel();
            }
            mToast = Toast.makeText(mContext, resId, Toast.LENGTH_LONG);
            mToast.show();
        }

        private void showToastByForce(String text) {
            if (mToast != null) {
                mToast.cancel();
            }
            mToast = Toast.makeText(mContext, text, Toast.LENGTH_LONG);
            mToast.show();
        }
    }
}