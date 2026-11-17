package com.example.engine3d

import android.opengl.GLES20
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class Mesh(vertices: FloatArray) {
    val vertexCount: Int = vertices.size / FLOATS_PER_VERTEX
    private val vertexBuffer: FloatBuffer = ByteBuffer
        .allocateDirect(vertices.size * 4)
        .order(ByteOrder.nativeOrder())
        .asFloatBuffer()
        .apply {
            put(vertices)
            position(0)
        }

    fun draw(shaders: GameShaders) {
        val strideBytes = FLOATS_PER_VERTEX * 4

        vertexBuffer.position(0)
        GLES20.glEnableVertexAttribArray(shaders.aPosition)
        GLES20.glVertexAttribPointer(shaders.aPosition, 3, GLES20.GL_FLOAT, false, strideBytes, vertexBuffer)

        vertexBuffer.position(3)
        GLES20.glEnableVertexAttribArray(shaders.aNormal)
        GLES20.glVertexAttribPointer(shaders.aNormal, 3, GLES20.GL_FLOAT, false, strideBytes, vertexBuffer)

        vertexBuffer.position(6)
        GLES20.glEnableVertexAttribArray(shaders.aTexCoord)
        GLES20.glVertexAttribPointer(shaders.aTexCoord, 2, GLES20.GL_FLOAT, false, strideBytes, vertexBuffer)

        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vertexCount)
    }

    companion object {
        const val FLOATS_PER_VERTEX = 8
    }
}

object MeshBuilder {

    private fun MutableList<Float>.addVertex(
        x: Float, y: Float, z: Float,
        nx: Float, ny: Float, nz: Float,
        u: Float, v: Float
    ) {
        add(x); add(y); add(z)
        add(nx); add(ny); add(nz)
        add(u); add(v)
    }

    fun createBox(w: Float, h: Float, d: Float, tileU: Float = 1f, tileV: Float = 1f): Mesh {
        val hw = w / 2f
        val hh = h / 2f
        val hd = d / 2f
        val list = ArrayList<Float>(36 * 8)

        fun quad(
            x0: Float, y0: Float, z0: Float,
            x1: Float, y1: Float, z1: Float,
            x2: Float, y2: Float, z2: Float,
            x3: Float, y3: Float, z3: Float,
            nx: Float, ny: Float, nz: Float
        ) {
            list.addVertex(x0, y0, z0, nx, ny, nz, 0f, tileV)
            list.addVertex(x1, y1, z1, nx, ny, nz, tileU, tileV)
            list.addVertex(x2, y2, z2, nx, ny, nz, tileU, 0f)
            list.addVertex(x0, y0, z0, nx, ny, nz, 0f, tileV)
            list.addVertex(x2, y2, z2, nx, ny, nz, tileU, 0f)
            list.addVertex(x3, y3, z3, nx, ny, nz, 0f, 0f)
        }

        // Front (+Z)
        quad(-hw, -hh, hd, hw, -hh, hd, hw, hh, hd, -hw, hh, hd, 0f, 0f, 1f)
        // Back (-Z)
        quad(hw, -hh, -hd, -hw, -hh, -hd, -hw, hh, -hd, hw, hh, -hd, 0f, 0f, -1f)
        // Left (-X)
        quad(-hw, -hh, -hd, -hw, -hh, hd, -hw, hh, hd, -hw, hh, -hd, -1f, 0f, 0f)
        // Right (+X)
        quad(hw, -hh, hd, hw, -hh, -hd, hw, hh, -hd, hw, hh, hd, 1f, 0f, 0f)
        // Top (+Y)
        quad(-hw, hh, hd, hw, hh, hd, hw, hh, -hd, -hw, hh, -hd, 0f, 1f, 0f)
        // Bottom (-Y)
        quad(-hw, -hh, -hd, hw, -hh, -hd, hw, -hh, hd, -hw, -hh, hd, 0f, -1f, 0f)

        return Mesh(list.toFloatArray())
    }

