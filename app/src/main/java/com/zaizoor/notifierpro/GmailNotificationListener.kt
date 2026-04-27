package com.zaizoor.notifierpro

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.app.Notification
import android.content.Intent
import android.util.Log

class GmailNotificationListener : NotificationListenerService() {
    private val handler = android.os.Handler(android.os.Looper.getMainLooper())
    private val CHANNEL_ID = "service_status"
    
    companion object {
        var lastPackageSeen = "None"
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        Log.d("NotifierPro", "Listener Connected. Foreground active.")
        startForegroundService()
    }

    private fun startForegroundService() {
        val notificationManager = getSystemService(android.content.Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = android.app.NotificationChannel(CHANNEL_ID, "Service Background", android.app.NotificationManager.IMPORTANCE_LOW)
            notificationManager.createNotificationChannel(channel)
        }

        val notification = androidx.core.app.NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Gmail Monitor Active")
            .setContentText("Monitoring orders in real-time...")
            .setSmallIcon(android.R.drawable.ic_dialog_email)
            .setPriority(androidx.core.app.NotificationCompat.PRIORITY_MIN)
            .build()

        startForeground(1001, notification)
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        lastPackageSeen = sbn.packageName
        
        // 1. Check if it's Gmail
        if (sbn.packageName != "com.google.android.gm") return

        val extras = sbn.notification.extras
        
        // Combine EVERYTHING into one searchable string
        val allContent = StringBuilder()
        allContent.append(extras.getCharSequence(Notification.EXTRA_TITLE) ?: "")
        allContent.append(" ")
        allContent.append(extras.getCharSequence(Notification.EXTRA_TEXT) ?: "")
        allContent.append(" ")
        allContent.append(extras.getCharSequence(Notification.EXTRA_BIG_TEXT) ?: "")
        allContent.append(" ")
        allContent.append(extras.getCharSequence(Notification.EXTRA_SUMMARY_TEXT) ?: "")
        
        val lines = extras.getCharSequenceArray(Notification.EXTRA_TEXT_LINES)
        if (lines != null) {
            for (line in lines) {
                allContent.append(" ").append(line)
            }
        }
        
        val fullText = allContent.toString().lowercase()
        Log.d("NotifierPro", "Scanning for dynamic keywords in: $fullText")

        // Get keywords from storage
        val subjectKeywords = KeywordManager.getSubjectKeywords(this)
        val bodyKeywords = KeywordManager.getBodyKeywords(this)

        // Dynamic Scan
        val hasSubjectKey = subjectKeywords.any { fullText.contains(it.lowercase()) }
        val hasBodyKey = bodyKeywords.any { fullText.contains(it.lowercase()) }

        if (hasSubjectKey && hasBodyKey) {
            Log.d("NotifierPro", "🔥🔥 DYNAMIC MATCH! Launching alert...")
            handler.post {
                android.widget.Toast.makeText(this, "Order Detected! Launching Alert...", android.widget.Toast.LENGTH_SHORT).show()
            }
            // Use title as subject fallback for the UI
            val displayTitle = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString() ?: "New Order"
            triggerAlert(displayTitle, fullText.take(200))
            HistoryManager.addLog(this, displayTitle, "Match found in: $fullText")
        } else {
            Log.d("NotifierPro", "❌ Filter failed. hasSubjectKey=$hasSubjectKey, hasBodyKey=$hasBodyKey")
        }
    }

    private fun triggerAlert(subject: String, body: String) {
        val intent = Intent(this, AlertActivity::class.java).apply {
            putExtra("subject", subject)
            putExtra("body", body)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }

        // For Android 10+, starting activity from background requires a FullScreenIntent
        val pendingIntent = android.app.PendingIntent.getActivity(
            this, 0, intent,
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
        )

        val channelId = "order_alerts"
        val notificationManager = getSystemService(android.content.Context.NOTIFICATION_SERVICE) as android.app.NotificationManager

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = android.app.NotificationChannel(channelId, "Order Alerts", android.app.NotificationManager.IMPORTANCE_HIGH).apply {
                description = "Fullscreen alerts for new orders"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val notification = androidx.core.app.NotificationCompat.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle("ORDER ALERT!")
            .setContentText(subject)
            .setPriority(androidx.core.app.NotificationCompat.PRIORITY_MAX)
            .setCategory(androidx.core.app.NotificationCompat.CATEGORY_ALARM)
            .setFullScreenIntent(pendingIntent, true)
            .setOngoing(true) // Samsung loves ongoing notifications for importance
            .setAutoCancel(true)
            .build()

        notificationManager.notify(1002, notification)
        
        // Also try direct launch as fallback
        try { startActivity(intent) } catch (e: Exception) { e.printStackTrace() }
    }
}
