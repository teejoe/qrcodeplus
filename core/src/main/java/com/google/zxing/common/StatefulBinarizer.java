package com.google.zxing.common;

import com.google.zxing.Binarizer;
import com.google.zxing.DecodeState;
import com.google.zxing.DecodeState.BinarizerAlgorithm;
import com.google.zxing.Logging;
import com.google.zxing.LuminanceSource;
import com.google.zxing.NotFoundException;

import java.util.Random;

/**
 * Created by mtj on 2017/11/2.
 */

public class StatefulBinarizer extends GlobalHistogramBinarizer {

    private static float[] CANDIDATES = {
            -1.0f, -1.0f -1.0f, -1.0f, -1.0f,  // < 0 : global histogram binarizer
            0.0f, 0.0f, 0.0f, 0.0f, 0.0f,       // == 0 : hybrid binarizer
            1.02f, 1.04f, 1.06f, 1.08f, 1.1f    // > 0 : Adjusted hybrid binarizer with scale factor K.
    };

    private BitMatrix mMatrix;
    private DecodeState mDecodeState;

    public StatefulBinarizer(LuminanceSource source, DecodeState decodeState) {
        super(source);
        mDecodeState = decodeState;
    }

    @Override
    public BitMatrix getBlackMatrix() throws NotFoundException {
        if (mMatrix != null) {
            return mMatrix;
        }

        int rand = new Random().nextInt(CANDIDATES.length);
        float factor = CANDIDATES[rand];
        if (factor < 0.0f) {
            mMatrix =  super.getBlackMatrix();      // Global histogram binarizer.
            mDecodeState.previousFailureHint.binarizerAlgorithm = BinarizerAlgorithm.GLOBAL_HISTOGRAM;
            Logging.d("use global histogram binarizer");
        } else if (factor < 0.00001f && factor > -0.00001f) {
            HybridBinarizer binarizer = new HybridBinarizer(getLuminanceSource());
            mMatrix = binarizer.getBlackMatrix();
            mDecodeState.previousFailureHint.binarizerAlgorithm = BinarizerAlgorithm.HYBRID;
            Logging.d("use hybrid");
        } else {
            AdjustedHybridBinarizer binarizer = new AdjustedHybridBinarizer(getLuminanceSource(), factor);
            mDecodeState.previousFailureHint.binarizerAlgorithm = BinarizerAlgorithm.ADJUSTED_HYBRID;
            mMatrix = binarizer.getBlackMatrix();
            Logging.d("use adjust hybrid:" + factor);
        }

        return mMatrix;
    }

    @Override
    public Binarizer createBinarizer(LuminanceSource source) {
        return new StatefulBinarizer(source, new DecodeState());
    }
}
