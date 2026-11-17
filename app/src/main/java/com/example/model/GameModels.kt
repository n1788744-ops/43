package com.example.model

enum class ChapterId(val number: Int) {
    CHAPTER_1_DUNGEON(1),
    CHAPTER_2_KITCHEN(2),
    CHAPTER_3_BEDROOM(3),
    CHAPTER_4_CHAPEL(4),
    CHAPTER_5_TOWER(5);

    companion object {
        fun fromNumber(num: Int): ChapterId = entries.firstOrNull { it.number == num } ?: CHAPTER_1_DUNGEON
    }
}

enum class WeaponType(
    val titleRu: String,
    val descriptionRu: String,
    val rangeMeters: Float,
    val stunSeconds: Float,
    val cooldownSeconds: Float,
    val isRanged: Boolean
) {
    NONE("Без оружия", "Найдите оружие в комнатах дома", 0f, 0f, 0f, false),
    IRON_POKER("Кованая кочерга", "Ближний бой: оглушает ведьму на 2.5 сек", 2.75f, 2.5f, 0.9f, false),
    SILVER_STAKE("Серебряный клинок", "Сильный удар: отбрасывает и оглушает на 3.6 сек", 2.95f, 3.6f, 1.0f, false),
    CROSSBOW("Арбалет ведьмобоя", "Дальний бой: серебряный болт оглушает на 5 сек", 18.0f, 5.0f, 1.35f, true)
}

enum class ItemType {
    KEY,
    RELIC,
    SALT_WARD,
    LANTERN_OIL,
    LORE_NOTE,
    WEAPON,
    AMMO_BOLT,
    MIRROR_CURSED,
    BLACK_CANDLE,
    ELECTRICAL_TAPE
}

enum class PuzzleType {
    DUNGEON_SHACKLES,  // Level 1: Dungeon padlock runes
    KITCHEN_BUFFET,    // Level 2: Haunted dinner course order
    BEDROOM_MUSICBOX,  // Level 3: Witch's music box melody
    CHAPEL_ORGAN,      // Level 4: Swamp chapel organ pipes
    TOWER_CROWN        // Level 5: Raven Crown 5-node ritual
}

data class InventoryItem(
    val id: String,
    val nameRu: String,
    val descriptionRu: String,
    val type: ItemType,
    val symbol: String,
    val weaponType: WeaponType = WeaponType.NONE
)

data class WorldItemSpawn(
    val markerChar: Char,
    val item: InventoryItem,
    val voiceLineRu: String
)

data class WorldNoteSpawn(
    val markerChar: Char,
    val titleRu: String,
    val bodyRu: String,
    val voiceLineRu: String
)

data class Vec3Colors(
    val r: Float,
    val g: Float,
    val b: Float
)

data class ChapterConfig(
    val id: ChapterId,
    val titleRu: String,
    val subtitleRu: String,
    val locationRu: String,
    val roomsListRu: String,
    val difficultyStars: Int,
    val summaryRu: String,
    val introStoryLines: List<String>,
    val introVoiceRu: String,
    val puzzleType: PuzzleType,
    val puzzleTitleRu: String,
    val puzzleClueRu: String,
    val requiredRelicCount: Int,
    val witchSpeed: Float,
    val witchChaseSpeed: Float,
    val witchVisionRange: Float,
    val witchHearingRange: Float,
    val witchCanTeleport: Boolean,
    val witchChecksClosets: Boolean,
    val fogColor: Vec3Colors,
    val ambientLight: Vec3Colors,
    val torchColor: Vec3Colors,
    val witchAuraColor: Vec3Colors,
    val mapGrid: List<String>,
    val itemSpawns: List<WorldItemSpawn>,
    val noteSpawns: List<WorldNoteSpawn>,
    val twistTitleRu: String,
    val twistSubtitleRu: String,
    val twistStoryRu: String,
    val twistVoiceRu: String
)

enum class NearbyTargetType {
    NONE,
    ITEM,
    NOTE,
    PUZZLE,
    CLOSET,
    DOOR,
    BED_STASH,
    KITCHEN_CHEST,
    ROTARY_PHONE,
    BREAKER_SWITCH,
    EXIT_DOOR
}

data class NearbyTarget(
    val type: NearbyTargetType = NearbyTargetType.NONE,
    val id: String = "",
    val labelRu: String = "",
    val canInteract: Boolean = true
)

data class DialogueLine(
    val speaker: String,
    val textRu: String,
    val timestampMs: Long = System.currentTimeMillis()
)

enum class GameOutcome {
    PLAYING,
    PAUSED,
    CAGED_ESCAPE,
    CAUGHT_BY_WITCH,
    BAD_ENDING_DINNER,
    CHAPTER_ESCAPED,
    POLICE_RESCUED
}

enum class StashContainerType {
    BED_STASH,
    KITCHEN_CHEST
}

data class StashDialogState(
    val containerType: StashContainerType,
    val pendingPickupItem: InventoryItem? = null
)

data class GameHudState(
    val chapterId: ChapterId = ChapterId.CHAPTER_1_DUNGEON,
    val chapterTitleRu: String = "",
    val stamina: Float = 1f,
    val lanternBattery: Float = 1f,
    val sanity: Float = 1f,
    val noiseLevel: Float = 0f,
    val witchProximity: Float = 0f,
    val witchAlerted: Boolean = false,
    val witchRaged: Boolean = false,
    val witchStunnedSeconds: Float = 0f,
    val captureAttempts: Int = 0,
    val cageCountdownSec: Float = 0f,
    val cageHardLock: Boolean = false,
    val isHiding: Boolean = false,
    val isFlashlightOn: Boolean = true,
    val isInPoisonMist: Boolean = false,
    val saltWardsCount: Int = 1,
    val equippedWeapon: WeaponType = WeaponType.NONE,
    val unlockedWeapons: List<WeaponType> = emptyList(),
    val crossbowAmmo: Int = 0,
    val weaponCooldownFraction: Float = 0f,
    val backpackItems: List<InventoryItem> = emptyList(),
    val stashedUnderBedItems: List<InventoryItem> = emptyList(),
    val kitchenChestItems: List<InventoryItem> = emptyList(),
    val mirrorWitchRoomRu: String = "",
    val hasBlackCandle: Boolean = false,
    val hasHeavyKey: Boolean = false,
    val evidencesCollected: Set<Int> = emptySet(),
    val cameraFlashAlpha: Float = 0f,
    val phoneWiredFixed: Boolean = false,
    val breakerPowerOn: Boolean = false,
    val huntModeActive: Boolean = false,
    val huntRemainingSec: Int = 180,
    val collectedRelicsCount: Int = 0,
    val requiredRelicsCount: Int = 1,
    val puzzleSolved: Boolean = false,
    val hasExitKey: Boolean = false,
    val objectiveTextRu: String = "",
    val elapsedSeconds: Int = 0,
    val nearbyTarget: NearbyTarget = NearbyTarget(),
    val activePuzzle: PuzzleType? = null,
    val activeNote: WorldNoteSpawn? = null,
    val activeDialogue: DialogueLine? = null,
    val outcome: GameOutcome = GameOutcome.PLAYING,
    val statusBannerRu: String? = null
)

data class PlayerProgress(
    val highestUnlockedChapter: Int = 1,
    val completedChapters: Set<Int> = emptySet(),
    val bestTimesSec: Map<Int, Int> = emptyMap(),
    val soundVolume: Float = 0.85f,
    val voiceEnabled: Boolean = true,
    val lookSensitivity: Float = 1.0f,
    val freeChapterAccess: Boolean = false,
    val tutorialSeen: Boolean = false
)
