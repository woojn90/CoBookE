package com.android.woojn.coursebookmarkapplication.util;

import static com.android.woojn.coursebookmarkapplication.Constants.DEFAULT_FOLDER_ID;
import static com.android.woojn.coursebookmarkapplication.Constants.FIELD_NAME_ID;
import static com.android.woojn.coursebookmarkapplication.fragment.ItemFragment.currentFolderId;
import static com.android.woojn.coursebookmarkapplication.fragment.ItemFragment.folderIds;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import com.android.woojn.coursebookmarkapplication.R;
import com.android.woojn.coursebookmarkapplication.model.Course;
import com.android.woojn.coursebookmarkapplication.model.Folder;
import com.android.woojn.coursebookmarkapplication.model.Item;
import com.android.woojn.coursebookmarkapplication.model.Section;

import java.util.ArrayList;

import io.realm.Realm;
import io.realm.RealmModel;

/**
 * Created by wjn on 2017-02-09.
 */

public class RealmDbUtility {

    public static <E extends RealmModel> int getNewIdByClass(Class<E> clazz) {
        int newId;
        Realm realm = Realm.getDefaultInstance();
        Number id = realm.where(clazz).max(FIELD_NAME_ID);
        realm.close();

        if (id == null) {
            newId = 1;
        } else {
            newId = id.intValue() + 1;
        }
        return newId;
    }

    public static <E extends RealmModel> void updateTextViewEmptyVisibility(Class<E> clazz, int parentId, TextView textViewEmpty) {
        Realm realm = Realm.getDefaultInstance();

        if (clazz.equals(Section.class)) {
            if (realm.where(Course.class).equalTo(FIELD_NAME_ID, parentId).findFirst().getSections().size() > 0) {
                textViewEmpty.setVisibility(View.GONE);
            } else {
                textViewEmpty.setVisibility(View.VISIBLE);
            }
        } else if (clazz.equals(Item.class)) {
            if (realm.where(Section.class).equalTo(FIELD_NAME_ID, parentId).findFirst().getItems().size() > 0) {
                textViewEmpty.setVisibility(View.GONE);
            } else {
                textViewEmpty.setVisibility(View.VISIBLE);
            }
        } else {
            if (realm.where(clazz).count() > 0) {
                textViewEmpty.setVisibility(View.GONE);
            } else {
                textViewEmpty.setVisibility(View.VISIBLE);
            }
        }
        realm.close();
    }

    public static void updateTextViewEmptyVisibilityByFolderId(int folderId, TextView textViewEmpty) {
        Realm realm = Realm.getDefaultInstance();

        Folder parentFolder = realm.where(Folder.class).equalTo(FIELD_NAME_ID, folderId).findFirst();
        if (parentFolder.getItems().size() > 0 || parentFolder.getFolders().size() > 0) {
            textViewEmpty.setVisibility(View.GONE);
        } else {
            textViewEmpty.setVisibility(View.VISIBLE);
        }
        realm.close();
    }

    public static void insertDefaultFolderIfNeeded(Context context) {
        Realm realm = Realm.getDefaultInstance();
        if (realm.where(Folder.class).equalTo(FIELD_NAME_ID, DEFAULT_FOLDER_ID).findFirst() == null) {
            realm.beginTransaction();
            folderIds = new ArrayList<>();
            folderIds.add(DEFAULT_FOLDER_ID);
            currentFolderId = DEFAULT_FOLDER_ID;
            Folder folder = realm.createObject(Folder.class, currentFolderId);
            folder.setTitle(context.getString(R.string.string_home));
            realm.commitTransaction();
        }
        realm.close();
    }

    // TODO: Resourse 분리 등의 관리가 필요
    public static void insertInitialData(Context context) {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        Course courseEx1 = realm.createObject(Course.class, getNewIdByClass(Course.class));
        courseEx1.setTitle("강남역 근처 데이트");
        courseEx1.setSearchWord("강남역");
        courseEx1.setDesc("강남역 근처 코스 예시");
        courseEx1.setFavorite(true);
        Section sectionEx11 = realm.createObject(Section.class, getNewIdByClass(Section.class));
        sectionEx11.setTitle("식당");
        sectionEx11.setSearchWord("맛집");
        Item sectionItemEx111 = realm.createObject(Item.class, getNewIdByClass(Item.class));
        sectionItemEx111.setUrl("http://m.blog.naver.com/kunby/220926402527");
        Item sectionItemEx112 = realm.createObject(Item.class, getNewIdByClass(Item.class));
        sectionItemEx112.setUrl("http://m.blog.naver.com/jollynii1/220926663281");
        Item sectionItemEx113 = realm.createObject(Item.class, getNewIdByClass(Item.class));
        sectionItemEx113.setUrl("http://m.blog.naver.com/dahaeyoo/220941988349");
        Section sectionEx12 = realm.createObject(Section.class, getNewIdByClass(Section.class));
        sectionEx12.setTitle("카페");
        sectionEx12.setSearchWord("분위기 카페");
        Item sectionItemEx121 = realm.createObject(Item.class, getNewIdByClass(Item.class));
        sectionItemEx121.setUrl("http://m.blog.naver.com/jvely0426/220941984868");
        Section sectionEx13 = realm.createObject(Section.class, getNewIdByClass(Section.class));
        sectionEx13.setTitle("술집");
        sectionEx13.setSearchWord("조용한 술집");
        Item sectionItemEx131 = realm.createObject(Item.class, getNewIdByClass(Item.class));
        sectionItemEx131.setUrl("http://m.blog.naver.com/party_tasty/220759306435");
        Item sectionItemEx132 = realm.createObject(Item.class, getNewIdByClass(Item.class));
        sectionItemEx132.setUrl("http://m.blog.naver.com/darong_1231/220864983989");

        sectionEx11.getItems().add(sectionItemEx111);
        sectionEx11.getItems().add(sectionItemEx112);
        sectionEx11.getItems().add(sectionItemEx113);
        sectionEx12.getItems().add(sectionItemEx121);
        sectionEx13.getItems().add(sectionItemEx131);
        sectionEx13.getItems().add(sectionItemEx132);
        courseEx1.getSections().add(sectionEx11);
        courseEx1.getSections().add(sectionEx12);
        courseEx1.getSections().add(sectionEx13);

        Folder folderEx1 = realm.createObject(Folder.class, getNewIdByClass(Folder.class));
        folderEx1.setTitle("공부");
        Folder folderEx3 = realm.createObject(Folder.class, getNewIdByClass(Folder.class));
        folderEx3.setTitle("뉴스");

        Item itemEx1 = realm.createObject(Item.class, getNewIdByClass(Item.class));
        itemEx1.setUrl(context.getString(R.string.pref_value_home_page_naver));
        Item itemEx2 = realm.createObject(Item.class, getNewIdByClass(Item.class));
        itemEx2.setUrl(context.getString(R.string.pref_value_home_page_daum));

        Folder homeFolder = realm.where(Folder.class).equalTo(FIELD_NAME_ID, DEFAULT_FOLDER_ID).findFirst();
        homeFolder.getFolders().add(folderEx1);
        homeFolder.getFolders().add(folderEx3);
        homeFolder.getItems().add(itemEx1);
        homeFolder.getItems().add(itemEx2);

        realm.commitTransaction();
        realm.close();
    }

}
