package com.zaizoor.notifierpro

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<ImageButton>(R.id.btnSettings).setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        findViewById<View>(R.id.btnViewVault).setOnClickListener {
            startActivity(Intent(this, HistoryActivity::class.java))
        }

        updateStatus()
        startStatusRefresh()
    }

    private fun updateStatus() {
        val tvStatus = findViewById<TextView>(R.id.tvListenerStatus) ?: return
        val vStatusDot = findViewById<View>(R.id.vStatusDot) ?: return
        val isEnabled = isNotificationServiceEnabled()
        
        tvStatus.text = if (isEnabled) "System Active" else "System Disabled"
        vStatusDot.backgroundTintList = android.content.res.ColorStateList.valueOf(
            if (isEnabled) android.graphics.Color.GREEN else android.graphics.Color.RED
        )

        // Sync Keywords to UI
        findViewById<TextView>(R.id.tvHomeSubjects).text = 
            KeywordManager.getSubjectKeywords(this).joinToString(" • ")
        findViewById<TextView>(R.id.tvHomeBody).text = 
            KeywordManager.getBodyKeywords(this).joinToString(" • ")
    }

    private fun startStatusRefresh() {
        handler.postDelayed(object : Runnable {
            override fun run() {
                updateStatus()
                handler.postDelayed(this, 2000)
            }
        }, 2000)
    }

    private fun isNotificationServiceEnabled(): Boolean {
        val cn = android.content.ComponentName(this, GmailNotificationListener::class.java)
        val flat = android.provider.Settings.Secure.getString(contentResolver, "enabled_notification_listeners")
        return flat != null && flat.contains(cn.flattenToString())
    }

    override fun onResume() {
        super.onResume()
        updateStatus()
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
    }
}
