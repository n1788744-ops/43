package com.example.engine3d

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.opengl.GLES20
import android.opengl.GLUtils
import kotlin.random.Random

class ProceduralTextures {

    val dungeonWall: Int
    val kitchenWall: Int
    val bedroomWall: Int
    val chapelWall: Int
    val towerWall: Int
    val floorPlanks: Int
    val floorStone: Int
    val ceilingWood: Int
    val interiorDoor: Int
    val exitDoor: Int
    val closetWood: Int
    val furnitureWood: Int
    val altarStone: Int
    val witchRobe: Int
    val wallPainting: Int
    val floorPaper: Int
    val rugFabric: Int
    val whiteSolid: Int

    private val allIds = mutableListOf<Int>()

    init {
        dungeonWall = uploadBitmap(generateDungeonPlasterWall())
        kitchenWall = uploadBitmap(generateKitchenWainscotWall())
        bedroomWall = uploadBitmap(generateBedroomDamaskWall())
        chapelWall = uploadBitmap(generateChapelWall())
        towerWall = uploadBitmap(generateTowerWall())
        floorPlanks = uploadBitmap(generateFloorPlanks())
        floorStone = uploadBitmap(generateCheckeredTileFloor())
        ceilingWood = uploadBitmap(generateCeilingWood())
        interiorDoor = uploadBitmap(generatePaneledHouseDoor())
        exitDoor = uploadBitmap(generateExitDoor())
        closetWood = uploadBitmap(generateClosetWood())
        furnitureWood = uploadBitmap(generateFurnitureWood())
        altarStone = uploadBitmap(generateAltarStone())
        witchRobe = uploadBitmap(generateWitchRobe())
        wallPainting = uploadBitmap(generateWallPainting())
        floorPaper = uploadBitmap(generateFloorPaper())
        rugFabric = uploadBitmap(generateRugFabric())
        whiteSolid = uploadBitmap(generateWhiteSolid())
    }

    fun release() {
        if (allIds.isNotEmpty()) {
            GLES20.glDeleteTextures(allIds.size, allIds.toIntArray(), 0)
            allIds.clear()
        }
    }

