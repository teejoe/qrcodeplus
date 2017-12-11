package com.m2x.qrcodeplus;

import android.Manifest.permission;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;


public class SplashActivity extends AppCompatActivity {

    private static final int CAMERA_PERMISSION_REQ = 1;
    private static final int SCANNER_ACTIVITY = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash);

        if (requestCameraPermission()) {
            startActivityForResult(new Intent(this, QRCodeScanActivity.class),
                    SCANNER_ACTIVITY);
        }
    }

    private boolean requestCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            return true;
        }

        ActivityCompat.requestPermissions(
                this,
                new String[]{permission.CAMERA},
                CAMERA_PERMISSION_REQ);

        return false;
    }


    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            String permissions[],
            int[] grantResults) {

        switch (requestCode) {
            case CAMERA_PERMISSION_REQ: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startActivityForResult(new Intent(this, QRCodeScanActivity.class),
                            SCANNER_ACTIVITY);
                } else {
                    finish();
                }
                return;
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SCANNER_ACTIVITY) {
            finish();
        }

        super.onActivityResult(requestCode, resultCode, data);
    }
}