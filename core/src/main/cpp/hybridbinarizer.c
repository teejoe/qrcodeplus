#define BLOCK_SIZE_POWER 3
#define BLOCK_SIZE (1 << BLOCK_SIZE_POWER) // ...0100...00
#define BLOCK_SIZE_MASK (BLOCK_SIZE - 1)   // ...0011...11
#define MINIMUM_DIMENSION (BLOCK_SIZE * 5)
#define MIN_DYNAMIC_RANGE 24

int** calculateBlackPoints(
        unsigned char* luminances,
        int subWidth,
        int subHeight,
        int width,
        int height) {

    int maxYOffset = height - BLOCK_SIZE;
    int maxXOffset = width - BLOCK_SIZE;

    int** blackPoints = malloc(subHeight * sizeof(int*));
    for (int i = 0; i < subHeight; i++) {
        blackPoints[i] = malloc(subWidth * sizeof(int));
    }

    for (int y = 0; y < subHeight; y++) {
        int yoffset = y << BLOCK_SIZE_POWER;
        if (yoffset > maxYOffset) {
            yoffset = maxYOffset;
        }
        for (int x = 0; x < subWidth; x++) {
            int xoffset = x << BLOCK_SIZE_POWER;
            if (xoffset > maxXOffset) {
                xoffset = maxXOffset;
            }
            int sum = 0;
            int min = 0xFF;
            int max = 0;
            for (int yy = 0, offset = yoffset * width + xoffset; yy < BLOCK_SIZE; yy++, offset += width) {
                for (int xx = 0; xx < BLOCK_SIZE; xx++) {
                    int pixel = luminances[offset + xx] & 0xFF;
                    sum += pixel;
                    // still looking for good contrast
                    if (pixel < min) {
                        min = pixel;
                    }
                    if (pixel > max) {
                        max = pixel;
                    }
                }
                // short-circuit min/max tests once dynamic range is met
                if (max - min > MIN_DYNAMIC_RANGE) {
                    // finish the rest of the rows quickly
                    for (yy++, offset += width; yy < BLOCK_SIZE; yy++, offset += width) {
                        for (int xx = 0; xx < BLOCK_SIZE; xx++) {
                            sum += luminances[offset + xx] & 0xFF;
                        }
                    }
                }
            }

            // The default estimate is the average of the values in the block.
            int average = sum >> (BLOCK_SIZE_POWER * 2);
            if (max - min <= MIN_DYNAMIC_RANGE) {
                // If variation within the block is low, assume this is a block with only light or only
                // dark pixels. In that case we do not want to use the average, as it would divide this
                // low contrast area into black and white pixels, essentially creating data out of noise.
                //
                // The default assumption is that the block is light/background. Since no estimate for
                // the level of dark pixels exists locally, use half the min for the block.
                average = min / 2;

                if (y > 0 && x > 0) {
                    // Correct the "white background" assumption for blocks that have neighbors by comparing
                    // the pixels in this block to the previously calculated black points. This is based on
                    // the fact that dark barcode symbology is always surrounded by some amount of light
                    // background for which reasonable black point estimates were made. The bp estimated at
                    // the boundaries is used for the interior.

                    // The (min < bp) is arbitrary but works better than other heuristics that were tried.
                    int averageNeighborBlackPoint =
                            (blackPoints[y - 1][x] + (2 * blackPoints[y][x - 1]) + blackPoints[y - 1][x - 1]) / 4;
                    if (min < averageNeighborBlackPoint) {
                        average = averageNeighborBlackPoint;
                    }
                }
            }
            blackPoints[y][x] = average;
        }
    }
    return blackPoints;
}

void thresholdBlock(unsigned char* luminances,
                    int xoffset,
                    int yoffset,
                    int threshold,
                    int stride,
                    int* matrix) {

    int rowSize = (stride + 31) / 32;
    for (int y = 0, offset = yoffset * stride + xoffset; y < BLOCK_SIZE; y++, offset += stride) {
        for (int x = 0; x < BLOCK_SIZE; x++) {
            // Comparison needs to be <= so that black == 0 pixels are black even if the threshold is 0.
            if ((luminances[offset + x] & 0xFF) <= threshold) {
                //matrix.set(xoffset + x, yoffset + y);
                int offset = (yoffset + y) * rowSize + ((xoffset + x) / 32);
                matrix[offset] |= 1 << ((xoffset + x) & 0x1f);
            }
        }
    }
}

int cap(int value, int min, int max) {
    return value < min ? min : value > max ? max : value;
}

void calculateThresholdForBlock(unsigned char* luminances,
                                int subWidth,
                                int subHeight,
                                int width,
                                int height,
                                float scaleFactor,
                                int* matrix) {

    int** blackPoints = calculateBlackPoints(luminances, subWidth, subHeight, width, height);

    int maxYOffset = height - BLOCK_SIZE;
    int maxXOffset = width - BLOCK_SIZE;
    for (int y = 0; y < subHeight; y++) {
        int yoffset = y << BLOCK_SIZE_POWER;
        if (yoffset > maxYOffset) {
            yoffset = maxYOffset;
        }
        int top = cap(y, 2, subHeight - 3);
        for (int x = 0; x < subWidth; x++) {
            int xoffset = x << BLOCK_SIZE_POWER;
            if (xoffset > maxXOffset) {
                xoffset = maxXOffset;
            }
            int left = cap(x, 2, subWidth - 3);
            int sum = 0;
            for (int z = -2; z <= 2; z++) {
                int* blackRow = blackPoints[top + z];
                sum += blackRow[left - 2] + blackRow[left - 1] + blackRow[left] + blackRow[left + 1] + blackRow[left + 2];
            }
            int average = sum / 25;
            thresholdBlock(luminances, xoffset, yoffset, (int) (scaleFactor * average), width, matrix);
        }
    }

    for (int i = 0; i < subHeight; i++) {
        free(blackPoints[i]);
    }
    free(blackPoints);
}