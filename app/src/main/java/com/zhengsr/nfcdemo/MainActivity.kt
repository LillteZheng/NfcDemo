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

            val data = delegate.readData()
            data?.let {
                cardContent.text = it.msg
            }

        }

       /* ZNfc.getNDEFDelegate()

        val tagNfc = intent?.getParcelableExtra<Tag>(NfcAdapter.EXTRA_TAG)

        val id = NfcUtils.init(intent).readNfcId()
        id?.let {

        }



        NfcUtils.readNfcMsg(intent){
            cardContent.text = it
        }*/


        /*Log.d(TAG, "zsr onNewIntent: $id")
        when (intent?.action) {
            //标准的 nfc 数据，可以拿到很多参数
            NfcAdapter.ACTION_NDEF_DISCOVERED -> {
                val rawMessages = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES)
                getData(rawMessages)


                //val rawMessages =  intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES)
                //系统需要读取 NdefMessage 中的第一条 NdefRecord，以确定如何解读整个 NDEF 消息
                // （一个 NDEF 消息可能具有多条 NDEF 记录）。在格式正确的 NDEF 消息中，第一条 NdefRecord 包含以下字段
                //val ndefMessage = rawMessages[0] as NdefMessage?

                Log.d(TAG, "zsr tag:  - $tagNfc")
                //拿到 NDEF 就可以和 NFC 通信
                val ndef: Ndef? = Ndef.get(tagNfc)
                //Log.d(TAG, "zsr onNewIntent: $ndef")
            }
            NfcAdapter.ACTION_TAG_DISCOVERED -> {
                Log.d(TAG, "zsr onNewIntent: $tagNfc")

                tagNfc?.techList?.let {
                    if ("android.nfc.tech.MifareUltralight" in it){
                        readMifareUltralight(tagNfc)
                    }
                }

            }
        }
        val ndefMessage = NdefMessage(arrayOf(NdefRecord.createApplicationRecord(targetName)))
        //获取转换称字节的大小
        val size = ndefMessage.toByteArray().size*/

        /* val ndef :Ndef?= Ndef.get(tagNfc)
         if (ndef != null) {
             ndef.connect()
 
             Log.d(TAG, "zsr : ${ndef}");
             //是否可写
             if(!ndef.isWritable){
                 return
             }
             val msg = ndef.ndefMessage
             Log.d(TAG, "zsr $msg: ");
             //容量是否够用
             ndef.maxSize.takeIf { it<size }.run { return }
         }else{
             //当我们买回来的 nfc 标签是没有格式化的，或者没有分区的执行到这里
             val format = NdefFormatable.get(tagNfc)
             format?:return
             format.connect()
 
         }*/

    }

    fun getUID(intent: Intent): String {
        val myTag = intent.getParcelableExtra<Tag>(NfcAdapter.EXTRA_TAG)
        // return BaseEncoding.base16().encode(myTag.id)
        // return myTag.id.toString()
        return String(myTag.id)
    }

    fun getData(ndefMessages: Array<Parcelable>) {


        for (curMsg in ndefMessages) {

            curMsg as NdefMessage
            // Print generic information about the NDEF message
            logMessage("Message", curMsg.toString())
            // The NDEF message usually contains 1+ records - print the number of recoreds
            logMessage("Records", curMsg.records.size.toString())

            // Loop through all the records contained in the message
            for (curRecord in curMsg.records) {
                if (curRecord.toUri() != null) {
                    // URI NDEF Tag
                    logMessage("- URI", curRecord.toUri().toString())
                } else {
                    // Other NDEF Tags - simply print the payload
                    logMessage("- Contents", curRecord.payload.contentToString())
                }
            }

        }

        /*val msgs = arrayOfNulls<NdefMessage>(rawMsgs.size)
        for (i in rawMsgs.indices) {
            msgs[i] = rawMsgs[i] as NdefMessage
        }

        val records = msgs[0]!!.records

        var recordData = ""

        for (record in records) {
            recordData += record.toString() + "\n"
        }*/

    }


    private fun readMifareUltralight(tag: Tag){
        val mifareUltralight = MifareUltralight.get(tag)
        try {
            mifareUltralight.connect()
            //前0-3为nfc的基础数据，4开始为实际数据
            val readPages = mifareUltralight.readPages(4)
            val msg = readPages.toString()
            Log.d(TAG, "zsr readMifareUltralight: $msg  ${String(readPages,0,readPages.size)}")

        }catch (e:Exception){
            Log.d(TAG, "zsr readMifareUltralight error: $e")
        }
    }

    private fun logMessage(header: String, text: String?) {
      //  cardContent?.append(if (text.isNullOrBlank()) fromHtml("<b>$header</b><br>") else fromHtml("<b>$header</b>: $text<br>"))
        Log.d(TAG, "zsr logMessage: $text")

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
        ZNfc.getNDEFDelegate().writeMsg(msg){ isSuccess,msg->
            if (isSuccess){
                Toast.makeText(this, "数据写入成功", Toast.LENGTH_SHORT).show()
                if (ZNfc.getType() == NfcType.EDEF) {
                    val data = ZNfc.getNDEFDelegate().readData()
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