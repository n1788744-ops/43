package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ChapterConfig
import com.example.model.PuzzleType
import com.example.ui.theme.ArcaneViolet
import com.example.ui.theme.BloodCrimson
import com.example.ui.theme.EmeraldWitchlight
import com.example.ui.theme.LanternAmber
import com.example.ui.theme.ManorCardBorder
import com.example.ui.theme.ManorSurface
import com.example.ui.theme.ManorSurfaceElevated
import com.example.ui.theme.MistSecondary
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.ParchmentText
import kotlinx.coroutines.delay

@Composable
fun PuzzleDialogOverlay(
    chapter: ChapterConfig,
    puzzleType: PuzzleType,
    onSolved: () -> Unit,
    onFailedAttempt: () -> Unit,
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianBg.copy(alpha = 0.88f))
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .widthIn(max = 520.dp)
                .fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            color = ManorSurface,
            border = BorderStroke(1.5.dp, EmeraldWitchlight.copy(alpha = 0.65f))
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = chapter.puzzleTitleRu,
                        style = MaterialTheme.typography.headlineMedium,
                        color = LanternAmber,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.testTag("puzzle_close_button")) {
                        Icon(Icons.Default.Close, contentDescription = "Закрыть головоломку", tint = ParchmentText)
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = ManorSurfaceElevated,
                    border = BorderStroke(1.dp, ManorCardBorder)
                ) {
                    Text(
                        text = chapter.puzzleClueRu,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MistSecondary,
                        modifier = Modifier.padding(12.dp)
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                when (puzzleType) {
                    PuzzleType.DUNGEON_SHACKLES -> DungeonShacklesPuzzle(onSolved, onFailedAttempt)
                    PuzzleType.KITCHEN_BUFFET -> SequenceChoicePuzzle(
                        options = listOf("🍞 Хлеб", "🍷 Вино", "🍎 Яблоко", "🏆 Чаша", "🧀 Сыр", "🕯 Свеча"),
                        target = listOf("🍞 Хлеб", "🍷 Вино", "🍎 Яблоко", "🏆 Чаша"),
                        prompt = "Соберите порядок подачи блюд в буфете:",
                        onSolved = onSolved,
                        onFailedAttempt = onFailedAttempt
                    )
                    PuzzleType.BEDROOM_MUSICBOX -> SequenceChoicePuzzle(
                        options = listOf("🌙 Ночь", "🪶 Перо", "🪞 Зеркало", "☀ Рассвет", "🗝 Ключ", "🌹 Роза"),
                        target = listOf("🌙 Ночь", "🪶 Перо", "🪞 Зеркало", "☀ Рассвет"),
                        prompt = "Нажмите ноты на шкатулке снов:",
                        onSolved = onSolved,
                        onFailedAttempt = onFailedAttempt
                    )
                    PuzzleType.CHAPEL_ORGAN -> SequenceChoicePuzzle(
                        options = listOf("ДО (Тьма)", "МИ (Ветер)", "СОЛЬ (Серебро)", "СИ (Рассвет)", "ФА (Туман)"),
                        target = listOf("ДО (Тьма)", "МИ (Ветер)", "СОЛЬ (Серебро)", "СИ (Рассвет)"),
                        prompt = "Нажмите клавиши церковного органа по нотам:",
                        onSolved = onSolved,
                        onFailedAttempt = onFailedAttempt
                    )
                    PuzzleType.TOWER_CROWN -> TowerCrownMemoryPuzzle(onSolved, onFailedAttempt)
                }
            }
        }
    }
}

