/********************
* author: teejoe
* 2017-12-04
*/
#include <stdlib.h>


#define LUMINANCE_BITS  5
#define LUMINANCE_SHIFT (8 - LUMINANCE_BITS)
#define LUMINANCE_BUCKETS (1 << LUMINANCE_BITS)

int estimateBlackPoint(int* buckets, int length)
{
    // Find the tallest peak in the histogram.
    int numBuckets = length;
    int maxBucketCount = 0;
    int firstPeak = 0;
    int firstPeakSize = 0;
    for (int x = 0; x < numBuckets; x++) {
        if (buckets[x] > firstPeakSize) {
            firstPeak = x;
            firstPeakSize = buckets[x];
        }
        if (buckets[x] > maxBucketCount) {
            maxBucketCount = buckets[x];
        }
    }

    // Find the second-tallest peak which is somewhat far from the tallest peak.
    int secondPeak = 0;
    int secondPeakScore = 0;
    for (int x = 0; x < numBuckets; x++) {
        int distanceToBiggest = x - firstPeak;
        // Encourage more distant second peaks by multiplying by square of distance.
        int score = buckets[x] * distanceToBiggest * distanceToBiggest;
        if (score > secondPeakScore) {
            secondPeak = x;
            secondPeakScore = score;
        }
    }

    // Make sure firstPeak corresponds to the black peak.
    if (firstPeak > secondPeak) {
        int temp = firstPeak;
        firstPeak = secondPeak;
        secondPeak = temp;
    }

    // If there is too little contrast in the image to pick a meaningful black point, throw rather
    // than waste time trying to decode the image, and risk false positives.
    if (secondPeak - firstPeak <= numBuckets / 16) {
        //throw LowContrastException.getLowContrastInstance();
    }

    // Find a valley between them that is low and closer to the white peak.
    int bestValley = secondPeak - 1;
    int bestValleyScore = -1;
    for (int x = secondPeak - 1; x > firstPeak; x--) {
        int fromFirst = x - firstPeak;
        int score = fromFirst * fromFirst * (secondPeak - x) * (maxBucketCount - buckets[x]);
        if (score > bestValleyScore) {
            bestValley = x;
            bestValleyScore = score;
        }
    }

    return bestValley << LUMINANCE_SHIFT;
}

void globalHistogramThreshold(unsigned char* luminance, int width, int height, int* bitMatrix)
{
    int* buckets = malloc(sizeof(int) * LUMINANCE_BUCKETS);
    memset(buckets, 0, sizeof(int) * LUMINANCE_BUCKETS);

    // Quickly calculates the histogram by sampling four rows from the image. This proved to be
    // more robust on the blackbox tests than sampling a diagonal as we used to do.
    for (int y = 1; y < 5; y++) {
        int row = height * y / 5;
        //byte[] localLuminances = source.getRow(row, luminances);
        int right = (width * 4) / 5;
        int offset = row * width;
        for (int x = width / 5; x < right; x++) {
            int pixel = luminance[offset + x] & 0xff;
            buckets[pixel >> LUMINANCE_SHIFT]++;
        }
    }
    int blackPoint = estimateBlackPoint(buckets, LUMINANCE_BUCKETS);

    // We delay reading the entire image luminance until the black point estimation succeeds.
    // Although we end up reading four rows twice, it is consistent with our motto of
    // "fail quickly" which is necessary for continuous scanning.
    int rowSize = (width + 31) / 32;
    for (int y = 0; y < height; y++) {
        int offset = y * width;
        for (int x = 0; x < width; x++) {
            int pixel = luminance[offset + x] & 0xff;
            if (pixel < blackPoint) {
                //matrix.set(x, y);
                bitMatrix[y * rowSize + (x / 32)] |= 1 << (x & 0x1f);
            }
        }
    }

    free(buckets);
}

