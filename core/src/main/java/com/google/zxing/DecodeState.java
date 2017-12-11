package com.google.zxing;

/**
 * Created by mtj on 2017/11/14.
 */

public class DecodeState {
    public enum FinderPatternAlgorithm {
        REGULAR,
        WEAK,
        WEAK2
    }

    public enum BinarizerAlgorithm {
        GLOBAL_HISTOGRAM,
        HYBRID,
        ADJUSTED_HYBRID
    }

    public static class FinderPatternHint {
        public boolean notEnough;           // found less than 3 finder patterns.
        public boolean tooMany;             // found more than MAX_CANDIDATES finder patterns.
        public float sensitivityIncrease; // [0, 1.0)  factor to increase finder pattern sensitive.

        public void clear() {
            notEnough = false;
            tooMany = false;
            sensitivityIncrease = 0.0f;
        }
    }

    public static class FailureHint {
        public boolean lowContrastImage;                // need to increase image contrast.
        public boolean finderPatternIncredible;         // may found wrong finder patterns.
        public boolean dimensionIncredible;             // dimension may be wrong.

        public FinderPatternHint finderPatternFinderHint = new FinderPatternHint();
        public FinderPatternHint weakFinderPatternFinderHint = new FinderPatternHint();
        public FinderPatternHint weakFinderPatternFinder2Hint = new FinderPatternHint();
        public FinderPatternAlgorithm finderPatternAlgorithm = FinderPatternAlgorithm.REGULAR;
        public BinarizerAlgorithm binarizerAlgorithm = BinarizerAlgorithm.GLOBAL_HISTOGRAM;

        public void clear() {
            lowContrastImage = false;
            finderPatternIncredible = false;
            dimensionIncredible = false;
            finderPatternFinderHint.clear();
            weakFinderPatternFinderHint.clear();
            weakFinderPatternFinder2Hint.clear();
            finderPatternAlgorithm = FinderPatternAlgorithm.REGULAR;
            binarizerAlgorithm = BinarizerAlgorithm.GLOBAL_HISTOGRAM;
        }
    }

    public static class SpecifiedParams {
        public FinderPatternAlgorithm finderPatternAlgorithm;
        public float finderPatternSensitivity;
    }

    public int currentRound;
    public float scaleFactor = 1.0f;
    public long startTime;
    public FailureHint previousFailureHint = new FailureHint();
    public SpecifiedParams specifiedParams;

    public void reset() {
        currentRound = 0;
        scaleFactor = 1.0f;
        previousFailureHint.clear();
        specifiedParams = null;
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("round:").append(currentRound)
                .append("\nscaleFactor:").append(scaleFactor)
                .append("\nbinarizer:").append(previousFailureHint.binarizerAlgorithm.name())
                .append("\nfinder pattern finder:")
                .append(previousFailureHint.finderPatternAlgorithm.name())
                .append("\nfinder pattern sensitivity:")
                .append(previousFailureHint.finderPatternFinderHint.sensitivityIncrease)
                .append("(regular), ")
                .append(previousFailureHint.weakFinderPatternFinderHint.sensitivityIncrease)
                .append("(weak), ")
                .append(previousFailureHint.weakFinderPatternFinder2Hint.sensitivityIncrease)
                .append("(weak2)");
        return builder.toString();
    }
}
