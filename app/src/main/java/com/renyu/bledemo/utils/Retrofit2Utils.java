package com.renyu.bledemo.utils;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by renyu on 2017/2/3.
 */

public class Retrofit2Utils {

    private static volatile Retrofit2Utils retrofit2Utils;

    private OkHttpClient.Builder okBuilder;
    private Retrofit retrofit;

    private Retrofit2Utils() {
        okBuilder=new OkHttpClient.Builder()
                .readTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .connectTimeout(10, TimeUnit.SECONDS);
        retrofit=new Retrofit.Builder()
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .client(okBuilder.build()).baseUrl("https://api.bmob.cn/").build();
    }

    public static Retrofit2Utils getInstance() {
        if (retrofit2Utils==null) {
            synchronized (Retrofit2Utils.class) {
                if (retrofit2Utils==null) {
                    retrofit2Utils=new Retrofit2Utils();
                }
            }
        }
        return retrofit2Utils;
    }

    public Retrofit getRetrofit() {
        return retrofit;
    }
}
