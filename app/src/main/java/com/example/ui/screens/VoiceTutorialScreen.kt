package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.Gamepad
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.TouchApp
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.model.DialogueLine
import com.example.ui.theme.BloodCrimson
import com.example.ui.theme.EmeraldWitchlight
import com.example.ui.theme.LanternAmber
import com.example.ui.theme.ManorCardBorder
import com.example.ui.theme.ManorSurface
import com.example.ui.theme.MistSecondary
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.ParchmentText
import kotlinx.coroutines.delay

private data class TutorialStep(
    val titleRu: String,
    val bodyRu: String,
    val voiceScriptRu: String,
    val icon: ImageVector,
    val accent: Color
)

/**
 * Spoken Russian Voice Tutorial screen shown before entering the Witch's house.
 * Nastya verbally explains mobile controls, real doors, hiding, weapons, and survival mechanics.
 */
@Composable
fun VoiceTutorialScreen(
    dialogue: DialogueLine?,
    onSpeakStep: (String) -> Unit,
    onContinueToGame: () -> Unit,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)

    val steps = remember {
        listOf(
            TutorialStep(
                titleRu = "1. Движение и обзор от 1-го лица",
                bodyRu = "Используй левый джойстик для ходьбы и кнопку «Бег» для ускорения. Проводи пальцем по правой части экрана, чтобы осматриваться вокруг.",
                voiceScriptRu = "Привет! Я Настя, и перед входом в дом я быстро расскажу, как управлять игрой. Слева находится джойстик движения и кнопка бега, а справа просто проводи пальцем по экрану, чтобы осматриваться по сторонам.",
                icon = Icons.Default.Gamepad,
                accent = LanternAmber
            ),
            TutorialStep(
                titleRu = "2. Настоящие комнаты и двери",
                bodyRu = "Дом состоит из настоящих комнат с дверьми. Подходи к закрытой двери, шкафу или предмету и нажимай зелёную кнопку «Открыть / Взять».",
                voiceScriptRu = "В этом новом доме настоящие комнаты и деревянные двери. Подходи к двери, записке или светящемуся предмету и жми зелёную кнопку действия. А в шкафах можно прятаться от ведьмы!",
                icon = Icons.Default.MeetingRoom,
                accent = EmeraldWitchlight
            ),
            TutorialStep(
                titleRu = "3. Оружие и самооборона",
                bodyRu = "Ищи кочергу, серебряный клинок и охотничий арбалет! Кнопка «Атака» бьёт или стреляет болтом, оглушая ведьму на несколько секунд.",
                voiceScriptRu = "Самое важное — в доме спрятано оружие! Уже в подвале-темнице найди кочергу, а дальше клинок и арбалет с серебряными болтами. Кнопка атаки оглушит ведьму и отбросит её назад!",
                icon = Icons.Default.Security,
                accent = BloodCrimson
            ),
            TutorialStep(
                titleRu = "4. Рюкзак (3 слота) и Проклятые вещи",
                bodyRu = "В рюкзак помещается только 3 предмета — прячьте лишнее в тайниках под кроватью, чтобы ведьма не отобрала при поимке!",
                voiceScriptRu = "В рюкзак помещаются всего три вещи! Прячь ценное под кроватью. Помни: проклятое зеркальце подскажет где ведьма, но звенит при беге; чёрная свеча светит в темноте, но пахнет; а тяжёлый ключ замедляет шаг!",
                icon = Icons.Default.FlashlightOn,
                accent = LanternAmber
            ),
            TutorialStep(
                titleRu = "5. Кухня, Полароид и звонок 112",
                bodyRu = "При поимке у вас 3 попытки и 30 сек на взлом клетки. Либо снимите 3 улики на Полароид, почините телефон и продержитесь 3 минуты до полиции!",
                voiceScriptRu = "Если ведьма поймает — у нас будет тридцать секунд на взлом клетки на кухне. А ещё можешь нажать кнопку камеры, снять котёл, паспорта и саму ведьму, починить телефон изолентой, включить рубильник и вызвать сто двенадцать!",
                icon = Icons.Default.TouchApp,
                accent = EmeraldWitchlight
            )
        )
    }

    var activeStepIndex by remember { mutableIntStateOf(0) }
    var replayTrigger by remember { mutableIntStateOf(0) }

    LaunchedEffect(replayTrigger) {
        for (i in steps.indices) {
            activeStepIndex = i
            onSpeakStep(steps[i].voiceScriptRu)
            delay(8500L)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(listOf(ObsidianBg, Color(0xFF171024), ObsidianBg))
            )
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack, modifier = Modifier.testTag("tutorial_back_button")) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад", tint = ParchmentText)
                }
                Text(
                    text = "Инструктаж перед входом",
                    style = MaterialTheme.typography.headlineMedium,
                    color = ParchmentText
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.widthIn(max = 560.dp).fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_nastya_portrait),
                    contentDescription = "Настя объясняет управление",
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .border(2.dp, EmeraldWitchlight, CircleShape),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Настя (17 лет) рассказывает правила", style = MaterialTheme.typography.titleMedium, color = LanternAmber)
                    Text(
                        text = dialogue?.textRu ?: steps[activeStepIndex].voiceScriptRu,
                        style = MaterialTheme.typography.bodyMedium,
                        color = ParchmentText
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Column(
                modifier = Modifier.widthIn(max = 560.dp).fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                steps.forEachIndexed { idx, step ->
                    val isCurrent = idx == activeStepIndex
                    Surface(
                        onClick = {
                            activeStepIndex = idx
                            onSpeakStep(step.voiceScriptRu)
                        },
                        shape = RoundedCornerShape(16.dp),
                        color = if (isCurrent) ManorSurface.copy(alpha = 0.96f) else ManorSurface.copy(alpha = 0.55f),
                        border = BorderStroke(
                            if (isCurrent) 1.5.dp else 1.dp,
                            if (isCurrent) step.accent else ManorCardBorder
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(step.icon, contentDescription = null, tint = step.accent, modifier = Modifier.size(26.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(step.titleRu, style = MaterialTheme.typography.titleMedium, color = ParchmentText)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(step.bodyRu, style = MaterialTheme.typography.bodyMedium, color = MistSecondary)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Column(
                modifier = Modifier.widthIn(max = 480.dp).fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onContinueToGame,
                    modifier = Modifier.fillMaxWidth().height(56.dp).testTag("tutorial_continue_button"),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = LanternAmber, contentColor = Color(0xFF1A1004))
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Я поняла! Войти в дом", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                }

                OutlinedButton(
                    onClick = { replayTrigger++ },
                    modifier = Modifier.fillMaxWidth().height(48.dp).testTag("tutorial_replay_button"),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, EmeraldWitchlight.copy(alpha = 0.6f))
                ) {
                    Icon(Icons.Default.RecordVoiceOver, contentDescription = null, tint = EmeraldWitchlight)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Озвучить всё заново", color = ParchmentText)
                }
            }
        }
    }
}
