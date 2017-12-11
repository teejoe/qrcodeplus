package com.google.zxing;

/**
 * Created by mtj on 2017/11/9.
 */

public interface FailHintCallback {
    void onLowContrast();

    void onNoPossibleFinderPattern();

    void onNoCredibleFinderPattern();
}
