package com.focusx.app.data

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "focus_sessions")
data class FocusSession(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val minutes: Int,
    val completedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "blocked_apps")
data class BlockedApp(
    @PrimaryKey val packageName: String,
    val displayName: String,
    val enabled: Boolean = true
)

@Entity(tableName = "blocked_sites")
data class BlockedSite(
    @PrimaryKey val host: String,
    val enabled: Boolean = true
)

@Entity(tableName = "focus_schedules")
data class FocusSchedule(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val startHour: Int,
    val startMinute: Int,
    val durationMinutes: Int,
    val enabled: Boolean = true
)

@Dao
interface FocusDao {
    @Insert suspend fun addSession(value: FocusSession)
    @Query("SELECT * FROM focus_sessions ORDER BY completedAt DESC")
    fun sessions(): Flow<List<FocusSession>>
    @Query("SELECT COALESCE(SUM(minutes), 0) FROM focus_sessions WHERE completedAt >= :from")
    fun minutesSince(from: Long): Flow<Int>

    @Query("SELECT * FROM blocked_apps ORDER BY displayName")
    fun blockedApps(): Flow<List<BlockedApp>>
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveBlockedApp(value: BlockedApp)
    @Query("DELETE FROM blocked_apps WHERE packageName = :pkg")
    suspend fun removeBlockedApp(pkg: String)

    @Query("SELECT * FROM blocked_sites ORDER BY host")
    fun blockedSites(): Flow<List<BlockedSite>>
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSite(value: BlockedSite)
    @Query("DELETE FROM blocked_sites WHERE host = :host")
    suspend fun removeSite(host: String)

    @Query("SELECT * FROM focus_schedules ORDER BY startHour, startMinute")
    fun schedules(): Flow<List<FocusSchedule>>
    @Insert suspend fun addSchedule(value: FocusSchedule)
    @Query("DELETE FROM focus_schedules WHERE id = :id")
    suspend fun removeSchedule(id: Long)
}

@Database(
    entities = [FocusSession::class, BlockedApp::class, BlockedSite::class, FocusSchedule::class],
    version = 1,
    exportSchema = false
)
abstract class FocusDatabase : RoomDatabase() {
    abstract fun dao(): FocusDao

    companion object {
        @Volatile private var instance: FocusDatabase? = null
        fun get(context: Context): FocusDatabase = instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(
                context.applicationContext, FocusDatabase::class.java, "focusx.db"
            ).build().also { instance = it }
        }
    }
}
