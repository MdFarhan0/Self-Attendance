package `in`.hridayan.driftly.home.presentation.components.dialog

import `in`.hridayan.driftly.core.data.model.SubjectEntity
import java.time.LocalTime

data class TodayClass(
    val subject: SubjectEntity,
    val startTime: LocalTime,
    val endTime: LocalTime,
    val duration: Long // minutes
)
