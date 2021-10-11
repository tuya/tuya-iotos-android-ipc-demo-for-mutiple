package com.tuya.myapplication

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.gson.Gson

/**
 * created by xsj
 **/
object HeartManager {
    private val TAG = "HeartManager"
    fun addHeart(heartBean: HeartBean) {
        Log.d("xsj", "heartbean is ${ Gson().toJson(heartBean)}")
        //增加心跳连接

    }

    //收到唤醒回调得移除当前的心跳,若ipc进程有存在需要kill掉
    fun wakeUpCallback(context: Context){
        Log.d("xsj", "wakeUpCallback broad cast send")
        val intent = Intent("ipc.action.boot")
        intent.component =
            ComponentName("com.tuya.ai.ipcsdkdemo", "com.tuya.ai.ipcsdkdemo.BootBroadCastRevicer")
        context.sendBroadcast(intent)
    }
}