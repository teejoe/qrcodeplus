/*
 * Copyright (C) 2010 ZXing authors
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

package com.google.zxing.client.android;

import android.graphics.Bitmap;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.ContrastedLuminanceSource;
import com.google.zxing.DecodeHintType;
import com.google.zxing.DecodeState;
import com.google.zxing.DownscaledLuminanceSource;
import com.google.zxing.Logging;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.PlanarYUVLuminanceSource;
import com.google.zxing.ReaderException;
import com.google.zxing.Result;
import com.google.zxing.common.StatefulBinarizer;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.util.Map;

final class DecodeHandler extends Handler {

    private static final String TAG = DecodeHandler.class.getSimpleName();

    private final CaptureActivity activity;
    private final MultiFormatReader multiFormatReader;
    private boolean running = true;
    private DecodeState mDecodeState;

    DecodeHandler(CaptureActivity activity, Map<DecodeHintType, Object> hints) {
        multiFormatReader = new MultiFormatReader();
        multiFormatReader.setHints(hints);
        mDecodeState = new DecodeState();
        hints.put(DecodeHintType.DECODE_STATE, mDecodeState);
        this.activity = activity;
    }

    public void resetDecodeState() {
        mDecodeState.reset();
    }

    @Override
    public void handleMessage(Message message) {
        if (message == null || !running) {
            return;
        }
        switch (message.what) {
            case R.id.decode:
                decode((byte[]) message.obj, message.arg1, message.arg2);
                break;
            case R.id.quit:
                running = false;
                Looper.myLooper().quit();
                break;
        }
    }

    /**
     * Decode the data within the viewfinder rectangle, and time how long it took. For efficiency,
     * reuse the same reader objects from one decode to the next.
     *
     * @param data   The YUV preview frame.
     * @param width  The width of the preview frame.
     * @param height The height of the preview frame.
     */
    private void decode(byte[] data, int width, int height) {
        long start = System.currentTimeMillis();
        mDecodeState.currentRound++;
        if (mDecodeState.currentRound == 1) {
            mDecodeState.startTime = start;
        }

        Logging.d("decode round:" + mDecodeState.currentRound);
        Result rawResult = null;
        PlanarYUVLuminanceSource source = activity.getCameraManager().buildLuminanceSource(data, width, height);
        LuminanceSource processedSource = source;
        if (mDecodeState.previousFailureHint.lowContrastImage) {
            // increase contrast.
            Logging.d("increase contrast");
            processedSource = new ContrastedLuminanceSource(source);
        } else {
            if ((System.currentTimeMillis() & 0x03) == 0) {
                // randomly increase contrast.
                Logging.d("randomly increase contrast");
                processedSource = new ContrastedLuminanceSource(source);
            }
        }

        if (processedSource != null) {
            BinaryBitmap bitmap = null;
            if ((start & 0x03) == 0) {  // mod 4: randomly try downscale image.
                Logging.d("randomly down scale image");
                LuminanceSource src = processedSource;
                mDecodeState.scaleFactor = 1.0f;
                for (int i = 0; i < 3; i++) {
                    src = new DownscaledLuminanceSource(src);
                    mDecodeState.scaleFactor = mDecodeState.scaleFactor * 0.5f;
                    bitmap = new BinaryBitmap(new StatefulBinarizer(src, mDecodeState));

                    try {
                        rawResult = multiFormatReader.decodeWithState(bitmap);
                        if (rawResult != null) break;
                    } catch (ReaderException re) {
                        // continue
                    } finally {
                        multiFormatReader.reset();
                    }
                }
            } else {
                bitmap = new BinaryBitmap(new StatefulBinarizer(processedSource, mDecodeState));
                mDecodeState.scaleFactor = 1.0f;
                try {
                    rawResult = multiFormatReader.decodeWithState(bitmap);
                } catch (ReaderException re) {
                    // continue
                } finally {
                    multiFormatReader.reset();
                }
            }
        }

        long now = System.currentTimeMillis();
        Logging.d("cost:" + (now - start) + "ms");

        Handler handler = activity.getHandler();
        if (rawResult != null) {
            // Don't log the barcode contents for security.
            long end = System.currentTimeMillis();
            Log.d(TAG, "Found barcode in " + (end - start) + " ms");
            if (handler != null) {
                Message message = Message.obtain(handler, R.id.decode_succeeded, rawResult);
                Bundle bundle = new Bundle();
                bundleThumbnail(source, bundle);
                message.setData(bundle);
                message.sendToTarget();
            }
        } else {
            if (handler != null) {
                Message message = Message.obtain(handler, R.id.decode_failed);
                message.sendToTarget();
            }
        }
    }

    private static void bundleThumbnail(PlanarYUVLuminanceSource source, Bundle bundle) {
        int[] pixels = source.renderThumbnail();
        int width = source.getThumbnailWidth();
        int height = source.getThumbnailHeight();
        Bitmap bitmap = Bitmap.createBitmap(pixels, 0, width, width, height, Bitmap.Config.ARGB_8888);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, out);
        bundle.putByteArray(DecodeThread.BARCODE_BITMAP, out.toByteArray());
        bundle.putFloat(DecodeThread.BARCODE_SCALED_FACTOR, (float) width / source.getWidth());
    }

}
