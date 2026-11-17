package com.example.engine3d

import android.opengl.GLES20
import android.opengl.Matrix
import com.example.model.WeaponType
import kotlin.math.PI
import kotlin.math.sin

/**
 * Renders Nastya's equipped first-person 3D weapon in view-space (bottom-right of camera)
 * with walking sway, melee slash animation, and crossbow recoil.
 */
class WeaponRenderer3D(private val textures: ProceduralTextures) {

    private val handleMesh = MeshBuilder.createCylinder(0.022f, 0.022f, 0.56f, 10)
    private val guardMesh = MeshBuilder.createBox(0.16f, 0.028f, 0.045f)
    private val bladeMesh = MeshBuilder.createBox(0.045f, 0.48f, 0.014f)
    private val hookTipMesh = MeshBuilder.createBox(0.09f, 0.026f, 0.024f)

    private val crossbowStockMesh = MeshBuilder.createBox(0.075f, 0.085f, 0.54f)
    private val crossbowLimbMesh = MeshBuilder.createBox(0.46f, 0.028f, 0.045f)
    private val boltShaftMesh = MeshBuilder.createCylinder(0.011f, 0.011f, 0.42f, 8)
    private val boltHeadMesh = MeshBuilder.createOctahedron(0.028f)

    private val modelMatrix = FloatArray(16)
    private val mvpMatrix = FloatArray(16)

    fun drawFirstPersonWeapon(
        shaders: GameShaders,
        projMatrix: FloatArray,
        cameraMatrixIdentityView: FloatArray,
        weapon: WeaponType,
        hasAmmo: Boolean,
        timeSec: Float,
        walkBob: Float,
        attackAnimProgress: Float // 0f = idle, 0..1 = swinging/recoiling
    ) {
        if (weapon == WeaponType.NONE) return

        // Clear depth slightly or render in view space using camera-relative transform
        GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT)

        val bobX = sin(walkBob * 0.5f) * 0.016f
        val bobY = sin(walkBob) * 0.018f
        val base = FloatArray(16)
        Matrix.setIdentityM(base, 0)

