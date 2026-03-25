package `in`.hridayan.driftly.calender.presentation.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.TrackChanges
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.toRoute
import `in`.hridayan.driftly.R
import `in`.hridayan.driftly.calender.presentation.components.bottomsheet.AttendanceTargetBottomSheet
import `in`.hridayan.driftly.calender.presentation.components.bottomsheet.DailyAttendanceBottomSheet
import `in`.hridayan.driftly.calender.presentation.components.bottomsheet.SubjectAttendanceDataBottomSheet
import `in`.hridayan.driftly.calender.presentation.components.bottomsheet.TimetableBottomSheet
import `in`.hridayan.driftly.calender.presentation.components.canvas.CalendarCanvas
import `in`.hridayan.driftly.calender.presentation.viewmodel.CalendarViewModel
import `in`.hridayan.driftly.core.common.LocalSettings
import `in`.hridayan.driftly.core.common.LocalWeakHaptic
import `in`.hridayan.driftly.core.domain.model.AttendanceStatus
import `in`.hridayan.driftly.core.domain.model.SubjectAttendance
import `in`.hridayan.driftly.core.presentation.components.button.BackButton
import `in`.hridayan.driftly.core.presentation.components.text.AutoResizeableText
import `in`.hridayan.driftly.home.presentation.viewmodel.HomeViewModel
import `in`.hridayan.driftly.navigation.CalendarScreen
import `in`.hridayan.driftly.navigation.LocalNavController
import kotlin.math.ceil

