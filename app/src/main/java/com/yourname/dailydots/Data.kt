package com.yourname.dailydots

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "habits")
data class Habit(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val emoji: String,
    val archived: Boolean = false
)

/** One row = habit completed on that day (epochDay = days since 1970-01-01). */
@Entity(tableName = "logs", primaryKeys = ["habitId", "epochDay"])
data class HabitLog(val habitId: Long, val epochDay: Long)

@Dao
abstract class HabitDao {
    @Query("SELECT * FROM habits WHERE archived = 0 ORDER BY id")
    abstract fun habits(): Flow<List<Habit>>

    @Query("SELECT * FROM habits WHERE archived = 0 ORDER BY id")
    abstract suspend fun habitsOnce(): List<Habit>

    @Query("SELECT * FROM logs")
    abstract fun logs(): Flow<List<HabitLog>>

    @Query("SELECT habitId FROM logs WHERE epochDay = :day")
    abstract suspend fun doneOn(day: Long): List<Long>

    @Query("SELECT COUNT(*) FROM logs WHERE habitId = :id AND epochDay = :day")
    abstract suspend fun isDone(id: Long, day: Long): Int

    @Query("SELECT COUNT(*) FROM habits WHERE archived = 0")
    abstract suspend fun activeCount(): Int

    @Insert
    abstract suspend fun insert(habit: Habit): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract suspend fun addLog(log: HabitLog)

    @Query("DELETE FROM logs WHERE habitId = :id AND epochDay = :day")
    abstract suspend fun removeLog(id: Long, day: Long)

    @Query("UPDATE habits SET archived = 1 WHERE id = :id")
    abstract suspend fun archive(id: Long)

    @Transaction
    open suspend fun toggle(id: Long, day: Long) {
        if (isDone(id, day) > 0) removeLog(id, day) else addLog(HabitLog(id, day))
    }
}

@Database(entities = [Habit::class, HabitLog::class], version = 1, exportSchema = false)
abstract class AppDb : RoomDatabase() {
    abstract fun dao(): HabitDao

    companion object {
        @Volatile private var instance: AppDb? = null

        fun get(context: Context): AppDb = instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(
                context.applicationContext, AppDb::class.java, "dailydots.db"
            ).build().also { instance = it }
        }
    }
}