        when (weapon) {
            WeaponType.IRON_POKER -> {
                val swing = sin(attackAnimProgress * PI).toFloat()
                Matrix.translateM(base, 0, 0.30f + bobX - swing * 0.24f, -0.26f + bobY - swing * 0.08f, -0.52f - swing * 0.14f)
                Matrix.rotateM(base, 0, 18f + swing * 52f, 0f, 1f, 0f)
                Matrix.rotateM(base, 0, -24f - swing * 58f, 1f, 0f, 0f)
                Matrix.rotateM(base, 0, 12f + swing * 32f, 0f, 0f, 1f)

                drawPart(shaders, projMatrix, cameraMatrixIdentityView, base, handleMesh, textures.whiteSolid,
                    tx = 0f, ty = 0.06f, tz = 0f, r = 0.28f, g = 0.28f, b = 0.32f, emissive = 0.12f)
                drawPart(shaders, projMatrix, cameraMatrixIdentityView, base, hookTipMesh, textures.whiteSolid,
                    tx = -0.04f, ty = 0.34f, tz = 0f, r = 0.52f, g = 0.35f, b = 0.25f, emissive = 0.16f)
            }
            WeaponType.SILVER_STAKE -> {
                val slash = sin(attackAnimProgress * PI).toFloat()
                Matrix.translateM(base, 0, 0.29f + bobX - slash * 0.32f, -0.24f + bobY - slash * 0.06f, -0.48f - slash * 0.12f)
                Matrix.rotateM(base, 0, 14f + slash * 68f, 0f, 1f, 0f)
                Matrix.rotateM(base, 0, -28f - slash * 55f, 1f, 0f, 0f)
                Matrix.rotateM(base, 0, 10f + slash * 44f, 0f, 0f, 1f)

                // Dark hilt
                drawPart(shaders, projMatrix, cameraMatrixIdentityView, base, handleMesh, textures.furnitureWood,
                    tx = 0f, ty = -0.14f, tz = 0f, sx = 1.2f, sy = 0.35f, sz = 1.2f, r = 0.5f, g = 0.35f, b = 0.25f, emissive = 0.1f)
                // Brass guard
                drawPart(shaders, projMatrix, cameraMatrixIdentityView, base, guardMesh, textures.whiteSolid,
                    tx = 0f, ty = -0.03f, tz = 0f, r = 0.92f, g = 0.72f, b = 0.28f, emissive = 0.25f)
                // Gleaming silver blade
                drawPart(shaders, projMatrix, cameraMatrixIdentityView, base, bladeMesh, textures.whiteSolid,
                    tx = 0f, ty = 0.22f, tz = 0f, r = 0.86f, g = 0.95f, b = 1.0f, emissive = 0.32f)
            }
            WeaponType.CROSSBOW -> {
                val recoil = sin(attackAnimProgress * PI).toFloat()
                Matrix.translateM(base, 0, 0.27f + bobX, -0.24f + bobY + recoil * 0.02f, -0.52f + recoil * 0.11f)
                Matrix.rotateM(base, 0, 8f, 0f, 1f, 0f)
                Matrix.rotateM(base, 0, -6f + recoil * 14f, 1f, 0f, 0f)

                // Wooden crossbow stock
                drawPart(shaders, projMatrix, cameraMatrixIdentityView, base, crossbowStockMesh, textures.furnitureWood,
                    tx = 0f, ty = 0f, tz = 0f, r = 0.75f, g = 0.55f, b = 0.38f, emissive = 0.14f)
                // Curved-look front bow limbs
                drawPart(shaders, projMatrix, cameraMatrixIdentityView, base, crossbowLimbMesh, textures.whiteSolid,
                    tx = 0f, ty = 0.035f, tz = -0.22f, r = 0.24f, g = 0.28f, b = 0.35f, emissive = 0.15f)
                // Loaded glowing silver bolt if ammo > 0
                if (hasAmmo) {
                    drawPart(shaders, projMatrix, cameraMatrixIdentityView, base, boltShaftMesh, textures.whiteSolid,
                        tx = 0f, ty = 0.055f, tz = -0.06f, rx = 90f, r = 0.85f, g = 0.92f, b = 1.0f, emissive = 0.4f)
                    drawPart(shaders, projMatrix, cameraMatrixIdentityView, base, boltHeadMesh, textures.whiteSolid,
                        tx = 0f, ty = 0.055f, tz = -0.28f, ry = timeSec * 120f, r = 0.35f, g = 1.0f, b = 0.85f, emissive = 0.85f)
                }
            }
            WeaponType.NONE -> Unit
        }
    }

    private fun drawPart(
        shaders: GameShaders,
        projMatrix: FloatArray,
        viewIdentity: FloatArray,
        parent: FloatArray,
        mesh: Mesh,
        textureId: Int,
        tx: Float = 0f, ty: Float = 0f, tz: Float = 0f,
        rx: Float = 0f, ry: Float = 0f, rz: Float = 0f,
        sx: Float = 1f, sy: Float = 1f, sz: Float = 1f,
        r: Float = 1f, g: Float = 1f, b: Float = 1f,
        emissive: Float = 0.15f
    ) {
        Matrix.translateM(modelMatrix, 0, parent, 0, tx, ty, tz)
        if (ry != 0f) Matrix.rotateM(modelMatrix, 0, ry, 0f, 1f, 0f)
        if (rx != 0f) Matrix.rotateM(modelMatrix, 0, rx, 1f, 0f, 0f)
        if (rz != 0f) Matrix.rotateM(modelMatrix, 0, rz, 0f, 0f, 1f)
        if (sx != 1f || sy != 1f || sz != 1f) Matrix.scaleM(modelMatrix, 0, sx, sy, sz)

        val vp = FloatArray(16)
        Matrix.multiplyMM(vp, 0, projMatrix, 0, viewIdentity, 0)
        Matrix.multiplyMM(mvpMatrix, 0, vp, 0, modelMatrix, 0)

        GLES20.glUniformMatrix4fv(shaders.uModelMatrix, 1, false, modelMatrix, 0)
        GLES20.glUniformMatrix4fv(shaders.uMvpMatrix, 1, false, mvpMatrix, 0)
        GLES20.glUniform4f(shaders.uColorTint, r, g, b, 1f)
        GLES20.glUniform1f(shaders.uEmissive, emissive)

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId)
        mesh.draw(shaders)
    }
}
