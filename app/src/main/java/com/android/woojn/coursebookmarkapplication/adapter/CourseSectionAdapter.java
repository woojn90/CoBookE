package com.android.woojn.coursebookmarkapplication.adapter;

import static com.android.woojn.coursebookmarkapplication.Constants.FIELD_NAME_ID;
import static com.android.woojn.coursebookmarkapplication.Constants.KEY_SECTION_ID;
import static com.android.woojn.coursebookmarkapplication.Constants.KEY_STRING_URL;
import static com.android.woojn.coursebookmarkapplication.util.DisplayUtility.showItemDialog;
import static com.android.woojn.coursebookmarkapplication.util.DisplayUtility.showPopupMenuIcon;
import static com.android.woojn.coursebookmarkapplication.util.RealmDbUtility
        .updateTextViewEmptyVisibility;
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
import android.widget.TextView;
import android.widget.Toast;

import com.android.woojn.coursebookmarkapplication.R;
import com.android.woojn.coursebookmarkapplication.activity.WebActivity;
import com.android.woojn.coursebookmarkapplication.model.Item;
import com.android.woojn.coursebookmarkapplication.model.Section;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.realm.OrderedRealmCollection;
import io.realm.Realm;
import io.realm.RealmRecyclerViewAdapter;
import io.realm.RealmResults;

/**
 * Created by wjn on 2017-02-07.
 */

public class CourseSectionAdapter extends RealmRecyclerViewAdapter<Section, CourseSectionAdapter.CourseSectionViewHolder> {

    private final Context mContext;
    private final OnRecyclerViewClickListener mListener;
    private Toast mToast;

    public CourseSectionAdapter(Context context, OrderedRealmCollection<Section> data, OnRecyclerViewClickListener listener) {
        super(context, data, true);
        mListener = listener;
        mContext = context;
    }

    public interface OnRecyclerViewClickListener {
        void onItemClick(int id, View view);
        void onItemLongClick(int id);
        void onItemDoubleTap(int id);
    }

    @Override
    public CourseSectionViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.course_section_list_item, parent, false);
        return new CourseSectionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final CourseSectionViewHolder holder, int position) {
        if (getData() != null) {
            final Section section = getData().get(position);
            holder.itemView.setTag(section.getId());
            holder.textViewSectionTitle.setText(section.getTitle());
            holder.textViewSectionSearchWord.setText("(" + section.getSearchWord() + ")");

            RealmResults<Item> sectionItems = section.getItems().sort(FIELD_NAME_ID);
            holder.recyclerViewCourseSectionDetail.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
            holder.recyclerViewCourseSectionDetail.setAdapter(new CourseSectionItemAdapter(mContext, sectionItems, holder));
            updateTextViewEmptyVisibility(Item.class, section.getId(), holder.textViewCourseSectionDetailEmpty);
        }
    }

    class CourseSectionViewHolder extends RecyclerView.ViewHolder
        implements CourseSectionItemAdapter.OnRecyclerViewClickListener {

        @BindView(R.id.tv_section_title)
        TextView textViewSectionTitle;
        @BindView(R.id.tv_section_search_word)
        TextView textViewSectionSearchWord;
        @BindView(R.id.tv_section_item_empty)
        TextView textViewCourseSectionDetailEmpty;
        @BindView(R.id.rv_section_item_list)
        RecyclerView recyclerViewCourseSectionDetail;

        public CourseSectionViewHolder(final View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            final GestureDetector gd = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
                @Override
                public void onLongPress(MotionEvent e) {
                    mListener.onItemLongClick((int) itemView.getTag());
                    return;
                }

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

        @OnClick({R.id.btn_section_search_page, R.id.btn_section_share})
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
        }

        @Override
        public void onItemLongClick(int id) {
            Realm realm = Realm.getDefaultInstance();
            Item item = realm.where(Item.class).equalTo(FIELD_NAME_ID, id).findFirst();
            deleteSectionItem(item);
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
                                case R.id.item_share_section_item:
                                    shareTextByRealmObject(mContext, item);
                                    realm.close();
                                    return true;
                            }
                            realm.close();
                            return false;
                        }
                    });
                    popupMenu.inflate(R.menu.menu_in_section_item_view);
                    popupMenu.show();
                    showPopupMenuIcon(popupMenu);
                    break;
            }
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
            if (mToast != null) {
                mToast.cancel();
            }
            mToast = Toast.makeText(mContext, R.string.msg_delete, Toast.LENGTH_LONG);
            mToast.show();
            updateTextViewEmptyVisibility(Item.class, (int) itemView.getTag(), textViewCourseSectionDetailEmpty);
        }
    }
}