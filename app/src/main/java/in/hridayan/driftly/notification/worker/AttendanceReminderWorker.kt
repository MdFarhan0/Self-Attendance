package `in`.hridayan.driftly.notification.worker

import android.Manifest
import android.content.Context
import androidx.annotation.RequiresPermission
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.hilt.android.qualifiers.ApplicationContext
import `in`.hridayan.driftly.notification.NotificationSetup

class AttendanceReminderWorker(
    @param:ApplicationContext private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    override suspend fun doWork(): Result {
        return try {
            val isHoliday = `in`.hridayan.driftly.core.utils.HolidayHelper.isHolidayModeActive(applicationContext)
            if (!isHoliday) {
                NotificationSetup.showAttendanceReminderNotification(applicationContext)
            }
            Result.success()
        } catch (_: Exception) {
            Result.retry()
        }
    }
}
