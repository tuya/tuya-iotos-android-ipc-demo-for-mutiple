//
// Created by xushengju on 2021/10/8.
//
#include <jni.h>
#include <stdio.h>
#include <string.h>
#include "tuya_lowpower_alive.h"
#include <arpa/inet.h>
#include "native_debug.h"

jclass lowpwerClass = nullptr;
jobject lowperObject = nullptr;
JavaVM *gJavaVm = nullptr;

class JNIEnvPtr {
public:
    JNIEnvPtr() {
        if (gJavaVm != nullptr) {
            if (gJavaVm->GetEnv((void **) &mEnv, JNI_VERSION_1_6) == JNI_EDETACHED) {
                int status = gJavaVm->AttachCurrentThread(&mEnv, nullptr);
                if (!status) {
                    mNeedDetach = true;
                } else {
//                    LOG("JavaVM AttachCurrentThread failed %d", status);
                }
            }
        }
    }

    ~JNIEnvPtr() {
        if (mNeedDetach) {
            gJavaVm->DetachCurrentThread();
            mNeedDetach = false;
        }
    }

    JNIEnv *operator->() {
        if (!mEnv) {
//            LOG("JNIEnv is nullptr");
        }
        return mEnv;
    }

    JNIEnv *getJNIEnv() {
        return mEnv;
    }

private:
    JNIEnv *mEnv = nullptr;
    bool mNeedDetach = false;
};

int data_callbcack_proc(int index) {
    LOGI("data_callbcack_proc index is %d", index);
    JNIEnvPtr envPtr;
    jmethodID wakeupId = envPtr->GetMethodID(lowpwerClass, "wakeUp", "(I)V");
    envPtr->CallVoidMethod(lowperObject, wakeupId, index);
    return 0;
}

int close_socket_callbcack_proc(int index, int result) {
    LOGI("close_socket_callbcack_proc index is %d, result is %d", index, result);
    JNIEnvPtr envPtr;
    jmethodID closeId = envPtr->GetMethodID(lowpwerClass, "closeResult", "(II)V");
    envPtr->CallVoidMethod(lowperObject, closeId, index, result);
    return 0;
}

extern "C"
JNIEXPORT jint  JNICALL
Java_com_tuya_lowpower_LowpowerManager_aliveInit(JNIEnv *env, jobject object) {
    lowpwerClass = static_cast<jclass>(env->NewGlobalRef(
            env->FindClass("com/tuya/lowpower/LowpowerManager")));
    lowperObject = env->NewGlobalRef(object);

    TUYA_LOWPOWER_ALIVE_CTX_S ctx = {0};
    ctx.alive_time_interval = 60;
    ctx.wakeup_callback = data_callbcack_proc;
    ctx.close_socket_callback = close_socket_callbcack_proc;
    int ret = tuya_ipc_lowpower_alive_init(&ctx);
    return ret;
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_tuya_lowpower_LowpowerManager_addDeviceHeart(JNIEnv *env, jobject object,
                                                      jint index, jint serverIp, jint port,
                                                      jstring pdevId, jint idLen, jstring pkey,
                                                      jint keyLen) {
    char *devId = const_cast<char *>(env->GetStringUTFChars(pdevId, nullptr));
    char *key = const_cast<char *>(env->GetStringUTFChars(pkey, nullptr));
    LOGI("addDeviceHeart index is %d, serverIp is %d, port is %d, pdevId is %s, idlen is %d, pkey is %s, keyLen is %d",
         index, serverIp, port, devId, idLen, key, keyLen);
    int ret = tuya_ipc_lowpower_alive_add(index, serverIp, port, devId, idLen, key, keyLen);
    env->ReleaseStringUTFChars(pdevId, devId);
    env->ReleaseStringUTFChars(pkey, key);
    return ret;
}

JNIEXPORT jint JNI_OnLoad(JavaVM *vm, void *) {
    JNIEnv *env;
    if (vm->GetEnv((void **) &env, JNI_VERSION_1_6) != JNI_OK) {
        return JNI_FALSE;
    }
    gJavaVm = vm;
    return JNI_VERSION_1_6;
}