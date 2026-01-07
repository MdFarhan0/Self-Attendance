package `in`.hridayan.driftly.notification

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import `in`.hridayan.driftly.core.domain.model.toDomain
import `in`.hridayan.driftly.core.domain.repository.ClassScheduleRepository
import `in`.hridayan.driftly.core.domain.repository.SubjectRepository
import kotlinx.coroutines.flow.first

@InstallIn(SingletonComponent::class)
@EntryPoint
interface RescheduleWorkerEntryPoint {
    fun subjectRepository(): SubjectRepository
    fun classScheduleRepository(): ClassScheduleRepository
}

class RescheduleAlarmsWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        try {
            val entryPoint = EntryPointAccessors.fromApplication(applicationContext, RescheduleWorkerEntryPoint::class.java)
            val subjectRepository = entryPoint.subjectRepository()
            val classScheduleRepository = entryPoint.classScheduleRepository()

            val subjects = subjectRepository.getAllSubjects().first()
            
            subjects.forEach { subject ->
                val schedules = classScheduleRepository.getSchedulesForSubject(subject.id).first().map { it.toDomain() }
                
                if (schedules.isNotEmpty()) {
                    // This method handles canceling old and scheduling new for the subject
                    ClassNotificationScheduler.scheduleAllClassNotifications(
                        context = applicationContext,
                        subjectId = subject.id,
                        subjectName = subject.subject,
                        schedules = schedules
                    )
                }
            }
            return Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            return Result.retry()
        }
    }
}
