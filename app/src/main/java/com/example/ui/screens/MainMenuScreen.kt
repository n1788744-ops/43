package com.example.ui.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.PlayerProgress
import com.example.ui.theme.CormorantDisplayFamily
import com.example.ui.theme.MontserratBodyFamily
import com.example.ui.theme.ParchmentText
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun MainMenuScreen(
    progress: PlayerProgress,
    onContinueClick: () -> Unit,
    onNewGameClick: () -> Unit,
    onChaptersClick: () -> Unit,
    onLoreClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onExitClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "menu_horror")
    val drift by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 14000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "drift"
    )
    val cameraSway by infiniteTransition.animateFloat(
        initialValue = -1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 6500),
            repeatMode = RepeatMode.Reverse
        ),
        label = "sway"
    )

    val coldHorrorMatrix = ColorMatrix().apply {
        setToSaturation(0.32f)
    }

    val hasContinue = progress.completedChapters.isNotEmpty() || progress.highestUnlockedChapter > 1

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF060A0D))
    ) {
        val sidebarWidth = (maxWidth * 0.36f).coerceIn(210.dp, 290.dp)

        // 1. Atmospheric Tilted Horror Interior Backdrop
        Image(
            painter = painterResource(id = R.drawable.img_menu_manor),
            contentDescription = "Мрачный интерьер проклятого дома",
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = 1.16f
                    scaleY = 1.16f
                    rotationZ = -4.2f + cameraSway * 1.1f
                    translationX = cameraSway * 14f
                },
            contentScale = ContentScale.Crop,
            colorFilter = ColorFilter.colorMatrix(coldHorrorMatrix)
        )

        // 2. Cold Cyan-Slate Fog, Volumetric Flashlight Beam & Dust Motes
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Cold moody blue-slate atmospheric tint
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xAA0A141A),
                        Color(0x55122028),
                        Color(0xBB070B0E)
                    )
                )
            )

            // Subtle tilted wooden floor perspective & doorframe shadow on right
            val doorX = size.width * 0.62f
            val doorTop = size.height * 0.16f
            val doorBottom = size.height * 0.80f
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0x66040608), Color(0x99020406)),
                    startY = doorTop,
                    endY = doorBottom
                ),
                topLeft = Offset(doorX, doorTop),
                size = androidx.compose.ui.geometry.Size(size.width * 0.16f, doorBottom - doorTop)
            )

            // Volumetric flashlight beam sweeping softly across the floor
            val beamOrigin = Offset(size.width * 0.34f, size.height * 0.56f)
            val beamTargetX = size.width * (0.86f + cameraSway * 0.06f)
            val beamTargetY = size.height * (0.42f + cameraSway * 0.05f)
            val beamPath = Path().apply {
                moveTo(beamOrigin.x, beamOrigin.y)
                lineTo(beamTargetX, beamTargetY - size.height * 0.22f)
                lineTo(beamTargetX + size.width * 0.12f, beamTargetY + size.height * 0.26f)
                close()
            }
            drawPath(
                path = beamPath,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0x3CD2ECEE),
                        Color(0x1C9DC4C8),
                        Color.Transparent
                    ),
                    start = beamOrigin,
                    end = Offset(beamTargetX, beamTargetY)
                )
            )

            // Floating dust particles caught in the light
            for (i in 0 until 24) {
                val seed = i * 0.137f
                val px = ((seed + drift * (0.18f + (i % 4) * 0.06f)) % 1f) * size.width
                val py = ((0.12f + (i * 0.036f) + sin((drift + seed) * 6.28f) * 0.025f)) * size.height
                val alpha = (0.15f + 0.16f * cos((drift + seed) * 6.28f)).coerceIn(0.05f, 0.35f)
                drawCircle(
                    color = Color(0xFFD6EEF0).copy(alpha = alpha),
                    radius = 1.6f + (i % 3) * 1.1f,
                    center = Offset(px, py)
                )
            }

            // Vignette around edges
            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(Color.Transparent, Color(0xCC040608)),
                    center = Offset(size.width * 0.60f, size.height * 0.48f),
                    radius = size.maxDimension * 0.72f
                )
            )
        }

        // 3. Left Translucent Horror Sidebar (matching reference screenshot composition)
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .width(sidebarWidth)
                .fillMaxHeight()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xD8050709),
                            Color(0xBA070A0D),
                            Color(0x70070A0D)
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 22.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top-left eerie distressed yellow horror title
                Column(modifier = Modifier.padding(top = 14.dp)) {
                    Text(
                        text = "ТЕНЬ\nВЕДЬМЫ",
                        style = MaterialTheme.typography.displayLarge.copy(
                            fontFamily = CormorantDisplayFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 34.sp,
                            lineHeight = 36.sp,
                            letterSpacing = 1.8.sp,
                            shadow = Shadow(
                                color = Color(0xCC000000),
                                offset = Offset(3f, 4f),
                                blurRadius = 8f
                            )
                        ),
                        color = Color(0xFFE0D234)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Чёрная Усадьба",
                        style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.4.sp),
                        color = Color(0xFF9EB0B8)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Minimalist Vertical Horror Menu List
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    HorrorMenuItem(
                        label = "ПРОДОЛЖИТЬ",
                        enabledLook = hasContinue,
                        testTag = "menu_continue_button",
                        onClick = onContinueClick
                    )
                    HorrorMenuItem(
                        label = "НОВАЯ ИГРА",
                        enabledLook = true,
                        testTag = "menu_play_button",
                        onClick = onNewGameClick
                    )
                    HorrorMenuItem(
                        label = "УРОВНИ ДОМА",
                        enabledLook = true,
                        testTag = "menu_chapters_button",
                        onClick = onChaptersClick
                    )
                    HorrorMenuItem(
                        label = "СЮЖЕТ И ГЕРОИ",
                        enabledLook = true,
                        testTag = "menu_lore_button",
                        onClick = onLoreClick
                    )
                    HorrorMenuItem(
                        label = "НАСТРОЙКИ",
                        enabledLook = true,
                        testTag = "menu_settings_button",
                        onClick = onSettingsClick
                    )
                    HorrorMenuItem(
                        label = "ВЫХОД ИЗ ИГРЫ",
                        enabledLook = true,
                        testTag = "menu_exit_button",
                        onClick = onExitClick
                    )
                }

                // Subtle bottom progress footer
                Text(
                    text = "Настя (17) • Пройдено ${progress.completedChapters.size}/5",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = Color(0xFF6B777E),
                    modifier = Modifier.padding(top = 12.dp)
                )
            }
        }
    }
}

@Composable
private fun HorrorMenuItem(
    label: String,
    enabledLook: Boolean,
    testTag: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp, horizontal = 2.dp)
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium.copy(
                fontFamily = MontserratBodyFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                letterSpacing = 1.1.sp,
                shadow = Shadow(
                    color = Color.Black,
                    offset = Offset(1.5f, 2f),
                    blurRadius = 4f
                )
            ),
            color = if (enabledLook) ParchmentText else ParchmentText.copy(alpha = 0.36f)
        )
    }
}
