package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "level_records")
data class LevelRecord(
    @PrimaryKey val levelId: String,
    val levelName: String,
    val difficulty: String,
    val bestPercent: Int = 0,
    val practicePercent: Int = 0,
    val attempts: Int = 0,
    val jumps: Int = 0,
    val isCompleted: Boolean = false,
    val isPracticeCompleted: Boolean = false,
    val starsAwarded: Int = 0,
    val coinsCollectedMask: Int = 0 // bitmask for 3 secret coins
)

@Entity(tableName = "custom_levels")
data class CustomLevel(
    @PrimaryKey val id: String,
    val title: String,
    val difficulty: String = "Normal",
    val bpm: Int = 135,
    val themeColorHex: String = "#00E5FF",
    val objectDataJson: String = "[]",
    val bestPercent: Int = 0,
    val attempts: Int = 0,
    val isCompleted: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "user_stats")
data class UserStats(
    @PrimaryKey val id: Int = 1,
    val totalJumps: Int = 0,
    val totalAttempts: Int = 0,
    val totalStars: Int = 0,
    val totalCoins: Int = 0,
    val selectedCubeIndex: Int = 0,
    val selectedShipIndex: Int = 0,
    val primaryColorHex: String = "#00FF66",
    val secondaryColorHex: String = "#FFCC00",
    val trailIndex: Int = 0,
    val sfxEnabled: Boolean = true,
    val musicEnabled: Boolean = true,
    val screenShakeEnabled: Boolean = true,
    val hapticsEnabled: Boolean = true
)