@Composable
private fun DungeonShacklesPuzzle(
    onSolved: () -> Unit,
    onFailedAttempt: () -> Unit
) {
    val options = listOf("⛓ Железо", "🔥 Уголь", "🗝 Ключ", "💧 Вода")
    var slot0 by remember { mutableIntStateOf(3) }
    var slot1 by remember { mutableIntStateOf(0) }
    var slot2 by remember { mutableIntStateOf(1) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    Text(
        "Нажимайте на замки каземата, чтобы выставить символы:",
        style = MaterialTheme.typography.bodyMedium,
        color = ParchmentText,
        textAlign = TextAlign.Center
    )
    Spacer(modifier = Modifier.height(14.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        val slots = listOf(slot0, slot1, slot2)
        slots.forEachIndexed { index, valueIdx ->
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .height(92.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .clickable {
                        errorMsg = null
                        when (index) {
                            0 -> slot0 = (slot0 + 1) % options.size
                            1 -> slot1 = (slot1 + 1) % options.size
                            else -> slot2 = (slot2 + 1) % options.size
                        }
                    }
                    .testTag("shackle_slot_$index"),
                shape = RoundedCornerShape(16.dp),
                color = ManorSurfaceElevated,
                border = BorderStroke(1.5.dp, EmeraldWitchlight.copy(alpha = 0.65f))
            ) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("${index + 1}", style = MaterialTheme.typography.labelSmall, color = MistSecondary)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        options[valueIdx],
                        style = MaterialTheme.typography.titleMedium,
                        color = ParchmentText,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }

    if (errorMsg != null) {
        Spacer(modifier = Modifier.height(10.dp))
        Text(errorMsg!!, style = MaterialTheme.typography.bodyMedium, color = BloodCrimson)
    }

    Spacer(modifier = Modifier.height(18.dp))
    Button(
        onClick = {
            if (slot0 == 0 && slot1 == 1 && slot2 == 2) {
                onSolved()
            } else {
                errorMsg = "Цепи зазвенели — неверный порядок! Ведьма слышит..."
                onFailedAttempt()
            }
        },
        modifier = Modifier.fillMaxWidth().height(50.dp).testTag("shackle_submit_button"),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(containerColor = LanternAmber, contentColor = Color(0xFF1A1004))
    ) {
        Text("Открыть Замок Каземата", style = MaterialTheme.typography.labelLarge)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SequenceChoicePuzzle(
    options: List<String>,
    target: List<String>,
    prompt: String,
    onSolved: () -> Unit,
    onFailedAttempt: () -> Unit
) {
    val entered = remember { mutableStateListOf<String>() }
    var feedback by remember { mutableStateOf<String?>(null) }

    Text(prompt, style = MaterialTheme.typography.bodyMedium, color = ParchmentText, textAlign = TextAlign.Center)
    Spacer(modifier = Modifier.height(10.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        for (i in target.indices) {
            val label = entered.getOrNull(i) ?: "—"
            Surface(
                modifier = Modifier.weight(1f).height(52.dp),
                shape = RoundedCornerShape(12.dp),
                color = ManorSurfaceElevated,
                border = BorderStroke(1.dp, if (entered.size > i) EmeraldWitchlight else ManorCardBorder)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(4.dp)) {
                    Text(label, style = MaterialTheme.typography.labelSmall, color = ParchmentText, textAlign = TextAlign.Center)
                }
            }
        }
    }

    Spacer(modifier = Modifier.height(14.dp))
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        options.forEach { sym ->
            OutlinedButton(
                onClick = {
                    feedback = null
                    if (entered.size < target.size) {
                        entered.add(sym)
                        if (entered.size == target.size) {
                            if (entered.toList() == target) {
                                onSolved()
                            } else {
                                feedback = "Фальшивый аккорд! Шум привлёк ведьму!"
                                entered.clear()
                                onFailedAttempt()
                            }
                        }
                    }
                },
                modifier = Modifier.height(48.dp),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, LanternAmber.copy(alpha = 0.55f))
            ) {
                Text(sym, color = ParchmentText)
            }
        }
    }

    if (feedback != null) {
        Spacer(modifier = Modifier.height(10.dp))
        Text(feedback!!, style = MaterialTheme.typography.bodyMedium, color = BloodCrimson, textAlign = TextAlign.Center)
    }

    Spacer(modifier = Modifier.height(12.dp))
    OutlinedButton(onClick = { entered.clear(); feedback = null }) {
        Text("Сбросить", color = MistSecondary)
    }
}

@Composable
private fun TowerCrownMemoryPuzzle(
    onSolved: () -> Unit,
    onFailedAttempt: () -> Unit
) {
    val nodeLabels = listOf("I", "II", "III", "IV", "V")
    var round by remember { mutableIntStateOf(1) }
    val roundSequences = remember {
        listOf(
            listOf(0, 2, 4),
            listOf(1, 3, 0, 4),
            listOf(2, 0, 4, 1, 3)
        )
    }
    var highlightedNode by remember { mutableIntStateOf(-1) }
    var isShowingSequence by remember { mutableStateOf(true) }
    val playerInput = remember { mutableStateListOf<Int>() }
    var statusText by remember { mutableStateOf("Круг 1/3: запомните вспышки Короны Ворона") }
    var triggerReplay by remember { mutableIntStateOf(0) }

    LaunchedEffect(round, triggerReplay) {
        isShowingSequence = true
        playerInput.clear()
        statusText = "Круг $round/3: смотрите внимательно..."
        delay(450L)
        val seq = roundSequences[round - 1]
        for (node in seq) {
            highlightedNode = node
            delay(480L)
            highlightedNode = -1
            delay(210L)
        }
        isShowingSequence = false
        statusText = "Круг $round/3: повторите порядок (${seq.size} перьев)"
    }

    Text(
        text = statusText,
        style = MaterialTheme.typography.titleMedium,
        color = if (isShowingSequence) LanternAmber else EmeraldWitchlight,
        textAlign = TextAlign.Center
    )
    Spacer(modifier = Modifier.height(16.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        nodeLabels.forEachIndexed { idx, label ->
            val lit = highlightedNode == idx
            Surface(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .clickable(enabled = !isShowingSequence) {
                        val expected = roundSequences[round - 1]
                        val nextIdx = playerInput.size
                        if (nextIdx < expected.size && expected[nextIdx] == idx) {
                            playerInput.add(idx)
                            if (playerInput.size == expected.size) {
                                if (round == 3) onSolved() else round++
                            }
                        } else {
                            statusText = "Корона вспыхнула алым! Попробуйте снова!"
                            onFailedAttempt()
                            triggerReplay++
                        }
                    }
                    .testTag("crown_node_$idx"),
                shape = CircleShape,
                color = if (lit) LanternAmber else ManorSurfaceElevated,
                border = BorderStroke(2.dp, if (lit) Color.White else ArcaneViolet.copy(alpha = 0.8f))
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = label,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (lit) ObsidianBg else ParchmentText
                    )
                }
            }
        }
    }

    Spacer(modifier = Modifier.height(14.dp))
    Text(
        "Введено: ${playerInput.size} / ${roundSequences[round - 1].size}",
        style = MaterialTheme.typography.bodyMedium,
        color = MistSecondary
    )
    Spacer(modifier = Modifier.height(8.dp))
    OutlinedButton(
        onClick = { if (!isShowingSequence) triggerReplay++ },
        enabled = !isShowingSequence
    ) {
        Text("Повторить показ", color = MistSecondary)
    }
}
