package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.FlashlightOff
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.LocalPolice
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.engine3d.FirstPersonGameEngine
import com.example.engine3d.GameGLSurfaceView
import com.example.model.ChapterConfig
import com.example.model.DialogueLine
import com.example.model.GameHudState
import com.example.model.GameOutcome
import com.example.model.NearbyTargetType
import com.example.model.PuzzleType
import com.example.model.StashDialogState
import com.example.model.WeaponType
import com.example.model.WorldNoteSpawn
import com.example.ui.theme.ArcaneViolet
import com.example.ui.theme.BloodCrimson
import com.example.ui.theme.EmeraldWitchlight
import com.example.ui.theme.LanternAmber
import com.example.ui.theme.ManorSurface
import com.example.ui.theme.MistSecondary
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.ParchmentText
import kotlin.math.hypot
import kotlin.math.roundToInt

@Composable
fun GameScreen(
    chapter: ChapterConfig,
    engine: FirstPersonGameEngine,
    hud: GameHudState,
    activePuzzle: PuzzleType?,
    activeNote: WorldNoteSpawn?,
    activeStash: StashDialogState?,
    activeDialogue: DialogueLine?,
    onMoveInput: (Float, Float, Boolean) -> Unit,
    onLookDelta: (Float, Float) -> Unit,
    onInteract: () -> Unit,
    onAttack: () -> Unit,
    onCycleWeapon: () -> Unit,
    onUseSalt: () -> Unit,
    onToggleFlashlight: () -> Unit,
    onTakePhoto: () -> Unit,
    onCageEscaped: () -> Unit,
    onStashUnderBed: (Int) -> Unit,
    onTakeFromBedStash: (Int) -> Unit,
    onTakeFromKitchenChest: (Int) -> Unit,
    onSwapPendingPickup: (Int) -> Unit,
    onDismissStash: () -> Unit,
    onTogglePause: () -> Unit,
    onPuzzleSolved: () -> Unit,
    onPuzzleFail: () -> Unit,
    onDismissPuzzle: () -> Unit,
    onDismissNote: () -> Unit,
    onRestart: () -> Unit,
    onNextChapter: () -> Unit,
    onExitToMenu: () -> Unit
) {
    BackHandler {
        when {
            activeStash != null -> onDismissStash()
            activePuzzle != null -> onDismissPuzzle()
            activeNote != null -> onDismissNote()
            else -> onTogglePause()
        }
    }

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val glView = remember(engine) { GameGLSurfaceView(context, engine) }

    DisposableEffect(lifecycleOwner, glView) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> glView.onResume()
                Lifecycle.Event.ON_PAUSE -> glView.onPause()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            glView.onPause()
        }
    }

    var sprintToggle by remember { mutableStateOf(false) }

    val pulseAnim = rememberInfiniteTransition(label = "danger_pulse")
    val dangerPulse by pulseAnim.animateFloat(
        initialValue = 0.6f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(tween(420), RepeatMode.Reverse),
        label = "pulse"
    )

    Box(modifier = Modifier.fillMaxSize().background(ObsidianBg)) {
        // 1. Real-Time 3D First-Person OpenGL Viewport
        AndroidView(
            factory = { glView },
            modifier = Modifier.fillMaxSize()
        )

        // 2. Touch-Look Drag Layer
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        onLookDelta(dragAmount.x * 0.24f, -dragAmount.y * 0.17f)
                    }
                }
        )

        // 3. Witch Proximity / Poison Mist / Closet Vignettes
        if (hud.witchProximity > 0.22f || hud.isInPoisonMist || hud.isHiding) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                if (hud.isHiding) drawRect(Color.Black.copy(alpha = 0.62f))
                if (hud.witchProximity > 0.22f) {
                    val alpha = ((hud.witchProximity - 0.22f) * 0.72f * dangerPulse).coerceIn(0f, 0.65f)
                    drawRect(
                        brush = Brush.radialGradient(
                            colors = listOf(Color.Transparent, BloodCrimson.copy(alpha = alpha)),
                            radius = size.maxDimension * 0.68f
                        )
                    )
                }
                if (hud.isInPoisonMist) {
                    drawRect(
                        brush = Brush.radialGradient(
                            colors = listOf(Color.Transparent, ArcaneViolet.copy(alpha = 0.42f)),
                            radius = size.maxDimension * 0.62f
                        )
                    )
                }
            }
        }

        // 3B. Polaroid Camera Flash Overlay
        if (hud.cameraFlashAlpha > 0.02f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White.copy(alpha = (hud.cameraFlashAlpha * 0.92f).coerceIn(0f, 0.92f)))
            )
        }

        // 4. Minimal Center Crosshair Dot
        if (hud.outcome == GameOutcome.PLAYING && activePuzzle == null && activeNote == null && activeStash == null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                val dotColor = if (hud.nearbyTarget.type != NearbyTargetType.NONE) {
                    EmeraldWitchlight
                } else if (hud.equippedWeapon == WeaponType.CROSSBOW) {
                    LanternAmber.copy(alpha = 0.8f)
                } else {
                    ParchmentText.copy(alpha = 0.45f)
                }
                Box(
                    modifier = Modifier
                        .size(if (hud.nearbyTarget.type != NearbyTargetType.NONE) 8.dp else 5.dp)
                        .clip(CircleShape)
                        .background(dotColor)
                )
            }
        }

        // 5. Clean Uncluttered Top Horror HUD (Sanity Bar Left + Compact Slots & Pause Right)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            // Top-Left: Sanity bar + slim Stamina/Battery mini bars (zero verbose labels)
            Column(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .widthIn(max = 178.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Psychology,
                        contentDescription = "Рассудок",
                        tint = if (hud.isInPoisonMist) ArcaneViolet else Color(0xFFB47CE8),
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        LinearProgressIndicator(
                            progress = { hud.sanity },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(7.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = if (hud.isInPoisonMist) BloodCrimson else Color(0xFF9F6CE0),
                            trackColor = Color.Black.copy(alpha = 0.65f)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${(hud.sanity * 100).roundToInt()}/100",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                        color = ParchmentText.copy(alpha = 0.88f)
                    )
                }

                // Slim Stamina & Flashlight bars (icon-only, no text clutter)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.AutoMirrored.Filled.DirectionsRun,
                            contentDescription = null,
                            tint = EmeraldWitchlight.copy(alpha = 0.85f),
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        LinearProgressIndicator(
                            progress = { hud.stamina },
                            modifier = Modifier.fillMaxWidth().height(3.5.dp).clip(CircleShape),
                            color = EmeraldWitchlight,
                            trackColor = Color.Black.copy(alpha = 0.5f)
                        )
                    }
                    Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.FlashlightOn,
                            contentDescription = null,
                            tint = LanternAmber.copy(alpha = 0.85f),
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        LinearProgressIndicator(
                            progress = { hud.lanternBattery },
                            modifier = Modifier.fillMaxWidth().height(3.5.dp).clip(CircleShape),
                            color = LanternAmber,
                            trackColor = Color.Black.copy(alpha = 0.5f)
                        )
                    }
                }

                // Compact Evidence / 112 Siren Timer / Cursed Mirror micro-pills
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color.Black.copy(alpha = 0.52f),
                        border = BorderStroke(
                            1.dp,
                            if (hud.evidencesCollected.size >= 3) EmeraldWitchlight.copy(alpha = 0.85f)
                            else Color.White.copy(alpha = 0.2f)
                        )
                    ) {
                        Text(
                            text = "📸 ${hud.evidencesCollected.size}/3",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                            color = if (hud.evidencesCollected.size >= 3) EmeraldWitchlight else ParchmentText,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    if (hud.huntModeActive) {
                        val m = hud.huntRemainingSec / 60
                        val s = hud.huntRemainingSec % 60
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if ((hud.huntRemainingSec % 2) == 0) Color(0xCC133368) else BloodCrimson.copy(alpha = 0.85f),
                            border = BorderStroke(1.dp, Color(0xFF79B8FF))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.LocalPolice, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "112 %d:%02d".format(m, s),
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold),
                                    color = Color.White
                                )
                            }
                        }
                    }
                }

                if (hud.mirrorWitchRoomRu.isNotBlank()) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color.Black.copy(alpha = 0.55f),
                        border = BorderStroke(1.dp, Color(0xFF66D9FF).copy(alpha = 0.65f))
                    ) {
                        Text(
                            text = "🪞 ${hud.mirrorWitchRoomRu}",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                            color = Color(0xFF8BE5FF),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            // Top-Right: 3-Slot Backpack Squares + Active Weapon + Pause
            Row(
                modifier = Modifier.align(Alignment.TopEnd),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.Top
            ) {
                for (slotIdx in 0 until 3) {
                    val slotItem = hud.backpackItems.getOrNull(slotIdx)
                    BackpackMiniSquare(item = slotItem)
                }

                // Active weapon slot (tap to switch weapon)
                if (hud.equippedWeapon != WeaponType.NONE) {
                    val wIcon = if (hud.equippedWeapon == WeaponType.CROSSBOW) Icons.Default.GpsFixed else Icons.Default.Security
                    val wBadge = if (hud.equippedWeapon == WeaponType.CROSSBOW) "${hud.crossbowAmmo}" else "⚔"
                    HudSquareSlot(
                        icon = wIcon,
                        tint = BloodCrimson,
                        badge = wBadge,
                        active = true,
                        onClick = onCycleWeapon
                    )
                }

                // Red-framed Horror Pause Button (plus tiny capture attempts badge)
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Surface(
                        onClick = onTogglePause,
                        modifier = Modifier.size(48.dp).testTag("hud_pause_button"),
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xCC240B12),
                        border = BorderStroke(1.5.dp, BloodCrimson.copy(alpha = 0.85f))
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Pause, contentDescription = "Пауза", tint = ParchmentText)
                        }
                    }
                    if (hud.captureAttempts > 0) {
                        Text(
                            text = "🍲${hud.captureAttempts}/3",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = BloodCrimson
                        )
                    }
                }
            }

            // Ephemeral thin alert pill at top-center only when triggered (never blocks 3D view)
            AnimatedVisibility(
                visible = hud.statusBannerRu != null,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 56.dp)
            ) {
                hud.statusBannerRu?.let { banner ->
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = if (hud.witchAlerted) BloodCrimson.copy(alpha = 0.78f) else Color.Black.copy(alpha = 0.62f),
                        border = BorderStroke(1.dp, if (hud.witchAlerted) BloodCrimson else LanternAmber.copy(alpha = 0.45f))
                    ) {
                        Text(
                            text = banner,
                            style = MaterialTheme.typography.labelSmall,
                            color = ParchmentText,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                        )
                    }
                }
            }
        }

        // 6. Slim Bottom Subtitle Strip (no bulky portrait box blocking screen)
        AnimatedVisibility(
            visible = activeDialogue != null && hud.outcome == GameOutcome.PLAYING,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 18.dp, start = 148.dp, end = 148.dp)
        ) {
            activeDialogue?.let { dlg ->
                Surface(
                    modifier = Modifier.widthIn(max = 460.dp),
                    shape = RoundedCornerShape(10.dp),
                    color = Color.Black.copy(alpha = 0.56f)
                ) {
                    Text(
                        text = dlg.textRu,
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
                        color = ParchmentText,
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
            }
        }

        // 7. Minimal Translucent Mobile Controls
        if (hud.outcome == GameOutcome.PLAYING && activePuzzle == null && activeNote == null && activeStash == null) {
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 18.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                // LEFT THUMB: Compact Sprint icon + Virtual Joystick
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    FilledIconButton(
                        onClick = { sprintToggle = !sprintToggle },
                        modifier = Modifier.size(48.dp).testTag("hud_sprint_button"),
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = if (sprintToggle) LanternAmber.copy(alpha = 0.9f) else Color.Black.copy(alpha = 0.45f),
                            contentColor = if (sprintToggle) ObsidianBg else ParchmentText
                        )
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.DirectionsRun,
                            contentDescription = "Бег",
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    VirtualAnalogJoystick(
                        onMove = { jx, jy, autoSprint ->
                            onMoveInput(jx, jy, sprintToggle || autoSprint)
                        }
                    )
                }

                // RIGHT THUMB: Contextual 1-word Action + Icon-only Attack / Salt / Flashlight
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (hud.nearbyTarget.type != NearbyTargetType.NONE) {
                        Button(
                            onClick = onInteract,
                            modifier = Modifier.height(48.dp).testTag("hud_interact_button"),
                            shape = RoundedCornerShape(24.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = EmeraldWitchlight.copy(alpha = 0.92f),
                                contentColor = Color(0xFF041D14)
                            )
                        ) {
                            Icon(Icons.Default.TouchApp, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = hud.nearbyTarget.labelRu,
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                maxLines = 1
                            )
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                        // Polaroid Camera Flash Button (Evidence 1..3)
                        FilledIconButton(
                            onClick = onTakePhoto,
                            enabled = !hud.isHiding,
                            modifier = Modifier.size(50.dp).testTag("hud_polaroid_button"),
                            colors = IconButtonDefaults.filledIconButtonColors(
                                containerColor = Color.Black.copy(alpha = 0.62f),
                                contentColor = Color(0xFF8BE5FF)
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.PhotoCamera,
                                contentDescription = "Полароид"
                            )
                        }

                        // Attack Icon Button
                        if (hud.equippedWeapon != WeaponType.NONE) {
                            FilledIconButton(
                                onClick = onAttack,
                                enabled = hud.weaponCooldownFraction <= 0.05f && !hud.isHiding,
                                modifier = Modifier.size(54.dp).testTag("hud_attack_button"),
                                colors = IconButtonDefaults.filledIconButtonColors(
                                    containerColor = BloodCrimson.copy(alpha = 0.88f),
                                    contentColor = Color.White,
                                    disabledContainerColor = Color.Black.copy(alpha = 0.35f)
                                )
                            ) {
                                Icon(
                                    imageVector = if (hud.equippedWeapon == WeaponType.CROSSBOW) Icons.Default.GpsFixed else Icons.Default.Bolt,
                                    contentDescription = "Атака"
                                )
                            }
                        }

                        // Salt Ward Icon Button with tiny count badge
                        Surface(
                            onClick = onUseSalt,
                            enabled = hud.saltWardsCount > 0,
                            modifier = Modifier.size(50.dp).testTag("hud_salt_button"),
                            shape = CircleShape,
                            color = Color.Black.copy(alpha = 0.55f),
                            border = BorderStroke(1.2.dp, if (hud.saltWardsCount > 0) EmeraldWitchlight.copy(alpha = 0.75f) else Color.White.copy(alpha = 0.2f))
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.AutoFixHigh,
                                    contentDescription = "Соль",
                                    tint = if (hud.saltWardsCount > 0) EmeraldWitchlight else MistSecondary.copy(alpha = 0.4f),
                                    modifier = Modifier.size(21.dp)
                                )
                                Text(
                                    text = "${hud.saltWardsCount}",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                                    color = ParchmentText,
                                    modifier = Modifier.align(Alignment.BottomEnd).padding(end = 6.dp, bottom = 4.dp)
                                )
                            }
                        }

                        // Flashlight Toggle
                        FilledIconButton(
                            onClick = onToggleFlashlight,
                            modifier = Modifier.size(50.dp).testTag("hud_flashlight_button"),
                            colors = IconButtonDefaults.filledIconButtonColors(
                                containerColor = if (hud.isFlashlightOn) LanternAmber.copy(alpha = 0.90f) else Color.Black.copy(alpha = 0.55f),
                                contentColor = if (hud.isFlashlightOn) ObsidianBg else MistSecondary
                            )
                        ) {
                            Icon(
                                imageVector = if (hud.isFlashlightOn) Icons.Default.FlashlightOn else Icons.Default.FlashlightOff,
                                contentDescription = "Фонарь"
                            )
                        }
                    }
                }
            }
        }

        // 8. Overlays
        activeNote?.let { note -> NoteReaderOverlay(note = note, onClose = onDismissNote) }

        activePuzzle?.let { pType ->
            PuzzleDialogOverlay(
                chapter = chapter,
                puzzleType = pType,
                onSolved = onPuzzleSolved,
                onFailedAttempt = onPuzzleFail,
                onDismiss = onDismissPuzzle
            )
        }

        activeStash?.let { stash ->
            StashChestOverlay(
                dialogState = stash,
                hud = hud,
                onStashUnderBed = onStashUnderBed,
                onTakeFromBedStash = onTakeFromBedStash,
                onTakeFromKitchenChest = onTakeFromKitchenChest,
                onSwapPendingPickup = onSwapPendingPickup,
                onDismiss = onDismissStash
            )
        }

        when (hud.outcome) {
            GameOutcome.PAUSED -> {
                if (activePuzzle == null && activeNote == null && activeStash == null) {
                    PauseOverlay(
                        chapterTitle = chapter.titleRu,
                        objectiveTextRu = hud.objectiveTextRu,
                        onResume = onTogglePause,
                        onRestart = onRestart,
                        onExitToMenu = onExitToMenu
                    )
                }
            }
            GameOutcome.CAGED_ESCAPE -> {
                KitchenCageLockpickOverlay(
                    hud = hud,
                    onUnlocked = onCageEscaped
                )
            }
            GameOutcome.CAUGHT_BY_WITCH -> {
                CaughtByWitchOverlay(
                    chapterTitle = chapter.titleRu,
                    onRetry = onRestart,
                    onExitToMenu = onExitToMenu
                )
            }
            GameOutcome.BAD_ENDING_DINNER -> {
                BadEndingDinnerOverlay(
                    chapterTitle = chapter.titleRu,
                    onRetry = onRestart,
                    onExitToMenu = onExitToMenu
                )
            }
            GameOutcome.CHAPTER_ESCAPED -> {
                ChapterTwistOutcomeOverlay(
                    chapter = chapter,
                    elapsedSeconds = hud.elapsedSeconds,
                    onNextChapter = onNextChapter,
                    onExitToMenu = onExitToMenu
                )
            }
            GameOutcome.POLICE_RESCUED -> {
                PoliceRescuedOverlay(
                    chapter = chapter,
                    elapsedSeconds = hud.elapsedSeconds,
                    onNextChapter = onNextChapter,
                    onExitToMenu = onExitToMenu
                )
            }
            GameOutcome.PLAYING -> Unit
        }
    }
}

