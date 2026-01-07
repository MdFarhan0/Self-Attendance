package `in`.hridayan.driftly.notification.worker

import android.Manifest
import android.content.Context
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import `in`.hridayan.driftly.core.domain.repository.ClassScheduleRepository
import `in`.hridayan.driftly.core.domain.repository.SubjectRepository
import `in`.hridayan.driftly.core.domain.model.toDomain
import `in`.hridayan.driftly.notification.NotificationSetup
import kotlinx.coroutines.flow.first
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.DayOfWeek

@EntryPoint
@InstallIn(SingletonComponent::class)
interface TimetableWorkerEntryPoint {
    fun subjectRepository(): SubjectRepository
    fun classScheduleRepository(): ClassScheduleRepository
}

class TimetableNotificationWorker(
    @param:ApplicationContext private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    override suspend fun doWork(): Result {
        return try {
            Log.d("TimetableWorker", "=== Checking for classes starting soon ===")
            
            val entryPoint = EntryPointAccessors.fromApplication(
                applicationContext,
                TimetableWorkerEntryPoint::class.java
            )
            val subjectRepository = entryPoint.subjectRepository()
            val classScheduleRepository = entryPoint.classScheduleRepository()

            val now = LocalDateTime.now()
            val currentTime = now.toLocalTime()
            val currentDay = now.dayOfWeek.value // 1 = Monday, 7 = Sunday
            
            Log.d("TimetableWorker", "Current time: $currentTime, Day: $currentDay")

            // Get all subjects
            val subjects = subjectRepository.getAllSubjects().first()
            
            for (subject in subjects) {
                // Get all schedules for this subject
                val schedules = classScheduleRepository.getSchedulesForSubject(subject.id)
                    .first()
                    .map { it.toDomain() }

                for (schedule in schedules) {
                    // Check if this schedule is for today
                    if (schedule.dayOfWeek == currentDay) {
                        val classStartTime = LocalTime.parse(schedule.startTime)
                        val classEndTime = LocalTime.parse(schedule.endTime)
                        
                        // Check if class is starting within the next 5 minutes or currently ongoing
                        val minutesUntilStart = java.time.Duration.between(currentTime, classStartTime).toMinutes()
                        val minutesSinceStart = java.time.Duration.between(classStartTime, currentTime).toMinutes()
                        
                        Log.d("TimetableWorker", "Subject: ${subject.subject}, Minutes until start: $minutesUntilStart")
                        
                        // Show notification if class starts within 5 minutes or just started (within 5 min)
                        if (minutesUntilStart in -5..5) {
                            Log.d("TimetableWorker", "Showing notification for ${subject.subject}")
                            NotificationSetup.showTimetableNotification(
                                context = applicationContext,
                                subjectId = subject.id,
                                subjectName = subject.subject,
                                startTime = schedule.startTime,
                                endTime = schedule.endTime,
                                location = schedule.location,
                                scheduleId = schedule.id
                            )
                        }
                    }
                }
            }
            
            Log.d("TimetableWorker", "Check complete")
            Result.success()
        } catch (e: Exception) {
            Log.e("TimetableWorker", "Error checking timetable", e)
            e.printStackTrace()
            Result.retry()
        }
    }
}
