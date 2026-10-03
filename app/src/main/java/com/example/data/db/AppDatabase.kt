package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [LevelRecord::class, CustomLevel::class, UserStats::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun gameDao(): GameDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "geometry_dash.db"
                ).addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        CoroutineScope(Dispatchers.IO).launch {
                            val database = getDatabase(context)
                            val dao = database.gameDao()
                            // Seed default user stats
                            dao.saveUserStats(UserStats())
                            // Seed official level records
                            val initialLevels = listOf(
                                LevelRecord("stereo_madness", "Stereo Madness", "Easy", starsAwarded = 1),
                                LevelRecord("back_on_track", "Back On Track", "Normal", starsAwarded = 2),
                                LevelRecord("polargeist", "Polargeist", "Hard", starsAwarded = 3),
                                LevelRecord("dry_out", "Dry Out", "Harder", starsAwarded = 4),
                                LevelRecord("jumper", "Jumper", "Insane", starsAwarded = 5)
                            )
                            dao.insertLevelRecords(initialLevels)
                        }
                    }
                }).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
