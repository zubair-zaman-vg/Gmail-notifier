package com.zaizoor.notifierpro

import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton

class SettingsActivity : AppCompatActivity() {

    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        findViewById<MaterialToolbar>(R.id.toolbar).setNavigationOnClickListener {
            finish()
        }

        findViewById<MaterialButton>(R.id.btnManageFilters).setOnClickListener {
            startActivity(Intent(this, FiltersActivity::class.java))
        }

        findViewById<MaterialButton>(R.id.btnNotifyAccess).setOnClickListener {
            startActivity(Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"))
        }

        findViewById<MaterialButton>(R.id.btnOverlayPerm).setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION).apply {
                    data = Uri.parse("package:$packageName")
                }
                startActivity(intent)
            }
        }

        findViewById<MaterialButton>(R.id.btnBatteryOptim).setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                startActivity(intent)
            }
        }

        findViewById<MaterialButton>(R.id.btnRepairService).setOnClickListener {
            repairServiceBinding()
        }

        findViewById<MaterialButton>(R.id.btnTestAlert).setOnClickListener {
            val intent = Intent(this, AlertActivity::class.java).apply {
                putExtra("subject", "TEST: Royal Alert")
                putExtra("body", "This is a Test from Settings. Order placed from Amazon.")
            }
            startActivity(intent)
        }

        startStatusRefresh()
    }

    private fun startStatusRefresh() {
        handler.postDelayed(object : Runnable {
            override fun run() {
                findViewById<android.widget.TextView>(R.id.tvLastPackage).text = 
                    "Last seen: ${GmailNotificationListener.lastPackageSeen}"
                handler.postDelayed(this, 2000)
            }
        }, 2000)
    }

    private fun repairServiceBinding() {
        val pm = packageManager
        val componentName = ComponentName(this, GmailNotificationListener::class.java)
        
        pm.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP)
        handler.postDelayed({
            pm.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP)
            Toast.makeText(this, "Service Re-bound Successfully!", Toast.LENGTH_SHORT).show()
        }, 1000)
    }
}
