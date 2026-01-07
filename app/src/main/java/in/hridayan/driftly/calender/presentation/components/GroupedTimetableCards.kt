package `in`.hridayan.driftly.calender.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import `in`.hridayan.driftly.core.domain.model.ClassSchedule
import `in`.hridayan.driftly.core.utils.TimeUtils

@Composable
fun GroupedTimetableCards(
    schedules: List<ClassSchedule>,
    modifier: Modifier = Modifier
) {
    if (schedules.isEmpty()) return
    
    // Group by day and sort
    val groupedByDay = schedules
        .groupBy { it.dayOfWeek }
        .toSortedMap()
    
    // Use Column with wrapContentHeight to avoid infinite height
    Column(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()  // KEY FIX: Use wrapContent instead of unbounded height
            .padding(horizontal = 25.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Weekly Schedule",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        groupedByDay.forEach { (dayOfWeek, daySchedules) ->
            DayScheduleGroup(
                dayOfWeek = dayOfWeek,
                schedules = daySchedules.sortedBy { it.startTime }
            )
        }
    }
}

@Composable
private fun DayScheduleGroup(
    dayOfWeek: Int,
    schedules: List<ClassSchedule>
) {
    val dayName = when (dayOfWeek) {
        1 -> "Monday"
        2 -> "Tuesday"
        3 -> "Wednesday"
        4 -> "Thursday"
        5 -> "Friday"
        6 -> "Saturday"
        7 -> "Sunday"
        else -> "Unknown"
    }
    
    Column(
        modifier = Modifier.wrapContentHeight(),  // Also wrap content here
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        // Render grouped cards with special corner rounding
        schedules.forEachIndexed { index, schedule ->
            val isFirst = index == 0
            val isLast = index == schedules.size - 1
            val isOnly = schedules.size == 1
            
            // Determine corner radius based on position
            val shape = when {
                isOnly -> RoundedCornerShape(25.dp)
                isFirst -> RoundedCornerShape(
                    topStart = 25.dp,
                    topEnd = 25.dp,
                    bottomStart = 2.dp,
                    bottomEnd = 2.dp
                )
                isLast -> RoundedCornerShape(
                    topStart = 2.dp,
                    topEnd = 2.dp,
                    bottomStart = 25.dp,
                    bottomEnd = 25.dp
                )
                else -> RoundedCornerShape(2.dp)
            }
            
            // ZERO ELEVATION Card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = shape,
                color = Color.White,
                tonalElevation = 0.dp,
                shadowElevation = 0.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
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
                            // Day - Show on first card of group
                            if (isFirst) {
                                Text(
                                    text = dayName,
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            
                            // Time FROM - TO
                            Text(
                                text = "${TimeUtils.format24To12Hour(schedule.startTime)} - ${TimeUtils.format24To12Hour(schedule.endTime)}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            
                            // Location
                            if (schedule.location != null) {
                                Text(
                                    text = "📍 ${schedule.location}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                    
                    // Duration
                    Text(
                        text = TimeUtils.formatDuration(
                            TimeUtils.calculateDuration(schedule.startTime, schedule.endTime)
                        ),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
            }
            
            // 1mm gap between cards (approximately 3dp)
            if (!isLast) {
                Spacer(modifier = Modifier.height(3.dp))
            }
        }
    }
}
