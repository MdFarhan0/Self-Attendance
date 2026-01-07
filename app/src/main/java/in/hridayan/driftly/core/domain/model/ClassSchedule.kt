package `in`.hridayan.driftly.core.domain.model

import `in`.hridayan.driftly.core.data.model.ClassScheduleEntity
import java.time.DayOfWeek
import java.time.LocalTime

data class ClassSchedule(
    val id: Int = 0,
    val subjectId: Int,
    val dayOfWeek: Int,
    val startTime: String,
    val endTime: String,
    val location: String? = null,
    val isEnabled: Boolean = true
) {
    fun toEntity(): ClassScheduleEntity {
        return ClassScheduleEntity(
            id = id,
            subjectId = subjectId,
            dayOfWeek = dayOfWeek,
            startTime = startTime,
            endTime = endTime,
            location = location,
            isEnabled = isEnabled
        )
    }

    fun getDayName(): String {
        return when (dayOfWeek) {
            1 -> "Monday"
            2 -> "Tuesday"
            3 -> "Wednesday"
            4 -> "Thursday"
            5 -> "Friday"
            6 -> "Saturday"
            7 -> "Sunday"
            else -> "Unknown"
        }
    }

    fun getFormattedTimeRange(): String {
        return "$startTime - $endTime"
    }
}

fun ClassScheduleEntity.toDomain(): ClassSchedule {
    return ClassSchedule(
        id = id,
        subjectId = subjectId,
        dayOfWeek = dayOfWeek,
        startTime = startTime,
        endTime = endTime,
        location = location,
        isEnabled = isEnabled
    )
}
