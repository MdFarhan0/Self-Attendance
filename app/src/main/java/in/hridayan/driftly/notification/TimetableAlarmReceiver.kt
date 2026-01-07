package `in`.hridayan.driftly.notification

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import `in`.hridayan.driftly.R

/**
 * BroadcastReceiver that fires at the exact scheduled time.
 */
class TimetableAlarmReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "TimetableAlarmReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        // 1. Mandatory Debug Log
        Log.e("ALARM_DEBUG", "🔥 Timetable alarm RECEIVED 🔥")
        Log.d(TAG, "Action: ${intent.action}")

        val appContext = context.applicationContext
        
        // Extract data
        val scheduleId = intent.getIntExtra("scheduleId", -1)
        val subjectId = intent.getIntExtra("subjectId", -1)
        val subjectName = intent.getStringExtra("subjectName") ?: "Unknown"
        val startTime = intent.getStringExtra("startTime") ?: ""
        val endTime = intent.getStringExtra("endTime") ?: ""
        val location = intent.getStringExtra("location")
        val type = intent.getStringExtra("type") ?: "START"
        val dayOfWeek = intent.getIntExtra("dayOfWeek", -1)

        if (scheduleId == -1 || subjectId == -1) {
            Log.e(TAG, "❌ Invalid IDs, aborting.")
            return
        }

        // Check settings
        val sharedPrefs = appContext.getSharedPreferences("driftly_settings", Context.MODE_PRIVATE)
        val notificationsEnabled = sharedPrefs.getBoolean("enable_timetable_notifications", true)
        
        if (!notificationsEnabled) {
            Log.d(TAG, "⚠️ Notifications disabled by user.")
            rescheduleForNextWeek(appContext, intent)
            return
        }

        // 2. Runtime Permission Check (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(appContext, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                Log.e(TAG, "❌ POST_NOTIFICATIONS permission missing! Notification dropped.")
                rescheduleForNextWeek(appContext, intent)
                return
            }
        }

        // 3. Show Notification
        if (type == "START") {
            try {
                showNotification(appContext, subjectId, subjectName, startTime, endTime, location, scheduleId)
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error showing notification", e)
            }
        }

        // 4. Reschedule
        rescheduleForNextWeek(appContext, intent)
    }

    private fun showNotification(
        context: Context,
        subjectId: Int,
        subjectName: String,
        startTime: String,
        endTime: String,
        location: String?,
        scheduleId: Int
    ) {
        val formattedStart = `in`.hridayan.driftly.core.utils.TimeUtils.format24To12Hour(startTime)
        val formattedEnd = `in`.hridayan.driftly.core.utils.TimeUtils.format24To12Hour(endTime)
        val duration = `in`.hridayan.driftly.core.utils.TimeUtils.formatDuration(
            `in`.hridayan.driftly.core.utils.TimeUtils.calculateDuration(startTime, endTime)
        )

        val message = buildString {
            append("$formattedStart - $formattedEnd ($duration)")
            if (!location.isNullOrBlank()) {
                append("\n📍 $location")
            }
        }

        // Use valid app icon
        val iconRes = R.drawable.ic_notifications

        `in`.hridayan.driftly.notification.helper.NotificationHelper.showNotificationWithActions(
            context = context,
            channelId = NotificationSetup.TIMETABLE_CHANNEL_ID,
            channelName = "Class Timetable",
            channelDescription = "Notifications for scheduled classes",
            notificationId = scheduleId,
            title = "Class Started: $subjectName",
            message = message,
            smallIconResId = iconRes,
            subjectId = subjectId,
            scheduleId = scheduleId
        )
    }

    private fun rescheduleForNextWeek(context: Context, intent: Intent) {
        val scheduleId = intent.getIntExtra("scheduleId", -1)
        val subjectId = intent.getIntExtra("subjectId", -1)
        val subjectName = intent.getStringExtra("subjectName") ?: return
        val startTime = intent.getStringExtra("startTime") ?: return
        val endTime = intent.getStringExtra("endTime") ?: return
        val location = intent.getStringExtra("location")
        val type = intent.getStringExtra("type") ?: "START"
        val dayOfWeek = intent.getIntExtra("dayOfWeek", -1)

        if (scheduleId == -1 || dayOfWeek == -1) return

        Log.d(TAG, "🔄 Rescheduling $type for next week...")
        TimetableAlarmScheduler.scheduleAlarm(
            context = context,
            scheduleId = scheduleId,
            subjectId = subjectId,
            subjectName = subjectName,
            dayOfWeek = dayOfWeek,
            startTime = startTime,
            endTime = endTime,
            location = location,
            type = type
        )
    }
}
