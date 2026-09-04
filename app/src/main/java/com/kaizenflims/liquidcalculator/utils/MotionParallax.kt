package com.kaizenflims.liquidcalculator.utils

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner

@Composable
fun rememberMotionParallax(enabled: Boolean): State<Offset> {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val maximumOffset = with(LocalDensity.current) { 7f * density }
    val output = remember { mutableStateOf(Offset.Zero) }
    val sensorManager = remember {
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    }
    val accelerometer = remember {
        sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    }
    val listener = remember(maximumOffset) {
        object : SensorEventListener {
            private var lastUpdateNanos = 0L

            override fun onSensorChanged(event: SensorEvent) {
                if (event.timestamp - lastUpdateNanos < 32_000_000L) return
                lastUpdateNanos = event.timestamp
                val target = Offset(
                    x = (-event.values[0] / SensorManager.GRAVITY_EARTH * maximumOffset)
                        .coerceIn(-maximumOffset, maximumOffset),
                    y = (event.values[1] / SensorManager.GRAVITY_EARTH * maximumOffset)
                        .coerceIn(-maximumOffset, maximumOffset),
                )
                output.value = Offset(
                    x = output.value.x * 0.72f + target.x * 0.28f,
                    y = output.value.y * 0.72f + target.y * 0.28f,
                )
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
        }
    }

    DisposableEffect(enabled, lifecycleOwner, accelerometer, listener) {
        fun register() {
            if (enabled && accelerometer != null) {
                sensorManager.registerListener(
                    listener,
                    accelerometer,
                    SensorManager.SENSOR_DELAY_UI,
                )
            }
        }

        fun unregister() {
            sensorManager.unregisterListener(listener)
            output.value = Offset.Zero
        }

        val observer = object : DefaultLifecycleObserver {
            override fun onResume(owner: LifecycleOwner) = register()
            override fun onPause(owner: LifecycleOwner) = unregister()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        if (lifecycleOwner.lifecycle.currentState.isAtLeast(androidx.lifecycle.Lifecycle.State.RESUMED)) {
            register()
        }
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            unregister()
        }
    }
    return output
}

