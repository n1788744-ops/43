package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.model.ChapterConfig
import com.example.model.WorldNoteSpawn
import com.example.ui.theme.BloodCrimson
import com.example.ui.theme.EmeraldWitchlight
import com.example.ui.theme.LanternAmber
import com.example.ui.theme.ManorCardBorder
import com.example.ui.theme.ManorSurface
import com.example.ui.theme.MistSecondary
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.ParchmentText

@Composable
fun NoteReaderOverlay(
    note: WorldNoteSpawn,
    onClose: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianBg.copy(alpha = 0.84f))
            .padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier.widthIn(max = 480.dp).fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            color = ManorSurface,
            border = BorderStroke(1.5.dp, LanternAmber.copy(alpha = 0.7f))
        ) {
            Column(
                modifier = Modifier.padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(note.titleRu, style = MaterialTheme.typography.headlineMedium, color = LanternAmber, textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.height(14.dp))
                Text(note.bodyRu, style = MaterialTheme.typography.bodyLarge, color = ParchmentText)
                Spacer(modifier = Modifier.height(20.dp))
                Button(
                    onClick = onClose,
                    modifier = Modifier.fillMaxWidth().height(48.dp).testTag("note_close_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = LanternAmber, contentColor = Color(0xFF1A1004))
                ) {
                    Text("Запомнить и продолжить", style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}

@Composable
fun PauseOverlay(
    chapterTitle: String,
    objectiveTextRu: String = "",
    onResume: () -> Unit,
    onRestart: () -> Unit,
    onExitToMenu: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianBg.copy(alpha = 0.85f))
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier.widthIn(max = 420.dp).fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            color = ManorSurface,
            border = BorderStroke(1.dp, ManorCardBorder)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Пауза", style = MaterialTheme.typography.headlineLarge, color = ParchmentText)
                Text(chapterTitle, style = MaterialTheme.typography.bodyMedium, color = LanternAmber, textAlign = TextAlign.Center)
                if (objectiveTextRu.isNotBlank()) {
                    Text(
                        text = "Цель: $objectiveTextRu",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MistSecondary,
                        textAlign = TextAlign.Center
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))

                Button(
                    onClick = onResume,
                    modifier = Modifier.fillMaxWidth().height(50.dp).testTag("pause_resume_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = LanternAmber, contentColor = Color(0xFF1A1004))
                ) {
                    Text("Продолжить побег", style = MaterialTheme.typography.labelLarge)
                }
                OutlinedButton(
                    onClick = onRestart,
                    modifier = Modifier.fillMaxWidth().height(48.dp).testTag("pause_restart_button"),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Начать главу заново", color = ParchmentText)
                }
                OutlinedButton(
                    onClick = onExitToMenu,
                    modifier = Modifier.fillMaxWidth().height(48.dp).testTag("pause_menu_button"),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Выйти в главное меню", color = MistSecondary)
                }
            }
        }
    }
}

@Composable
fun CaughtByWitchOverlay(
    chapterTitle: String,
    onRetry: () -> Unit,
    onExitToMenu: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xE81C040A))
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier.widthIn(max = 440.dp).fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            color = ManorSurface,
            border = BorderStroke(2.dp, BloodCrimson)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("ВЕДЬМА ПОЙМАЛА НАСТЮ", style = MaterialTheme.typography.headlineMedium, color = BloodCrimson, textAlign = TextAlign.Center)
                Text(chapterTitle, style = MaterialTheme.typography.bodyMedium, color = MistSecondary)
                Text(
                    "Совет: выключайте фонарь, когда ведьма рядом, прячьтесь в шкафах и используйте соляные обереги!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = ParchmentText,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(6.dp))
                Button(
                    onClick = onRetry,
                    modifier = Modifier.fillMaxWidth().height(52.dp).testTag("caught_retry_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BloodCrimson, contentColor = Color.White)
                ) {
                    Text("Попытаться снова", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                }
                OutlinedButton(
                    onClick = onExitToMenu,
                    modifier = Modifier.fillMaxWidth().height(48.dp).testTag("caught_menu_button"),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("В главное меню", color = MistSecondary)
                }
            }
        }
    }
}

