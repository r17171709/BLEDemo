package com.renyu.bledemo.impl;

import com.renyu.bledemo.params.AddResultBean;
import com.renyu.bledemo.params.TableBean;

import okhttp3.RequestBody;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;
import rx.Observable;

/**
 * Created by renyu on 2017/2/3.
 */

public interface BmobImpl {
    @POST("1/schemas/bledata")
    Observable<TableBean> createTable(
            @Header("X-Bmob-Application-Id") String applicationId,
            @Header("X-Bmob-Master-Key") String masterKey,
            @Body RequestBody requestBody);

    @POST("/1/classes/bledata")
    Observable<AddResultBean> addValue(
            @Header("X-Bmob-Application-Id") String applicationId,
            @Header("X-Bmob-REST-API-Key") String masterKey,
            @Header("Content-Type") String contentType,
            @Body RequestBody requestBody);
}
