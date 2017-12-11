package com.google.zxing;

/**
 * Created by mtj on 2017/12/1.
 */

public class NativeLib {
    static {
        System.loadLibrary("qrcodeplus");
    }

    public static native byte[] downscaleByHalf(byte[] luminance, int width, int height);

    public static native void increaseContrast(byte[] luminance, int width, int height);

    // for hybrid binarizer
    public static native int[][] calculateBlackPoints(byte[] luminance,
                                                      int subWidth,
                                                      int subHeight,
                                                      int width,
                                                      int height);
    public static native void calculateThresholdForBlock(byte[] luminance,
                                                         int subWidth,
                                                         int subHeight,
                                                         int width,
                                                         int height,
                                                         float scaleFactor,
                                                         int[] matrix);

    // for global histogram binarizer.
    public static native void globalHistogramThreshold(byte[] luminance,
                                                       int width,
                                                       int height,
                                                       int[] matrix);

    // denoise
    public static native void medianBlur(byte[] luminance, int width, int height);
}
