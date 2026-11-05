package com.example.dialerapp

import android.content.Context
import android.os.PowerManager
import android.telecom.Call
import android.telecom.InCallService

class CallService : InCallService() {

    private var wakeLock: PowerManager.WakeLock? = null

    override fun onCallAdded(call: Call) {
        super.onCallAdded(call)
        manageProximityLock()
    }

    override fun onCallRemoved(call: Call) {
        super.onCallRemoved(call)
        releaseWakeLock()
    }

    private fun manageProximityLock() {
        val prefs = getSharedPreferences("dialer_settings", Context.MODE_PRIVATE)
        val isSensorDisabled = prefs.getBoolean("disable_proximity", false)

        if (isSensorDisabled) {
            // User requested to disable the sensor: keep the screen ON during calls
            releaseWakeLock()
        } else {
            // Default behavior: allow proximity sensor to turn screen off near ear
            val powerManager = getSystemService(Context.POWER_SERVICE) as? PowerManager
            if (powerManager?.isWakeLockLevelSupported(PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK) == true) {
                if (wakeLock == null) {
                    wakeLock = powerManager.newWakeLock(
                        PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK,
                        "DialerApp:ProximityLock"
                    )
                }
                if (wakeLock?.isHeld == false) {
                    wakeLock?.acquire()
                }
            }
        }
    }

    private fun releaseWakeLock() {
        try {
            if (wakeLock?.isHeld == true) {
                wakeLock?.release()
            }
        } catch (_: Exception) {}
        wakeLock = null
    }

    override fun onDestroy() {
        super.onDestroy()
        releaseWakeLock()
    }
}
