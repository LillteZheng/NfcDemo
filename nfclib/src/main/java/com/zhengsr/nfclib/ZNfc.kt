package com.zhengsr.nfclib

import android.content.Intent
import android.nfc.Tag
import android.nfc.tech.NfcB

/**
 * @author by zhengshaorui 2020/8/26 19:38
 * describe：
 */
typealias NfcListener = (String) -> Unit
object ZNfc {

    fun init(intent: Intent): ZNfc {
        val builder = NfcBuilder.instance
        builder.intent = intent
        return this
    }

    fun getCardId(): String? {
        return NfcBuilder.instance.getCardId()
    }

    fun getNfcTag():Tag?{
        return NfcBuilder.instance.getNfcTag()
    }


    fun readMsg():String?{

    }
}

