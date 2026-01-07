# Timetable Notification System - Part 3

This document contains the background worker for marking attendance, constants, and the ViewModel integration.

## 8. MarkAttendanceWorker.kt
Handles the database update when a user clicks "Attended" or "Missed" on the notification.

```kotlin
package in.hridayan.driftly.notification

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import in.hridayan.driftly.core.data.model.AttendanceEntity
import in.hridayan.driftly.core.domain.model.AttendanceStatus
import in.hridayan.driftly.core.domain.repository.AttendanceRepository
import java.time.LocalDate

class MarkAttendanceWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface MarkAttendanceWorkerEntryPoint {
        fun getAttendanceRepository(): AttendanceRepository
    }

    override suspend fun doWork(): Result {
        val subjectId = inputData.getInt("subjectId", -1)
        val action = inputData.getString("action") ?: return Result.failure()

        if (subjectId == -1) return Result.failure()

        val repository = EntryPointAccessors.fromApplication(
            applicationContext, MarkAttendanceWorkerEntryPoint::class.java
        ).getAttendanceRepository()

        val status = when (action) {
            "ATTENDED" -> AttendanceStatus.PRESENT
            "MISSED" -> AttendanceStatus.ABSENT
            else -> return Result.failure()
        }

        val date = LocalDate.now()
        val record = AttendanceEntity(
            subjectId = subjectId,
            date = date.toString(),
            status = status
        )

        return try {
            repository.insertAttendance(record)
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
```

## 9. HomeViewModel.kt (Alarms Integration)
This shows how the UI triggers the scheduling when a user saves their timetable.

```kotlin
// Inside HomeViewModel.kt

fun saveSchedulesForSubject(subjectId: Int, schedules: List<ClassSchedule>) {
    viewModelScope.launch {
        // 1. Get current schedules to handle deletions
        val currentSchedules = classScheduleRepository.getSchedulesForSubject(subjectId).first().map { it.toDomain() }
        val newScheduleIds = schedules.map { it.id }.toSet()
        val schedulesToDelete = currentSchedules.filter { it.id != 0 && it.id !in newScheduleIds }

        // 2. Cancel alarms for deleted schedules
        schedulesToDelete.forEach { schedule ->
            TimetableAlarmScheduler.cancelClassAlarms(context, schedule.id)
            classScheduleRepository.deleteSchedule(schedule.toEntity())
        }

        // 3. Save new/updated schedules to DB
        if (schedules.isNotEmpty()) {
            val entitiesToUpsert = schedules.map { it.copy(subjectId = subjectId).toEntity() }
            classScheduleRepository.insertSchedules(entitiesToUpsert)
            
            // 4. Fetch updated list with generated IDs and schedule exact alarms
            val updatedSchedules = classScheduleRepository.getSchedulesForSubject(subjectId).first().map { it.toDomain() }
            updatedSchedules.forEach { schedule ->
                TimetableAlarmScheduler.scheduleClassAlarms(
                    context = context,
                    scheduleId = schedule.id,
                    subjectId = subjectId,
                    subjectName = getSubjectById(subjectId).first().subject,
                    dayOfWeek = schedule.dayOfWeek,
                    startTime = schedule.startTime,
                    endTime = schedule.endTime,
                    location = schedule.location
                )
            }
        }
    }
}
```

## 10. SettingsKeys.kt (Preference Key)
The key used to toggle timetable notifications in the app settings.

```kotlin
// Inside SettingsKeys enum class
ENABLE_TIMETABLE_NOTIFICATIONS(true),
```

---
**Summary of Implementation:**
1. **Trigger:** `HomeViewModel` calls `TimetableAlarmScheduler.scheduleClassAlarms`.
2. **Scheduling:** `TimetableAlarmScheduler` uses `AlarmManager.setExactAndAllowWhileIdle`.
3. **Execution:** `TimetableAlarmReceiver` wakes up the device, triggers `NotificationSetup`.
4. **Notification:** Displayed via `NotificationHelper` with "Attended"/"Missed" buttons.
5. **Marking:** `AttendanceActionReceiver` enqueues `MarkAttendanceWorker` to save to DB.
6. **Persistence:** `BootCompletedReceiver` ensures alarms are rebuilt after device restart.
```
