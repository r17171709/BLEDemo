package com.renyu.bledemo.utils;

import android.app.Activity;
import android.util.Log;

import com.renyu.bledemo.app.MyApplication;
import com.renyu.bledemo.params.AddRequestBean;

import java.util.ArrayList;

import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Created by renyu on 2017/2/14.
 */

public class ReamlUtils {

    public static void write(Activity activity, final ArrayList<AddRequestBean> beans, final OnWriteResultListener listener) {
        Realm realm=((MyApplication) activity.getApplication()).realm;
        realm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                for (AddRequestBean bean : beans) {
                    realm.copyToRealmOrUpdate(bean);
                }
            }
        }, new Realm.Transaction.OnSuccess() {
            @Override
            public void onSuccess() {
                Log.d("ReamlUtils", "reaml onSuccess");
                if (listener!=null) {
                    listener.onSuccess();
                }
            }
        }, new Realm.Transaction.OnError() {
            @Override
            public void onError(Throwable error) {
                Log.d("ReamlUtils", "reaml onError");
                if (listener!=null) {
                    listener.onError(error);
                }
            }
        });
    }

    public static ArrayList<AddRequestBean> findOne(Activity activity, String machineId) {
        Realm realm=((MyApplication) activity.getApplication()).realm;
        RealmResults<AddRequestBean> results=realm.where(AddRequestBean.class).equalTo("machineId", machineId).findAll();
        ArrayList<AddRequestBean> temps=new ArrayList<>();
        for (AddRequestBean result : results) {
            temps.add(result);
        }
        return temps;
    }

    public interface OnWriteResultListener {
        void onSuccess();
        void onError(Throwable error);
    }
}
