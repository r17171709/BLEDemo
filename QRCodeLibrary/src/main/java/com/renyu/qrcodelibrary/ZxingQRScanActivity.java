package com.renyu.qrcodelibrary;

import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.bingoogolapple.qrcode.core.QRCodeView;
import cn.bingoogolapple.qrcode.zxing.ZXingView;

/**
 * Created by renyu on 2017/1/19.
 */

public class ZxingQRScanActivity extends AppCompatActivity {

    ZXingView zxing_scan_view;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_zxingqrscan);
        ButterKnife.bind(this);

//        zxing_scan_view.changeToScanBarcodeStyle();
        zxing_scan_view = findViewById(R.id.zxing_scan_view);
        zxing_scan_view.setDelegate(new QRCodeView.Delegate() {
            @Override
            public void onScanQRCodeSuccess(String result) {
                Log.d("ZxingQRScanActivity", result);
                Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
                vibrator.vibrate(200);
                zxing_scan_view.startSpot();

                Intent intent=new Intent();
                intent.putExtra("result", result);
                setResult(RESULT_OK, intent);
                finish();
            }

            @Override
            public void onScanQRCodeOpenCameraError() {

            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        zxing_scan_view.startCamera();
//        zxing_scan_view.startCamera(Camera.CameraInfo.CAMERA_FACING_FRONT);
        zxing_scan_view.showScanRect();
        zxing_scan_view.post(new Runnable() {
            @Override
            public void run() {
                zxing_scan_view.startSpot();
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        zxing_scan_view.stopCamera();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        zxing_scan_view.onDestroy();
    }
}
