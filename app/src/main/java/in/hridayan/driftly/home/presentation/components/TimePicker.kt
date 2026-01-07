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
    time: String,
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
                Text(
                    text = String.format("%02d:%02d %s", displayHour, minute, if (isPM) "PM" else "AM"),
                    style = MaterialTheme.typography.headlineMedium
                )

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
                    items(quickTimes) { (label, time24) ->
                        FilterChip(
                            selected = false,
                            onClick = { onTimeSelected(time24) },
                            label = { Text(label, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
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

                    NumberPicker(
                        value = minute,
                        onValueChange = { minute = it },
                        range = 0..59,
                        label = "Min",
                        step = 5
                    )

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
