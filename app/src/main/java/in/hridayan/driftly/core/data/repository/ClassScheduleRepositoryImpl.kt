package `in`.hridayan.driftly.core.data.repository

import `in`.hridayan.driftly.core.data.database.ClassScheduleDao
import `in`.hridayan.driftly.core.data.model.ClassScheduleEntity
import `in`.hridayan.driftly.core.domain.repository.ClassScheduleRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ClassScheduleRepositoryImpl @Inject constructor(
    private val classScheduleDao: ClassScheduleDao
) : ClassScheduleRepository {
    override fun getSchedulesForSubject(subjectId: Int): Flow<List<ClassScheduleEntity>> {
        return classScheduleDao.getSchedulesForSubject(subjectId)
    }

    override fun getSchedulesForDay(subjectId: Int, dayOfWeek: Int): Flow<List<ClassScheduleEntity>> {
        return classScheduleDao.getSchedulesForDay(subjectId, dayOfWeek)
    }

    override fun getAllSchedules(): Flow<List<ClassScheduleEntity>> {
        return classScheduleDao.getAllSchedules()
    }

    override suspend fun insertSchedule(schedule: ClassScheduleEntity) {
        classScheduleDao.insertSchedule(schedule)
    }

    override suspend fun insertSchedules(schedules: List<ClassScheduleEntity>) {
        classScheduleDao.insertSchedules(schedules)
    }

    override suspend fun updateSchedule(schedule: ClassScheduleEntity) {
        classScheduleDao.updateSchedule(schedule)
    }

    override suspend fun deleteSchedule(schedule: ClassScheduleEntity) {
        classScheduleDao.deleteSchedule(schedule)
    }

    override suspend fun deleteSchedulesForSubject(subjectId: Int) {
        classScheduleDao.deleteSchedulesForSubject(subjectId)
    }

    override suspend fun deleteAllSchedules() {
        classScheduleDao.deleteAllSchedules()
    }

    override fun getScheduleCount(subjectId: Int): Flow<Int> {
        return classScheduleDao.getScheduleCount(subjectId)
    }

    override suspend fun getAllSchedulesOnce(): List<ClassScheduleEntity> {
        return classScheduleDao.getAllSchedulesOnce()
    }

    override suspend fun insertAllSchedules(schedules: List<ClassScheduleEntity>) {
        classScheduleDao.insertSchedules(schedules)
    }
}
