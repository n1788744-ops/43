package com.example.engine3d

import android.opengl.GLES20
import android.opengl.Matrix
import kotlin.math.cos
import kotlin.math.sin

/**
 * Detailed articulated 3D Witch model built from hierarchical meshes:
 * tattered swaying skirt + cloak wings, corset torso, shoulder shawl,
 * pale gaunt head with crooked nose & glowing eyes, flowing hair strands,
 * crooked pointed witch hat with glowing rune band, articulated arms & claws,
 * and a gnarled staff topped with a spinning glowing crystal orb.
 */
class WitchModel3D(private val textures: ProceduralTextures) {

    private val skirtMesh = MeshBuilder.createCylinder(0.54f, 0.20f, 1.05f, 18)
    private val capeWingMesh = MeshBuilder.createBox(0.38f, 1.15f, 0.05f)
    private val torsoMesh = MeshBuilder.createCylinder(0.18f, 0.25f, 0.56f, 14)
    private val shawlMesh = MeshBuilder.createCylinder(0.35f, 0.21f, 0.22f, 14)
    private val neckMesh = MeshBuilder.createCylinder(0.08f, 0.09f, 0.16f, 10)
    private val headMesh = MeshBuilder.createSphere(0.18f, 12, 16)
    private val noseMesh = MeshBuilder.createCylinder(0.038f, 0.004f, 0.14f, 8)
    private val eyeMesh = MeshBuilder.createSphere(0.032f, 8, 10)
    private val hairStrandMesh = MeshBuilder.createCylinder(0.032f, 0.055f, 0.62f, 8)

    private val hatBrimMesh = MeshBuilder.createCylinder(0.46f, 0.42f, 0.045f, 18)
    private val hatBandMesh = MeshBuilder.createCylinder(0.205f, 0.185f, 0.07f, 16)
    private val hatLowerCone = MeshBuilder.createCylinder(0.20f, 0.11f, 0.36f, 16)
    private val hatTipCone = MeshBuilder.createCylinder(0.11f, 0.01f, 0.34f, 14)

    private val upperArmMesh = MeshBuilder.createCylinder(0.055f, 0.068f, 0.36f, 10)
    private val forearmMesh = MeshBuilder.createCylinder(0.042f, 0.055f, 0.34f, 10)
    private val clawMesh = MeshBuilder.createCylinder(0.014f, 0.002f, 0.12f, 6)

    private val staffPoleMesh = MeshBuilder.createCylinder(0.032f, 0.026f, 1.88f, 10)
    private val staffCradleMesh = MeshBuilder.createCylinder(0.085f, 0.13f, 0.14f, 10)
    private val staffOrbMesh = MeshBuilder.createOctahedron(0.115f)

    private val modelMatrix = FloatArray(16)
    private val mvpMatrix = FloatArray(16)

