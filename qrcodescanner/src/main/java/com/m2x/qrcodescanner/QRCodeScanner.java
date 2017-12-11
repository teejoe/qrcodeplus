package com.m2x.qrcodescanner;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.os.Handler;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.google.zxing.Logging;
import com.google.zxing.Result;
import com.m2x.qrcodescanner.R;
import com.m2x.qrcodescanner.camera.CameraManager;

import java.io.IOException;

/**
 * Created by mtj on 2017/12/7.
 */

public abstract class QRCodeScanner implements SurfaceHolder.Callback {

    private static final String TAG = QRCodeScanner.class.getSimpleName();

    private Context mContext;
    private SurfaceView mSurfaceView;
    private ViewfinderView mViewFinderView;
    private CameraManager cameraManager;
    private CaptureActivityHandler mHandler;
    private AmbientLightManager ambientLightManager;
    private boolean hasSurface;
    private int requestedFramingRectWidth;
    private int requestedFramingRectHeight;
    private boolean paused = false;

    public QRCodeScanner(Activity activity, SurfaceView surfaceView, ViewfinderView viewfinderView) {
        mContext = activity.getApplicationContext();
        mSurfaceView = surfaceView;
        mViewFinderView = viewfinderView;
        ambientLightManager = new AmbientLightManager(mContext);
        hasSurface = false;
    }

    public Handler getHandler() {
        return mHandler;
    }

    public CameraManager getCameraManager() {
        return cameraManager;
    }

    /**
     * Should be called in Activity.onResume()
     */
    public void resume() {
        cameraManager = new CameraManager(mContext);
        mViewFinderView.setCameraManager(cameraManager);
        if (requestedFramingRectWidth > 0 && requestedFramingRectHeight > 0) {
            cameraManager.setManualFramingRect(requestedFramingRectWidth, requestedFramingRectHeight);
        }

        mHandler = null;
        ambientLightManager.start(cameraManager);

        SurfaceHolder surfaceHolder = mSurfaceView.getHolder();
        if (hasSurface) {
            // The activity was paused but not stopped, so the surface still exists. Therefore
            // surfaceCreated() won't be called, so init the camera here.
            initCamera(surfaceHolder);
        } else {
            // Install the callback and wait for surfaceCreated() to init the camera.
            surfaceHolder.addCallback(this);
        }
    }

    /**
     * Show be called in Activity.onPause()
     */
    public void pause() {
        if (mHandler != null) {
            mHandler.quitSynchronously();
            mHandler = null;
        }
        ambientLightManager.stop();
        cameraManager.closeDriver();
        //historyManager = null; // Keep for onActivityResult
        if (!hasSurface) {
            SurfaceHolder surfaceHolder = mSurfaceView.getHolder();
            surfaceHolder.removeCallback(this);
        }
    }

    public void setFramingRect(int width, int height) {
        if (cameraManager != null) {
            cameraManager.setManualFramingRect(width, height);
        } else {
            requestedFramingRectWidth = width;
            requestedFramingRectHeight = height;
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (holder == null) {
            Log.e(TAG, "*** WARNING *** surfaceCreated() gave us a null surface!");
        }
        if (!hasSurface) {
            hasSurface = true;
            initCamera(holder);
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        hasSurface = false;
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        // do nothing
    }

    private void initCamera(SurfaceHolder surfaceHolder) {
        if (surfaceHolder == null) {
            throw new IllegalStateException("No SurfaceHolder provided");
        }
        if (cameraManager.isOpen()) {
            Log.w(TAG, "initCamera() while already open -- late SurfaceView callback?");
            return;
        }
        try {
            cameraManager.openDriver(surfaceHolder);
            // Creating the handler starts the preview, which can also throw a RuntimeException.
            if (mHandler == null) {
                mHandler = new CaptureActivityHandler(this, null, null, null, cameraManager);
            }
        } catch (IOException ioe) {
            Log.w(TAG, ioe);
            onFatalError(ioe.getMessage());
        } catch (RuntimeException e) {
            // Barcode Scanner has seen crashes in the wild of this variety:
            // java.?lang.?RuntimeException: Fail to connect to camera service
            Log.w(TAG, "Unexpected error initializing camera", e);
            onFatalError(e.getMessage());
        }
        mViewFinderView.beginAnimateLaser();

        if (paused) {
            pauseDecode();
        }
    }

    public void handleDecode(Result rawResult, Bitmap barcode, float scaleFactor) {
        if (cameraManager.getCWNeededRotation() != 0) {
            // rotate bitmap;
            Matrix matrix = new Matrix();
            matrix.postRotate(cameraManager.getCWNeededRotation());
            barcode = Bitmap.createBitmap(barcode, 0, 0,
                    barcode.getWidth(), barcode.getHeight(), matrix, true);
        }

        DecodeResult result = new DecodeResult();
        result.rawResult = rawResult;
        result.preview = barcode;
        handleDecodeSuccess(result);
    }

    public void enableLog(boolean enable) {
        Logging.enableLog(enable);
    }

    public void pauseDecode() {
        paused = true;
        if (mHandler != null) {
            mHandler.pause();
        }
    }

    public void resumeDecode() {
        paused = false;
        if (cameraManager != null) {
            cameraManager.setZoom(0);
        }
        if (mHandler != null) {
            mHandler.sendEmptyMessageDelayed(R.id.restart_preview, 0);
        }
    }

    protected abstract void onFatalError(String message);

    protected abstract void handleDecodeSuccess(DecodeResult result);
}
