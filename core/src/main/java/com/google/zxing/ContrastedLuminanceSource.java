package com.google.zxing;

import com.google.zxing.qrcode.DecodeConfigs;

/**
 * Represent a luminance source witch increases contrast of the original one.
 * Created by mtj on 2017/11/6.
 */

public class ContrastedLuminanceSource extends LuminanceSource {

    private final byte[] data;

    public ContrastedLuminanceSource(LuminanceSource source) {
        super(source.getWidth(), source.getHeight());
        data = source.getMatrix();
        if (DecodeConfigs.USE_NATIVE_METHODS) {
            NativeLib.increaseContrast(data, getWidth(), getHeight());
        } else {
            increaseContrast();
        }
    }

    private void increaseContrast() {
        int width = getWidth();
        int height = getHeight();
        int anzpixel= width * height;
        int[] histogram = new int[256];

        //read pixel intensities into histogram
        for (int x = 1; x < width; x++) {
            for (int y = 1; y < height; y++) {
                int valueBefore = data[y * width + x] & 0xff;
                histogram[valueBefore]++;
            }
        }

        int sum =0;
        // build a Lookup table LUT containing scale factor
        float[] lut = new float[256];
        for (int i = 0; i < 255; i++) {
            sum += histogram[i];
            lut[i] = sum * 255 / anzpixel;
        }

        // transform image using sum histogram as a Lookup table
        for (int x = 1; x < width; x++) {
            for (int y = 1; y < height; y++) {
                byte valueBefore = data[y * width + x];
                byte valueAfter= (byte) lut[valueBefore & 0xff];
                data[y * width + x] = valueAfter;
            }
        }
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
