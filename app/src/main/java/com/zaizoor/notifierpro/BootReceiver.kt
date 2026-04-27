package com.zaizoor.notifierpro

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.d("NotifierPro", "Boot completed. Service checking...")
            // NotificationListenerService is automatically managed by Android, 
            // but we can trigger a check or a foreground service here if needed.
        }
    }
}
