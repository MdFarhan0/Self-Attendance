package `in`.hridayan.driftly.core.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "subjects")
data class SubjectEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val subject: String,
    val subjectCode: String? = null,
    val targetPercentage: Float = 75.0f,
    val histogramLabel: String? = null,
    val isTargetSet: Boolean = false,
    val savedYear: Int? = null,
    val savedMonth: Int? = null
)