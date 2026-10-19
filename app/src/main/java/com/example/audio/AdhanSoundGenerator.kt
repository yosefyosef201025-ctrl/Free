package com.example.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.*

object AdhanSoundGenerator {

    private const val SAMPLE_RATE = 22050

    /**
     * Synthesizes audio samples for the specified alert style.
     */
    suspend fun playAlert(
        alertStyle: AlertStyle,
        onPlaybackStarted: () -> Unit = {},
        onPlaybackFinished: () -> Unit = {}
    ) = withContext(Dispatchers.Default) {
        val samples = when (alertStyle) {
            AlertStyle.ADHAN -> generateAdhanMelody()
            AlertStyle.TAKBEER -> generateTakbeerMelody()
            AlertStyle.CHIME -> generateSpiritualChime()
            AlertStyle.SHORT_BEEP -> generateShortBeep()
        }

        playPcmSamples(samples, onPlaybackStarted, onPlaybackFinished)
    }

    enum class AlertStyle {
        ADHAN,
        TAKBEER,
        CHIME,
        SHORT_BEEP
    }

    private suspend fun playPcmSamples(
        samples: ShortArray,
        onPlaybackStarted: () -> Unit,
        onPlaybackFinished: () -> Unit
    ) {
        val minBufferSize = AudioTrack.getMinBufferSize(
            SAMPLE_RATE,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )
        val bufferSize = max(minBufferSize, samples.size * 2)

        val audioTrack = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(SAMPLE_RATE)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()
            )
            .setBufferSizeInBytes(bufferSize)
            .setTransferMode(AudioTrack.MODE_STREAM)
            .build()

