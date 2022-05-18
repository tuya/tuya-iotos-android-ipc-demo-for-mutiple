package com.tuya.lowpower

import android.util.Log

object LowpowerManager {
    init {
        System.loadLibrary("tuya_android")
    }

    interface LowpowerListener {
        fun wakeUp(index: Int)
        fun closeResult(index: Int, result: Int)
    }

    private var lowpowerListener: LowpowerListener? = null

    fun aliveInit(lowpowerListener: LowpowerListener): Int {
        this.lowpowerListener = lowpowerListener
        return aliveInit()
    }

    private fun wakeUp(index: Int) {
        lowpowerListener?.wakeUp(index)
    }

    private fun closeResult(index: Int, result: Int) {
        lowpowerListener?.closeResult(index, result)
    }

    private external fun aliveInit(): Int

//    /*
// *  tuya_ipc_lowpower_alive_add 添加包活设备。保证轻量，单线程操作。
// *  index:参考TUYA_LOWPOWER_ALIVE_CTX_S中index说明。
// *  serverIp：低功耗服务器IP
// *  port:包活端口
// *  pdevID：设备ID
// *  idLen:设备ID长度
// *  pkey：local key
// *  keyLen:local key len
// */
//    int tuya_ipc_lowpower_alive_add(int index,unsigned int serverIp,signed int port,char* pdevId, int idLen, char* pkey, int keyLen);

    external fun addDeviceHeart(
        index: Int, serverIp: Int, port: Int,
        pdevId: String, idLen: Int, pkey: String,
        keyLen: Int
    ): Int

    fun log(data: ByteArray) {
        Log.d("tuya_embedded", String(data))
    }
}