package com.example.flowwidget

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.google.android.material.button.MaterialButton

class MainActivity : ComponentActivity() {

    private val OVERLAY_PERMISSION_REQ_CODE = 1234
    private lateinit var btnToggle: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnToggle = findViewById(R.id.btn_toggle_widget)
        val btnSettings = findViewById<MaterialButton>(R.id.btn_settings)

        updateToggleButtonState()

        btnToggle.setOnClickListener {
            val intent = Intent(this, FloatingWidgetService::class.java)
            if (isServiceRunning(FloatingWidgetService::class.java)) {
                intent.putExtra("STOP_BY_USER", true)
                startService(intent) // Envia sinal para o serviço se auto-parar
                updateToggleButtonState()
                Toast.makeText(this, "Widget desativado", Toast.LENGTH_SHORT).show()
            } else {
                if (checkOverlayPermission()) {
                    checkBatteryOptimization()
                    startWidgetService()
                } else {
                    requestOverlayPermission()
                }
            }
        }

        btnSettings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        updateToggleButtonState()
    }

    private fun checkBatteryOptimization() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val intent = Intent()
            val pm = getSystemService(POWER_SERVICE) as PowerManager
            if (!pm.isIgnoringBatteryOptimizations(packageName)) {
                intent.action = Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
                intent.data = Uri.parse("package:$packageName")
                startActivity(intent)
            }
        }
    }

    private fun startWidgetService() {
        val intent = Intent(this, FloatingWidgetService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
        updateToggleButtonState()
    }

    private fun updateToggleButtonState() {
        if (isServiceRunning(FloatingWidgetService::class.java)) {
            btnToggle.setColorFilter(Color.parseColor("#FF5252")) // Vermelho sutil
        } else {
            btnToggle.setColorFilter(Color.WHITE)
        }
    }

    private fun isServiceRunning(serviceClass: Class<*>): Boolean {
        val manager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        @Suppress("DEPRECATION")
        for (service in manager.getRunningServices(Int.MAX_VALUE)) {
            if (serviceClass.name == service.service.className) {
                return true
            }
        }
        return false
    }

    private fun checkOverlayPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(this)
        } else {
            true
        }
    }

    private fun requestOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            @Suppress("DEPRECATION")
            startActivityForResult(intent, OVERLAY_PERMISSION_REQ_CODE)
        }
    }

    @Suppress("DEPRECATION")
    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == OVERLAY_PERMISSION_REQ_CODE) {
            if (checkOverlayPermission()) {
                startWidgetService()
            } else {
                Toast.makeText(this, "Permissão necessária para o widget.", Toast.LENGTH_LONG).show()
            }
        }
    }
}
