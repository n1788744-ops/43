package com.example

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.screens.ChapterIntroScreen
import com.example.ui.screens.ChapterSelectScreen
import com.example.ui.screens.CharacterLoreScreen
import com.example.ui.screens.GameScreen
import com.example.ui.screens.MainMenuScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.VoiceTutorialScreen
import com.example.ui.theme.WitchShadowTheme
import com.example.viewmodel.AppScreen
import com.example.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WitchShadowTheme {
                WitchShadowApp(viewModel = viewModel)
            }
        }
    }

    override fun onPause() {
        super.onPause()
        viewModel.onAppPause()
    }

    override fun onResume() {
        super.onResume()
        viewModel.onAppResume()
    }
}

@Composable
fun WitchShadowApp(viewModel: MainViewModel) {
    val screen by viewModel.screen.collectAsStateWithLifecycle()
    val progress by viewModel.progress.collectAsStateWithLifecycle()
    val hudState by viewModel.hudState.collectAsStateWithLifecycle()
    val activePuzzle by viewModel.activePuzzle.collectAsStateWithLifecycle()
    val activeNote by viewModel.activeNote.collectAsStateWithLifecycle()
    val activeStash by viewModel.activeStash.collectAsStateWithLifecycle()
    val activeDialogue by viewModel.activeDialogue.collectAsStateWithLifecycle()
    val context = LocalContext.current

    when (val current = screen) {
        is AppScreen.MainMenu -> {
            MainMenuScreen(
                progress = progress,
                onContinueClick = viewModel::continueAdventure,
                onNewGameClick = viewModel::startNewGame,
                onChaptersClick = viewModel::navigateToChapters,
                onLoreClick = viewModel::navigateToLore,
                onSettingsClick = viewModel::navigateToSettings,
                onExitClick = { (context as? Activity)?.finish() }
            )
        }
        is AppScreen.ChapterSelect -> {
            ChapterSelectScreen(
                progress = progress,
                onSelectChapter = { id -> viewModel.startChapterIntro(id) },
                onToggleFreeAccess = viewModel::setFreeChapterAccess,
                onBack = viewModel::navigateToMenu
            )
        }
        is AppScreen.VoiceTutorial -> {
            VoiceTutorialScreen(
                dialogue = activeDialogue,
                onSpeakStep = viewModel::speakTutorialLine,
                onContinueToGame = viewModel::completeVoiceTutorial,
                onBack = viewModel::navigateToMenu
            )
        }
        is AppScreen.ChapterIntro -> {
            ChapterIntroScreen(
                chapter = current.chapter,
                dialogue = activeDialogue,
                onStartChapter = { viewModel.beginChapterGameplay(current.chapter) },
                onBack = viewModel::navigateToMenu
            )
        }
        is AppScreen.Playing -> {
            val engine = remember(current.chapter.id, current.runId) {
                viewModel.getOrCreateEngine(current.chapter)
            }
            GameScreen(
                chapter = current.chapter,
                engine = engine,
                hud = hudState,
                activePuzzle = activePuzzle,
                activeNote = activeNote,
                activeStash = activeStash,
                activeDialogue = activeDialogue,
                onMoveInput = viewModel::setMoveInput,
                onLookDelta = viewModel::addLookDelta,
                onInteract = viewModel::interact,
                onAttack = viewModel::attackWithWeapon,
                onCycleWeapon = viewModel::cycleWeapon,
                onUseSalt = viewModel::useSaltWard,
                onToggleFlashlight = viewModel::toggleFlashlight,
                onTakePhoto = viewModel::takePolaroidPhoto,
                onCageEscaped = viewModel::completeCageEscape,
                onStashUnderBed = viewModel::stashUnderBed,
                onTakeFromBedStash = viewModel::takeFromBedStash,
                onTakeFromKitchenChest = viewModel::takeFromKitchenChest,
                onSwapPendingPickup = viewModel::swapPendingPickup,
                onDismissStash = viewModel::closeStashModal,
                onTogglePause = viewModel::togglePauseGameplay,
                onPuzzleSolved = viewModel::completePuzzleSuccess,
                onPuzzleFail = viewModel::reportPuzzleFailure,
                onDismissPuzzle = viewModel::closePuzzleWithoutSolving,
                onDismissNote = viewModel::dismissNote,
                onRestart = viewModel::restartCurrentChapter,
                onNextChapter = viewModel::proceedToNextChapter,
                onExitToMenu = viewModel::navigateToMenu
            )
        }
        is AppScreen.Settings -> {
            SettingsScreen(
                progress = progress,
                onVolumeChange = viewModel::setSoundVolume,
                onVoiceEnabledChange = viewModel::setVoiceEnabled,
                onSensitivityChange = viewModel::setLookSensitivity,
                onTestVoice = viewModel::testNastyaVoice,
                onOpenTutorial = viewModel::openVoiceTutorialFromSettings,
                onBack = viewModel::navigateToMenu
            )
        }
        is AppScreen.CharacterLore -> {
            CharacterLoreScreen(onBack = viewModel::navigateToMenu)
        }
    }
}
