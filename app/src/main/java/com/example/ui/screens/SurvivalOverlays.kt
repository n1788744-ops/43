package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backpack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.model.GameHudState
import com.example.model.InventoryItem
import com.example.model.StashContainerType
import com.example.model.StashDialogState
import com.example.ui.theme.BloodCrimson
import com.example.ui.theme.EmeraldWitchlight
import com.example.ui.theme.LanternAmber
import com.example.ui.theme.ManorCardBorder
import com.example.ui.theme.ManorSurface
import com.example.ui.theme.ManorSurfaceElevated
import com.example.ui.theme.MistSecondary
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.ParchmentText
import kotlin.math.ceil
import kotlin.math.sin
import kotlinx.coroutines.delay

@Composable
fun KitchenCageLockpickOverlay(
    hud: GameHudState,
    onUnlocked: () -> Unit
) {
    val totalPins = if (hud.cageHardLock) 5 else 3
    var lockedPins by remember(hud.captureAttempts, hud.cageHardLock) { mutableIntStateOf(0) }
    var phase by remember { mutableFloatStateOf(0f) }
    var missFeedback by remember { mutableStateOf(false) }

    val zoneMin = if (hud.cageHardLock) 0.30f else 0.20f
    val zoneMax = if (hud.cageHardLock) 0.70f else 0.80f
    val speed = if (hud.cageHardLock) 3.7f else 2.5f

    LaunchedEffect(hud.captureAttempts, hud.cageHardLock) {
        while (true) {
            delay(16L)
            phase += 0.016f * speed
        }
    }

    val currentPos = 0.5f + 0.48f * sin(phase)
    val inZone = currentPos in zoneMin..zoneMax
    val secLeft = ceil(hud.cageCountdownSec).toInt().coerceAtLeast(0)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xDE160409))
            .padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier.widthIn(max = 460.dp).fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            color = ManorSurface,
            border = BorderStroke(2.dp, if (secLeft <= 10) BloodCrimson else LanternAmber)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (hud.cageHardLock) "ПОПЫТКА 2/3 • СЛОЖНЫЙ ЗАМОК" else "ПОПЫТКА 1/3 • КЛЕТКА",
                        style = MaterialTheme.typography.labelLarge,
                        color = if (hud.cageHardLock) BloodCrimson else LanternAmber
                    )
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (secLeft <= 10) BloodCrimson.copy(alpha = 0.25f) else Color.Black.copy(alpha = 0.45f),
                        border = BorderStroke(1.dp, if (secLeft <= 10) BloodCrimson else LanternAmber.copy(alpha = 0.6f))
                    ) {
                        Text(
                            text = "${secLeft}с",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = if (secLeft <= 10) BloodCrimson else ParchmentText,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { (hud.cageCountdownSec / 30f).coerceIn(0f, 1f) },
                    modifier = Modifier.fillMaxWidth().height(7.dp).clip(RoundedCornerShape(4.dp)),
                    color = if (secLeft <= 10) BloodCrimson else LanternAmber,
                    trackColor = Color.Black.copy(alpha = 0.6f)
                )

                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = if (hud.cageHardLock) {
                        "Ведьма забрала вещи из рюкзака в сундук! Взломайте $totalPins штифтов, пока вода не закипела."
                    } else {
                        "Настя кричит: «Отпусти!» Взломайте замок ($totalPins штифта), пока ведьма режет травы."
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = ParchmentText,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Pins indicator row
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    for (idx in 0 until totalPins) {
                        val locked = idx < lockedPins
                        val isCurrent = idx == lockedPins
                        Surface(
                            modifier = Modifier.width(46.dp).height(112.dp),
                            shape = RoundedCornerShape(10.dp),
                            color = ManorSurfaceElevated,
                            border = BorderStroke(
                                1.5.dp,
                                when {
                                    locked -> EmeraldWitchlight
                                    isCurrent && inZone -> EmeraldWitchlight
                                    isCurrent -> LanternAmber
                                    else -> ManorCardBorder
                                }
                            )
                        ) {
                            Box(modifier = Modifier.fillMaxSize().padding(5.dp)) {
                                // Sweet zone band
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .fillMaxHeight(if (hud.cageHardLock) 0.40f else 0.56f)
                                        .align(Alignment.Center)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(EmeraldWitchlight.copy(alpha = 0.22f))
                                )
                                // Pin marker
                                val barFraction = when {
                                    locked -> 0.5f
                                    isCurrent -> currentPos
                                    else -> 0.12f
                                }
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = ((1f - barFraction) * 76f).dp)
                                        .height(14.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(
                                            when {
                                                locked -> EmeraldWitchlight
                                                isCurrent && inZone -> EmeraldWitchlight
                                                isCurrent -> LanternAmber
                                                else -> MistSecondary.copy(alpha = 0.4f)
                                            }
                                        )
                                )
                                Icon(
                                    imageVector = if (locked) Icons.Default.LockOpen else Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = if (locked) EmeraldWitchlight else MistSecondary.copy(alpha = 0.7f),
                                    modifier = Modifier.size(16.dp).align(Alignment.BottomCenter)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = if (missFeedback) {
                        "Отмычка соскользнула! Нажимайте, когда ползунок в зелёной зоне."
                    } else {
                        "Штифты: $lockedPins / $totalPins • Жмите в зелёной зоне"
                    },
                    style = MaterialTheme.typography.labelMedium,
                    color = if (missFeedback) BloodCrimson else MistSecondary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        if (inZone) {
                            missFeedback = false
                            val next = lockedPins + 1
                            if (next >= totalPins) {
                                onUnlocked()
                            } else {
                                lockedPins = next
                            }
                        } else {
                            missFeedback = true
                            if (lockedPins > 0) lockedPins--
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(54.dp).testTag("cage_pick_pin_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (inZone) EmeraldWitchlight else LanternAmber,
                        contentColor = Color(0xFF071A10)
                    )
                ) {
                    Text(
                        text = if (inZone) "ЗАЩЕЛКНУТЬ ШТИФТ (${lockedPins + 1}/$totalPins)" else "ПОДНЯТЬ ОТМЫЧКУ",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    }
}

@Composable
fun StashChestOverlay(
    dialogState: StashDialogState,
    hud: GameHudState,
    onStashUnderBed: (Int) -> Unit,
    onTakeFromBedStash: (Int) -> Unit,
    onTakeFromKitchenChest: (Int) -> Unit,
    onSwapPendingPickup: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    val pending = dialogState.pendingPickupItem
    val isChest = dialogState.containerType == StashContainerType.KITCHEN_CHEST

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianBg.copy(alpha = 0.86f))
            .padding(18.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier.widthIn(max = 500.dp).fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            color = ManorSurface,
            border = BorderStroke(1.5.dp, if (pending != null) LanternAmber else EmeraldWitchlight.copy(alpha = 0.7f))
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Backpack,
                        contentDescription = null,
                        tint = if (pending != null) LanternAmber else EmeraldWitchlight
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = when {
                            pending != null -> "Рюкзак полон (3/3)"
                            isChest -> "Сундук на кухне"
                            else -> "Тайник под кроватью"
                        },
                        style = MaterialTheme.typography.headlineSmall,
                        color = ParchmentText
                    )
                }

                if (pending != null) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = LanternAmber.copy(alpha = 0.14f),
                        border = BorderStroke(1.dp, LanternAmber.copy(alpha = 0.6f))
                    ) {
                        Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
                            Text(
                                text = "Найдено: ${pending.symbol} ${pending.nameRu}",
                                style = MaterialTheme.typography.titleMedium,
                                color = LanternAmber
                            )
                            Text(
                                text = pending.descriptionRu,
                                style = MaterialTheme.typography.bodySmall,
                                color = ParchmentText
                            )
                        }
                    }
                    Text(
                        text = "Выберите предмет, который оставите на полу вместо найденного:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MistSecondary
                    )
                    hud.backpackItems.forEachIndexed { idx, item ->
                        StashItemRow(
                            item = item,
                            actionLabel = "Заменить",
                            accent = LanternAmber,
                            onClick = { onSwapPendingPickup(idx) },
                            tag = "swap_slot_$idx"
                        )
                    }
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Не брать находку", color = MistSecondary)
                    }
                } else if (isChest) {
                    Text(
                        text = "Сюда ведьма складывает вещи, отобранные из рюкзака при поимке. Свободно в рюкзаке: ${3 - hud.backpackItems.size}/3.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MistSecondary
                    )
                    if (hud.kitchenChestItems.isEmpty()) {
                        Text(
                            text = "Сундук сейчас пуст.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = ParchmentText.copy(alpha = 0.7f),
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    } else {
                        hud.kitchenChestItems.forEachIndexed { idx, item ->
                            StashItemRow(
                                item = item,
                                actionLabel = if (hud.backpackItems.size < 3) "Забрать" else "Рюкзак 3/3",
                                enabled = hud.backpackItems.size < 3,
                                accent = EmeraldWitchlight,
                                onClick = { onTakeFromKitchenChest(idx) },
                                tag = "chest_take_$idx"
                            )
                        }
                    }
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth().height(48.dp).testTag("stash_close_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = LanternAmber, contentColor = Color(0xFF1A1004))
                    ) {
                        Text("Закрыть сундук")
                    }
                } else {
                    Text(
                        text = "Спрятанные под кроватью вещи ведьма не сможет отобрать. Нажмите на предмет в рюкзаке, чтобы спрятать.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MistSecondary
                    )

                    Text(
                        text = "В рюкзаке (${hud.backpackItems.size}/3):",
                        style = MaterialTheme.typography.titleSmall,
                        color = LanternAmber
                    )
                    if (hud.backpackItems.isEmpty()) {
                        Text("Рюкзак пуст", style = MaterialTheme.typography.bodySmall, color = MistSecondary)
                    } else {
                        hud.backpackItems.forEachIndexed { idx, item ->
                            StashItemRow(
                                item = item,
                                actionLabel = "Спрятать",
                                accent = LanternAmber,
                                onClick = { onStashUnderBed(idx) },
                                tag = "bed_stash_put_$idx"
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Под кроватью (${hud.stashedUnderBedItems.size}):",
                        style = MaterialTheme.typography.titleSmall,
                        color = EmeraldWitchlight
                    )
                    if (hud.stashedUnderBedItems.isEmpty()) {
                        Text("Тайник пока пуст", style = MaterialTheme.typography.bodySmall, color = MistSecondary)
                    } else {
                        hud.stashedUnderBedItems.forEachIndexed { idx, item ->
                            StashItemRow(
                                item = item,
                                actionLabel = if (hud.backpackItems.size < 3) "В рюкзак" else "Нет места",
                                enabled = hud.backpackItems.size < 3,
                                accent = EmeraldWitchlight,
                                onClick = { onTakeFromBedStash(idx) },
                                tag = "bed_stash_take_$idx"
                            )
                        }
                    }

                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth().height(48.dp).testTag("stash_close_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = LanternAmber, contentColor = Color(0xFF1A1004))
                    ) {
                        Text("Закрыть тайник")
                    }
                }
            }
        }
    }
}

@Composable
private fun StashItemRow(
    item: InventoryItem,
    actionLabel: String,
    accent: Color,
    enabled: Boolean = true,
    onClick: () -> Unit,
    tag: String
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = ManorSurfaceElevated,
        border = BorderStroke(1.dp, accent.copy(alpha = 0.45f))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = item.symbol, fontSize = 20.sp)
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(item.nameRu, style = MaterialTheme.typography.titleSmall, color = ParchmentText)
                Text(item.descriptionRu, style = MaterialTheme.typography.bodySmall, color = MistSecondary)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = onClick,
                enabled = enabled,
                modifier = Modifier.height(42.dp).testTag(tag),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = accent,
                    contentColor = Color(0xFF0A110E)
                )
            ) {
                Text(actionLabel, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
            }
        }
    }
}
