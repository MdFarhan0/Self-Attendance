# Work 1: Complete Implementation Code Templates

This document contains ALL the code needed to complete Work 1 to 100%.
Copy each section into the appropriate file.

---

## PROGRESS SUMMARY

**✅ COMPLETED (35%):**
1. Database layer - ClassScheduleEntity, DAO, Repository
2. Domain models - ClassSchedule, NotificationMode  
3. Utilities - TimeUtils
4. Settings keys - NOTIFICATION_MODE, GRACE_PERIOD_MINUTES

**📝 THIS DOCUMENT PROVIDES (remaining 65%):**
5. UI Components
6. ViewModels & State Management
7. Screen Integrations
8. Notification System

---

## SECTION 1: TIME PICKER COMPONENT

### File: `app/src/main/java/in/hridayan/driftly/home/presentation/components/TimePicker.kt`

```kotlin
package `in`.hridayan.driftly.home.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import `in`.hridayan.driftly.core.presentation.theme.Shape
import `in`.hridayan.driftly.core.utils.TimeUtils

@Composable
fun TimePickerField(
    label: String,
    time: String, // Format: "HH:mm"
    onTimeChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showDialog by remember { mutableStateOf(false) }
    val displayTime = TimeUtils.format24To12Hour(time)

    OutlinedTextField(
        value = displayTime,
        onValueChange = {},
        modifier = modifier.clickable { showDialog = true },
        label = { Text(label) },
        enabled = false,
        colors = OutlinedTextFieldDefaults.colors(
            disabledTextColor = MaterialTheme.colorScheme.onSurface,
            disabledBorderColor = MaterialTheme.colorScheme.outline,
            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
        ),
        trailingIcon = {
            Icon(Icons.Default.AccessTime, contentDescription = "Select time")
        }
    )

    if (showDialog) {
        TimePickerDialog(
            initialTime = time,
            onDismiss = { showDialog = false },
            onTimeSelected = {
                onTimeChange(it)
                showDialog = false
            }
        )
    }
}

@Composable
private fun TimePickerDialog(
    initialTime: String,
    onDismiss: () -> Unit,
    onTimeSelected: (String) -> Unit
) {
    var hour by remember { mutableStateOf(TimeUtils.getHour(initialTime)) }
    var minute by remember { mutableStateOf(TimeUtils.getMinute(initialTime)) }
    var isPM by remember { mutableStateOf(hour >= 12) }

    // Convert to 12-hour format for display
    val displayHour = when {
        hour == 0 -> 12
        hour > 12 -> hour - 12
        else -> hour
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Time") },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Time display
                Text(
                    text = String.format("%02d:%02d %s", displayHour, minute, if (isPM) "PM" else "AM"),
                    style = MaterialTheme.typography.headlineMedium
                )

                // Quick select chips
                Text("Quick Select", style = MaterialTheme.typography.labelMedium)
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val quickTimes = listOf(
                        "8:00 AM" to "08:00",
                        "9:00 AM" to "09:00",
                        "10:00 AM" to "10:00",
                        "11:00 AM" to "11:00",
                        "12:00 PM" to "12:00",
                        "1:00 PM" to "13:00",
                        "2:00 PM" to "14:00",
                        "3:00 PM" to "15:00",
                        "4:00 PM" to "16:00"
                    )
                    items(quickTimes) { (label, time24) =>
                        FilterChip(
                            selected = false,
                            onClick = {
                                onTimeSelected(time24)
                            },
                            label = { Text(label, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }

                // Hour, Minute, AM/PM selectors
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Hour selector
                    NumberPicker(
                        value = displayHour,
                        onValueChange = {
                            var newHour = it
                            if (isPM && it != 12) newHour += 12
                            if (!isPM && it == 12) newHour = 0
                            hour = newHour
                        },
                        range = 1..12,
                        label = "Hour"
                    )

                    Text(":", style = MaterialTheme.typography.headlineSmall)

                    // Minute selector
                    NumberPicker(
                        value = minute,
                        onValueChange = { minute = it },
                        range = 0..59,
                        label = "Min",
                        step = 5
                    )

                    // AM/PM toggle
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Period", style = MaterialTheme.typography.labelSmall)
                        Row(
                            modifier = Modifier
                                .clip(Shape.cardCornerMedium)
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            listOf(false to "AM", true to "PM").forEach { (pm, label) ->
                                Box(
                                    modifier = Modifier
                                        .clickable {
                                            if (isPM != pm) {
                                                isPM = pm
                                                // Adjust hour
                                                if (pm) {
                                                    if (hour < 12) hour += 12
                                                } else {
                                                    if (hour >= 12) hour -= 12
                                                }
                                            }
                                        }
                                        .background(
                                            if (isPM == pm)
                                                MaterialTheme.colorScheme.primary
                                            else
                                                MaterialTheme.colorScheme.surfaceVariant
                                        )
                                        .padding(horizontal = 16.dp, vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        label,
                                        color = if (isPM == pm)
                                            MaterialTheme.colorScheme.onPrimary
                                        else
                                            MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val time24 = TimeUtils.formatTime(hour, minute)
                onTimeSelected(time24)
            }) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun NumberPicker(
    value: Int,
    onValueChange: (Int) -> Unit,
    range: IntRange,
    label: String,
    step: Int = 1,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(label, style = MaterialTheme.typography.labelSmall)
        
        IconButton(
            onClick = {
                val newValue = value + step
                if (newValue <= range.last) {
                    onValueChange(newValue)
                } else {
                    onValueChange(range.first)
                }
            }
        ) {
            Icon(Icons.Default.KeyboardArrowUp, "Increase")
        }

        Text(
            text = String.format("%02d", value),
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier
                .background(
                    MaterialTheme.colorScheme.surfaceVariant,
                    Shape.cardCornerSmall
                )
                .padding(horizontal = 16.dp, vertical = 8.dp)
        )

        IconButton(
            onClick = {
                val newValue = value - step
                if (newValue >= range.first) {
                    onValueChange(newValue)
                } else {
                    onValueChange(range.last)
                }
            }
        ) {
            Icon(Icons.Default.KeyboardArrowDown, "Decrease")
        }
    }
}
```

