package `in`.hridayan.driftly.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.temporal.TemporalAdjusters

/**
 * AlarmManager-based scheduler for timetable notifications.
 * Uses setExactAndAllowWhileIdle() to ensure notifications fire at exact times.
 */
object TimetableAlarmScheduler {

    private const val TAG = "TimetableAlarmScheduler"
    private const val END_ALARM_OFFSET = 1_000_000

    fun timetableToMillis(dayOfWeek: Int, time: String): Long {
        val now = ZonedDateTime.now(ZoneId.systemDefault())
        val today = LocalDate.now()
        val targetDayOfWeek = DayOfWeek.of(dayOfWeek)
        val localTime = LocalTime.parse(time)

        var targetDate = today.with(TemporalAdjusters.nextOrSame(targetDayOfWeek))
        var targetDateTime = ZonedDateTime.of(targetDate, localTime, ZoneId.systemDefault())

        if (!targetDateTime.isAfter(now)) {
            targetDate = today.with(TemporalAdjusters.next(targetDayOfWeek))
            targetDateTime = ZonedDateTime.of(targetDate, localTime, ZoneId.systemDefault())
        }

        return targetDateTime.toInstant().toEpochMilli()
    }

    fun canScheduleExactAlarms(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = context.applicationContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.canScheduleExactAlarms()
        } else {
            true
        }
    }

    fun openExactAlarmSettings(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            try {
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
            } catch (_: Exception) {
            }
        }
    }

    fun scheduleAlarm(
        context: Context,
        scheduleId: Int,
        subjectId: Int,
        subjectName: String,
        dayOfWeek: Int,
        startTime: String,
        endTime: String,
        location: String?,
        type: String
    ): Boolean {
        val appContext = context.applicationContext

        val timeToUse = if (type == "START") startTime else endTime
        val triggerAtMillis = timetableToMillis(dayOfWeek, timeToUse)
        val now = System.currentTimeMillis()

        if (triggerAtMillis <= now) {
            return false
        }

        if (!canScheduleExactAlarms(appContext)) {
            return false
        }

        val alarmManager = appContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(appContext, TimetableAlarmReceiver::class.java).apply {
            putExtra("scheduleId", scheduleId)
            putExtra("subjectId", subjectId)
            putExtra("subjectName", subjectName)
            putExtra("startTime", startTime)
            putExtra("endTime", endTime)
            putExtra("location", location)
            putExtra("type", type)
            putExtra("dayOfWeek", dayOfWeek)
            putExtra("triggerTime", triggerAtMillis)
        }

        val requestCode = if (type == "END") scheduleId + END_ALARM_OFFSET else scheduleId

        val pendingIntent = PendingIntent.getBroadcast(
            appContext,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return try {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent
            )
            true
        } catch (_: Exception) {
            false
        }
    }

    fun scheduleClassAlarms(
        context: Context,
        scheduleId: Int,
        subjectId: Int,
        subjectName: String,
        dayOfWeek: Int,
        startTime: String,
        endTime: String,
        location: String?
    ): Boolean {
        return scheduleAlarm(
            context = context.applicationContext,
            scheduleId = scheduleId,
            subjectId = subjectId,
            subjectName = subjectName,
            dayOfWeek = dayOfWeek,
            startTime = startTime,
            endTime = endTime,
            location = location,
            type = "START"
        )
    }

    fun cancelClassAlarms(context: Context, scheduleId: Int) {
        val appContext = context.applicationContext
        val alarmManager = appContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(appContext, TimetableAlarmReceiver::class.java)

        val startPendingIntent = PendingIntent.getBroadcast(
            appContext,
            scheduleId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_NO_CREATE
        )
        if (startPendingIntent != null) {
            alarmManager.cancel(startPendingIntent)
            startPendingIntent.cancel()
        }

        val endPendingIntent = PendingIntent.getBroadcast(
            appContext,
            scheduleId + END_ALARM_OFFSET,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_NO_CREATE
        )
        if (endPendingIntent != null) {
            alarmManager.cancel(endPendingIntent)
            endPendingIntent.cancel()
        }
    }

    fun cancelAllAlarmsForSubject(context: Context, scheduleIds: List<Int>) {
        scheduleIds.forEach { cancelClassAlarms(context, it) }
    }
}
