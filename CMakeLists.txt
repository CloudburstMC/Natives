cmake_minimum_required(VERSION 2.8.4)

if (POLICY CMP0048)
    cmake_policy(SET CMP0048 OLD)
endif (POLICY CMP0048)

project(cloudburst-natives C CXX)

set(CMAKE_BUILD_TYPE Release)
set(SRC_DIR src/main/c)

find_package(JNI REQUIRED)

include_directories(${JNI_INCLUDE_DIRS})
include_directories(target/libdeflate/include)

add_library(cloudburst-natives SHARED ${MBED_SOURCE} src/main/c/zlib_processor.h src/main/c/zlib_processor.c)
