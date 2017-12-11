package com.m2x.qrcodeplus;

import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.m2x.qrcodescanner.DecodeResult;
import com.m2x.qrcodescanner.QRCodeScanner;
import com.m2x.qrcodescanner.ViewfinderView;


public class QRCodeScanActivity extends AppCompatActivity {

    private QRCodeScanner mQRCodeScanner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setTitle("QRCode Scanner Demo");
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        SurfaceView surfaceView = findViewById(R.id.surface_view);
        ViewfinderView viewFinderView = findViewById(R.id.viewfinder_view);
        final ImageView resultView = findViewById(R.id.result_view);

        mQRCodeScanner = new QRCodeScanner(this, surfaceView, viewFinderView) {
            @Override
            protected void onFatalError(String message) {
                Toast.makeText(QRCodeScanActivity.this, "" + message, Toast.LENGTH_LONG).show();
            }

            @Override
            protected void handleDecodeSuccess(DecodeResult result) {
                vibrate();
                new AlertDialog.Builder(QRCodeScanActivity.this)
                        .setMessage(result.rawResult.getText())
                        .setCancelable(false)
                        .setPositiveButton("ok", new OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mQRCodeScanner.resumeDecode();
                                resultView.setVisibility(View.GONE);
                            }
                        }).show();

                resultView.setVisibility(View.VISIBLE);
                resultView.setImageBitmap(result.preview);
            }
        };
    }

    @Override
    public void onResume() {
        super.onResume();
        mQRCodeScanner.resume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mQRCodeScanner.pause();
    }

    private synchronized void vibrate() {
        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator == null) return;
        try {
            vibrator.vibrate(200);
        } catch (SecurityException e) {
            // let this go
        }
    }
}