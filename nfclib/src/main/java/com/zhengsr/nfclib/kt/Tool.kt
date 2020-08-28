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
private val HEX_CHARS = "0123456789ABCDEF"
fun hexStringToByteArray(data: String) : ByteArray {

    val result = ByteArray(data.length / 2)

    for (i in 0 until data.length step 2) {
        val firstIndex = HEX_CHARS.indexOf(data[i]);
        val secondIndex = HEX_CHARS.indexOf(data[i + 1]);

        val octet = firstIndex.shl(4).or(secondIndex)
        result.set(i.shr(1), octet.toByte())
    }

    return result
}