//
// Created by 张树杰 on 2018/7/17.
//

#ifndef TYFDCLIENT_NATIVE_DEBUG_H
#define TYFDCLIENT_NATIVE_DEBUG_H

#include <android/log.h>


#define LOG_TAG "Tuya-GW-IoT"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGW(...) __android_log_print(ANDROID_LOG_WARN, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)


#define ASSERT(cond, fmt, ...)                                \
  if (!(cond)) {                                              \
    __android_log_assert(#cond, LOG_TAG, fmt, ##__VA_ARGS__); \
  }

/**
 * [ADD by zhangshujie] 2018/6/15
 * */
/* 普通的调试打印*/
//#define TYLOGV(fmt, arg...) __android_log_print(ANDROID_LOG_VERBOSE, LOG_TAG, fmt, ##arg)
#define TYLOGI(fmt, arg...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, fmt, ##arg)
//#define TYLOGD(fmt, arg...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, fmt, ##arg)
//#define TYLOGW(fmt, arg...) __android_log_print(ANDROID_LOG_WARN, LOG_TAG, fmt, ##arg)
//#define TYLOGE(fmt, arg...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, fmt, ##arg)

/**带函数名称和行号的打印*/
//#define TYPOSLOGV(fmt, arg...) TYLOGV("[%s:%d]" fmt, __FUNCTION__, __LINE__, ##arg)
#define TYPOSLOGI(fmt, arg...) TYLOGI("[%s:%d]" fmt, __FUNCTION__, __LINE__, ##arg)
//#define TYPOSLOGD(fmt, arg...) TYLOGD("[%s:%d]" fmt, __FUNCTION__, __LINE__, ##arg)
//#define TYPOSLOGW(fmt, arg...) TYLOGW("[%s:%d]" fmt, __FUNCTION__, __LINE__, ##arg)
//#define TYPOSLOGE(fmt, arg...) TYLOGE("[%s:%d]" fmt, __FUNCTION__, __LINE__, ##arg)

#if 0
#define TYPOSLOGV(fmt, arg...)
#define TYPOSLOGI(fmt, arg...)
#define TYPOSLOGD(fmt, arg...)
#define TYPOSLOGW(fmt, arg...)
#define TYPOSLOGE(fmt, arg...)
#endif



/** 函数调试打印*/
#define TYFUNCINV(fmt, arg...) TYLOGV("{[file:%s--line:%d]}" fmt, __FILE__, __LINE__, __FUNCTION__, ##arg)
#define TYFUNCOUTV(fmt, arg...) TYLOGV("{[file:%s--line:%d]}" fmt, __FILE__, __LINE__, __FUNCTION__, ##arg)
#define TYFUNCIND(fmt, arg...) TYLOGD("{[file:%s--line:%d]}" fmt, __FILE__, __LINE__, __FUNCTION__, ##arg)
#define TYFUNCOUTD(fmt, arg...) TYLOGD("{[file:%s--line:%d]}" fmt, __FILE__, __LINE__, __FUNCTION__, ##arg)


#endif //TYFDCLIENT_NATIVE_DEBUG_H
