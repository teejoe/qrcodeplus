/*
 * Copyright (C) 2008 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.m2x.qrcodescanner;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import com.m2x.qrcodescanner.camera.CameraManager;


/**
 * This view is overlaid on top of the camera preview. It adds the viewfinder rectangle and partial
 * transparency outside it, as well as the laser scanner animation.
 * If you want to customize onDraw, please override drawMask(), drawLaser(), drawCustom() method.
 * onDraw() is final here.
 * if you wan to modify framing rect size, please use QRCodeScanner.setFrameRect(int width, int height).
 *
 * @author teejoe@163.com (mtj)
 */
public class ViewfinderView extends View {


    protected static final int POINT_SIZE = 6;

    private ScaleGestureDetector mScaleDetector;
    private GestureDetector mDoubleTabDetector;
    private CameraManager cameraManager;
    private Rect framingRect;

    protected final Paint maskPaint;
    protected final Paint laserPaint;
    protected final int maskColor = 0x60000000;
    protected final int laserColor = 0xffcc0000;

    protected long animateBeginTime;
    protected boolean animateLaserLine = false;
    protected int animatePeriod = 2500;   // milliseconds.

    // This constructor is used when the class is built from an XML resource.
    public ViewfinderView(Context context, AttributeSet attrs) {
        super(context, attrs);

        // Initialize these once for performance rather than calling them every time in onDraw().
        mScaleDetector = new ScaleGestureDetector(context, new ScaleListener());
        mDoubleTabDetector = new GestureDetector(context, new DoubleTapListener());

        maskPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        laserPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        maskPaint.setColor(maskColor);
        laserPaint.setColor(laserColor);
    }

    public void setCameraManager(CameraManager cameraManager) {
        this.cameraManager = cameraManager;
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            if (cameraManager == null) return false;

            try {
                int currentZoom = cameraManager.getZoom();
                if (detector.getScaleFactor() > 1.0f) {
                    currentZoom++;
                } else {
                    currentZoom--;
                }
                cameraManager.setZoom(currentZoom);
                return true;
            } catch (Exception e) {
                return false;
            }
        }
    }

    private class DoubleTapListener extends SimpleOnGestureListener {
        @Override
        public boolean onDoubleTap(MotionEvent e) {
            if (cameraManager == null) return false;

            int currentZoom = cameraManager.getZoom();
            int maxZoom = cameraManager.getMaxZoom();
            if (currentZoom != maxZoom) {
                currentZoom = maxZoom;
            } else {
                currentZoom = 0;
            }
            cameraManager.setZoom(currentZoom);
            return true;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        boolean scale = mScaleDetector.onTouchEvent(e);
        boolean doubleTap = mDoubleTabDetector.onTouchEvent(e);

        return scale || doubleTap || super.onTouchEvent(e);
    }

    @Override
    public void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        updateFrameRect(w, h);
    }

    @Override
    public final void onDraw(Canvas canvas) {
        if (cameraManager == null) {
            return; // not ready yet, early draw before done configuring
        }

        if (framingRect == null) {
            updateFrameRect(canvas.getWidth(), canvas.getHeight());
        }

        if (framingRect == null) {
            return;
        }

        drawMask(canvas, framingRect);

        drawLaser(canvas, framingRect);

        drawCustom(canvas, framingRect);
    }

    private void updateFrameRect(int width, int height) {
        if (cameraManager != null) {
            Rect previewRect = cameraManager.getFramingRect();
            Rect fullRect = cameraManager.getFullRect();

            if (previewRect == null || fullRect == null) return;

            float xRatio = (float) width / fullRect.width();
            float yRatio = (float) height / fullRect.height();

            framingRect = new Rect((int) (xRatio * previewRect.left),
                    (int) (yRatio * previewRect.top),
                    (int) (xRatio * previewRect.right),
                    (int) (yRatio * previewRect.bottom));
            onFrameRectUpdated(framingRect);
        }
    }

    protected void onFrameRectUpdated(Rect framingRect) {
    }

    protected void drawMask(Canvas canvas, Rect frame) {
        int width = canvas.getWidth();
        int height = canvas.getHeight();
        canvas.drawRect(0, 0, width, frame.top, maskPaint);
        canvas.drawRect(0, frame.top, frame.left, frame.bottom + 1, maskPaint);
        canvas.drawRect(frame.right + 1, frame.top, width, frame.bottom + 1, maskPaint);
        canvas.drawRect(0, frame.bottom + 1, width, height, maskPaint);
    }

    protected void drawLaser(Canvas canvas, Rect frame) {
        int middle = frame.height() / 2 + frame.top;
        if (animateLaserLine) {
            middle += getLaserVerticalPosOffset();
        }
        canvas.drawRect(frame.left + 2, middle - 1, frame.right - 1, middle + 2, laserPaint);

        // Request another update at the animation interval, but only repaint the laser line,
        // not the entire viewfinder mask.
        if (animateLaserLine) {
            invalidate(
                    frame.left - POINT_SIZE,
                    frame.top - POINT_SIZE,
                    frame.right + POINT_SIZE,
                    frame.bottom + POINT_SIZE);
        }
    }

    protected void drawCustom(Canvas canvas, Rect frame) {

    }

    public void beginAnimateLaser() {
        animateBeginTime = System.currentTimeMillis();
        animateLaserLine = true;
    }

    private float getLaserVerticalPosOffset() {
        final float height = framingRect.height();

        long t = (System.currentTimeMillis() - animateBeginTime) % (2 * animatePeriod);
        if (t < animatePeriod) {
            return (float) (-height / 2 * Math.cos(Math.PI * t / animatePeriod));
        } else {
            return (float) (height / 2 * Math.cos(Math.PI * t / animatePeriod));
        }
    }
}
