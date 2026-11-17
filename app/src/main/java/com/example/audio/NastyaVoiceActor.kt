package com.example.audio

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.Voice
import com.example.model.DialogueLine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Locale

/**
 * Pleasant Russian female voice acting for Nastya (17 years old).
 * Selects the best available Russian female TTS voice with youthful pitch,
 * while synchronously broadcasting expressive Russian dialogue subtitles.
 */
class NastyaVoiceActor(
    context: Context,
    private val scope: CoroutineScope
) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private var ttsReady = false

    @Volatile
    private var voiceEnabled = true

    private var lastSpokenAtMs = 0L
    private var subtitleHideJob: Job? = null

    private val _activeDialogue = MutableStateFlow<DialogueLine?>(null)
    val activeDialogue: StateFlow<DialogueLine?> = _activeDialogue.asStateFlow()

    init {
        try {
            tts = TextToSpeech(context.applicationContext, this)
        } catch (_: Exception) {
            ttsReady = false
        }
    }

    override fun onInit(status: Int) {
        if (status != TextToSpeech.SUCCESS) {
            ttsReady = false
            return
        }
        val engine = tts ?: return
        val ruLocale = Locale("ru", "RU")
        val langResult = engine.setLanguage(ruLocale)
        if (langResult == TextToSpeech.LANG_MISSING_DATA || langResult == TextToSpeech.LANG_NOT_SUPPORTED) {
            ttsReady = false
            return
        }

        // Choose the most natural Russian female voice if available
        try {
            val ruVoices = engine.voices.orEmpty().filter {
                it.locale.language.equals("ru", ignoreCase = true)
            }
            val preferredFemale = ruVoices.sortedWith(
                compareByDescending<Voice> { isLikelyFemaleVoice(it.name) }
                    .thenBy { it.isNetworkConnectionRequired }
                    .thenByDescending { it.quality }
            ).firstOrNull()
            if (preferredFemale != null) {
                engine.voice = preferredFemale
            }
        } catch (_: Exception) {
        }

        // Youthful, pleasant 17-year-old girl tone
        engine.setPitch(1.16f)
        engine.setSpeechRate(0.97f)
        ttsReady = true
    }

    private fun isLikelyFemaleVoice(name: String): Boolean {
        val lower = name.lowercase(Locale.ROOT)
        return lower.contains("female") ||
            lower.contains("ruf") ||
            lower.contains("milena") ||
            lower.contains("katya") ||
            lower.contains("irina") ||
            lower.contains("svetlana") ||
            lower.contains("x-dfc") ||
            lower.contains("x-ruc") ||
            lower.contains("x-rue")
    }

    fun setVoiceEnabled(enabled: Boolean) {
        voiceEnabled = enabled
        if (!enabled) {
            try {
                tts?.stop()
            } catch (_: Exception) {
            }
        }
    }

    fun speakNastya(textRu: String, forceInterrupt: Boolean = false) {
        val now = System.currentTimeMillis()
        if (!forceInterrupt && now - lastSpokenAtMs < 1800L) return
        lastSpokenAtMs = now

        _activeDialogue.value = DialogueLine(speaker = "Настя (17 лет)", textRu = textRu, timestampMs = now)
        subtitleHideJob?.cancel()
        subtitleHideJob = scope.launch(Dispatchers.Main) {
            val readDurationMs = (2600L + textRu.length * 62L).coerceIn(3600L, 9500L)
            delay(readDurationMs)
            if (_activeDialogue.value?.timestampMs == now) {
                _activeDialogue.value = null
            }
        }

        if (voiceEnabled && ttsReady) {
            try {
                val queueMode = if (forceInterrupt) TextToSpeech.QUEUE_FLUSH else TextToSpeech.QUEUE_ADD
                tts?.speak(textRu, queueMode, null, "nastya_$now")
            } catch (_: Exception) {
            }
        }
    }

    fun testVoiceSample() {
        speakNastya(
            "Привет! Меня зовут Настя, мне семнадцать лет. Помоги мне выбраться из дома ведьмы!",
            forceInterrupt = true
        )
    }

    fun stopSpeaking() {
        subtitleHideJob?.cancel()
        _activeDialogue.value = null
        try {
            tts?.stop()
        } catch (_: Exception) {
        }
    }

    fun shutdown() {
        stopSpeaking()
        try {
            tts?.shutdown()
        } catch (_: Exception) {
        }
        tts = null
    }
}
