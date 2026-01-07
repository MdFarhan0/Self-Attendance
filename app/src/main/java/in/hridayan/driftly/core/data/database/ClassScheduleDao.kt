package `in`.hridayan.driftly.core.data.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import `in`.hridayan.driftly.core.data.model.ClassScheduleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ClassScheduleDao {
    @Query("SELECT * FROM class_schedules WHERE subjectId = :subjectId AND isEnabled = 1 ORDER BY dayOfWeek ASC, startTime ASC")
    fun getSchedulesForSubject(subjectId: Int): Flow<List<ClassScheduleEntity>>

    @Query("SELECT * FROM class_schedules WHERE subjectId = :subjectId AND dayOfWeek = :dayOfWeek AND isEnabled = 1")
    fun getSchedulesForDay(subjectId: Int, dayOfWeek: Int): Flow<List<ClassScheduleEntity>>

    @Query("SELECT * FROM class_schedules WHERE isEnabled = 1 ORDER BY dayOfWeek ASC, startTime ASC")
    fun getAllSchedules(): Flow<List<ClassScheduleEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchedule(schedule: ClassScheduleEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchedules(schedules: List<ClassScheduleEntity>)

    @Update
    suspend fun updateSchedule(schedule: ClassScheduleEntity)

    @Delete
    suspend fun deleteSchedule(schedule: ClassScheduleEntity)

    @Query("DELETE FROM class_schedules WHERE subjectId = :subjectId")
    suspend fun deleteSchedulesForSubject(subjectId: Int)

    @Query("DELETE FROM class_schedules")
    suspend fun deleteAllSchedules()

    @Query("SELECT COUNT(*) FROM class_schedules WHERE subjectId = :subjectId AND isEnabled = 1")
    fun getScheduleCount(subjectId: Int): Flow<Int>

    @Query("SELECT * FROM class_schedules")
    suspend fun getAllSchedulesOnce(): List<ClassScheduleEntity>
}
