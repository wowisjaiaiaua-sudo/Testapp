package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.game.GameObject
import com.example.game.LevelMetadata
import com.example.game.LevelTemplates
import com.example.game.ObstacleType
import com.example.ui.GameViewModel
import com.example.ui.Screen
import com.example.ui.game.GameCanvasRenderer
import kotlin.math.floor
import kotlin.math.roundToInt

@Composable
fun LevelEditorScreen(
    editingLevelId: String?,
    viewModel: GameViewModel
) {
    BackHandler {
        viewModel.navigateTo(Screen.LEVEL_SELECT)
    }

    var levelTitle by remember { mutableStateOf("My Creation") }
    var levelBpm by remember { mutableIntStateOf(135) }
    var gridScrollX by remember { mutableFloatStateOf(0f) }
    var selectedTool by remember { mutableStateOf<ObstacleType?>(ObstacleType.BLOCK) }
    var isEraserMode by remember { mutableStateOf(false) }

    val editorObjects = remember { mutableStateListOf<GameObject>() }
    val customLevels by viewModel.customLevels.collectAsStateWithLifecycle()

    // Load existing level if editing
    LaunchedEffect(editingLevelId, customLevels) {
        if (editingLevelId != null && editorObjects.isEmpty()) {
            val existing = customLevels.firstOrNull { it.id == editingLevelId }
            if (existing != null) {
                levelTitle = existing.title
                levelBpm = existing.bpm
                val deserialized = LevelTemplates.deserializeObjects(existing.objectDataJson)
                editorObjects.clear()
                editorObjects.addAll(deserialized)
            }
        }
    }

    val tools = listOf(
        ObstacleType.BLOCK,
        ObstacleType.HALF_BLOCK,
        ObstacleType.SPIKE,
        ObstacleType.SPIKE_DOWN,
        ObstacleType.PAD_YELLOW,
        ObstacleType.PAD_PURPLE,
        ObstacleType.PAD_GRAVITY,
        ObstacleType.ORB_YELLOW,
        ObstacleType.ORB_GRAVITY,
        ObstacleType.PORTAL_SHIP,
        ObstacleType.PORTAL_CUBE,
        ObstacleType.SAW_BLADE,
        ObstacleType.COIN
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF090D1C))
            .testTag("level_editor_screen")
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top Toolbar: Back, Title, BPM, Test, Save
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0F162B))
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { viewModel.navigateTo(Screen.LEVEL_SELECT) },
                        modifier = Modifier.size(38.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    OutlinedTextField(
                        value = levelTitle,
                        onValueChange = { levelTitle = it },
                        modifier = Modifier
                            .width(180.dp)
                            .height(48.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF00FF66),
                            unfocusedBorderColor = Color(0x66FFFFFF),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                }

                // Grid Pan Controls (< Scroll >)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { gridScrollX = (gridScrollX - 5f).coerceAtLeast(0f) },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(Icons.Default.KeyboardArrowLeft, contentDescription = "Scroll Left", tint = Color.White)
                    }
                    Text(
                        text = "X: ${gridScrollX.toInt()}",
                        color = Color(0xFF00F0FF),
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    IconButton(
                        onClick = { gridScrollX += 5f },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(Icons.Default.KeyboardArrowRight, contentDescription = "Scroll Right", tint = Color.White)
                    }
                }

                // Action Buttons: Playtest, Save, Clear
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            editorObjects.clear()
                            viewModel.soundEngine.playCrash()
                        },
                        modifier = Modifier
                            .size(38.dp)
                            .background(Color(0x33FF0055), CircleShape)
                    ) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear", tint = Color(0xFFFF5252))
                    }

                    // Test Level Button
                    Button(
                        onClick = {
                            viewModel.soundEngine.playJump()
                            val testMeta = LevelMetadata(
                                id = editingLevelId ?: "custom_test",
                                title = levelTitle,
                                difficulty = "Custom",
                                stars = 1,
                                bpm = levelBpm,
                                bgColorHex = "#120B24",
                                floorColorHex = "#21153E",
                                accentColorHex = "#00F0FF",
                                totalLength = maxOf(120f, (editorObjects.maxOfOrNull { it.x } ?: 30f) + 15f)
                            )
                            viewModel.navigateTo(
                                Screen.GAME(testMeta, editorObjects.toList(), isPractice = false, customLevelId = editingLevelId)
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00FF66)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(38.dp)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.Black)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("TEST", color = Color.Black, fontWeight = FontWeight.Bold)
                    }

                    // Save Level Button
                    Button(
                        onClick = {
                            viewModel.soundEngine.playCoin()
                            viewModel.saveCustomLevel(
                                id = editingLevelId,
                                title = levelTitle,
                                bpm = levelBpm,
                                themeHex = "#00F0FF",
                                objects = editorObjects.toList()
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00F0FF)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(38.dp)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, tint = Color.Black)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("SAVE", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Interactive Editor Grid Canvas
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .pointerInput(gridScrollX, selectedTool, isEraserMode) {
                        detectTapGestures { tapOffset ->
                            val unitPx = size.width / 14f
                            val floorYPx = size.height * 0.78f

                            // Convert screen tap coordinate to world grid units
                            val worldX = floor((tapOffset.x / unitPx) + gridScrollX)
                            val worldY = floor((floorYPx - tapOffset.y) / unitPx).coerceAtLeast(0f)

                            if (isEraserMode) {
                                // Delete object at this grid cell
                                val removed = editorObjects.removeAll { it.x == worldX && it.y == worldY }
                                if (removed) {
                                    viewModel.soundEngine.playPad()
                                }
                            } else if (selectedTool != null) {
                                // Remove any existing object at cell first, then add selected
                                editorObjects.removeAll { it.x == worldX && it.y == worldY }
                                val newId = (editorObjects.maxOfOrNull { it.id } ?: 0) + 1
                                editorObjects.add(
                                    GameObject(
                                        id = newId,
                                        x = worldX,
                                        y = worldY,
                                        type = selectedTool!!
                                    )
                                )
                                viewModel.soundEngine.playOrb()
                            }
                        }
                    }
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height
                    val unitPx = w / 14f
                    val floorYPx = h * 0.78f

                    // Draw Background Grid
                    val gridCols = (w / unitPx).toInt() + 1
                    for (c in 0..gridCols) {
                        val gx = c * unitPx
                        drawLine(
                            color = Color(0x1800F0FF),
                            start = Offset(gx, 0f),
                            end = Offset(gx, floorYPx),
                            strokeWidth = 1f
                        )
                    }
                    val gridRows = (floorYPx / unitPx).toInt() + 1
                    for (r in 0..gridRows) {
                        val gy = floorYPx - (r * unitPx)
                        drawLine(
                            color = Color(0x1800F0FF),
                            start = Offset(0f, gy),
                            end = Offset(w, gy),
                            strokeWidth = 1f
                        )
                    }

                    // Floor Line
                    drawRect(
                        color = Color(0xFF1B233D),
                        topLeft = Offset(0f, floorYPx),
                        size = Size(w, h - floorYPx)
                    )
                    drawLine(
                        color = Color(0xFF00FF66),
                        start = Offset(0f, floorYPx),
                        end = Offset(w, floorYPx),
                        strokeWidth = 3f
                    )

                    // Draw all placed objects
                    for (obj in editorObjects) {
                        val ox = (obj.x - gridScrollX) * unitPx
                        val oy = floorYPx - (obj.y * unitPx)
                        if (ox in -unitPx..(w + unitPx)) {
                            // Draw object outline box on grid
                            drawRect(
                                color = Color(0x4400FF66),
                                topLeft = Offset(ox, oy - unitPx * obj.height),
                                size = Size(unitPx * obj.width, unitPx * obj.height)
                            )
                            // Draw the object using GameCanvasRenderer
                            GameCanvasRenderer.drawGameObject(
                                drawScope = this,
                                obj = obj,
                                ox = ox,
                                oy = oy,
                                unitPx = unitPx,
                                accentColor = Color(0xFF00FF66),
                                gameTime = 0f
                            )
                        }
                    }
                }
            }

            // Bottom Palette Bar: Select object to place or eraser
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0F162B))
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Eraser button
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .background(
                            if (isEraserMode) Color(0xFFFF1744) else Color(0xFF1E2844),
                            RoundedCornerShape(12.dp)
                        )
                        .clickable {
                            isEraserMode = !isEraserMode
                            viewModel.soundEngine.playPad()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Eraser", tint = Color.White)
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Scrollable tool list
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(tools) { tool ->
                        val isSelected = !isEraserMode && selectedTool == tool
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .background(
                                    if (isSelected) Color(0xFF00FF66) else Color(0xFF1E2844),
                                    RoundedCornerShape(12.dp)
                                )
                                .border(
                                    if (isSelected) 2.dp else 1.dp,
                                    if (isSelected) Color.White else Color(0x33FFFFFF),
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable {
                                    isEraserMode = false
                                    selectedTool = tool
                                    viewModel.soundEngine.playOrb()
                                }
                                .padding(4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = tool.name.replace("PORTAL_", "P_").replace("PAD_", "PAD\n").take(6),
                                color = if (isSelected) Color.Black else Color.White,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}
