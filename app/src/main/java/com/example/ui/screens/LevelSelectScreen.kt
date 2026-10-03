package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.db.CustomLevel
import com.example.data.db.LevelRecord
import com.example.game.LevelMetadata
import com.example.game.LevelTemplates
import com.example.ui.GameViewModel
import com.example.ui.Screen

@Composable
fun LevelSelectScreen(viewModel: GameViewModel) {
    BackHandler {
        viewModel.navigateTo(Screen.MENU)
    }

    var selectedTab by remember { mutableIntStateOf(0) }
    val levelRecords by viewModel.levelRecords.collectAsStateWithLifecycle()
    val customLevels by viewModel.customLevels.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF090D1E), Color(0xFF11172E), Color(0xFF090D1E))
                )
            )
            .testTag("level_select_screen")
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
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
                        .testTag("back_to_menu_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back to Menu",
                        tint = Color.White
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Text(
                    text = "SELECT LEVEL",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp
                )
            }

            // Tabs: Official vs Custom
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color(0xFF0A0F22),
                contentColor = Color(0xFF00FF66),
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = Color(0xFF00FF66),
                        height = 3.dp
                    )
                }
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = {
                        viewModel.soundEngine.playPad()
                        selectedTab = 0
                    },
                    text = {
                        Text(
                            "OFFICIAL LEVELS",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = {
                        viewModel.soundEngine.playPad()
                        selectedTab = 1
                    },
                    text = {
                        Text(
                            "CUSTOM LEVELS (${customLevels.size})",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (selectedTab == 0) {
                // Official levels horizontal carousel
                LazyRow(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 28.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    items(LevelTemplates.OFFICIAL_LEVELS) { meta ->
                        val record = levelRecords.firstOrNull { it.levelId == meta.id }
                        OfficialLevelCard(
                            meta = meta,
                            record = record,
                            onPlay = { practice ->
                                viewModel.startOfficialLevel(meta, practice)
                            }
                        )
                    }
                }
            } else {
                // Custom levels column
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp)
                ) {
                    Button(
                        onClick = {
                            viewModel.soundEngine.playJump()
                            viewModel.navigateTo(Screen.LEVEL_EDITOR())
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00F0FF)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("create_new_level_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = Color.Black
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "CREATE NEW CUSTOM LEVEL",
                            color = Color.Black,
                            fontWeight = FontWeight.Black
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (customLevels.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No custom levels created yet.\nTap above to build your own rhythm obstacle track!",
                                color = Color(0xFF8899B3),
                                fontSize = 15.sp,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(14.dp),
                            contentPadding = PaddingValues(bottom = 24.dp)
                        ) {
                            items(customLevels, key = { it.id }) { custom ->
                                CustomLevelItemCard(
                                    custom = custom,
                                    onPlay = { practice ->
                                        viewModel.startCustomLevel(custom, practice)
                                    },
                                    onEdit = {
                                        viewModel.navigateTo(Screen.LEVEL_EDITOR(custom.id))
                                    },
                                    onDelete = {
                                        viewModel.deleteCustomLevel(custom.id)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun OfficialLevelCard(
    meta: LevelMetadata,
    record: LevelRecord?,
    onPlay: (isPractice: Boolean) -> Unit
) {
    val bestPercent = record?.bestPercent ?: 0
    val practicePercent = record?.practicePercent ?: 0
    val coinsMask = record?.coinsCollectedMask ?: 0

    val accent = try {
        Color(android.graphics.Color.parseColor(meta.accentColorHex))
    } catch (_: Exception) {
        Color(0xFF00F0FF)
    }

    Box(
        modifier = Modifier
            .width(310.dp)
            .height(250.dp)
            .background(Color(0xFF12182D), RoundedCornerShape(20.dp))
            .border(2.5.dp, accent, RoundedCornerShape(20.dp))
            .shadow(16.dp, RoundedCornerShape(20.dp), spotColor = accent)
            .padding(18.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Level Title & Stars
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = meta.title,
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = "${meta.difficulty.uppercase()} • ${meta.bpm} BPM",
                        color = accent,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Stars",
                        tint = Color(0xFFFFD700),
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = " ${meta.stars}",
                        color = Color(0xFFFFD700),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }

            // Normal Progress Bar
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("NORMAL MODE", color = Color(0xFFAAAAAA), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    Text("$bestPercent%", color = Color(0xFF00FF66), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { bestPercent / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = Color(0xFF00FF66),
                    trackColor = Color(0xFF1E2844)
                )
            }

            // Practice Progress Bar
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("PRACTICE MODE", color = Color(0xFFAAAAAA), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    Text("$practicePercent%", color = Color(0xFF00E5FF), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { practicePercent / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = Color(0xFF00E5FF),
                    trackColor = Color(0xFF1E2844)
                )
            }

            // Secret Coins (3 coins)
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("COINS: ", color = Color(0xFFAAAAAA), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                for (i in 0..2) {
                    val collected = (coinsMask and (1 shl i)) != 0
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .background(
                                if (collected) Color(0xFFFFD700) else Color(0x33FFFFFF),
                                CircleShape
                            )
                            .border(1.5.dp, Color(0xFFFFD700), CircleShape)
                    )
                }
            }

            // Action Buttons (Play Normal vs Practice)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = { onPlay(false) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00FF66)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1.5f)
                        .height(44.dp)
                        .testTag("play_level_${meta.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Play",
                        tint = Color.Black
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("PLAY", color = Color.Black, fontWeight = FontWeight.Black)
                }

                Button(
                    onClick = { onPlay(true) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .testTag("practice_level_${meta.id}")
                ) {
                    Text("PRACTICE", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
fun CustomLevelItemCard(
    custom: CustomLevel,
    onPlay: (isPractice: Boolean) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val themeCol = try {
        Color(android.graphics.Color.parseColor(custom.themeColorHex))
    } catch (_: Exception) {
        Color(0xFF00F0FF)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF12182D), RoundedCornerShape(16.dp))
            .border(2.dp, themeCol, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = custom.title,
                    color = Color.White,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${custom.bpm} BPM • Best: ${custom.bestPercent}% • Attempts: ${custom.attempts}",
                    color = Color(0xFF8899B3),
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onEdit,
                    modifier = Modifier
                        .size(38.dp)
                        .background(Color(0x22FFFFFF), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Level",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier
                        .size(38.dp)
                        .background(Color(0x22FF0055), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Level",
                        tint = Color(0xFFFF5252),
                        modifier = Modifier.size(18.dp)
                    )
                }

                Button(
                    onClick = { onPlay(false) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00FF66)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.height(38.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Play",
                        tint = Color.Black
                    )
                    Text("PLAY", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
