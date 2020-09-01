package com.zhengsr.nfcdemo

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Manifest.permission.BIND_NFC_SERVICE
        //startActivity(Intent(this,HostCardActivity::class.java))
    }

    fun mainCard(view: View) {
        Toast.makeText(this, "不是系统应用，无法模拟!!", Toast.LENGTH_SHORT).show()
        //startActivity(Intent(this,HostCardActivity::class.java))
    }
    fun readNfc(view: View) {
        startActivity(Intent(this,ReadNfcActivity::class.java))
    }


}