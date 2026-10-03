package com.example.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.sin
import kotlin.random.Random

class SoundEngine {
    private val sampleRate = 44100
    private var sfxTrack: AudioTrack? = null
    private var bgmTrack: AudioTrack? = null
    private var bgmJob: Job? = null

    var sfxEnabled: Boolean = true
    var musicEnabled: Boolean = true

    init {
        try {
            val minBuf = AudioTrack.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            )
            val audioAttrs = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
            val format = AudioFormat.Builder()
                .setSampleRate(sampleRate)
                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                .build()

            sfxTrack = AudioTrack.Builder()
                .setAudioAttributes(audioAttrs)
                .setAudioFormat(format)
                .setBufferSizeInBytes(maxOf(minBuf * 2, 8192))
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build()

            sfxTrack?.play()
        } catch (_: Exception) {}
    }

    fun playJump() {
        if (!sfxEnabled) return
        CoroutineScope(Dispatchers.Default).launch {
            // Rising pitch sweep 280Hz -> 650Hz in 80ms
            val durationMs = 80
            val numSamples = (sampleRate * (durationMs / 1000.0)).toInt()
            val buffer = ShortArray(numSamples)
            var phase = 0.0
            for (i in 0 until numSamples) {
                val t = i.toDouble() / numSamples
                val freq = 280.0 + (650.0 - 280.0) * t
                val env = 1.0 - (t * 0.4)
                phase += 2.0 * PI * freq / sampleRate
                val sample = sin(phase) * env * 18000
                buffer[i] = sample.toInt().coerceIn(-32767, 32767).toShort()
            }
            writeSfx(buffer)
        }
    }

    fun playPad() {
        if (!sfxEnabled) return
        CoroutineScope(Dispatchers.Default).launch {
            // Springy double chirp 350Hz -> 850Hz in 120ms
            val durationMs = 120
            val numSamples = (sampleRate * (durationMs / 1000.0)).toInt()
            val buffer = ShortArray(numSamples)
            var phase = 0.0
            for (i in 0 until numSamples) {
                val t = i.toDouble() / numSamples
                val freq = 350.0 + (500.0 * sin(t * PI))
                val env = (1.0 - t) * 0.9
                phase += 2.0 * PI * freq / sampleRate
                val sample = sin(phase) * env * 22000
                buffer[i] = sample.toInt().coerceIn(-32767, 32767).toShort()
            }
            writeSfx(buffer)
        }
    }

    fun playOrb() {
        if (!sfxEnabled) return
        CoroutineScope(Dispatchers.Default).launch {
            // High crystalline chime 880Hz + 1320Hz harmonic in 100ms
            val durationMs = 100
            val numSamples = (sampleRate * (durationMs / 1000.0)).toInt()
            val buffer = ShortArray(numSamples)
            var phase1 = 0.0
            var phase2 = 0.0
            for (i in 0 until numSamples) {
                val t = i.toDouble() / numSamples
                val env = (1.0 - t) * (1.0 - t)
                phase1 += 2.0 * PI * 880.0 / sampleRate
                phase2 += 2.0 * PI * 1320.0 / sampleRate
                val sample = (sin(phase1) * 0.7 + sin(phase2) * 0.5) * env * 20000
                buffer[i] = sample.toInt().coerceIn(-32767, 32767).toShort()
            }
            writeSfx(buffer)
        }
    }

    fun playGravityFlip() {
        if (!sfxEnabled) return
        CoroutineScope(Dispatchers.Default).launch {
            // Warp glide 700Hz down to 240Hz
            val durationMs = 110
            val numSamples = (sampleRate * (durationMs / 1000.0)).toInt()
            val buffer = ShortArray(numSamples)
            var phase = 0.0
            for (i in 0 until numSamples) {
                val t = i.toDouble() / numSamples
                val freq = 700.0 - (460.0 * t)
                val env = 1.0 - t
                phase += 2.0 * PI * freq / sampleRate
                val sample = (sin(phase) + 0.3 * sin(phase * 2.0)) * env * 19000
                buffer[i] = sample.toInt().coerceIn(-32767, 32767).toShort()
            }
            writeSfx(buffer)
        }
    }

    fun playCoin() {
        if (!sfxEnabled) return
        CoroutineScope(Dispatchers.Default).launch {
            // Classic 2-tone secret coin pickup: 987Hz (B5) -> 1318Hz (E6)
            val durationMs = 160
            val half = durationMs / 2
            val numSamples = (sampleRate * (durationMs / 1000.0)).toInt()
            val buffer = ShortArray(numSamples)
            var phase = 0.0
            for (i in 0 until numSamples) {
                val currentMs = (i.toDouble() / sampleRate) * 1000.0
                val freq = if (currentMs < half) 987.0 else 1318.0
                val env = 1.0 - (currentMs / durationMs) * 0.6
                phase += 2.0 * PI * freq / sampleRate
                val sample = sin(phase) * env * 22000
                buffer[i] = sample.toInt().coerceIn(-32767, 32767).toShort()
            }
            writeSfx(buffer)
        }
    }

    fun playCrash() {
        if (!sfxEnabled) return
        CoroutineScope(Dispatchers.Default).launch {
            // Crunchy explosive white noise burst + punchy low thud
            val durationMs = 180
            val numSamples = (sampleRate * (durationMs / 1000.0)).toInt()
            val buffer = ShortArray(numSamples)
            var lowPhase = 0.0
            for (i in 0 until numSamples) {
                val t = i.toDouble() / numSamples
                val env = (1.0 - t) * (1.0 - t)
                val noise = (Random.nextDouble() * 2.0 - 1.0)
                val lowFreq = 140.0 * (1.0 - t * 0.7)
                lowPhase += 2.0 * PI * lowFreq / sampleRate
                val sample = (noise * 0.75 + sin(lowPhase) * 0.5) * env * 26000
                buffer[i] = sample.toInt().coerceIn(-32767, 32767).toShort()
            }
            writeSfx(buffer)
        }
    }

    fun playCheckpoint() {
        if (!sfxEnabled) return
        CoroutineScope(Dispatchers.Default).launch {
            val durationMs = 90
            val numSamples = (sampleRate * (durationMs / 1000.0)).toInt()
            val buffer = ShortArray(numSamples)
            var phase = 0.0
            for (i in 0 until numSamples) {
                val t = i.toDouble() / numSamples
                val env = (1.0 - t)
                phase += 2.0 * PI * 1046.5 / sampleRate // C6
                val sample = sin(phase) * env * 17000
                buffer[i] = sample.toInt().coerceIn(-32767, 32767).toShort()
            }
            writeSfx(buffer)
        }
    }

    fun playWin() {
        if (!sfxEnabled) return
        CoroutineScope(Dispatchers.Default).launch {
            // Victorious fanfare arpeggio: C5, E5, G5, C6
            val notes = doubleArrayOf(523.25, 659.25, 783.99, 1046.50)
            val noteDurationMs = 120
            val totalSamples = (sampleRate * ((notes.size * noteDurationMs) / 1000.0)).toInt()
            val buffer = ShortArray(totalSamples)
            var phase = 0.0
            for (n in notes.indices) {
                val freq = notes[n]
                val noteSamples = (sampleRate * (noteDurationMs / 1000.0)).toInt()
                val offset = n * noteSamples
                for (i in 0 until noteSamples) {
                    val t = i.toDouble() / noteSamples
                    val env = 1.0 - (t * 0.3)
                    phase += 2.0 * PI * freq / sampleRate
                    val sample = sin(phase) * env * 22000
                    val idx = offset + i
                    if (idx < buffer.size) {
                        buffer[idx] = sample.toInt().coerceIn(-32767, 32767).toShort()
                    }
                }
            }
            writeSfx(buffer)
        }
    }

    private fun writeSfx(buffer: ShortArray) {
        try {
            sfxTrack?.write(buffer, 0, buffer.size)
        } catch (_: Exception) {}
    }

    /**
     * Real-time procedural rhythm soundtrack synthesizer.
     * Generates driving techno/electronic music synchronized with level BPM!
     */
    fun startBgm(bpm: Int = 135) {
        stopBgm()
        if (!musicEnabled) return

        bgmJob = CoroutineScope(Dispatchers.Default).launch {
            try {
                val minBuf = AudioTrack.getMinBufferSize(
                    sampleRate,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT
                )
                val audioAttrs = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
                val format = AudioFormat.Builder()
                    .setSampleRate(sampleRate)
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()

                val track = AudioTrack.Builder()
                    .setAudioAttributes(audioAttrs)
                    .setAudioFormat(format)
                    .setBufferSizeInBytes(maxOf(minBuf * 4, 16384))
                    .setTransferMode(AudioTrack.MODE_STREAM)
                    .build()

                bgmTrack = track
                track.play()

                val beatDuration = 60.0 / bpm
                val sixteenthDuration = beatDuration / 4.0
                val samplesPerSixteenth = (sampleRate * sixteenthDuration).toInt()
                val buffer = ShortArray(samplesPerSixteenth)

                var step = 0
                val bassNotesAm = doubleArrayOf(110.0, 110.0, 130.81, 146.83) // A, A, C, D
                val bassNotesF = doubleArrayOf(87.31, 87.31, 110.0, 130.81)    // F, F, A, C
                val bassNotesC = doubleArrayOf(130.81, 130.81, 146.83, 164.81) // C, C, D, E
                val bassNotesG = doubleArrayOf(98.0, 98.0, 123.47, 146.83)     // G, G, B, D

                var bassPhase = 0.0
                var kickPhase = 0.0
                var leadPhase = 0.0

                while (isActive && musicEnabled) {
                    val bar = (step / 16) % 4
                    val stepInBar = step % 16
                    val currentScale = when (bar) {
                        0 -> bassNotesAm
                        1 -> bassNotesF
                        2 -> bassNotesC
                        else -> bassNotesG
                    }
                    val bassFreq = currentScale[stepInBar % 4]
                    val isKick = (stepInBar % 4 == 0) // Kick on quarter notes
                    val isHiHat = (stepInBar % 2 == 1) // Hi-hat on 8th off-beats
                    val isSnare = (stepInBar == 4 || stepInBar == 12)

                    // Arpeggio lead note
                    val leadFreq = bassFreq * (if ((step % 3) == 0) 4.0 else if ((step % 2) == 0) 3.0 else 2.5)

                    for (i in 0 until samplesPerSixteenth) {
                        val t = i.toDouble() / samplesPerSixteenth
                        var mixedSample = 0.0

                        // 1. Kick Drum
                        if (isKick) {
                            val kickEnv = (1.0 - t) * (1.0 - t)
                            val kFreq = 130.0 * (1.0 - t * 0.7) + 35.0
                            kickPhase += 2.0 * PI * kFreq / sampleRate
                            mixedSample += sin(kickPhase) * kickEnv * 12000.0
                        }

                        // 2. Snare / Clack
                        if (isSnare) {
                            val snareEnv = (1.0 - t) * (1.0 - t)
                            val noise = (Random.nextDouble() * 2.0 - 1.0)
                            mixedSample += noise * snareEnv * 8500.0
                        }

                        // 3. Hi-Hat
                        if (isHiHat) {
                            val hatEnv = (1.0 - t) * 0.7
                            val noise = (Random.nextDouble() * 2.0 - 1.0)
                            mixedSample += noise * hatEnv * 4000.0
                        }

                        // 4. Bass synth (sawtooth-like)
                        val bassEnv = 1.0 - (t * 0.4)
                        bassPhase += 2.0 * PI * bassFreq / sampleRate
                        val saw = ((bassPhase / PI) % 2.0) - 1.0
                        mixedSample += saw * bassEnv * 6500.0

                        // 5. Lead arpeggio
                        leadPhase += 2.0 * PI * leadFreq / sampleRate
                        val leadEnv = (1.0 - t) * 0.8
                        mixedSample += sin(leadPhase) * leadEnv * 4500.0

                        buffer[i] = mixedSample.toInt().coerceIn(-32767, 32767).toShort()
                    }

                    track.write(buffer, 0, buffer.size)
                    step++
                }
            } catch (_: Exception) {}
        }
    }

    fun stopBgm() {
        bgmJob?.cancel()
        bgmJob = null
        try {
            bgmTrack?.pause()
            bgmTrack?.flush()
            bgmTrack?.stop()
            bgmTrack?.release()
        } catch (_: Exception) {}
        bgmTrack = null
    }

    fun release() {
        stopBgm()
        try {
            sfxTrack?.stop()
            sfxTrack?.release()
        } catch (_: Exception) {}
        sfxTrack = null
    }
}
