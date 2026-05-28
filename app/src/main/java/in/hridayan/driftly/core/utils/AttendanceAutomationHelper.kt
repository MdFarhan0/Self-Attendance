package `in`.hridayan.driftly.core.utils

import `in`.hridayan.driftly.core.data.model.AttendanceEntity
import `in`.hridayan.driftly.core.domain.model.AttendanceStatus
import `in`.hridayan.driftly.core.domain.repository.AttendanceRepository
import `in`.hridayan.driftly.core.domain.repository.ClassScheduleRepository
import `in`.hridayan.driftly.core.domain.repository.SubjectRepository
import `in`.hridayan.driftly.settings.data.local.SettingsKeys
import `in`.hridayan.driftly.settings.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.first
import java.time.LocalDate

suspend fun runAutoHandleUnmarkedDays(
    settingsRepository: SettingsRepository,
    subjectRepository: SubjectRepository,
    attendanceRepository: AttendanceRepository,
    classScheduleRepository: ClassScheduleRepository
) {
    val isEnabled = settingsRepository.getBoolean(SettingsKeys.AUTO_HANDLE_UNMARKED_DAYS).first()
    if (!isEnabled) return
    
    val behavior = settingsRepository.getInt(SettingsKeys.AUTO_HANDLE_UNMARKED_DAYS_BEHAVIOR).first()
    if (behavior == 2) return // Do Nothing
    
    val status = when (behavior) {
        0 -> AttendanceStatus.PRESENT // Mark all subjects as Attended
        1 -> AttendanceStatus.ABSENT  // Mark all subjects as Absent
        else -> return
    }
    
    val subjects = subjectRepository.getAllSubjectsOnce()
    if (subjects.isEmpty()) return
    
    val schedules = classScheduleRepository.getAllSchedulesOnce()
    if (schedules.isEmpty()) return
    
    val today = LocalDate.now()
    val attendanceToInsert = mutableListOf<AttendanceEntity>()
    
    val existingAttendance = attendanceRepository.getAllAttendancesOnce()
    val existingSet = existingAttendance.map { it.subjectId to it.date }.toSet()
    
    for (i in 1..30) {
        val pastDate = today.minusDays(i.toLong())
        val dayOfWeek = pastDate.dayOfWeek.value // 1 = Monday, ..., 7 = Sunday
        
        val daySchedules = schedules.filter { it.isEnabled && it.dayOfWeek == dayOfWeek }
        if (daySchedules.isEmpty()) continue
        
        for (schedule in daySchedules) {
            val key = schedule.subjectId to pastDate.toString()
            if (!existingSet.contains(key)) {
                attendanceToInsert.add(
                    AttendanceEntity(
                        subjectId = schedule.subjectId,
                        date = pastDate.toString(),
                        status = status
                    )
                )
            }
        }
    }
    
    if (attendanceToInsert.isNotEmpty()) {
        attendanceRepository.insertAllAttendances(attendanceToInsert)
    }
}