    fun createCylinder(
        radiusBottom: Float,
        radiusTop: Float,
        height: Float,
        segments: Int = 16
    ): Mesh {
        val list = ArrayList<Float>()
        val hy = height / 2f
        val slopeY = (radiusBottom - radiusTop) / height.coerceAtLeast(0.001f)

        for (i in 0 until segments) {
            val a0 = (2.0 * PI * i / segments).toFloat()
            val a1 = (2.0 * PI * (i + 1) / segments).toFloat()
            val c0 = cos(a0); val s0 = sin(a0)
            val c1 = cos(a1); val s1 = sin(a1)

            val x0b = c0 * radiusBottom; val z0b = s0 * radiusBottom
            val x1b = c1 * radiusBottom; val z1b = s1 * radiusBottom
            val x0t = c0 * radiusTop; val z0t = s0 * radiusTop
            val x1t = c1 * radiusTop; val z1t = s1 * radiusTop

            val nLen0 = sqrt(c0 * c0 + slopeY * slopeY + s0 * s0)
            val nx0 = c0 / nLen0; val ny0 = slopeY / nLen0; val nz0 = s0 / nLen0
            val nLen1 = sqrt(c1 * c1 + slopeY * slopeY + s1 * s1)
            val nx1 = c1 / nLen1; val ny1 = slopeY / nLen1; val nz1 = s1 / nLen1

            val u0 = i.toFloat() / segments
            val u1 = (i + 1).toFloat() / segments

            // Side quad
            list.addVertex(x0b, -hy, z0b, nx0, ny0, nz0, u0, 1f)
            list.addVertex(x1b, -hy, z1b, nx1, ny1, nz1, u1, 1f)
            list.addVertex(x1t, hy, z1t, nx1, ny1, nz1, u1, 0f)

            list.addVertex(x0b, -hy, z0b, nx0, ny0, nz0, u0, 1f)
            list.addVertex(x1t, hy, z1t, nx1, ny1, nz1, u1, 0f)
            list.addVertex(x0t, hy, z0t, nx0, ny0, nz0, u0, 0f)

            // Bottom cap
            if (radiusBottom > 0.001f) {
                list.addVertex(0f, -hy, 0f, 0f, -1f, 0f, 0.5f, 0.5f)
                list.addVertex(x1b, -hy, z1b, 0f, -1f, 0f, 0.5f + c1 * 0.5f, 0.5f + s1 * 0.5f)
                list.addVertex(x0b, -hy, z0b, 0f, -1f, 0f, 0.5f + c0 * 0.5f, 0.5f + s0 * 0.5f)
            }
            // Top cap
            if (radiusTop > 0.001f) {
                list.addVertex(0f, hy, 0f, 0f, 1f, 0f, 0.5f, 0.5f)
                list.addVertex(x0t, hy, z0t, 0f, 1f, 0f, 0.5f + c0 * 0.5f, 0.5f + s0 * 0.5f)
                list.addVertex(x1t, hy, z1t, 0f, 1f, 0f, 0.5f + c1 * 0.5f, 0.5f + s1 * 0.5f)
            }
        }
        return Mesh(list.toFloatArray())
    }

    fun createSphere(radius: Float, rings: Int = 10, sectors: Int = 14): Mesh {
        val list = ArrayList<Float>()
        for (r in 0 until rings) {
            val v0 = r.toFloat() / rings
            val v1 = (r + 1).toFloat() / rings
            val phi0 = (PI * v0).toFloat()
            val phi1 = (PI * v1).toFloat()

            for (s in 0 until sectors) {
                val u0 = s.toFloat() / sectors
                val u1 = (s + 1).toFloat() / sectors
                val theta0 = (2.0 * PI * u0).toFloat()
                val theta1 = (2.0 * PI * u1).toFloat()

                fun pt(phi: Float, theta: Float): Triple<Float, Float, Float> {
                    val x = sin(phi) * cos(theta)
                    val y = cos(phi)
                    val z = sin(phi) * sin(theta)
                    return Triple(x, y, z)
                }

                val p00 = pt(phi0, theta0)
                val p10 = pt(phi1, theta0)
                val p11 = pt(phi1, theta1)
                val p01 = pt(phi0, theta1)

                list.addVertex(p00.first * radius, p00.second * radius, p00.third * radius, p00.first, p00.second, p00.third, u0, v0)
                list.addVertex(p10.first * radius, p10.second * radius, p10.third * radius, p10.first, p10.second, p10.third, u0, v1)
                list.addVertex(p11.first * radius, p11.second * radius, p11.third * radius, p11.first, p11.second, p11.third, u1, v1)

                list.addVertex(p00.first * radius, p00.second * radius, p00.third * radius, p00.first, p00.second, p00.third, u0, v0)
                list.addVertex(p11.first * radius, p11.second * radius, p11.third * radius, p11.first, p11.second, p11.third, u1, v1)
                list.addVertex(p01.first * radius, p01.second * radius, p01.third * radius, p01.first, p01.second, p01.third, u1, v0)
            }
        }
        return Mesh(list.toFloatArray())
    }

