package com.android.woojn.coursebookmarkapplication.model;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by wjn on 2017-02-18.
 */

public class Folder extends RealmObject {
    @PrimaryKey
    private int id;
    private String title;
    private RealmList<Folder> folders;
    private RealmList<Item> items;

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public RealmList<Folder> getFolders() {
        return folders;
    }

    public RealmList<Item> getItems() {
        return items;
    }
}
