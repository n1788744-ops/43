package com.example.engine3d

import android.content.Context
import android.opengl.GLSurfaceView

class GameGLSurfaceView(
    context: Context,
    renderer: FirstPersonGameEngine
) : GLSurfaceView(context) {

    init {
        setEGLContextClientVersion(2)
        setRenderer(renderer)
        renderMode = RENDERMODE_CONTINUOUSLY
    }
}
