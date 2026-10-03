package com.example.ui.screens

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
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.GameViewModel
import com.example.ui.Screen
import com.example.ui.components.SettingsDialog

@Composable
fun MenuScreen(viewModel: GameViewModel) {
    var showSettings by remember { mutableStateOf(false) }

    // Floating pulsing animation for play button
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "playPulse"
    )

    val logoBounce by infiniteTransition.animateFloat(
        initialValue = -8f,
        targetValue = 8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "logoBounce"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF090D1E),
                        Color(0xFF0E1A38),
                        Color(0xFF090D1E)
                    )
                )
            )
            .testTag("menu_screen")
    ) {
        // Decorative background geometric particles
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            // Grid lines
            val step = 70f
            for (x in 0..(w / step).toInt()) {
                drawLine(
                    color = Color(0x1000F0FF),
                    start = Offset(x * step, 0f),
                    end = Offset(x * step, h),
                    strokeWidth = 1f
                )
            }
            for (y in 0..(h / step).toInt()) {
                drawLine(
                    color = Color(0x1000F0FF),
                    start = Offset(0f, y * step),
                    end = Offset(w, y * step),
                    strokeWidth = 1f
                )
            }
        }

        // Top Header: Title & Settings
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "V2.2 ARCADE",
                color = Color(0xFF00FF66),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 2.sp
            )

            IconButton(
                onClick = {
                    viewModel.soundEngine.playPad()
                    showSettings = true
                },
                modifier = Modifier
                    .size(48.dp)
                    .background(Color(0x33FFFFFF), CircleShape)
                    .testTag("settings_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = Color.White
                )
            }
        }

        // Center Content: Logo & Main Action Buttons
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Stylized Geometry Dash Logo
            Box(
                modifier = Modifier
                    .padding(bottom = 28.dp)
                    .scale(1f + (logoBounce / 200f)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "GEOMETRY",
                        color = Color(0xFF00F0FF),
                        fontSize = 42.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 4.sp,
                        modifier = Modifier.shadow(16.dp, spotColor = Color(0xFF00F0FF))
                    )
                    Text(
                        text = "DASH",
                        color = Color(0xFFFFD700),
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 6.sp,
                        modifier = Modifier.shadow(16.dp, spotColor = Color(0xFFFFD700))
                    )
                }
            }

            // Big Center Play Button
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .scale(pulseScale)
                    .background(
                        Brush.radialGradient(
                            listOf(Color(0xFF00FF66), Color(0xFF009933))
                        ),
                        CircleShape
                    )
                    .border(4.dp, Color.White, CircleShape)
                    .shadow(24.dp, CircleShape, spotColor = Color(0xFF00FF66))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        viewModel.soundEngine.playJump()
                        viewModel.navigateTo(Screen.LEVEL_SELECT)
                    }
                    .testTag("play_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Play Levels",
                    tint = Color.White,
                    modifier = Modifier.size(64.dp)
                )
            }

            Spacer(modifier = Modifier.height(36.dp))

            // Sub Navigation Icons (Icon Kit, Level Editor, Stats)
            Row(
                horizontalArrangement = Arrangement.spacedBy(28.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                MenuFeatureButton(
                    icon = Icons.Default.Face,
                    title = "ICON KIT",
                    accentColor = Color(0xFF00F0FF),
                    tag = "icon_kit_button"
                ) {
                    viewModel.soundEngine.playOrb()
                    viewModel.navigateTo(Screen.ICON_KIT)
                }

                MenuFeatureButton(
                    icon = Icons.Default.Build,
                    title = "EDITOR",
                    accentColor = Color(0xFFFF9900),
                    tag = "level_editor_button"
                ) {
                    viewModel.soundEngine.playPad()
                    viewModel.navigateTo(Screen.LEVEL_EDITOR())
                }

                MenuFeatureButton(
                    icon = Icons.Default.EmojiEvents,
                    title = "STATS",
                    accentColor = Color(0xFFFF007F),
                    tag = "stats_button"
                ) {
                    viewModel.soundEngine.playCoin()
                    viewModel.navigateTo(Screen.STATS)
                }
            }
        }

        // Settings Dialog
        if (showSettings) {
            SettingsDialog(
                viewModel = viewModel,
                onDismiss = { showSettings = false }
            )
        }
    }
}

@Composable
fun MenuFeatureButton(
    icon: ImageVector,
    title: String,
    accentColor: Color,
    tag: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .testTag(tag)
    ) {
        Box(
            modifier = Modifier
                .size(62.dp)
                .background(Color(0xFF141A30), RoundedCornerShape(16.dp))
                .border(2.5.dp, accentColor, RoundedCornerShape(16.dp))
                .shadow(8.dp, RoundedCornerShape(16.dp), spotColor = accentColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = accentColor,
                modifier = Modifier.size(32.dp)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = title,
            color = Color.White,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
    }
}