        try {
            audioTrack.play()
            onPlaybackStarted()
            audioTrack.write(samples, 0, samples.size)
            // Wait brief moment for remaining buffer to flush
            kotlinx.coroutines.delay(100)
        } catch (_: Exception) {
        } finally {
            try {
                audioTrack.stop()
                audioTrack.release()
            } catch (_: Exception) {
            }
            onPlaybackFinished()
        }
    }

    /**
     * Generates an atmospheric, sacred Adhan melody (Bayati maqam).
     * Phrases: Al-la-hu Ak-bar (D4 - F4 - G4 - F4 - E4 - D4)
     */
    private fun generateAdhanMelody(): ShortArray {
        val d4 = 293.66
        val eb4 = 311.13
        val e4HalfFlat = 320.0 // Bayati quarter-tone characteristic
        val f4 = 349.23
        val g4 = 392.00
        val a4 = 440.00

        // Sequence of notes: (freq, durationMs, envelopeType)
        val melody = listOf(
            // Takbeer 1: Allahu Akbar
            Note(d4, 700, 0.4),
            Note(f4, 900, 0.5),
            Note(g4, 1100, 0.6),
            Note(f4, 600, 0.4),
            Note(e4HalfFlat, 700, 0.35),
            Note(d4, 1200, 0.3),
            Note(0.0, 300, 0.0), // pause

            // Takbeer 2: Allahu Akbar
            Note(d4, 600, 0.4),
            Note(f4, 800, 0.55),
            Note(g4, 1200, 0.65),
            Note(a4, 900, 0.5),
            Note(g4, 700, 0.4),
            Note(f4, 800, 0.35),
            Note(d4, 1400, 0.3),
            Note(0.0, 400, 0.0), // pause

            // Ash-hadu an la ilaha illallah
            Note(f4, 700, 0.45),
            Note(g4, 800, 0.5),
            Note(a4, 1300, 0.6),
            Note(g4, 800, 0.45),
            Note(f4, 900, 0.4),
            Note(e4HalfFlat, 800, 0.35),
            Note(d4, 1800, 0.25)
        )

        return renderNotesToPcm(melody)
    }

    /**
     * Takbeerat melody: "Allahu Akbar Allahu Akbar, La Ilaha Illallah"
     */
    private fun generateTakbeerMelody(): ShortArray {
        val c4 = 261.63
        val e4 = 329.63
        val g4 = 392.00
        val c5 = 523.25

        val notes = listOf(
            Note(c4, 500, 0.5),
            Note(e4, 500, 0.6),
            Note(g4, 900, 0.7),
            Note(0.0, 150, 0.0),
            Note(c4, 500, 0.5),
            Note(e4, 500, 0.6),
            Note(g4, 900, 0.7),
            Note(0.0, 200, 0.0),
            Note(c5, 700, 0.6),
            Note(g4, 600, 0.5),
            Note(e4, 600, 0.4),
            Note(c4, 1200, 0.3)
        )
        return renderNotesToPcm(notes)
    }

    /**
     * Serene bell chime with warm harmonic decay.
     */
    private fun generateSpiritualChime(): ShortArray {
        val root = 528.0 // Solfeggio / meditative tone
        val totalMs = 3500
        val totalSamples = (SAMPLE_RATE * (totalMs / 1000.0)).toInt()
        val result = ShortArray(totalSamples)

        for (i in 0 until totalSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            val envelope = exp(-1.2 * t) // smooth decay
            // Harmonics
            val wave = 0.55 * sin(2 * Math.PI * root * t) +
                    0.25 * sin(2 * Math.PI * (root * 2.0) * t) +
                    0.12 * sin(2 * Math.PI * (root * 3.01) * t) +
                    0.08 * sin(2 * Math.PI * (root * 4.16) * t)
            val sample = (wave * envelope * 28000.0).coerceIn(-32767.0, 32767.0).toInt().toShort()
            result[i] = sample
        }
        return result
    }

    /**
     * Short notification chime beep.
     */
    private fun generateShortBeep(): ShortArray {
        val notes = listOf(
            Note(587.33, 200, 0.5), // D5
            Note(880.00, 450, 0.6)  // A5
        )
        return renderNotesToPcm(notes)
    }

    private data class Note(val freqHz: Double, val durationMs: Int, val maxVolume: Double)

    private fun renderNotesToPcm(notes: List<Note>): ShortArray {
        val totalSamples = notes.sumOf { (SAMPLE_RATE * (it.durationMs / 1000.0)).toInt() }
        val output = ShortArray(totalSamples)
        var offset = 0

        for (note in notes) {
            val count = (SAMPLE_RATE * (note.durationMs / 1000.0)).toInt()
            if (note.freqHz == 0.0) {
                // Silence / Pause
                for (i in 0 until count) {
                    output[offset + i] = 0
                }
            } else {
                val attackSamples = (SAMPLE_RATE * 0.06).toInt()
                val releaseSamples = (SAMPLE_RATE * 0.12).toInt()

                for (i in 0 until count) {
                    val t = i.toDouble() / SAMPLE_RATE

                    // ADSR Envelope
                    val env = when {
                        i < attackSamples -> (i.toDouble() / attackSamples)
                        i > count - releaseSamples -> ((count - i).toDouble() / releaseSamples)
                        else -> 1.0 - (0.2 * (i - attackSamples) / count)
                    }

                    // Warm vocal overtone synthesis (fundamental + 2nd + 3rd harmonics + subtle vibrato)
                    val vibrato = 1.0 + 0.006 * sin(2 * Math.PI * 5.2 * t)
                    val currentFreq = note.freqHz * vibrato
                    val fundamental = sin(2 * Math.PI * currentFreq * t)
                    val h2 = 0.35 * sin(2 * Math.PI * (currentFreq * 2.0) * t)
                    val h3 = 0.18 * sin(2 * Math.PI * (currentFreq * 3.0) * t)
                    val h4 = 0.08 * sin(2 * Math.PI * (currentFreq * 4.0) * t)

                    val mixed = (fundamental + h2 + h3 + h4) * env * note.maxVolume
                    val sample = (mixed * 30000.0).coerceIn(-32767.0, 32767.0).toInt().toShort()
                    output[offset + i] = sample
                }
            }
            offset += count
        }
        return output
    }
}
