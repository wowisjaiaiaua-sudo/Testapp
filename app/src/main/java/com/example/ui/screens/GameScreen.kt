package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.game.GameEngine
import com.example.game.GameObject
import com.example.game.LevelMetadata
import com.example.ui.GameViewModel
import com.example.ui.Screen
import com.example.ui.game.GameCanvasRenderer

@Composable
fun GameScreen(
    meta: LevelMetadata,
    initialObjects: List<GameObject>,
    isPracticeInitial: Boolean,
    customLevelId: String?,
    viewModel: GameViewModel
) {
    var isPractice by remember { mutableStateOf(isPracticeInitial) }
    var isPaused by remember { mutableStateOf(false) }
    var isLevelComplete by remember { mutableStateOf(false) }
    var completeAttempts by remember { mutableIntStateOf(1) }
    var completeCoinsMask by remember { mutableIntStateOf(0) }
    var completeJumps by remember { mutableIntStateOf(0) }

    val userStats by viewModel.userStats.collectAsStateWithLifecycle()

    // Initialize GameEngine
    val engine = remember(meta.id, isPractice) {
        GameEngine(
            levelMeta = meta,
            initialObjects = initialObjects,
            soundEngine = viewModel.soundEngine,
            isPractice = isPractice,
            onLevelComplete = { attempts, coins, jumps ->
                completeAttempts = attempts
                completeCoinsMask = coins
                completeJumps = jumps
                isLevelComplete = true
                viewModel.onGameSessionEnd(
                    levelId = meta.id,
                    levelName = meta.title,
                    difficulty = meta.difficulty,
                    percent = 100,
                    isPractice = isPractice,
                    isCompleted = true,
                    stars = meta.stars,
                    coinsMask = coins,
                    jumps = jumps,
                    customLevelId = customLevelId
                )
            },
            onCrash = { pct, attempts ->
                viewModel.onGameSessionEnd(
                    levelId = meta.id,
                    levelName = meta.title,
                    difficulty = meta.difficulty,
                    percent = pct,
                    isPractice = isPractice,
                    isCompleted = false,
                    stars = meta.stars,
                    coinsMask = 0,
                    jumps = 1,
                    customLevelId = customLevelId
                )
            }
        )
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.soundEngine.stopBgm()
        }
    }

    BackHandler {
        if (!isPaused && !isLevelComplete) {
            isPaused = true
            viewModel.soundEngine.stopBgm()
        } else {
            viewModel.navigateTo(Screen.LEVEL_SELECT)
        }
    }

    var gameTime by remember { mutableFloatStateOf(0f) }
    var frameTrigger by remember { mutableFloatStateOf(0f) }

    // Real-time Game Loop
    LaunchedEffect(isPaused, isLevelComplete) {
        if (isPaused || isLevelComplete) return@LaunchedEffect
        var lastNanos = System.nanoTime()
        while (true) {
            withFrameNanos { now ->
                val dt = ((now - lastNanos) / 1_000_000_000f).coerceIn(0.001f, 0.04f)
                lastNanos = now
                gameTime += dt
                engine.update(dt)
                frameTrigger = engine.playerX // triggers recomposition of Canvas
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(isPaused, isLevelComplete) {
                if (isPaused || isLevelComplete) return@pointerInput
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    engine.setHolding(true)
                    // Wait until pointer is released
                    do {
                        val event = awaitPointerEvent()
                    } while (event.changes.any { it.pressed })
                    engine.setHolding(false)
                }
            }
            .testTag("game_screen")
    ) {
        // High-Performance Game Canvas
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Read frameTrigger to recompose on every frame
            val currentPos = frameTrigger
            if (currentPos < -1000f) return@Canvas
            GameCanvasRenderer.drawGame(
                drawScope = this,
                engine = engine,
                userStats = userStats,
                gameTime = gameTime
            )
        }

        // Top HUD Overlay
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Pause Button
            IconButton(
                onClick = {
                    isPaused = true
                    viewModel.soundEngine.stopBgm()
                },
                modifier = Modifier
                    .size(40.dp)
                    .background(Color(0x55000000), CircleShape)
                    .border(1.5.dp, Color(0x88FFFFFF), CircleShape)
                    .testTag("pause_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Pause,
                    contentDescription = "Pause",
                    tint = Color.White
                )
            }

            // Level Name & Progress Bar in Center
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f).padding(horizontal = 24.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = meta.title,
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black
                    )
                    if (isPractice) {
                        Text(
                            text = "• PRACTICE",
                            color = Color(0xFF00E5FF),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { engine.progressPercent / 100f },
                    modifier = Modifier
                        .fillMaxWidth(0.6f)
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = if (isPractice) Color(0xFF00E5FF) else Color(0xFF00FF66),
                    trackColor = Color(0x55000000)
                )
            }

            // Attempt Counter
            Box(
                modifier = Modifier
                    .background(Color(0x55000000), RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "Attempt ${engine.attemptCount}",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        // Practice Mode Bottom Controls (Place / Delete Checkpoint)
        if (isPractice && !isPaused && !isLevelComplete) {
            Row(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(20.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Delete Checkpoint Button
                IconButton(
                    onClick = { engine.removeCheckpoint() },
                    modifier = Modifier
                        .size(54.dp)
                        .background(Color(0xDDFF1744), CircleShape)
                        .border(2.dp, Color.White, CircleShape)
                        .testTag("remove_checkpoint_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Remove Checkpoint",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }

                // Place Checkpoint Button (Diamond Green)
                IconButton(
                    onClick = { engine.placeCheckpoint() },
                    modifier = Modifier
                        .size(58.dp)
                        .background(Color(0xDD00FF66), CircleShape)
                        .border(2.5.dp, Color.White, CircleShape)
                        .testTag("place_checkpoint_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Place Checkpoint",
                        tint = Color.Black,
                        modifier = Modifier.size(34.dp)
                    )
                }
            }
        }

        // Pause Menu Dialog
        if (isPaused) {
            Dialog(onDismissRequest = {
                isPaused = false
                viewModel.soundEngine.startBgm(meta.bpm)
            }) {
                Column(
                    modifier = Modifier
                        .width(340.dp)
                        .background(Color(0xFF0F1528), RoundedCornerShape(20.dp))
                        .border(2.dp, Color(0xFF00F0FF), RoundedCornerShape(20.dp))
                        .padding(24.dp)
                        .testTag("pause_dialog"),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "PAUSED",
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 3.sp
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Resume
                    Button(
                        onClick = {
                            isPaused = false
                            viewModel.soundEngine.startBgm(meta.bpm)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00FF66)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, tint = Color.Black)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("RESUME", color = Color.Black, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Restart
                    Button(
                        onClick = {
                            engine.restartLevel()
                            isPaused = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = null, tint = Color.Black)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("RESTART", color = Color.Black, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Practice Mode Toggle
                    Button(
                        onClick = {
                            isPractice = !isPractice
                            engine.restartLevel()
                            isPaused = false
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isPractice) Color(0xFFFF9900) else Color(0xFFD000FF)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Text(
                            text = if (isPractice) "SWITCH TO NORMAL MODE" else "SWITCH TO PRACTICE MODE",
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Exit to Levels
                    Button(
                        onClick = {
                            viewModel.soundEngine.stopBgm()
                            viewModel.navigateTo(Screen.LEVEL_SELECT)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2A344D)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Home, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("EXIT TO MENU", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Level Complete Victory Dialog
        if (isLevelComplete) {
            Dialog(onDismissRequest = {}) {
                Column(
                    modifier = Modifier
                        .width(360.dp)
                        .background(Color(0xFF0D172E), RoundedCornerShape(20.dp))
                        .border(3.dp, Color(0xFFFFD700), RoundedCornerShape(20.dp))
                        .shadow(24.dp, RoundedCornerShape(20.dp), spotColor = Color(0xFFFFD700))
                        .padding(24.dp)
                        .testTag("win_dialog"),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = "Victory",
                        tint = Color(0xFFFFD700),
                        modifier = Modifier.size(60.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "LEVEL COMPLETE!",
                        color = Color(0xFFFFD700),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp
                    )

                    Text(
                        text = meta.title,
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("ATTEMPTS", color = Color(0xFFAAAAAA), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text("$completeAttempts", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Black)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("JUMPS", color = Color(0xFFAAAAAA), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text("$completeJumps", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Black)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("STARS", color = Color(0xFFAAAAAA), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = Color(0xFFFFD700),
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(" +${meta.stars}", color = Color(0xFFFFD700), fontSize = 20.sp, fontWeight = FontWeight.Black)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = {
                                engine.restartLevel()
                                isLevelComplete = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f).height(46.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Refresh, contentDescription = null, tint = Color.Black)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("REPLAY", color = Color.Black, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                viewModel.soundEngine.stopBgm()
                                viewModel.navigateTo(Screen.LEVEL_SELECT)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00FF66)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1.2f).height(46.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Home, contentDescription = null, tint = Color.Black)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("LEVELS", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
