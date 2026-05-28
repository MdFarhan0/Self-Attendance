package `in`.hridayan.driftly.export.pdf.model

data class AttendancePdfItem(
    val subjectName: String,
    val subjectCode: String = "",
    val attendedClasses: Int,
    val missedClasses: Int,
    val threshold: Int,
    val percentage: Float
)
