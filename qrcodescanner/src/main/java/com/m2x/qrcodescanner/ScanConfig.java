package com.m2x.qrcodescanner;

import com.google.zxing.BarcodeFormat;
import com.m2x.qrcodescanner.camera.FrontLightMode;

import java.util.Set;

/**
 * Created by mtj on 2017/12/7.
 */

public class ScanConfig {

    private static boolean mUseAutoFocus = true;
    private static boolean mDisableExposure = true;
    private static boolean mDisableContinuousFocus = true;
    private static boolean mDisableMetering = true;
    private static boolean mDisableBarcodeSceneMode = true;
    private static boolean mInvertScan = false;
    private static FrontLightMode mFrontLightMode = FrontLightMode.OFF;
    private static Set<BarcodeFormat> mSupportFormats = DecodeFormatManager.QR_CODE_FORMATS;

    private ScanConfig() {
    }

    public static void setUseAutoFocus(boolean use) {
        mUseAutoFocus = use;
    }

    public static boolean getUseAutoFocus() {
        return mUseAutoFocus;
    }

    public static boolean getDisableExposure() {
        return mDisableExposure;
    }

    public static boolean getDisableContinuousFocus() {
        return mDisableContinuousFocus;
    }

    public static boolean getDisableMetering() {
        return mDisableMetering;
    }

    public static boolean getDisableBarcodeSceneMode() {
        return mDisableBarcodeSceneMode;
    }

    public static boolean getInvertScan() {
        return mInvertScan;
    }

    public static FrontLightMode getFrontLightMode() {
        return mFrontLightMode;
    }

    public static Set<BarcodeFormat> getSupportFormats() {
        return mSupportFormats;
    }
}
