package com.zhengsr.nfclib.delegate.ndef

import android.net.Uri
import android.nfc.NdefRecord
import android.util.Log
import com.zhengsr.nfclib.bean.NfcRecord
import java.nio.charset.Charset
import java.util.*

/**
 * @author by zhengshaorui 2020/8/28 09:03
 * describe：
 */

internal object RecordDelegate {

    private val TAG = "RecordDelegate"


    /**
     * 根据类型，返回需要的NfcRecord
     */
    fun getRecordFromTnf(record: NdefRecord): NfcRecord? {


        return when (record.tnf) {
            NdefRecord.TNF_EXTERNAL_TYPE -> {
                createExternalRecord(record)
            }
            NdefRecord.TNF_ABSOLUTE_URI -> {
                return createURLRecord(record.toUri())
            }
            NdefRecord.TNF_MIME_MEDIA ->
                return createMIMERecord(
                    String(record.type, Charset.forName("UTF-8")),
                    record.payload
                )
            NdefRecord.TNF_WELL_KNOWN -> return createWellKnownRecord(record)
            else -> null
        }
    }


    private fun createExternalRecord(record: NdefRecord): NfcRecord {
        val bean = NfcRecord()
        bean.recordType = NfcRecord.TYPE_EXTERNAL
        bean.msg = String(record.payload)
        bean.data = record.payload
        return bean
    }

    /**
     * Constructs URL NfcRecord
     */
    private fun createURLRecord(uri: Uri?): NfcRecord? {
        if (uri == null) return null
        val bean = NfcRecord()
        bean.recordType = NfcRecord.TYPE_URI
        bean.msg = uri.toString()
        bean.data = bean.msg?.toByteArray()
        return bean
    }

    /**
     * Constructs MIME  NfcRecord
     */
    private fun createMIMERecord(mediaType: String, payload: ByteArray): NfcRecord? {
        val bean = NfcRecord()
        bean.recordType = NfcRecord.TYPE_MIME
        bean.data = payload
        bean.msg = String(payload)
        return bean
    }

    /**
     * Constructs well known type (TEXT or URI) NfcRecord
     */
    private fun createWellKnownRecord(record: NdefRecord): NfcRecord? {
        if (Arrays.equals(record.type, NdefRecord.RTD_URI)) {
            return createURLRecord(record.toUri())
        }
        return if (Arrays.equals(record.type, NdefRecord.RTD_TEXT)) {
            val msg = record.toMimeType()
            createText(record.payload)
        } else null
    }


    /**
     * Constructs TEXT NfcRecord
     */
    private fun createText(payload: ByteArray): NfcRecord? {
        // Check that text byte array is not empty.
        if (payload.isEmpty()) {
            return null
        }
        val bean = NfcRecord()
        //判断语言编码，第7位0位 utf-8，1位utf-16
        val textEncoding = if ((payload[0].toInt() and 0x80) == 0) "UTF-8" else "Utf-16"
        //语言编码的长度,0-5bit
        val languageLength = payload[0].toInt() and 0x3f

        val startPos = languageLength + 1
        //拿到当前语言
        // val language = String(payload, 1, languageLength, Charset.forName("US-ASCII"))


        val info = String(
            payload, languageLength + 1, payload.size - languageLength - 1,
            Charset.forName(textEncoding)
        )
        bean.recordType = NfcRecord.TYPE_TEXT
        bean.data = payload.copyOfRange(startPos, payload.size)
        bean.data?.let {
            val ss = String(it, Charset.forName(textEncoding))
        }
        bean.msg = info
        return bean
    }
}