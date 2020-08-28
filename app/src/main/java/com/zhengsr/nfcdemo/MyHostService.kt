package com.zhengsr.nfcdemo

import android.nfc.cardemulation.HostApduService
import android.os.Bundle
import android.util.Log

/**
 * 需要系统应用，没签名，没啥实践了
 */
class MyHostService : HostApduService() {

    private  val TAG = "MyHostService"

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "zsr onCreate: ")
    }

    // 正确信号

    // 错误信号
    private val UNKNOWN_ERROR: ByteArray = "0000".toByteArray()
    /**
     * 通常，NFC 读取器向您的设备发送的第一个 APDU 是“SELECT AID”APDU；此 APDU 包含读取器要与之通信的 AID
     */
    override fun processCommandApdu(commandApdu: ByteArray?, extras: Bundle?): ByteArray {
        //执行程序唉祝线程
            //使用sendResponseApdu()发送响应
        //合理的数据上限约为1kb，通过可以在300ms内交换成功

        Log.d(TAG, "zsr processCommandApdu:$extras $commandApdu ")
        return UNKNOWN_ERROR
    }
    override fun onDeactivated(reason: Int) {
        //使用了则会调用这个
        Log.d(TAG, "zsr onDeactivated: $reason")
    }


}
