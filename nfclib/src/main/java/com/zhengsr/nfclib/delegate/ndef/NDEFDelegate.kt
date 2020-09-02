package com.zhengsr.nfclib.delegate.ndef

import android.net.Uri
import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.Tag
import android.nfc.tech.Ndef
import android.nfc.tech.NdefFormatable
import android.os.Build
import android.util.Log
import com.zhengsr.nfclib.NfcConverter
import com.zhengsr.nfclib.NfcWriteListener
import com.zhengsr.nfclib.bean.NfcRecord
import com.zhengsr.nfclib.delegate.INFcDelegate
import java.io.UnsupportedEncodingException
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

    internal fun ConveterDispatcher(converter: NfcConverter): NDEFDelegate {
        //拿得到ndef，则tag一定有的
        tag = converter.getNfcTag()!!
        return this
    }


    fun readRecord(): NfcRecord? {
        val ndef = Ndef.get(tag)
        return try {
            ndef?.connect()
            val message = ndef.ndefMessage
            message?.let {
                getNfcRecord(it.records[0])
            }
        } catch (e: Exception) {
            Log.d(TAG, "zsr readData: $e")
            null
        } finally {

            ndef.close()
        }
    }

    fun readRecords():List<NfcRecord>?{
        val ndef = Ndef.get(tag)
        val records = mutableListOf<NfcRecord>()
        return try {
            ndef?.connect()
            val message = ndef.ndefMessage
            message?.let {
                it.records.forEach { record->
                    getNfcRecord(record)?.let { nfcRecord ->
                        records.add(nfcRecord)
                    }
                }
            }
            records
        } catch (e: Exception) {
            Log.d(TAG, "zsr readData: $e")
            null
        } finally {
            ndef.close()
        }

    }




    fun writeMsg(msg: String, block: NfcWriteListener) {
        val record =  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            NdefRecord.createTextRecord(Locale.CHINA.language, msg)
        } else {
            NdefRecord.createMime("text/plain", msg.toByteArray())
        }
        val message = NdefMessage(arrayOf(record))
        writeNdefData(block, msg.length, message)
    }

    fun writeMime(mime:String,msg:String,block: NfcWriteListener){
        val record = NdefRecord.createMime(mime,msg.toByteArray())
        val message = NdefMessage(arrayOf(record))
        writeNdefData(block, msg.length, message)
    }

    fun writeUri(uriMsg: String, block: NfcWriteListener) {
        val record = NdefRecord.createUri(uriMsg)
        val message = NdefMessage(arrayOf(record))
        writeNdefData(block, uriMsg.length, message)
    }
    fun writeUri(uri: Uri, block: NfcWriteListener) {
        writeUri(uri.toString(),block)
    }
    fun writeExternal(domain:String, type:String, msg:String,block: NfcWriteListener){
        val record = NdefRecord.createExternal(domain,type,msg.toByteArray())
        val message = NdefMessage(arrayOf(record))
        writeNdefData(block, msg.length, message)
    }


    fun writeNDEFRecord(vararg records: NdefRecord, block: NfcWriteListener){
        var size  = 0;
        records.forEach {
           size =  it.payload.size
        }
        val message = NdefMessage(records)
        writeNdefData(block,size,message)

    }


    /**
     * 写NDEF数据
     */
    private fun writeNdefData(block: NfcWriteListener, dataSize: Int, message: NdefMessage) {
        val ndef = Ndef.get(tag)
        try {
            if (ndef != null) {
                ndef.connect()
                if (!ndef.isWritable) {
                    block(false, "your card only readable")
                    return
                }

                if (ndef.maxSize < dataSize) {
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
    private fun getNfcRecord(ndefRecord: NdefRecord): NfcRecord? {
        return RecordDelegate.getRecordFromTnf(ndefRecord)
    }





}