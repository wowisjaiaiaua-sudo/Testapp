package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.GameViewModel
import com.example.ui.Screen

data class AchievementItem(
    val id: String,
    val title: String,
    val description: String,
    val current: Int,
    val target: Int,
    val isUnlocked: Boolean
)

@Composable
fun StatsScreen(viewModel: GameViewModel) {
    BackHandler {
        viewModel.navigateTo(Screen.MENU)
    }

    val stats by viewModel.userStats.collectAsStateWithLifecycle()
    val levelRecords by viewModel.levelRecords.collectAsStateWithLifecycle()
    val customLevels by viewModel.customLevels.collectAsStateWithLifecycle()

    val completedLevelsCount = levelRecords.count { it.isCompleted }

    // Build Achievements
    val achievements = listOf(
        AchievementItem(
            id = "jump_50",
            title = "Rhythm Rookie",
            description = "Jump 50 times across all levels",
            current = stats.totalJumps,
            target = 50,
            isUnlocked = stats.totalJumps >= 50
        ),
        AchievementItem(
            id = "jump_250",
            title = "Spike Hopper",
            description = "Jump 250 times",
            current = stats.totalJumps,
            target = 250,
            isUnlocked = stats.totalJumps >= 250
        ),
        AchievementItem(
            id = "attempts_20",
            title = "Never Give Up",
            description = "Attempt levels 20 times",
            current = stats.totalAttempts,
            target = 20,
            isUnlocked = stats.totalAttempts >= 20
        ),
        AchievementItem(
            id = "attempts_100",
            title = "Tenacity Master",
            description = "Attempt levels 100 times",
            current = stats.totalAttempts,
            target = 100,
            isUnlocked = stats.totalAttempts >= 100
        ),
        AchievementItem(
            id = "beat_stereo",
            title = "Stereo Conqueror",
            description = "Complete Stereo Madness in Normal Mode",
            current = if (levelRecords.any { it.levelId == "stereo_madness" && it.isCompleted }) 1 else 0,
            target = 1,
            isUnlocked = levelRecords.any { it.levelId == "stereo_madness" && it.isCompleted }
        ),
        AchievementItem(
            id = "beat_dryout",
            title = "Rocket Aviator",
            description = "Complete Dry Out in Normal Mode",
            current = if (levelRecords.any { it.levelId == "dry_out" && it.isCompleted }) 1 else 0,
            target = 1,
            isUnlocked = levelRecords.any { it.levelId == "dry_out" && it.isCompleted }
        ),
        AchievementItem(
            id = "coins_3",
            title = "Secret Seeker",
            description = "Collect 3 Secret Coins",
            current = stats.totalCoins,
            target = 3,
            isUnlocked = stats.totalCoins >= 3
        ),
        AchievementItem(
            id = "creator",
            title = "The Architect",
            description = "Create and save a custom level in Editor",
            current = customLevels.size,
            target = 1,
            isUnlocked = customLevels.isNotEmpty()
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF090D1E), Color(0xFF141C36), Color(0xFF090D1E))
                )
            )
            .testTag("stats_screen")
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
                        .testTag("back_from_stats")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Text(
                    text = "STATS & ACHIEVEMENTS",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Stat Cards Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatMetricCard(
                    title = "TOTAL JUMPS",
                    value = "${stats.totalJumps}",
                    accentColor = Color(0xFF00FF66),
                    modifier = Modifier.weight(1f)
                )
                StatMetricCard(
                    title = "ATTEMPTS",
                    value = "${stats.totalAttempts}",
                    accentColor = Color(0xFF00F0FF),
                    modifier = Modifier.weight(1f)
                )
                StatMetricCard(
                    title = "STARS",
                    value = "${stats.totalStars}",
                    accentColor = Color(0xFFFFD700),
                    modifier = Modifier.weight(1f)
                )
                StatMetricCard(
                    title = "COMPLETED",
                    value = "$completedLevelsCount",
                    accentColor = Color(0xFFFF007F),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "ACHIEVEMENTS",
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Achievement List
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(achievements) { item ->
                    AchievementRowCard(item)
                }
            }
        }
    }
}

@Composable
fun StatMetricCard(
    title: String,
    value: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(Color(0xFF10172C), RoundedCornerShape(14.dp))
            .border(1.5.dp, accentColor, RoundedCornerShape(14.dp))
            .padding(vertical = 10.dp, horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = title,
                color = Color(0xFFAAAAAA),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                color = accentColor,
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

@Composable
fun AchievementRowCard(item: AchievementItem) {
    val progress = (item.current.toFloat() / item.target).coerceIn(0f, 1f)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF10172C), RoundedCornerShape(12.dp))
            .border(
                1.dp,
                if (item.isUnlocked) Color(0xFF00FF66) else Color(0x33FFFFFF),
                RoundedCornerShape(12.dp)
            )
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (item.isUnlocked) Icons.Default.CheckCircle else Icons.Default.Lock,
                contentDescription = null,
                tint = if (item.isUnlocked) Color(0xFF00FF66) else Color(0xFF667799),
                modifier = Modifier.size(28.dp)
            )

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    color = if (item.isUnlocked) Color.White else Color(0xFFB0B0C0),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = item.description,
                    color = Color(0xFF7E8B9E),
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(5.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = if (item.isUnlocked) Color(0xFF00FF66) else Color(0xFF00F0FF),
                    trackColor = Color(0xFF1F2B48)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = "${item.current.coerceAtMost(item.target)} / ${item.target}",
                color = if (item.isUnlocked) Color(0xFF00FF66) else Color(0xFFAAAAAA),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}
