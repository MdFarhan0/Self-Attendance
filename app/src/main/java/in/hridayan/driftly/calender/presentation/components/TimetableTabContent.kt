package `in`.hridayan.driftly.calender.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import `in`.hridayan.driftly.core.domain.model.ClassSchedule
import `in`.hridayan.driftly.core.presentation.theme.Shape
import `in`.hridayan.driftly.core.utils.TimeUtils
import `in`.hridayan.driftly.home.presentation.components.dialog.AddClassTimeDialog
import `in`.hridayan.driftly.home.presentation.components.dialog.TimetableEntryDialog

@Composable
fun TimetableTabContent(
    subjectId: Int,
    schedules: List<ClassSchedule>,
    onSchedulesUpdate: (List<ClassSchedule>) -> Unit,
    modifier: Modifier = Modifier
) {
    var showTimetableDialog by remember { mutableStateOf(false) }
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedDay by remember { mutableStateOf(1) }
    var editingSchedule by remember { mutableStateOf<ClassSchedule?>(null) }

    Box(modifier = modifier.fillMaxSize()) {
        if (schedules.isEmpty()) {
            // Empty State
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    Icons.Default.Schedule,
                    contentDescription = null,
                    modifier = Modifier.size(80.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    "No Timetable Set",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "Add your weekly class schedule to receive automatic notifications",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                Spacer(Modifier.height(24.dp))
                Button(
                    onClick = { showTimetableDialog = true }
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Add Timetable")
                }
            }
        } else {
            // Schedule List
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(7.dp)
            ) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Weekly Schedule",
                            style = MaterialTheme.typography.titleMedium
                        )
                        FilledTonalButton(
                            onClick = { showTimetableDialog = true }
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Edit")
                        }
                    }
                }

                val groupedSchedules = schedules.groupBy { it.dayOfWeek }.toSortedMap()
                
                groupedSchedules.forEach { (dayOfWeek, daySchedules) ->
                    item(key = "day_header_$dayOfWeek") {
                        Text(
                            text = getDayName(dayOfWeek),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }

                    item(key = "day_group_$dayOfWeek") {
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            daySchedules.forEach { schedule ->
                                TimetableScheduleCard(
                                    schedule = schedule,
                                    onEdit = {
                                        editingSchedule = schedule
                                        selectedDay = schedule.dayOfWeek
                                        showAddDialog = true
                                    },
                                    onDelete = {
                                        val updated = schedules.filterNot { it.id == schedule.id }
                                        onSchedulesUpdate(updated)
                                    }
                                )
                            }
                        }
                    }
                }

                item {
                    Spacer(Modifier.height(80.dp)) // Space for FAB
                }
            }
        }
    }

    if (showTimetableDialog) {
        TimetableEntryDialog(
            subjectId = subjectId,
            initialSchedules = schedules,
            onDismiss = { showTimetableDialog = false },
            onSave = {
                onSchedulesUpdate(it)
                showTimetableDialog = false
            }
        )
    }

    if (showAddDialog) {
        AddClassTimeDialog(
            dayOfWeek = selectedDay,
            dayName = getDayName(selectedDay),
            subjectId = subjectId,
            existingSchedule = editingSchedule,
            onDismiss = {
                showAddDialog = false
                editingSchedule = null
            },
            onSave = { newSchedule ->
                val updated = if (editingSchedule != null) {
                    schedules.map { if (it.id == editingSchedule!!.id) newSchedule else it }
                } else {
                    schedules + newSchedule
                }
                onSchedulesUpdate(updated)
                showAddDialog = false
                editingSchedule = null
            }
        )
    }
}

@Composable
private fun TimetableScheduleCard(
    schedule: ClassSchedule,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Schedule,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "${TimeUtils.format24To12Hour(schedule.startTime)} - ${TimeUtils.format24To12Hour(schedule.endTime)}",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                
                if (schedule.location != null) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = schedule.location,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Spacer(Modifier.height(4.dp))
                Text(
                    text = TimeUtils.formatDuration(
                        TimeUtils.calculateDuration(schedule.startTime, schedule.endTime)
                    ),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row {
                IconButton(onClick = onEdit) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Edit",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

private fun getDayName(dayOfWeek: Int): String {
    return when (dayOfWeek) {
        1 -> "Monday"
        2 -> "Tuesday"
        3 -> "Wednesday"
        4 -> "Thursday"
        5 -> "Friday"
        6 -> "Saturday"
        7 -> "Sunday"
        else -> "Unknown"
    }
}
