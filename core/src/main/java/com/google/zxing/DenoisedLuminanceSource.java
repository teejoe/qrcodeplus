package com.google.zxing;

import com.google.zxing.qrcode.DecodeConfigs;

/**
 * Represent a luminance source witch increases contrast of the original one.
 * Created by mtj on 2017/11/6.
 */

public class DenoisedLuminanceSource extends LuminanceSource {

    private final byte[] data;

    public DenoisedLuminanceSource(LuminanceSource source) {
        super(source.getWidth(), source.getHeight());
        data = source.getMatrix();
        if (DecodeConfigs.USE_NATIVE_METHODS) {
            NativeLib.medianBlur(data, source.getWidth(), source.getHeight());
        } else {
            denoise(data, source.getWidth(), source.getHeight());
        }
    }

    private void denoise(byte[] data, int width, int height) {
        NativeLib.medianBlur(data, width, height);
    }

    @Override
    public byte[] getRow(int y, byte[] row) {
        if (y < 0 || y >= getHeight()) {
            throw new IllegalArgumentException("Requested row is outside the image: " + y);
        }
        int width = getWidth();
        if (row == null || row.length < width) {
            row = new byte[width];
        }
        int offset = y * width;
        System.arraycopy(data, offset, row, 0, width);
        return row;
    }

    @Override
    public byte[] getMatrix() {
        int area = getHeight() * getWidth();
        byte[] matrix = new byte[area];
        System.arraycopy(data, 0, matrix, 0, area);
        return matrix;
    }

    @Override
    public boolean isCropSupported() {
        return false;
    }
}
