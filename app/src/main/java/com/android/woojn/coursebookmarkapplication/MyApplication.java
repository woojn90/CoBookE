package com.android.woojn.coursebookmarkapplication;

import android.app.Application;

import io.realm.Realm;

/**
 * Created by wjn on 2017-02-08.
 */

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Realm.init(this);
    }
}