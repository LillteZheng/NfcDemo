package com.zhengsr.nfcdemo

import android.app.PendingIntent
import android.content.Intent
import android.nfc.NfcAdapter
import android.nfc.tech.Ndef
import android.os.Bundle
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
import com.zhengsr.nfclib.kt.hexStringToByteArray

class ReadNfcActivity : AppCompatActivity() {
    private val TAG = "ReadNfcActivity"
    private var nfcAdapter: NfcAdapter? = null
    private var pendingIntent: PendingIntent? = null
    private lateinit var cardContent: TextView
    private lateinit var cardId : TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_read_nfc)
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


        Log.d(TAG, "zsr onCreate: ${intent}")
    }


    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        Log.d(TAG, "zsr onNewIntent: $intent")
        //注册intent
        ZNfc.inject(intent!!)
        if(ZNfc.getType() == NfcType.EDEF){

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
       /* nfcAdapter?.enableReaderMode(this, this,
            NfcAdapter.FLAG_READER_NFC_A,
            null)*/
    }
    

    override fun onPause() {
        super.onPause()
        //回复默认状态
        nfcAdapter?.disableForegroundDispatch(this)
      //  nfcAdapter?.disableReaderMode(this)
    }

    fun write(view: View) {
        val msgEd = findViewById<EditText>(R.id.msg_ed)
        val msg = msgEd.text.toString()
        val domain = "com.zhengsr.nfcdemo" //usually your app's package name
        val type = "externalType"
        val mime = "application/com.zhengsr.nfctest";
        ZNfc.getNDEFDelegate().writeMime(mime,msg){ isSuccess, result->
            if (isSuccess){
                Toast.makeText(this, "数据写入成功", Toast.LENGTH_SHORT).show()
                if (ZNfc.getType() == NfcType.EDEF) {
                    val data = ZNfc.getNDEFDelegate().readRecord()
                    data?.let {
                        cardContent.text = it.msg
                    }
                }
            }else{
                Toast.makeText(this, "数据写入失败: $result", Toast.LENGTH_SHORT).show()
            }
            msgEd.setText("")
        }

    }

   /* override fun onTagDiscovered(tag: Tag?) {
        Log.d(TAG, "zsr onTagDiscovered: $tag")
        val isoDep = IsoDep.get(tag)
        if (isoDep != null){
            isoDep.connect()
            val AID = "F123466666"
            //转换指令为byte[]
            //转换指令为byte[]
            val command: ByteArray = buildSelectApdu(AID)
            Log.d(TAG, "zsr onTagDiscovered: ${command.contentToString()}")

            // 发送指令

            // 发送指令
            val result = isoDep.transceive(command)
            Log.d(TAG, "zsr result: ${result.contentToString()}")
        }

    }*/

    private fun buildSelectApdu(aid: String): ByteArray {
        val HEADER = "00A40400"
        return hexStringToByteArray(HEADER + String.format("%02X", aid.length / 2) + aid)
    }
}