package com.zhengsr.nfclib

import android.content.Intent
import android.nfc.NfcAdapter
import android.nfc.Tag
import java.lang.RuntimeException

/**
 * @author by zhengshaorui 2020/8/26 19:39
 * describe：
 */

internal class NfcBuilder {


    lateinit var intent: Intent
    private var nfcTag: Tag? = null

    companion object {
        val instance: NfcBuilder by lazy { NfcBuilder() }
    }


    fun init(intent: Intent) {
        this.intent = intent

        getNfcTag()

    }

    fun getCardId(): String? {
        checkIntent()
        return NfcConverter.getCardId(nfcTag)
    }


    private fun checkIntent() {
        if (!::intent.isInitialized) {
            throw RuntimeException("intent is null ,please use ZNfc.init() to init !")
        }
    }

    /**
     * 获取tag
     */
    fun getNfcTag(): Tag? {
        checkIntent()
        nfcTag = intent.getParcelableExtra<Tag>(NfcAdapter.EXTRA_TAG)
        return nfcTag
    }

    fun readNDEFMsg(): String? {
        checkIntent()
        return when (intent.action) {
            NfcAdapter.EXTRA_NDEF_MESSAGES -> {
                null
            }
            NfcAdapter.ACTION_TAG_DISCOVERED -> {
                null
            }
            else ->null
        }
    }



}