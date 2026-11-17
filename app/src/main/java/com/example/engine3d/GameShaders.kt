package com.example.engine3d

import android.opengl.GLES20

class GameShaders {

    val programId: Int

    val uMvpMatrix: Int
    val uModelMatrix: Int
    val uTexture: Int
    val uColorTint: Int
    val uEmissive: Int
    val uCameraPos: Int
    val uCameraDir: Int
    val uFlashlightOn: Int
    val uAmbientLight: Int
    val uFogColor: Int
    val uFogDensity: Int
    val uWitchPos: Int
    val uWitchAuraColor: Int
    val uTorchPos0: Int
    val uTorchPos1: Int
    val uTorchColor: Int

    val aPosition: Int
    val aNormal: Int
    val aTexCoord: Int

    init {
        val vertexSrc = """
            uniform mat4 uMvpMatrix;
            uniform mat4 uModelMatrix;

            attribute vec4 aPosition;
            attribute vec3 aNormal;
            attribute vec2 aTexCoord;

            varying vec3 vWorldPos;
            varying vec3 vNormal;
            varying vec2 vTexCoord;

            void main() {
                vec4 worldPos = uModelMatrix * aPosition;
                vWorldPos = worldPos.xyz;
                vNormal = normalize((uModelMatrix * vec4(aNormal, 0.0)).xyz);
                vTexCoord = aTexCoord;
                gl_Position = uMvpMatrix * aPosition;
            }
        """.trimIndent()

        val fragmentSrc = """
            precision mediump float;

            uniform sampler2D uTexture;
            uniform vec4 uColorTint;
            uniform float uEmissive;

            uniform vec3 uCameraPos;
            uniform vec3 uCameraDir;
            uniform float uFlashlightOn;

            uniform vec3 uAmbientLight;
            uniform vec3 uFogColor;
            uniform float uFogDensity;

            uniform vec3 uWitchPos;
            uniform vec3 uWitchAuraColor;

            uniform vec3 uTorchPos0;
            uniform vec3 uTorchPos1;
            uniform vec3 uTorchColor;

            varying vec3 vWorldPos;
            varying vec3 vNormal;
            varying vec2 vTexCoord;

            vec3 computePointLight(vec3 lightPos, vec3 lightColor, float range) {
                vec3 toLight = lightPos - vWorldPos;
                float dist = length(toLight);
                if (dist > range) return vec3(0.0);
                vec3 dir = toLight / max(dist, 0.001);
                float ndotl = max(dot(vNormal, dir), 0.12);
                float att = clamp(1.0 - dist / range, 0.0, 1.0);
                return lightColor * ndotl * att * att;
            }

            void main() {
                vec4 tex = texture2D(uTexture, vTexCoord) * uColorTint;
                vec3 N = normalize(vNormal);

                vec3 totalLight = uAmbientLight;

                // Player flashlight spotlight + soft close lantern fill
                vec3 toCam = uCameraPos - vWorldPos;
                float camDist = length(toCam);
                vec3 dirFromCam = -toCam / max(camDist, 0.001);

                // Close dim lantern warmth around Nastya even if flashlight is off
                float closeGlow = clamp(1.0 - camDist / 3.8, 0.0, 1.0);
                totalLight += vec3(0.32, 0.24, 0.16) * closeGlow * closeGlow;

                if (uFlashlightOn > 0.01) {
                    float spotCos = dot(dirFromCam, normalize(uCameraDir));
                    float spot = smoothstep(0.76, 0.94, spotCos);
                    float rangeAtt = clamp(1.0 - camDist / 16.5, 0.0, 1.0);
                    float ndotl = max(dot(N, -dirFromCam), 0.18);
                    totalLight += vec3(1.05, 0.94, 0.78) * uFlashlightOn * spot * rangeAtt * rangeAtt * ndotl;
                }

                // Witch eerie point aura
                totalLight += computePointLight(uWitchPos, uWitchAuraColor * 1.35, 9.0);

                // Nearest two flickering wall torches / crystals
                totalLight += computePointLight(uTorchPos0, uTorchColor * 1.25, 10.5);
                totalLight += computePointLight(uTorchPos1, uTorchColor * 1.25, 10.5);

                vec3 litColor = tex.rgb * (totalLight + vec3(uEmissive));

                // Exponential horror fog
                float fogFactor = 1.0 - exp(-pow(camDist * uFogDensity, 1.65));
                fogFactor = clamp(fogFactor, 0.0, 1.0);
                vec3 finalRgb = mix(litColor, uFogColor, fogFactor * (1.0 - clamp(uEmissive * 0.75, 0.0, 0.85)));

                gl_FragColor = vec4(finalRgb, tex.a);
            }
        """.trimIndent()

        val vShader = compileShader(GLES20.GL_VERTEX_SHADER, vertexSrc)
        val fShader = compileShader(GLES20.GL_FRAGMENT_SHADER, fragmentSrc)

        programId = GLES20.glCreateProgram().also { prog ->
            GLES20.glAttachShader(prog, vShader)
            GLES20.glAttachShader(prog, fShader)
            GLES20.glLinkProgram(prog)
        }

        uMvpMatrix = GLES20.glGetUniformLocation(programId, "uMvpMatrix")
        uModelMatrix = GLES20.glGetUniformLocation(programId, "uModelMatrix")
        uTexture = GLES20.glGetUniformLocation(programId, "uTexture")
        uColorTint = GLES20.glGetUniformLocation(programId, "uColorTint")
        uEmissive = GLES20.glGetUniformLocation(programId, "uEmissive")
        uCameraPos = GLES20.glGetUniformLocation(programId, "uCameraPos")
        uCameraDir = GLES20.glGetUniformLocation(programId, "uCameraDir")
        uFlashlightOn = GLES20.glGetUniformLocation(programId, "uFlashlightOn")
        uAmbientLight = GLES20.glGetUniformLocation(programId, "uAmbientLight")
        uFogColor = GLES20.glGetUniformLocation(programId, "uFogColor")
        uFogDensity = GLES20.glGetUniformLocation(programId, "uFogDensity")
        uWitchPos = GLES20.glGetUniformLocation(programId, "uWitchPos")
        uWitchAuraColor = GLES20.glGetUniformLocation(programId, "uWitchAuraColor")
        uTorchPos0 = GLES20.glGetUniformLocation(programId, "uTorchPos0")
        uTorchPos1 = GLES20.glGetUniformLocation(programId, "uTorchPos1")
        uTorchColor = GLES20.glGetUniformLocation(programId, "uTorchColor")

        aPosition = GLES20.glGetAttribLocation(programId, "aPosition")
        aNormal = GLES20.glGetAttribLocation(programId, "aNormal")
        aTexCoord = GLES20.glGetAttribLocation(programId, "aTexCoord")
    }

    private fun compileShader(type: Int, code: String): Int {
        val shader = GLES20.glCreateShader(type)
        GLES20.glShaderSource(shader, code)
        GLES20.glCompileShader(shader)
        return shader
    }
}
