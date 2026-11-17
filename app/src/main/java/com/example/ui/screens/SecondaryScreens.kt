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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.ChapterCatalog
import com.example.model.ChapterConfig
import com.example.model.ChapterId
import com.example.model.DialogueLine
import com.example.model.PlayerProgress
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChapterSelectScreen(
    progress: PlayerProgress,
    onSelectChapter: (ChapterId) -> Unit,
    onToggleFreeAccess: (Boolean) -> Unit,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianBg)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        TopAppBar(
            title = { Text("Главы особняка", style = MaterialTheme.typography.headlineMedium) },
            navigationIcon = {
                IconButton(onClick = onBack, modifier = Modifier.testTag("chapters_back_button")) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = ObsidianBg,
                titleContentColor = ParchmentText,
                navigationIconContentColor = ParchmentText
            )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Optional unlock-all switch for exploring any chapter
            Surface(
                modifier = Modifier.widthIn(max = 600.dp).fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = ManorSurface,
                border = BorderStroke(1.dp, ManorCardBorder)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Свободный выбор глав", style = MaterialTheme.typography.titleMedium, color = ParchmentText)
                        Text(
                            "Открыть доступ ко всем локациям сразу",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MistSecondary
                        )
                    }
                    Switch(
                        checked = progress.freeChapterAccess,
                        onCheckedChange = onToggleFreeAccess,
                        modifier = Modifier.testTag("free_chapters_switch"),
                        colors = SwitchDefaults.colors(checkedThumbColor = ObsidianBg, checkedTrackColor = EmeraldWitchlight)
                    )
                }
            }

            ChapterCatalog.chapters.forEach { ch ->
                val isUnlocked = progress.freeChapterAccess || ch.id.number <= progress.highestUnlockedChapter
                val isCompleted = progress.completedChapters.contains(ch.id.number)
                val bestTime = progress.bestTimesSec[ch.id.number]

                Card(
                    modifier = Modifier.widthIn(max = 600.dp).fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = if (isUnlocked) ManorSurface else ManorSurface.copy(alpha = 0.55f)),
                    border = BorderStroke(
                        1.dp,
                        if (isCompleted) EmeraldWitchlight.copy(alpha = 0.6f)
                        else if (isUnlocked) LanternAmber.copy(alpha = 0.45f)
                        else ManorCardBorder
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(ch.titleRu, style = MaterialTheme.typography.titleLarge, color = ParchmentText)
                                Text(ch.locationRu, style = MaterialTheme.typography.bodyMedium, color = LanternAmber)
                                Text(ch.roomsListRu, style = MaterialTheme.typography.labelSmall, color = EmeraldWitchlight)
                            }
                            if (isCompleted) {
                                Icon(Icons.Default.CheckCircle, contentDescription = "Пройдено", tint = EmeraldWitchlight)
                            } else if (!isUnlocked) {
                                Icon(Icons.Default.Lock, contentDescription = "Заблокировано", tint = MistSecondary)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Difficulty stars (1..5)
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text("Сложность: ", style = MaterialTheme.typography.labelSmall, color = MistSecondary)
                            for (i in 1..5) {
                                Icon(
                                    imageVector = if (i <= ch.difficultyStars) Icons.Default.Star else Icons.Default.StarOutline,
                                    contentDescription = null,
                                    tint = if (i <= ch.difficultyStars) BloodCrimson else MistSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            if (bestTime != null) {
                                Spacer(modifier = Modifier.width(12.dp))
                                Text("Рекорд: ${bestTime}с", style = MaterialTheme.typography.labelSmall, color = EmeraldWitchlight)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(ch.summaryRu, style = MaterialTheme.typography.bodyMedium, color = MistSecondary)
                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = { onSelectChapter(ch.id) },
                            enabled = isUnlocked,
                            modifier = Modifier.fillMaxWidth().height(48.dp).testTag("play_chapter_${ch.id.number}"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isUnlocked) LanternAmber else ManorSurfaceElevated,
                                contentColor = Color(0xFF1A1004)
                            )
                        ) {
                            Icon(
                                imageVector = if (isUnlocked) Icons.Default.PlayArrow else Icons.Default.Lock,
                                contentDescription = null
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isUnlocked) "Войти в главу" else "Пройдите предыдущую главу",
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun ChapterIntroScreen(
    chapter: ChapterConfig,
    dialogue: DialogueLine?,
    onStartChapter: () -> Unit,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(ObsidianBg, Color(0xFF1A1026), ObsidianBg)
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
                IconButton(onClick = onBack, modifier = Modifier.testTag("intro_back_button")) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад", tint = ParchmentText)
                }
            }

            Image(
                painter = painterResource(id = R.drawable.img_nastya_portrait),
                contentDescription = "Настя, 17 лет",
                modifier = Modifier
                    .size(112.dp)
                    .clip(CircleShape)
                    .border(2.dp, LanternAmber, CircleShape),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.height(16.dp))
            Text(chapter.titleRu, style = MaterialTheme.typography.headlineLarge, color = ParchmentText)
            Text(chapter.subtitleRu, style = MaterialTheme.typography.titleMedium, color = EmeraldWitchlight)
            Spacer(modifier = Modifier.height(20.dp))

            Column(
                modifier = Modifier.widthIn(max = 560.dp).fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                chapter.introStoryLines.forEach { line ->
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = ManorSurface.copy(alpha = 0.85f),
                        border = BorderStroke(1.dp, ManorCardBorder)
                    ) {
                        Text(
                            text = line,
                            style = MaterialTheme.typography.bodyLarge,
                            color = ParchmentText,
                            modifier = Modifier.padding(14.dp)
                        )
                    }
                }
            }

            if (dialogue != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Surface(
                    modifier = Modifier.widthIn(max = 560.dp).fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = EmeraldWitchlight.copy(alpha = 0.14f),
                    border = BorderStroke(1.dp, EmeraldWitchlight.copy(alpha = 0.55f))
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.RecordVoiceOver, contentDescription = null, tint = EmeraldWitchlight)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "«${dialogue.textRu}»",
                            style = MaterialTheme.typography.bodyMedium,
                            color = ParchmentText
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f, fill = true))
            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = onStartChapter,
                modifier = Modifier
                    .widthIn(max = 460.dp)
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("start_gameplay_button"),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = LanternAmber, contentColor = Color(0xFF1A1004))
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Осмотреться в комнате", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    progress: PlayerProgress,
    onVolumeChange: (Float) -> Unit,
    onVoiceEnabledChange: (Boolean) -> Unit,
    onSensitivityChange: (Float) -> Unit,
    onTestVoice: () -> Unit,
    onOpenTutorial: () -> Unit,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianBg)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        TopAppBar(
            title = { Text("Настройки", style = MaterialTheme.typography.headlineMedium) },
            navigationIcon = {
                IconButton(onClick = onBack, modifier = Modifier.testTag("settings_back_button")) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = ObsidianBg,
                titleContentColor = ParchmentText,
                navigationIconContentColor = ParchmentText
            )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Surface(
                modifier = Modifier.widthIn(max = 560.dp).fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                color = ManorSurface,
                border = BorderStroke(1.dp, ManorCardBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Громкость тревожного звука: ${(progress.soundVolume * 100).toInt()}%",
                        style = MaterialTheme.typography.titleMedium,
                        color = ParchmentText
                    )
                    Slider(
                        value = progress.soundVolume,
                        onValueChange = onVolumeChange,
                        colors = SliderDefaults.colors(thumbColor = LanternAmber, activeTrackColor = LanternAmber)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        "Чувствительность обзора: ${"%.1f".format(progress.lookSensitivity)}x",
                        style = MaterialTheme.typography.titleMedium,
                        color = ParchmentText
                    )
                    Slider(
                        value = progress.lookSensitivity,
                        onValueChange = onSensitivityChange,
                        valueRange = 0.5f..2.0f,
                        colors = SliderDefaults.colors(thumbColor = EmeraldWitchlight, activeTrackColor = EmeraldWitchlight)
                    )
                }
            }

            Surface(
                modifier = Modifier.widthIn(max = 560.dp).fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                color = ManorSurface,
                border = BorderStroke(1.dp, ManorCardBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Русская озвучка Насти", style = MaterialTheme.typography.titleMedium, color = ParchmentText)
                            Text("Голос 17-летней девушки и субтитры", style = MaterialTheme.typography.bodyMedium, color = MistSecondary)
                        }
                        Switch(
                            checked = progress.voiceEnabled,
                            onCheckedChange = onVoiceEnabledChange,
                            colors = SwitchDefaults.colors(checkedThumbColor = ObsidianBg, checkedTrackColor = LanternAmber)
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = onTestVoice,
                        modifier = Modifier.fillMaxWidth().height(48.dp).testTag("test_voice_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldWitchlight, contentColor = Color(0xFF041D14))
                    ) {
                        Icon(Icons.Default.RecordVoiceOver, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Проверить голос Насти")
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedButton(
                        onClick = onOpenTutorial,
                        modifier = Modifier.fillMaxWidth().height(48.dp).testTag("open_tutorial_from_settings_button"),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, LanternAmber.copy(alpha = 0.7f))
                    ) {
                        Icon(Icons.Default.MenuBook, contentDescription = null, tint = LanternAmber)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Голосовой инструктаж Насти", color = ParchmentText)
                    }
                }
            }

            Surface(
                modifier = Modifier.widthIn(max = 560.dp).fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                color = ManorSurface,
                border = BorderStroke(1.dp, ManorCardBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Мобильное управление и новые функции", style = MaterialTheme.typography.titleMedium, color = LanternAmber)
                    Text("• Левый джойстик: движение Насти (потяните до края или зажмите «Бег» для ускорения).", style = MaterialTheme.typography.bodyMedium, color = MistSecondary)
                    Text("• Правая часть экрана: поворот камеры от первого лица; зелёная кнопка открывает двери, шкафы и тайники под кроватью.", style = MaterialTheme.typography.bodyMedium, color = MistSecondary)
                    Text("• Рюкзак (3 слота) и Проклятые предметы: прячьте вещи под кровать! Зеркальце показывает комнату ведьмы (но звенит при беге), Чёрная свеча освещает знаки (ведьма чует запах), Тяжёлый ключ снижает скорость бега на 20%.", style = MaterialTheme.typography.bodyMedium, color = MistSecondary)
                    Text("• Кухня и 3 попытки: при поимке у вас есть 30 сек на взлом клетки. На 2-й раз ведьма убирает рюкзак в сундук на кухне!", style = MaterialTheme.typography.bodyMedium, color = MistSecondary)
                    Text("• Полароид и звонок 112: снимите котёл, паспорта и саму ведьму (вспышка выдаёт!), замотайте провод изолентой, включите рубильник и продержитесь 3:00 под нарастающие сирены!", style = MaterialTheme.typography.bodyMedium, color = MistSecondary)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CharacterLoreScreen(onBack: () -> Unit) {
    BackHandler(onBack = onBack)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianBg)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        TopAppBar(
            title = { Text("Герои и тайна дома", style = MaterialTheme.typography.headlineMedium) },
            navigationIcon = {
                IconButton(onClick = onBack, modifier = Modifier.testTag("lore_back_button")) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = ObsidianBg,
                titleContentColor = ParchmentText,
                navigationIconContentColor = ParchmentText
            )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Surface(
                modifier = Modifier.widthIn(max = 560.dp).fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                color = ManorSurface,
                border = BorderStroke(1.dp, LanternAmber.copy(alpha = 0.5f))
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(id = R.drawable.img_nastya_portrait),
                        contentDescription = "Настя",
                        modifier = Modifier.size(72.dp).clip(CircleShape).border(2.dp, LanternAmber, CircleShape),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text("Настя (17 лет)", style = MaterialTheme.typography.titleLarge, color = ParchmentText)
                        Text(
                            "Главная героиня. Пришла в себя за решёткой в подвале-темнице Чёрной Усадьбы. Быстро учится обращаться с оружием, взламывать магические замки и спасать подруг.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MistSecondary
                        )
                    }
                }
            }

            LoreCard(
                title = "Лена (17 лет) — Пойманная подруга",
                accent = EmeraldWitchlight,
                body = "Была схвачена раньше Насти. Оставляла подсказки на кухне, в часовне и в башне, а также спрятала охотничий арбалет для отпора ведьме."
            )
            LoreCard(
                title = "Маша (17 лет) — Третья пленница",
                accent = LanternAmber,
                body = "Её держали в спальне второго этажа. Её дневники раскрывают тайну проклятого особняка у болота и способ разрушить Корону Воронов."
            )
            LoreCard(
                title = "Мать Воронов — Хозяйка Чёрной Усадьбы",
                accent = ArcaneViolet,
                body = "Древняя ведьма, которая сама умеет открывать двери и проверять шкафы на верхних этажах. Теперь против неё можно защищаться оружием — но берегитесь ядовитого тумана и засад!"
            )
            LoreCard(
                title = "Оружие против ведьмы",
                accent = BloodCrimson,
                body = "Кованая кочерга (в подвале-темнице) оглушает вблизи; Серебряный клинок (на кухне и в часовне) отбрасывает ведьму; Арбалет ведьмобоя (в спальне и башне) стреляет серебряными болтами с расстояния."
            )
        }
    }
}

@Composable
private fun LoreCard(title: String, accent: Color, body: String) {
    Surface(
        modifier = Modifier.widthIn(max = 560.dp).fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = ManorSurface,
        border = BorderStroke(1.dp, accent.copy(alpha = 0.45f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleLarge, color = accent)
            Spacer(modifier = Modifier.height(6.dp))
            Text(body, style = MaterialTheme.typography.bodyMedium, color = ParchmentText)
        }
    }
}
