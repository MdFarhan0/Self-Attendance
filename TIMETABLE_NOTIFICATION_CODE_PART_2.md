# Timetable Notification System - Part 2

This document contains the notification display logic, action handlers, and boot persistence.

## 4. NotificationSetup.kt
Defines notification channels and high-level notification construction.

```kotlin
package in.hridayan.driftly.notification

import android.Manifest
import android.content.Context
import androidx.annotation.RequiresPermission
import in.hridayan.driftly.R
import in.hridayan.driftly.notification.helper.NotificationHelper

object NotificationSetup {
    const val TIMETABLE_CHANNEL_ID = "timetable_channel"

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    fun showTimetableNotification(
        context: Context,
        subjectId: Int,
        subjectName: String,
        startTime: String,
        endTime: String,
        location: String?,
        scheduleId: Int
    ) {
        val formattedStart = in.hridayan.driftly.core.utils.TimeUtils.format24To12Hour(startTime)
        val formattedEnd = in.hridayan.driftly.core.utils.TimeUtils.format24To12Hour(endTime)
        val duration = in.hridayan.driftly.core.utils.TimeUtils.formatDuration(
            in.hridayan.driftly.core.utils.TimeUtils.calculateDuration(startTime, endTime)
        )

        val message = buildString {
            append("$formattedStart - $formattedEnd ($duration)")
            if (!location.isNullOrBlank()) {
                append("\n📍 $location")
            }
        }

        NotificationHelper.showNotificationWithActions(
            context = context,
            channelId = TIMETABLE_CHANNEL_ID,
            channelName = "Class Timetable",
            channelDescription = "Notifications for scheduled classes",
            notificationId = scheduleId,
            title = "Class Started: $subjectName",
            message = message,
            smallIconResId = android.R.drawable.ic_dialog_info,
            subjectId = subjectId,
            scheduleId = scheduleId
        )
    }
}
```

## 5. NotificationHelper.kt
Helper for building notifications with "Attended" and "Missed" actions.

```kotlin
package in.hridayan.driftly.notification.helper

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.annotation.DrawableRes
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import in.hridayan.driftly.MainActivity
import in.hridayan.driftly.R

object NotificationHelper {

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    fun showNotificationWithActions(
        context: Context,
        channelId: String,
        channelName: String,
        channelDescription: String,
        notificationId: Int,
        title: String,
        message: String,
        @DrawableRes smallIconResId: Int,
        subjectId: Int,
        scheduleId: Int,
        priority: Int = NotificationCompat.PRIORITY_HIGH
    ) {
        createNotificationChannel(context, channelId, channelName, channelDescription)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(context, notificationId, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)

        // Action: Attended
        val attendedIntent = Intent(context, in.hridayan.driftly.notification.AttendanceActionReceiver::class.java).apply {
            putExtra("subjectId", subjectId)
            putExtra("notificationId", scheduleId)
            putExtra("action", "ATTENDED")
        }
        val attendedPI = PendingIntent.getBroadcast(context, scheduleId * 10 + 1, attendedIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)

        // Action: Missed
        val missedIntent = Intent(context, in.hridayan.driftly.notification.AttendanceActionReceiver::class.java).apply {
            putExtra("subjectId", subjectId)
            putExtra("notificationId", scheduleId)
            putExtra("action", "MISSED")
        }
        val missedPI = PendingIntent.getBroadcast(context, scheduleId * 10 + 2, missedIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(smallIconResId)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(priority)
            .setCategory(NotificationCompat.CATEGORY_EVENT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .addAction(android.R.drawable.ic_input_add, "Attended", attendedPI)
            .addAction(android.R.drawable.ic_delete, "Missed", missedPI)
            .build()

        NotificationManagerCompat.from(context).notify(notificationId, notification)
    }

    private fun createNotificationChannel(context: Context, channelId: String, name: String, desc: String) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(channelId, name, importance).apply { description = desc }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}
```

## 6. AttendanceActionReceiver.kt
Handles the clicks on notification actions. It enqueues a background worker to mark attendance in the database.

```kotlin
package in.hridayan.driftly.notification

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf

class AttendanceActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val subjectId = intent.getIntExtra("subjectId", -1)
        val notificationId = intent.getIntExtra("notificationId", -1)
        val action = intent.getStringExtra("action")
        
        if (subjectId == -1 || action == null) return
        
        // Dismiss notification
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.cancel(notificationId)

        if (action == "CLEAR") return
        
        // Mark attendance via Worker
        val workData = workDataOf("subjectId" to subjectId, "action" to action)
        val work = OneTimeWorkRequestBuilder<MarkAttendanceWorker>()
            .setInputData(workData)
            .build()

        WorkManager.getInstance(context).enqueue(work)
    }
}
```

## 7. BootCompletedReceiver.kt
Ensures alarms are rescheduled when the device reboots.

```kotlin
package in.hridayan.driftly.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import in.hridayan.driftly.core.data.database.SubjectDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class BootCompletedReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED || 
            intent.action == Intent.ACTION_MY_PACKAGE_REPLACED) {
            
            val pendingResult = goAsync()
            CoroutineScope(Dispatchers.IO + SupervisorJob()).launch {
                try {
                    val db = SubjectDatabase.getDatabase(context)
                    val schedules = db.classScheduleDao().getAllSchedulesOnce()
                    
                    for (schedule in schedules) {
                        if (schedule.isEnabled) {
                            val subject = db.subjectDao().getSubjectById(schedule.subjectId).first()
                            TimetableAlarmScheduler.scheduleClassAlarms(
                                context, schedule.id, schedule.subjectId, 
                                subject.subject, schedule.dayOfWeek, 
                                schedule.startTime, schedule.endTime, schedule.location
                            )
                        }
                    }
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}
```