@Composable
fun ChapterTwistOutcomeOverlay(
    chapter: ChapterConfig,
    elapsedSeconds: Int,
    onNextChapter: () -> Unit,
    onExitToMenu: () -> Unit
) {
    val isFinal = chapter.id.number == 5
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianBg.copy(alpha = 0.92f))
            .padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier.widthIn(max = 540.dp).fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            color = ManorSurface,
            border = BorderStroke(2.dp, if (isFinal) LanternAmber else EmeraldWitchlight)
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_nastya_portrait),
                    contentDescription = "Настя",
                    modifier = Modifier.size(84.dp).clip(CircleShape).border(2.dp, EmeraldWitchlight, CircleShape),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = if (isFinal) "ПОБЕГ ЗАВЕРШЁН!" else "ГЛАВА ПРОЙДЕНА",
                    style = MaterialTheme.typography.labelLarge,
                    color = EmeraldWitchlight
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = chapter.twistTitleRu,
                    style = MaterialTheme.typography.headlineMedium,
                    color = LanternAmber,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "${chapter.twistSubtitleRu} • Время: ${elapsedSeconds} сек",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MistSecondary
                )
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = chapter.twistStoryRu,
                    style = MaterialTheme.typography.bodyLarge,
                    color = ParchmentText
                )
                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = if (isFinal) onExitToMenu else onNextChapter,
                    modifier = Modifier.fillMaxWidth().height(54.dp).testTag("twist_next_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = LanternAmber, contentColor = Color(0xFF1A1004))
                ) {
                    Text(
                        text = if (isFinal) "Вернуться в главное меню" else "Перейти к следующей главе",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
                if (!isFinal) {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = onExitToMenu,
                        modifier = Modifier.fillMaxWidth().height(46.dp)
                    ) {
                        Text("В главное меню", color = MistSecondary)
                    }
                }
            }
        }
    }
}

@Composable
fun BadEndingDinnerOverlay(
    chapterTitle: String,
    onRetry: () -> Unit,
    onExitToMenu: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xF0180308))
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier.widthIn(max = 460.dp).fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            color = ManorSurface,
            border = BorderStroke(2.dp, BloodCrimson)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "ПЛОХАЯ КОНЦОВКА • КУХНЯ",
                    style = MaterialTheme.typography.labelLarge,
                    color = BloodCrimson
                )
                Text(
                    text = "«Вода закипела, пора ужинать!»",
                    style = MaterialTheme.typography.headlineMedium,
                    color = ParchmentText,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "$chapterTitle\n\nКотёл закипел, и ведьма отворила клетку в третий раз. Прячьте ценные предметы под кроватью и взламывайте замок за 30 секунд!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MistSecondary,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(6.dp))
                Button(
                    onClick = onRetry,
                    modifier = Modifier.fillMaxWidth().height(52.dp).testTag("bad_dinner_retry_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BloodCrimson, contentColor = Color.White)
                ) {
                    Text("Начать главу заново", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                }
                OutlinedButton(
                    onClick = onExitToMenu,
                    modifier = Modifier.fillMaxWidth().height(48.dp).testTag("bad_dinner_menu_button"),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("В главное меню", color = MistSecondary)
                }
            }
        }
    }
}

@Composable
fun PoliceRescuedOverlay(
    chapter: ChapterConfig,
    elapsedSeconds: Int,
    onNextChapter: () -> Unit,
    onExitToMenu: () -> Unit
) {
    val isFinal = chapter.id.number == 5
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xEE08121E))
            .padding(22.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier.widthIn(max = 520.dp).fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            color = ManorSurface,
            border = BorderStroke(2.dp, EmeraldWitchlight)
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_nastya_portrait),
                    contentDescription = "Настя",
                    modifier = Modifier.size(84.dp).clip(CircleShape).border(2.dp, EmeraldWitchlight, CircleShape),
                    contentScale = ContentScale.Crop
                )
                Text(
                    text = "АЛЬТЕРНАТИВНЫЙ ФИНАЛ • 112",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color(0xFF5CA8FF)
                )
                Text(
                    text = "ПОЛИЦИЯ ПРОРВАЛАСЬ В УСАДЬБУ!",
                    style = MaterialTheme.typography.headlineMedium,
                    color = EmeraldWitchlight,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "${chapter.titleRu} • Время: ${elapsedSeconds} сек",
                    style = MaterialTheme.typography.bodyMedium,
                    color = LanternAmber
                )
                Text(
                    text = "Настя передала 3 неопровержимых снимка Полароида (котёл и книга рецептов, рюкзаки пропавших подростков и фото самой ведьмы), починила телефон и пережила 3 минуты яростной охоты. Полицейские сирены и штурмовая группа выбили ворота и спасли пленниц!",
                    style = MaterialTheme.typography.bodyLarge,
                    color = ParchmentText,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = if (isFinal) onExitToMenu else onNextChapter,
                    modifier = Modifier.fillMaxWidth().height(54.dp).testTag("police_next_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldWitchlight, contentColor = Color(0xFF051C12))
                ) {
                    Text(
                        text = if (isFinal) "В главное меню" else "Перейти к следующей главе",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
                if (!isFinal) {
                    OutlinedButton(
                        onClick = onExitToMenu,
                        modifier = Modifier.fillMaxWidth().height(46.dp)
                    ) {
                        Text("В главное меню", color = MistSecondary)
                    }
                }
            }
        }
    }
}

