package `in`.hridayan.driftly.notification

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import `in`.hridayan.driftly.core.data.model.AttendanceEntity
import `in`.hridayan.driftly.core.domain.model.AttendanceStatus
import `in`.hridayan.driftly.core.domain.repository.AttendanceRepository
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

        // Get repository via EntryPoint
        val repository = EntryPointAccessors.fromApplication(
            applicationContext,
            MarkAttendanceWorkerEntryPoint::class.java
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
            // No month/year needed as per Entity definition
        )

        try {
            repository.insertAttendance(record)
            return Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            return Result.retry()
        }
    }
}
