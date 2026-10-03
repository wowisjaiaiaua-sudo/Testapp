package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.SoundEngine
import com.example.data.db.AppDatabase
import com.example.data.db.CustomLevel
import com.example.data.db.GameRepository
import com.example.data.db.LevelRecord
import com.example.data.db.UserStats
import com.example.game.GameObject
import com.example.game.LevelMetadata
import com.example.game.LevelTemplates
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

sealed class Screen {
    object MENU : Screen()
    object LEVEL_SELECT : Screen()
    data class GAME(
        val meta: LevelMetadata,
        val objects: List<GameObject>,
        val isPractice: Boolean,
        val customLevelId: String? = null
    ) : Screen()
    object ICON_KIT : Screen()
    data class LEVEL_EDITOR(val editingLevelId: String? = null) : Screen()
    object STATS : Screen()
}

class GameViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: GameRepository
    val soundEngine: SoundEngine = SoundEngine()

    private val _currentScreen = MutableStateFlow<Screen>(Screen.MENU)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    val userStats: StateFlow<UserStats>
    val levelRecords: StateFlow<List<LevelRecord>>
    val customLevels: StateFlow<List<CustomLevel>>

    init {
        val database = AppDatabase.getDatabase(application)
        repository = GameRepository(database.gameDao())

        userStats = repository.userStats
            .map { it ?: UserStats() }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserStats())

        levelRecords = repository.allLevelRecords
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        customLevels = repository.customLevels
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    }

    fun navigateTo(screen: Screen) {
        if (screen !is Screen.GAME) {
            soundEngine.stopBgm()
        }
        _currentScreen.value = screen
    }

    fun startOfficialLevel(meta: LevelMetadata, practice: Boolean = false) {
        val objects = LevelTemplates.getLevelObjects(meta.id)
        _currentScreen.value = Screen.GAME(meta, objects, practice)
    }

    fun startCustomLevel(custom: CustomLevel, practice: Boolean = false) {
        val objects = LevelTemplates.deserializeObjects(custom.objectDataJson)
        val meta = LevelMetadata(
            id = custom.id,
            title = custom.title,
            difficulty = custom.difficulty,
            stars = 2,
            bpm = custom.bpm,
            bgColorHex = "#120B24",
            floorColorHex = "#21153E",
            accentColorHex = custom.themeColorHex,
            totalLength = maxOf(120f, (objects.maxOfOrNull { it.x } ?: 50f) + 15f)
        )
        _currentScreen.value = Screen.GAME(meta, objects, practice, customLevelId = custom.id)
    }

    fun onGameSessionEnd(
        levelId: String,
        levelName: String,
        difficulty: String,
        percent: Int,
        isPractice: Boolean,
        isCompleted: Boolean,
        stars: Int,
        coinsMask: Int,
        jumps: Int,
        customLevelId: String? = null
    ) {
        viewModelScope.launch {
            if (customLevelId != null) {
                repository.updateCustomLevelProgress(customLevelId, percent, isCompleted)
            } else {
                repository.updateLevelProgress(
                    levelId = levelId,
                    levelName = levelName,
                    difficulty = difficulty,
                    percent = percent,
                    isPractice = isPractice,
                    isCompleted = isCompleted,
                    stars = stars,
                    newCoinsMask = coinsMask,
                    jumpsInSession = jumps
                )
            }
        }
    }

    fun saveCustomLevel(
        id: String?,
        title: String,
        bpm: Int,
        themeHex: String,
        objects: List<GameObject>
    ) {
        viewModelScope.launch {
            val levelId = id ?: UUID.randomUUID().toString()
            val json = LevelTemplates.serializeObjects(objects)
            val custom = CustomLevel(
                id = levelId,
                title = title.ifBlank { "My Level" },
                bpm = bpm,
                themeColorHex = themeHex,
                objectDataJson = json,
                updatedAt = System.currentTimeMillis()
            )
            repository.saveCustomLevel(custom)
            navigateTo(Screen.LEVEL_SELECT)
        }
    }

    fun deleteCustomLevel(id: String) {
        viewModelScope.launch {
            repository.deleteCustomLevel(id)
        }
    }

    fun updateCustomizer(
        cubeIndex: Int? = null,
        shipIndex: Int? = null,
        primaryHex: String? = null,
        secondaryHex: String? = null
    ) {
        viewModelScope.launch {
            val cur = userStats.value
            val updated = cur.copy(
                selectedCubeIndex = cubeIndex ?: cur.selectedCubeIndex,
                selectedShipIndex = shipIndex ?: cur.selectedShipIndex,
                primaryColorHex = primaryHex ?: cur.primaryColorHex,
                secondaryColorHex = secondaryHex ?: cur.secondaryColorHex
            )
            repository.updateUserStats(updated)
        }
    }

    fun toggleSfx() {
        viewModelScope.launch {
            val cur = userStats.value
            val newVal = !cur.sfxEnabled
            soundEngine.sfxEnabled = newVal
            repository.updateUserStats(cur.copy(sfxEnabled = newVal))
        }
    }

    fun toggleMusic() {
        viewModelScope.launch {
            val cur = userStats.value
            val newVal = !cur.musicEnabled
            soundEngine.musicEnabled = newVal
            if (!newVal) {
                soundEngine.stopBgm()
            }
            repository.updateUserStats(cur.copy(musicEnabled = newVal))
        }
    }

    fun toggleScreenShake() {
        viewModelScope.launch {
            val cur = userStats.value
            repository.updateUserStats(cur.copy(screenShakeEnabled = !cur.screenShakeEnabled))
        }
    }

    fun toggleHaptics() {
        viewModelScope.launch {
            val cur = userStats.value
            repository.updateUserStats(cur.copy(hapticsEnabled = !cur.hapticsEnabled))
        }
    }

    override fun onCleared() {
        super.onCleared()
        soundEngine.release()
    }
}
