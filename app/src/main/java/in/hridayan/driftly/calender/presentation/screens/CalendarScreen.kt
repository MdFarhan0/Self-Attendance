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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.outlined.CheckCircleOutline
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.TrackChanges
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonGroup
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
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
import `in`.hridayan.driftly.export.presentation.ExportReportBottomSheet
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
import androidx.compose.ui.layout.layout
import kotlin.math.ceil

data class AttendanceInsight(
    val message: String,
    val icon: ImageVector,
    val bunkCount: Int = 0,
    val requiredCount: Int = 0
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
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
    var showExportBottomSheet by rememberSaveable { mutableStateOf(false) }
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
                title = { },
                navigationIcon = {
                    Box(modifier = Modifier.padding(start = 6.dp)) {
                        BackButton()
                    }
                },
                actions = {
                    FilledTonalButton(
                        onClick = {
                            weakHaptic()
                            showExportBottomSheet = true
                        },
                        shapes = ButtonDefaults.shapes(),
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        modifier = Modifier.height(40.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Description,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "PDF Report",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(15.dp))
                }
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
            // New large subject title
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 25.dp)
                    .alpha(0.95f)
                    .basicMarquee(),
                text = subject,
                style = MaterialTheme.typography.displaySmallEmphasized.copy(
                    fontWeight = FontWeight.ExtraBold
                ),
                maxLines = 1
            )

            CalendarCanvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 15.dp)
                    .layout { measurable, constraints ->
                        val placeable = measurable.measure(constraints)
                        val offsetPx = 6.dp.roundToPx()
                        layout(placeable.width, placeable.height - offsetPx) {
                            placeable.placeRelative(0, -offsetPx)
                        }
                    },
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

            Spacer(modifier = Modifier.height(3.dp)) // 3dp + 5dp root arrangement = 8dp gap



            // ── 4 Action Buttons in 2 Rows with animateWidth ──
            val calendarBtnSources = remember { List(4) { MutableInteractionSource() } }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 15.dp),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                @Suppress("DEPRECATION")
                ButtonGroup(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    // Overview button (57%)
                    Button(
                        modifier = Modifier
                            .weight(0.57f)
                            .animateWidth(calendarBtnSources[0]),
                        interactionSource = calendarBtnSources[0],
                        shapes = ButtonDefaults.shapes(),
                        onClick = {
                            weakHaptic()
                            isMonthlyMode = false
                            showSubjectAttendanceDataBottomSheet = true
                        },
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

                    // Target button (43%)
                    Button(
                        modifier = Modifier
                            .weight(0.43f)
                            .animateWidth(calendarBtnSources[1]),
                        interactionSource = calendarBtnSources[1],
                        shapes = ButtonDefaults.shapes(),
                        onClick = {
                            weakHaptic()
                            showTargetBottomSheet = true
                        },
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

                @Suppress("DEPRECATION")
                ButtonGroup(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    // Monthly button (43%)
                    Button(
                        modifier = Modifier
                            .weight(0.43f)
                            .animateWidth(calendarBtnSources[2]),
                        interactionSource = calendarBtnSources[2],
                        shapes = ButtonDefaults.shapes(),
                        onClick = {
                            weakHaptic()
                            isMonthlyMode = true
                            showSubjectAttendanceDataBottomSheet = true
                        },
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

                    // Timetable button (57%)
                    Button(
                        modifier = Modifier
                            .weight(0.57f)
                            .animateWidth(calendarBtnSources[3]),
                        interactionSource = calendarBtnSources[3],
                        shapes = ButtonDefaults.shapes(),
                        onClick = {
                            weakHaptic()
                            showTimetableBottomSheet = true
                        },
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

            Spacer(modifier = Modifier.height(5.dp)) // Extra 5dp to make total 10dp from previous element

            // Insight Section (No Card)
            AnimatedContent(
                targetState = insight,
                transitionSpec = {
                    (slideInVertically { it } + fadeIn()).togetherWith(
                        slideOutVertically { -it } + fadeOut()
                    )
                },
                label = "insight_animation",
                modifier = Modifier.padding(horizontal = 25.dp) // Adjusted padding for text look
            ) { currentInsight ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = currentInsight.icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
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

        if (showExportBottomSheet) {
            ExportReportBottomSheet(
                onDismiss = {
                    showExportBottomSheet = false
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
                icon = Icons.Outlined.Info
            )
        }
        currentPercentage < T -> {
            AttendanceInsight(
                message = "Attend the next $requiredAttend ${if (requiredAttend == 1) "class" else "classes"} to reach ${T.toInt()}%",
                icon = Icons.Outlined.School,
                requiredCount = requiredAttend
            )
        }
        bunkCount >= 1 -> {
            AttendanceInsight(
                message = "You can bunk $bunkCount more ${if (bunkCount == 1) "class" else "classes"} and still stay above ${T.toInt()}%",
                icon = Icons.Outlined.CheckCircleOutline,
                bunkCount = bunkCount
            )
        }
        else -> {
            AttendanceInsight(
                message = "You're just safe at ${T.toInt()}%. Don't miss the next class",
                icon = Icons.Outlined.Warning
            )
        }
    }
}