@Composable
private fun BackpackMiniSquare(item: com.example.model.InventoryItem?) {
    Surface(
        modifier = Modifier.size(44.dp),
        shape = RoundedCornerShape(9.dp),
        color = Color.Black.copy(alpha = 0.56f),
        border = BorderStroke(
            1.2.dp,
            if (item != null) LanternAmber.copy(alpha = 0.85f) else Color.White.copy(alpha = 0.22f)
        )
    ) {
        Box(contentAlignment = Alignment.Center) {
            if (item != null) {
                Text(text = item.symbol, fontSize = 18.sp)
            } else {
                Text(text = "·", fontSize = 16.sp, color = MistSecondary.copy(alpha = 0.45f))
            }
        }
    }
}

@Composable
private fun HudSquareSlot(
    icon: ImageVector,
    tint: Color,
    badge: String,
    active: Boolean,
    onClick: (() -> Unit)? = null
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            onClick = onClick ?: {},
            enabled = onClick != null,
            modifier = Modifier.size(48.dp),
            shape = RoundedCornerShape(10.dp),
            color = Color.Black.copy(alpha = 0.58f),
            border = BorderStroke(1.2.dp, if (active) tint.copy(alpha = 0.85f) else Color.White.copy(alpha = 0.25f))
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (active) tint else MistSecondary.copy(alpha = 0.55f),
                    modifier = Modifier.size(22.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = badge,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, fontWeight = FontWeight.SemiBold),
            color = if (active) ParchmentText else MistSecondary
        )
    }
}

