package `in`.hridayan.driftly.core.data.model

import androidx.room.Entity
import `in`.hridayan.driftly.core.domain.model.AttendanceStatus
import kotlinx.serialization.Serializable

import androidx.room.PrimaryKey

@Serializable
@Entity(tableName = "attendance")
data class AttendanceEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val subjectId: Int,
    val date: String,
    val status: AttendanceStatus = AttendanceStatus.UNMARKED,
    val note: String? = null
)