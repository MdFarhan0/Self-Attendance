package `in`.hridayan.driftly.home.presentation.components.dialog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import `in`.hridayan.driftly.core.domain.model.ClassSchedule
import `in`.hridayan.driftly.core.presentation.theme.Shape
import `in`.hridayan.driftly.core.utils.TimeUtils
import `in`.hridayan.driftly.home.presentation.components.TimePickerField

@Composable
fun AddClassTimeDialog(
    dayOfWeek: Int,
    dayName: String,
    subjectId: Int,
    existingSchedule: ClassSchedule? = null,
    onDismiss: () -> Unit,
    onSave: (ClassSchedule) -> Unit
) {
    var startTime by remember { mutableStateOf(existingSchedule?.startTime ?: "09:00") }
    var endTime by remember { mutableStateOf(existingSchedule?.endTime ?: "10:00") }
    var location by remember { mutableStateOf(existingSchedule?.location ?: "") }
    var showError by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(Shape.cardCornerLarge)
                .background(MaterialTheme.colorScheme.surfaceContainer)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = if (existingSchedule != null) "Edit Class - $dayName" else "Add Class - $dayName",
                style = MaterialTheme.typography.titleLarge
            )

            TimePickerField(
                label = "Start Time",
                time = startTime,
                onTimeChange = {
                    startTime = it
                    showError = false
                },
                modifier = Modifier.fillMaxWidth()
            )

            TimePickerField(
                label = "End Time",
                time = endTime,
                onTimeChange = {
                    endTime = it
                    showError = false
                },
                modifier = Modifier.fillMaxWidth()
            )

            if (TimeUtils.isValidTimeRange(startTime, endTime)) {
                val duration = TimeUtils.calculateDuration(startTime, endTime)
                Text(
                    text = "Duration: ${TimeUtils.formatDuration(duration)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else if (showError) {
                Text(
                    text = "End time must be after start time",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }

            OutlinedTextField(
                value = location,
                onValueChange = { location = it },
                label = { Text("Location (Optional)") },
                placeholder = { Text("e.g., Lab 301, Room 205") },
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Cancel")
                }

                Button(
                    onClick = {
                        if (TimeUtils.isValidTimeRange(startTime, endTime)) {
                            onSave(
                                ClassSchedule(
                                    id = existingSchedule?.id ?: 0,
                                    subjectId = subjectId,
                                    dayOfWeek = dayOfWeek,
                                    startTime = startTime,
                                    endTime = endTime,
                                    location = location.trim().takeIf { it.isNotBlank() }
                                )
                            )
                        } else {
                            showError = true
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(if (existingSchedule != null) "Update" else "Add")
                }
            }
        }
    }
}
