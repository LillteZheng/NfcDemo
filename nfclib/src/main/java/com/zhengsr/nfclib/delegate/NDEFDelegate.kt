package com.zhengsr.nfclib.delegate

import android.net.Uri
import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.Tag
import android.nfc.tech.Ndef
import android.nfc.tech.NdefFormatable
import android.os.Build
import android.util.Log
import com.zhengsr.nfclib.NfcConverter
import java.io.UnsupportedEncodingException
import java.nio.charset.Charset
import java.util.*

/**
 * @author by zhengshaorui 2020/8/27 10:20
 * describe：
 */

class NDEFDelegate : INFcDelegate() {

    private lateinit var tag: Tag

    companion object {
        private const val TAG = "NDEFDelegate"
        val instance: NDEFDelegate by lazy {
            NDEFDelegate()
        }


    }

    internal fun getConverter(converter: NfcConverter): NDEFDelegate {
        //拿得到ndef，则tag一定有的
        tag = converter.getNfcTag()!!
        return this
    }


    fun readData(): NdefData? {
        val ndef = Ndef.get(tag)
        return try {
            ndef?.connect()
            val message = ndef.ndefMessage
            message?.let {
                toNfcRecord(it.records[0])
            }
        } catch (e: Exception) {
            Log.d(TAG, "zsr readData: $e")
            null
        } finally {

            ndef.close()
        }
    }

    fun writeMsg(msg: String, block: (Boolean, String) -> Unit) {

        val record = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            NdefRecord.createTextRecord(Locale.CHINA.language, msg)
        } else {
            NdefRecord.createMime("text/plain", msg.toByteArray())
        }
        val uriRecord = NdefRecord.createUri(Uri.parse(msg))
        val message = NdefMessage(arrayOf(uriRecord))

        val ndef = Ndef.get(tag)
        try {
            if (ndef != null) {
                ndef.connect()
                if (!ndef.isWritable) {
                    block(false, "your card only readable")
                    return
                }

                if (ndef.maxSize < msg.length) {
                    block(false, "msg is too long")
                    return
                }
                ndef.writeNdefMessage(message)
                ndef.close()
                block(true, "write success!")
            } else {
                //此时的 nfc 还未分区和格式化
                val formatable = NdefFormatable.get(tag)
                if (formatable != null) {
                    formatable.connect()
                    formatable.format(message)
                    formatable.close()
                    block(true, "write success!")
                } else {
                    block(false, "write fail NdefFormatable is null")
                }
            }


        } catch (e: Exception) {
            block(false, "write fail $e")
        } finally {
            ndef.close()
        }
    }


    @Throws(UnsupportedEncodingException::class)
    private fun toNfcRecord(ndefRecord: NdefRecord): com.zhengsr.nfclib.delegate.NdefData? {
        Log.d(TAG, "zsr toNfcRecord: ${ndefRecord.tnf}")
        when (ndefRecord.tnf) {
            
            NdefRecord.TNF_ABSOLUTE_URI ->{
                return createURLRecord(ndefRecord.toUri())
            }
            NdefRecord.TNF_MIME_MEDIA ->
                return createMIMERecord(String(ndefRecord.type, Charset.forName("UTF-8")), ndefRecord.payload);

            NdefRecord.TNF_WELL_KNOWN -> return createWellKnownRecord(ndefRecord)
        }
        return null
    }

    /**
     * Constructs URL NfcRecord
     */
    private fun createURLRecord(uri: Uri?): NdefData? {
        if (uri == null) return null
        val bean = NdefData()
        bean.msg = uri.toString()
        Log.d(TAG, "zsr createURLRecord: ${bean.msg}")

        return bean

    }


    /**
     * Constructs MIME or JSON NfcRecord
     */
    private fun createMIMERecord(mediaType: String, payload: ByteArray): NdefData? {
        val bean = NdefData()
        bean.data = payload
        bean.msg = String(payload)
        return bean
    }

    /**
     * Constructs well known type (TEXT or URI) NfcRecord
     */
    private fun createWellKnownRecord(record: NdefRecord): NdefData? {
        if (Arrays.equals(record.type, NdefRecord.RTD_URI)) {
            return createURLRecord(record.toUri())
        }
        return if (Arrays.equals(record.type, NdefRecord.RTD_TEXT)) {
            val msg = record.toMimeType()
            Log.d(TAG, "zsr createWellKnownRecord: $msg")
            createText(record.payload)
        } else null
    }


    /**
     * Constructs TEXT NfcRecord
     */
    private fun createText(payload: ByteArray): com.zhengsr.nfclib.delegate.NdefData? {
        // Check that text byte array is not empty.
        if (payload.isEmpty()) {
            return null
        }

        val bean = NdefData()

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
        bean.data = payload.copyOfRange(startPos, payload.size)
        bean.data?.let {
            val ss = String(it, Charset.forName(textEncoding))
            Log.d(TAG, "zsr createText: $ss")
        }
        bean.msg = info
        return bean
    }


}