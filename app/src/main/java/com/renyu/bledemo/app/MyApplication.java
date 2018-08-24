package com.renyu.bledemo.app;

import android.app.Application;

import com.blankj.utilcode.util.Utils;

import io.realm.Realm;
import io.realm.RealmConfiguration;

/**
 * Created by renyu on 2017/2/14.
 */

public class MyApplication extends Application {

    public Realm realm;

    @Override
    public void onCreate() {
        super.onCreate();

        Utils.init(this);

        Realm.init(this);
        RealmConfiguration configuration=new RealmConfiguration.Builder().name("devicesinfo").schemaVersion(1).build();
        realm=Realm.getInstance(configuration);
    }
}
