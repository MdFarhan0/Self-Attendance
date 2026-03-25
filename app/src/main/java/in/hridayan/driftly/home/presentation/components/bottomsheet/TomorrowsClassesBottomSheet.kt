@file:OptIn(ExperimentalMaterial3Api::class)

package `in`.hridayan.driftly.home.presentation.components.bottomsheet

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import `in`.hridayan.driftly.core.data.model.SubjectEntity
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun TomorrowsClassesBottomSheet(
    tomorrowsClasses: List<TodayClass>,
    onDismiss: () -> Unit,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
) {
    val tomorrow = LocalDate.now().plusDays(1)
    val dayOfWeek = tomorrow.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault())
    val dayAndDate = tomorrow.format(DateTimeFormatter.ofPattern("MMMM d", Locale.getDefault()))
    val classCount = tomorrowsClasses.size
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
                text = "Tomorrow's Classes",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "$dayOfWeek, $dayAndDate • $classCount ${if (classCount == 1) "Class" else "Classes"}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(20.dp))

            if (tomorrowsClasses.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No classes scheduled for tomorrow 🎉",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                // Sorted by start time
                val sorted = tomorrowsClasses.sortedBy { it.startTime }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(scrollState),
                    verticalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    sorted.forEachIndexed { index, classItem ->
                        val isFirst = index == 0
                        val isLast = index == sorted.size - 1
                        val isOnly = sorted.size == 1

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

                        TomorrowClassCard(classItem = classItem, shape = shape)
                    }
                }
            }
        }
    }
}

@Composable
private fun TomorrowClassCard(
    classItem: TodayClass,
    shape: RoundedCornerShape
) {
    val timeFmt = DateTimeFormatter.ofPattern("hh:mm a", Locale.getDefault())
    val durationText = when {
        classItem.duration >= 60 -> "${classItem.duration / 60}h"
            .let { if (classItem.duration % 60 > 0) "$it ${classItem.duration % 60}m" else it }
        else -> "${classItem.duration}m"
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = shape,
        color = MaterialTheme.colorScheme.secondaryContainer,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                // Subject name
                Text(
                    text = classItem.subject.subject,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Spacer(modifier = Modifier.height(2.dp))
                // Time range below
                Text(
                    text = "${classItem.startTime.format(timeFmt)} - ${classItem.endTime.format(timeFmt)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                )
            }

            // Duration chip on right
            Surface(
                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.12f),
                shape = RoundedCornerShape(50.dp)
            ) {
                Text(
                    text = durationText,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                )
            }
        }
    }
}
