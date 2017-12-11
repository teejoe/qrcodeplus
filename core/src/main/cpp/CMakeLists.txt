cmake_minimum_required(VERSION 3.4.1)

set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -Wall")

add_library(qrcodeplus SHARED
            qrcodeplus.c
            hybridbinarizer.c
            globalhistogrambinarizer.c
            denoise.c)

# Include libraries needed for qrcodeplus lib
target_link_libraries(qrcodeplus
                      android
                      jnigraphics
                      log
                      m)
