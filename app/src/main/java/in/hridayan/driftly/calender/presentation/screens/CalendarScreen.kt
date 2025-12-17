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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.toRoute
import `in`.hridayan.driftly.R
import `in`.hridayan.driftly.calender.presentation.components.bottomsheet.AttendanceTargetBottomSheet
import `in`.hridayan.driftly.calender.presentation.components.bottomsheet.SubjectAttendanceDataBottomSheet
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

        Column(
            modifier = Modifier.padding(it), verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
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

            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 15.dp),
                onClick = {
                    weakHaptic()
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

            // Smart Attendance Insight Card
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

            if (showSubjectAttendanceDataBottomSheet) {
                SubjectAttendanceDataBottomSheet(
                    onDismiss = {
                        showSubjectAttendanceDataBottomSheet = false
                    },
                    subjectId = subjectId
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
}
