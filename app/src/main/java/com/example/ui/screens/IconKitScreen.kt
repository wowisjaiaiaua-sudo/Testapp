package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.GameViewModel
import com.example.ui.Screen
import com.example.ui.game.GameCanvasRenderer

@Composable
fun IconKitScreen(viewModel: GameViewModel) {
    BackHandler {
        viewModel.navigateTo(Screen.MENU)
    }

    val stats by viewModel.userStats.collectAsStateWithLifecycle()
    var selectedCategory by remember { mutableIntStateOf(0) } // 0 = Cube, 1 = Ship

    val primaryColor = try {
        Color(android.graphics.Color.parseColor(stats.primaryColorHex))
    } catch (_: Exception) {
        Color(0xFF00FF66)
    }

    val secondaryColor = try {
        Color(android.graphics.Color.parseColor(stats.secondaryColorHex))
    } catch (_: Exception) {
        Color(0xFFFFD700)
    }

    // Animation for preview jump
    val infiniteTransition = rememberInfiniteTransition(label = "iconPreview")
    val jumpY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -35f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "jumpY"
    )
    val rotAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 180f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rot"
    )

    val colorPalette = listOf(
        "#00FF66", // Neon Green
        "#00F0FF", // Cyber Cyan
        "#FFD700", // Sunburst Yellow
        "#FF007F", // Hot Pink
        "#9D00FF", // Electric Purple
        "#FF2A2A", // Laser Red
        "#FF7700", // Bright Orange
        "#FFFFFF"  // Bright White
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF090D1E), Color(0xFF131A33), Color(0xFF090D1E))
                )
            )
            .testTag("icon_kit_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 12.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        viewModel.soundEngine.playJump()
                        viewModel.navigateTo(Screen.MENU)
                    },
                    modifier = Modifier
                        .size(44.dp)
                        .background(Color(0x33FFFFFF), CircleShape)
                        .testTag("back_to_menu_from_kit")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Text(
                    text = "THE ICON KIT",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Live Preview Stage & Character Selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Animated Character Preview Podium
                Box(
                    modifier = Modifier
                        .size(150.dp)
                        .background(Color(0xFF0D1426), RoundedCornerShape(20.dp))
                        .border(2.5.dp, Color(0xFF00F0FF), RoundedCornerShape(20.dp))
                        .shadow(16.dp, RoundedCornerShape(20.dp), spotColor = Color(0xFF00F0FF)),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.size(90.dp)) {
                        val sz = size.width
                        val center = Offset(sz / 2f, sz / 2f + jumpY)
                        if (selectedCategory == 0) {
                            rotate(degrees = rotAngle, pivot = center) {
                                GameCanvasRenderer.drawPlayerCube(
                                    drawScope = this,
                                    px = 0f,
                                    py = jumpY,
                                    size = sz,
                                    primaryColor = primaryColor,
                                    secondaryColor = secondaryColor,
                                    skinIndex = stats.selectedCubeIndex
                                )
                            }
                        } else {
                            val tilt = jumpY * 0.4f
                            rotate(degrees = tilt, pivot = center) {
                                GameCanvasRenderer.drawPlayerShip(
                                    drawScope = this,
                                    px = 0f,
                                    py = jumpY,
                                    size = sz,
                                    primaryColor = primaryColor,
                                    secondaryColor = secondaryColor,
                                    skinIndex = stats.selectedShipIndex
                                )
                            }
                        }
                    }
                }

                // Category switcher (Cube vs Ship) & Skin selector
                Column(modifier = Modifier.weight(1f)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        CategoryTabButton("CUBES", selected = selectedCategory == 0) {
                            viewModel.soundEngine.playOrb()
                            selectedCategory = 0
                        }
                        CategoryTabButton("SHIPS", selected = selectedCategory == 1) {
                            viewModel.soundEngine.playOrb()
                            selectedCategory = 1
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "SELECT ICON STYLE:",
                        color = Color(0xFFAAAAAA),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Skin thumbnails
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(6) { index ->
                            val isSelected = if (selectedCategory == 0) stats.selectedCubeIndex == index else stats.selectedShipIndex == index
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .background(Color(0xFF151D33), RoundedCornerShape(12.dp))
                                    .border(
                                        if (isSelected) 3.dp else 1.5.dp,
                                        if (isSelected) Color(0xFF00FF66) else Color(0x44FFFFFF),
                                        RoundedCornerShape(12.dp)
                                    )
                                    .clickable {
                                        viewModel.soundEngine.playPad()
                                        if (selectedCategory == 0) {
                                            viewModel.updateCustomizer(cubeIndex = index)
                                        } else {
                                            viewModel.updateCustomizer(shipIndex = index)
                                        }
                                    }
                                    .testTag("skin_item_$index"),
                                contentAlignment = Alignment.Center
                            ) {
                                Canvas(modifier = Modifier.size(34.dp)) {
                                    if (selectedCategory == 0) {
                                        GameCanvasRenderer.drawPlayerCube(
                                            drawScope = this,
                                            px = 0f,
                                            py = 0f,
                                            size = size.width,
                                            primaryColor = primaryColor,
                                            secondaryColor = secondaryColor,
                                            skinIndex = index
                                        )
                                    } else {
                                        GameCanvasRenderer.drawPlayerShip(
                                            drawScope = this,
                                            px = 0f,
                                            py = 0f,
                                            size = size.width,
                                            primaryColor = primaryColor,
                                            secondaryColor = secondaryColor,
                                            skinIndex = index
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Primary Color Picker
            Column {
                Text(
                    text = "PRIMARY COLOR (BASE)",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(colorPalette.size) { i ->
                        val hex = colorPalette[i]
                        val isSel = stats.primaryColorHex.equals(hex, ignoreCase = true)
                        val col = Color(android.graphics.Color.parseColor(hex))
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .background(col, CircleShape)
                                .border(if (isSel) 3.dp else 1.dp, if (isSel) Color.White else Color.Transparent, CircleShape)
                                .clickable {
                                    viewModel.soundEngine.playOrb()
                                    viewModel.updateCustomizer(primaryHex = hex)
                                }
                                .testTag("primary_color_$i")
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Secondary Color Picker
            Column {
                Text(
                    text = "SECONDARY COLOR (OUTLINE & GLOW)",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(colorPalette.size) { i ->
                        val hex = colorPalette[i]
                        val isSel = stats.secondaryColorHex.equals(hex, ignoreCase = true)
                        val col = Color(android.graphics.Color.parseColor(hex))
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .background(col, CircleShape)
                                .border(if (isSel) 3.dp else 1.dp, if (isSel) Color.White else Color.Transparent, CircleShape)
                                .clickable {
                                    viewModel.soundEngine.playOrb()
                                    viewModel.updateCustomizer(secondaryHex = hex)
                                }
                                .testTag("secondary_color_$i")
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryTabButton(title: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .background(
                if (selected) Color(0xFF00FF66) else Color(0xFF1E2844),
                RoundedCornerShape(10.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = title,
            color = if (selected) Color.Black else Color.White,
            fontWeight = FontWeight.Black,
            fontSize = 12.sp
        )
    }
}
