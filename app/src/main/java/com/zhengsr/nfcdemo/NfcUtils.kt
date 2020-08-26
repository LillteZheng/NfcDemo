package com.zhengsr.nfcdemo

import android.content.Intent
import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.IsoDep
import android.nfc.tech.Ndef
import android.nfc.tech.NdefFormatable
import android.util.Log
import java.nio.charset.Charset
import java.util.*
import kotlin.experimental.and

/**
 * @author by zhengshaorui 2020/8/24 16:05
 * describe：NFC 卡管理
 */
typealias ReadListener = (String) -> Unit

object NfcUtils {
    private const val TAG = "NfcManager"
    private var nfcTag: Tag? = null
    private var intent: Intent? = null


    /**
     * 需要先初始化
     */
    fun init(intent: Intent?): NfcUtils {
        this.intent = intent
        getNfcTag(intent)
        return this
    }


    /**
     * 读取nfc id
     */
    fun readNfcId(): String? {
        return nfcTag?.let {
            ByteArrayToHexString(it.id)
        }
    }

    /**
     * 获取tag
     */
    fun getNfcTag(intent: Intent?): Tag? {
        nfcTag = intent?.getParcelableExtra<Tag>(NfcAdapter.EXTRA_TAG)
        return nfcTag
    }


    fun readNfcMsg(intent: Intent?, block: ReadListener) {
        val tag = getNfcTag(intent) ?: return
        when (intent?.action) {
            NfcAdapter.ACTION_NDEF_DISCOVERED -> {
                //todo 读取 ndef 的数据
                nfcTag?.let {
                    readNDEFMsg(it, block)
                }
                val message = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES)

            }
            NfcAdapter.ACTION_TAG_DISCOVERED -> {
                tag.techList?.forEach {
                    when (it) {
                        Ndef::class.java.name -> {
                            nfcTag?.let { tag ->
                                readNDEFMsg(tag, block)
                            }
                        }
                        IsoDep::class.java.name -> {
                            //todo 交通卡
                        }
                    }
                }
            }
        }
    }



    fun writeData(msg: String, block: (Boolean, String) -> Unit) {


        if (nfcTag == null) {
            block(false, "you should getNfcTag to get NFC tag")
            return
        }




        //
        //中文编码
        val langBytes = Locale.CHINA.language.toByteArray(Charset.forName("US-ASCII"))
        //数据类型为 utf-8
        val utfEncoding = Charset.forName("UTF-8")
        //数据转换成数组
        val textBytes = msg.toByteArray(utfEncoding)
        //因为固定为 utf-8 的格式，所以 payload 的第7位为0
        val utfBit: Int = 0
        //定义和初始化状态字节
        val status = (utfBit + langBytes.size).toChar()
        //创建存储数据的数据
        val data = ByteArray(1 + langBytes.size + textBytes.size)
        //设置第一位的状态吗
        data[0] = status.toByte()
        //设置语言编码
        System.arraycopy(langBytes, 0, data, 1, langBytes.size)
        //存入实际的数据
        System.arraycopy(textBytes, 0, data, 1 + langBytes.size, textBytes.size)

        //创建 NdefRecord,这个是第一个 record，以确定如何解读整个 NDEF 消息
        val record =
            NdefRecord(NdefRecord.TNF_WELL_KNOWN, NdefRecord.RTD_TEXT, ByteArray(0), data)
        //创建 ndefmessage
       // val ndefMessage = NdefMessage(arrayOf(record))
        val mimeRecord = NdefRecord.createMime(
            "application/vnd.com.zhengsr.nfcdemo.nfcutils",
            msg.toByteArray(Charset.forName("US-ASCII"))
        )
        val textRecord = NdefRecord.createTextRecord(Locale.CHINA.language, msg)

        val ndefMessage = NdefMessage(mimeRecord)
        val size = ndefMessage.toByteArray().size

        val ndef = Ndef.get(nfcTag)
        try {
            if (ndef != null) {
                ndef.connect()
                if (!ndef.isWritable) {
                    block(false,"NFC为只读")
                    return
                }
                //容量够不够用
                if (ndef.maxSize < size) {
                    block(false,"容量不够了")
                    return
                }
                ndef.writeNdefMessage(ndefMessage)
                block(true, "write success!")
            } else {
                //此时的 nfc 还未分区和格式化
                val formatable = NdefFormatable.get(nfcTag)
                if (formatable != null) {
                    formatable.connect()
                    formatable.format(ndefMessage)
                    block(true, "write success!")
                } else {
                    block(false, "NdefFormatable is null")
                }
            }
        } catch (e: Exception) {
            block(false, e.toString())
        } finally {
            ndef?.close()
        }


    }


    //todo 后面弄成 一个 textRecord
    private fun readNDEFMsg(tag: Tag, block: ReadListener) {
        val ndef = Ndef.get(tag)
        try {
            ndef.connect()
            val msg = ndef.ndefMessage
            if (msg == null) {
                block("没有数据")
            } else {
                msg.records ?: return
                Log.d(TAG, "zsr readNDEFMsg: ${ndef.maxSize} ${msg.records.size}")

                val fs = msg.records[0]
                val m = String(fs.payload)
                Log.d(TAG, "zsr readNDEFMsg: $m")
                val payload = msg.records[0].payload

               
                //判断语言编码，第7位0位 utf-8，1位utf-16
                val textEncoding = if ((payload[0].toInt() and 0x80) == 0) "UTF-8" else "Utf-16"
                //语言编码的长度,0-5bit
                val languageLength = payload[0].toInt() and 0x3f

                //拿到当前语言
                val language = String(payload, 1, languageLength, Charset.forName("US-ASCII"))

                val info = String(
                    payload, languageLength +1, payload.size - languageLength - 1,
                    Charset.forName(textEncoding)
                )

                block(info)

            }
        } catch (e: Exception) {
            block("读取失败: $e")
        } finally {
            ndef?.close()
        }

    }


    fun bytesToHexString(src: ByteArray?): String? {
        val stringBuilder = StringBuilder("")
        if (src == null || src.size <= 0) {
            return null
        }
        for (i in 0..src.size - 1) {
            val v = src[i].toInt() and 0xFF
            val hv = Integer.toHexString(v)
            if (hv.length < 2) {
                stringBuilder.append(0)
            }
            stringBuilder.append(hv)
        }
        return stringBuilder.toString()
    }

    private fun ByteArrayToHexString(inarray: ByteArray): String? {
        var i: Int
        var j: Int
        var num: Int
        //16个字节
        val hex = arrayOf(
            "0", "1", "2", "3", "4", "5",
            "6", "7", "8", "9", "A", "B",
            "C", "D", "E", "F"
        )
        var out = ""
        j = 0
        while (j < inarray.size) {
            num = inarray[j].toInt() and 0xff
            i = num shr 4 and 0x0f
            out += hex[i]
            i = num and 0x0f
            out += hex[i]
            ++j
        }
        return out
    }
}