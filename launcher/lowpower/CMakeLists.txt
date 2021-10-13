cmake_minimum_required(VERSION 3.4.1)

include_directories(${CMAKE_CURRENT_SOURCE_DIR}/src/main/cpp)

add_library(
        tuya_android
        SHARED
        ${CMAKE_CURRENT_SOURCE_DIR}/src/main/cpp/native-lib.cpp
)

find_library( # Sets the name of the path variable.
        log-lib
        log
        )

find_library( # Sets the name of the path variable.
        android-lib
        android
        )

add_library(tuya_iot SHARED IMPORTED)
set_target_properties(tuya_iot PROPERTIES IMPORTED_LOCATION
        ${CMAKE_CURRENT_SOURCE_DIR}/libs/${CMAKE_ANDROID_ARCH_ABI}/libtuya_iot.a)

target_link_libraries(
        tuya_android
        tuya_iot
        ${log-lib}
        ${android-lib}
)