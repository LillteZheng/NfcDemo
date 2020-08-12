package com.zhengsr.nfcdemo

import android.app.PendingIntent
import android.content.Intent
import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.Ndef
import android.nfc.tech.NdefFormatable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast

class MainActivity : AppCompatActivity() {
    private  val TAG = "NfcActivity"
    private var nfcAdapter: NfcAdapter? = null
    private var pendingIntent: PendingIntent? = null
    private val targetName = "com.android.mms"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        nfcAdapter = NfcAdapter.getDefaultAdapter(this)
        if (nfcAdapter == null) {
            Toast.makeText(this, "您的设备不支持NFC功能", Toast.LENGTH_SHORT).show()
            finish()
        }
        val intent = Intent(this,javaClass).apply {
            //设置称 single_top
            addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }
        pendingIntent = PendingIntent.getActivity(this,0, intent,0)
        val count = 10
        for (i in 0 until count) {
        }

    }


    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        Log.d(TAG, "zsr onNewIntent: "+intent?.action)
        val tagNfc = intent?.getParcelableExtra<Tag>(NfcAdapter.EXTRA_TAG)
        val ndefMessage = NdefMessage(arrayOf(NdefRecord.createApplicationRecord(targetName)))
        //获取转换称字节的大小
        val size = ndefMessage.toByteArray().size

        val ndef = Ndef.get(tagNfc)
        if (ndef != null) {
            ndef.connect()

            //是否可写
            if(!ndef.isWritable){
                return
            }
            //容量是否够用
            ndef.maxSize.takeIf { it<size }.run { return }
        }else{
            //当我们买回来的 nfc 标签是没有格式化的，或者没有分区的执行到这里
            val format = NdefFormatable.get(tagNfc)
            format?:return
            format.connect()

        }

    }

    override fun onResume() {
        super.onResume()
        //优先于其他 nfc 设备，并跳到自身应用
        nfcAdapter?.enableForegroundDispatch(this,pendingIntent, null,null)
    }

    override fun onPause() {
        super.onPause()
        //回复默认状态
        nfcAdapter?.disableForegroundDispatch(this)
    }
}