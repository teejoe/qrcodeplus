/********************
* author: teejoe
* 2017-12-05
*/

#define MASK_SIZE 3
#define HALF_MASK_SIZE (MASK_SIZE >> 1)

void quick_sort(unsigned char* data, int start, int end);

void medianBlur(unsigned char* src, int width, int height)
{
    unsigned char* dst = malloc(sizeof(unsigned char) * width * height);

    int minX, minY, maxX, maxY;
    int maskArea;
    int temp;
    unsigned char* maskData = malloc(sizeof(unsigned char) * MASK_SIZE * MASK_SIZE);
    for (int y = 0; y < height; y++) {
        for (int x = 0; x < width; x++) {
            minX = x - HALF_MASK_SIZE;
            minY = y - HALF_MASK_SIZE;
            maxX = x + HALF_MASK_SIZE;
            maxY = y + HALF_MASK_SIZE;
            minX = (minX < 0)? 0: minX;
            minY = (minY < 0)? 0: minY;
            maxX = (maxX >= width)? (width - 1): maxX;
            maxY = (maxY >= height)? (height - 1): maxY;

            // get masked data.
            int index = 0;
            for (int i = minX; i <= maxX; i++) {
                for (int j = minY; j <= maxY; j++) {
                    maskData[index++] = src[j * width + i];
                }
            }

            // sort. (mask size is very small, bubble sort is good enough.)
            maskArea = (maxX - minX + 1) * (maxY - minY + 1);
            for (int i = maskArea - 1; i > 0; i--) {
                for (int j = 0; j < i; j++) {
                    if (maskData[j] > maskData[j + 1]) {
                        temp = maskData[j];
                        maskData[j] = maskData[j + 1];
                        maskData[j + 1] = temp;
                    }
                }
            }
            //quick_sort(maskData, 0, maskArea - 1);

            // get median value.
            dst[y * width + x] = maskData[maskArea >> 1];
        }
    }

    memcpy(src, dst, sizeof(unsigned char) * width * height);
    free(dst);
    free(maskData);
}

void quick_sort(unsigned char* list, int low, int high) {
    if (low >= high) return;

    int pivot = (low + high) / 2;

    // swap pivot and high.
    unsigned char temp = list[pivot];
    list[pivot] = list[high];
    list[high] = temp;

    int small = low - 1;
    for (int index = low; index < high; index++) {
        if (list[index] < list[high]) {
            small++;

            temp = list[small];
            list[small] = list[index];
            list[index] = temp;
        }
    }

    small++;
    temp = list[small];
    list[small] = list[high];
    list[high] = temp;

    quick_sort(list, low, small - 1);
    quick_sort(list, small + 1, high);
}