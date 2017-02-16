package com.renyu.bledemo.utils;

import android.util.Log;

import com.renyu.bledemo.impl.BmobImpl;
import com.renyu.bledemo.params.AddResultBean;
import com.renyu.bledemo.params.TableBean;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by renyu on 2017/2/3.
 */

public class UploadUtils {

    public static void createTable(String... column) {
        RequestBody body=RequestBody.create(MediaType.parse("application/json; charset=utf-8"), getTableString(column));
        Retrofit2Utils.getInstance().getRetrofit().create(BmobImpl.class)
                .createTable("82f157b2c817f3743d61db908dd8bbaa", "d3f3a33c38bbb570c34666ce540cc3af", body)
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(AndroidSchedulers.mainThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<TableBean>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(TableBean bean) {
                        Log.d("SActivity", "OK");
                    }
                });
    }

    private static String getTableString(String... column) {
        String json=null;
        try {
            JSONObject object=new JSONObject();
            object.put("className", "bledata");

            JSONObject stringObjectType=new JSONObject();
            stringObjectType.put("type", "String");

            JSONObject fields=new JSONObject();
            for (String value : column) {
                fields.put(value, stringObjectType);
            }

            object.put("fields", fields);

            json=object.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json;
    }

    public static void addValue(HashMap<String, String> values) {
        JSONObject object=new JSONObject();
        try {
            Iterator<Map.Entry<String, String>> iterator=values.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, String> entry=iterator.next();
                object.put(entry.getKey(), entry.getValue());
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        RequestBody body=RequestBody.create(MediaType.parse("application/json; charset=utf-8"), object.toString());
        Retrofit2Utils.getInstance().getRetrofit().create(BmobImpl.class)
                .addValue("82f157b2c817f3743d61db908dd8bbaa", "2e6564067b2299d00c53322e0e8e65d7", "application/json", body)
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(AndroidSchedulers.mainThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<AddResultBean>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(AddResultBean addResultBean) {
                        Log.d("SActivity", "OK");
                    }
                });
    }
}
