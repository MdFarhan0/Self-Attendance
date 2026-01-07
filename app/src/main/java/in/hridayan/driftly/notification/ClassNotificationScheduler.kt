
package `in`.hridayan.driftly.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import `in`.hridayan.driftly.core.domain.model.ClassSchedule
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.temporal.TemporalAdjusters

object ClassNotificationScheduler {

    /**
     * Schedule notifications for all classes of a subject
     */
    fun scheduleAllClassNotifications(
        context: Context,
        subjectId: Int,
        subjectName: String,
        schedules: List<ClassSchedule>
    ) {
        // Cancel first (to avoid duplicates if updated)
        schedules.forEach { schedule ->
            cancelScheduleNotification(context, schedule.id)
        }

        schedules.forEach { schedule ->
            scheduleClassNotification(context, subjectId, subjectName, schedule)
        }
    }

    /**
     * Schedule notification for a single class (Next Occurrence)
     */
    fun scheduleClassNotification(
        context: Context,
        subjectId: Int,
        subjectName: String,
        schedule: ClassSchedule
    ) {
        val triggerTime = calculateNextOccurrence(schedule.dayOfWeek, schedule.startTime)
        
        scheduleAlarm(
            context,
            subjectId,
            schedule.id,
            subjectName,
            schedule.startTime,
            schedule.endTime,
            schedule.location,
            schedule.dayOfWeek,
            triggerTime
        )
    }

    /**
     * Reschedule for Next Week (Called by Receiver)
     */
    fun scheduleNextWeek(
        context: Context,
        subjectId: Int,
        scheduleId: Int,
        subjectName: String,
        startTime: String,
        endTime: String,
        location: String?,
        dayOfWeek: Int
    ) {
        // Current time is just past the trigger time.
        // We want 7 days from now (or rather, 7 days from the just-triggered time).
        // Safest: Calculate next occurrence from NOW + 1 minute logic.
        
        val nextTriggerTime = calculateNextOccurrence(dayOfWeek, startTime, lookAheadFromToday = false) // Force next week logic if today
        // Actually generic calculator is better:
        // If scheduled for today 9am and it's 9:01am, generic calc returns next week.
        
        scheduleAlarm(
            context,
            subjectId,
            scheduleId, // ID
            subjectName,
            startTime,
            endTime,
            location,
            dayOfWeek,
            nextTriggerTime
        )
    }

    private fun scheduleAlarm(
        context: Context,
        subjectId: Int,
        scheduleId: Int,
        subjectName: String,
        startTime: String,
        endTime: String,
        location: String?,
        dayOfWeek: Int,
        triggerTimeMillis: Long
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        Log.d("ClassScheduler", "=== SCHEDULING ALARM ===")
        Log.d("ClassScheduler", "Subject: $subjectName")
        Log.d("ClassScheduler", "Schedule ID: $scheduleId")
        Log.d("ClassScheduler", "Day of week: $dayOfWeek")
        Log.d("ClassScheduler", "Start time: $startTime")
        Log.d("ClassScheduler", "Trigger time: $triggerTimeMillis (${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(java.util.Date(triggerTimeMillis))})")
        Log.d("ClassScheduler", "Current time: ${System.currentTimeMillis()} (${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(java.util.Date())})")
        
        // Permission check for Android 12+ (S)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val canSchedule = alarmManager.canScheduleExactAlarms()
            Log.d("ClassScheduler", "Can schedule exact alarms: $canSchedule")
            if (!canSchedule) {
                Log.e("ClassScheduler", "FATAL: Cannot schedule exact alarms. Permission missing!")
                return 
            }
        }

        val intent = Intent(context, ClassAlarmReceiver::class.java).apply {
            putExtra("subjectId", subjectId)
            putExtra("scheduleId", scheduleId)
            putExtra("subjectName", subjectName)
            putExtra("startTime", startTime)
            putExtra("endTime", endTime)
            putExtra("location", location)
            putExtra("dayOfWeek", dayOfWeek)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            scheduleId, // Unique ID per class schedule
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerTimeMillis,
            pendingIntent
        )
        
        Log.d("ClassScheduler", "Alarm scheduled successfully!")
        Log.d("ClassScheduler", "=============================")
    }

    fun cancelScheduleNotification(context: Context, scheduleId: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, ClassAlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            scheduleId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_NO_CREATE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
    }
    
    // Legacy support/Clean up
    fun cancelNotifications(context: Context, subjectId: Int) {
        // Since we track by scheduleId, we can't easily cancel all by subjectId unless we have the list.
        // Or we loop through possible IDs? No.
        // This is usually called when updating a subject. We should pass the OLD list to clean up.
        // For now, WorkManager cleanup is sufficient for old style.
        androidx.work.WorkManager.getInstance(context).cancelAllWorkByTag("class_notification_$subjectId")
    }

    private fun calculateNextOccurrence(dayOfWeek: Int, startTime: String, lookAheadFromToday: Boolean = true): Long {
        val now = LocalDateTime.now()
        val classTime = LocalTime.parse(startTime)
        
        var nextDate = now.with(TemporalAdjusters.nextOrSame(DayOfWeek.of(dayOfWeek))) // Finds next or today
        
        // If today matches, check time
        if (nextDate.toLocalDate() == now.toLocalDate()) {
            if (now.toLocalTime().isAfter(classTime)) {
                // Keep today only if lookAheadFromToday is false (meaning specific calculation)
                // usually if passed, we want next week
                nextDate = nextDate.plusWeeks(1)
            }
        }
        
        return nextDate.with(classTime).atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
    }
}