    fun draw(
        shaders: GameShaders,
        viewProjMatrix: FloatArray,
        x: Float,
        z: Float,
        yawDeg: Float,
        timeSec: Float,
        walkPhase: Float,
        isChasing: Boolean,
        isStunned: Boolean
    ) {
        val hoverBob = if (isStunned) {
            sin(timeSec * 18f) * 0.04f
        } else {
            sin(walkPhase * 2f) * 0.045f
        }
        val bodyLean = when {
            isStunned -> -14f
            isChasing -> 16f
            else -> 4f
        }
        val shakeYaw = if (isStunned) sin(timeSec * 38f) * 14f else 0f

        val root = FloatArray(16)
        Matrix.setIdentityM(root, 0)
        Matrix.translateM(root, 0, x, hoverBob, z)
        Matrix.rotateM(root, 0, yawDeg + shakeYaw, 0f, 1f, 0f)

        // 1. Flowing lower robe / skirt
        drawPart(
            shaders, viewProjMatrix, root, skirtMesh, textures.witchRobe,
            tx = 0f, ty = 0.54f, tz = 0f,
            rx = sin(walkPhase) * 3f, ry = 0f, rz = cos(walkPhase) * 2.5f,
            r = 0.85f, g = 0.82f, b = 0.95f, emissive = 0.04f
        )

        // 2. Tattered cloak wings behind her
        val wingFlutter = sin(timeSec * 3.8f) * 7f
        drawPart(
            shaders, viewProjMatrix, root, capeWingMesh, textures.witchRobe,
            tx = -0.18f, ty = 0.86f, tz = -0.20f,
            rx = 14f + wingFlutter, ry = 18f, rz = -10f,
            r = 0.6f, g = 0.5f, b = 0.72f, emissive = 0.02f
        )
        drawPart(
            shaders, viewProjMatrix, root, capeWingMesh, textures.witchRobe,
            tx = 0.18f, ty = 0.86f, tz = -0.20f,
            rx = 14f - wingFlutter, ry = -18f, rz = 10f,
            r = 0.6f, g = 0.5f, b = 0.72f, emissive = 0.02f
        )

        // Upper body pivot (hips at y = 1.02)
        val upperRoot = FloatArray(16)
        Matrix.translateM(upperRoot, 0, root, 0, 0f, 1.02f, 0f)
        Matrix.rotateM(upperRoot, 0, bodyLean, 1f, 0f, 0f)

        // 3. Corset Torso & Shoulder Shawl
        drawPart(
            shaders, viewProjMatrix, upperRoot, torsoMesh, textures.witchRobe,
            tx = 0f, ty = 0.28f, tz = 0f,
            r = 0.75f, g = 0.70f, b = 0.85f, emissive = 0.03f
        )
        drawPart(
            shaders, viewProjMatrix, upperRoot, shawlMesh, textures.witchRobe,
            tx = 0f, ty = 0.48f, tz = 0f,
            r = 0.55f, g = 0.42f, b = 0.65f, emissive = 0.05f
        )

        // 4. Neck & Head
        val headRoot = FloatArray(16)
        Matrix.translateM(headRoot, 0, upperRoot, 0, 0f, 0.62f, 0.02f)
        val headTilt = if (isChasing) sin(timeSec * 6f) * 6f else sin(timeSec * 1.6f) * 8f
        Matrix.rotateM(headRoot, 0, headTilt, 0f, 0f, 1f)

        drawPart(
            shaders, viewProjMatrix, headRoot, neckMesh, textures.whiteSolid,
            tx = 0f, ty = -0.02f, tz = 0f,
            r = 0.58f, g = 0.64f, b = 0.56f, emissive = 0.03f
        )
        // Pale gaunt head
        drawPart(
            shaders, viewProjMatrix, headRoot, headMesh, textures.whiteSolid,
            tx = 0f, ty = 0.14f, tz = 0.01f,
            sx = 0.92f, sy = 1.18f, sz = 0.98f,
            r = 0.64f, g = 0.70f, b = 0.60f, emissive = 0.05f
        )
        // Crooked elongated nose pointing forward (+Z)
        drawPart(
            shaders, viewProjMatrix, headRoot, noseMesh, textures.whiteSolid,
            tx = 0f, ty = 0.12f, tz = 0.21f,
            rx = 102f, ry = 0f, rz = 0f,
            r = 0.58f, g = 0.64f, b = 0.55f, emissive = 0.04f
        )

        // Glowing piercing eyes! Crimson when chasing, eerie emerald on patrol, silver when stunned
        val eyeR = when {
            isStunned -> 0.7f
            isChasing -> 1.0f
            else -> 0.22f
        }
        val eyeG = when {
            isStunned -> 0.9f
            isChasing -> 0.14f
            else -> 1.0f
        }
        val eyeB = when {
            isStunned -> 1.0f
            isChasing -> 0.18f
            else -> 0.58f
        }
        drawPart(
            shaders, viewProjMatrix, headRoot, eyeMesh, textures.whiteSolid,
            tx = -0.068f, ty = 0.18f, tz = 0.155f,
            r = eyeR, g = eyeG, b = eyeB, emissive = 0.95f
        )
        drawPart(
            shaders, viewProjMatrix, headRoot, eyeMesh, textures.whiteSolid,
            tx = 0.068f, ty = 0.18f, tz = 0.155f,
            r = eyeR, g = eyeG, b = eyeB, emissive = 0.95f
        )

        // Flowing dark/silver hair strands framing face & back
        for (i in -2..2) {
            val angle = i * 28f
            drawPart(
                shaders, viewProjMatrix, headRoot, hairStrandMesh, textures.whiteSolid,
                tx = sin(Math.toRadians(angle.toDouble())).toFloat() * 0.15f,
                ty = -0.08f,
                tz = -0.06f - cos(Math.toRadians(angle.toDouble())).toFloat() * 0.10f,
                rx = 10f, ry = 0f, rz = i * -6f,
                r = 0.14f, g = 0.12f, b = 0.18f, emissive = 0.02f
            )
        }

        // 5. Iconic Pointed Crooked Witch Hat
        val hatRoot = FloatArray(16)
        Matrix.translateM(hatRoot, 0, headRoot, 0, 0f, 0.30f, -0.01f)
        Matrix.rotateM(hatRoot, 0, -10f, 1f, 0f, 0f)
        Matrix.rotateM(hatRoot, 0, if (isStunned) -16f else 6f, 0f, 0f, 1f)

        drawPart(
            shaders, viewProjMatrix, hatRoot, hatBrimMesh, textures.witchRobe,
            tx = 0f, ty = 0f, tz = 0f,
            r = 0.25f, g = 0.18f, b = 0.32f, emissive = 0.03f
        )
        drawPart(
            shaders, viewProjMatrix, hatRoot, hatBandMesh, textures.whiteSolid,
            tx = 0f, ty = 0.05f, tz = 0f,
            r = if (isChasing) 0.95f else 0.25f,
            g = if (isChasing) 0.25f else 0.85f,
            b = if (isChasing) 0.35f else 0.62f,
            emissive = 0.55f
        )
        drawPart(
            shaders, viewProjMatrix, hatRoot, hatLowerCone, textures.witchRobe,
            tx = 0f, ty = 0.22f, tz = -0.02f,
            rx = -8f, ry = 0f, rz = 0f,
            r = 0.25f, g = 0.18f, b = 0.32f, emissive = 0.03f
        )
        // Crooked bent top tip!
        drawPart(
            shaders, viewProjMatrix, hatRoot, hatTipCone, textures.witchRobe,
            tx = 0.04f, ty = 0.51f, tz = -0.10f,
            rx = -32f, ry = 0f, rz = -14f,
            r = 0.24f, g = 0.16f, b = 0.30f, emissive = 0.03f
        )

        // 6. Left Arm (Reaches forward when chasing, clutches face when stunned!)
        val leftShoulder = FloatArray(16)
        Matrix.translateM(leftShoulder, 0, upperRoot, 0, -0.29f, 0.50f, 0f)
        val leftArmPitch = when {
            isStunned -> -115f
            isChasing -> -78f + sin(timeSec * 8f) * 14f
            else -> -15f + sin(walkPhase) * 18f
        }
        Matrix.rotateM(leftShoulder, 0, leftArmPitch, 1f, 0f, 0f)
        Matrix.rotateM(leftShoulder, 0, if (isStunned) -28f else -10f, 0f, 0f, 1f)

        drawPart(
            shaders, viewProjMatrix, leftShoulder, upperArmMesh, textures.witchRobe,
            tx = 0f, ty = -0.18f, tz = 0f,
            r = 0.42f, g = 0.30f, b = 0.50f, emissive = 0.03f
        )
        val leftElbow = FloatArray(16)
        Matrix.translateM(leftElbow, 0, leftShoulder, 0, 0f, -0.35f, 0f)
        Matrix.rotateM(leftElbow, 0, -32f, 1f, 0f, 0f)
        drawPart(
            shaders, viewProjMatrix, leftElbow, forearmMesh, textures.whiteSolid,
            tx = 0f, ty = -0.16f, tz = 0f,
            r = 0.62f, g = 0.68f, b = 0.58f, emissive = 0.04f
        )
        // 3 Claws
        for (c in -1..1) {
            drawPart(
                shaders, viewProjMatrix, leftElbow, clawMesh, textures.whiteSolid,
                tx = c * 0.025f, ty = -0.36f, tz = 0.02f,
                rx = 165f, ry = 0f, rz = c * 12f,
                r = 0.16f, g = 0.14f, b = 0.18f, emissive = 0.02f
            )
        }

        // 7. Right Arm + Gnarled Staff & Glowing Crystal Orb
        val rightShoulder = FloatArray(16)
        Matrix.translateM(rightShoulder, 0, upperRoot, 0, 0.29f, 0.50f, 0f)
        val rightArmPitch = when {
            isStunned -> -110f
            isChasing -> -52f + cos(timeSec * 7f) * 10f
            else -> -32f - sin(walkPhase) * 14f
        }
        Matrix.rotateM(rightShoulder, 0, rightArmPitch, 1f, 0f, 0f)
        Matrix.rotateM(rightShoulder, 0, 12f, 0f, 0f, 1f)

        drawPart(
            shaders, viewProjMatrix, rightShoulder, upperArmMesh, textures.witchRobe,
            tx = 0f, ty = -0.18f, tz = 0f,
            r = 0.42f, g = 0.30f, b = 0.50f, emissive = 0.03f
        )
        val rightElbow = FloatArray(16)
        Matrix.translateM(rightElbow, 0, rightShoulder, 0, 0f, -0.35f, 0f)
        Matrix.rotateM(rightElbow, 0, -40f, 1f, 0f, 0f)
        drawPart(
            shaders, viewProjMatrix, rightElbow, forearmMesh, textures.whiteSolid,
            tx = 0f, ty = -0.16f, tz = 0f,
            r = 0.62f, g = 0.68f, b = 0.58f, emissive = 0.04f
        )

        // Magical Staff anchored in right hand
        val staffRoot = FloatArray(16)
        Matrix.translateM(staffRoot, 0, rightElbow, 0, 0.02f, -0.32f, 0.04f)
        Matrix.rotateM(staffRoot, 0, 62f, 1f, 0f, 0f)
        drawPart(
            shaders, viewProjMatrix, staffRoot, staffPoleMesh, textures.whiteSolid,
            tx = 0f, ty = 0.10f, tz = 0f,
            r = 0.28f, g = 0.18f, b = 0.12f, emissive = 0.02f
        )
        drawPart(
            shaders, viewProjMatrix, staffRoot, staffCradleMesh, textures.whiteSolid,
            tx = 0f, ty = 1.04f, tz = 0f,
            r = 0.32f, g = 0.26f, b = 0.20f, emissive = 0.05f
        )
        // Spinning glowing crystal atop staff
        drawPart(
            shaders, viewProjMatrix, staffRoot, staffOrbMesh, textures.whiteSolid,
            tx = 0f, ty = 1.18f, tz = 0f,
            rx = timeSec * 45f, ry = timeSec * 85f, rz = 0f,
            r = eyeR, g = eyeG, b = eyeB,
            emissive = if (isStunned) 0.4f else 0.92f
        )
    }

