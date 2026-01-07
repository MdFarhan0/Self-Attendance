package `in`.hridayan.driftly.core.domain.repository

import `in`.hridayan.driftly.core.data.model.ClassScheduleEntity
import kotlinx.coroutines.flow.Flow

interface ClassScheduleRepository {
    fun getSchedulesForSubject(subjectId: Int): Flow<List<ClassScheduleEntity>>
    fun getSchedulesForDay(subjectId: Int, dayOfWeek: Int): Flow<List<ClassScheduleEntity>>
    fun getAllSchedules(): Flow<List<ClassScheduleEntity>>
    suspend fun insertSchedule(schedule: ClassScheduleEntity)
    suspend fun insertSchedules(schedules: List<ClassScheduleEntity>)
    suspend fun updateSchedule(schedule: ClassScheduleEntity)
    suspend fun deleteSchedule(schedule: ClassScheduleEntity)
    suspend fun deleteSchedulesForSubject(subjectId: Int)
    suspend fun deleteAllSchedules()
    fun getScheduleCount(subjectId: Int): Flow<Int>
    suspend fun getAllSchedulesOnce(): List<ClassScheduleEntity>
    suspend fun insertAllSchedules(schedules: List<ClassScheduleEntity>)
}
