package com.example.phonehelper

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        enable_permission_btn.setOnClickListener {
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            startActivity(intent)
        }
    }


}