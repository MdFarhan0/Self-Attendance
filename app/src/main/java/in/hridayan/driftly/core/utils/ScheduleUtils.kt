package `in`.hridayan.driftly.core.utils

import `in`.hridayan.driftly.core.domain.model.ClassSchedule
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime

object ScheduleUtils {
    
    /**
     * Gets the next scheduled class from current date/time
     * @param schedules List of all class schedules
     * @return Next upcoming class or null if none found
     */
    fun getNextScheduledClass(schedules: List<ClassSchedule>): ClassSchedule? {
        if (schedules.isEmpty()) return null

        val now = LocalTime.now()
        val today = TimeUtils.getCurrentDayOfWeek()
        
        // First, check for classes later today
        val todayClasses = schedules
            .filter { it.dayOfWeek == today && it.isEnabled }
            .sortedBy { it.startTime }
        
        for (classSchedule in todayClasses) {
            val classTime = LocalTime.parse(classSchedule.startTime)
            if (classTime.isAfter(now)) {
                return classSchedule
            }
        }
        
        // If no classes today, find next class in upcoming days
        for (daysAhead in 1..7) {
            val checkDay = ((today + daysAhead - 1) % 7) + 1
            val dayClasses = schedules
                .filter { it.dayOfWeek == checkDay && it.isEnabled }
                .sortedBy { it.startTime }
            
            if (dayClasses.isNotEmpty()) {
                return dayClasses.first()
            }
        }
        
        return null
    }

    /**
     * Gets all classes for a specific day
     */
    fun getClassesForDay(schedules: List<ClassSchedule>, dayOfWeek: Int): List<ClassSchedule> {
        return schedules
            .filter { it.dayOfWeek == dayOfWeek && it.isEnabled }
            .sortedBy { it.startTime }
    }

    /**
     * Gets all classes for today
     */
    fun getTodayClasses(schedules: List<ClassSchedule>): List<ClassSchedule> {
        val today = TimeUtils.getCurrentDayOfWeek()
        return getClassesForDay(schedules, today)
    }

    /**
     * Checks if a class is currently ongoing
     */
    fun isClassOngoing(schedule: ClassSchedule): Boolean {
        val today = TimeUtils.getCurrentDayOfWeek()
        if (schedule.dayOfWeek != today) return false
        
        val now = LocalTime.now()
        val startTime = LocalTime.parse(schedule.startTime)
        val endTime = LocalTime.parse(schedule.endTime)
        
        return now.isAfter(startTime) && now.isBefore(endTime)
    }

    /**
     * Checks if a class has passed today
     */
    fun hasClassPassed(schedule: ClassSchedule): Boolean {
        val today = TimeUtils.getCurrentDayOfWeek()
        if (schedule.dayOfWeek != today) return false
        
        val now = LocalTime.now()
        val endTime = LocalTime.parse(schedule.endTime)
        
        return now.isAfter(endTime)
    }

    /**
     * Gets formatted string for next class display
     * @return String like "Next: Mon 8:45 AM" or null
     */
    fun getNextClassDisplayText(schedules: List<ClassSchedule>): String? {
        val nextClass = getNextScheduledClass(schedules) ?: return null
        
        val dayName = when (nextClass.dayOfWeek) {
            1 -> "Mon"
            2 -> "Tue"
            3 -> "Wed"
            4 -> "Thu"
            5 -> "Fri"
            6 -> "Sat"
            7 -> "Sun"
            else -> ""
        }
        
        val time = TimeUtils.format24To12Hour(nextClass.startTime)
        
        // Add "Today" or "Tomorrow" if applicable
        val today = TimeUtils.getCurrentDayOfWeek()
        val tomorrow = if (today == 7) 1 else today + 1
        
        return when (nextClass.dayOfWeek) {
            today -> "Today at $time"
            tomorrow -> "Tomorrow at $time"
            else -> "$dayName $time"
        }
    }

    /**
     * Gets count of total scheduled classes per week
     */
    fun getTotalWeeklyClasses(schedules: List<ClassSchedule>): Int {
        return schedules.count { it.isEnabled }
    }

    /**
     * Checks if there are any classes scheduled for the week
     */
    fun hasScheduledClasses(schedules: List<ClassSchedule>): Boolean {
        return schedules.any { it.isEnabled }
    }

    /**
     * Gets day name from day number
     */
    fun getDayName(dayOfWeek: Int, short: Boolean = false): String {
        return when (dayOfWeek) {
            1 -> if (short) "Mon" else "Monday"
            2 -> if (short) "Tue" else "Tuesday"
            3 -> if (short) "Wed" else "Wednesday"
            4 -> if (short) "Thu" else "Thursday"
            5 -> if (short) "Fri" else "Friday"
            6 -> if (short) "Sat" else "Saturday"
            7 -> if (short) "Sun" else "Sunday"
            else -> "Unknown"
        }
    }

    /**
     * Validates that class times don't overlap for the same day
     */
    fun hasTimeConflicts(
        schedules: List<ClassSchedule>,
        newSchedule: ClassSchedule
    ): Boolean {
        val sameDaySchedules = schedules.filter {
            it.dayOfWeek == newSchedule.dayOfWeek &&
            it.id != newSchedule.id &&
            it.isEnabled
        }

        val newStart = LocalTime.parse(newSchedule.startTime)
        val newEnd = LocalTime.parse(newSchedule.endTime)

        for (existing in sameDaySchedules) {
            val existingStart = LocalTime.parse(existing.startTime)
            val existingEnd = LocalTime.parse(existing.endTime)

            // Check for any overlap
            if (newStart.isBefore(existingEnd) && newEnd.isAfter(existingStart)) {
                return true
            }
        }

        return false
    }
}
