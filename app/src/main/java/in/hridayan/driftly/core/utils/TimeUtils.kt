package `in`.hridayan.driftly.core.utils

import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

object TimeUtils {
    
    /**
     * Converts 24-hour format time string to 12-hour format with AM/PM
     * @param time24 Time in "HH:mm" format (e.g., "14:30")
     * @return Time in "h:mm a" format (e.g., "2:30 PM")
     */
    fun format24To12Hour(time24: String): String {
        return try {
            val time = LocalTime.parse(time24, DateTimeFormatter.ofPattern("HH:mm"))
            time.format(DateTimeFormatter.ofPattern("h:mm a", Locale.getDefault()))
        } catch (e: Exception) {
            time24
        }
    }

    /**
     * Converts 12-hour format time to 24-hour format
     * @param time12 Time in "h:mm a" format (e.g., "2:30 PM")
     * @return Time in "HH:mm" format (e.g., "14:30")
     */
    fun format12To24Hour(time12: String): String {
        return try {
            val time = LocalTime.parse(time12, DateTimeFormatter.ofPattern("h:mm a", Locale.ENGLISH))
            time.format(DateTimeFormatter.ofPattern("HH:mm"))
        } catch (e: Exception) {
            time12
        }
    }

    /**
     * Formats time from hour and minute components
     * @param hour Hour (0-23)
     * @param minute Minute (0-59)
     * @return Time in "HH:mm" format
     */
    fun formatTime(hour: Int, minute: Int): String {
        return String.format(Locale.getDefault(), "%02d:%02d", hour, minute)
    }

    /**
     * Parses hour from time string
     * @param time Time in "HH:mm" format
     * @return Hour (0-23)
     */
    fun getHour(time: String): Int {
        return try {
            time.split(":")[0].toInt()
        } catch (e: Exception) {
            0
        }
    }

    /**
     * Parses minute from time string
     * @param time Time in "HH:mm" format
     * @return Minute (0-59)
     */
    fun getMinute(time: String): Int {
        return try {
            time.split(":")[1].toInt()
        } catch (e: Exception) {
            0
        }
    }

    /**
     * Calculates duration between two times in minutes
     * @param startTime Start time in "HH:mm"
     * @param endTime End time in "HH:mm"
     * @return Duration in minutes
     */
    fun calculateDuration(startTime: String, endTime: String): Int {
        return try {
            val start = LocalTime.parse(startTime, DateTimeFormatter.ofPattern("HH:mm"))
            val end = LocalTime.parse(endTime, DateTimeFormatter.ofPattern("HH:mm"))
            val duration = java.time.Duration.between(start, end)
            duration.toMinutes().toInt()
        } catch (e: Exception) {
            0
        }
    }

    /**
     * Formats duration in human-readable format
     * @param durationMinutes Duration in minutes
     * @return Formatted string (e.g., "2 hours", "1 hour 30 min")
     */
    fun formatDuration(durationMinutes: Int): String {
        val hours = durationMinutes / 60
        val minutes = durationMinutes % 60
        
        return when {
            hours == 0 -> "$minutes min"
            minutes == 0 -> if (hours == 1) "1 hour" else "$hours hours"
            else -> if (hours == 1) "1 hour $minutes min" else "$hours hours $minutes min"
        }
    }

    /**
     * Checks if end time is after start time
     * @return true if valid time range
     */
    fun isValidTimeRange(startTime: String, endTime: String): Boolean {
        return try {
            val start = LocalTime.parse(startTime, DateTimeFormatter.ofPattern("HH:mm"))
            val end = LocalTime.parse(endTime, DateTimeFormatter.ofPattern("HH:mm"))
            end.isAfter(start)
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Gets current day of week (1=Monday, 7=Sunday)
     */
    fun getCurrentDayOfWeek(): Int {
        return java.time.LocalDate.now().dayOfWeek.value
    }

    /**
     * Checks if given time is in the past today
     */
    fun isTimePast(time: String): Boolean {
        return try {
            val classTime = LocalTime.parse(time, DateTimeFormatter.ofPattern("HH:mm"))
            val now = LocalTime.now()
            classTime.isBefore(now)
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Calculates end time given start time and duration
     * @param startTime Start time in "HH:mm"
     * @param durationMinutes Duration in minutes
     * @return End time in "HH:mm"
     */
    fun calculateEndTime(startTime: String, durationMinutes: Int): String {
        return try {
            val start = LocalTime.parse(startTime, DateTimeFormatter.ofPattern("HH:mm"))
            val end = start.plusMinutes(durationMinutes.toLong())
            end.format(DateTimeFormatter.ofPattern("HH:mm"))
        } catch (e: Exception) {
            startTime
        }
    }
}
