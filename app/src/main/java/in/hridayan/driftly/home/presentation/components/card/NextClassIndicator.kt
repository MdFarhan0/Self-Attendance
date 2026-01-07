package `in`.hridayan.driftly.home.presentation.components.card

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import `in`.hridayan.driftly.core.domain.model.ClassSchedule
import `in`.hridayan.driftly.core.utils.ScheduleUtils

@Composable
fun NextClassIndicator(
    schedules: List<ClassSchedule>,
    modifier: Modifier = Modifier
) {
    val nextClassText = ScheduleUtils.getNextClassDisplayText(schedules)
    
    if (nextClassText != null) {
        Row(
            modifier = modifier,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Schedule,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = nextClassText,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun TimetableStatusBadge(
    scheduleCount: Int,
    modifier: Modifier = Modifier
) {
    if (scheduleCount > 0) {
        Text(
            text = "$scheduleCount classes/week",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = modifier
        )
    }
}
