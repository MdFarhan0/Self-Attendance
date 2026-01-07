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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.gestures.ScrollableDefaults
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material.icons.rounded.Settings
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.toRoute
import `in`.hridayan.driftly.R
import `in`.hridayan.driftly.calender.presentation.components.bottomsheet.AttendanceTargetBottomSheet
import `in`.hridayan.driftly.calender.presentation.components.bottomsheet.SubjectAttendanceDataBottomSheet
import `in`.hridayan.driftly.calender.presentation.components.canvas.CalendarCanvas
import `in`.hridayan.driftly.calender.presentation.components.GroupedTimetableCards
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

fun calculateAttendanceInsight(
    presentCount: Int,
    totalCount: Int,
    targetPercentage: Float = 75f
): AttendanceInsight {
    // STEP 0: Get per-subject configuration
    val A = presentCount
    val C = totalCount
    val T = targetPercentage
    
    // STEP 1: Convert target percentage to fraction
    val P = T / 100.0
    
    // STEP 2: Calculate current attendance percentage (DISPLAY ONLY)
    val currentPercentage = if (C > 0) {
        (A.toDouble() / C.toDouble()) * 100
    } else {
        0.0
    }
    
    // STEP 3: Calculate BUNK COUNT (MOST IMPORTANT)
    val rawBunk = if (P > 0) (A / P) - C else 0.0
    val bunkCount = kotlin.math.floor(rawBunk).toInt().coerceAtLeast(0)
    
    // STEP 4: Calculate REQUIRED ATTEND COUNT (only if below target)
    val rawNeed = if (P < 1.0) ((P * C) - A) / (1 - P) else 0.0
    val requiredAttend = kotlin.math.ceil(rawNeed).toInt().coerceAtLeast(0)
    
    // STEP 5: Decide attendance state (STRICT FLOW)
    return when {
        C == 0 -> {
            // NO_DATA
            AttendanceInsight(
                message = "Attend your first class to get started",
                icon = Icons.Rounded.Info
            )
        }
        currentPercentage < T -> {
            // BELOW_TARGET
            AttendanceInsight(
                message = "Attend the next $requiredAttend ${if (requiredAttend == 1) "class" else "classes"} to reach ${T.toInt()}%",
                icon = Icons.Rounded.School
            )
        }
        bunkCount >= 1 -> {
            // CAN_BUNK
            AttendanceInsight(
                message = "You can bunk $bunkCount more ${if (bunkCount == 1) "class" else "classes"} and still stay above ${T.toInt()}%",
                icon = Icons.Rounded.CheckCircle
            )
        }
        else -> {
            // JUST_SAFE (bunkCount == 0)
            AttendanceInsight(
                message = "You're just safe at ${T.toInt()}%. Don't miss the next class",
                icon = Icons.Rounded.Warning
            )
        }
    }
}

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

    // Get attendance counts for insights
    val attendanceCounts by homeViewModel.getSubjectAttendanceCounts(subjectId)
        .collectAsState(initial = SubjectAttendance())

    // Fetch monthly attendance
    val monthlyAttendanceCounts by androidx.compose.runtime.remember(subjectId, year, month) {
        viewModel.getMonthlyAttendanceCounts(subjectId, year, month)
    }.collectAsState(initial = SubjectAttendance())

    var isMonthlyMode by rememberSaveable { mutableStateOf(false) }

    // Get subject's target percentage and setup status
    val targetPercentage = subjectEntity.value?.targetPercentage ?: 75.0f
    val isTargetSet = subjectEntity.value?.isTargetSet ?: false

    val insight = calculateAttendanceInsight(
        presentCount = attendanceCounts.presentCount,
        totalCount = attendanceCounts.totalCount,
        targetPercentage = targetPercentage
    )

    // Show target setup ONLY if target has never been set (isTargetSet == false)
    // Wait for subject to load from database first
    LaunchedEffect(subjectId) {
        // Small delay to ensure database has loaded
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
                navigationIcon = { BackButton() },
                actions = {
                    val infiniteTransition = rememberInfiniteTransition(label = "gear_rotation")
                    val rotation by infiniteTransition.animateFloat(
                        initialValue = 0f,
                        targetValue = 360f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(durationMillis = 3000, easing = LinearEasing),
                            repeatMode = RepeatMode.Restart
                        ),
                        label = "rotation"
                    )
                    
                    IconButton(
                        onClick = {
                            weakHaptic()
                            showTargetBottomSheet = true
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Settings,
                            contentDescription = "Attendance Target Settings",
                            modifier = Modifier.rotate(rotation)
                        )
                    }
                }
            )
        }) {
        // Collect schedules BEFORE LazyColumn
        val schedules by homeViewModel.getSchedulesForSubject(subjectId)
            .collectAsState(initial = emptyList())

        LazyColumn(
            modifier = Modifier.padding(it),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            flingBehavior = ScrollableDefaults.flingBehavior()
        ) {
            item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 15.dp),
                shape = RoundedCornerShape(25.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 0.dp
                )
            ) {
                CalendarCanvas(
                    modifier = Modifier.padding(20.dp),
                    year = year,
                    month = month,
                    markedDates = markedDates,
                    streakMap = streakMap,
                    onStatusChange = onStatusChange,
                    onNavigate = { newYear, newMonth ->
                        viewModel.updateMonthYear(newYear, newMonth)
                        viewModel.saveMonthYearForSubject(subjectId)
                    },
                    onResetMonth = {
                        viewModel.resetYearMonthToCurrent()
                        viewModel.saveMonthYearForSubject(subjectId)
                    }
                )
            }
            }

            item {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Overall Attendance Button
                    Button(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 15.dp),
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
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AutoResizeableText(
                                text = stringResource(R.string.attendance_overview),
                                style = MaterialTheme.typography.titleMedium
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                                contentDescription = null
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(5.dp))

                    // Monthly Attendance Button
                    Button(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 15.dp),
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
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AutoResizeableText(
                                text = "Monthly Attendance",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                                contentDescription = null
                            )
                        }
                    }
                }
            }

            item {
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

            if (schedules.isNotEmpty()) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                        Text(
                            text = "Weekly Schedule",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(horizontal = 25.dp)
                        )

                        val groupedByDay = schedules.groupBy { it.dayOfWeek }.toSortedMap()
                        groupedByDay.forEach { (day, daySchedules) ->
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                // Day Header Chip
                                Surface(
                                    color = MaterialTheme.colorScheme.tertiaryContainer,
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier
                                        .padding(horizontal = 25.dp)
                                        .padding(bottom = 6.dp)
                                ) {
                                    Text(
                                        text = getDayName(day),
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                        color = MaterialTheme.colorScheme.onTertiaryContainer
                                    )
                                }

                                val sorted = daySchedules.sortedBy { it.startTime }
                                sorted.forEachIndexed { index, schedule ->
                                    val isFirst = index == 0
                                    val isLast = index == sorted.size - 1
                                    val isOnly = sorted.size == 1

                                    val shape = when {
                                        isOnly -> RoundedCornerShape(25.dp)
                                        isFirst -> RoundedCornerShape(
                                            topStart = 25.dp, topEnd = 25.dp,
                                            bottomStart = 2.dp, bottomEnd = 2.dp
                                        )
                                        isLast -> RoundedCornerShape(
                                            topStart = 2.dp, topEnd = 2.dp,
                                            bottomStart = 25.dp, bottomEnd = 25.dp
                                        )
                                        else -> RoundedCornerShape(2.dp)
                                    }

                                    TimetableCardItem(
                                        schedule = schedule,
                                        shape = shape,
                                        modifier = Modifier.padding(horizontal = 25.dp)
                                    )
                                }
                            }
                        }
                        
                        // Bottom padding to ensure last item is visible above navigation bar/FAB
                        Spacer(Modifier.height(100.dp))
                    }
                }
            }
        }
        
        // Bottom Sheets OUTSIDE LazyColumn
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
    }
}

@Composable
private fun TimetableCardItem(
    schedule: `in`.hridayan.driftly.core.domain.model.ClassSchedule,
    shape: RoundedCornerShape,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = shape,
        color = Color.White,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Schedule,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                
                Column {
                    Text(
                        text = "${`in`.hridayan.driftly.core.utils.TimeUtils.format24To12Hour(schedule.startTime)} - ${`in`.hridayan.driftly.core.utils.TimeUtils.format24To12Hour(schedule.endTime)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Normal,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    if (schedule.location != null) {
                        Text(
                            text = "📍 ${schedule.location}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            Surface(
                color = MaterialTheme.colorScheme.secondaryContainer,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = `in`.hridayan.driftly.core.utils.TimeUtils.formatDuration(
                        `in`.hridayan.driftly.core.utils.TimeUtils.calculateDuration(schedule.startTime, schedule.endTime)
                    ),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

private fun getDayName(day: Int) = when (day) {
    1 -> "Monday"
    2 -> "Tuesday"
    3 -> "Wednesday"
    4 -> "Thursday"
    5 -> "Friday"
    6 -> "Saturday"
    7 -> "Sunday"
    else -> ""
}

