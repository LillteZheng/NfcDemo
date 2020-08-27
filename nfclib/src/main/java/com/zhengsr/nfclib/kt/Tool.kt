package com.zhengsr.nfclib.kt

/**
 * @author by zhengshaorui 2020/8/27 10:31
 * describe：工具类，都用顶层函数
 */

internal fun bytesToHexString(src: ByteArray?): String? {
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