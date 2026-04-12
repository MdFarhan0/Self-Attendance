@file:OptIn(ExperimentalMaterial3Api::class)

package `in`.hridayan.driftly.home.presentation.components.bottomsheet

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.School
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import `in`.hridayan.driftly.calender.presentation.screens.calculateAttendanceInsight
import `in`.hridayan.driftly.core.domain.model.SubjectAttendance
import `in`.hridayan.driftly.home.presentation.viewmodel.HomeViewModel

@Composable
fun BunkDetailsBottomSheet(
    onDismiss: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val subjects by viewModel.subjectList.collectAsState(initial = emptyList())
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
                .navigationBarsPadding()
                .padding(horizontal = 10.dp)
                .padding(bottom = 28.dp, top = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Text(
                text = "Bunk Details",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Smart attendance insights for all subjects",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Subject cards
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                subjects.forEachIndexed { index, subject ->
                    val counts by viewModel.getSubjectAttendanceCounts(subject.id)
                        .collectAsState(initial = SubjectAttendance())

                    val insight = calculateAttendanceInsight(
                        presentCount = counts.presentCount,
                        totalCount = counts.totalCount,
                        targetPercentage = subject.targetPercentage
                    )

                    val totalSubjects = subjects.size
                    val shape = when {
                        totalSubjects == 1 -> RoundedCornerShape(13.dp)
                        index == 0 -> RoundedCornerShape(topStart = 13.dp, topEnd = 13.dp, bottomStart = 2.dp, bottomEnd = 2.dp)
                        index == totalSubjects - 1 -> RoundedCornerShape(topStart = 2.dp, topEnd = 2.dp, bottomStart = 13.dp, bottomEnd = 13.dp)
                        else -> RoundedCornerShape(2.dp)
                    }

                    // Card colors: standardized with the others
                    val containerColor = MaterialTheme.colorScheme.primaryContainer
                    val contentColor = MaterialTheme.colorScheme.onPrimaryContainer

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = shape,
                        color = containerColor,
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
                            Column(modifier = Modifier.weight(1f)) {
                                // Subject name
                                Text(
                                    text = subject.subject,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = contentColor
                                )
                                // Short, concise indicator
                                Text(
                                    text = insight.message,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = contentColor.copy(alpha = 0.8f)
                                )
                            }

                            Icon(
                                imageVector = insight.icon,
                                contentDescription = null,
                                tint = contentColor.copy(alpha = 0.9f),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