@Composable
private fun VirtualAnalogJoystick(
    onMove: (x: Float, y: Float, autoSprint: Boolean) -> Unit
) {
    var knobOffset by remember { mutableStateOf(Offset.Zero) }
    val sizeDp = 118.dp

    Box(
        modifier = Modifier
            .size(sizeDp)
            .testTag("virtual_joystick")
            .pointerInput(Unit) {
                val maxRadius = size.width * 0.38f
                awaitPointerEventScope {
                    while (true) {
                        val downEvent = awaitPointerEvent()
                        val pointer = downEvent.changes.firstOrNull { it.pressed } ?: continue
                        val center = Offset(size.width / 2f, size.height / 2f)

                        fun updateFrom(pos: Offset) {
                            val delta = pos - center
                            val dist = hypot(delta.x, delta.y)
                            val clamped = if (dist > maxRadius) {
                                Offset(delta.x / dist * maxRadius, delta.y / dist * maxRadius)
                            } else {
                                delta
                            }
                            knobOffset = clamped
                            val nx = (clamped.x / maxRadius).coerceIn(-1f, 1f)
                            val ny = (-clamped.y / maxRadius).coerceIn(-1f, 1f)
                            val mag = hypot(nx, ny)
                            onMove(nx, ny, mag > 0.84f)
                        }

                        updateFrom(pointer.position)
                        pointer.consume()

                        var tracking = true
                        while (tracking) {
                            val moveEvent = awaitPointerEvent()
                            val tracked = moveEvent.changes.firstOrNull { it.id == pointer.id }
                            if (tracked == null || !tracked.pressed) {
                                tracking = false
                            } else {
                                updateFrom(tracked.position)
                                tracked.consume()
                            }
                        }
                        knobOffset = Offset.Zero
                        onMove(0f, 0f, false)
                    }
                }
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val outerR = size.minDimension * 0.45f
            drawCircle(color = ManorSurface.copy(alpha = 0.45f), radius = outerR, center = center)
            drawCircle(color = ParchmentText.copy(alpha = 0.24f), radius = outerR, center = center, style = Stroke(width = 2.5f))
            val knobPos = center + knobOffset
            drawCircle(color = LanternAmber.copy(alpha = 0.78f), radius = outerR * 0.40f, center = knobPos)
            drawCircle(color = Color.White.copy(alpha = 0.38f), radius = outerR * 0.40f, center = knobPos, style = Stroke(width = 1.5f))
        }
    }
}
