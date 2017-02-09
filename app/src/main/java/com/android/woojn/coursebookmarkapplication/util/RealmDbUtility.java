package com.android.woojn.coursebookmarkapplication.util;

import io.realm.Realm;
import io.realm.RealmModel;

/**
 * Created by wjn on 2017-02-09.
 */

public class RealmDbUtility {

    // beginTransaction(); 후에 사용해야함
    public static <E extends RealmModel> int getNewIdByClass(Realm realm, Class<E> clazz) {
        int newId;
        Number id = realm.where(clazz).max("id");

        if (id == null) {
            newId = 1;
        } else {
            newId = id.intValue() + 1;
        }
        return newId;
    }
}