    private fun drawPart(
        shaders: GameShaders,
        viewProj: FloatArray,
        parent: FloatArray,
        mesh: Mesh,
        textureId: Int,
        tx: Float = 0f, ty: Float = 0f, tz: Float = 0f,
        rx: Float = 0f, ry: Float = 0f, rz: Float = 0f,
        sx: Float = 1f, sy: Float = 1f, sz: Float = 1f,
        r: Float = 1f, g: Float = 1f, b: Float = 1f,
        emissive: Float = 0f
    ) {
        Matrix.translateM(modelMatrix, 0, parent, 0, tx, ty, tz)
        if (ry != 0f) Matrix.rotateM(modelMatrix, 0, ry, 0f, 1f, 0f)
        if (rx != 0f) Matrix.rotateM(modelMatrix, 0, rx, 1f, 0f, 0f)
        if (rz != 0f) Matrix.rotateM(modelMatrix, 0, rz, 0f, 0f, 1f)
        if (sx != 1f || sy != 1f || sz != 1f) Matrix.scaleM(modelMatrix, 0, sx, sy, sz)

        Matrix.multiplyMM(mvpMatrix, 0, viewProj, 0, modelMatrix, 0)

        GLES20.glUniformMatrix4fv(shaders.uModelMatrix, 1, false, modelMatrix, 0)
        GLES20.glUniformMatrix4fv(shaders.uMvpMatrix, 1, false, mvpMatrix, 0)
        GLES20.glUniform4f(shaders.uColorTint, r, g, b, 1f)
        GLES20.glUniform1f(shaders.uEmissive, emissive)

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId)
        mesh.draw(shaders)
    }
}
