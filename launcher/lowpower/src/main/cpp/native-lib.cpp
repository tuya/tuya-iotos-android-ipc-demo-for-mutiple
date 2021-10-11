//
// Created by xushengju on 2021/10/8.
//
#include <jni.h>
#include <stdio.h>
#include <string.h>

JNIEXPORT jint JNI_OnLoad(JavaVM *vm, void *) {
    JNIEnv *env;
    if (vm->GetEnv((void **) &env, JNI_VERSION_1_6) != JNI_OK) {
        return JNI_FALSE;
    }
    return JNI_VERSION_1_6;
}