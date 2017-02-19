package com.renyu.bledemo.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.renyu.bledemo.R;

import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by renyu on 2017/2/16.
 */

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        ButterKnife.bind(this);
    }

    @OnClick({R.id.button_b, R.id.button_q, R.id.button_s, R.id.button_ota})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button_b:
                startActivity(new Intent(SplashActivity.this, BActivity.class));
                break;
            case R.id.button_q:
                startActivity(new Intent(SplashActivity.this, QActivity.class));
                break;
            case R.id.button_s:
                startActivity(new Intent(SplashActivity.this, SActivity.class));
                break;
            case R.id.button_ota:
                startActivity(new Intent(SplashActivity.this, OTAActivity.class));
                break;
        }
    }
}
