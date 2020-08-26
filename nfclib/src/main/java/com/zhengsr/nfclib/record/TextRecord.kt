package com.zhengsr.nfclib.record

import android.icu.text.AlphabeticIndex
import android.nfc.NdefRecord
import android.nfc.Tag
import android.nfc.tech.Ndef
import android.util.Log
import com.zhengsr.nfclib.NfcListener
import java.nio.charset.Charset
import java.sql.Array
import java.util.*

/**
 * @author by zhengshaorui 2020/8/26 20:19
 * describe：
 */
internal class TextRecord {

    fun getRecordMsg(tag: Tag, block: NfcListener) {
        val ndef = Ndef.get(tag)
        try {
            ndef.connect()
            val msg = ndef.ndefMessage
            if (msg == null) {
                block("")
            } else {
                msg.records ?: return

                val record = msg.records[0]
                val payload = record.payload

                //判断语言编码，第7位0位 utf-8，1位utf-16
                val textEncoding = if ((payload[0].toInt() and 0x80) == 0) "UTF-8" else "Utf-16"
                //语言编码的长度,0-5bit
                val languageLength = payload[0].toInt() and 0x3f

                //拿到当前语言
                // val language = String(payload, 1, languageLength, Charset.forName("US-ASCII"))
                val info = String(
                    payload, languageLength + 1, payload.size - languageLength - 1,
                    Charset.forName(textEncoding)
                )
                block(info)

            }
        } catch (e: Exception) {
            block("read fail: $e")
        } finally {
            ndef?.close()
        }
    }
}