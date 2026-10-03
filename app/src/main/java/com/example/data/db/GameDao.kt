package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface GameDao {
    @Query("SELECT * FROM level_records")
    fun getAllLevelRecords(): Flow<List<LevelRecord>>

    @Query("SELECT * FROM level_records WHERE levelId = :id LIMIT 1")
    fun getLevelRecord(id: String): Flow<LevelRecord?>

    @Query("SELECT * FROM level_records WHERE levelId = :id LIMIT 1")
    suspend fun getLevelRecordDirect(id: String): LevelRecord?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLevelRecord(record: LevelRecord)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertLevelRecords(records: List<LevelRecord>)

    @Query("SELECT * FROM custom_levels ORDER BY updatedAt DESC")
    fun getAllCustomLevels(): Flow<List<CustomLevel>>

    @Query("SELECT * FROM custom_levels WHERE id = :id LIMIT 1")
    fun getCustomLevel(id: String): Flow<CustomLevel?>

    @Query("SELECT * FROM custom_levels WHERE id = :id LIMIT 1")
    suspend fun getCustomLevelDirect(id: String): CustomLevel?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveCustomLevel(level: CustomLevel)

    @Query("DELETE FROM custom_levels WHERE id = :id")
    suspend fun deleteCustomLevel(id: String)

    @Query("SELECT * FROM user_stats WHERE id = 1 LIMIT 1")
    fun getUserStats(): Flow<UserStats?>

    @Query("SELECT * FROM user_stats WHERE id = 1 LIMIT 1")
    suspend fun getUserStatsDirect(): UserStats?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveUserStats(stats: UserStats)
}
