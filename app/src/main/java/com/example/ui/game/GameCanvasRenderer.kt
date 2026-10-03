package com.example.ui.game

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import com.example.data.db.UserStats
import com.example.game.GameEngine
import com.example.game.GameObject
import com.example.game.ObstacleType
import com.example.game.PlayerForm
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

object GameCanvasRenderer {

    fun drawGame(
        drawScope: DrawScope,
        engine: GameEngine,
        userStats: UserStats,
        gameTime: Float
    ) {
        val width = drawScope.size.width
        val height = drawScope.size.height

        // World-to-screen scaling:
        // We want ~14 world units visible horizontally on screen
        val unitPx = width / 14f
        // Floor is located at world Y = 0.
        // Let's place the floor line around 78% from top of screen
        val floorYPx = height * 0.78f

        // Apply screen shake if active
        val shakeX = if (engine.screenShake > 0f) (Random.nextFloat() - 0.5f) * engine.screenShake * 30f else 0f
        val shakeY = if (engine.screenShake > 0f) (Random.nextFloat() - 0.5f) * engine.screenShake * 30f else 0f

        drawScope.translate(left = shakeX, top = shakeY) {
            val cameraX = engine.cameraX

            // 1. Draw Background
            val bgColor = parseColor(engine.levelMeta.bgColorHex, Color(0xFF0D2B45))
            drawRect(color = bgColor, size = Size(width, height))

            // Background Parallax Neon Grid
            val bgGridSize = unitPx * 1.5f
            val bgScroll = (cameraX * unitPx * 0.3f) % bgGridSize
            val bgStrokeColor = Color.White.copy(alpha = 0.05f)

            var gx = -bgScroll
            while (gx < width) {
                drawLine(
                    color = bgStrokeColor,
                    start = Offset(gx, 0f),
                    end = Offset(gx, floorYPx),
                    strokeWidth = 1.5f
                )
                gx += bgGridSize
            }
            var gy = 0f
            while (gy < floorYPx) {
                drawLine(
                    color = bgStrokeColor,
                    start = Offset(0f, gy),
                    end = Offset(width, gy),
                    strokeWidth = 1.5f
                )
                gy += bgGridSize
            }

            // 2. Draw Floor & Scrolling Floor Pattern
            val floorColor = parseColor(engine.levelMeta.floorColorHex, Color(0xFF1B3A4B))
            val accentColor = parseColor(engine.levelMeta.accentColorHex, Color(0xFF00F0FF))

            drawRect(
                color = floorColor,
                topLeft = Offset(0f, floorYPx),
                size = Size(width, height - floorYPx)
            )

            // Bright Floor Neon Boundary Line
            drawLine(
                color = accentColor,
                start = Offset(0f, floorYPx),
                end = Offset(width, floorYPx),
                strokeWidth = 4f
            )

            // Scrolling floor diagonal techno stripes
            val floorStripeWidth = unitPx * 0.8f
            val floorScroll = (cameraX * unitPx) % floorStripeWidth
            var fx = -floorScroll
            while (fx < width + floorStripeWidth) {
                drawLine(
                    color = accentColor.copy(alpha = 0.25f),
                    start = Offset(fx, floorYPx),
                    end = Offset(fx - floorStripeWidth * 0.6f, height),
                    strokeWidth = 2.5f
                )
                fx += floorStripeWidth
            }

            // Helper lambda for coordinate transform
            fun worldToScreenX(wx: Float): Float = (wx - cameraX) * unitPx
            fun worldToScreenY(wy: Float): Float = floorYPx - (wy * unitPx)

            // 3. Draw Checkpoints in Practice Mode
            for (cp in engine.checkpoints) {
                val cx = worldToScreenX(cp.x + 0.5f)
                val cy = worldToScreenY(cp.y + 0.5f)
                if (cx in -unitPx..(width + unitPx)) {
                    drawPracticeDiamond(this, cx, cy, unitPx * 0.5f)
                }
            }

            // 4. Draw Game Objects
            for (obj in engine.objects) {
                val ox = worldToScreenX(obj.x)
                val oy = worldToScreenY(obj.y)

                // Culling: only draw objects visible on screen
                if (ox + (obj.width * unitPx) < -unitPx || ox > width + unitPx) continue

                drawGameObject(this, obj, ox, oy, unitPx, accentColor, gameTime)
            }

            // 5. Draw Player Trail
            val primaryColor = parseColor(userStats.primaryColorHex, Color(0xFF00FF66))
            val secondaryColor = parseColor(userStats.secondaryColorHex, Color(0xFFFFCC00))

            for (i in 0 until engine.trail.size - 1) {
                val p1 = engine.trail[i]
                val p2 = engine.trail[i + 1]
                val x1 = worldToScreenX(p1.x)
                val y1 = worldToScreenY(p1.y)
                val x2 = worldToScreenX(p2.x)
                val y2 = worldToScreenY(p2.y)
                drawLine(
                    color = primaryColor.copy(alpha = p1.alpha * 0.6f),
                    start = Offset(x1, y1),
                    end = Offset(x2, y2),
                    strokeWidth = unitPx * 0.35f * p1.alpha
                )
            }

            // 6. Draw Player (Cube or Ship)
            if (!engine.isDead) {
                val px = worldToScreenX(engine.playerX)
                val py = worldToScreenY(engine.playerY + engine.playerSize) // top-left in screen coords
                val pSizePx = engine.playerSize * unitPx

                val centerPx = Offset(px + pSizePx / 2f, py + pSizePx / 2f)

                rotate(degrees = engine.playerRotation, pivot = centerPx) {
                    if (engine.playerForm == PlayerForm.CUBE) {
                        drawPlayerCube(
                            this,
                            px,
                            py,
                            pSizePx,
                            primaryColor,
                            secondaryColor,
                            userStats.selectedCubeIndex
                        )
                    } else {
                        drawPlayerShip(
                            this,
                            px,
                            py,
                            pSizePx,
                            primaryColor,
                            secondaryColor,
                            userStats.selectedShipIndex
                        )
                    }
                }
            }

            // 7. Draw Particles
            for (p in engine.particles) {
                val px = worldToScreenX(p.x)
                val py = worldToScreenY(p.y)
                val pSize = p.size * unitPx
                rotate(degrees = p.rotation, pivot = Offset(px, py)) {
                    drawRect(
                        color = p.color.copy(alpha = p.alpha.coerceIn(0f, 1f)),
                        topLeft = Offset(px - pSize / 2f, py - pSize / 2f),
                        size = Size(pSize, pSize)
                    )
                }
            }
        }
    }

