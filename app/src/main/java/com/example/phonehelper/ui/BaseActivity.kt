package com.example.phonehelper.ui

import android.Manifest
import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.provider.Settings
import android.view.accessibility.AccessibilityManager
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.phonehelper.PhoneHelperService
import com.example.phonehelper.R


@SuppressLint("Registered")
abstract class BaseActivity: AppCompatActivity() {

    companion object {
        const val REQ_CODE_PERMISSIONS = 437894723
    }

    private val alertDialog by lazy {
        AlertDialog.Builder(this)
            .setTitle(R.string.permission_required)
            .setMessage(R.string.you_must_enable_accessibility_service)
            .setPositiveButton(R.string.cta_go_to_settings) { _, _ -> openAccessibilitySettings() }
            .setOnCancelListener { finish() }
            .create().apply {
                setCanceledOnTouchOutside(false)
            }
    }

    override fun onBackPressed() {
        if (alertDialog.isShowing) finish()
    }

    override fun onResume() {
        super.onResume()

        if (!isAccessibilityServiceEnabled(this, PhoneHelperService::class.java)) {
            showEnabledAccessibilityServiceDialog()
        }
    }

    private fun showEnabledAccessibilityServiceDialog() {
        if (alertDialog.isShowing.not()) alertDialog.show()
    }

    private fun openAccessibilitySettings() {
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
        startActivity(intent)
    }

    private fun isAccessibilityServiceEnabled(context: Context,
                                              service: Class<out AccessibilityService?>): Boolean {
        val am = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
        val enabledServices =
            am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK)
        for (enabledService in enabledServices) {
            val enabledServiceInfo: ServiceInfo = enabledService.resolveInfo.serviceInfo
            if (enabledServiceInfo.packageName == context.packageName && enabledServiceInfo.name == service.name
            ) return true
        }
        return false
    }

    private fun requestStoragePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_EXTERNAL_STORAGE) && ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                //TODO show rationale
            } else {
                ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    REQ_CODE_PERMISSIONS)
            }
        }
    }

}