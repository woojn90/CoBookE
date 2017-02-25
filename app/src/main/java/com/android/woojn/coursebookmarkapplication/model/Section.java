package com.android.woojn.coursebookmarkapplication.model;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by wjn on 2017-02-08.
 */

public class Section extends RealmObject {

    @PrimaryKey
    private int id;
    private String title;
    private String searchWord;
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

    public String getSearchWord() {
        return searchWord;
    }

    public void setSearchWord(String searchWord) {
        this.searchWord = searchWord;
    }

    public RealmList<Item> getItems() {
        return items;
    }

    /**
     * spinner에서 사용하기 위해 title만 return합니다.
     */
    @Override
    public String toString() {
        return title;
    }
}