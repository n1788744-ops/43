package com.example.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin
import kotlin.random.Random

class HorrorSoundEngine(private val context: Context) {

    private val sampleRate = 22050
    private val bufferSamples = 1024

    @Volatile
    private var running = false

    @Volatile
    private var paused = false

    @Volatile
    private var masterVolume = 0.85f

    @Volatile
    private var witchProximity = 0f // 0f..1f

    @Volatile
    private var inGameplay = false

    @Volatile
    private var menuAtmosphere = true

    @Volatile
    private var huntActive = false

    @Volatile
    private var huntProgress = 0f // 0f..1f (louder near rescue)

    private var audioThread: Thread? = null
    private val sfxQueue = ConcurrentLinkedQueue<ActiveSfx>()

    private val vibrator: Vibrator? = try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            (context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager)?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    } catch (_: Exception) {
        null
    }

    private data class ActiveSfx(
        val type: SfxType,
        var sampleIndex: Int = 0,
        val totalSamples: Int,
        val gain: Float = 1f
    )

    private enum class SfxType {
        FOOTSTEP_WALK,
        FOOTSTEP_SPRINT,
        PICKUP,
        UNLOCK,
        LOCKED,
        FLASHLIGHT,
        SALT_BANISH,
        WITCH_ALERT,
        PUZZLE_OK,
        PUZZLE_ERR,
        JUMPSCARE,
        DOOR_CREAK,
        WEAPON_SWING,
        CROSSBOW_SHOT,
        WEAPON_HIT,
        CAMERA_SHUTTER,
        MIRROR_JINGLE,
        CAGE_SLAM,
        PHONE_DIAL,
        BREAKER_CLUNK
    }

    fun start() {
        if (running) return
        running = true
        audioThread = Thread({
            runAudioLoop()
        }, "WitchHorrorAudio").apply {
            isDaemon = true
            start()
        }
    }

    fun stop() {
        running = false
        audioThread?.interrupt()
        audioThread = null
    }

    fun setPaused(isPaused: Boolean) {
        paused = isPaused
    }

    fun setVolume(volume: Float) {
        masterVolume = volume.coerceIn(0f, 1f)
    }

    fun setGameplayContext(isInGame: Boolean, proximity: Float) {
        inGameplay = isInGame
        menuAtmosphere = !isInGame
        witchProximity = proximity.coerceIn(0f, 1f)
        if (!isInGame) {
            huntActive = false
            huntProgress = 0f
        }
    }

    fun setHuntState(active: Boolean, progress01: Float) {
        huntActive = active
        huntProgress = progress01.coerceIn(0f, 1f)
    }

    fun playFootstep(sprint: Boolean) {
        sfxQueue.add(
            ActiveSfx(
                type = if (sprint) SfxType.FOOTSTEP_SPRINT else SfxType.FOOTSTEP_WALK,
                totalSamples = (sampleRate * 0.11f).toInt(),
                gain = if (sprint) 0.48f else 0.28f
            )
        )
    }

    fun playPickup() {
        sfxQueue.add(ActiveSfx(SfxType.PICKUP, totalSamples = (sampleRate * 0.45f).toInt(), gain = 0.55f))
    }

    fun playUnlock() {
        sfxQueue.add(ActiveSfx(SfxType.UNLOCK, totalSamples = (sampleRate * 0.65f).toInt(), gain = 0.65f))
    }

    fun playLockedRattle() {
        sfxQueue.add(ActiveSfx(SfxType.LOCKED, totalSamples = (sampleRate * 0.28f).toInt(), gain = 0.45f))
    }

    fun playDoorCreak() {
        sfxQueue.add(ActiveSfx(SfxType.DOOR_CREAK, totalSamples = (sampleRate * 0.42f).toInt(), gain = 0.48f))
    }

    fun playWeaponSwing() {
        sfxQueue.add(ActiveSfx(SfxType.WEAPON_SWING, totalSamples = (sampleRate * 0.20f).toInt(), gain = 0.55f))
    }

    fun playCrossbowShot() {
        sfxQueue.add(ActiveSfx(SfxType.CROSSBOW_SHOT, totalSamples = (sampleRate * 0.26f).toInt(), gain = 0.68f))
        vibratePulse(40L, 140)
    }

    fun playWeaponHit() {
        sfxQueue.add(ActiveSfx(SfxType.WEAPON_HIT, totalSamples = (sampleRate * 0.50f).toInt(), gain = 0.78f))
        vibratePulse(75L, 210)
    }

    fun playFlashlightClick() {
        sfxQueue.add(ActiveSfx(SfxType.FLASHLIGHT, totalSamples = (sampleRate * 0.06f).toInt(), gain = 0.45f))
    }

    fun playSaltBanish() {
        sfxQueue.add(ActiveSfx(SfxType.SALT_BANISH, totalSamples = (sampleRate * 0.85f).toInt(), gain = 0.75f))
        vibratePulse(60L, 180)
    }

    fun playWitchAlert() {
        sfxQueue.add(ActiveSfx(SfxType.WITCH_ALERT, totalSamples = (sampleRate * 0.9f).toInt(), gain = 0.7f))
        vibratePulse(85L, 210)
    }

    fun playPuzzleResult(success: Boolean) {
        sfxQueue.add(
            ActiveSfx(
                type = if (success) SfxType.PUZZLE_OK else SfxType.PUZZLE_ERR,
                totalSamples = (sampleRate * 0.55f).toInt(),
                gain = 0.6f
            )
        )
    }

    fun playJumpScare() {
        sfxQueue.add(ActiveSfx(SfxType.JUMPSCARE, totalSamples = (sampleRate * 1.15f).toInt(), gain = 0.95f))
        vibratePulse(220L, 255)
    }

    fun playCameraShutter() {
        sfxQueue.add(ActiveSfx(SfxType.CAMERA_SHUTTER, totalSamples = (sampleRate * 0.24f).toInt(), gain = 0.72f))
    }

    fun playMirrorJingle() {
        sfxQueue.add(ActiveSfx(SfxType.MIRROR_JINGLE, totalSamples = (sampleRate * 0.28f).toInt(), gain = 0.35f))
    }

    fun playCageTrap() {
        sfxQueue.add(ActiveSfx(SfxType.CAGE_SLAM, totalSamples = (sampleRate * 0.85f).toInt(), gain = 0.88f))
        vibratePulse(180L, 240)
    }

    fun playPhoneDial() {
        sfxQueue.add(ActiveSfx(SfxType.PHONE_DIAL, totalSamples = (sampleRate * 0.75f).toInt(), gain = 0.65f))
    }

    fun playBreakerSwitch() {
        sfxQueue.add(ActiveSfx(SfxType.BREAKER_CLUNK, totalSamples = (sampleRate * 0.32f).toInt(), gain = 0.68f))
        vibratePulse(50L, 170)
    }

    private fun vibratePulse(durationMs: Long, amplitude: Int) {
        try {
            val vib = vibrator ?: return
            if (!vib.hasVibrator()) return
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vib.vibrate(VibrationEffect.createOneShot(durationMs, amplitude.coerceIn(1, 255)))
            } else {
                @Suppress("DEPRECATION")
                vib.vibrate(durationMs)
            }
        } catch (_: Exception) {
        }
    }

    private fun runAudioLoop() {
        val minBuf = AudioTrack.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        ).coerceAtLeast(bufferSamples * 2)

        val track = try {
            AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(minBuf * 2)
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build()
        } catch (_: Exception) {
            return
        }

        try {
            track.play()
        } catch (_: Exception) {
            track.release()
            return
        }

        val pcmBuffer = ShortArray(bufferSamples)
        var phase1 = 0.0
        var phase2 = 0.0
        var phase3 = 0.0
        var tensionPhaseA = 0.0
        var tensionPhaseB = 0.0
        var lfoPhase = 0.0
        var sirenPhase = 0.0
        var sirenSweep = 0.0
        var windFilter = 0f
        var heartbeatTimer = 0f
        var lastHapticBeat = 0L

        val rng = Random(System.currentTimeMillis())
        val twoPi = 2.0 * PI

        while (running) {
            if (paused || masterVolume <= 0.01f) {
                pcmBuffer.fill(0)
                try {
                    track.write(pcmBuffer, 0, pcmBuffer.size)
                    Thread.sleep(25)
                } catch (_: Exception) {
                    break
                }
                continue
            }

            val prox = witchProximity
            val isGame = inGameplay
            val bpm = 58f + prox * 98f
            val beatPeriod = 60f / bpm
            val dt = 1f / sampleRate

            for (i in 0 until bufferSamples) {
                phase1 = (phase1 + twoPi * 44.0 / sampleRate) % twoPi
                phase2 = (phase2 + twoPi * 45.4 / sampleRate) % twoPi
                phase3 = (phase3 + twoPi * 66.0 / sampleRate) % twoPi
                lfoPhase = (lfoPhase + twoPi * 0.16 / sampleRate) % twoPi

                val lfo = 0.5f + 0.5f * sin(lfoPhase).toFloat()
                // Creepy sub-bass drone
                val drone = (sin(phase1) * 0.42 + sin(phase2) * 0.35 + sin(phase3) * 0.23).toFloat() *
                    (0.11f + prox * 0.14f)

                // Ghostly wind whisper (low-passed white noise modulated by LFO)
                val white = rng.nextFloat() * 2f - 1f
                windFilter = windFilter * 0.965f + white * 0.035f
                val windGain = if (isGame) (0.14f + lfo * 0.12f + prox * 0.18f) else (0.12f + lfo * 0.08f)
                val wind = windFilter * windGain

                // Eerie dissonant minor-second string whine when witch approaches
                tensionPhaseA = (tensionPhaseA + twoPi * (415.0 + prox * 35.0) / sampleRate) % twoPi
                tensionPhaseB = (tensionPhaseB + twoPi * (440.0 + prox * 42.0) / sampleRate) % twoPi
                val tensionEnv = if (isGame && prox > 0.25f) ((prox - 0.25f) / 0.75f) else 0f
                val tension = ((sin(tensionPhaseA) + sin(tensionPhaseB)) * 0.5).toFloat() *
                    (tensionEnv * tensionEnv * 0.16f) * (0.6f + 0.4f * lfo)

                // Dynamic Heartbeat in gameplay
                var heart = 0f
                if (isGame && prox > 0.12f) {
                    heartbeatTimer += dt
                    if (heartbeatTimer >= beatPeriod) {
                        heartbeatTimer -= beatPeriod
                        val now = System.currentTimeMillis()
                        if (prox > 0.58f && now - lastHapticBeat > 320L) {
                            lastHapticBeat = now
                            vibratePulse(32L, (70 + (prox * 120)).toInt())
                        }
                    }
                    // Lub at t=0..0.09, Dub at t=0.14..0.24
                    if (heartbeatTimer < 0.09f) {
                        val t = heartbeatTimer
                        heart = sin(twoPi * 54.0 * t).toFloat() * exp(-t * 32f) * (0.28f + prox * 0.55f)
                    } else if (heartbeatTimer in 0.14f..0.24f) {
                        val t = heartbeatTimer - 0.14f
                        heart = sin(twoPi * 48.0 * t).toFloat() * exp(-t * 36f) * (0.22f + prox * 0.42f)
                    }
                }

                // Growing distant-to-close police sirens during 3:00 Hunt Mode
                var siren = 0f
                if (isGame && huntActive) {
                    sirenSweep = (sirenSweep + twoPi * 0.42 / sampleRate) % twoPi
                    val sirenFreq = 580.0 + 240.0 * sin(sirenSweep)
                    sirenPhase = (sirenPhase + twoPi * sirenFreq / sampleRate) % twoPi
                    val sirenVol = 0.03f + huntProgress * 0.14f
                    siren = sin(sirenPhase).toFloat() * sirenVol
                }

                // Mix active one-shot SFX
                var sfxMix = 0f
                val iter = sfxQueue.iterator()
                while (iter.hasNext()) {
                    val sfx = iter.next()
                    if (sfx.sampleIndex >= sfx.totalSamples) {
                        iter.remove()
                        continue
                    }
                    val t = sfx.sampleIndex.toFloat() / sampleRate
                    val progress = sfx.sampleIndex.toFloat() / sfx.totalSamples
                    sfxMix += sampleSfx(sfx.type, t, progress, rng) * sfx.gain
                    sfx.sampleIndex++
                }

                val sample = ((drone + wind + tension + heart + siren + sfxMix) * masterVolume)
                    .coerceIn(-0.98f, 0.98f)
                pcmBuffer[i] = (sample * Short.MAX_VALUE).toInt().toShort()
            }

            try {
                track.write(pcmBuffer, 0, pcmBuffer.size)
            } catch (_: Exception) {
                break
            }
        }

        try {
            track.stop()
            track.release()
        } catch (_: Exception) {
        }
    }

    private fun sampleSfx(type: SfxType, t: Float, progress: Float, rng: Random): Float {
        val env = (1f - progress)
        return when (type) {
            SfxType.FOOTSTEP_WALK -> {
                val thump = sin(2.0 * PI * 95.0 * t).toFloat() * exp(-t * 38f)
                val grit = (rng.nextFloat() * 2f - 1f) * 0.35f * exp(-t * 45f)
                (thump * 0.65f + grit)
            }
            SfxType.FOOTSTEP_SPRINT -> {
                val thump = sin(2.0 * PI * 120.0 * t).toFloat() * exp(-t * 34f)
                val grit = (rng.nextFloat() * 2f - 1f) * 0.5f * exp(-t * 40f)
                (thump * 0.7f + grit)
            }
            SfxType.PICKUP -> {
                val f1 = sin(2.0 * PI * 659.25 * t).toFloat()
                val f2 = sin(2.0 * PI * 987.77 * t).toFloat()
                (f1 * 0.5f + f2 * 0.5f) * exp(-t * 6.5f)
            }
            SfxType.UNLOCK -> {
                val clunk = sin(2.0 * PI * 145.0 * t).toFloat() * exp(-t * 18f)
                val chord = (sin(2.0 * PI * 523.25 * t) + sin(2.0 * PI * 659.25 * t)).toFloat() * 0.35f * exp(-t * 4.5f)
                clunk + chord
            }
            SfxType.LOCKED -> {
                val rattle = sin(2.0 * PI * 210.0 * t).toFloat() * sin(2.0 * PI * 26.0 * t).toFloat()
                rattle * exp(-t * 11f)
            }
            SfxType.FLASHLIGHT -> {
                (rng.nextFloat() * 2f - 1f) * exp(-t * 95f) * 0.8f
            }
            SfxType.SALT_BANISH -> {
                val shimmer = (sin(2.0 * PI * 880.0 * t) + sin(2.0 * PI * 1318.0 * t)).toFloat() * 0.45f
                val hiss = (rng.nextFloat() * 2f - 1f) * 0.45f
                (shimmer + hiss) * env * exp(-t * 2.2f)
            }
            SfxType.WITCH_ALERT -> {
                val screech = sin(2.0 * PI * (720.0 - 280.0 * progress) * t).toFloat() * 0.45f
                val sub = sin(2.0 * PI * 82.0 * t).toFloat() * 0.5f
                (screech + sub) * env
            }
            SfxType.PUZZLE_OK -> {
                val freq = if (progress < 0.45f) 523.25 else 783.99
                sin(2.0 * PI * freq * t).toFloat() * exp(-t * 4.2f)
            }
            SfxType.PUZZLE_ERR -> {
                (sin(2.0 * PI * 138.0 * t) + sin(2.0 * PI * 146.0 * t)).toFloat() * 0.45f * env
            }
            SfxType.JUMPSCARE -> {
                val slam = sin(2.0 * PI * 58.0 * t).toFloat() * exp(-t * 3.5f)
                val dissonant = (sin(2.0 * PI * 554.0 * t) + sin(2.0 * PI * 587.0 * t) + sin(2.0 * PI * 830.0 * t)).toFloat() * 0.25f
                val noise = (rng.nextFloat() * 2f - 1f) * 0.35f * exp(-t * 4f)
                (slam + dissonant + noise) * env
            }
            SfxType.DOOR_CREAK -> {
                val creakFreq = 195.0 + sin(progress * 9.0) * 65.0
                val creak = sin(2.0 * PI * creakFreq * t).toFloat() * 0.45f
                val scrape = (rng.nextFloat() * 2f - 1f) * 0.16f
                (creak + scrape) * env * sin(progress * PI).toFloat()
            }
            SfxType.WEAPON_SWING -> {
                val whoosh = (rng.nextFloat() * 2f - 1f) * 0.55f * sin(progress * PI).toFloat()
                val air = sin(2.0 * PI * (280.0 - 150.0 * progress) * t).toFloat() * 0.35f * env
                whoosh + air
            }
            SfxType.CROSSBOW_SHOT -> {
                val twang = sin(2.0 * PI * (420.0 - 220.0 * progress) * t).toFloat() * exp(-t * 14f)
                val snap = (rng.nextFloat() * 2f - 1f) * 0.55f * exp(-t * 48f)
                twang * 0.7f + snap
            }
            SfxType.WEAPON_HIT -> {
                val impact = sin(2.0 * PI * 115.0 * t).toFloat() * exp(-t * 10f)
                val ring = sin(2.0 * PI * 920.0 * t).toFloat() * 0.4f * exp(-t * 6f)
                val shriek = sin(2.0 * PI * (590.0 - 240.0 * progress) * t).toFloat() * 0.32f * env
                impact + ring + shriek
            }
            SfxType.CAMERA_SHUTTER -> {
                val click = (rng.nextFloat() * 2f - 1f) * 0.7f * exp(-t * 62f)
                val flashCharge = sin(2.0 * PI * (1200.0 + 900.0 * progress) * t).toFloat() * 0.25f * env
                click + flashCharge
            }
            SfxType.MIRROR_JINGLE -> {
                val chime1 = sin(2.0 * PI * 2140.0 * t).toFloat() * exp(-t * 11f)
                val chime2 = sin(2.0 * PI * 2780.0 * t).toFloat() * exp(-t * 13f)
                (chime1 + chime2) * 0.4f
            }
            SfxType.CAGE_SLAM -> {
                val clang = sin(2.0 * PI * 140.0 * t).toFloat() * exp(-t * 4.5f)
                val barRing = (sin(2.0 * PI * 425.0 * t) + sin(2.0 * PI * 685.0 * t)).toFloat() * 0.35f * exp(-t * 3.8f)
                clang + barRing
            }
            SfxType.PHONE_DIAL -> {
                val tone = (sin(2.0 * PI * 425.0 * t) + sin(2.0 * PI * 850.0 * t)).toFloat() * 0.32f
                val pulse = if ((t * 5f).toInt() % 2 == 0) 1f else 0.15f
                tone * pulse * env
            }
            SfxType.BREAKER_CLUNK -> {
                val thud = sin(2.0 * PI * 85.0 * t).toFloat() * exp(-t * 22f)
                val buzz = sin(2.0 * PI * 120.0 * t).toFloat() * 0.25f * exp(-t * 8f)
                thud + buzz
            }
        }
    }
}
