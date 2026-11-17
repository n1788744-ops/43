package com.example.engine3d

import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import com.example.audio.HorrorSoundEngine
import com.example.audio.NastyaVoiceActor
import com.example.model.ChapterConfig
import com.example.model.GameHudState
import com.example.model.GameOutcome
import com.example.model.InventoryItem
import com.example.model.ItemType
import com.example.model.NearbyTarget
import com.example.model.NearbyTargetType
import com.example.model.PuzzleType
import com.example.model.StashContainerType
import com.example.model.StashDialogState
import com.example.model.WeaponType
import com.example.model.WorldItemSpawn
import com.example.model.WorldNoteSpawn
import java.util.ArrayDeque
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin
import kotlin.random.Random

class FirstPersonGameEngine(
    private val chapter: ChapterConfig,
    private val soundEngine: HorrorSoundEngine,
    private val voiceActor: NastyaVoiceActor,
    private val onHudUpdate: (GameHudState) -> Unit,
    private val onOpenPuzzle: (PuzzleType) -> Unit,
    private val onOpenNote: (WorldNoteSpawn) -> Unit,
    private val onOpenStash: (StashDialogState) -> Unit
) : GLSurfaceView.Renderer {

    companion object {
        const val CELL_SIZE = 3.0f
        const val WALL_HEIGHT = 3.6f
        const val PLAYER_EYE_HEIGHT = 1.62f
        const val PLAYER_RADIUS = 0.50f
        const val MAX_BACKPACK_SLOTS = 3
    }

    private lateinit var shaders: GameShaders
    private lateinit var textures: ProceduralTextures
    private lateinit var witchModel: WitchModel3D
    private lateinit var weaponRenderer: WeaponRenderer3D

    // Shared 3D meshes
    private lateinit var wallBox: Mesh
    private lateinit var floorMesh: Mesh
    private lateinit var ceilingMesh: Mesh
    private lateinit var exitDoorMesh: Mesh
    private lateinit var interiorDoorLeafMesh: Mesh
    private lateinit var doorLintelMesh: Mesh
    private lateinit var doorPostMesh: Mesh
    private lateinit var ironBarMesh: Mesh
    private lateinit var ironCrossbarMesh: Mesh
    private lateinit var tableTopMesh: Mesh
    private lateinit var tableLegMesh: Mesh
    private lateinit var benchTopMesh: Mesh
    private lateinit var plateDiscMesh: Mesh
    private lateinit var candleStickMesh: Mesh
    private lateinit var bedMattressMesh: Mesh
    private lateinit var bedPillowMesh: Mesh
    private lateinit var paintingMesh: Mesh
    private lateinit var floorPaperMesh: Mesh
    private lateinit var rugMesh: Mesh
    private lateinit var closetMesh: Mesh
    private lateinit var altarMesh: Mesh
    private lateinit var pillarMesh: Mesh
    private lateinit var cauldronMesh: Mesh
    private lateinit var torchPostMesh: Mesh
    private lateinit var flameMesh: Mesh
    private lateinit var itemCrystalMesh: Mesh
    private lateinit var itemSphereMesh: Mesh
    private lateinit var noteMesh: Mesh
    private lateinit var mirrorFrameMesh: Mesh
    private lateinit var mirrorGlassMesh: Mesh
    private lateinit var mistDiscMesh: Mesh
    private lateinit var boltProjectileMesh: Mesh

    private val projMatrix = FloatArray(16)
    private val viewMatrix = FloatArray(16)
    private val viewProjMatrix = FloatArray(16)
    private val identityMatrix = FloatArray(16).apply { Matrix.setIdentityM(this, 0) }
    private val modelMatrix = FloatArray(16)
    private val mvpMatrix = FloatArray(16)

    private val rows = chapter.mapGrid.size
    private val cols = chapter.mapGrid.first().length

    private data class PointLightProp(val x: Float, val y: Float, val z: Float)
    private data class WorldItemEntity(val x: Float, val z: Float, val spawn: WorldItemSpawn, var collected: Boolean = false)
    private data class WorldNoteEntity(val x: Float, val z: Float, val spawn: WorldNoteSpawn, var readOnce: Boolean = false)
    private data class ClosetEntity(val x: Float, val z: Float)
    private data class WallPainting(val x: Float, val z: Float, val ryDeg: Float)
    private data class FloorPaper(val x: Float, val z: Float, val ryDeg: Float)
    private data class DoorEntity(
        val row: Int,
        val col: Int,
        val x: Float,
        val z: Float,
        val runsAlongX: Boolean, // true if doorway connects North-South rooms
        var isOpen: Boolean = false,
        var openAngleDeg: Float = 0f
    )
    private data class ActiveBolt(
        var x: Float,
        var y: Float,
        var z: Float,
        val vx: Float,
        val vy: Float,
        val vz: Float,
        var lifeSec: Float = 2.0f
    )

    private val wallCells = Array(rows) { BooleanArray(cols) }
    private val doorMap = mutableMapOf<Pair<Int, Int>, DoorEntity>()
    private val doors = mutableListOf<DoorEntity>()
    private val ironBarCells = mutableListOf<Pair<Float, Float>>()
    private val furnitureCells = mutableListOf<Pair<Float, Float>>()
    private val wallPaintings = mutableListOf<WallPainting>()
    private val floorPapers = mutableListOf<FloorPaper>()
    private val floorRugs = mutableListOf<Pair<Float, Float>>()
    private val cauldronCells = mutableListOf<Pair<Float, Float>>()
    private val bedStashSpots = mutableListOf<Pair<Float, Float>>()
    private val mistCells = mutableListOf<Pair<Float, Float>>()
    private val mirrorEntities = mutableListOf<Pair<Float, Float>>()
    private val torchLights = mutableListOf<PointLightProp>()
    private val itemEntities = mutableListOf<WorldItemEntity>()
    private val noteEntities = mutableListOf<WorldNoteEntity>()
    private val closets = mutableListOf<ClosetEntity>()
    private val walkableCells = mutableListOf<Pair<Int, Int>>()
    private val activeBolts = mutableListOf<ActiveBolt>()

    private var exitDoorX = 0f
    private var exitDoorZ = 0f
    private var puzzleAltarX = 0f
    private var puzzleAltarZ = 0f
    private var kitchenChestX = -999f
    private var kitchenChestZ = -999f
    private var evidenceTeensX = -999f
    private var evidenceTeensZ = -999f
    private var rotaryPhoneX = -999f
    private var rotaryPhoneZ = -999f
    private var breakerSwitchX = -999f
    private var breakerSwitchZ = -999f

    // Player state
    private var playerX = 4.5f
    private var playerZ = 4.5f
    private var playerYawDeg = 90f // Start looking toward dungeon bars in Level 1
    private var playerPitchDeg = 0f
    private var headBobPhase = 0f
    private var footstepAccum = 0f

    private var stamina = 1f
    private var lanternBattery = 1f
    private var sanity = 1f
    private var isFlashlightOn = true
    private var isHiding = false
    private var hidingSpotX = 0f
    private var hidingSpotZ = 0f
    private var preHideX = 0f
    private var preHideZ = 0f
    private var sawPlayerHide = false

    // Weapons state
    private val unlockedWeapons = mutableListOf<WeaponType>()
    private var equippedWeapon = WeaponType.NONE
    private var crossbowAmmo = if (chapter.id.number >= 4) 3 else 0
    private var attackCooldown = 0f
    private var attackAnimTimer = 0f

    // Witch state
    private var witchX = 12f
    private var witchZ = 12f
    private var witchYawDeg = 0f
    private var witchWalkPhase = 0f
    private var witchAlerted = false
    private var witchInvestigateTargetX = -1f
    private var witchInvestigateTargetZ = -1f
    private var witchPatrolTarget: Pair<Int, Int>? = null
    private var witchStunnedTimer = 0f
    private var witchTeleportCooldown = 14f
    private var witchWarnCooldown = 0f

    // Progression & outcome
    private val backpackItems = mutableListOf<InventoryItem>() // MAX 3 SLOTS
    private val stashedUnderBedItems = mutableListOf<InventoryItem>()
    private val kitchenChestItems = mutableListOf<InventoryItem>()
    private var pendingPickupEntity: WorldItemEntity? = null

    private var captureAttempts = 0
    private var cageCountdownSec = 0f
    private var cageHardLock = false
    private val evidencesCollected = mutableSetOf<Int>()
    private var cameraFlashAlpha = 0f
    private var phoneWiredFixed = false
    private var breakerPowerOn = false
    private var huntModeActive = false
    private var huntRemainingFloat = 180f
    private var witchRaged = false
    private var mirrorJingleCooldown = 0f

    private var saltWardsCount = 1
    private var puzzleSolved = false
    private var hasExitKey = false
    private var outcome = GameOutcome.PLAYING
    private var elapsedFloat = 0f
    private var totalTimeSec = 0f
    private var lastFrameNs = 0L
    private var lastHudSyncMs = 0L
    private var statusBanner: String? = null
    private var statusBannerTimer = 0f

    // Thread-safe touch inputs
    private val inputLock = Any()
    private var inputMoveX = 0f
    private var inputMoveY = 0f
    private var inputSprint = false
    private var pendingLookYaw = 0f
    private var pendingLookPitch = 0f
    private var pendingInteract = false
    private var pendingUseSalt = false
    private var pendingToggleFlashlight = false
    private var pendingAttack = false
    private var pendingCycleWeapon = false
    private var pendingTakePhoto = false
    private var pausedByUi = false

    private val rng = Random(System.currentTimeMillis())

    init {
        // Carry over weapons on later chapters so Nastya keeps feeling stronger!
        if (chapter.id.number >= 2) unlockedWeapons.add(WeaponType.IRON_POKER)
        if (chapter.id.number >= 3) unlockedWeapons.add(WeaponType.SILVER_STAKE)
        if (chapter.id.number >= 4) unlockedWeapons.add(WeaponType.CROSSBOW)
        equippedWeapon = unlockedWeapons.lastOrNull() ?: WeaponType.NONE
        parseMap()
    }

    private fun cellCenter(col: Int, row: Int): Pair<Float, Float> {
        return (col + 0.5f) * CELL_SIZE to (row + 0.5f) * CELL_SIZE
    }

    private fun parseMap() {
        val itemMap = chapter.itemSpawns.associateBy { it.markerChar }
        val noteMap = chapter.noteSpawns.associateBy { it.markerChar }

        for (r in 0 until rows) {
            val rowStr = chapter.mapGrid[r]
            for (c in 0 until cols) {
                val ch = rowStr[c]
                val (wx, wz) = cellCenter(c, r)
                when (ch) {
                    '#' -> wallCells[r][c] = true
                    'I' -> {
                        wallCells[r][c] = true
                        ironBarCells.add(wx to wz)
                    }
                    'B' -> {
                        wallCells[r][c] = true
                        furnitureCells.add(wx to wz)
                    }
                    'O' -> {
                        wallCells[r][c] = true
                        cauldronCells.add(wx to wz)
                    }
                    else -> {
                        walkableCells.add(r to c)
                        when (ch) {
                            'S' -> {
                                playerX = wx
                                playerZ = wz
                            }
                            'W' -> {
                                witchX = wx
                                witchZ = wz
                            }
                            'E' -> {
                                exitDoorX = wx
                                exitDoorZ = wz
                            }
                            'P' -> {
                                puzzleAltarX = wx
                                puzzleAltarZ = wz
                            }
                            'K' -> {
                                kitchenChestX = wx
                                kitchenChestZ = wz
                            }
                            'V' -> {
                                evidenceTeensX = wx
                                evidenceTeensZ = wz
                            }
                            'F' -> {
                                rotaryPhoneX = wx
                                rotaryPhoneZ = wz
                            }
                            'X' -> {
                                breakerSwitchX = wx
                                breakerSwitchZ = wz
                            }
                            'U' -> {
                                bedStashSpots.add(wx to wz)
                            }
                            'D' -> {
                                val wallLeft = c > 0 && chapter.mapGrid[r][c - 1] in listOf('#', 'I')
                                val wallRight = c < cols - 1 && chapter.mapGrid[r][c + 1] in listOf('#', 'I')
                                val door = DoorEntity(r, c, wx, wz, runsAlongX = (wallLeft || wallRight))
                                doors.add(door)
                                doorMap[r to c] = door
                            }
                            'H' -> closets.add(ClosetEntity(wx, wz))
                            'T' -> torchLights.add(PointLightProp(wx, 2.35f, wz))
                            'M' -> mistCells.add(wx to wz)
                            'R' -> mirrorEntities.add(wx to wz)
                            else -> {
                                itemMap[ch]?.let { spawn -> itemEntities.add(WorldItemEntity(wx, wz, spawn)) }
                                noteMap[ch]?.let { nSpawn -> noteEntities.add(WorldNoteEntity(wx, wz, nSpawn)) }
                                if (ch == '.') {
                                    val hash = r * 31 + c * 17
                                    if (hash % 11 == 0) {
                                        floorPapers.add(FloorPaper(wx + ((hash % 5) - 2) * 0.22f, wz + ((hash % 3) - 1) * 0.24f, (hash * 37 % 360).toFloat()))
                                    } else if (hash % 19 == 0 && chapter.id.number >= 2) {
                                        floorRugs.add(wx to wz)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Decorate select solid walls facing open rooms with framed paintings
        for (r in 1 until rows - 1) {
            for (c in 1 until cols - 1) {
                if (chapter.mapGrid[r][c] != '#') continue
                if ((r * 7 + c * 13) % 9 != 0) continue
                val (wx, wz) = cellCenter(c, r)
                val halfWall = CELL_SIZE / 2f + 0.04f
                when {
                    chapter.mapGrid[r + 1][c] == '.' -> wallPaintings.add(WallPainting(wx, wz + halfWall, 0f))
                    chapter.mapGrid[r - 1][c] == '.' -> wallPaintings.add(WallPainting(wx, wz - halfWall, 180f))
                    chapter.mapGrid[r][c + 1] == '.' -> wallPaintings.add(WallPainting(wx + halfWall, wz, 90f))
                    chapter.mapGrid[r][c - 1] == '.' -> wallPaintings.add(WallPainting(wx - halfWall, wz, 270f))
                }
            }
        }
    }

    fun setMoveInput(x: Float, y: Float, sprint: Boolean) {
        synchronized(inputLock) {
            inputMoveX = x.coerceIn(-1f, 1f)
            inputMoveY = y.coerceIn(-1f, 1f)
            inputSprint = sprint
        }
    }

    fun addLookDelta(dxDeg: Float, dyDeg: Float) {
        synchronized(inputLock) {
            pendingLookYaw += dxDeg
            pendingLookPitch += dyDeg
        }
    }

    fun triggerInteract() {
        synchronized(inputLock) { pendingInteract = true }
    }

    fun triggerUseSalt() {
        synchronized(inputLock) { pendingUseSalt = true }
    }

    fun triggerAttack() {
        synchronized(inputLock) { pendingAttack = true }
    }

    fun triggerCycleWeapon() {
        synchronized(inputLock) { pendingCycleWeapon = true }
    }

    fun triggerPolaroidPhoto() {
        synchronized(inputLock) { pendingTakePhoto = true }
    }

    fun toggleFlashlight() {
        synchronized(inputLock) { pendingToggleFlashlight = true }
    }

    fun setPaused(paused: Boolean) {
        synchronized(inputLock) {
            pausedByUi = paused
            if (paused && outcome == GameOutcome.PLAYING) outcome = GameOutcome.PAUSED
            else if (!paused && outcome == GameOutcome.PAUSED) outcome = GameOutcome.PLAYING
        }
        lastFrameNs = System.nanoTime()
        pushHudSnapshot()
    }

    // Kitchen Cage Lockpick Solved!
    fun onCageEscaped() {
        synchronized(inputLock) {
            outcome = GameOutcome.PLAYING
            pausedByUi = false
            witchStunnedTimer = 6.5f
            witchAlerted = false
            showBanner("Клетка открыта! Бегите!")
        }
        soundEngine.playUnlock()
        voiceActor.speakNastya("Успела! Замок открыт — скорее бежим от печи!", forceInterrupt = true)
        pushHudSnapshot()
    }

    // 3-Slot Backpack, Under-Bed Stash & Kitchen Chest Actions
    fun stashBackpackItemUnderBed(index: Int) {
        synchronized(inputLock) {
            if (index !in backpackItems.indices) return
            val item = backpackItems.removeAt(index)
            stashedUnderBedItems.add(item)
            hasExitKey = backpackItems.any { it.type == ItemType.KEY }
            showBanner("Спрятано под кровать: ${item.nameRu}")
        }
        soundEngine.playPickup()
        pushHudSnapshot()
    }

    fun takeBedStashItemToBackpack(index: Int) {
        synchronized(inputLock) {
            if (index !in stashedUnderBedItems.indices) return
            if (backpackItems.size >= MAX_BACKPACK_SLOTS) {
                showBanner("Рюкзак полон (3/3)! Сначала освободите слот.")
                return
            }
            val item = stashedUnderBedItems.removeAt(index)
            backpackItems.add(item)
            hasExitKey = backpackItems.any { it.type == ItemType.KEY }
            showBanner("Взято в рюкзак: ${item.nameRu}")
        }
        soundEngine.playPickup()
        pushHudSnapshot()
    }

    fun takeKitchenChestItemToBackpack(index: Int) {
        synchronized(inputLock) {
            if (index !in kitchenChestItems.indices) return
            if (backpackItems.size >= MAX_BACKPACK_SLOTS) {
                showBanner("Рюкзак полон (3/3)! Освободите место.")
                return
            }
            val item = kitchenChestItems.removeAt(index)
            backpackItems.add(item)
            hasExitKey = backpackItems.any { it.type == ItemType.KEY }
            showBanner("Возвращено: ${item.nameRu}")
        }
        soundEngine.playPickup()
        pushHudSnapshot()
    }

    fun swapPendingPickupWithSlot(dropIndex: Int) {
        synchronized(inputLock) {
            val pending = pendingPickupEntity ?: return
            if (dropIndex !in backpackItems.indices) return
            val dropped = backpackItems.removeAt(dropIndex)
            // Place dropped item back on map right at Nastya's feet so nothing is ever lost!
            itemEntities.add(
                WorldItemEntity(
                    x = playerX,
                    z = playerZ,
                    spawn = WorldItemSpawn(
                        markerChar = '?',
                        item = dropped,
                        voiceLineRu = "Подобрала ${dropped.nameRu} обратно."
                    ),
                    collected = false
                )
            )
            pending.collected = true
            backpackItems.add(pending.spawn.item)
            pendingPickupEntity = null
            hasExitKey = backpackItems.any { it.type == ItemType.KEY }
            pausedByUi = false
            outcome = GameOutcome.PLAYING
            showBanner("Взято: ${pending.spawn.item.nameRu}")
            voiceActor.speakNastya(pending.spawn.voiceLineRu, forceInterrupt = true)
        }
        soundEngine.playPickup()
        pushHudSnapshot()
    }

    fun dismissStashModal() {
        synchronized(inputLock) {
            pendingPickupEntity = null
            pausedByUi = false
            if (outcome == GameOutcome.PAUSED) outcome = GameOutcome.PLAYING
        }
        pushHudSnapshot()
    }

    fun onPuzzleSolved() {
        synchronized(inputLock) {
            puzzleSolved = true
            pausedByUi = false
            outcome = GameOutcome.PLAYING
            showBanner("Магическая печать снята! Ищите выход!")
        }
        soundEngine.playPuzzleResult(true)
        voiceActor.speakNastya("Получилось! Печать с двери снята! Бежим к выходу!", forceInterrupt = true)
        pushHudSnapshot()
    }

    fun onPuzzleFailedAttempt() {
        synchronized(inputLock) {
            witchInvestigateTargetX = playerX
            witchInvestigateTargetZ = playerZ
            showBanner("Шум загадки привлёк ведьму!")
        }
        soundEngine.playPuzzleResult(false)
        soundEngine.playWitchAlert()
        voiceActor.speakNastya("Ой нет, она услышала! Держи оружие наготове!", forceInterrupt = true)
    }

    fun resumeFromNote() {
        synchronized(inputLock) {
            pausedByUi = false
            outcome = GameOutcome.PLAYING
        }
    }

    private fun showBanner(text: String) {
        statusBanner = text
        statusBannerTimer = 3.2f
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES20.glClearColor(chapter.fogColor.r, chapter.fogColor.g, chapter.fogColor.b, 1f)
        GLES20.glEnable(GLES20.GL_DEPTH_TEST)
        GLES20.glEnable(GLES20.GL_CULL_FACE)

        shaders = GameShaders()
        textures = ProceduralTextures()
        witchModel = WitchModel3D(textures)
        weaponRenderer = WeaponRenderer3D(textures)

        wallBox = MeshBuilder.createBox(CELL_SIZE, WALL_HEIGHT, CELL_SIZE, tileU = 1f, tileV = 1.2f)
        val totalW = cols * CELL_SIZE
        val totalD = rows * CELL_SIZE
        floorMesh = MeshBuilder.createPlaneXZ(totalW, totalD, tileU = cols.toFloat(), tileV = rows.toFloat(), facingUp = true)
        ceilingMesh = MeshBuilder.createPlaneXZ(totalW, totalD, tileU = cols.toFloat(), tileV = rows.toFloat(), facingUp = false)

        exitDoorMesh = MeshBuilder.createBox(1.85f, 2.95f, 0.28f)
        interiorDoorLeafMesh = MeshBuilder.createBox(CELL_SIZE - 0.16f, 2.75f, 0.16f)
        doorLintelMesh = MeshBuilder.createBox(CELL_SIZE, 0.85f, 0.34f)
        doorPostMesh = MeshBuilder.createBox(0.22f, WALL_HEIGHT, 0.36f)
        ironBarMesh = MeshBuilder.createCylinder(0.055f, 0.055f, WALL_HEIGHT, 8)
        ironCrossbarMesh = MeshBuilder.createBox(CELL_SIZE, 0.12f, 0.14f)
        tableTopMesh = MeshBuilder.createBox(2.05f, 0.12f, 1.15f)
        tableLegMesh = MeshBuilder.createBox(0.12f, 0.84f, 0.12f)
        benchTopMesh = MeshBuilder.createBox(1.85f, 0.09f, 0.42f)
        plateDiscMesh = MeshBuilder.createCylinder(0.22f, 0.18f, 0.04f, 12)
        candleStickMesh = MeshBuilder.createCylinder(0.045f, 0.055f, 0.24f, 8)
        bedMattressMesh = MeshBuilder.createBox(1.65f, 0.34f, 2.25f)
        bedPillowMesh = MeshBuilder.createBox(0.95f, 0.14f, 0.48f)
        paintingMesh = MeshBuilder.createBox(1.15f, 0.92f, 0.07f)
        floorPaperMesh = MeshBuilder.createBox(0.34f, 0.015f, 0.44f)
        rugMesh = MeshBuilder.createBox(2.35f, 0.02f, 1.75f)

        closetMesh = MeshBuilder.createBox(1.25f, 2.45f, 0.85f)
        altarMesh = MeshBuilder.createCylinder(0.65f, 0.48f, 1.15f, 12)
        pillarMesh = MeshBuilder.createCylinder(0.45f, 0.42f, WALL_HEIGHT, 12)
        cauldronMesh = MeshBuilder.createCylinder(0.48f, 0.62f, 0.92f, 14)
        torchPostMesh = MeshBuilder.createCylinder(0.06f, 0.08f, 1.9f, 8)
        flameMesh = MeshBuilder.createOctahedron(0.16f)
        itemCrystalMesh = MeshBuilder.createOctahedron(0.24f)
        itemSphereMesh = MeshBuilder.createSphere(0.19f, 8, 12)
        noteMesh = MeshBuilder.createBox(0.46f, 0.05f, 0.34f)
        mirrorFrameMesh = MeshBuilder.createBox(1.1f, 2.35f, 0.14f)
        mirrorGlassMesh = MeshBuilder.createBox(0.88f, 2.05f, 0.17f)
        mistDiscMesh = MeshBuilder.createCylinder(1.25f, 1.45f, 0.16f, 16)
        boltProjectileMesh = MeshBuilder.createOctahedron(0.11f)

        lastFrameNs = System.nanoTime()
        pushHudSnapshot()
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
        val aspect = width.toFloat() / height.coerceAtLeast(1)
        Matrix.perspectiveM(projMatrix, 0, 64f, aspect, 0.10f, 60f)
    }

    override fun onDrawFrame(gl: GL10?) {
        val nowNs = System.nanoTime()
        val dt = ((nowNs - lastFrameNs) / 1_000_000_000f).coerceIn(0.001f, 0.05f)
        lastFrameNs = nowNs

        updateSimulation(dt)
        renderScene()

        val nowMs = System.currentTimeMillis()
        if (nowMs - lastHudSyncMs > 75L) {
            lastHudSyncMs = nowMs
            pushHudSnapshot()
        }
    }

    private fun updateSimulation(dt: Float) {
        val moveX: Float
        val moveY: Float
        val wantSprint: Boolean
        val lookYaw: Float
        val lookPitch: Float
        val doInteract: Boolean
        val doSalt: Boolean
        val doToggleLight: Boolean
        val doAttack: Boolean
        val doCycleWeapon: Boolean
        val doPhoto: Boolean
        val isPaused: Boolean

        synchronized(inputLock) {
            moveX = inputMoveX
            moveY = inputMoveY
            wantSprint = inputSprint
            lookYaw = pendingLookYaw
            lookPitch = pendingLookPitch
            pendingLookYaw = 0f
            pendingLookPitch = 0f
            doInteract = pendingInteract
            pendingInteract = false
            doSalt = pendingUseSalt
            pendingUseSalt = false
            doToggleLight = pendingToggleFlashlight
            pendingToggleFlashlight = false
            doAttack = pendingAttack
            pendingAttack = false
            doCycleWeapon = pendingCycleWeapon
            pendingCycleWeapon = false
            doPhoto = pendingTakePhoto
            pendingTakePhoto = false
            isPaused = pausedByUi || outcome != GameOutcome.PLAYING
        }

        totalTimeSec += dt
        if (cameraFlashAlpha > 0f) {
            cameraFlashAlpha = (cameraFlashAlpha - dt * 2.8f).coerceAtLeast(0f)
        }
        if (statusBannerTimer > 0f) {
            statusBannerTimer -= dt
            if (statusBannerTimer <= 0f) statusBanner = null
        }

        // Handle 30-second Kitchen Cage Escape Timer live!
        if (outcome == GameOutcome.CAGED_ESCAPE) {
            cageCountdownSec = (cageCountdownSec - dt).coerceAtLeast(0f)
            soundEngine.setGameplayContext(true, 0.72f)
            if (cageCountdownSec <= 0f) {
                triggerBadEndingDinner()
            }
            return
        }

        if (isPaused) {
            soundEngine.setGameplayContext(true, 0f)
            return
        }

        elapsedFloat += dt

        // Handle 3:00 (180s) Police Hunt Mode countdown & growing sirens!
        if (huntModeActive) {
            huntRemainingFloat = (huntRemainingFloat - dt).coerceAtLeast(0f)
            val huntProg = (1f - (huntRemainingFloat / 180f)).coerceIn(0f, 1f)
            soundEngine.setHuntState(true, huntProg)
            if (huntRemainingFloat <= 0f) {
                outcome = GameOutcome.POLICE_RESCUED
                soundEngine.setHuntState(false, 1f)
                soundEngine.playUnlock()
                voiceActor.speakNastya("Сирены у самых дверей! Полиция прорвалась в дом! Мы спасены!", forceInterrupt = true)
                pushHudSnapshot()
                return
            }
        }

        // Animate hinged doors smoothly
        for (door in doors) {
            val targetAngle = if (door.isOpen) 92f else 0f
            val diff = targetAngle - door.openAngleDeg
            if (kotlin.math.abs(diff) > 0.5f) {
                door.openAngleDeg += diff * (dt * 7.5f).coerceAtMost(1f)
            } else {
                door.openAngleDeg = targetAngle
            }
        }

        // Weapon timers
        if (attackCooldown > 0f) attackCooldown = (attackCooldown - dt).coerceAtLeast(0f)
        if (attackAnimTimer > 0f) attackAnimTimer = (attackAnimTimer - dt).coerceAtLeast(0f)
        if (mirrorJingleCooldown > 0f) mirrorJingleCooldown = (mirrorJingleCooldown - dt).coerceAtLeast(0f)

        // Look rotation
        playerYawDeg = (playerYawDeg + lookYaw) % 360f
        playerPitchDeg = (playerPitchDeg + lookPitch).coerceIn(-62f, 62f)

        // Cycle weapon
        if (doCycleWeapon && unlockedWeapons.size > 1) {
            val idx = unlockedWeapons.indexOf(equippedWeapon)
            equippedWeapon = unlockedWeapons[(idx + 1) % unlockedWeapons.size]
            showBanner("Оружие: ${equippedWeapon.titleRu}")
        }

        // Weapon Attack!
        if (doAttack && !isHiding) {
            performWeaponAttack()
        }

        // Polaroid Camera Flash & Evidence Snapshot!
        if (doPhoto && !isHiding) {
            performPolaroidPhoto()
        }

        // Update active crossbow projectiles
        updateProjectiles(dt)

        // Flashlight toggle & drain
        if (doToggleLight) {
            if (lanternBattery > 0.02f || isFlashlightOn) {
                isFlashlightOn = !isFlashlightOn
                soundEngine.playFlashlightClick()
            } else {
                showBanner("Фонарь разряжен — найдите масло!")
            }
        }
        if (isFlashlightOn && !isHiding) {
            lanternBattery = (lanternBattery - dt * 0.0055f).coerceAtLeast(0f)
            if (lanternBattery <= 0f) {
                isFlashlightOn = false
                showBanner("Фонарь погас! Ищите масло в комнатах!")
                voiceActor.speakNastya("Фонарь сел! Как же темно...")
            }
        }

        // Salt Ward use
        if (doSalt) {
            if (saltWardsCount > 0) {
                saltWardsCount--
                val distToWitch = hypot(witchX - playerX, witchZ - playerZ)
                soundEngine.playSaltBanish()
                if (distToWitch < 8.5f) {
                    witchStunnedTimer = 4.4f
                    witchAlerted = false
                    showBanner("Соляной оберег ослепил ведьму на 4.4 сек!")
                    voiceActor.speakNastya("Получай! Бежим, пока она ослеплена!", forceInterrupt = true)
                } else {
                    showBanner("Оберег рассеялся — ведьма была далеко.")
                }
            } else {
                showBanner("У вас не осталось соляных оберегов!")
            }
        }

        val target = findNearbyTarget()
        if (doInteract) {
            handleInteraction(target)
        }

        val yawRad = Math.toRadians(playerYawDeg.toDouble())
        val forwardX = sin(yawRad).toFloat()
        val forwardZ = -cos(yawRad).toFloat()
        val rightX = cos(yawRad).toFloat()
        val rightZ = sin(yawRad).toFloat()

        var inMist = false
        for ((mx, mz) in mistCells) {
            if (hypot(playerX - mx, playerZ - mz) < 1.55f) {
                inMist = true
                break
            }
        }

        val inputMag = hypot(moveX, moveY).coerceAtMost(1f)
        val canSprint = wantSprint && stamina > 0.08f && !inMist && inputMag > 0.25f
        val isMoving = !isHiding && inputMag > 0.08f
        val hasHeavyKey = backpackItems.any { it.type == ItemType.KEY }
        val hasCursedMirror = backpackItems.any { it.type == ItemType.MIRROR_CURSED }

        if (isMoving) {
            var baseSpeed = if (canSprint) 5.2f else if (inMist) 2.3f else 3.35f
            if (hasHeavyKey) baseSpeed *= 0.80f // Heavy Key reduces speed by 20%!

            val moveDirX = (forwardX * moveY + rightX * moveX)
            val moveDirZ = (forwardZ * moveY + rightZ * moveX)
            val norm = hypot(moveDirX, moveDirZ).coerceAtLeast(1f)
            movePlayerWithCollision((moveDirX / norm) * inputMag * baseSpeed * dt, (moveDirZ / norm) * inputMag * baseSpeed * dt)

            headBobPhase += dt * (if (canSprint) 12.5f else 7.8f)
            footstepAccum += dt * (if (canSprint) 2.8f else 1.75f)
            if (footstepAccum >= 1f) {
                footstepAccum -= 1f
                soundEngine.playFootstep(canSprint)
            }
            if (hasCursedMirror && canSprint && mirrorJingleCooldown <= 0f) {
                mirrorJingleCooldown = 1.6f
                soundEngine.playMirrorJingle()
            }
        }

        if (canSprint) {
            stamina = (stamina - dt * 0.22f).coerceAtLeast(0f)
        } else {
            stamina = (stamina + dt * (if (inMist) 0.03f else 0.16f)).coerceAtMost(1f)
        }
        sanity = if (inMist && !isHiding) (sanity - dt * 0.045f).coerceAtLeast(0.1f) else (sanity + dt * 0.025f).coerceAtMost(1f)

        updateWitchAi(dt, isMoving && canSprint)

        val distToWitch = hypot(witchX - playerX, witchZ - playerZ)
        val catchDist = if (isHiding) 0.72f else 1.25f
        if (witchStunnedTimer <= 0f && distToWitch < catchDist && (!isHiding || sawPlayerHide)) {
            handlePlayerCaughtByWitch()
        }
    }

    private fun handlePlayerCaughtByWitch() {
        isHiding = false
        captureAttempts++
        when (captureAttempts) {
            1 -> {
                movePlayerToKitchenCage()
                outcome = GameOutcome.CAGED_ESCAPE
                cageCountdownSec = 30.0f
                cageHardLock = false
                soundEngine.playCageTrap()
                showBanner("Попытка 1/3: Взломай замок клетки за 30 сек!")
                voiceActor.speakNastya("Отпусти! Она кинула меня в клетку на кухне! Нужно срочно взломать замок, пока ведьма режет травы!", forceInterrupt = true)
            }
            2 -> {
                // Confiscate all items in the 3-slot backpack into the Kitchen Chest (under-bed items stay SAFE!)
                if (backpackItems.isNotEmpty()) {
                    kitchenChestItems.addAll(backpackItems)
                    backpackItems.clear()
                    hasExitKey = false
                }
                movePlayerToKitchenCage()
                outcome = GameOutcome.CAGED_ESCAPE
                cageCountdownSec = 30.0f
                cageHardLock = true
                soundEngine.playCageTrap()
                showBanner("Попытка 2/3: Вещи отобраны в сундук! Сложный замок!")
                voiceActor.speakNastya("Она разозлилась, забрала всё из рюкзака и бросила в сундук! Замок сложнее — быстрее!", forceInterrupt = true)
            }
            else -> {
                triggerBadEndingDinner()
            }
        }
        pushHudSnapshot()
    }

    private fun movePlayerToKitchenCage() {
        if (kitchenChestX > 0f) {
            playerX = kitchenChestX
            playerZ = kitchenChestZ
        }
        // Place Witch outside chopping herbs / patrolling a distant walkable tile
        val farCell = walkableCells.maxByOrNull { (r, c) ->
            val (cx, cz) = cellCenter(c, r)
            hypot(cx - playerX, cz - playerZ)
        }
        if (farCell != null) {
            val (wx, wz) = cellCenter(farCell.second, farCell.first)
            witchX = wx
            witchZ = wz
        }
        witchAlerted = false
    }

    private fun triggerBadEndingDinner() {
        captureAttempts = 3
        outcome = GameOutcome.BAD_ENDING_DINNER
        soundEngine.setHuntState(false, 0f)
        soundEngine.playJumpScare()
        voiceActor.speakNastya("Нет! Котёл закипел... «Вода закипела, пора ужинать!»", forceInterrupt = true)
        pushHudSnapshot()
    }

    private fun performPolaroidPhoto() {
        cameraFlashAlpha = 1.0f
        soundEngine.playCameraShutter()

        val yawRad = Math.toRadians(playerYawDeg.toDouble())
        val fwdX = sin(yawRad).toFloat()
        val fwdZ = -cos(yawRad).toFloat()

        fun isFacingPoint(tx: Float, tz: Float, maxDist: Float, minDot: Float = 0.25f): Boolean {
            if (tx < 0f) return false
            val dx = tx - playerX
            val dz = tz - playerZ
            val d = hypot(dx, dz)
            if (d > maxDist) return false
            if (d < 1.3f) return true
            val dot = (dx / d) * fwdX + (dz / d) * fwdZ
            return dot >= minDot && hasLineOfSight(playerX, playerZ, tx, tz)
        }

        // Check Evidence #3: The Witch herself with her cleaver/staff (flash alerts her!)
        if (!evidencesCollected.contains(3) && isFacingPoint(witchX, witchZ, 11.5f, 0.22f)) {
            evidencesCollected.add(3)
            witchAlerted = true
            witchInvestigateTargetX = playerX
            witchInvestigateTargetZ = playerZ
            soundEngine.playWitchAlert()
            showBanner("Улика 3/3: Фото ведьмы! Вспышка выдала вас!")
            voiceActor.speakNastya("Улика три из трёх — снимок ведьмы! Бежим, вспышка нас выдала!", forceInterrupt = true)
            pushHudSnapshot()
            return
        }

        // Check Evidence #1: Boiling cauldron & human recipe book (Kitchen)
        val nearCauldron = cauldronCells.any { (cx, cz) -> isFacingPoint(cx, cz, 6.2f, 0.20f) }
        if (!evidencesCollected.contains(1) && nearCauldron) {
            evidencesCollected.add(1)
            showBanner("Улика 1/3: Сняты котёл и книга рецептов!")
            voiceActor.speakNastya("Первая улика снята! Котёл и книга жутких рецептов на фото.", forceInterrupt = true)
            pushHudSnapshot()
            return
        }

        // Check Evidence #2: Backpacks & passports of missing teens (Basement)
        if (!evidencesCollected.contains(2) && isFacingPoint(evidenceTeensX, evidenceTeensZ, 6.0f, 0.20f)) {
            evidencesCollected.add(2)
            showBanner("Улика 2/3: Сняты рюкзаки и паспорта!")
            voiceActor.speakNastya("Вторая улика — рюкзаки и паспорта пропавших ребят! Полиция поверит!", forceInterrupt = true)
            pushHudSnapshot()
            return
        }

        // Even a missed flash nearby draws the Witch's attention!
        if (hypot(witchX - playerX, witchZ - playerZ) < 8.5f) {
            witchInvestigateTargetX = playerX
            witchInvestigateTargetZ = playerZ
        }
        showBanner("В кадре нет улик (ищите котёл, паспорта или ведьму)")
    }

    private fun performWeaponAttack() {
        if (equippedWeapon == WeaponType.NONE || attackCooldown > 0f) return

        if (equippedWeapon == WeaponType.CROSSBOW) {
            if (crossbowAmmo <= 0) {
                soundEngine.playLockedRattle()
                showBanner("Болты закончились! Переключитесь на ближнее оружие.")
                return
            }
            crossbowAmmo--
            attackCooldown = equippedWeapon.cooldownSeconds
            attackAnimTimer = 0.35f
            soundEngine.playCrossbowShot()

            val yawRad = Math.toRadians(playerYawDeg.toDouble())
            val pitchRad = Math.toRadians(playerPitchDeg.toDouble())
            val dx = (sin(yawRad) * cos(pitchRad)).toFloat()
            val dy = sin(pitchRad).toFloat()
            val dz = (-cos(yawRad) * cos(pitchRad)).toFloat()
            activeBolts.add(
                ActiveBolt(
                    x = playerX + dx * 0.55f,
                    y = PLAYER_EYE_HEIGHT - 0.12f + dy * 0.55f,
                    z = playerZ + dz * 0.55f,
                    vx = dx * 18.5f,
                    vy = dy * 18.5f,
                    vz = dz * 18.5f
                )
            )
            return
        }

        // Melee attack (IRON_POKER or SILVER_STAKE)
        attackCooldown = equippedWeapon.cooldownSeconds
        attackAnimTimer = 0.36f
        soundEngine.playWeaponSwing()

        val dist = hypot(witchX - playerX, witchZ - playerZ)
        if (dist <= equippedWeapon.rangeMeters && hasLineOfSight(playerX, playerZ, witchX, witchZ)) {
            val yawRad = Math.toRadians(playerYawDeg.toDouble())
            val fwdX = sin(yawRad).toFloat()
            val fwdZ = -cos(yawRad).toFloat()
            val toWitchX = (witchX - playerX) / dist.coerceAtLeast(0.01f)
            val toWitchZ = (witchZ - playerZ) / dist.coerceAtLeast(0.01f)
            val dot = fwdX * toWitchX + fwdZ * toWitchZ
            if (dot > 0.35f || dist < 1.35f) {
                hitWitchWithWeapon(equippedWeapon.stunSeconds, knockbackDist = 1.9f)
            }
        }
    }

    private fun updateProjectiles(dt: Float) {
        val iter = activeBolts.iterator()
        while (iter.hasNext()) {
            val b = iter.next()
            b.x += b.vx * dt
            b.y += b.vy * dt
            b.z += b.vz * dt
            b.lifeSec -= dt

            val r = (b.z / CELL_SIZE).toInt()
            val c = (b.x / CELL_SIZE).toInt()
            val hitWall = r !in 0 until rows || c !in 0 until cols || wallCells[r][c] || b.y < 0.05f || b.y > WALL_HEIGHT
            if (hitWall || b.lifeSec <= 0f) {
                iter.remove()
                continue
            }
            if (hypot(b.x - witchX, b.z - witchZ) < 0.9f) {
                iter.remove()
                hitWitchWithWeapon(WeaponType.CROSSBOW.stunSeconds, knockbackDist = 2.4f)
            }
        }
    }

    private fun hitWitchWithWeapon(stunSec: Float, knockbackDist: Float) {
        witchStunnedTimer = stunSec
        witchAlerted = false
        soundEngine.playWeaponHit()
        // Knock Witch back safely if walkable
        val dx = witchX - playerX
        val dz = witchZ - playerZ
        val len = hypot(dx, dz).coerceAtLeast(0.01f)
        val kx = witchX + (dx / len) * knockbackDist
        val kz = witchZ + (dz / len) * knockbackDist
        val kr = (kz / CELL_SIZE).toInt()
        val kc = (kx / CELL_SIZE).toInt()
        if (kr in 0 until rows && kc in 0 until cols && !wallCells[kr][kc]) {
            witchX = kx
            witchZ = kz
        }
        showBanner("Попадание! Ведьма оглушена на ${"%.1f".format(stunSec)} сек!")
        voiceActor.speakNastya("Есть! Получила! Бежим, пока она приходит в себя!", forceInterrupt = true)
    }

    private fun handleInteraction(target: NearbyTarget) {
        if (isHiding) {
            isHiding = false
            playerX = preHideX
            playerZ = preHideZ
            showBanner("Вы вышли из укрытия.")
            return
        }

        when (target.type) {
            NearbyTargetType.DOOR -> {
                val parts = target.id.split("_")
                val r = parts.getOrNull(0)?.toIntOrNull() ?: return
                val c = parts.getOrNull(1)?.toIntOrNull() ?: return
                val door = doorMap[r to c] ?: return
                door.isOpen = !door.isOpen
                soundEngine.playDoorCreak()
                showBanner(if (door.isOpen) "Дверь открыта" else "Дверь закрыта")
            }
            NearbyTargetType.ITEM -> {
                val ent = itemEntities.firstOrNull { !it.collected && it.spawn.item.id == target.id } ?: return
                val item = ent.spawn.item
                when (item.type) {
                    ItemType.SALT_WARD -> {
                        ent.collected = true
                        saltWardsCount++
                        soundEngine.playPickup()
                        showBanner("Подобрано: ${item.nameRu}")
                        voiceActor.speakNastya(ent.spawn.voiceLineRu, forceInterrupt = true)
                    }
                    ItemType.LANTERN_OIL -> {
                        ent.collected = true
                        lanternBattery = 1f
                        isFlashlightOn = true
                        soundEngine.playPickup()
                        showBanner("Фонарь заправлен!")
                        voiceActor.speakNastya(ent.spawn.voiceLineRu, forceInterrupt = true)
                    }
                    ItemType.WEAPON -> {
                        ent.collected = true
                        val w = item.weaponType
                        if (w != WeaponType.NONE) {
                            if (!unlockedWeapons.contains(w)) unlockedWeapons.add(w)
                            equippedWeapon = w
                            if (w == WeaponType.CROSSBOW) crossbowAmmo += 3
                        }
                        soundEngine.playPickup()
                        showBanner("Оружие: ${item.nameRu}")
                        voiceActor.speakNastya(ent.spawn.voiceLineRu, forceInterrupt = true)
                    }
                    ItemType.AMMO_BOLT -> {
                        ent.collected = true
                        crossbowAmmo += 4
                        if (!unlockedWeapons.contains(WeaponType.CROSSBOW)) {
                            unlockedWeapons.add(WeaponType.CROSSBOW)
                            equippedWeapon = WeaponType.CROSSBOW
                        }
                        soundEngine.playPickup()
                        showBanner("Болты +4")
                        voiceActor.speakNastya(ent.spawn.voiceLineRu, forceInterrupt = true)
                    }
                    else -> {
                        // KEY, RELIC, MIRROR_CURSED, BLACK_CANDLE, ELECTRICAL_TAPE use the 3-slot Backpack!
                        if (backpackItems.size < MAX_BACKPACK_SLOTS) {
                            ent.collected = true
                            backpackItems.add(item)
                            hasExitKey = backpackItems.any { it.type == ItemType.KEY }
                            soundEngine.playPickup()
                            showBanner("В рюкзаке (${backpackItems.size}/3): ${item.nameRu}")
                            voiceActor.speakNastya(ent.spawn.voiceLineRu, forceInterrupt = true)
                        } else {
                            pendingPickupEntity = ent
                            pausedByUi = true
                            outcome = GameOutcome.PAUSED
                            soundEngine.playLockedRattle()
                            onOpenStash(StashDialogState(StashContainerType.BED_STASH, pendingPickupItem = item))
                        }
                    }
                }
                pushHudSnapshot()
            }
            NearbyTargetType.BED_STASH -> {
                pausedByUi = true
                outcome = GameOutcome.PAUSED
                onOpenStash(StashDialogState(StashContainerType.BED_STASH))
            }
            NearbyTargetType.KITCHEN_CHEST -> {
                pausedByUi = true
                outcome = GameOutcome.PAUSED
                onOpenStash(StashDialogState(StashContainerType.KITCHEN_CHEST))
            }
            NearbyTargetType.BREAKER_SWITCH -> {
                if (!breakerPowerOn) {
                    breakerPowerOn = true
                    soundEngine.playBreakerSwitch()
                    showBanner("Рубильник включён!")
                    voiceActor.speakNastya("Щиток заработал! Теперь линия телефона под напряжением!", forceInterrupt = true)
                } else {
                    showBanner("Питание уже включено")
                }
                pushHudSnapshot()
            }
            NearbyTargetType.ROTARY_PHONE -> {
                if (!phoneWiredFixed) {
                    val tapeIdx = backpackItems.indexOfFirst { it.type == ItemType.ELECTRICAL_TAPE }
                    if (tapeIdx >= 0) {
                        backpackItems.removeAt(tapeIdx)
                        phoneWiredFixed = true
                        soundEngine.playPickup()
                        showBanner("Провод починен изолентой!")
                        voiceActor.speakNastya("Провод замотан! Теперь проверь рубильник и 3 улики, чтобы набрать 112!", forceInterrupt = true)
                    } else {
                        soundEngine.playLockedRattle()
                        showBanner("Провод перерезан! Нужна изолента")
                        voiceActor.speakNastya("Провод телефона перерезан... Надо найти изоленту!", forceInterrupt = true)
                    }
                } else if (!breakerPowerOn) {
                    soundEngine.playLockedRattle()
                    showBanner("Нет тока — включите рубильник в подвале!")
                    voiceActor.speakNastya("Гудка нет... Включи рубильник в подвале!")
                } else if (evidencesCollected.size < 3) {
                    soundEngine.playLockedRattle()
                    showBanner("Снимите 3 улики на Полароид (${evidencesCollected.size}/3)")
                    voiceActor.speakNastya("Полиция без доказательств не приедет! Нужно 3 фото: котёл, паспорта и саму ведьму!")
                } else if (!huntModeActive) {
                    huntModeActive = true
                    huntRemainingFloat = 180f
                    witchRaged = true
                    witchAlerted = true
                    witchInvestigateTargetX = playerX
                    witchInvestigateTargetZ = playerZ
                    soundEngine.playPhoneDial()
                    soundEngine.setHuntState(true, 0f)
                    showBanner("ЗВОНОК В 112! ДЕРЖИТЕСЬ 3:00 ДО СИРЕН!")
                    voiceActor.speakNastya("112! Улики отправлены! Наряд выехал — держимся три минуты! Ведьма в ярости!", forceInterrupt = true)
                } else {
                    showBanner("Полиция уже в пути! Держитесь!")
                }
                pushHudSnapshot()
            }
            NearbyTargetType.NOTE -> {
                val note = noteEntities.firstOrNull { it.spawn.titleRu == target.id } ?: return
                note.readOnce = true
                pausedByUi = true
                outcome = GameOutcome.PAUSED
                voiceActor.speakNastya(note.spawn.voiceLineRu, forceInterrupt = true)
                onOpenNote(note.spawn)
            }
            NearbyTargetType.CLOSET -> {
                val closet = closets.minByOrNull { hypot(it.x - playerX, it.z - playerZ) } ?: return
                preHideX = playerX
                preHideZ = playerZ
                hidingSpotX = closet.x
                hidingSpotZ = closet.z
                isHiding = true
                sawPlayerHide = (chapter.witchChecksClosets || witchRaged) && witchAlerted && hasLineOfSight(witchX, witchZ, playerX, playerZ)
                showBanner(
                    if (sawPlayerHide) "Ведьма видела, как вы спрятались!"
                    else "Вы спрятались в шкафу. Дышите тихо..."
                )
            }
            NearbyTargetType.PUZZLE -> {
                if (puzzleSolved) {
                    showBanner("Магическая печать уже снята!")
                    return
                }
                val relicsHeld = backpackItems.count { it.type == ItemType.RELIC }
                if (relicsHeld < chapter.requiredRelicCount) {
                    soundEngine.playLockedRattle()
                    showBanner("Нужны реликвии в рюкзаке ($relicsHeld / ${chapter.requiredRelicCount})")
                    voiceActor.speakNastya("Механизм не поддаётся... Все реликвии должны быть в рюкзаке!")
                } else {
                    pausedByUi = true
                    outcome = GameOutcome.PAUSED
                    onOpenPuzzle(chapter.puzzleType)
                }
            }
            NearbyTargetType.EXIT_DOOR -> {
                if (!puzzleSolved) {
                    soundEngine.playLockedRattle()
                    showBanner("Выход скован магией! Решите загадку.")
                    voiceActor.speakNastya("Дверь заперта магией! Сначала нужно решить загадку в комнатах.")
                } else if (!hasExitKey) {
                    soundEngine.playLockedRattle()
                    showBanner("Нужен Тяжёлый ключ в рюкзаке!")
                    voiceActor.speakNastya("Замок не открывается... Нужен тяжёлый ключ!")
                } else {
                    soundEngine.playUnlock()
                    soundEngine.setHuntState(false, 0f)
                    outcome = GameOutcome.CHAPTER_ESCAPED
                    voiceActor.speakNastya("Дверь открылась! Бежим на следующий этаж!", forceInterrupt = true)
                    pushHudSnapshot()
                }
            }
            NearbyTargetType.NONE -> Unit
        }
    }

    private fun updateWitchAi(dt: Float, playerSprinting: Boolean) {
        if (witchStunnedTimer > 0f) {
            witchStunnedTimer = (witchStunnedTimer - dt).coerceAtLeast(0f)
            return
        }
        if (witchWarnCooldown > 0f) witchWarnCooldown -= dt

        val hasMirror = backpackItems.any { it.type == ItemType.MIRROR_CURSED }
        val hasBlackCandle = backpackItems.any { it.type == ItemType.BLACK_CANDLE }

        val dx = playerX - witchX
        val dz = playerZ - witchZ
        val dist = hypot(dx, dz)

        var sightRange = if (isFlashlightOn || hasBlackCandle) chapter.witchVisionRange else chapter.witchVisionRange * 0.56f
        if (witchRaged) sightRange *= 1.30f
        val canSeePlayer = (!isHiding || sawPlayerHide) &&
            dist < sightRange &&
            hasLineOfSight(witchX, witchZ, playerX, playerZ)

        val effectiveHearing = if (hasMirror) chapter.witchHearingRange * 1.95f else chapter.witchHearingRange
        val canHearPlayer = !isHiding && playerSprinting && dist < effectiveHearing
        val canSmellCandle = !isHiding && hasBlackCandle && dist < 10.5f

        if (canSeePlayer) {
            if (!witchAlerted) {
                soundEngine.playWitchAlert()
                if (witchWarnCooldown <= 0f) {
                    witchWarnCooldown = 14f
                    voiceActor.speakNastya("Она меня заметила! Используй оружие или беги!", forceInterrupt = true)
                }
            }
            witchAlerted = true
            witchInvestigateTargetX = playerX
            witchInvestigateTargetZ = playerZ
        } else if (canHearPlayer || canSmellCandle) {
            witchInvestigateTargetX = playerX
            witchInvestigateTargetZ = playerZ
            if (!witchAlerted && witchWarnCooldown <= 0f) {
                witchWarnCooldown = 12f
                showBanner(if (canSmellCandle) "Ведьма учуяла запах Чёрной свечи!" else if (hasMirror) "Звон зеркальца выдал ваш бег!" else "Ведьма услышала шаги!")
            }
        } else if (witchRaged) {
            // In 112 Hunt Mode, Witch relentlessly closes in toward player's zone!
            witchInvestigateTargetX = playerX
            witchInvestigateTargetZ = playerZ
        } else if (witchAlerted && dist > sightRange * 1.15f) {
            witchAlerted = false
        }

        if (chapter.witchCanTeleport && !witchAlerted) {
            witchTeleportCooldown -= dt
            if (witchTeleportCooldown <= 0f) {
                witchTeleportCooldown = if (witchRaged) 10f else 16f
                val candidates = walkableCells.filter { (r, c) ->
                    val (cx, cz) = cellCenter(c, r)
                    val d = hypot(cx - playerX, cz - playerZ)
                    d in 9.5f..16.5f
                }
                if (candidates.isNotEmpty()) {
                    val (tr, tc) = candidates[rng.nextInt(candidates.size)]
                    val (nx, nz) = cellCenter(tc, tr)
                    witchX = nx
                    witchZ = nz
                    showBanner("В тенях мелькнул силуэт ведьмы...")
                }
            }
        }

        val targetWorldX: Float
        val targetWorldZ: Float
        val baseSpeed: Float

        when {
            witchAlerted -> {
                targetWorldX = playerX
                targetWorldZ = playerZ
                baseSpeed = chapter.witchChaseSpeed
            }
            witchInvestigateTargetX > 0f -> {
                targetWorldX = witchInvestigateTargetX
                targetWorldZ = witchInvestigateTargetZ
                baseSpeed = chapter.witchSpeed * 1.15f
                if (hypot(witchX - targetWorldX, witchZ - targetWorldZ) < 1.1f) {
                    witchInvestigateTargetX = -1f
                    witchInvestigateTargetZ = -1f
                }
            }
            else -> {
                val patrol = witchPatrolTarget
                if (patrol == null || hypot(
                        witchX - cellCenter(patrol.second, patrol.first).first,
                        witchZ - cellCenter(patrol.second, patrol.first).second
                    ) < 1.0f
                ) {
                    witchPatrolTarget = walkableCells[rng.nextInt(walkableCells.size)]
                }
                val p = witchPatrolTarget!!
                val (px, pz) = cellCenter(p.second, p.first)
                targetWorldX = px
                targetWorldZ = pz
                baseSpeed = chapter.witchSpeed
            }
        }

        val speed = if (witchRaged) baseSpeed * 1.36f else baseSpeed

        val (stepX, stepZ) = nextBfsWaypoint(witchX, witchZ, targetWorldX, targetWorldZ)
        val toStepX = stepX - witchX
        val toStepZ = stepZ - witchZ
        val stepLen = hypot(toStepX, toStepZ).coerceAtLeast(0.001f)

        // Witch opens closed interior doors as she walks through them
        val wr = (stepZ / CELL_SIZE).toInt()
        val wc = (stepX / CELL_SIZE).toInt()
        doorMap[wr to wc]?.let { d ->
            if (!d.isOpen && hypot(d.x - witchX, d.z - witchZ) < 2.2f) {
                d.isOpen = true
                soundEngine.playDoorCreak()
            }
        }

        val moveDist = (speed * dt).coerceAtMost(stepLen)
        witchX += (toStepX / stepLen) * moveDist
        witchZ += (toStepZ / stepLen) * moveDist
        witchWalkPhase += dt * speed * 2.2f

        val targetYaw = Math.toDegrees(atan2(toStepX.toDouble(), toStepZ.toDouble())).toFloat()
        witchYawDeg = lerpAngleDeg(witchYawDeg, targetYaw, (dt * 7f).coerceAtMost(1f))
    }

    private fun lerpAngleDeg(a: Float, b: Float, t: Float): Float {
        var diff = (b - a) % 360f
        if (diff > 180f) diff -= 360f
        if (diff < -180f) diff += 360f
        return a + diff * t
    }

    private fun nextBfsWaypoint(fromX: Float, fromZ: Float, toX: Float, toZ: Float): Pair<Float, Float> {
        val sr = (fromZ / CELL_SIZE).toInt().coerceIn(0, rows - 1)
        val sc = (fromX / CELL_SIZE).toInt().coerceIn(0, cols - 1)
        val gr = (toZ / CELL_SIZE).toInt().coerceIn(0, rows - 1)
        val gc = (toX / CELL_SIZE).toInt().coerceIn(0, cols - 1)
        if (sr == gr && sc == gc) return toX to toZ

        val visited = Array(rows) { BooleanArray(cols) }
        val parentR = Array(rows) { IntArray(cols) { -1 } }
        val parentC = Array(rows) { IntArray(cols) { -1 } }
        val queue = ArrayDeque<Pair<Int, Int>>()
        queue.add(sr to sc)
        visited[sr][sc] = true

        val dirs = arrayOf(-1 to 0, 1 to 0, 0 to -1, 0 to 1)
        var found = false
        while (queue.isNotEmpty()) {
            val (r, c) = queue.removeFirst()
            if (r == gr && c == gc) {
                found = true
                break
            }
            for ((dr, dc) in dirs) {
                val nr = r + dr
                val nc = c + dc
                if (nr in 0 until rows && nc in 0 until cols && !visited[nr][nc] && !wallCells[nr][nc]) {
                    visited[nr][nc] = true
                    parentR[nr][nc] = r
                    parentC[nr][nc] = c
                    queue.add(nr to nc)
                }
            }
        }

        if (!found) return toX to toZ
        var curR = gr
        var curC = gc
        while (true) {
            val pr = parentR[curR][curC]
            val pc = parentC[curR][curC]
            if (pr == -1 || (pr == sr && pc == sc)) break
            curR = pr
            curC = pc
        }
        return cellCenter(curC, curR)
    }

    private fun hasLineOfSight(x0: Float, z0: Float, x1: Float, z1: Float): Boolean {
        val dist = hypot(x1 - x0, z1 - z0)
        val steps = (dist / 0.4f).toInt().coerceAtLeast(1)
        for (i in 1 until steps) {
            val t = i.toFloat() / steps
            val sx = x0 + (x1 - x0) * t
            val sz = z0 + (z1 - z0) * t
            val r = (sz / CELL_SIZE).toInt()
            val c = (sx / CELL_SIZE).toInt()
            if (r !in 0 until rows || c !in 0 until cols || wallCells[r][c]) return false
            // Closed wooden doors also block sight!
            val door = doorMap[r to c]
            if (door != null && !door.isOpen) return false
        }
        return true
    }

    private fun movePlayerWithCollision(dx: Float, dz: Float) {
        val tryX = playerX + dx
        if (!collidesWithWalls(tryX, playerZ, PLAYER_RADIUS)) playerX = tryX
        val tryZ = playerZ + dz
        if (!collidesWithWalls(playerX, tryZ, PLAYER_RADIUS)) playerZ = tryZ
    }

    private fun collidesWithWalls(px: Float, pz: Float, radius: Float): Boolean {
        val minC = ((px - radius) / CELL_SIZE).toInt() - 1
        val maxC = ((px + radius) / CELL_SIZE).toInt() + 1
        val minR = ((pz - radius) / CELL_SIZE).toInt() - 1
        val maxR = ((pz + radius) / CELL_SIZE).toInt() + 1

        for (r in minR..maxR) {
            for (c in minC..maxC) {
                val isClosedDoor = doorMap[r to c]?.let { !it.isOpen && it.openAngleDeg < 45f } == true
                if (r !in 0 until rows || c !in 0 until cols || wallCells[r][c] || isClosedDoor) {
                    val left = c * CELL_SIZE
                    val right = (c + 1) * CELL_SIZE
                    val top = r * CELL_SIZE
                    val bottom = (r + 1) * CELL_SIZE
                    val closestX = px.coerceIn(left, right)
                    val closestZ = pz.coerceIn(top, bottom)
                    val d2 = (px - closestX) * (px - closestX) + (pz - closestZ) * (pz - closestZ)
                    if (d2 < radius * radius) return true
                }
            }
        }
        return false
    }

    private fun findNearbyTarget(): NearbyTarget {
        if (isHiding) return NearbyTarget(NearbyTargetType.CLOSET, "exit_closet", "Выйти")

        itemEntities.filter { !it.collected }.minByOrNull { hypot(it.x - playerX, it.z - playerZ) }?.let {
            if (hypot(it.x - playerX, it.z - playerZ) < 2.4f) {
                return NearbyTarget(NearbyTargetType.ITEM, it.spawn.item.id, "Взять")
            }
        }
        noteEntities.minByOrNull { hypot(it.x - playerX, it.z - playerZ) }?.let {
            if (hypot(it.x - playerX, it.z - playerZ) < 2.25f) {
                return NearbyTarget(NearbyTargetType.NOTE, it.spawn.titleRu, "Записка")
            }
        }
        doors.minByOrNull { hypot(it.x - playerX, it.z - playerZ) }?.let { door ->
            if (hypot(door.x - playerX, door.z - playerZ) < 2.45f) {
                val label = if (door.isOpen) "Закрыть" else "Открыть"
                return NearbyTarget(NearbyTargetType.DOOR, "${door.row}_${door.col}", label)
            }
        }
        if (rotaryPhoneX > 0f && hypot(rotaryPhoneX - playerX, rotaryPhoneZ - playerZ) < 2.35f) {
            val label = when {
                !phoneWiredFixed -> "Починить"
                !breakerPowerOn -> "Нет тока"
                evidencesCollected.size < 3 -> "112 (${evidencesCollected.size}/3)"
                huntModeActive -> "112 (В пути)"
                else -> "Позвонить 112"
            }
            return NearbyTarget(NearbyTargetType.ROTARY_PHONE, "rotary_phone", label)
        }
        if (breakerSwitchX > 0f && hypot(breakerSwitchX - playerX, breakerSwitchZ - playerZ) < 2.35f) {
            return NearbyTarget(
                NearbyTargetType.BREAKER_SWITCH,
                "breaker",
                if (breakerPowerOn) "Включён" else "Рубильник",
                canInteract = !breakerPowerOn
            )
        }
        if (kitchenChestX > 0f && hypot(kitchenChestX - playerX, kitchenChestZ - playerZ) < 2.35f) {
            val label = if (kitchenChestItems.isNotEmpty()) "Сундук (${kitchenChestItems.size})" else "Сундук"
            return NearbyTarget(NearbyTargetType.KITCHEN_CHEST, "kitchen_chest", label)
        }
        bedStashSpots.minByOrNull { hypot(it.first - playerX, it.second - playerZ) }?.let { (bx, bz) ->
            if (hypot(bx - playerX, bz - playerZ) < 2.4f) {
                val label = if (stashedUnderBedItems.isNotEmpty()) "Тайник (${stashedUnderBedItems.size})" else "Под кровать"
                return NearbyTarget(NearbyTargetType.BED_STASH, "bed_stash", label)
            }
        }
        if (hypot(puzzleAltarX - playerX, puzzleAltarZ - playerZ) < 2.5f) {
            val label = if (puzzleSolved) "Решено" else "Замок"
            return NearbyTarget(NearbyTargetType.PUZZLE, "puzzle", label, canInteract = !puzzleSolved)
        }
        if (hypot(exitDoorX - playerX, exitDoorZ - playerZ) < 2.65f) {
            return NearbyTarget(NearbyTargetType.EXIT_DOOR, "exit", "Выход")
        }
        closets.minByOrNull { hypot(it.x - playerX, it.z - playerZ) }?.let {
            if (hypot(it.x - playerX, it.z - playerZ) < 2.25f) {
                return NearbyTarget(NearbyTargetType.CLOSET, "closet", "Спрятаться")
            }
        }
        return NearbyTarget()
    }

    private fun computeWitchRoomNameRu(): String {
        if (cauldronCells.any { (cx, cz) -> hypot(witchX - cx, witchZ - cz) < 5.5f }) {
            return "Кухня (у котла)"
        }
        if (kitchenChestX > 0f && hypot(witchX - kitchenChestX, witchZ - kitchenChestZ) < 5.0f) {
            return "Кухня"
        }
        if (evidenceTeensX > 0f && hypot(witchX - evidenceTeensX, witchZ - evidenceTeensZ) < 5.0f) {
            return "Угол пленниц"
        }
        if (hypot(witchX - puzzleAltarX, witchZ - puzzleAltarZ) < 5.0f) {
            return "У алтаря"
        }
        val normC = (witchX / (cols * CELL_SIZE)).coerceIn(0f, 1f)
        val normR = (witchZ / (rows * CELL_SIZE)).coerceIn(0f, 1f)
        return when {
            normR < 0.34f && normC < 0.45f -> "Северо-западный отсек"
            normR < 0.34f && normC >= 0.45f -> "Северо-восточное крыло"
            normR > 0.68f && normC < 0.45f -> "Юго-западная комната"
            normR > 0.68f && normC >= 0.45f -> "Юго-восточная комната"
            normC < 0.32f -> "Западный коридор"
            normC > 0.68f -> "Восточный коридор"
            else -> "Центр этажа"
        }
    }

    private fun buildObjectiveText(): String {
        if (huntModeActive) {
            val mins = huntRemainingFloat.toInt() / 60
            val secs = huntRemainingFloat.toInt() % 60
            return "112: Держитесь ${mins}:%02d до приезда полиции!".format(secs)
        }
        val relicsHeld = backpackItems.count { it.type == ItemType.RELIC }
        return when {
            evidencesCollected.size == 3 && (!phoneWiredFixed || !breakerPowerOn) ->
                "Улики 3/3! Почините телефон изолентой, включите рубильник и звоните 112"
            evidencesCollected.size == 3 ->
                "Всё готово! Наберите 112 по дисковому телефону!"
            !puzzleSolved && relicsHeld < chapter.requiredRelicCount ->
                "Ищите реликвии ($relicsHeld/${chapter.requiredRelicCount}), ключ или 3 улики для Полароида"
            !puzzleSolved ->
                "Решите загадку: ${chapter.puzzleTitleRu} (или вызовите 112)"
            !hasExitKey ->
                "Найдите Тяжёлый ключ (в комнатах или под кроватью / в сундуке)"
            else ->
                "Путь открыт! Бегите к выходу с этажа!"
        }
    }

    private fun pushHudSnapshot() {
        val distToWitch = hypot(witchX - playerX, witchZ - playerZ)
        val prox = (1f - (distToWitch / 15f)).coerceIn(0f, 1f)
        val inMist = mistCells.any { (mx, mz) -> hypot(playerX - mx, playerZ - mz) < 1.55f }
        val (sprintNow, moveMagNow) = synchronized(inputLock) {
            inputSprint to hypot(inputMoveX, inputMoveY)
        }
        val cdFrac = if (equippedWeapon.cooldownSeconds > 0f) {
            (attackCooldown / equippedWeapon.cooldownSeconds).coerceIn(0f, 1f)
        } else 0f

        val relicsHeld = backpackItems.count { it.type == ItemType.RELIC }
        val hasMirror = backpackItems.any { it.type == ItemType.MIRROR_CURSED }
        val hasBlackCandle = backpackItems.any { it.type == ItemType.BLACK_CANDLE }
        val hasHeavyKey = backpackItems.any { it.type == ItemType.KEY }
        hasExitKey = hasHeavyKey

        onHudUpdate(
            GameHudState(
                chapterId = chapter.id,
                chapterTitleRu = chapter.titleRu,
                stamina = stamina,
                lanternBattery = lanternBattery,
                sanity = sanity,
                noiseLevel = if (sprintNow && !isHiding) 0.85f else if (moveMagNow > 0.2f && !isHiding) 0.35f else 0.05f,
                witchProximity = prox,
                witchAlerted = witchAlerted,
                witchRaged = witchRaged,
                witchStunnedSeconds = witchStunnedTimer,
                captureAttempts = captureAttempts,
                cageCountdownSec = cageCountdownSec,
                cageHardLock = cageHardLock,
                isHiding = isHiding,
                isFlashlightOn = isFlashlightOn,
                isInPoisonMist = inMist,
                saltWardsCount = saltWardsCount,
                equippedWeapon = equippedWeapon,
                unlockedWeapons = unlockedWeapons.toList(),
                crossbowAmmo = crossbowAmmo,
                weaponCooldownFraction = cdFrac,
                backpackItems = backpackItems.toList(),
                stashedUnderBedItems = stashedUnderBedItems.toList(),
                kitchenChestItems = kitchenChestItems.toList(),
                mirrorWitchRoomRu = if (hasMirror) computeWitchRoomNameRu() else "",
                hasBlackCandle = hasBlackCandle,
                hasHeavyKey = hasHeavyKey,
                evidencesCollected = evidencesCollected.toSet(),
                cameraFlashAlpha = cameraFlashAlpha,
                phoneWiredFixed = phoneWiredFixed,
                breakerPowerOn = breakerPowerOn,
                huntModeActive = huntModeActive,
                huntRemainingSec = huntRemainingFloat.toInt().coerceAtLeast(0),
                collectedRelicsCount = relicsHeld,
                requiredRelicsCount = chapter.requiredRelicCount,
                puzzleSolved = puzzleSolved,
                hasExitKey = hasExitKey,
                objectiveTextRu = buildObjectiveText(),
                elapsedSeconds = elapsedFloat.toInt(),
                nearbyTarget = findNearbyTarget(),
                outcome = outcome,
                statusBannerRu = statusBanner
            )
        )
    }

    // =========================================================================
    // 3D RENDERING
    // =========================================================================

    private fun renderScene() {
        GLES20.glClearColor(chapter.fogColor.r, chapter.fogColor.g, chapter.fogColor.b, 1f)
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)

        GLES20.glUseProgram(shaders.programId)
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glUniform1i(shaders.uTexture, 0)

        val camX = if (isHiding) hidingSpotX else playerX
        val camZ = if (isHiding) hidingSpotZ else playerZ
        val bobY = if (isHiding) 0f else sin(headBobPhase) * 0.045f
        val camY = PLAYER_EYE_HEIGHT + bobY

        val yawRad = Math.toRadians(playerYawDeg.toDouble())
        val pitchRad = Math.toRadians(playerPitchDeg.toDouble())
        val dirX = (sin(yawRad) * cos(pitchRad)).toFloat()
        val dirY = sin(pitchRad).toFloat()
        val dirZ = (-cos(yawRad) * cos(pitchRad)).toFloat()

        Matrix.setLookAtM(viewMatrix, 0, camX, camY, camZ, camX + dirX, camY + dirY, camZ + dirZ, 0f, 1f, 0f)
        Matrix.multiplyMM(viewProjMatrix, 0, projMatrix, 0, viewMatrix, 0)

        GLES20.glUniform3f(shaders.uCameraPos, camX, camY, camZ)
        GLES20.glUniform3f(shaders.uCameraDir, dirX, dirY, dirZ)

        val hasBlackCandle = backpackItems.any { it.type == ItemType.BLACK_CANDLE }
        val distToWitch = hypot(witchX - camX, witchZ - camZ)
        val witchFlicker = if (distToWitch < 6f && sin(totalTimeSec * 29f) > 0.72f) 0.35f else 1f
        val baseFlash = if (isFlashlightOn && !isHiding) (0.74f + 0.26f * lanternBattery) * witchFlicker else if (hasBlackCandle && !isHiding) 0.58f else 0f
        GLES20.glUniform1f(shaders.uFlashlightOn, baseFlash)

        val candleAmbientBoost = if (hasBlackCandle) 0.14f else 0f
        GLES20.glUniform3f(
            shaders.uAmbientLight,
            (chapter.ambientLight.r + candleAmbientBoost).coerceAtMost(0.55f),
            (chapter.ambientLight.g + candleAmbientBoost * 0.78f).coerceAtMost(0.50f),
            (chapter.ambientLight.b + candleAmbientBoost * 1.1f).coerceAtMost(0.60f)
        )
        GLES20.glUniform3f(shaders.uFogColor, chapter.fogColor.r, chapter.fogColor.g, chapter.fogColor.b)
        GLES20.glUniform1f(shaders.uFogDensity, if (isHiding) 0.13f else if (hasBlackCandle) 0.056f else 0.082f)

        val auraPulse = 0.85f + 0.25f * sin(totalTimeSec * (if (witchRaged) 9.5f else 4.5f))
        val auraR = if (witchRaged || witchAlerted) 1.0f else chapter.witchAuraColor.r
        val auraG = if (witchRaged) 0.05f else if (witchAlerted) 0.16f else chapter.witchAuraColor.g
        val auraB = if (witchRaged) 0.12f else if (witchAlerted) 0.24f else chapter.witchAuraColor.b
        GLES20.glUniform3f(shaders.uWitchPos, witchX, 1.45f, witchZ)
        GLES20.glUniform3f(shaders.uWitchAuraColor, auraR * auraPulse, auraG * auraPulse, auraB * auraPulse)

        val sortedTorches = torchLights.sortedBy { hypot(it.x - camX, it.z - camZ) }
        val t0 = sortedTorches.getOrNull(0) ?: PointLightProp(-999f, 2f, -999f)
        val t1 = sortedTorches.getOrNull(1) ?: PointLightProp(-999f, 2f, -999f)
        val flicker = 0.86f + 0.14f * sin(totalTimeSec * 11.3f)
        GLES20.glUniform3f(shaders.uTorchPos0, t0.x, t0.y, t0.z)
        GLES20.glUniform3f(shaders.uTorchPos1, t1.x, t1.y, t1.z)
        GLES20.glUniform3f(
            shaders.uTorchColor,
            chapter.torchColor.r * flicker,
            chapter.torchColor.g * flicker,
            chapter.torchColor.b * flicker
        )

        val centerMapX = (cols * CELL_SIZE) / 2f
        val centerMapZ = (rows * CELL_SIZE) / 2f
        val floorTex = if (chapter.id.number == 1 || chapter.id.number == 4) textures.floorStone else textures.floorPlanks
        drawStaticMesh(floorMesh, floorTex, centerMapX, 0f, centerMapZ)
        drawStaticMesh(ceilingMesh, textures.ceilingWood, centerMapX, WALL_HEIGHT, centerMapZ)

        val wallTex = when (chapter.id.number) {
            1 -> textures.dungeonWall
            2 -> textures.kitchenWall
            3 -> textures.bedroomWall
            4 -> textures.chapelWall
            else -> textures.towerWall
        }

        for (r in 0 until rows) {
            for (c in 0 until cols) {
                val ch = chapter.mapGrid[r][c]
                val (wx, wz) = cellCenter(c, r)
                when (ch) {
                    '#' -> drawStaticMesh(wallBox, wallTex, wx, WALL_HEIGHT / 2f, wz)
                    'O' -> {
                        // Bubbling Kitchen Cauldron + Recipe Book of Victims (Evidence #1 across chapters)
                        drawStaticMesh(cauldronMesh, textures.altarStone, wx, 0.46f, wz, r = 0.44f, g = 0.42f, b = 0.48f)
                        val boilPulse = 0.40f + 0.05f * sin(totalTimeSec * 5.2f)
                        drawStaticMesh(
                            mistDiscMesh, textures.whiteSolid, wx, 0.92f, wz,
                            sx = boilPulse, sy = 0.35f, sz = boilPulse,
                            r = if (captureAttempts >= 2) 1.0f else 0.42f,
                            g = if (captureAttempts >= 2) 0.35f else 0.92f,
                            b = 0.16f,
                            emissive = 0.85f
                        )
                        // Recipe book stand next to cauldron
                        drawStaticMesh(
                            noteMesh, textures.whiteSolid, wx + 0.62f, 0.96f, wz + 0.35f,
                            rx = 22f, ry = -25f, r = 0.88f, g = 0.22f, b = 0.24f,
                            emissive = if (evidencesCollected.contains(1)) 0.15f else 0.55f
                        )
                    }
                }
            }
        }

        // Iron Dungeon Prison Bars ('I')
        for ((ix, iz) in ironBarCells) {
            for (offset in listOf(-1.05f, -0.35f, 0.35f, 1.05f)) {
                drawStaticMesh(ironBarMesh, textures.whiteSolid, ix + offset, WALL_HEIGHT / 2f, iz, r = 0.28f, g = 0.30f, b = 0.35f)
            }
            drawStaticMesh(ironCrossbarMesh, textures.whiteSolid, ix, 0.55f, iz, r = 0.26f, g = 0.28f, b = 0.33f)
            drawStaticMesh(ironCrossbarMesh, textures.whiteSolid, ix, 2.35f, iz, r = 0.26f, g = 0.28f, b = 0.33f)
            drawStaticMesh(ironCrossbarMesh, textures.whiteSolid, ix, 3.45f, iz, r = 0.26f, g = 0.28f, b = 0.33f)
        }

        // Under-Bed Stash Spots ('U') - Carved Bed with mattress + secret compartment under frame
        for ((bx, bz) in bedStashSpots) {
            drawStaticMesh(bedMattressMesh, textures.rugFabric, bx, 0.42f, bz, r = 0.82f, g = 0.68f, b = 0.72f)
            drawStaticMesh(bedPillowMesh, textures.whiteSolid, bx, 0.65f, bz - 0.72f, r = 0.92f, g = 0.88f, b = 0.80f)
            drawStaticMesh(tableLegMesh, textures.furnitureWood, bx - 0.72f, 0.22f, bz - 1.02f)
            drawStaticMesh(tableLegMesh, textures.furnitureWood, bx + 0.72f, 0.22f, bz - 1.02f)
            drawStaticMesh(tableLegMesh, textures.furnitureWood, bx - 0.72f, 0.22f, bz + 1.02f)
            drawStaticMesh(tableLegMesh, textures.furnitureWood, bx + 0.72f, 0.22f, bz + 1.02f)
            if (stashedUnderBedItems.isNotEmpty() || hasBlackCandle) {
                drawStaticMesh(
                    itemCrystalMesh, textures.whiteSolid, bx, 0.14f, bz + 0.95f,
                    sx = 0.45f, sy = 0.45f, sz = 0.45f, ry = totalTimeSec * 45f,
                    r = 0.30f, g = 0.95f, b = 0.75f, emissive = 0.65f
                )
            }
        }

        // Kitchen Confiscated Items Chest ('K')
        if (kitchenChestX > 0f) {
            drawStaticMesh(bedMattressMesh, textures.furnitureWood, kitchenChestX, 0.36f, kitchenChestZ, sx = 0.68f, sy = 1.45f, sz = 0.44f, r = 0.72f, g = 0.56f, b = 0.42f)
            drawStaticMesh(
                noteMesh, textures.whiteSolid, kitchenChestX, 0.64f, kitchenChestZ,
                sx = 1.6f, sy = 1.8f, sz = 1.2f,
                r = 0.95f, g = 0.78f, b = 0.25f,
                emissive = if (kitchenChestItems.isNotEmpty()) 0.75f else 0.20f
            )
        }

        // Evidence #2: Backpacks & Passports of Previous Missing Teens ('V')
        if (evidenceTeensX > 0f) {
            val evGlow = if (evidencesCollected.contains(2)) 0.18f else 0.62f
            drawStaticMesh(bedPillowMesh, textures.rugFabric, evidenceTeensX - 0.32f, 0.22f, evidenceTeensZ - 0.15f, sx = 0.68f, sy = 1.8f, sz = 1.15f, r = 0.35f, g = 0.55f, b = 0.78f, emissive = evGlow * 0.4f)
            drawStaticMesh(bedPillowMesh, textures.closetWood, evidenceTeensX + 0.34f, 0.22f, evidenceTeensZ + 0.18f, sx = 0.65f, sy = 1.7f, sz = 1.05f, r = 0.75f, g = 0.32f, b = 0.28f, emissive = evGlow * 0.4f)
            drawStaticMesh(floorPaperMesh, textures.whiteSolid, evidenceTeensX - 0.05f, 0.04f, evidenceTeensZ + 0.38f, ry = 18f, r = 0.78f, g = 0.14f, b = 0.22f, emissive = evGlow)
            drawStaticMesh(floorPaperMesh, textures.whiteSolid, evidenceTeensX + 0.18f, 0.04f, evidenceTeensZ - 0.34f, ry = -32f, r = 0.22f, g = 0.38f, b = 0.76f, emissive = evGlow)
        }

        // Rotary Phone 112 ('F')
        if (rotaryPhoneX > 0f) {
            drawStaticMesh(altarMesh, textures.furnitureWood, rotaryPhoneX, 0.48f, rotaryPhoneZ, sx = 0.65f, sy = 0.85f, sz = 0.65f)
            drawStaticMesh(noteMesh, textures.whiteSolid, rotaryPhoneX, 1.01f, rotaryPhoneZ, sx = 0.9f, sy = 2.6f, sz = 1.0f, r = 0.16f, g = 0.16f, b = 0.20f)
            val ready = phoneWiredFixed && breakerPowerOn && evidencesCollected.size >= 3
            drawStaticMesh(
                itemCrystalMesh, textures.whiteSolid, rotaryPhoneX, 1.24f, rotaryPhoneZ,
                sx = 0.55f, sy = 0.55f, sz = 0.55f, ry = totalTimeSec * 90f,
                r = if (ready) 0.25f else 0.95f,
                g = if (ready) 0.95f else if (phoneWiredFixed) 0.72f else 0.22f,
                b = if (huntModeActive) 1.0f else 0.24f,
                emissive = 0.9f
            )
        }

        // Basement Electrical Breaker Switch ('X')
        if (breakerSwitchX > 0f) {
            drawStaticMesh(closetMesh, textures.altarStone, breakerSwitchX, 1.1f, breakerSwitchZ, sx = 0.55f, sy = 0.55f, sz = 0.45f, r = 0.45f, g = 0.48f, b = 0.52f)
            drawStaticMesh(
                itemCrystalMesh, textures.whiteSolid, breakerSwitchX, 1.35f, breakerSwitchZ,
                sx = 0.6f, sy = 0.6f, sz = 0.6f,
                r = if (breakerPowerOn) 0.25f else 1.0f,
                g = if (breakerPowerOn) 1.0f else 0.20f,
                b = 0.25f,
                emissive = 0.95f
            )
        }

        // Real 3D Furnished House Interior ('B') - Tables, Benches, Tableware, Beds & Pews
        for ((idx, cell) in furnitureCells.withIndex()) {
            val (bx, bz) = cell
            if (chapter.id.number == 3 && idx % 2 == 0) {
                drawStaticMesh(bedMattressMesh, textures.rugFabric, bx, 0.42f, bz, r = 0.85f, g = 0.72f, b = 0.74f)
                drawStaticMesh(bedPillowMesh, textures.whiteSolid, bx, 0.65f, bz - 0.72f, r = 0.90f, g = 0.88f, b = 0.82f)
                drawStaticMesh(tableLegMesh, textures.furnitureWood, bx - 0.72f, 0.22f, bz - 1.02f)
                drawStaticMesh(tableLegMesh, textures.furnitureWood, bx + 0.72f, 0.22f, bz - 1.02f)
                drawStaticMesh(tableLegMesh, textures.furnitureWood, bx - 0.72f, 0.22f, bz + 1.02f)
                drawStaticMesh(tableLegMesh, textures.furnitureWood, bx + 0.72f, 0.22f, bz + 1.02f)
            } else {
                drawStaticMesh(tableTopMesh, textures.furnitureWood, bx, 0.86f, bz)
                for (lx in listOf(-0.84f, 0.84f)) {
                    for (lz in listOf(-0.44f, 0.44f)) {
                        drawStaticMesh(tableLegMesh, textures.furnitureWood, bx + lx, 0.42f, bz + lz)
                    }
                }
                drawStaticMesh(benchTopMesh, textures.furnitureWood, bx, 0.46f, bz + 0.78f)
                drawStaticMesh(tableLegMesh, textures.furnitureWood, bx - 0.74f, 0.23f, bz + 0.78f, sx = 0.8f, sy = 0.55f, sz = 0.8f)
                drawStaticMesh(tableLegMesh, textures.furnitureWood, bx + 0.74f, 0.23f, bz + 0.78f, sx = 0.8f, sy = 0.55f, sz = 0.8f)

                drawStaticMesh(plateDiscMesh, textures.whiteSolid, bx - 0.55f, 0.94f, bz + 0.16f, r = 0.86f, g = 0.85f, b = 0.82f)
                drawStaticMesh(plateDiscMesh, textures.whiteSolid, bx + 0.52f, 0.94f, bz - 0.14f, r = 0.86f, g = 0.85f, b = 0.82f)
                drawStaticMesh(candleStickMesh, textures.whiteSolid, bx, 1.04f, bz, r = 0.94f, g = 0.90f, b = 0.76f)
                drawStaticMesh(
                    flameMesh, textures.whiteSolid, bx, 1.22f, bz,
                    sx = 0.35f, sy = 0.55f, sz = 0.35f,
                    ry = totalTimeSec * 120f,
                    r = 1.0f, g = 0.68f, b = 0.22f, emissive = 0.95f
                )
            }
        }

        // Framed Manor Wall Paintings
        for (p in wallPaintings) {
            drawStaticMesh(paintingMesh, textures.wallPainting, p.x, 1.98f, p.z, ry = p.ryDeg)
        }

        // Woven Floor Rugs & Scattered Loose Papers
        for ((rx, rz) in floorRugs) {
            drawStaticMesh(rugMesh, textures.rugFabric, rx, 0.012f, rz)
        }
        for (fp in floorPapers) {
            drawStaticMesh(floorPaperMesh, textures.floorPaper, fp.x, 0.014f, fp.z, ry = fp.ryDeg)
        }

        // Real Hinged 3D Interior Doors ('D') with Frames & Lintels
        for (door in doors) {
            val baseRot = if (door.runsAlongX) 0f else 90f
            drawStaticMesh(doorLintelMesh, wallTex, door.x, WALL_HEIGHT - 0.425f, door.z, ry = baseRot)
            val halfSpan = CELL_SIZE / 2f - 0.11f
            if (door.runsAlongX) {
                drawStaticMesh(doorPostMesh, textures.furnitureWood, door.x - halfSpan, WALL_HEIGHT / 2f, door.z)
                drawStaticMesh(doorPostMesh, textures.furnitureWood, door.x + halfSpan, WALL_HEIGHT / 2f, door.z)
            } else {
                drawStaticMesh(doorPostMesh, textures.furnitureWood, door.x, WALL_HEIGHT / 2f, door.z - halfSpan)
                drawStaticMesh(doorPostMesh, textures.furnitureWood, door.x, WALL_HEIGHT / 2f, door.z + halfSpan)
            }

            val hingeOffset = (CELL_SIZE - 0.16f) / 2f
            val leafM = FloatArray(16)
            Matrix.setIdentityM(leafM, 0)
            Matrix.translateM(leafM, 0, door.x, 1.375f, door.z)
            Matrix.rotateM(leafM, 0, baseRot, 0f, 1f, 0f)
            Matrix.translateM(leafM, 0, -hingeOffset, 0f, 0f)
            Matrix.rotateM(leafM, 0, door.openAngleDeg, 0f, 1f, 0f)
            Matrix.translateM(leafM, 0, hingeOffset, 0f, 0f)
            Matrix.multiplyMM(mvpMatrix, 0, viewProjMatrix, 0, leafM, 0)
            GLES20.glUniformMatrix4fv(shaders.uModelMatrix, 1, false, leafM, 0)
            GLES20.glUniformMatrix4fv(shaders.uMvpMatrix, 1, false, mvpMatrix, 0)
            GLES20.glUniform4f(shaders.uColorTint, 1f, 1f, 1f, 1f)
            GLES20.glUniform1f(shaders.uEmissive, 0.04f)
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures.interiorDoor)
            interiorDoorLeafMesh.draw(shaders)
        }

        // Mirrors
        for ((mx, mz) in mirrorEntities) {
            drawStaticMesh(mirrorFrameMesh, textures.closetWood, mx, 1.22f, mz)
            drawStaticMesh(mirrorGlassMesh, textures.whiteSolid, mx, 1.22f, mz, r = 0.55f, g = 0.72f, b = 0.98f, emissive = 0.28f)
        }

        // Poison Mist Zones
        for ((mx, mz) in mistCells) {
            val pulse = 0.85f + 0.15f * sin(totalTimeSec * 2.5f + mx)
            drawStaticMesh(mistDiscMesh, textures.whiteSolid, mx, 0.08f, mz, sx = pulse, sy = 1f, sz = pulse,
                r = 0.62f, g = 0.22f, b = 0.88f, emissive = 0.45f)
        }

        // Torches
        for (torch in torchLights) {
            drawStaticMesh(torchPostMesh, textures.closetWood, torch.x, 0.95f, torch.z)
            drawStaticMesh(flameMesh, textures.whiteSolid, torch.x, 2.02f, torch.z, ry = totalTimeSec * 90f,
                r = chapter.torchColor.r, g = chapter.torchColor.g, b = chapter.torchColor.b, emissive = 0.95f)
        }

        // Hiding Wardrobes
        for (cl in closets) {
            drawStaticMesh(closetMesh, textures.closetWood, cl.x, 1.225f, cl.z)
        }

        // Puzzle Altar + Black Candle Secret Runes when carried!
        drawStaticMesh(altarMesh, textures.altarStone, puzzleAltarX, 0.58f, puzzleAltarZ)
        val altarColorR = if (puzzleSolved) 0.98f else if (hasBlackCandle) 0.76f else 0.28f
        val altarColorG = if (puzzleSolved) 0.78f else if (hasBlackCandle) 0.45f else 0.95f
        val altarColorB = if (puzzleSolved) 0.28f else if (hasBlackCandle) 1.0f else 0.72f
        drawStaticMesh(itemCrystalMesh, textures.whiteSolid,
            puzzleAltarX, 1.48f + sin(totalTimeSec * 2.2f) * 0.08f, puzzleAltarZ,
            ry = totalTimeSec * 55f, sx = 1.25f, sy = 1.25f, sz = 1.25f,
            r = altarColorR, g = altarColorG, b = altarColorB, emissive = 0.85f)
        if (hasBlackCandle) {
            for (angleIdx in 0..3) {
                val ang = totalTimeSec * 1.4f + angleIdx * 1.57f
                drawStaticMesh(
                    itemCrystalMesh, textures.whiteSolid,
                    puzzleAltarX + cos(ang) * 0.85f, 1.3f, puzzleAltarZ + sin(ang) * 0.85f,
                    sx = 0.42f, sy = 0.42f, sz = 0.42f,
                    r = 0.78f, g = 0.35f, b = 1.0f, emissive = 0.95f
                )
            }
        }

        // Exit Door
        val doorGlow = if (puzzleSolved && hasExitKey) 0.55f else 0.12f
        drawStaticMesh(
            exitDoorMesh, textures.exitDoor, exitDoorX, 1.48f, exitDoorZ,
            r = if (puzzleSolved && hasExitKey) 0.45f else 0.95f,
            g = if (puzzleSolved && hasExitKey) 1.0f else 0.45f,
            b = if (puzzleSolved && hasExitKey) 0.55f else 0.35f,
            emissive = doorGlow
        )

        // Collectible Items, Weapons, Cursed Artifacts & Tape
        for (ent in itemEntities) {
            if (ent.collected) continue
            val hoverY = 0.95f + sin(totalTimeSec * 2.8f + ent.x) * 0.11f
            val (ir, ig, ib) = when (ent.spawn.item.type) {
                ItemType.KEY -> Triple(1.0f, 0.82f, 0.25f)
                ItemType.SALT_WARD -> Triple(0.45f, 0.88f, 1.0f)
                ItemType.LANTERN_OIL -> Triple(1.0f, 0.55f, 0.18f)
                ItemType.WEAPON -> Triple(1.0f, 0.28f, 0.36f)
                ItemType.AMMO_BOLT -> Triple(0.65f, 0.96f, 1.0f)
                ItemType.MIRROR_CURSED -> Triple(0.55f, 0.92f, 1.0f)
                ItemType.BLACK_CANDLE -> Triple(0.82f, 0.38f, 1.0f)
                ItemType.ELECTRICAL_TAPE -> Triple(0.22f, 0.65f, 1.0f)
                else -> Triple(0.32f, 1.0f, 0.65f)
            }
            val mesh = if (ent.spawn.item.type == ItemType.LANTERN_OIL || ent.spawn.item.type == ItemType.SALT_WARD || ent.spawn.item.type == ItemType.ELECTRICAL_TAPE) {
                itemSphereMesh
            } else {
                itemCrystalMesh
            }
            drawStaticMesh(mesh, textures.whiteSolid, ent.x, hoverY, ent.z, ry = totalTimeSec * 72f,
                r = ir, g = ig, b = ib, emissive = 0.90f)
        }

        // Lore Notes
        for (note in noteEntities) {
            drawStaticMesh(noteMesh, textures.whiteSolid, note.x, 0.85f + sin(totalTimeSec * 2f) * 0.04f, note.z,
                ry = totalTimeSec * 22f, r = 0.95f, g = 0.88f, b = 0.68f,
                emissive = if (note.readOnce) 0.25f else 0.62f)
        }

        // Flying Crossbow Bolts
        for (b in activeBolts) {
            drawStaticMesh(boltProjectileMesh, textures.whiteSolid, b.x, b.y, b.z,
                ry = totalTimeSec * 360f, r = 0.35f, g = 1.0f, b = 0.90f, emissive = 0.95f)
        }

        // 3D Articulated Witch (crimson raged look in 112 Hunt Mode!)
        witchModel.draw(
            shaders = shaders,
            viewProjMatrix = viewProjMatrix,
            x = witchX,
            z = witchZ,
            yawDeg = witchYawDeg,
            timeSec = totalTimeSec,
            walkPhase = witchWalkPhase,
            isChasing = witchAlerted || witchRaged,
            isStunned = witchStunnedTimer > 0f
        )

        // First-Person 3D Weapon Viewmodel (bottom-right of camera)
        if (!isHiding && equippedWeapon != WeaponType.NONE) {
            GLES20.glUniform1f(shaders.uFogDensity, 0.0f)
            GLES20.glUniform3f(shaders.uCameraPos, 0f, 0f, 0f)
            GLES20.glUniform3f(shaders.uCameraDir, 0f, 0f, -1f)
            val animFrac = if (attackAnimTimer > 0f) (1f - (attackAnimTimer / 0.36f)).coerceIn(0f, 1f) else 0f
            weaponRenderer.drawFirstPersonWeapon(
                shaders = shaders,
                projMatrix = projMatrix,
                cameraMatrixIdentityView = identityMatrix,
                weapon = equippedWeapon,
                hasAmmo = crossbowAmmo > 0,
                timeSec = totalTimeSec,
                walkBob = headBobPhase,
                attackAnimProgress = animFrac
            )
        }
    }

    private fun drawStaticMesh(
        mesh: Mesh,
        textureId: Int,
        x: Float,
        y: Float,
        z: Float,
        rx: Float = 0f,
        ry: Float = 0f,
        rz: Float = 0f,
        sx: Float = 1f,
        sy: Float = 1f,
        sz: Float = 1f,
        r: Float = 1f,
        g: Float = 1f,
        b: Float = 1f,
        emissive: Float = 0f
    ) {
        Matrix.setIdentityM(modelMatrix, 0)
        Matrix.translateM(modelMatrix, 0, x, y, z)
        if (ry != 0f) Matrix.rotateM(modelMatrix, 0, ry, 0f, 1f, 0f)
        if (rx != 0f) Matrix.rotateM(modelMatrix, 0, rx, 1f, 0f, 0f)
        if (rz != 0f) Matrix.rotateM(modelMatrix, 0, rz, 0f, 0f, 1f)
        if (sx != 1f || sy != 1f || sz != 1f) Matrix.scaleM(modelMatrix, 0, sx, sy, sz)

        Matrix.multiplyMM(mvpMatrix, 0, viewProjMatrix, 0, modelMatrix, 0)

        GLES20.glUniformMatrix4fv(shaders.uModelMatrix, 1, false, modelMatrix, 0)
        GLES20.glUniformMatrix4fv(shaders.uMvpMatrix, 1, false, mvpMatrix, 0)
        GLES20.glUniform4f(shaders.uColorTint, r, g, b, 1f)
        GLES20.glUniform1f(shaders.uEmissive, emissive)

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId)
        mesh.draw(shaders)
    }
}
