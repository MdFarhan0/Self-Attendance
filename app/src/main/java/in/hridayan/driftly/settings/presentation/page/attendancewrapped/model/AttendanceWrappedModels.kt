package `in`.hridayan.driftly.settings.presentation.page.attendancewrapped.model

import androidx.compose.ui.graphics.Color

data class WrappedSubjectCard(
    val subjectId: Int,
    val subjectName: String,
    val attended: Int,
    val skipped: Int,
    val total: Int,
    val targetPercentage: Float,
) {
    val attendancePercentage: Float =
        if (total == 0) 0f else (attended.toFloat() / total.toFloat()) * 100f

    val isLarge: Boolean = attendancePercentage < 60f || skipped > 15
}

data class WrappedSummary(
    val overallPercentage: Float = 0f,
    val totalAttended: Int = 0,
    val totalSkipped: Int = 0,
    val totalClasses: Int = 0,
)

data class WrappedUiState(
    val subjects: List<WrappedSubjectCard> = emptyList(),
    val summary: WrappedSummary = WrappedSummary()
)

data class WrappedCardPalette(
    val container: Color,
    val content: Color,
    val accent: Color
)
