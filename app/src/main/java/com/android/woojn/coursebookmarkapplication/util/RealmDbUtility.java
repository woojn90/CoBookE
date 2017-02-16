package com.android.woojn.coursebookmarkapplication.util;

import android.view.View;
import android.widget.TextView;

import com.android.woojn.coursebookmarkapplication.model.Course;
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
        Number id = realm.where(clazz).max("id");
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
        if (clazz.equals(Course.class)) {
            if (realm.where(clazz).count() > 0) {
                textViewEmpty.setVisibility(View.GONE);
            } else {
                textViewEmpty.setVisibility(View.VISIBLE);
            }
        } else if (clazz.equals(Section.class)) {
            if (realm.where(Course.class).equalTo("id", parentId).findFirst().getSections().size() > 0) {
                textViewEmpty.setVisibility(View.GONE);
            } else {
                textViewEmpty.setVisibility(View.VISIBLE);
            }
        } else if (clazz.equals(SectionDetail.class)) {
            if (realm.where(Section.class).equalTo("id", parentId).findFirst().getSectionDetails().size() > 0) {
                textViewEmpty.setVisibility(View.GONE);
            } else {
                textViewEmpty.setVisibility(View.VISIBLE);
            }
        }
        realm.close();
    }
}
