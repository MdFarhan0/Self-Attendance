@file:OptIn(ExperimentalMaterial3Api::class)

package `in`.hridayan.driftly.calender.presentation.components.card

import android.annotation.SuppressLint
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import `in`.hridayan.driftly.R
import `in`.hridayan.driftly.calender.presentation.image.UndrawDatePicker
import `in`.hridayan.driftly.calender.presentation.viewmodel.CalendarViewModel
import `in`.hridayan.driftly.core.common.LocalWeakHaptic
import `in`.hridayan.driftly.core.domain.model.SubjectAttendance
import `in`.hridayan.driftly.core.presentation.components.progress.AnimatedHalfCircleProgress
import `in`.hridayan.driftly.core.presentation.theme.Shape
import `in`.hridayan.driftly.home.presentation.components.label.Label
import `in`.hridayan.driftly.home.presentation.viewmodel.HomeViewModel
import kotlinx.coroutines.launch

@Composable
fun AttendanceCardWithTabs(modifier: Modifier = Modifier, subjectId: Int) {
    val attendanceDataTabs =
        listOf(
            stringResource(R.string.this_month_data),
            stringResource(R.string.all_months_data)
        )

    val pagerState = rememberPagerState(pageCount = { attendanceDataTabs.size })
    val coroutineScope = rememberCoroutineScope()
    val selectedTabIndex = remember { derivedStateOf { pagerState.currentPage } }
    val weakHaptic = LocalWeakHaptic.current

    Card(
        modifier = modifier
            .clip(Shape.cardCornerLarge)
            .animateContentSize(
                animationSpec = tween(
                    durationMillis = 500,
                    easing = FastOutSlowInEasing
                )
            ),
        colors = CardDefaults.cardColors(containerColor = BottomSheetDefaults.ContainerColor)
    ) {
        Column {
            PrimaryTabRow(
                selectedTabIndex = selectedTabIndex.value,
                containerColor = BottomSheetDefaults.ContainerColor,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.fillMaxWidth()
            ) {
                attendanceDataTabs.forEachIndexed { index, currentTab ->
                    Tab(
                        selected = index == selectedTabIndex.value,
                        selectedContentColor = MaterialTheme.colorScheme.primaryContainer,
                        unselectedContentColor = MaterialTheme.colorScheme.outline,
                        onClick = {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(attendanceDataTabs.indexOf(currentTab))
                                weakHaptic()
                            }
                        },
                        text = {
                            Text(
                                text = currentTab,
                                color = if (index == selectedTabIndex.value) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                            )
                        }
                    )
                }
            }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxWidth()
            ) { index ->
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    when (index) {
                        0 -> ThisMonthView(
                            modifier = Modifier.padding(25.dp),
                            subjectId = subjectId
                        )

                        1 -> AllMonthsView(
                            modifier = Modifier.padding(25.dp),
                            subjectId = subjectId
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AllMonthsView(
    modifier: Modifier = Modifier,
    subjectId: Int,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val counts by viewModel.getSubjectAttendanceCounts(subjectId)
        .collectAsState(initial = SubjectAttendance())

    val progress = counts.presentCount.toFloat() / counts.totalCount.toFloat()

    if (counts.totalCount == 0) {
        UndrawDatePicker(modifier = modifier)
    } else {
        ProgressView(
            modifier = modifier,
            counts = counts,
            progress = progress
        )
    }
}

@Composable
private fun ThisMonthView(
    modifier: Modifier = Modifier,
    subjectId: Int,
    viewModel: CalendarViewModel = hiltViewModel()
) {
    val selectedMonthYear = viewModel.selectedMonthYear.value
    val counts by viewModel.getMonthlyAttendanceCounts(
        subjectId,
        selectedMonthYear.year,
        selectedMonthYear.month.value
    )
        .collectAsState(initial = SubjectAttendance())
    val progress = counts.presentCount.toFloat() / counts.totalCount.toFloat()

    if (counts.totalCount == 0) {
        UndrawDatePicker(modifier = modifier)
    } else {
        ProgressView(
            modifier = modifier,
            counts = counts,
            progress = progress
        )
    }
}

@SuppressLint("DefaultLocale")
@Composable
private fun ProgressView(
    modifier: Modifier = Modifier,
    counts: SubjectAttendance,
    progress: Float
) {
    val progressText = "${String.format("%.0f", progress * 100)}%"
    val progressColor = lerp(
        start = MaterialTheme.colorScheme.error,
        stop = MaterialTheme.colorScheme.primary,
        fraction = progress.coerceIn(0f, 1f)
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Large percentage display
        Text(
            text = progressText,
            style = MaterialTheme.typography.displayLarge.copy(
                fontWeight = androidx.compose.ui.text.font.FontWeight.ExtraBold,
                fontSize = 64.sp
            ),
            color = progressColor
        )
        
        Text(
            text = "Attendance Score",
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // Three stat cards
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Present card
            StatCard(
                modifier = Modifier.weight(1f),
                number = counts.presentCount.toString(),
                label = stringResource(R.string.present).uppercase(),
                backgroundColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                textColor = MaterialTheme.colorScheme.primary
            )
            
            // Absent card
            StatCard(
                modifier = Modifier.weight(1f),
                number = counts.absentCount.toString(),
                label = stringResource(R.string.absent).uppercase(),
                backgroundColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f),
                textColor = MaterialTheme.colorScheme.error
            )
            
            // Total card
            StatCard(
                modifier = Modifier.weight(1f),
                number = counts.totalCount.toString(),
                label = stringResource(R.string.total).uppercase(),
                backgroundColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f),
                textColor = MaterialTheme.colorScheme.tertiary
            )
        }
    }
}

@Composable
private fun StatCard(
    modifier: Modifier = Modifier,
    number: String,
    label: String,
    backgroundColor: androidx.compose.ui.graphics.Color,
    textColor: androidx.compose.ui.graphics.Color
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp, horizontal = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = number,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                ),
                color = textColor
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
                ),
                color = textColor.copy(alpha = 0.7f)
            )
        }
    }
}
