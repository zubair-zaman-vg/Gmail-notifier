package com.zaizoor.notifierpro

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.*

class AlertActivity : AppCompatActivity() {

    private lateinit var flashlightManager: FlashlightManager
    private lateinit var ttsManager: TTSManager
    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        android.util.Log.d("NotifierPro", "AlertActivity launched successfully!")
        
        // Setup Window Flags for Background/Lockscreen launch
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                    or WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                    or WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                    or WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD)
        }

        setContentView(R.layout.activity_alert)

        flashlightManager = FlashlightManager(this)
        ttsManager = TTSManager(this)

        val subject = intent.getStringExtra("subject") ?: "New Order"
        val body = intent.getStringExtra("body") ?: "Check your Gmail for details."
        
        findViewById<TextView>(R.id.tvAlertSubject).text = subject
        findViewById<TextView>(R.id.tvAlertBody).text = body
        findViewById<TextView>(R.id.tvTime).text = SimpleDateFormat("MMM dd, yyyy - HH:mm:ss", Locale.getDefault()).format(Date())

        startAlertSequence()
        setupAnimations()
        setupButtons()
        startVibration()
    }

    private fun startVibration() {
        val vibrator = getSystemService(android.content.Context.VIBRATOR_SERVICE) as android.os.Vibrator
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            vibrator.vibrate(android.os.VibrationEffect.createWaveform(longArrayOf(0, 500, 200, 500, 200), 0))
        } else {
            vibrator.vibrate(longArrayOf(0, 500, 200, 500, 200), 0)
        }
    }

    private fun startAlertSequence() {
        // 1. Start Flashlight (increased to loop for 60s)
        flashlightManager.startBlinking(60) // 60 blinks approx 60 seconds
        
        // 2. Start Voice Loop (5 times, 1s delay)
        repeatVoice(5)
        
        // 3. Auto-stop everything sensory after 1 minute
        handler.postDelayed({
            stopSensoryAlerts()
        }, 60000)
    }

    private fun repeatVoice(count: Int) {
        if (count <= 0) return
        ttsManager.speak("Order placed")
        handler.postDelayed({
            repeatVoice(count - 1)
        }, 2000) // 1s speech + 1s delay = approx 2s cycle
    }

    private fun stopSensoryAlerts() {
        flashlightManager.stopBlinking()
        (getSystemService(android.content.Context.VIBRATOR_SERVICE) as android.os.Vibrator).cancel()
        ttsManager.shutdown() // Stop any remaining voice
        // Note: We do NOT call finish() here, so the popup stays open.
    }

    private fun setupAnimations() {
        val glowView = findViewById<View>(R.id.vGlow)
        val animator = ObjectAnimator.ofFloat(glowView, "alpha", 0.1f, 0.6f)
        animator.duration = 1000
        animator.repeatCount = ValueAnimator.INFINITE
        animator.repeatMode = ValueAnimator.REVERSE
        animator.start()
    }

    private fun setupButtons() {
        findViewById<Button>(R.id.btnDismiss).setOnClickListener {
            finish()
        }

        findViewById<Button>(R.id.btnSnooze).setOnClickListener {
            // Snooze for 1 minute: Simple way is to post a delayed trigger
            handler.postDelayed({
                val intent = Intent(this, AlertActivity::class.java).apply {
                    putExtras(this@AlertActivity.intent)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                startActivity(intent)
            }, 60000)
            finish()
        }

        findViewById<Button>(R.id.btnOpenGmail).setOnClickListener {
            val intent = packageManager.getLaunchIntentForPackage("com.google.android.gm")
            if (intent != null) {
                startActivity(intent)
            }
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        (getSystemService(android.content.Context.VIBRATOR_SERVICE) as android.os.Vibrator).cancel()
        flashlightManager.stopBlinking()
        ttsManager.shutdown()
        handler.removeCallbacksAndMessages(null)
    }
}
