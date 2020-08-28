package com.zhengsr.nfcdemo

import android.app.PendingIntent
import android.content.Intent
import android.nfc.NdefMessage
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.MifareUltralight
import android.os.Bundle
import android.os.Parcelable
import android.text.Html
import android.text.Spanned
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.zhengsr.nfclib.NfcType
import com.zhengsr.nfclib.ZNfc


class MainActivity : AppCompatActivity() {
    private val TAG = "NfcActivity"
    private var nfcAdapter: NfcAdapter? = null
    private var pendingIntent: PendingIntent? = null
    private val targetName = "com.android.mms"
    private lateinit var cardContent: TextView
    private lateinit var cardId :TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        cardContent = findViewById(R.id.card_num)
        cardId = findViewById(R.id.card_id)

        nfcAdapter = NfcAdapter.getDefaultAdapter(this)
        if (nfcAdapter == null) {
            Toast.makeText(this, "您的设备不支持NFC功能", Toast.LENGTH_SHORT).show()
            finish()
        }
        val intent = Intent(this, javaClass).apply {
            //设置称 single_top
            addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }
        pendingIntent = PendingIntent.getActivity(this, 0, intent, 0)


    }


    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        Log.d(TAG, "zsr onNewIntent: " + intent?.action)

        if(ZNfc.init(intent!!).getType() == NfcType.EDEF){
            val delegate = ZNfc.getNDEFDelegate()
            cardId.text = fromHtml("卡ID: ${delegate.getCardId()}")

            val data = delegate.readRecord()
            data?.let {
                cardContent.text = it.msg
            }
        }
    }
    private fun fromHtml(html: String): Spanned {
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY)
        } else {
            @Suppress("DEPRECATION")
            Html.fromHtml(html)
        }
    }

    override fun onResume() {
        super.onResume()
        //优先于其他 nfc 设备，并跳到自身应用
        nfcAdapter?.enableForegroundDispatch(this, pendingIntent, null, null)
    }

    override fun onPause() {
        super.onPause()
        //回复默认状态
        nfcAdapter?.disableForegroundDispatch(this)
    }

    fun write(view: View) {
        val msgEd = findViewById<EditText>(R.id.msg_ed)
        val msg = msgEd.text.toString()
        val domain = "com.zhengsr" //usually your app's package name
        val type = "nfcdemo"
        ZNfc.getNDEFDelegate().writeMsg(msg){ isSuccess,msg->
            if (isSuccess){
                Toast.makeText(this, "数据写入成功", Toast.LENGTH_SHORT).show()
                if (ZNfc.getType() == NfcType.EDEF) {
                    val data = ZNfc.getNDEFDelegate().readRecord()
                    data?.let {
                        cardContent.text = it.msg
                    }
                }
            }else{
                Toast.makeText(this, "数据写入失败: $msg", Toast.LENGTH_SHORT).show()
            }
            msgEd.setText("")
        }

    }


}