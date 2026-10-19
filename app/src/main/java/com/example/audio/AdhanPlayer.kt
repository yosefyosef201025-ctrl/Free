package com.example.audio

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.example.model.AlertMode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AdhanPlayer(private val context: Context) {

    private val scope = CoroutineScope(Dispatchers.Main)
    private var currentPlayJob: Job? = null

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentAlertMode = MutableStateFlow<AlertMode?>(null)
    val currentAlertMode: StateFlow<AlertMode?> = _currentAlertMode.asStateFlow()

    fun play(alertMode: AlertMode, triggerVibration: Boolean = true) {
        stop()

        if (triggerVibration || alertMode == AlertMode.VIBRATE) {
            vibrate(context, if (alertMode == AlertMode.VIBRATE) 1200 else 400)
        }

        if (alertMode == AlertMode.SILENT || alertMode == AlertMode.VIBRATE) {
            return
        }

        val style = when (alertMode) {
            AlertMode.ADHAN -> AdhanSoundGenerator.AlertStyle.ADHAN
            AlertMode.TAKBEER -> AdhanSoundGenerator.AlertStyle.TAKBEER
            AlertMode.CHIME -> AdhanSoundGenerator.AlertStyle.CHIME
            else -> AdhanSoundGenerator.AlertStyle.SHORT_BEEP
        }

        _isPlaying.value = true
        _currentAlertMode.value = alertMode

        currentPlayJob = scope.launch {
            AdhanSoundGenerator.playAlert(
                alertStyle = style,
                onPlaybackStarted = {
                    _isPlaying.value = true
                },
                onPlaybackFinished = {
                    _isPlaying.value = false
                    _currentAlertMode.value = null
                }
            )
        }
    }

    fun stop() {
        currentPlayJob?.cancel()
        currentPlayJob = null
        _isPlaying.value = false
        _currentAlertMode.value = null
    }

    companion object {
        fun vibrate(context: Context, durationMs: Long = 500) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                    vm?.defaultVibrator?.vibrate(
                        VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE)
                    )
                } else {
                    @Suppress("DEPRECATION")
                    val v = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                    @Suppress("DEPRECATION")
                    v?.vibrate(durationMs)
                }
            } catch (_: Exception) {
            }
        }
    }
}
