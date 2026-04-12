@file:OptIn(ExperimentalMaterial3Api::class)

package `in`.hridayan.driftly.calender.presentation.components.bottomsheet

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import `in`.hridayan.driftly.core.domain.model.ClassSchedule
import `in`.hridayan.driftly.core.utils.TimeUtils

private fun getDayName(dayOfWeek: Int) = when (dayOfWeek) {
    1 -> "Monday"; 2 -> "Tuesday"; 3 -> "Wednesday"
    4 -> "Thursday"; 5 -> "Friday"; 6 -> "Saturday"; 7 -> "Sunday"
    else -> "Unknown"
}

@Composable
fun TimetableBottomSheet(
    subjectName: String,
    schedules: List<ClassSchedule>,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scrollState = rememberScrollState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        scrimColor = MaterialTheme.colorScheme.scrim.copy(alpha = 0.6f),
        tonalElevation = 0.dp,
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.padding(horizontal = 15.dp, vertical = 16.dp),
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(scrollState)
                .padding(horizontal = 10.dp)
                .padding(bottom = 28.dp, top = 16.dp)
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Text(
                text = "Timetable",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )
            Text(
                text = subjectName,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(20.dp))

            if (schedules.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.CalendarMonth,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier.size(52.dp)
                        )
                        Text(
                            text = "No timetable set",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Go to Settings → Timetable to add your class schedule.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                // Sort all schedules: by day first, then by start time within each day
                val sorted = schedules.sortedWith(compareBy({ it.dayOfWeek }, { it.startTime }))

                Column(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 3.dp),
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    val total = sorted.size
                    sorted.forEachIndexed { index, schedule ->
                        val shape = when {
                            total == 1 -> RoundedCornerShape(13.dp)
                            index == 0 -> RoundedCornerShape(topStart = 13.dp, topEnd = 13.dp, bottomStart = 2.dp, bottomEnd = 2.dp)
                            index == total - 1 -> RoundedCornerShape(topStart = 2.dp, topEnd = 2.dp, bottomStart = 13.dp, bottomEnd = 13.dp)
                            else -> RoundedCornerShape(2.dp)
                        }
                        TimetableRowCard(schedule = schedule, shape = shape)
                    }
                }
            }
        }
    }
}

@Composable
private fun TimetableRowCard(
    schedule: ClassSchedule,
    shape: androidx.compose.ui.graphics.Shape,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = shape,
        color = MaterialTheme.colorScheme.primaryContainer,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Day name — left
            Text(
                text = getDayName(schedule.dayOfWeek),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.weight(1f)
            )

            Text(
                text = "${TimeUtils.format24To12Hour(schedule.startTime)} - ${TimeUtils.format24To12Hour(schedule.endTime)}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f),
                modifier = Modifier.weight(1.6f),
                textAlign = TextAlign.End
            )
        }
    }
}
