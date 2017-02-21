package com.android.woojn.coursebookmarkapplication.util;

import static com.android.woojn.coursebookmarkapplication.Constants.FIELD_NAME_ID;

import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.android.woojn.coursebookmarkapplication.model.Course;
import com.android.woojn.coursebookmarkapplication.model.Folder;
import com.android.woojn.coursebookmarkapplication.model.Item;
import com.android.woojn.coursebookmarkapplication.model.Section;
import com.android.woojn.coursebookmarkapplication.model.SectionDetail;

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

    public static <E extends RealmModel> void setTextViewEmptyVisibility(Class<E> clazz, int parentId, TextView textViewEmpty) {
        Realm realm = Realm.getDefaultInstance();

        if (clazz.equals(Section.class)) {
            if (realm.where(Course.class).equalTo(FIELD_NAME_ID, parentId).findFirst().getSections().size() > 0) {
                textViewEmpty.setVisibility(View.GONE);
            } else {
                textViewEmpty.setVisibility(View.VISIBLE);
            }
        } else if (clazz.equals(SectionDetail.class)) {
            if (realm.where(Section.class).equalTo(FIELD_NAME_ID, parentId).findFirst().getSectionDetails().size() > 0) {
                textViewEmpty.setVisibility(View.GONE);
            } else {
                textViewEmpty.setVisibility(View.VISIBLE);
            }
        } else if (clazz.equals(Folder.class) || clazz.equals(Item.class)) {
            if (realm.where(Folder.class).count() > 0 || realm.where(Item.class).count() > 0) {
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

    public static void setTextViewEmptyVisibilityByFolderId(int folderId, TextView textViewEmpty) {
        Realm realm = Realm.getDefaultInstance();

        Folder parentFolder = realm.where(Folder.class).equalTo(FIELD_NAME_ID, folderId).findFirst();
        if (parentFolder.getItems().size() > 0 || parentFolder.getFolders().size() > 0) {
            textViewEmpty.setVisibility(View.GONE);
        } else {
            textViewEmpty.setVisibility(View.VISIBLE);
        }
        realm.close();
    }
}
