package com.kaizenflims.liquidcalculator.utils

import android.os.Build
import android.view.HapticFeedbackConstants
import android.view.View
import com.kaizenflims.liquidcalculator.glass.GlassButtonKind

fun performCalculatorHaptic(
    view: View,
    kind: GlassButtonKind,
    enabled: Boolean,
) {
    if (!enabled) return
    val feedback = when (kind) {
        GlassButtonKind.Number, GlassButtonKind.Scientific ->
            HapticFeedbackConstants.KEYBOARD_TAP
        GlassButtonKind.Utility, GlassButtonKind.Operator ->
            HapticFeedbackConstants.CLOCK_TICK
        GlassButtonKind.Equals ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                HapticFeedbackConstants.CONFIRM
            } else {
                HapticFeedbackConstants.VIRTUAL_KEY
            }
    }
    view.performHapticFeedback(feedback)
}