---

## SECTION 2: ADD CLASS DIALOG

### File: `app/src/main/java/in/hridayan/driftly/home/presentation/components/dialog/AddClassTimeDialog.kt`

```kotlin
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
    existingSchedule: ClassSchedule? = null,
    onDismiss: () -> Unit,
    onSave: (ClassSchedule) -> Unit
) {
    var startTime by remember {
        mutableStateOf(existingSchedule?.startTime ?: "09:00")
    }
    var endTime by remember {
        mutableStateOf(existingSchedule?.endTime ?: "10:00")
    }
    var location by remember {
        mutableStateOf(existingSchedule?.location ?: "")
    }
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

            // Start Time
            TimePickerField(
                label = "Start Time",
                time = startTime,
                onTimeChange = {
                    startTime = it
                    showError = false
                },
                modifier = Modifier.fillMaxWidth()
            )

            // End Time
            TimePickerField(
                label = "End Time",
                time = endTime,
                onTimeChange = {
                    endTime = it
                    showError = false
                },
                modifier = Modifier.fillMaxWidth()
            )

            // Duration display
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

            // Location (optional)
            OutlinedTextField(
                value = location,
                onValueChange = { location = it },
                label = { Text("Location (Optional)") },
                placeholder = { Text("e.g., Lab 301, Room 205") },
                modifier = Modifier.fillMaxWidth()
            )

            // Buttons
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
                                    subjectId = existingSchedule?.subjectId ?: 0,
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
```

---

**NOTE:** This file is getting very long. The complete implementation would require approximately 15-20 more files with similar detail.

**Estimated total lines of code remaining:** ~3,500-4,000 lines

Would you like me to:
1. **Continue with more code templates in subsequent responses** (this will take multiple exchanges)
2. **Provide a high-level architecture guide** so you/another dev can implement
3. **Focus on specific priority components** (e.g., just the timetable UI, or just notifications)

Let me know how you'd like to proceed! 🚀
