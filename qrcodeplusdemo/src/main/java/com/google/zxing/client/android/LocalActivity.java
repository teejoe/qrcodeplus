package com.google.zxing.client.android;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.Logging;
import com.google.zxing.LuminanceSource;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.ReaderException;
import com.google.zxing.Result;
import com.google.zxing.common.GlobalHistogramBinarizer;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;

import java.io.BufferedInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

/**
 * Created by mtj on 2017/10/24.
 */

public class LocalActivity extends Activity {

    public static final int ACTIVITY_PICK_FROM_GALLERY = 0x1010;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.local);

        findViewById(R.id.open_gallery).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                pickFromGallery();
            }
        });
    }

    public static LuminanceSource buildLuminanceImageFromBitmap(Bitmap bmp) {
        if (bmp == null) return null;
        int width = bmp.getWidth();
        int height = bmp.getHeight();
        int[] pixels = new int[width * height];

        bmp.getPixels(pixels, 0, width, 0, 0, width, height);
        return new RGBLuminanceSource(width, height, pixels);
    }

    private void decodeBitmap(final Bitmap bmp) {
        if (bmp == null) return;

        new AsyncTask<Void, Void, String>() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                //mCodeScanner.pauseDecode();
            }

            @Override
            protected String doInBackground(Void... voids) {
                LuminanceSource source = buildLuminanceImageFromBitmap(bmp);
                if (source == null) {
                    return null;
                }

                try {
                    //BinaryBitmap binaryBitmap = new BinaryBitmap(new HybridBinarizer(source));
                    BinaryBitmap binaryBitmap = new BinaryBitmap(new GlobalHistogramBinarizer(source));
                    Bitmap bitmap = Utils.binaryBitmap2Bitmap(binaryBitmap);
                    String path = String.format("/mnt/sdcard/qrcode_%d.png",
                            System.currentTimeMillis() / 1000);
                    Utils.saveBitmap(bitmap, path);

                    QRCodeReader re = new QRCodeReader();
                    Result rawResult = re.decode(binaryBitmap);
                    if (rawResult != null) {
                        return rawResult.getText();
                    }
                } catch (ReaderException e) {
                    //Logging.d("failed:" + e.getClass().getName() + e.getMessage());
                    //Log.e("m2x_log", "failed:" + Log.getStackTraceString(e));
                    //e.printStackTrace();
                    Logging.logWithMethodName("error");
                    Logging.logStackTrace(e);
                }
                return null;
            }

            @Override
            protected void onPostExecute(String result) {
                super.onPostExecute(result);

                if (result == null) {
                    Toast.makeText(LocalActivity.this, "未发现二维码", Toast.LENGTH_SHORT).show();
                } else {
                    //handleQrCodeContent(result);
                    Toast.makeText(LocalActivity.this, result, Toast.LENGTH_LONG).show();
                }
            }
        }.execute();
    }

    private void pickFromGallery() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setAction(Intent.ACTION_GET_CONTENT);

        if (intent.resolveActivity(getPackageManager()) == null) return;

        startActivityForResult(Intent.createChooser(intent, "请选择打开的应用"),
                ACTIVITY_PICK_FROM_GALLERY);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == ACTIVITY_PICK_FROM_GALLERY && resultCode == RESULT_OK) {
            if (intent == null) return;
            try {
                InputStream inputStream = getContentResolver().openInputStream(intent.getData());
                Bitmap bmp = BitmapFactory.decodeStream(new BufferedInputStream(inputStream));
                decodeBitmap(bmp);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }
}