    fun createOctahedron(radius: Float): Mesh {
        val list = ArrayList<Float>()
        val top = floatArrayOf(0f, radius, 0f)
        val bot = floatArrayOf(0f, -radius, 0f)
        val ring = arrayOf(
            floatArrayOf(radius, 0f, 0f),
            floatArrayOf(0f, 0f, radius),
            floatArrayOf(-radius, 0f, 0f),
            floatArrayOf(0f, 0f, -radius)
        )

        fun tri(a: FloatArray, b: FloatArray, c: FloatArray) {
            val ux = b[0] - a[0]; val uy = b[1] - a[1]; val uz = b[2] - a[2]
            val vx = c[0] - a[0]; val vy = c[1] - a[1]; val vz = c[2] - a[2]
            var nx = uy * vz - uz * vy
            var ny = uz * vx - ux * vz
            var nz = ux * vy - uy * vx
            val len = sqrt(nx * nx + ny * ny + nz * nz).coerceAtLeast(0.001f)
            nx /= len; ny /= len; nz /= len
            list.addVertex(a[0], a[1], a[2], nx, ny, nz, 0.5f, 0f)
            list.addVertex(b[0], b[1], b[2], nx, ny, nz, 0f, 1f)
            list.addVertex(c[0], c[1], c[2], nx, ny, nz, 1f, 1f)
        }

        for (i in 0 until 4) {
            val n = (i + 1) % 4
            tri(top, ring[n], ring[i])
            tri(bot, ring[i], ring[n])
        }
        return Mesh(list.toFloatArray())
    }

    fun createPlaneXZ(width: Float, depth: Float, tileU: Float = 1f, tileV: Float = 1f, facingUp: Boolean = true): Mesh {
        val hw = width / 2f
        val hd = depth / 2f
        val ny = if (facingUp) 1f else -1f
        val list = ArrayList<Float>(6 * 8)
        if (facingUp) {
            list.addVertex(-hw, 0f, hd, 0f, ny, 0f, 0f, tileV)
            list.addVertex(hw, 0f, hd, 0f, ny, 0f, tileU, tileV)
            list.addVertex(hw, 0f, -hd, 0f, ny, 0f, tileU, 0f)
            list.addVertex(-hw, 0f, hd, 0f, ny, 0f, 0f, tileV)
            list.addVertex(hw, 0f, -hd, 0f, ny, 0f, tileU, 0f)
            list.addVertex(-hw, 0f, -hd, 0f, ny, 0f, 0f, 0f)
        } else {
            list.addVertex(-hw, 0f, -hd, 0f, ny, 0f, 0f, 0f)
            list.addVertex(hw, 0f, -hd, 0f, ny, 0f, tileU, 0f)
            list.addVertex(hw, 0f, hd, 0f, ny, 0f, tileU, tileV)
            list.addVertex(-hw, 0f, -hd, 0f, ny, 0f, 0f, 0f)
            list.addVertex(hw, 0f, hd, 0f, ny, 0f, tileU, tileV)
            list.addVertex(-hw, 0f, hd, 0f, ny, 0f, 0f, tileV)
        }
        return Mesh(list.toFloatArray())
    }
}
