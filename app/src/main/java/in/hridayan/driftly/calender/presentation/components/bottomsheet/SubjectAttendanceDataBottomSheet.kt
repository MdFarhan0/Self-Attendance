@file:OptIn(ExperimentalMaterial3Api::class)

package `in`.hridayan.driftly.calender.presentation.components.bottomsheet

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import `in`.hridayan.driftly.R
import `in`.hridayan.driftly.core.domain.model.SubjectAttendance
import `in`.hridayan.driftly.home.presentation.viewmodel.HomeViewModel

@Composable
fun SubjectAttendanceDataBottomSheet(
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit = {},
    data: SubjectAttendance,
    title: String = "Attendance Overview"
) {
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = false
    )

    ModalBottomSheet(
        modifier = modifier,
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        shape = RoundedCornerShape(topStart = 25.dp, topEnd = 25.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 25.dp)
                .padding(bottom = 30.dp, top = 10.dp)
        ) {
            // Title
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 20.dp)
            )

            // Stats content
            AttendanceStats(counts = data)
        }
    }
}

@SuppressLint("DefaultLocale")
@Composable
private fun AttendanceStats(
    counts: SubjectAttendance
) {
    val progress = if (counts.totalCount > 0) {
        counts.presentCount.toFloat() / counts.totalCount.toFloat()
    } else {
        0f
    }
    
    val progressText = "${String.format("%.0f", progress * 100)}%"
    val progressColor = lerp(
        start = MaterialTheme.colorScheme.error,
        stop = MaterialTheme.colorScheme.primary,
        fraction = progress.coerceIn(0f, 1f)
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Large percentage display
        Text(
            text = progressText,
            style = MaterialTheme.typography.displayLarge.copy(
                fontWeight = FontWeight.ExtraBold,
                fontSize = 64.sp
            ),
            color = progressColor
        )
        
        Text(
            text = "Attendance Score",
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Medium
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(5.dp))

        // Three stat cards
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 5.dp),
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
        shape = RoundedCornerShape(16.dp)
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
                    fontWeight = FontWeight.Bold
                ),
                color = textColor
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = textColor.copy(alpha = 0.7f)
            )
        }
    }
}