    private fun uploadBitmap(bmp: Bitmap): Int {
        val ids = IntArray(1)
        GLES20.glGenTextures(1, ids, 0)
        val id = ids[0]
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, id)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR_MIPMAP_LINEAR)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT)
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bmp, 0)
        GLES20.glGenerateMipmap(GLES20.GL_TEXTURE_2D)
        bmp.recycle()
        allIds.add(id)
        return id
    }

    private fun generateWhiteSolid(): Bitmap {
        return Bitmap.createBitmap(16, 16, Bitmap.Config.ARGB_8888).apply {
            eraseColor(Color.WHITE)
        }
    }

    private fun drawGrimeAndCracks(c: Canvas, rng: Random, count: Int) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        for (i in 0 until count) {
            paint.color = Color.argb(28 + rng.nextInt(30), 18, 20, 22)
            c.drawCircle(rng.nextFloat() * 256f, rng.nextFloat() * 256f, 10f + rng.nextFloat() * 22f, paint)
        }
        paint.color = Color.argb(95, 22, 20, 18)
        paint.strokeWidth = 1.6f
        paint.style = Paint.Style.STROKE
        for (i in 0 until 6) {
            var cx = rng.nextFloat() * 220f + 18f
            var cy = rng.nextFloat() * 140f + 10f
            for (seg in 0 until 4) {
                val nx = cx + (rng.nextFloat() - 0.5f) * 24f
                val ny = cy + rng.nextFloat() * 22f
                c.drawLine(cx, cy, nx, ny, paint)
                cx = nx
                cy = ny
            }
        }
    }

    private fun generateDungeonPlasterWall(): Bitmap {
        val bmp = Bitmap.createBitmap(256, 256, Bitmap.Config.ARGB_8888)
        val c = Canvas(bmp)
        val rng = Random(42)
        // Upper cracked aged plaster & damp brick patches
        c.drawColor(Color.rgb(92, 98, 96))
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        for (r in 0..3) {
            val y = r * 30f
            val off = if (r % 2 == 0) 0f else 28f
            var x = -off
            while (x < 256f) {
                val shade = 74 + rng.nextInt(26)
                paint.color = Color.rgb(shade, shade + 4, shade + 5)
                c.drawRect(x + 2f, y + 2f, x + 54f, y + 27f, paint)
                x += 58f
            }
        }
        // Ochre-wood dado rail & dark slate-crimson lower wall
        paint.color = Color.rgb(108, 86, 46)
        c.drawRect(0f, 122f, 256f, 134f, paint)
        paint.color = Color.rgb(66, 36, 34)
        c.drawRect(0f, 134f, 256f, 240f, paint)
        // Dark baseboard
        paint.color = Color.rgb(32, 24, 22)
        c.drawRect(0f, 240f, 256f, 256f, paint)
        drawGrimeAndCracks(c, rng, 18)
        return bmp
    }

    private fun generateKitchenWainscotWall(): Bitmap {
        val bmp = Bitmap.createBitmap(256, 256, Bitmap.Config.ARGB_8888)
        val c = Canvas(bmp)
        val rng = Random(120)
        // Aged gray-olive plaster upper wall (matching old manor dining/kitchen look)
        c.drawColor(Color.rgb(118, 122, 118))
        val paint = Paint()
        for (i in 0..24) {
            val s = 96 + rng.nextInt(30)
            paint.color = Color.argb(40, s, s + 2, s - 4)
            c.drawRect(rng.nextFloat() * 240f, rng.nextFloat() * 120f, rng.nextFloat() * 256f, rng.nextFloat() * 126f, paint)
        }
        // Brass/ochre trim strip
        paint.color = Color.rgb(116, 94, 48)
        c.drawRect(0f, 124f, 256f, 136f, paint)
        // Rich terracotta-crimson lower wainscoting
        paint.color = Color.rgb(88, 36, 30)
        c.drawRect(0f, 136f, 256f, 242f, paint)
        paint.color = Color.rgb(66, 26, 22)
        for (i in 0..7) {
            c.drawRect(i * 32f + 3f, 144f, (i + 1) * 32f - 3f, 234f, paint)
        }
        paint.color = Color.rgb(34, 22, 18)
        c.drawRect(0f, 242f, 256f, 256f, paint)
        drawGrimeAndCracks(c, rng, 16)
        return bmp
    }

    private fun generateBedroomDamaskWall(): Bitmap {
        val bmp = Bitmap.createBitmap(256, 256, Bitmap.Config.ARGB_8888)
        val c = Canvas(bmp)
        val rng = Random(211)
        // Faded dusty plum striped wallpaper upper half
        c.drawColor(Color.rgb(74, 54, 78))
        val paint = Paint()
        for (x in 0 until 256 step 32) {
            paint.color = Color.rgb(88, 66, 94)
            c.drawRect(x.toFloat(), 0f, x + 14f, 136f, paint)
            paint.color = Color.rgb(112, 86, 118)
            c.drawRect(x + 6f, 0f, x + 8f, 136f, paint)
        }
        // Wood chair rail & carved dark walnut lower paneling
        paint.color = Color.rgb(98, 74, 44)
        c.drawRect(0f, 132f, 256f, 142f, paint)
        paint.color = Color.rgb(46, 28, 24)
        c.drawRect(0f, 142f, 256f, 242f, paint)
        paint.color = Color.rgb(62, 38, 30)
        for (i in 0..3) {
            c.drawRect(i * 64f + 6f, 150f, (i + 1) * 64f - 6f, 232f, paint)
        }
        paint.color = Color.rgb(24, 16, 16)
        c.drawRect(0f, 242f, 256f, 256f, paint)
        drawGrimeAndCracks(c, rng, 12)
        return bmp
    }

    private fun generateChapelWall(): Bitmap {
        val bmp = Bitmap.createBitmap(256, 256, Bitmap.Config.ARGB_8888)
        val c = Canvas(bmp)
        val rng = Random(277)
        c.drawColor(Color.rgb(54, 72, 68))
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        // Stained glass panels & carved stone dado
        paint.color = Color.rgb(42, 128, 98)
        c.drawRect(38f, 18f, 106f, 130f, paint)
        c.drawRect(150f, 18f, 218f, 130f, paint)
        paint.color = Color.rgb(24, 34, 32)
        paint.strokeWidth = 6f
        paint.style = Paint.Style.STROKE
        c.drawRect(38f, 18f, 106f, 130f, paint)
        c.drawRect(150f, 18f, 218f, 130f, paint)
        paint.style = Paint.Style.FILL
        paint.color = Color.rgb(42, 54, 48)
        c.drawRect(0f, 142f, 256f, 256f, paint)
        drawGrimeAndCracks(c, rng, 14)
        return bmp
    }

    private fun generateTowerWall(): Bitmap {
        val bmp = Bitmap.createBitmap(256, 256, Bitmap.Config.ARGB_8888)
        val c = Canvas(bmp)
        val rng = Random(303)
        c.drawColor(Color.rgb(66, 56, 86))
        val paint = Paint()
        for (i in 0..7) {
            paint.color = Color.rgb(74 + rng.nextInt(16), 62 + rng.nextInt(14), 96 + rng.nextInt(18))
            c.drawRect(i * 32f + 1f, 0f, (i + 1) * 32f - 1f, 142f, paint)
        }
        paint.color = Color.rgb(98, 76, 46)
        c.drawRect(0f, 138f, 256f, 148f, paint)
        paint.color = Color.rgb(42, 28, 52)
        c.drawRect(0f, 148f, 256f, 256f, paint)
        drawGrimeAndCracks(c, rng, 15)
        return bmp
    }

    private fun generateFloorPlanks(): Bitmap {
        val bmp = Bitmap.createBitmap(256, 256, Bitmap.Config.ARGB_8888)
        val c = Canvas(bmp)
        c.drawColor(Color.rgb(44, 30, 22))
        val paint = Paint()
        val rng = Random(19)
        for (i in 0 until 8) {
            val y = i * 32f
            val tone = 66 + rng.nextInt(24)
            paint.color = Color.rgb(tone, (tone * 0.68f).toInt(), (tone * 0.48f).toInt())
            c.drawRect(0f, y + 1.5f, 256f, y + 30.5f, paint)
            // Plank grain
            paint.color = Color.argb(35, 24, 14, 8)
            c.drawRect(0f, y + 10f, 256f, y + 12f, paint)
            c.drawRect(0f, y + 22f, 256f, y + 23.5f, paint)
            // Board seam & nails
            paint.color = Color.rgb(24, 15, 10)
            val seam = (i * 83 + 40) % 256
            c.drawRect(seam.toFloat(), y, seam + 2.5f, y + 32f, paint)
            c.drawCircle((seam + 7f) % 256f, y + 7f, 1.8f, paint)
            c.drawCircle((seam + 7f) % 256f, y + 25f, 1.8f, paint)
        }
        return bmp
    }

    private fun generateCheckeredTileFloor(): Bitmap {
        val bmp = Bitmap.createBitmap(256, 256, Bitmap.Config.ARGB_8888)
        val c = Canvas(bmp)
        c.drawColor(Color.rgb(34, 38, 42))
        val paint = Paint()
        val rng = Random(88)
        for (r in 0..3) {
            for (col in 0..3) {
                val base = if ((r + col) % 2 == 0) 96 + rng.nextInt(16) else 66 + rng.nextInt(14)
                paint.color = Color.rgb(base, base + 3, base + 7)
                c.drawRect(col * 64f + 2f, r * 64f + 2f, (col + 1) * 64f - 2f, (r + 1) * 64f - 2f, paint)
            }
        }
        drawGrimeAndCracks(c, rng, 18)
        return bmp
    }

    private fun generateCeilingWood(): Bitmap {
        val bmp = Bitmap.createBitmap(256, 256, Bitmap.Config.ARGB_8888)
        val c = Canvas(bmp)
        c.drawColor(Color.rgb(34, 28, 24))
        val paint = Paint()
        paint.color = Color.rgb(52, 42, 34)
        for (i in 0..3) {
            c.drawRect(0f, i * 64f + 6f, 256f, i * 64f + 24f, paint)
        }
        return bmp
    }

    private fun generatePaneledHouseDoor(): Bitmap {
        val bmp = Bitmap.createBitmap(256, 256, Bitmap.Config.ARGB_8888)
        val c = Canvas(bmp)
        // Painted weathered light-slate/ivory manor door (matching screenshot 2 interior door!)
        c.drawColor(Color.rgb(148, 152, 150))
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        // Outer stiles shadow
        paint.color = Color.rgb(112, 116, 114)
        c.drawRect(18f, 16f, 238f, 244f, paint)
        paint.color = Color.rgb(142, 146, 144)
        c.drawRect(26f, 24f, 230f, 236f, paint)
        // Upper recessed tall panel + lower recessed panel
        paint.color = Color.rgb(96, 100, 98)
        c.drawRect(44f, 34f, 212f, 134f, paint)
        c.drawRect(44f, 154f, 212f, 224f, paint)
        paint.color = Color.rgb(128, 132, 130)
        c.drawRect(54f, 44f, 202f, 124f, paint)
        c.drawRect(54f, 164f, 202f, 214f, paint)
        // Brass/steel handle & lock plate
        paint.color = Color.rgb(56, 52, 46)
        c.drawRect(208f, 126f, 226f, 154f, paint)
        paint.color = Color.rgb(220, 186, 98)
        c.drawCircle(216f, 138f, 8f, paint)
        drawGrimeAndCracks(c, Random(77), 10)
        return bmp
    }

    private fun generateExitDoor(): Bitmap {
        val bmp = Bitmap.createBitmap(256, 256, Bitmap.Config.ARGB_8888)
        val c = Canvas(bmp)
        c.drawColor(Color.rgb(78, 46, 30))
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.color = Color.rgb(34, 36, 42)
        c.drawRect(0f, 38f, 256f, 56f, paint)
        c.drawRect(0f, 196f, 256f, 214f, paint)
        paint.color = Color.rgb(48, 28, 18)
        for (i in 1..4) {
            c.drawRect(i * 51f, 0f, i * 51f + 3f, 256f, paint)
        }
        paint.color = Color.rgb(245, 185, 72)
        c.drawCircle(128f, 128f, 22f, paint)
        paint.color = Color.rgb(24, 14, 10)
        c.drawCircle(128f, 122f, 8f, paint)
        c.drawRect(124f, 124f, 132f, 144f, paint)
        return bmp
    }

    private fun generateClosetWood(): Bitmap {
        val bmp = Bitmap.createBitmap(256, 256, Bitmap.Config.ARGB_8888)
        val c = Canvas(bmp)
        c.drawColor(Color.rgb(66, 40, 26))
        val paint = Paint()
        paint.color = Color.rgb(36, 20, 14)
        c.drawRect(126f, 0f, 130f, 256f, paint)
        paint.color = Color.rgb(18, 12, 12)
        for (y in 24..110 step 14) {
            c.drawRect(26f, y.toFloat(), 114f, y + 6f, paint)
            c.drawRect(142f, y.toFloat(), 230f, y + 6f, paint)
        }
        return bmp
    }

    private fun generateFurnitureWood(): Bitmap {
        val bmp = Bitmap.createBitmap(128, 128, Bitmap.Config.ARGB_8888)
        val c = Canvas(bmp)
        c.drawColor(Color.rgb(108, 66, 36))
        val paint = Paint()
        paint.color = Color.rgb(78, 46, 24)
        for (y in 0 until 128 step 14) {
            c.drawRect(0f, y.toFloat(), 128f, y + 3f, paint)
        }
        return bmp
    }

    private fun generateWallPainting(): Bitmap {
        val bmp = Bitmap.createBitmap(128, 128, Bitmap.Config.ARGB_8888)
        val c = Canvas(bmp)
        // Dark ornate wood frame
        c.drawColor(Color.rgb(42, 26, 16))
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.color = Color.rgb(104, 78, 38)
        c.drawRect(8f, 8f, 120f, 120f, paint)
        // Haunted moonlit manor canvas inside frame
        paint.color = Color.rgb(24, 34, 42)
        c.drawRect(16f, 16f, 112f, 112f, paint)
        paint.color = Color.rgb(168, 186, 180)
        c.drawCircle(84f, 40f, 13f, paint)
        paint.color = Color.rgb(14, 18, 22)
        c.drawRect(16f, 76f, 112f, 112f, paint)
        c.drawRect(34f, 48f, 68f, 90f, paint)
        return bmp
    }

    private fun generateFloorPaper(): Bitmap {
        val bmp = Bitmap.createBitmap(64, 64, Bitmap.Config.ARGB_8888)
        val c = Canvas(bmp)
        c.drawColor(Color.rgb(206, 198, 176))
        val paint = Paint()
        paint.color = Color.rgb(76, 70, 62)
        for (y in 12..52 step 9) {
            c.drawRect(10f, y.toFloat(), 52f, y + 2f, paint)
        }
        return bmp
    }

    private fun generateRugFabric(): Bitmap {
        val bmp = Bitmap.createBitmap(128, 128, Bitmap.Config.ARGB_8888)
        val c = Canvas(bmp)
        c.drawColor(Color.rgb(92, 28, 32))
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.color = Color.rgb(148, 110, 48)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 6f
        c.drawRect(10f, 10f, 118f, 118f, paint)
        paint.color = Color.rgb(52, 18, 24)
        paint.style = Paint.Style.FILL
        c.drawCircle(64f, 64f, 26f, paint)
        return bmp
    }

    private fun generateAltarStone(): Bitmap {
        val bmp = Bitmap.createBitmap(256, 256, Bitmap.Config.ARGB_8888)
        val c = Canvas(bmp)
        c.drawColor(Color.rgb(58, 54, 68))
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(72, 218, 168)
            style = Paint.Style.STROKE
            strokeWidth = 6f
        }
        c.drawCircle(128f, 128f, 86f, paint)
        c.drawCircle(128f, 128f, 52f, paint)
        c.drawLine(42f, 128f, 214f, 128f, paint)
        c.drawLine(128f, 42f, 128f, 214f, paint)
        return bmp
    }

    private fun generateWitchRobe(): Bitmap {
        val bmp = Bitmap.createBitmap(256, 256, Bitmap.Config.ARGB_8888)
        val c = Canvas(bmp)
        c.drawColor(Color.rgb(34, 20, 44))
        val paint = Paint()
        val rng = Random(666)
        for (x in 0 until 256 step 12) {
            val shade = 24 + rng.nextInt(26)
            paint.color = Color.rgb(shade + 8, shade - 4, shade + 18)
            c.drawRect(x.toFloat(), 0f, x + 10f, 256f, paint)
        }
        paint.color = Color.rgb(132, 68, 178)
        c.drawRect(0f, 224f, 256f, 240f, paint)
        return bmp
    }
}
