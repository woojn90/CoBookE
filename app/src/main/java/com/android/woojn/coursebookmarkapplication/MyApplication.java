package com.android.woojn.coursebookmarkapplication;

import android.app.Application;

import io.realm.DynamicRealm;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmMigration;

/**
 * Created by wjn on 2017-02-08.
 */

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Realm.init(this);

        RealmMigration migration = new RealmMigration() {
            @Override
            public void migrate(DynamicRealm realm, long oldVersion, long newVersion) {
                // Do nothing (DB 변경시 수정)
            }
        };

        RealmConfiguration config = new RealmConfiguration.Builder()
                .schemaVersion(0)
                .migration(migration)
                .build();
        Realm.setDefaultConfiguration(config);
    }
}