package com.zaizoor.notifierpro

import android.content.Context
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraManager
import android.os.Handler
import android.os.Looper

class FlashlightManager(context: Context) {
    private val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    private var cameraId: String? = null
    private val handler = Handler(Looper.getMainLooper())
    private var isBlinking = false

    init {
        try {
            cameraId = cameraManager.cameraIdList[0] // Typically back camera
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun startBlinking(times: Int) {
        if (cameraId == null || isBlinking) return
        isBlinking = true
        var count = 0
        val runnable = object : Runnable {
            override fun run() {
                if (count < times * 2) {
                    try {
                        toggleTorch(count % 2 == 0)
                    } catch (e: CameraAccessException) {
                        isBlinking = false
                        return
                    }
                    count++
                    handler.postDelayed(this, 400)
                } else {
                    isBlinking = false
                    toggleTorch(false)
                }
            }
        }
        handler.post(runnable)
    }

    fun stopBlinking() {
        handler.removeCallbacksAndMessages(null)
        toggleTorch(false)
        isBlinking = false
    }

    private fun toggleTorch(enabled: Boolean) {
        cameraId?.let {
            try {
                cameraManager.setTorchMode(it, enabled)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
