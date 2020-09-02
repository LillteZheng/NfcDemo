package com.zhengsr.nfclib.delegate

import android.content.Intent
import android.nfc.NdefRecord
import android.nfc.Tag
import com.zhengsr.nfclib.NfcConverter

/**
 * @author by zhengshaorui 2020/8/27 10:52
 * describe：
 */
abstract class INFcDelegate {



    fun getCardId(): String? {
        return NfcConverter.instance.getCardId()
    }

    fun getNfcTag(): Tag? {
        return NfcConverter.instance.getNfcTag()
    }

}

