package `in`.hridayan.driftly.notification

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.datastore.preferences.core.booleanPreferencesKey
import `in`.hridayan.driftly.core.utils.TimeUtils
import `in`.hridayan.driftly.settings.data.local.SettingsKeys
import `in`.hridayan.driftly.settings.data.local.provider.settingsDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class ClassAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        android.util.Log.d("ClassAlarmReceiver", "=== ALARM RECEIVED ===")
        android.util.Log.d("ClassAlarmReceiver", "Action: ${intent.action}")
        android.util.Log.d("ClassAlarmReceiver", "Subject: ${intent.getStringExtra("subjectName")}")
        
        if (intent.action == Intent.ACTION_BOOT_COMPLETED || intent.action == "android.app.action.SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED" || intent.action == Intent.ACTION_MY_PACKAGE_REPLACED) {
            android.util.Log.d("ClassAlarmReceiver", "Handling boot/permission change, scheduling worker")
            val request = androidx.work.OneTimeWorkRequestBuilder<RescheduleAlarmsWorker>().build()
            androidx.work.WorkManager.getInstance(context).enqueue(request)
            return
        }

        val subjectName = intent.getStringExtra("subjectName") ?: return
        val startTime = intent.getStringExtra("startTime") ?: return
        val endTime = intent.getStringExtra("endTime") ?: return
        val location = intent.getStringExtra("location")
        val subjectId = intent.getIntExtra("subjectId", -1)
        val scheduleId = intent.getIntExtra("scheduleId", -1)

        if (subjectId == -1 || scheduleId == -1) return

        // Check verification in a Coroutine
        CoroutineScope(Dispatchers.IO).launch {
            val settings = context.settingsDataStore.data.first()
            val isEnabled = settings[booleanPreferencesKey(SettingsKeys.ENABLE_TIMETABLE_NOTIFICATIONS.name)] ?: true
            
            android.util.Log.d("ClassAlarmReceiver", "Timetable notifications enabled: $isEnabled")
            
            if (isEnabled) {
                android.util.Log.d("ClassAlarmReceiver", "Showing notification for $subjectName")
                showNotification(context, subjectId, scheduleId, subjectName, startTime, endTime, location)
            } else {
                android.util.Log.w("ClassAlarmReceiver", "Notifications disabled in settings, skipping")
            }
            
            // Re-schedule for next week
            android.util.Log.d("ClassAlarmReceiver", "Rescheduling for next week")
            ClassNotificationScheduler.scheduleNextWeek(context, subjectId, scheduleId, subjectName, startTime, endTime, location, intent.getIntExtra("dayOfWeek", 1))
        }
    }

    @SuppressLint("MissingPermission")
    private fun showNotification(
        context: Context,
        subjectId: Int,
        scheduleId: Int,
        subjectName: String,
        startTime: String,
        endTime: String,
        location: String?
    ) {
        createNotificationChannel(context)

        val formattedStart = TimeUtils.format24To12Hour(startTime)
        val formattedEnd = TimeUtils.format24To12Hour(endTime)
        val duration = TimeUtils.formatDuration(
            TimeUtils.calculateDuration(startTime, endTime)
        )

        val title = "Class Started: $subjectName"
        val message = buildString {
            append("$formattedStart - $formattedEnd ($duration)")
            if (!location.isNullOrBlank()) {
                append("\n📍 $location")
            }
        }

        // Open App Intent
        val launchIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)
        val pendingIntent = PendingIntent.getActivity(
            context,
            scheduleId, // Unique ID per schedule
            launchIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notificationBuilder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_EVENT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true) // Swipeable!
            .setOngoing(false)   // Swipeable!
            .addAction(
                android.R.drawable.ic_input_add,
                "Attended",
                createAttendanceIntent(context, subjectId, "ATTENDED", scheduleId)
            )
            .addAction(
                android.R.drawable.ic_delete,
                "Missed",
                createAttendanceIntent(context, subjectId, "MISSED", scheduleId)
            )

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        // Use scheduleId as notification ID to allow multiple simultaneous notifications
        notificationManager.notify(scheduleId, notificationBuilder.build())
    }

    private fun createAttendanceIntent(context: Context, subjectId: Int, action: String, scheduleId: Int): PendingIntent {
        val intent = Intent(context, AttendanceActionReceiver::class.java).apply {
            putExtra("subjectId", subjectId)
            putExtra("notificationId", scheduleId) // Pass scheduleId to cancel correct notification
            putExtra("action", action)
        }
        return PendingIntent.getBroadcast(
            context,
            scheduleId * 10 + (if (action == "ATTENDED") 1 else 2), // Unique Request Code
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Class Timetable",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for scheduled classes"
                enableVibration(true)
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    companion object {
        private const val CHANNEL_ID = "class_timetable_alarms"
    }
}
