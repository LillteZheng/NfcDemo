package com.zhengsr.nfclib

import android.nfc.Tag

/**
 * @author by zhengshaorui 2020/8/26 19:54
 * describe：
 */
internal object NfcConverter{



    fun getCardId(tag: Tag?):String?{
        return tag?.let {
            bytesToHexString(it.id)
        }
    }


    /**
     * byte to hex
     */
    private fun bytesToHexString(src: ByteArray?): String? {
        val stringBuilder = StringBuilder("")
        if (src == null || src.size <= 0) {
            return null
        }
        for (element in src) {
            val v = element.toInt() and 0xFF
            val hv = Integer.toHexString(v)
            if (hv.length < 2) {
                stringBuilder.append(0)
            }
            stringBuilder.append(hv)
        }
        return stringBuilder.toString()
    }

    fun read
}