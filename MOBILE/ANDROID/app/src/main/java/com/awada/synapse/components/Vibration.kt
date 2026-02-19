package com.awada.synapse.components

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

@Suppress("DEPRECATION")
fun vibrateStrongClick(context: Context) {
    val effect = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        VibrationEffect.createOneShot(30, VibrationEffect.DEFAULT_AMPLITUDE)
    } else {
        null
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        val v = vm?.defaultVibrator
        if (v != null) {
            if (effect != null) v.vibrate(effect) else v.vibrate(30)
        }
        return
    }

    val v = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator ?: return
    if (effect != null) v.vibrate(effect) else v.vibrate(30)
}

