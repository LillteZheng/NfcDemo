package com.zhengsr.nfclib.delegate

import android.os.Parcelable

/**
 * @author by zhengshaorui 2020/8/27 10:38
 * describe：用来统一给外部的NDEF数据封装类
 */
class NdefData {
    companion object {
        const val TYPE_TEXT = 1
        const val TYPE_URI = 2
    }

    var recordType = TYPE_TEXT
    var msg: String? = null
    var data: ByteArray? = null
    override fun toString(): String {
        return "NdefData(recordType=$recordType, msg=$msg, data=${data?.contentToString()})"
    }


}