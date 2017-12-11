package com.google.zxing;

import com.google.zxing.qrcode.DecodeConfigs;

/**
 * Represent a downscaled luminance source with half width and half height of the original one.
 * Created by mtj on 2017/11/6.
 */

public class DownscaledLuminanceSource extends LuminanceSource {

    private LuminanceSource mSource;

    public DownscaledLuminanceSource(LuminanceSource source) {
        super(source.getWidth() >> 1, source.getHeight() >> 1);
        mSource = source;
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

        byte[] sourceRow = mSource.getRow(y, null);
        for (int x = 0; x < width; x++) {
            row[x] = (byte) ((sourceRow[2 * x] >> 1) + (sourceRow[2 * x + 1] >> 1));
        }

        return row;
    }

    @Override
    public byte[] getMatrix() {
        if (DecodeConfigs.USE_NATIVE_METHODS) {
            return NativeLib.downscaleByHalf(mSource.getMatrix(), mSource.getWidth(), mSource.getHeight());
        }

        int width = getWidth();
        int height = getHeight();
        int sourceWidth = mSource.getWidth();
        byte[] matrix = new byte[width * height];
        byte[] sourceMatrix = mSource.getMatrix();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                matrix[y * width + x] = (byte) ((sourceMatrix[(y * sourceWidth << 1) + (x << 1)]
                                        + sourceMatrix[((y << 1) + 1) * sourceWidth + (x << 1)]
                                        + sourceMatrix[((y << 1) + 1) * sourceWidth + (x << 1) + 1]
                                        + sourceMatrix[(y * sourceWidth << 1) + (x << 1) + 1]) >> 2);
            }
        }

        return matrix;
    }

    @Override
    public boolean isCropSupported() {
        return false;
    }
}