    fun drawGameObject(
        drawScope: DrawScope,
        obj: GameObject,
        ox: Float,
        oy: Float, // This is at world Y, which corresponds to bottom of the block
        unitPx: Float,
        accentColor: Color,
        gameTime: Float
    ) {
        val topY = oy - (obj.height * unitPx)
        val objWidthPx = obj.width * unitPx
        val objHeightPx = obj.height * unitPx

        when (obj.type) {
            ObstacleType.SPIKE -> {
                val path = Path().apply {
                    moveTo(ox, oy)
                    lineTo(ox + objWidthPx / 2f, topY)
                    lineTo(ox + objWidthPx, oy)
                    close()
                }
                // Black base with neon outline
                drawScope.drawPath(path, color = Color(0xFF101018), style = Fill)
                drawScope.drawPath(path, color = Color(0xFFFF2A2A), style = Stroke(width = 3.5f))

                // Inner smaller triangle
                val innerPath = Path().apply {
                    moveTo(ox + objWidthPx * 0.25f, oy - 2f)
                    lineTo(ox + objWidthPx / 2f, topY + objHeightPx * 0.35f)
                    lineTo(ox + objWidthPx * 0.75f, oy - 2f)
                    close()
                }
                drawScope.drawPath(innerPath, color = Color(0xFFFF6B6B), style = Fill)
            }

            ObstacleType.SPIKE_DOWN -> {
                val path = Path().apply {
                    moveTo(ox, topY)
                    lineTo(ox + objWidthPx / 2f, oy)
                    lineTo(ox + objWidthPx, topY)
                    close()
                }
                drawScope.drawPath(path, color = Color(0xFF101018), style = Fill)
                drawScope.drawPath(path, color = Color(0xFFFF2A2A), style = Stroke(width = 3.5f))
            }

            ObstacleType.BLOCK -> {
                // Neon block with cyber grid detail
                drawScope.drawRect(
                    color = Color(0xFF121829),
                    topLeft = Offset(ox, topY),
                    size = Size(objWidthPx, objHeightPx)
                )
                drawScope.drawRect(
                    color = accentColor,
                    topLeft = Offset(ox, topY),
                    size = Size(objWidthPx, objHeightPx),
                    style = Stroke(width = 3.5f)
                )
                // Inner concentric square
                val pad = objWidthPx * 0.22f
                drawScope.drawRect(
                    color = accentColor.copy(alpha = 0.5f),
                    topLeft = Offset(ox + pad, topY + pad),
                    size = Size(objWidthPx - pad * 2, objHeightPx - pad * 2),
                    style = Stroke(width = 2f)
                )
            }

            ObstacleType.HALF_BLOCK -> {
                drawScope.drawRect(
                    color = Color(0xFF121829),
                    topLeft = Offset(ox, topY),
                    size = Size(objWidthPx, objHeightPx)
                )
                drawScope.drawRect(
                    color = accentColor,
                    topLeft = Offset(ox, topY),
                    size = Size(objWidthPx, objHeightPx),
                    style = Stroke(width = 3f)
                )
            }

            ObstacleType.PAD_YELLOW -> {
                val padH = unitPx * 0.25f
                val py = oy - padH
                drawScope.drawRect(
                    color = Color(0xFFFFD700),
                    topLeft = Offset(ox + unitPx * 0.1f, py),
                    size = Size(unitPx * 0.8f, padH)
                )
                // Chevron up
                val chPath = Path().apply {
                    moveTo(ox + unitPx * 0.25f, py + padH * 0.75f)
                    lineTo(ox + unitPx * 0.5f, py + padH * 0.2f)
                    lineTo(ox + unitPx * 0.75f, py + padH * 0.75f)
                }
                drawScope.drawPath(chPath, color = Color.White, style = Stroke(width = 2.5f))
            }

            ObstacleType.PAD_PURPLE -> {
                val padH = unitPx * 0.25f
                val py = oy - padH
                drawScope.drawRect(
                    color = Color(0xFFD000FF),
                    topLeft = Offset(ox + unitPx * 0.1f, py),
                    size = Size(unitPx * 0.8f, padH)
                )
            }

            ObstacleType.PAD_GRAVITY -> {
                val padH = unitPx * 0.25f
                val py = oy - padH
                drawScope.drawRect(
                    color = Color(0xFF00E5FF),
                    topLeft = Offset(ox + unitPx * 0.1f, py),
                    size = Size(unitPx * 0.8f, padH)
                )
            }

            ObstacleType.ORB_YELLOW -> {
                val cx = ox + unitPx * 0.5f
                val cy = oy - unitPx * 0.5f
                val r = unitPx * 0.42f
                val pulse = (sin(gameTime * 6f) * 0.08f + 1f) * r

                drawScope.drawCircle(color = Color(0xFFFFD700).copy(alpha = 0.25f), radius = pulse * 1.25f, center = Offset(cx, cy))
                drawScope.drawCircle(color = Color(0xFFFFD700), radius = r, center = Offset(cx, cy), style = Stroke(width = 4f))
                drawScope.drawCircle(color = Color(0xFFFFEA00), radius = r * 0.55f, center = Offset(cx, cy), style = Fill)
            }

            ObstacleType.ORB_PURPLE -> {
                val cx = ox + unitPx * 0.5f
                val cy = oy - unitPx * 0.5f
                val r = unitPx * 0.42f
                drawScope.drawCircle(color = Color(0xFFD000FF).copy(alpha = 0.25f), radius = r * 1.2f, center = Offset(cx, cy))
                drawScope.drawCircle(color = Color(0xFFD000FF), radius = r, center = Offset(cx, cy), style = Stroke(width = 4f))
                drawScope.drawCircle(color = Color(0xFFE040FB), radius = r * 0.55f, center = Offset(cx, cy), style = Fill)
            }

            ObstacleType.ORB_GRAVITY -> {
                val cx = ox + unitPx * 0.5f
                val cy = oy - unitPx * 0.5f
                val r = unitPx * 0.42f
                drawScope.drawCircle(color = Color(0xFF00E5FF).copy(alpha = 0.25f), radius = r * 1.2f, center = Offset(cx, cy))
                drawScope.drawCircle(color = Color(0xFF00E5FF), radius = r, center = Offset(cx, cy), style = Stroke(width = 4f))
                drawScope.drawCircle(color = Color(0xFF18FFFF), radius = r * 0.55f, center = Offset(cx, cy), style = Fill)
            }

            ObstacleType.SAW_BLADE -> {
                val cx = ox + unitPx * 0.5f
                val cy = oy - unitPx * 0.5f
                val r = unitPx * 0.45f
                val rot = (gameTime * 360f * 2.5f) % 360f

                drawScope.rotate(degrees = rot, pivot = Offset(cx, cy)) {
                    val teeth = 8
                    val sawPath = Path()
                    for (i in 0 until teeth * 2) {
                        val angle = (i * PI / teeth).toFloat()
                        val currentR = if (i % 2 == 0) r else r * 0.65f
                        val sx = cx + cos(angle) * currentR
                        val sy = cy + sin(angle) * currentR
                        if (i == 0) sawPath.moveTo(sx, sy) else sawPath.lineTo(sx, sy)
                    }
                    sawPath.close()
                    drawScope.drawPath(sawPath, color = Color(0xFF181822), style = Fill)
                    drawScope.drawPath(sawPath, color = Color(0xFFFF1744), style = Stroke(width = 3.5f))
                    drawScope.drawCircle(color = Color(0xFFFF5252), radius = r * 0.28f, center = Offset(cx, cy))
                }
            }

            ObstacleType.PORTAL_SHIP -> {
                val cx = ox + unitPx * 0.5f
                val cy = topY + objHeightPx * 0.5f
                drawScope.drawRoundRect(
                    color = Color(0xFFFF007F),
                    topLeft = Offset(ox + unitPx * 0.2f, topY),
                    size = Size(unitPx * 0.6f, objHeightPx),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(unitPx * 0.3f, unitPx * 0.3f),
                    style = Stroke(width = 5f)
                )
                drawScope.drawRoundRect(
                    color = Color(0xFFFF007F).copy(alpha = 0.3f),
                    topLeft = Offset(ox + unitPx * 0.25f, topY + 4f),
                    size = Size(unitPx * 0.5f, objHeightPx - 8f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(unitPx * 0.25f, unitPx * 0.25f),
                    style = Fill
                )
            }

            ObstacleType.PORTAL_CUBE -> {
                drawScope.drawRoundRect(
                    color = Color(0xFF00FF66),
                    topLeft = Offset(ox + unitPx * 0.2f, topY),
                    size = Size(unitPx * 0.6f, objHeightPx),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(unitPx * 0.3f, unitPx * 0.3f),
                    style = Stroke(width = 5f)
                )
            }

            ObstacleType.PORTAL_GRAVITY_DOWN, ObstacleType.PORTAL_GRAVITY_UP -> {
                drawScope.drawRoundRect(
                    color = Color(0xFF00E5FF),
                    topLeft = Offset(ox + unitPx * 0.2f, topY),
                    size = Size(unitPx * 0.6f, objHeightPx),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(unitPx * 0.3f, unitPx * 0.3f),
                    style = Stroke(width = 5f)
                )
            }

            ObstacleType.COIN -> {
                if (!obj.collected) {
                    val cx = ox + unitPx * 0.5f
                    val cy = oy - unitPx * 0.5f
                    val coinScaleX = abs(cos(gameTime * 4f)) // Rotating effect
                    val r = unitPx * 0.38f

                    drawScope.drawOval(
                        color = Color(0xFFFFD700),
                        topLeft = Offset(cx - r * coinScaleX, cy - r),
                        size = Size(r * 2f * coinScaleX, r * 2f)
                    )
                    drawScope.drawOval(
                        color = Color(0xFFFFF176),
                        topLeft = Offset(cx - r * 0.7f * coinScaleX, cy - r * 0.7f),
                        size = Size(r * 1.4f * coinScaleX, r * 1.4f),
                        style = Stroke(width = 2.5f)
                    )
                }
            }

            ObstacleType.FINISH -> {
                // Grand glowing Finish Gate
                val finishColor = Color(0xFF00FF66)
                drawScope.drawRect(
                    color = finishColor,
                    topLeft = Offset(ox, topY),
                    size = Size(unitPx * 0.4f, objHeightPx)
                )
                // Animated glowing wave lines
                val waveH = unitPx * 0.6f
                for (i in 0..6) {
                    val wy = topY + (i * waveH)
                    val offsetW = sin(gameTime * 8f + i) * unitPx * 0.3f
                    drawScope.drawLine(
                        color = Color.White,
                        start = Offset(ox + unitPx * 0.2f, wy),
                        end = Offset(ox + unitPx * 1.2f + offsetW, wy),
                        strokeWidth = 3f
                    )
                }
            }
        }
    }

    private fun drawPracticeDiamond(drawScope: DrawScope, cx: Float, cy: Float, size: Float) {
        val path = Path().apply {
            moveTo(cx, cy - size)
            lineTo(cx + size * 0.75f, cy)
            lineTo(cx, cy + size)
            lineTo(cx - size * 0.75f, cy)
            close()
        }
        drawScope.drawPath(path, color = Color(0xFF00FF66), style = Fill)
        drawScope.drawPath(path, color = Color.White, style = Stroke(width = 2.5f))
    }

    fun drawPlayerCube(
        drawScope: DrawScope,
        px: Float,
        py: Float,
        size: Float,
        primaryColor: Color,
        secondaryColor: Color,
        skinIndex: Int
    ) {
        // Base Square
        drawScope.drawRect(
            color = primaryColor,
            topLeft = Offset(px, py),
            size = Size(size, size)
        )
        // Secondary Outline
        drawScope.drawRect(
            color = secondaryColor,
            topLeft = Offset(px, py),
            size = Size(size, size),
            style = Stroke(width = size * 0.12f)
        )

        // Internal Iconic Geometry Dash Face styles
        when (skinIndex % 6) {
            0 -> {
                // Classic Expressive Eyes & Mouth
                val eyeW = size * 0.18f
                val eyeH = size * 0.25f
                val eyeY = py + size * 0.28f
                // Left eye
                drawScope.drawRect(color = secondaryColor, topLeft = Offset(px + size * 0.22f, eyeY), size = Size(eyeW, eyeH))
                // Right eye
                drawScope.drawRect(color = secondaryColor, topLeft = Offset(px + size * 0.60f, eyeY), size = Size(eyeW, eyeH))
                // Gritted Mouth
                drawScope.drawRect(
                    color = secondaryColor,
                    topLeft = Offset(px + size * 0.25f, py + size * 0.65f),
                    size = Size(size * 0.5f, size * 0.12f)
                )
            }
            1 -> {
                // Ninja Headband & Fierce Slanted Eyes
                drawScope.drawRect(
                    color = secondaryColor,
                    topLeft = Offset(px + size * 0.1f, py + size * 0.2f),
                    size = Size(size * 0.8f, size * 0.18f)
                )
                // Left slanted eye
                val leftEye = Path().apply {
                    moveTo(px + size * 0.22f, py + size * 0.48f)
                    lineTo(px + size * 0.42f, py + size * 0.42f)
                    lineTo(px + size * 0.38f, py + size * 0.58f)
                    close()
                }
                drawScope.drawPath(leftEye, color = secondaryColor)
                // Right slanted eye
                val rightEye = Path().apply {
                    moveTo(px + size * 0.78f, py + size * 0.48f)
                    lineTo(px + size * 0.58f, py + size * 0.42f)
                    lineTo(px + size * 0.62f, py + size * 0.58f)
                    close()
                }
                drawScope.drawPath(rightEye, color = secondaryColor)
            }
            2 -> {
                // Robot Cyclops Visor
                drawScope.drawRoundRect(
                    color = secondaryColor,
                    topLeft = Offset(px + size * 0.15f, py + size * 0.35f),
                    size = Size(size * 0.7f, size * 0.28f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(size * 0.1f, size * 0.1f)
                )
                drawScope.drawCircle(
                    color = Color.White,
                    radius = size * 0.1f,
                    center = Offset(px + size * 0.5f, py + size * 0.49f)
                )
            }
            3 -> {
                // Creeper / 8-Bit Pixel Face
                drawScope.drawRect(color = secondaryColor, topLeft = Offset(px + size * 0.22f, py + size * 0.25f), size = Size(size * 0.18f, size * 0.18f))
                drawScope.drawRect(color = secondaryColor, topLeft = Offset(px + size * 0.60f, py + size * 0.25f), size = Size(size * 0.18f, size * 0.18f))
                drawScope.drawRect(color = secondaryColor, topLeft = Offset(px + size * 0.41f, py + size * 0.43f), size = Size(size * 0.18f, size * 0.28f))
                drawScope.drawRect(color = secondaryColor, topLeft = Offset(px + size * 0.30f, py + size * 0.60f), size = Size(size * 0.40f, size * 0.16f))
            }
            4 -> {
                // Star Core
                val starPath = Path().apply {
                    val cx = px + size * 0.5f
                    val cy = py + size * 0.5f
                    val rOut = size * 0.35f
                    val rIn = size * 0.15f
                    for (i in 0 until 8) {
                        val angle = (i * PI / 4).toFloat()
                        val r = if (i % 2 == 0) rOut else rIn
                        val sx = cx + cos(angle) * r
                        val sy = cy + sin(angle) * r
                        if (i == 0) moveTo(sx, sy) else lineTo(sx, sy)
                    }
                    close()
                }
                drawScope.drawPath(starPath, color = secondaryColor)
            }
            else -> {
                // Cyber Diamond Insignia
                val diamond = Path().apply {
                    val cx = px + size * 0.5f
                    val cy = py + size * 0.5f
                    moveTo(cx, cy - size * 0.3f)
                    lineTo(cx + size * 0.3f, cy)
                    lineTo(cx, cy + size * 0.3f)
                    lineTo(cx - size * 0.3f, cy)
                    close()
                }
                drawScope.drawPath(diamond, color = secondaryColor)
                drawScope.drawCircle(color = primaryColor, radius = size * 0.12f, center = Offset(px + size * 0.5f, py + size * 0.5f))
            }
        }
    }

    fun drawPlayerShip(
        drawScope: DrawScope,
        px: Float,
        py: Float,
        size: Float,
        primaryColor: Color,
        secondaryColor: Color,
        skinIndex: Int
    ) {
        val cx = px + size * 0.5f
        val cy = py + size * 0.5f

        // Sleek Arrow Ship Body
        val shipPath = Path().apply {
            moveTo(px + size * 0.95f, cy) // Nose pointing right
            lineTo(px + size * 0.15f, py + size * 0.15f) // Top wing
            lineTo(px + size * 0.28f, cy) // Engine indent
            lineTo(px + size * 0.15f, py + size * 0.85f) // Bottom wing
            close()
        }
        drawScope.drawPath(shipPath, color = primaryColor, style = Fill)
        drawScope.drawPath(shipPath, color = secondaryColor, style = Stroke(width = size * 0.1f))

        // Cockpit Window
        drawScope.drawRoundRect(
            color = Color(0xFF00F0FF),
            topLeft = Offset(px + size * 0.45f, cy - size * 0.16f),
            size = Size(size * 0.32f, size * 0.32f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(size * 0.1f, size * 0.1f)
        )

        // Engine Fire Thruster
        val firePath = Path().apply {
            moveTo(px + size * 0.25f, cy - size * 0.14f)
            lineTo(px - size * 0.15f, cy)
            lineTo(px + size * 0.25f, cy + size * 0.14f)
            close()
        }
        drawScope.drawPath(firePath, color = Color(0xFFFF5722), style = Fill)
        drawScope.drawPath(firePath, color = Color(0xFFFFEB3B), style = Stroke(width = 2f))
    }

    private fun parseColor(hex: String, fallback: Color): Color {
        return try {
            Color(android.graphics.Color.parseColor(hex))
        } catch (_: Exception) {
            fallback
        }
    }
}
