package com.example.game

import androidx.compose.ui.graphics.Color
import com.example.audio.SoundEngine
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.random.Random

class GameEngine(
    val levelMeta: LevelMetadata,
    initialObjects: List<GameObject>,
    val soundEngine: SoundEngine,
    val isPractice: Boolean,
    var onLevelComplete: (attempts: Int, coinsMask: Int, jumps: Int) -> Unit = { _, _, _ -> },
    var onCrash: (percent: Int, attempts: Int) -> Unit = { _, _ -> }
) {
    var objects: List<GameObject> = initialObjects.map { it.copyObj() }

    // Player State
    var playerX: Float = 0f
    var playerY: Float = 0f
    var playerVx: Float = 9.2f // Forward speed in units/sec
    var playerVy: Float = 0f
    var playerRotation: Float = 0f
    var playerForm: PlayerForm = PlayerForm.CUBE
    var gravityDir: Float = 1f // 1f = normal down, -1f = inverted ceiling
    var onGround: Boolean = true
    var isDead: Boolean = false
    var isWin: Boolean = false
    var isHoldingJump: Boolean = false

    // Camera & Screen Shake
    var cameraX: Float = 0f
    var screenShake: Float = 0f

    // Stats
    var attemptCount: Int = 1
    var jumpCount: Int = 0
    var sessionCoinsMask: Int = 0
    var progressPercent: Int = 0

    // Practice mode checkpoints
    val checkpoints = mutableListOf<Checkpoint>()

    // Visual particles & trail
    val particles = mutableListOf<Particle>()
    val trail = mutableListOf<TrailPoint>()

    // Timing
    private var deathTimer: Float = 0f
    private val respawnDelay: Float = 0.75f // Fast retry loop

    val playerSize: Float = 1.0f

    init {
        resetPlayer(soft = false)
        soundEngine.startBgm(levelMeta.bpm)
    }

    fun setHolding(holding: Boolean) {
        val wasHolding = isHoldingJump
        isHoldingJump = holding

        if (holding && !wasHolding && !isDead && !isWin) {
            handleTap()
        }
    }

    private fun handleTap() {
        jumpCount++

        // Check for Jump Orbs interaction first
        val nearbyOrb = objects.firstOrNull { obj ->
            !obj.collected && (obj.type == ObstacleType.ORB_YELLOW || obj.type == ObstacleType.ORB_PURPLE || obj.type == ObstacleType.ORB_GRAVITY) &&
                    abs(obj.x + 0.5f - (playerX + 0.5f)) < 1.3f &&
                    abs(obj.y + 0.5f - (playerY + 0.5f)) < 1.4f
        }

        if (nearbyOrb != null) {
            when (nearbyOrb.type) {
                ObstacleType.ORB_YELLOW -> {
                    playerVy = 18.5f * gravityDir
                    onGround = false
                    soundEngine.playOrb()
                    spawnOrbRingParticles(nearbyOrb.x + 0.5f, nearbyOrb.y + 0.5f, Color(0xFFFFD700))
                }
                ObstacleType.ORB_PURPLE -> {
                    playerVy = 13.5f * gravityDir
                    onGround = false
                    soundEngine.playOrb()
                    spawnOrbRingParticles(nearbyOrb.x + 0.5f, nearbyOrb.y + 0.5f, Color(0xFFD000FF))
                }
                ObstacleType.ORB_GRAVITY -> {
                    gravityDir = -gravityDir
                    playerVy = 7.0f * gravityDir
                    onGround = false
                    soundEngine.playGravityFlip()
                    spawnOrbRingParticles(nearbyOrb.x + 0.5f, nearbyOrb.y + 0.5f, Color(0xFF00E5FF))
                }
                else -> {}
            }
            return
        }

        // Standard Cube Jump
        if (playerForm == PlayerForm.CUBE && onGround) {
            playerVy = 18.0f * gravityDir
            onGround = false
            soundEngine.playJump()
            spawnJumpDust(playerX + 0.5f, if (gravityDir > 0) playerY else playerY + playerSize)
        }
    }

    fun update(dt: Float) {
        val clampedDt = dt.coerceIn(0.001f, 0.05f)

        // Screen shake decay
        if (screenShake > 0f) {
            screenShake = (screenShake - clampedDt * 30f).coerceAtLeast(0f)
        }

        // Particle updates
        updateParticles(clampedDt)

        if (isDead) {
            deathTimer += clampedDt
            if (deathTimer >= respawnDelay) {
                respawn()
            }
            return
        }

        if (isWin) {
            return
        }

        // Update trail
        trail.add(0, TrailPoint(playerX + 0.5f, playerY + 0.5f))
        if (trail.size > 14) trail.removeAt(trail.size - 1)
        for (tp in trail) {
            tp.alpha = (tp.alpha - clampedDt * 2.5f).coerceAtLeast(0f)
        }

        // 1. Horizontal movement
        playerX += playerVx * clampedDt
        cameraX = playerX - 3.2f // Keep player slightly left of center for widescreen anticipation

        // Calculate progress percentage
        val pct = ((playerX / levelMeta.totalLength) * 100f).toInt().coerceIn(0, 100)
        progressPercent = maxOf(progressPercent, pct)

        // 2. Vertical movement & physics by form
        if (playerForm == PlayerForm.CUBE) {
            val gravityAcc = -60.0f * gravityDir
            playerVy += gravityAcc * clampedDt
            playerY += playerVy * clampedDt

            // Rotation in air
            if (!onGround) {
                val rotSpeed = 480f * gravityDir
                playerRotation += rotSpeed * clampedDt
            } else {
                // Snap to nearest 90 degree when on ground
                val targetRot = (playerRotation / 90f).roundToInt() * 90f
                playerRotation = playerRotation + (targetRot - playerRotation) * 0.4f
            }
        } else if (playerForm == PlayerForm.SHIP) {
            // Ship physics
            val thrust = if (isHoldingJump) 42f * gravityDir else -32f * gravityDir
            playerVy = (playerVy + thrust * clampedDt).coerceIn(-14f, 14f)
            playerY += playerVy * clampedDt

            // Ship pitch tilting based on vertical speed
            val targetTilt = (playerVy * 2.5f).coerceIn(-35f, 35f) * gravityDir
            playerRotation = playerRotation + (targetTilt - playerRotation) * 0.25f

            // Ship exhaust particles
            if (Random.nextFloat() < 0.7f) {
                particles.add(
                    Particle(
                        x = playerX,
                        y = playerY + 0.5f,
                        vx = -Random.nextFloat() * 4f - 2f,
                        vy = (Random.nextFloat() - 0.5f) * 2f,
                        size = Random.nextFloat() * 0.25f + 0.1f,
                        color = Color(0xFFFF5722),
                        maxLife = 12f
                    )
                )
            }
        }

        // Check floor and ceiling boundaries
        if (gravityDir > 0) {
            if (playerY <= 0f) {
                playerY = 0f
                playerVy = 0f
                onGround = true
                if (isHoldingJump && playerForm == PlayerForm.CUBE) {
                    handleTap()
                }
            }
        } else {
            // Inverted gravity floor is at y = 6f or ceiling
            if (playerY >= 6.5f) {
                playerY = 6.5f
                playerVy = 0f
                onGround = true
                if (isHoldingJump && playerForm == PlayerForm.CUBE) {
                    handleTap()
                }
            }
        }

        // In Ship mode, clamp between 0f floor and 7.5f ceiling
        if (playerForm == PlayerForm.SHIP) {
            if (playerY < 0.1f) {
                playerY = 0.1f
                playerVy = 0f
            }
            if (playerY > 7.5f) {
                playerY = 7.5f
                playerVy = 0f
            }
        }

        // 3. Collision Detection with Game Objects
        checkCollisions()

        // 4. Check for Win condition
        if (playerX >= levelMeta.totalLength) {
            triggerWin()
        }
    }

    private fun checkCollisions() {
        var landedOnSolid = false

        // Check active objects within viewing range
        val activeObjects = objects.filter { abs(it.x - playerX) < 4f }

        for (obj in activeObjects) {
            when (obj.type) {
                ObstacleType.SPIKE -> {
                    // Forgiving triangle hitbox
                    val hitboxMarginX = 0.25f
                    val spikeLeft = obj.x + hitboxMarginX
                    val spikeRight = obj.x + 1f - hitboxMarginX
                    val spikeBottom = obj.y
                    val spikeTop = obj.y + 0.85f

                    val pLeft = playerX + 0.15f
                    val pRight = playerX + playerSize - 0.15f
                    val pBottom = playerY + 0.15f
                    val pTop = playerY + playerSize - 0.15f

                    if (pRight > spikeLeft && pLeft < spikeRight && pBottom < spikeTop && pTop > spikeBottom) {
                        triggerDeath()
                        return
                    }
                }
                ObstacleType.SPIKE_DOWN -> {
                    val hitboxMarginX = 0.25f
                    val spikeLeft = obj.x + hitboxMarginX
                    val spikeRight = obj.x + 1f - hitboxMarginX
                    val spikeTop = obj.y + 1f
                    val spikeBottom = obj.y + 0.15f

                    val pLeft = playerX + 0.15f
                    val pRight = playerX + playerSize - 0.15f
                    val pBottom = playerY + 0.15f
                    val pTop = playerY + playerSize - 0.15f

                    if (pRight > spikeLeft && pLeft < spikeRight && pTop > spikeBottom && pBottom < spikeTop) {
                        triggerDeath()
                        return
                    }
                }
                ObstacleType.SAW_BLADE -> {
                    val sawCenterX = obj.x + 0.5f
                    val sawCenterY = obj.y + 0.5f
                    val pCenterX = playerX + 0.5f
                    val pCenterY = playerY + 0.5f
                    val distSq = (pCenterX - sawCenterX) * (pCenterX - sawCenterX) +
                            (pCenterY - sawCenterY) * (pCenterY - sawCenterY)
                    if (distSq < 0.45f) { // ~0.67 unit radius
                        triggerDeath()
                        return
                    }
                }
                ObstacleType.BLOCK, ObstacleType.HALF_BLOCK -> {
                    val blockHeight = if (obj.type == ObstacleType.HALF_BLOCK) 0.5f else 1f
                    val bLeft = obj.x
                    val bRight = obj.x + 1f
                    val bBottom = obj.y
                    val bTop = obj.y + blockHeight

                    val pLeft = playerX + 0.1f
                    val pRight = playerX + playerSize - 0.1f
                    val pBottom = playerY
                    val pTop = playerY + playerSize

                    // Check horizontal overlap
                    if (pRight > bLeft && pLeft < bRight) {
                        if (gravityDir > 0) {
                            // Normal gravity landing on top of block
                            val isLanding = playerVy <= 0f && (pBottom - bTop) in -0.3f..0.25f
                            if (isLanding) {
                                playerY = bTop
                                playerVy = 0f
                                onGround = true
                                landedOnSolid = true
                                if (isHoldingJump && playerForm == PlayerForm.CUBE) {
                                    handleTap()
                                }
                            } else if (pBottom < bTop - 0.25f && pTop > bBottom + 0.25f) {
                                // Front wall collision
                                triggerDeath()
                                return
                            }
                        } else {
                            // Inverted gravity landing on bottom of block
                            val isLandingInverted = playerVy >= 0f && (bBottom - pTop) in -0.3f..0.25f
                            if (isLandingInverted) {
                                playerY = bBottom - playerSize
                                playerVy = 0f
                                onGround = true
                                landedOnSolid = true
                                if (isHoldingJump && playerForm == PlayerForm.CUBE) {
                                    handleTap()
                                }
                            } else if (pTop > bBottom + 0.25f && pBottom < bTop - 0.25f) {
                                triggerDeath()
                                return
                            }
                        }
                    }
                }
                ObstacleType.PAD_YELLOW -> {
                    if (abs(obj.x + 0.5f - (playerX + 0.5f)) < 0.8f && abs(obj.y - playerY) < 0.6f) {
                        playerVy = 22.0f * gravityDir
                        onGround = false
                        soundEngine.playPad()
                        spawnPadRings(obj.x + 0.5f, obj.y + 0.2f, Color(0xFFFFD700))
                    }
                }
                ObstacleType.PAD_PURPLE -> {
                    if (abs(obj.x + 0.5f - (playerX + 0.5f)) < 0.8f && abs(obj.y - playerY) < 0.6f) {
                        playerVy = 14.5f * gravityDir
                        onGround = false
                        soundEngine.playPad()
                        spawnPadRings(obj.x + 0.5f, obj.y + 0.2f, Color(0xFFD000FF))
                    }
                }
                ObstacleType.PAD_GRAVITY -> {
                    if (abs(obj.x + 0.5f - (playerX + 0.5f)) < 0.8f && abs(obj.y - playerY) < 0.6f) {
                        gravityDir = -gravityDir
                        playerVy = 12.0f * gravityDir
                        onGround = false
                        soundEngine.playGravityFlip()
                        spawnPadRings(obj.x + 0.5f, obj.y + 0.2f, Color(0xFF00E5FF))
                    }
                }
                ObstacleType.PORTAL_SHIP -> {
                    if (abs(obj.x + 0.5f - (playerX + 0.5f)) < 0.7f && abs(obj.y + 1.5f - (playerY + 0.5f)) < 2.5f) {
                        if (playerForm != PlayerForm.SHIP) {
                            playerForm = PlayerForm.SHIP
                            soundEngine.playGravityFlip()
                            spawnPortalBurst(obj.x + 0.5f, playerY + 0.5f, Color(0xFFFF007F))
                        }
                    }
                }
                ObstacleType.PORTAL_CUBE -> {
                    if (abs(obj.x + 0.5f - (playerX + 0.5f)) < 0.7f && abs(obj.y + 1.5f - (playerY + 0.5f)) < 2.5f) {
                        if (playerForm != PlayerForm.CUBE) {
                            playerForm = PlayerForm.CUBE
                            soundEngine.playGravityFlip()
                            spawnPortalBurst(obj.x + 0.5f, playerY + 0.5f, Color(0xFF00FF66))
                        }
                    }
                }
                ObstacleType.PORTAL_GRAVITY_UP -> {
                    if (abs(obj.x + 0.5f - (playerX + 0.5f)) < 0.7f && gravityDir > 0) {
                        gravityDir = -1f
                        soundEngine.playGravityFlip()
                        spawnPortalBurst(obj.x + 0.5f, playerY + 0.5f, Color(0xFF00E5FF))
                    }
                }
                ObstacleType.PORTAL_GRAVITY_DOWN -> {
                    if (abs(obj.x + 0.5f - (playerX + 0.5f)) < 0.7f && gravityDir < 0) {
                        gravityDir = 1f
                        soundEngine.playGravityFlip()
                        spawnPortalBurst(obj.x + 0.5f, playerY + 0.5f, Color(0xFF00FF66))
                    }
                }
                ObstacleType.COIN -> {
                    if (!obj.collected && abs(obj.x + 0.5f - (playerX + 0.5f)) < 0.8f && abs(obj.y + 0.5f - (playerY + 0.5f)) < 0.8f) {
                        obj.collected = true
                        sessionCoinsMask = sessionCoinsMask or (1 shl (obj.id % 3))
                        soundEngine.playCoin()
                        spawnCoinSparkles(obj.x + 0.5f, obj.y + 0.5f)
                    }
                }
                ObstacleType.FINISH -> {
                    if (playerX >= obj.x) {
                        triggerWin()
                        return
                    }
                }
                else -> {}
            }
        }

        if (!landedOnSolid && playerY > 0f && (gravityDir > 0 || playerY < 6.5f)) {
            onGround = false
        }
    }

    private fun triggerDeath() {
        if (isDead) return
        isDead = true
        deathTimer = 0f
        screenShake = 1.0f
        soundEngine.playCrash()
        soundEngine.stopBgm()

        // Spawn death particles (exploding neon squares)
        spawnDeathExplosion(playerX + 0.5f, playerY + 0.5f)

        onCrash(progressPercent, attemptCount)
    }

    private fun triggerWin() {
        if (isWin || isDead) return
        isWin = true
        progressPercent = 100
        soundEngine.playWin()
        soundEngine.stopBgm()
        spawnWinCelebration(playerX + 0.5f, playerY + 0.5f)
        onLevelComplete(attemptCount, sessionCoinsMask, jumpCount)
    }

    fun respawn() {
        attemptCount++
        isDead = false
        deathTimer = 0f
        screenShake = 0f
        particles.clear()
        trail.clear()

        if (isPractice && checkpoints.isNotEmpty()) {
            val cp = checkpoints.last()
            playerX = cp.x
            playerY = cp.y
            playerVx = cp.vx
            playerVy = cp.vy
            playerRotation = cp.rotation
            playerForm = cp.form
            gravityDir = cp.gravityDir
            cameraX = cp.cameraX
            onGround = false
            soundEngine.startBgm(levelMeta.bpm)
        } else {
            resetPlayer(soft = true)
            soundEngine.startBgm(levelMeta.bpm)
        }
    }

    fun restartLevel() {
        attemptCount = 1
        jumpCount = 0
        progressPercent = 0
        sessionCoinsMask = 0
        checkpoints.clear()
        particles.clear()
        trail.clear()
        for (o in objects) {
            o.collected = false
        }
        resetPlayer(soft = false)
        soundEngine.startBgm(levelMeta.bpm)
    }

    private fun resetPlayer(soft: Boolean) {
        playerX = 0f
        playerY = 0f
        playerVx = 9.2f
        playerVy = 0f
        playerRotation = 0f
        playerForm = PlayerForm.CUBE
        gravityDir = 1f
        onGround = true
        isDead = false
        isWin = false
        cameraX = -3.2f
    }

    // Practice Mode checkpoint controls
    fun placeCheckpoint() {
        if (isDead || isWin) return
        val cp = Checkpoint(
            x = playerX,
            y = playerY,
            vx = playerVx,
            vy = playerVy,
            rotation = playerRotation,
            form = playerForm,
            gravityDir = gravityDir,
            cameraX = cameraX,
            percent = progressPercent
        )
        checkpoints.add(cp)
        soundEngine.playCheckpoint()
        spawnCheckpointDing(playerX + 0.5f, playerY + 0.5f)
    }

    fun removeCheckpoint() {
        if (checkpoints.isNotEmpty()) {
            checkpoints.removeAt(checkpoints.size - 1)
            soundEngine.playCheckpoint()
        }
    }

    // Particle Spawners
    private fun spawnDeathExplosion(cx: Float, cy: Float) {
        val colors = listOf(Color(0xFF00FF66), Color(0xFF00F0FF), Color(0xFFFFCC00), Color(0xFFFF007F), Color.White)
        for (i in 0..35) {
            val angle = Random.nextFloat() * 2f * Math.PI.toFloat()
            val speed = Random.nextFloat() * 12f + 4f
            particles.add(
                Particle(
                    x = cx,
                    y = cy,
                    vx = (kotlin.math.cos(angle) * speed),
                    vy = (kotlin.math.sin(angle) * speed),
                    size = Random.nextFloat() * 0.4f + 0.2f,
                    color = colors[Random.nextInt(colors.size)],
                    maxLife = Random.nextFloat() * 25f + 25f,
                    rotation = Random.nextFloat() * 360f,
                    vRot = (Random.nextFloat() - 0.5f) * 720f
                )
            )
        }
    }

    private fun spawnJumpDust(cx: Float, cy: Float) {
        for (i in 0..5) {
            particles.add(
                Particle(
                    x = cx + (Random.nextFloat() - 0.5f) * 0.4f,
                    y = cy,
                    vx = (Random.nextFloat() - 0.5f) * 3f,
                    vy = -Random.nextFloat() * 2f * gravityDir,
                    size = Random.nextFloat() * 0.2f + 0.1f,
                    color = Color(0x99FFFFFF),
                    maxLife = 12f
                )
            )
        }
    }

    private fun spawnOrbRingParticles(cx: Float, cy: Float, col: Color) {
        for (i in 0..12) {
            val angle = (i.toFloat() / 12f) * 2f * Math.PI.toFloat()
            particles.add(
                Particle(
                    x = cx,
                    y = cy,
                    vx = kotlin.math.cos(angle) * 5f,
                    vy = kotlin.math.sin(angle) * 5f,
                    size = 0.18f,
                    color = col,
                    maxLife = 16f
                )
            )
        }
    }

    private fun spawnPadRings(cx: Float, cy: Float, col: Color) {
        for (i in 0..8) {
            particles.add(
                Particle(
                    x = cx + (Random.nextFloat() - 0.5f) * 0.8f,
                    y = cy,
                    vx = (Random.nextFloat() - 0.5f) * 2f,
                    vy = Random.nextFloat() * 8f * gravityDir,
                    size = 0.2f,
                    color = col,
                    maxLife = 15f
                )
            )
        }
    }

    private fun spawnPortalBurst(cx: Float, cy: Float, col: Color) {
        for (i in 0..15) {
            particles.add(
                Particle(
                    x = cx,
                    y = cy + (Random.nextFloat() - 0.5f) * 2f,
                    vx = Random.nextFloat() * 8f + 2f,
                    vy = (Random.nextFloat() - 0.5f) * 6f,
                    size = 0.22f,
                    color = col,
                    maxLife = 20f
                )
            )
        }
    }

    private fun spawnCoinSparkles(cx: Float, cy: Float) {
        for (i in 0..16) {
            val angle = Random.nextFloat() * 2f * Math.PI.toFloat()
            val speed = Random.nextFloat() * 6f + 2f
            particles.add(
                Particle(
                    x = cx,
                    y = cy,
                    vx = kotlin.math.cos(angle) * speed,
                    vy = kotlin.math.sin(angle) * speed,
                    size = 0.22f,
                    color = Color(0xFFFFD700),
                    maxLife = 22f
                )
            )
        }
    }

    private fun spawnCheckpointDing(cx: Float, cy: Float) {
        for (i in 0..10) {
            particles.add(
                Particle(
                    x = cx,
                    y = cy,
                    vx = (Random.nextFloat() - 0.5f) * 4f,
                    vy = (Random.nextFloat() - 0.5f) * 4f,
                    size = 0.25f,
                    color = Color(0xFF00FF66),
                    maxLife = 18f
                )
            )
        }
    }

    private fun spawnWinCelebration(cx: Float, cy: Float) {
        val colors = listOf(Color(0xFFFFD700), Color(0xFF00FF66), Color(0xFF00F0FF), Color(0xFFFF007F))
        for (i in 0..60) {
            val angle = Random.nextFloat() * 2f * Math.PI.toFloat()
            val speed = Random.nextFloat() * 15f + 5f
            particles.add(
                Particle(
                    x = cx,
                    y = cy,
                    vx = kotlin.math.cos(angle) * speed,
                    vy = kotlin.math.sin(angle) * speed,
                    size = Random.nextFloat() * 0.35f + 0.2f,
                    color = colors[Random.nextInt(colors.size)],
                    maxLife = 45f
                )
            )
        }
    }

    private fun updateParticles(dt: Float) {
        val it = particles.iterator()
        while (it.hasNext()) {
            val p = it.next()
            p.currentLife += dt * 60f
            if (p.isDead) {
                it.remove()
            } else {
                p.x += p.vx * dt
                p.y += p.vy * dt
                p.rotation += p.vRot * dt
                p.alpha = 1.0f - (p.currentLife / p.maxLife)
            }
        }
    }
}
