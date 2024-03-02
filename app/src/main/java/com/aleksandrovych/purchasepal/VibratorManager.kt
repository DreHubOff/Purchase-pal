package com.aleksandrovych.purchasepal

import android.content.Context
import android.os.VibrationEffect
import android.os.Vibrator
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class VibratorManager @Inject constructor(@ApplicationContext context: Context) {

    val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

    fun vibrateDevice() {
        // Check if the device supports vibration
        if (vibrator.hasVibrator()) {
            // Vibrate for 500 milliseconds
            val vibrationEffect =
                VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE)
            vibrator.vibrate(vibrationEffect)
        }
    }
}