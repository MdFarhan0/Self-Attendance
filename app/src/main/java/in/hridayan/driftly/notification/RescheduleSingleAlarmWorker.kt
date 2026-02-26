package `in`.hridayan.driftly.notification

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class RescheduleSingleAlarmWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val subjectId = inputData.getInt("subjectId", -1)
        val scheduleId = inputData.getInt("scheduleId", -1)
        val subjectName = inputData.getString("subjectName") ?: return Result.failure()
        val startTime = inputData.getString("startTime") ?: return Result.failure()
        val endTime = inputData.getString("endTime") ?: return Result.failure()
        val location = inputData.getString("location")
        val dayOfWeek = inputData.getInt("dayOfWeek", 1)

        if (subjectId == -1 || scheduleId == -1) return Result.failure()

        ClassNotificationScheduler.scheduleNextWeek(
            context = applicationContext,
            subjectId = subjectId,
            scheduleId = scheduleId,
            subjectName = subjectName,
            startTime = startTime,
            endTime = endTime,
            location = location,
            dayOfWeek = dayOfWeek
        )

        return Result.success()
    }
}
