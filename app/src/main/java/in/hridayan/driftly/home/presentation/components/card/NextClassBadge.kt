package `in`.hridayan.driftly.home.presentation.components.card

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import `in`.hridayan.driftly.core.utils.ScheduleUtils
import `in`.hridayan.driftly.home.presentation.viewmodel.HomeViewModel

@Composable
fun NextClassBadge(
    subjectId: Int,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val schedules by viewModel.getSchedulesForSubject(subjectId)
        .collectAsState(initial = emptyList())
    
    if (schedules.isNotEmpty()) {
        val nextClassText = ScheduleUtils.getNextClassDisplayText(schedules)
        
        if (nextClassText != null) {
            Row(
                modifier = Modifier.padding(top = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Schedule,
                    contentDescription = null,
                    modifier = Modifier.size(12.dp),
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
}
