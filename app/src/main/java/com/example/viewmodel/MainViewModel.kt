package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.HorrorSoundEngine
import com.example.audio.NastyaVoiceActor
import com.example.data.ChapterCatalog
import com.example.data.SettingsRepository
import com.example.engine3d.FirstPersonGameEngine
import com.example.model.ChapterConfig
import com.example.model.ChapterId
import com.example.model.DialogueLine
import com.example.model.GameHudState
import com.example.model.GameOutcome
import com.example.model.PlayerProgress
import com.example.model.PuzzleType
import com.example.model.StashDialogState
import com.example.model.WorldNoteSpawn
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface AppScreen {
    data object MainMenu : AppScreen
    data object ChapterSelect : AppScreen
    data class VoiceTutorial(val pendingChapter: ChapterConfig?) : AppScreen
    data class ChapterIntro(val chapter: ChapterConfig) : AppScreen
    data class Playing(val chapter: ChapterConfig, val runId: Int) : AppScreen
    data object Settings : AppScreen
    data object CharacterLore : AppScreen
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val settingsRepo = SettingsRepository(application)
    val soundEngine = HorrorSoundEngine(application)
    val voiceActor = NastyaVoiceActor(application, viewModelScope)

    val progress: StateFlow<PlayerProgress> = settingsRepo.progressFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = PlayerProgress()
    )

    private val _screen = MutableStateFlow<AppScreen>(AppScreen.MainMenu)
    val screen: StateFlow<AppScreen> = _screen.asStateFlow()

    private val _hudState = MutableStateFlow(GameHudState())
    val hudState: StateFlow<GameHudState> = _hudState.asStateFlow()

    private val _activePuzzle = MutableStateFlow<PuzzleType?>(null)
    val activePuzzle: StateFlow<PuzzleType?> = _activePuzzle.asStateFlow()

    private val _activeNote = MutableStateFlow<WorldNoteSpawn?>(null)
    val activeNote: StateFlow<WorldNoteSpawn?> = _activeNote.asStateFlow()

    private val _activeStash = MutableStateFlow<StashDialogState?>(null)
    val activeStash: StateFlow<StashDialogState?> = _activeStash.asStateFlow()

    val activeDialogue: StateFlow<DialogueLine?> = voiceActor.activeDialogue

    private var currentEngine: FirstPersonGameEngine? = null
    private var recordedCompletionForRun = false
    private var runCounter = 0

    init {
        soundEngine.start()
        viewModelScope.launch {
            progress.collectLatest { p ->
                soundEngine.setVolume(p.soundVolume)
                voiceActor.setVoiceEnabled(p.voiceEnabled)
            }
        }
    }

    fun onAppPause() {
        soundEngine.setPaused(true)
        if (_screen.value is AppScreen.Playing) {
            currentEngine?.setPaused(true)
        }
    }

    fun onAppResume() {
        soundEngine.setPaused(false)
    }

    fun navigateToMenu() {
        voiceActor.stopSpeaking()
        currentEngine = null
        _activePuzzle.value = null
        _activeNote.value = null
        _activeStash.value = null
        soundEngine.setHuntState(false, 0f)
        soundEngine.setGameplayContext(false, 0f)
        _screen.value = AppScreen.MainMenu
    }

    fun navigateToChapters() {
        _screen.value = AppScreen.ChapterSelect
    }

    fun navigateToSettings() {
        _screen.value = AppScreen.Settings
    }

    fun navigateToLore() {
        _screen.value = AppScreen.CharacterLore
    }

    fun continueAdventure() {
        val nextNum = progress.value.highestUnlockedChapter.coerceIn(1, 5)
        startChapterIntro(ChapterId.fromNumber(nextNum))
    }

    fun startNewGame() {
        startChapterIntro(ChapterId.CHAPTER_1_DUNGEON)
    }

    fun startChapterIntro(chapterId: ChapterId, bypassTutorial: Boolean = false) {
        val chapter = ChapterCatalog.getChapter(chapterId)
        if (!bypassTutorial && !progress.value.tutorialSeen) {
            _screen.value = AppScreen.VoiceTutorial(pendingChapter = chapter)
        } else {
            _screen.value = AppScreen.ChapterIntro(chapter)
            voiceActor.speakNastya(chapter.introVoiceRu, forceInterrupt = true)
        }
    }

    fun openVoiceTutorialFromSettings() {
        _screen.value = AppScreen.VoiceTutorial(pendingChapter = null)
    }

    fun speakTutorialLine(text: String) {
        voiceActor.speakNastya(text, forceInterrupt = true)
    }

    fun completeVoiceTutorial() {
        val pending = (_screen.value as? AppScreen.VoiceTutorial)?.pendingChapter
        voiceActor.stopSpeaking()
        viewModelScope.launch {
            settingsRepo.markTutorialSeen()
        }
        if (pending != null) {
            startChapterIntro(pending.id, bypassTutorial = true)
        } else {
            _screen.value = AppScreen.MainMenu
        }
    }

    fun beginChapterGameplay(chapter: ChapterConfig): FirstPersonGameEngine {
        recordedCompletionForRun = false
        _activePuzzle.value = null
        _activeNote.value = null
        _activeStash.value = null
        soundEngine.setHuntState(false, 0f)

        val engine = FirstPersonGameEngine(
            chapter = chapter,
            soundEngine = soundEngine,
            voiceActor = voiceActor,
            onHudUpdate = { state ->
                _hudState.value = state
                val escaped = state.outcome == GameOutcome.CHAPTER_ESCAPED || state.outcome == GameOutcome.POLICE_RESCUED
                if (escaped && !recordedCompletionForRun) {
                    recordedCompletionForRun = true
                    viewModelScope.launch {
                        settingsRepo.recordChapterCompleted(chapter.id.number, state.elapsedSeconds)
                    }
                    if (state.outcome == GameOutcome.CHAPTER_ESCAPED) {
                        voiceActor.speakNastya(chapter.twistVoiceRu, forceInterrupt = true)
                    }
                }
            },
            onOpenPuzzle = { puzzleType ->
                _activePuzzle.value = puzzleType
            },
            onOpenNote = { note ->
                _activeNote.value = note
            },
            onOpenStash = { stashState ->
                _activeStash.value = stashState
            }
        )
        currentEngine = engine
        runCounter++
        _hudState.value = GameHudState(
            chapterId = chapter.id,
            chapterTitleRu = chapter.titleRu
        )
        _screen.value = AppScreen.Playing(chapter, runCounter)
        soundEngine.setGameplayContext(true, 0.1f)
        return engine
    }

    fun getOrCreateEngine(chapter: ChapterConfig): FirstPersonGameEngine {
        return currentEngine ?: beginChapterGameplay(chapter)
    }

    fun restartCurrentChapter() {
        val cur = (_screen.value as? AppScreen.Playing)?.chapter ?: return
        currentEngine = null
        beginChapterGameplay(cur)
    }

    fun proceedToNextChapter() {
        val cur = (_screen.value as? AppScreen.Playing)?.chapter ?: return
        val nextNum = cur.id.number + 1
        if (nextNum <= 5) {
            currentEngine = null
            startChapterIntro(ChapterId.fromNumber(nextNum), bypassTutorial = true)
        } else {
            navigateToMenu()
        }
    }

    fun togglePauseGameplay() {
        val engine = currentEngine ?: return
        val isCurrentlyPaused = _hudState.value.outcome == GameOutcome.PAUSED
        engine.setPaused(!isCurrentlyPaused)
    }

    fun dismissNote() {
        _activeNote.value = null
        currentEngine?.resumeFromNote()
    }

    fun closePuzzleWithoutSolving() {
        _activePuzzle.value = null
        currentEngine?.setPaused(false)
    }

    fun completePuzzleSuccess() {
        _activePuzzle.value = null
        currentEngine?.onPuzzleSolved()
    }

    fun reportPuzzleFailure() {
        currentEngine?.onPuzzleFailedAttempt()
    }

    // Touch controls forwarded to engine
    fun setMoveInput(x: Float, y: Float, sprint: Boolean) {
        currentEngine?.setMoveInput(x, y, sprint)
    }

    fun addLookDelta(dxDeg: Float, dyDeg: Float) {
        val sens = progress.value.lookSensitivity
        currentEngine?.addLookDelta(dxDeg * sens, dyDeg * sens)
    }

    fun interact() {
        currentEngine?.triggerInteract()
    }

    fun attackWithWeapon() {
        currentEngine?.triggerAttack()
    }

    fun cycleWeapon() {
        currentEngine?.triggerCycleWeapon()
    }

    fun useSaltWard() {
        currentEngine?.triggerUseSalt()
    }

    fun toggleFlashlight() {
        currentEngine?.toggleFlashlight()
    }

    fun takePolaroidPhoto() {
        currentEngine?.triggerPolaroidPhoto()
    }

    fun completeCageEscape() {
        currentEngine?.onCageEscaped()
    }

    fun stashUnderBed(index: Int) {
        currentEngine?.stashBackpackItemUnderBed(index)
    }

    fun takeFromBedStash(index: Int) {
        currentEngine?.takeBedStashItemToBackpack(index)
    }

    fun takeFromKitchenChest(index: Int) {
        currentEngine?.takeKitchenChestItemToBackpack(index)
    }

    fun swapPendingPickup(dropIndex: Int) {
        _activeStash.value = null
        currentEngine?.swapPendingPickupWithSlot(dropIndex)
    }

    fun closeStashModal() {
        _activeStash.value = null
        currentEngine?.dismissStashModal()
    }

    // Settings updates
    fun setSoundVolume(vol: Float) {
        viewModelScope.launch { settingsRepo.setSoundVolume(vol) }
    }

    fun setVoiceEnabled(enabled: Boolean) {
        viewModelScope.launch { settingsRepo.setVoiceEnabled(enabled) }
    }

    fun setLookSensitivity(sens: Float) {
        viewModelScope.launch { settingsRepo.setLookSensitivity(sens) }
    }

    fun setFreeChapterAccess(enabled: Boolean) {
        viewModelScope.launch { settingsRepo.setFreeChapterAccess(enabled) }
    }

    fun testNastyaVoice() {
        voiceActor.testVoiceSample()
    }

    override fun onCleared() {
        super.onCleared()
        soundEngine.stop()
        voiceActor.shutdown()
    }
}
