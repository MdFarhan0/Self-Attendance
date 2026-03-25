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
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    val subjects by viewModel.subjectList.collectAsState(initial = emptyList())
    val scrollState = rememberScrollState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        tonalElevation = 0.dp,
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.padding(horizontal = 15.dp, vertical = 16.dp),
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp, top = 25.dp)
        ) {
            // Header
            Text(
                text = "Bunk Details",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Smart attendance insights for all subjects",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Subject cards
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                subjects.forEachIndexed { index, subject ->
                    val counts by viewModel.getSubjectAttendanceCounts(subject.id)
                        .collectAsState(initial = SubjectAttendance())

                    val insight = calculateAttendanceInsight(
                        presentCount = counts.presentCount,
                        totalCount = counts.totalCount,
                        targetPercentage = subject.targetPercentage
                    )

                    val isFirst = index == 0
                    val isLast = index == subjects.size - 1
                    val isOnly = subjects.size == 1

                    val shape = when {
                        isOnly -> RoundedCornerShape(25.dp)
                        isFirst -> RoundedCornerShape(
                            topStart = 25.dp, topEnd = 25.dp,
                            bottomStart = 10.dp, bottomEnd = 10.dp
                        )
                        isLast -> RoundedCornerShape(
                            topStart = 10.dp, topEnd = 10.dp,
                            bottomStart = 25.dp, bottomEnd = 25.dp
                        )
                        else -> RoundedCornerShape(10.dp)
                    }

                    // Determine card tone: can bunk → primaryContainer, must attend → errorContainer
                    val canBunk = insight.icon == Icons.Rounded.CheckCircle
                    val containerColor = if (canBunk)
                        MaterialTheme.colorScheme.primaryContainer
                    else
                        MaterialTheme.colorScheme.errorContainer
                    val contentColor = if (canBunk)
                        MaterialTheme.colorScheme.onPrimaryContainer
                    else
                        MaterialTheme.colorScheme.onErrorContainer

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = shape,
                        color = containerColor,
                        tonalElevation = 0.dp,
                        shadowElevation = 0.dp
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 18.dp, vertical = 14.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            // Subject name
                            Text(
                                text = subject.subject,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = contentColor
                            )

                            // Insight message with icon
                            Row(
                                verticalAlignment = Alignment.Top,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = if (canBunk) Icons.Rounded.CheckCircle else Icons.Rounded.School,
                                    contentDescription = null,
                                    tint = contentColor,
                                    modifier = Modifier.size(16.dp).padding(top = 1.dp)
                                )
                                Text(
                                    text = insight.message,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = contentColor,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
