package com.zhengsr.nfclib

import android.content.Intent
import android.nfc.NfcAdapter
import android.nfc.tech.Ndef
import com.zhengsr.nfclib.delegate.NDEFDelegate
import java.lang.RuntimeException

/**
 * @author by zhengshaorui 2020/8/26 19:38
 * describe：
 */
typealias NfcListener = (String) -> Unit

object ZNfc {
    private const val TAG = "ZNfc"
    private var type: NfcType = NfcType.UNKOWN

    fun init(intent: Intent): ZNfc {
        val converter = NfcConverter.instance
        converter.intent = intent

        when (intent.action) {
            NfcAdapter.ACTION_NDEF_DISCOVERED -> {
                type = NfcType.EDEF
            }
            NfcAdapter.ACTION_TAG_DISCOVERED ->{
                converter.getNfcTag()?.techList?.forEach {
                    when(it){
                        Ndef::class.java.name -> type= NfcType.EDEF
                    }
                }

            }
        }

        return this
    }


    fun getType(): NfcType {
        return type
    }


    fun getNDEFDelegate(): NDEFDelegate{
        if (type != NfcType.EDEF){
            throw RuntimeException("you card cannot support NDEF")
        }
        return NfcConverter.instance.getDelegate(type) as NDEFDelegate
    }


}

