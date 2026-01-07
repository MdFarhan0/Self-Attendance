# Timetable Notification System - Part 1

This document contains the core implementation of the exact alarm-based timetable notification system.

## 1. AndroidManifest.xml (Permissions & Receivers)

```xml
<!-- Required Permissions -->
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
<uses-permission android:name="android.permission.SCHEDULE_EXACT_ALARM" />
<uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED" />

<application ...>
    <!-- Timetable Alarm Receiver -->
    <receiver
        android:name=".notification.TimetableAlarmReceiver"
        android:enabled="true"
        android:exported="false">
        <intent-filter>
            <action android:name="in.hridayan.driftly.TIMETABLE_ALARM" />
        </intent-filter>
    </receiver>
    
    <!-- Boot Receiver to reschedule alarms -->
    <receiver
        android:name=".notification.BootCompletedReceiver"
        android:enabled="true"
        android:exported="false">
        <intent-filter>
            <action android:name="android.intent.action.BOOT_COMPLETED" />
            <action android:name="android.intent.action.MY_PACKAGE_REPLACED" />
            <action android:name="android.intent.action.QUICKBOOT_POWERON" />
        </intent-filter>
    </receiver>

    <!-- Receiver for Notification Buttons (Attended/Missed) -->
    <receiver
        android:name=".notification.AttendanceActionReceiver"
        android:enabled="true"
        android:exported="false" />
</application>
```

## 2. TimetableAlarmScheduler.kt
Responsible for converting class times to epoch milliseconds and scheduling exact alarms using `AlarmManager`.

```kotlin
package in.hridayan.driftly.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.util.Log
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZonedDateTime
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters

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
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.canScheduleExactAlarms()
        } else true
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
        type: String // "START" or "END"
    ): Boolean {
        val timeToUse = if (type == "START") startTime else endTime
        val triggerAtMillis = timetableToMillis(dayOfWeek, timeToUse)
        val now = System.currentTimeMillis()

        if (triggerAtMillis <= now || !canScheduleExactAlarms(context)) return false

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, TimetableAlarmReceiver::class.java).apply {
            action = "in.hridayan.driftly.TIMETABLE_ALARM"
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
            context, requestCode, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return try {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun scheduleClassAlarms(context: Context, scheduleId: Int, subjectId: Int, subjectName: String, dayOfWeek: Int, startTime: String, endTime: String, location: String?): Boolean {
        return scheduleAlarm(context, scheduleId, subjectId, subjectName, dayOfWeek, startTime, endTime, location, "START")
    }

    fun cancelClassAlarms(context: Context, scheduleId: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, TimetableAlarmReceiver::class.java).apply {
            action = "in.hridayan.driftly.TIMETABLE_ALARM"
        }
        val startPI = PendingIntent.getBroadcast(context, scheduleId, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val endPI = PendingIntent.getBroadcast(context, scheduleId + END_ALARM_OFFSET, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        alarmManager.cancel(startPI)
        alarmManager.cancel(endPI)
    }

    fun cancelAllAlarmsForSubject(context: Context, scheduleIds: List<Int>) {
        scheduleIds.forEach { cancelClassAlarms(context, it) }
    }
}
```

## 3. TimetableAlarmReceiver.kt
BroadcastReceiver that triggers when an alarm fires. It shows the notification and reschedules the alarm for the next week.

```kotlin
package in.hridayan.driftly.notification

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat

class TimetableAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val scheduleId = intent.getIntExtra("scheduleId", -1)
        val subjectId = intent.getIntExtra("subjectId", -1)
        val subjectName = intent.getStringExtra("subjectName") ?: "Unknown"
        val startTime = intent.getStringExtra("startTime") ?: ""
        val endTime = intent.getStringExtra("endTime") ?: ""
        val location = intent.getStringExtra("location")
        val type = intent.getStringExtra("type") ?: "START"
        val dayOfWeek = intent.getIntExtra("dayOfWeek", -1)

        if (scheduleId == -1 || subjectId == -1) return

        val sharedPrefs = context.getSharedPreferences("driftly_settings", Context.MODE_PRIVATE)
        val enabled = sharedPrefs.getBoolean("enable_timetable_notifications", true)
        
        if (!enabled) {
            rescheduleForNextWeek(context, intent)
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                rescheduleForNextWeek(context, intent)
                return
            }
        }

        if (type == "START") {
            NotificationSetup.showTimetableNotification(context, subjectId, subjectName, startTime, endTime, location, scheduleId)
        }

        rescheduleForNextWeek(context, intent)
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

        if (scheduleId != -1 && dayOfWeek != -1) {
            TimetableAlarmScheduler.scheduleAlarm(context, scheduleId, subjectId, subjectName, dayOfWeek, startTime, endTime, location, type)
        }
    }
}
```