data class AttendanceInsight(
    val message: String,
    val icon: ImageVector
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    viewModel: CalendarViewModel = hiltViewModel(),
    homeViewModel: HomeViewModel = hiltViewModel(),
) {
    val weakHaptic = LocalWeakHaptic.current
    val navController = LocalNavController.current
    val args = navController.currentBackStackEntry?.toRoute<CalendarScreen>()
    val subjectId = args?.subjectId ?: 0
    val subject = args?.subject ?: ""
    val markedDates by viewModel.markedDatesFlow.collectAsState()
    val dayStatusMap by viewModel.dayStatusMapFlow.collectAsState()
    val streakMap by viewModel.streakMapFlow.collectAsState(initial = emptyMap())
    val subjectEntity = viewModel.getSubjectEntityById(subjectId).collectAsState(initial = null)
    val savedYear = subjectEntity.value?.savedYear
    val savedMonth = subjectEntity.value?.savedMonth
    val monthYear = viewModel.selectedMonthYear.value
    val year = monthYear.year
    val month = monthYear.monthValue
    val shouldRememberMonthYear = LocalSettings.current.rememberCalendarMonthYear

    var showSubjectAttendanceDataBottomSheet by rememberSaveable { mutableStateOf(false) }
    var showTargetBottomSheet by rememberSaveable { mutableStateOf(false) }
    var showTimetableBottomSheet by rememberSaveable { mutableStateOf(false) }
    var selectedDateForBottomSheet by rememberSaveable { mutableStateOf<String?>(null) }

    val attendanceCounts by homeViewModel.getSubjectAttendanceCounts(subjectId)
        .collectAsState(initial = SubjectAttendance())

    val monthlyAttendanceCounts by androidx.compose.runtime.remember(subjectId, year, month) {
        viewModel.getMonthlyAttendanceCounts(subjectId, year, month)
    }.collectAsState(initial = SubjectAttendance())

    var isMonthlyMode by rememberSaveable { mutableStateOf(false) }

    val targetPercentage = subjectEntity.value?.targetPercentage ?: 75.0f
    val isTargetSet = subjectEntity.value?.isTargetSet ?: false

    val insight = calculateAttendanceInsight(
        presentCount = attendanceCounts.presentCount,
        totalCount = attendanceCounts.totalCount,
        targetPercentage = targetPercentage
    )

    LaunchedEffect(subjectId) {
        kotlinx.coroutines.delay(100)
        val entity = subjectEntity.value
        if (entity != null && !entity.isTargetSet) {
            showTargetBottomSheet = true
        }
    }

    val onStatusChange: (String, AttendanceStatus?) -> Unit =
        { date, status ->
            viewModel.onStatusChange(subjectId, date, status)
        }

    LaunchedEffect(savedYear, savedMonth, shouldRememberMonthYear) {
        if (savedYear != null && savedMonth != null && shouldRememberMonthYear) {
            viewModel.updateMonthYear(savedYear, savedMonth)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        modifier = Modifier
                            .fillMaxWidth()
                            .basicMarquee(),
                        text = subject,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                        ),
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1
                    )
                },
                navigationIcon = { BackButton() }
            )
        }) { paddingValue ->
        val schedules by homeViewModel.getSchedulesForSubject(subjectId)
            .collectAsState(initial = emptyList())

        Column(
            modifier = Modifier
                .padding(paddingValue)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            CalendarCanvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 15.dp),
                year = year,
                month = month,
                markedDates = markedDates,
                dayStatusMap = dayStatusMap,
                streakMap = streakMap,
                onStatusChange = onStatusChange,
                onDateClick = { dateString ->
                    selectedDateForBottomSheet = dateString
                },
                onNavigate = { newYear, newMonth ->
                    viewModel.updateMonthYear(newYear, newMonth)
                    viewModel.saveMonthYearForSubject(subjectId)
                },
                onResetMonth = {
                    viewModel.resetYearMonthToCurrent()
                    viewModel.saveMonthYearForSubject(subjectId)
                }
            )



            // -----------------------------------------------
            // Button Grid: 2 rows × 2 cols with custom widths
            // Row 1: Overview (55%) | Target (45%)
            // Row 2: Monthly (45%) | Timetable (55%)
            // -----------------------------------------------
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 15.dp),
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                // ── Row 1 ──
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                ) {
                    // Overview button (55%)
                    Button(
                        modifier = Modifier.weight(0.55f),
                        onClick = {
                            weakHaptic()
                            isMonthlyMode = false
                            showSubjectAttendanceDataBottomSheet = true
                        },
                        shape = RoundedCornerShape(40.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(vertical = 11.dp)
                        ) {
                            AutoResizeableText(
                                text = stringResource(R.string.attendance_overview),
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    // Target button (45%)
                    Button(
                        modifier = Modifier.weight(0.45f),
                        onClick = {
                            weakHaptic()
                            showTargetBottomSheet = true
                        },
                        shape = RoundedCornerShape(40.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(vertical = 11.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.TrackChanges,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            AutoResizeableText(
                                text = "Target",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                        }
                    }
                }

                // ── Row 2 ──
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                ) {
                    // Monthly Attendance button (45%)
                    Button(
                        modifier = Modifier.weight(0.45f),
                        onClick = {
                            weakHaptic()
                            isMonthlyMode = true
                            showSubjectAttendanceDataBottomSheet = true
                        },
                        shape = RoundedCornerShape(40.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(vertical = 11.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.CalendarMonth,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            AutoResizeableText(
                                text = "Monthly",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                        }
                    }

                    // Timetable button (55%)
                    Button(
                        modifier = Modifier.weight(0.55f),
                        onClick = {
                            weakHaptic()
                            showTimetableBottomSheet = true
                        },
                        shape = RoundedCornerShape(40.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(vertical = 11.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.School,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            AutoResizeableText(
                                text = "Timetable",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                        }
                    }
                }
            }

            // Insight Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 25.dp),
                shape = RoundedCornerShape(25.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 0.dp
                )
            ) {
                AnimatedContent(
                    targetState = insight,
                    transitionSpec = {
                        (slideInVertically { it } + fadeIn()).togetherWith(
                            slideOutVertically { -it } + fadeOut()
                        )
                    },
                    label = "insight_animation"
                ) { currentInsight ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = currentInsight.icon,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(32.dp)
                        )
                        Text(
                            text = currentInsight.message,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // ── Bottom Sheets ──

        // Daily multi-attendance bottom sheet (shown when a date is clicked)
        selectedDateForBottomSheet?.let { dateStr ->
            DailyAttendanceBottomSheet(
                subjectId = subjectId,
                dateString = dateStr,
                onDismiss = {
                    selectedDateForBottomSheet = null
                },
                viewModel = viewModel
            )
        }

        if (showSubjectAttendanceDataBottomSheet) {
            val data = if (isMonthlyMode) monthlyAttendanceCounts else attendanceCounts
            val monthName = java.time.Month.of(month).getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale.getDefault())
            val title = if (isMonthlyMode) "Attendance ($monthName)" else "Attendance Overview"

            SubjectAttendanceDataBottomSheet(
                onDismiss = {
                    showSubjectAttendanceDataBottomSheet = false
                },
                data = data,
                title = title
            )
        }

        if (showTargetBottomSheet) {
            AttendanceTargetBottomSheet(
                subjectId = subjectId,
                currentTarget = targetPercentage,
                onDismiss = {
                    showTargetBottomSheet = false
                }
            )
        }

        if (showTimetableBottomSheet) {
            TimetableBottomSheet(
                subjectName = subject,
                schedules = schedules,
                onDismiss = {
                    showTimetableBottomSheet = false
                }
            )
        }
    }
}

fun calculateAttendanceInsight(
    presentCount: Int,
    totalCount: Int,
    targetPercentage: Float = 75f
): AttendanceInsight {
    val A = presentCount
    val C = totalCount
    val T = targetPercentage

    val P = T / 100.0

    val currentPercentage = if (C > 0) {
        (A.toDouble() / C.toDouble()) * 100
    } else {
        0.0
    }

    val rawBunk = if (P > 0) (A / P) - C else 0.0
    val bunkCount = kotlin.math.floor(rawBunk).toInt().coerceAtLeast(0)

    val rawNeed = if (P < 1.0) ((P * C) - A) / (1 - P) else 0.0
    val requiredAttend = kotlin.math.ceil(rawNeed).toInt().coerceAtLeast(0)

    return when {
        C == 0 -> {
            AttendanceInsight(
                message = "Attend your first class to get started",
                icon = Icons.Rounded.Info
            )
        }
        currentPercentage < T -> {
            AttendanceInsight(
                message = "Attend the next $requiredAttend ${if (requiredAttend == 1) "class" else "classes"} to reach ${T.toInt()}%",
                icon = Icons.Rounded.School
            )
        }
        bunkCount >= 1 -> {
            AttendanceInsight(
                message = "You can bunk $bunkCount more ${if (bunkCount == 1) "class" else "classes"} and still stay above ${T.toInt()}%",
                icon = Icons.Rounded.CheckCircle
            )
        }
        else -> {
            AttendanceInsight(
                message = "You're just safe at ${T.toInt()}%. Don't miss the next class",
                icon = Icons.Rounded.Warning
            )
        }
    }
}
