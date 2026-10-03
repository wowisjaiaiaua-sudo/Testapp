package com.example.game

import androidx.compose.ui.graphics.Color

enum class ObstacleType {
    SPIKE,
    SPIKE_DOWN,
    BLOCK,
    HALF_BLOCK,
    PAD_YELLOW,
    PAD_PURPLE,
    PAD_GRAVITY,
    ORB_YELLOW,
    ORB_PURPLE,
    ORB_GRAVITY,
    PORTAL_SHIP,
    PORTAL_CUBE,
    PORTAL_GRAVITY_DOWN,
    PORTAL_GRAVITY_UP,
    SAW_BLADE,
    COIN,
    FINISH
}

enum class PlayerForm {
    CUBE,
    SHIP
}

data class GameObject(
    val id: Int,
    var x: Float,
    var y: Float, // Grid or world units: 0 is floor, 1 unit = block size (e.g. 50f)
    var width: Float = 1f,
    var height: Float = 1f,
    val type: ObstacleType,
    var rotation: Float = 0f,
    var collected: Boolean = false
) {
    fun copyObj(): GameObject = copy()
}

data class Checkpoint(
    val x: Float,
    val y: Float,
    val vx: Float,
    val vy: Float,
    val rotation: Float,
    val form: PlayerForm,
    val gravityDir: Float,
    val cameraX: Float,
    val percent: Int
)

data class Particle(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    var size: Float,
    val color: Color,
    var alpha: Float = 1f,
    val maxLife: Float = 30f,
    var currentLife: Float = 0f,
    var rotation: Float = 0f,
    var vRot: Float = 0f
) {
    val isDead: Boolean get() = currentLife >= maxLife
}

data class TrailPoint(
    val x: Float,
    val y: Float,
    var alpha: Float = 0.8f
)

data class LevelMetadata(
    val id: String,
    val title: String,
    val difficulty: String,
    val stars: Int,
    val bpm: Int,
    val bgColorHex: String,
    val floorColorHex: String,
    val accentColorHex: String,
    val totalLength: Float
)
