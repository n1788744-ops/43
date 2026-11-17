package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.model.PlayerProgress
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "witch_shadow_prefs")

class SettingsRepository(private val context: Context) {

    private object Keys {
        val HIGHEST_CHAPTER = intPreferencesKey("highest_unlocked_chapter")
        val COMPLETED_CHAPTERS = stringSetPreferencesKey("completed_chapters")
        val SOUND_VOLUME = floatPreferencesKey("sound_volume")
        val VOICE_ENABLED = booleanPreferencesKey("voice_enabled")
        val LOOK_SENSITIVITY = floatPreferencesKey("look_sensitivity")
        val FREE_ACCESS = booleanPreferencesKey("free_chapter_access")
        val TUTORIAL_SEEN = booleanPreferencesKey("voice_tutorial_seen")
        fun bestTimeKey(chapter: Int) = intPreferencesKey("best_time_ch_$chapter")
    }

    val progressFlow: Flow<PlayerProgress> = context.dataStore.data.map { prefs ->
        val completed = prefs[Keys.COMPLETED_CHAPTERS]?.mapNotNull { it.toIntOrNull() }?.toSet() ?: emptySet()
        val bestTimes = buildMap {
            for (ch in 1..5) {
                prefs[Keys.bestTimeKey(ch)]?.let { put(ch, it) }
            }
        }
        PlayerProgress(
            highestUnlockedChapter = prefs[Keys.HIGHEST_CHAPTER] ?: 1,
            completedChapters = completed,
            bestTimesSec = bestTimes,
            soundVolume = prefs[Keys.SOUND_VOLUME] ?: 0.85f,
            voiceEnabled = prefs[Keys.VOICE_ENABLED] ?: true,
            lookSensitivity = prefs[Keys.LOOK_SENSITIVITY] ?: 1.0f,
            freeChapterAccess = prefs[Keys.FREE_ACCESS] ?: false,
            tutorialSeen = prefs[Keys.TUTORIAL_SEEN] ?: false
        )
    }

    suspend fun recordChapterCompleted(chapterNumber: Int, elapsedSeconds: Int) {
        context.dataStore.edit { prefs ->
            val currentUnlocked = prefs[Keys.HIGHEST_CHAPTER] ?: 1
            if (chapterNumber >= currentUnlocked && chapterNumber < 5) {
                prefs[Keys.HIGHEST_CHAPTER] = chapterNumber + 1
            }
            val currentCompleted = prefs[Keys.COMPLETED_CHAPTERS] ?: emptySet()
            prefs[Keys.COMPLETED_CHAPTERS] = currentCompleted + chapterNumber.toString()

            val prevBest = prefs[Keys.bestTimeKey(chapterNumber)]
            if (prevBest == null || elapsedSeconds < prevBest) {
                prefs[Keys.bestTimeKey(chapterNumber)] = elapsedSeconds
            }
        }
    }

    suspend fun markTutorialSeen() {
        context.dataStore.edit { it[Keys.TUTORIAL_SEEN] = true }
    }

    suspend fun setSoundVolume(volume: Float) {
        context.dataStore.edit { it[Keys.SOUND_VOLUME] = volume.coerceIn(0f, 1f) }
    }

    suspend fun setVoiceEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.VOICE_ENABLED] = enabled }
    }

    suspend fun setLookSensitivity(sensitivity: Float) {
        context.dataStore.edit { it[Keys.LOOK_SENSITIVITY] = sensitivity.coerceIn(0.5f, 2.0f) }
    }

    suspend fun setFreeChapterAccess(enabled: Boolean) {
        context.dataStore.edit { it[Keys.FREE_ACCESS] = enabled }
    }
}
