package com.zhengsr.nfcdemo

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class HostCardActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_simi_main)
        startService(Intent(this,MyHostService::class.java))
    }
}