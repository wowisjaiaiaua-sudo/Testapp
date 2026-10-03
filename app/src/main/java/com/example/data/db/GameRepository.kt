package com.example.data.db

import kotlinx.coroutines.flow.Flow

class GameRepository(private val dao: GameDao) {

    val allLevelRecords: Flow<List<LevelRecord>> = dao.getAllLevelRecords()
    val customLevels: Flow<List<CustomLevel>> = dao.getAllCustomLevels()
    val userStats: Flow<UserStats?> = dao.getUserStats()

    fun getLevelRecord(levelId: String): Flow<LevelRecord?> = dao.getLevelRecord(levelId)

    suspend fun getLevelRecordDirect(levelId: String): LevelRecord? = dao.getLevelRecordDirect(levelId)

    suspend fun updateLevelProgress(
        levelId: String,
        levelName: String,
        difficulty: String,
        percent: Int,
        isPractice: Boolean,
        isCompleted: Boolean,
        stars: Int,
        newCoinsMask: Int,
        jumpsInSession: Int
    ) {
        val existing = dao.getLevelRecordDirect(levelId) ?: LevelRecord(
            levelId = levelId,
            levelName = levelName,
            difficulty = difficulty,
            starsAwarded = stars
        )

        val newBest = if (isPractice) existing.bestPercent else maxOf(existing.bestPercent, percent)
        val newPractice = if (isPractice) maxOf(existing.practicePercent, percent) else existing.practicePercent
        val nowCompleted = existing.isCompleted || (!isPractice && isCompleted)
        val nowPracticeCompleted = existing.isPracticeCompleted || (isPractice && isCompleted)
        val combinedCoins = existing.coinsCollectedMask or newCoinsMask

        val updatedRecord = existing.copy(
            bestPercent = newBest,
            practicePercent = newPractice,
            attempts = existing.attempts + 1,
            jumps = existing.jumps + jumpsInSession,
            isCompleted = nowCompleted,
            isPracticeCompleted = nowPracticeCompleted,
            coinsCollectedMask = combinedCoins
        )
        dao.insertLevelRecord(updatedRecord)

        // Also update UserStats
        val stats = dao.getUserStatsDirect() ?: UserStats()
        var addedStars = 0
        if (!existing.isCompleted && nowCompleted) {
            addedStars = stars
        }
        var newCoinsGained = 0
        val oldCoinCount = Integer.bitCount(existing.coinsCollectedMask)
        val newCoinCount = Integer.bitCount(combinedCoins)
        if (newCoinCount > oldCoinCount) {
            newCoinsGained = newCoinCount - oldCoinCount
        }

        dao.saveUserStats(
            stats.copy(
                totalAttempts = stats.totalAttempts + 1,
                totalJumps = stats.totalJumps + jumpsInSession,
                totalStars = stats.totalStars + addedStars,
                totalCoins = stats.totalCoins + newCoinsGained
            )
        )
    }

    suspend fun updateCustomLevelProgress(id: String, percent: Int, isCompleted: Boolean) {
        val existing = dao.getCustomLevelDirect(id) ?: return
        val updated = existing.copy(
            bestPercent = maxOf(existing.bestPercent, percent),
            attempts = existing.attempts + 1,
            isCompleted = existing.isCompleted || isCompleted,
            updatedAt = System.currentTimeMillis()
        )
        dao.saveCustomLevel(updated)

        val stats = dao.getUserStatsDirect() ?: UserStats()
        dao.saveUserStats(
            stats.copy(
                totalAttempts = stats.totalAttempts + 1
            )
        )
    }

    suspend fun saveCustomLevel(level: CustomLevel) = dao.saveCustomLevel(level)

    suspend fun deleteCustomLevel(id: String) = dao.deleteCustomLevel(id)

    suspend fun updateUserStats(stats: UserStats) = dao.saveUserStats(stats)
}
