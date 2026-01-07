package `in`.hridayan.driftly.core.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable
import java.time.DayOfWeek

@Serializable
@Entity(tableName = "class_schedules")
data class ClassScheduleEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val subjectId: Int,
    val dayOfWeek: Int, // 1=Monday, 2=Tuesday, ..., 7=Sunday
    val startTime: String, // Format: "HH:mm" (e.g., "08:45")
    val endTime: String,   // Format: "HH:mm" (e.g., "10:45")
    val location: String? = null,
    val isEnabled: Boolean = true
)
