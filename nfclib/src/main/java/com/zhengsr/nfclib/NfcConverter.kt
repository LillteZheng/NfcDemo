package com.zhengsr.nfclib

import android.content.Intent
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.Ndef
import android.nfc.tech.NdefFormatable
import com.zhengsr.nfclib.delegate.INFcDelegate
import com.zhengsr.nfclib.delegate.ndef.NDEFDelegate
import com.zhengsr.nfclib.kt.bytesToHexString
import java.lang.RuntimeException

/**
 * @author by zhengshaorui 2020/8/26 19:39
 * describe：
 */

internal class NfcConverter {


    private lateinit var intent: Intent
    private var nfcTag: Tag? = null

    companion object {
        val instance: NfcConverter by lazy { NfcConverter() }
    }


    fun init(intent: Intent) {
        this.intent = intent

        getNfcTag()

    }

    fun getCardId(): String? {
        checkIntent()
        return nfcTag?.let {
            bytesToHexString(it.id)
        }
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

    /**
     * 根据 intent 返回不同的 实现类
     */

    fun getDelegate(type: NfcType): INFcDelegate? {
        checkIntent()
        return when (type) {
            NfcType.EDEF -> {
                return NDEFDelegate.instance.ConveterDispatcher(this)
            }
            else -> null
        }
    }

    internal fun setIntent(intent: Intent): NfcType {
        this.intent = intent
        var type = NfcType.UNKOWN
        when (intent.action) {
            NfcAdapter.ACTION_NDEF_DISCOVERED -> type = NfcType.EDEF
            NfcAdapter.ACTION_TAG_DISCOVERED -> {
                getNfcTag()?.techList?.forEach {
                    when (it) {
                        Ndef::class.java.name -> type = NfcType.EDEF
                        NdefFormatable::class.java.name -> type = NfcType.EDEF
                    }
                }

            }
        }
        return type
    }




}