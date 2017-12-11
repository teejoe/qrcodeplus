#ifndef HYBRID_BYNARIZER_H
#define HYBRID_BYNARIZER_H

int** calculateBlackPoints(unsigned char* luminances, int subWidth, int subHeight, int width, int height);

void calculateThresholdForBlock(unsigned char* luminances, int subWidth, int subHeight, int width, int height, float scaleFactor, int* matrix);

#endif