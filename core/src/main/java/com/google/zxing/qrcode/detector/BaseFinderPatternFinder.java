package com.google.zxing.qrcode.detector;

import com.google.zxing.DecodeHintType;
import com.google.zxing.DecodeState;
import com.google.zxing.DecodeState.FinderPatternHint;
import com.google.zxing.Logging;
import com.google.zxing.NotFoundException;
import com.google.zxing.common.BitMatrix;

import java.util.List;
import java.util.Map;

/**
 * Created by mtj on 2017/11/9.
 */

public abstract class BaseFinderPatternFinder {
    public static final int MAX_CANDIDATES = 6;

    protected static final int CENTER_QUORUM = 2;
    protected final BitMatrix image;
    protected DecodeState decodeState;
    protected float sensitivityIncrease = 0.0f;

    public BaseFinderPatternFinder(BitMatrix image) {
        this.image = image;
    }

    protected void setFinderHint(FinderPatternHint hint) {
        if (hint.notEnough) {
            setSensitivityIncrease(hint.sensitivityIncrease + 0.1f);
        }
        if (hint.tooMany) {
            setSensitivityIncrease(hint.sensitivityIncrease - 0.1f);
        }
        hint.sensitivityIncrease = sensitivityIncrease;
        Logging.d("current sensitivity:" + sensitivityIncrease);
    }

    protected void setSensitivityIncrease(float factor) {
        if (factor < 0.0f) {
            factor = 0.0f;
        } else if (factor > 0.99f) {
            factor = 0.99f;
        }
        sensitivityIncrease = factor;
    }

    abstract FinderPatternInfo find(Map<DecodeHintType, ?> hints) throws NotFoundException;

    protected boolean hasTwoCrediblePatterns(List<FinderPattern> possibleCenters) {
        if (possibleCenters.size() !=2 ) {
            return false;
        }

        FinderPattern first = possibleCenters.get(0);
        FinderPattern second = possibleCenters.get(1);

        if (first.getCount() < CENTER_QUORUM || second.getCount() < CENTER_QUORUM) {
            return false;
        }

        if (Math.abs(first.getEstimatedModuleSize() - second.getEstimatedModuleSize()) / first.getEstimatedModuleSize() > 0.1f) {
            return false;
        }

        Logging.d("found two credible finder patterns: (" + first.getX() + "," + first.getY() + ")" +
                " (" + second.getX() + "," + second.getY() + ")" + " module size:" +
                (first.getEstimatedModuleSize() + second.getEstimatedModuleSize()) / 2.0f);
        return true;
    }

    protected FinderPattern guessThirdPattern(FinderPattern first, FinderPattern second) {
        if (first.getY() > second.getY()) {
            FinderPattern temp = first;
            first = second;
            second = temp;
        }

        float dy = second.getY() - first.getY();
        float dx = Math.abs(first.getX() - second.getX());
        float x, y;

        if (Math.abs(dx - dy) / (dy + dx) < 0.4f) {
            // the two finder are diagonal.
            x = (first.getX() + second.getX()) / 2.0f - (second.getY() - first.getY()) / 2.0f;
            y = (first.getY() + second.getY()) / 2.0f - (first.getX() - second.getX()) / 2.0f;
            if (x < 0 || y < 0 || x >= image.getWidth() || y >= image.getHeight()) {
                x = (first.getX() + second.getX()) / 2.0f - (second.getY() - first.getY()) / 2.0f;
                y = (first.getY() + second.getY()) / 2.0f - (first.getX() - second.getX()) / 2.0f;
            } else if ((System.currentTimeMillis() & 0x01) == 0) {
                x = (first.getX() + second.getX()) / 2.0f + (second.getY() - first.getY()) / 2.0f;
                y = (first.getY() + second.getY()) / 2.0f + (first.getX() - second.getX()) / 2.0f;
            }
            
            if (x < 0 || y < 0 || x >= image.getWidth() || y >= image.getHeight()) {
                return null;
            }
        } else {
            // the two finder are in same edge.
            x = second.getX() + second.getY() - first.getY();
            y = second.getY() - second.getX() + first.getX();
            if (x < 0 || y < 0 || x >= image.getWidth() || y >= image.getHeight()) {
                x = first.getX() + second.getY() - first.getY();
                y = first.getY() - second.getX() + first.getX();
            } else if ((System.currentTimeMillis() & 0x01) == 0) {
                x = first.getX() + second.getY() - first.getY();
                y = first.getY() - second.getX() + first.getX();
            }

            if (x < 0 || y < 0 || x >= image.getWidth() || y >= image.getHeight()) {
                return null;
            }
        }

        float moduleSize = (first.getEstimatedModuleSize() + second.getEstimatedModuleSize()) / 2.0f;
        return new FinderPattern(x, y, moduleSize);
    }
}
