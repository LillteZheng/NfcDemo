package com.zhengsr.nfclib

import android.content.Intent
import android.nfc.NfcAdapter
import android.nfc.Tag
import com.zhengsr.nfclib.delegate.ndef.NDEFDelegate
import java.lang.RuntimeException

/**
 * @author by zhengshaorui 2020/8/26 19:38
 * describe：
 */
typealias NfcWriteListener = (Boolean, String) -> Unit

object ZNfc {
    private const val TAG = "ZNfc"
    private var type: NfcType = NfcType.UNKOWN
    fun inject(intent: Intent): ZNfc {
        val converter = NfcConverter.instance
        type = converter.setIntent(intent)
        return this
    }

    fun getNfcTag(): Tag? {
        return NfcConverter.instance.getNfcTag()
    }

    fun getType(): NfcType {
        return type
    }

    fun getNDEFDelegate(): NDEFDelegate {
        if (type != NfcType.EDEF) {
            throw RuntimeException("your NfcCard cannot support NDEF")
        }
        return NfcConverter.instance.getDelegate(type) as NDEFDelegate
    }


}

