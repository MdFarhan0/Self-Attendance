package `in`.hridayan.driftly.home.presentation.components.dialog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import `in`.hridayan.driftly.core.domain.model.ClassSchedule
import `in`.hridayan.driftly.core.utils.TimeUtils
import `in`.hridayan.driftly.home.presentation.components.bottomsheet.TimetableInputBottomSheet

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TimetableEntryDialog(
    subjectId: Int,
    initialSchedules: List<ClassSchedule> = emptyList(),
    onDismiss: () -> Unit,
    onSave: (List<ClassSchedule>) -> Unit
) {
    val schedules = remember { mutableStateListOf<ClassSchedule>().apply { addAll(initialSchedules) } }
    var showAddSheet by remember { mutableStateOf(false) }
    var editingSchedule by remember { mutableStateOf<ClassSchedule?>(null) }
    
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surfaceContainerLow)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Custom Header with Close and Save - NO TopAppBar for flat design
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 24.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onDismiss) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    
                    val saveButtonInteraction = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
                    Button(
                        onClick = { onSave(schedules.toList()) },
                        modifier = Modifier,
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 0.dp,
                            pressedElevation = 0.dp
                        )
                    ) {
                        Text(
                            "Save",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // Title Section
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .padding(bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Weekly Timetable",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        fontSize = 32.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${schedules.size} classes added",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.alpha(0.6f)
                    )
                }

                // Content
                if (schedules.isEmpty()) {
                    // Empty State - Centered
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Clock Icon Background Circle
                            Box(
                                modifier = Modifier
                                    .size(120.dp)
                                    .background(
                                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Schedule,
                                    contentDescription = null,
                                    modifier = Modifier.size(56.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                                )
                            }
                            
                            Spacer(Modifier.height(8.dp))
                            
                            Text(
                                "No classes yet",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                "Tap + to add your class schedule for\nthe week.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.alpha(0.7f),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                } else {
                    // Class List - ZERO ELEVATION cards
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(horizontal = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(0.dp),
                        contentPadding = PaddingValues(bottom = 100.dp)
                    ) {
                        val grouped = schedules.groupBy { it.dayOfWeek }.toSortedMap()
                        
                        grouped.forEach { (day, daySchedules) ->
                            item {
                                Text(
                                    text = getDayName(day),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
                                )
                            }
                            
                            item {
                                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    daySchedules.sortedBy { it.startTime }.forEachIndexed { index, schedule ->
                                val isFirst = index == 0
                                val isLast = index == daySchedules.size - 1
                                val isOnly = daySchedules.size == 1
                                
                                val shape = when {
                                    isOnly -> RoundedCornerShape(25.dp)
                                    isFirst -> RoundedCornerShape(
                                        topStart = 25.dp, topEnd = 25.dp,
                                        bottomStart = 2.dp, bottomEnd = 2.dp
                                    )
                                    isLast -> RoundedCornerShape(
                                        topStart = 2.dp, topEnd = 2.dp,
                                        bottomStart = 25.dp, bottomEnd = 25.dp
                                    )
                                    else -> RoundedCornerShape(2.dp)
                                }
                                
                                // ZERO ELEVATION Card - Same color as Add Class bottom sheet
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = shape,
                                    color = MaterialTheme.colorScheme.surface,
                                    tonalElevation = 0.dp,
                                    shadowElevation = 0.dp
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp, vertical = 12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            modifier = Modifier.weight(1f),
                                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.Schedule,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(24.dp)
                                            )
                                            Column {
                                                Text(
                                                    "${TimeUtils.format24To12Hour(schedule.startTime)} - ${TimeUtils.format24To12Hour(schedule.endTime)}",
                                                    style = MaterialTheme.typography.titleMedium,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                                if (schedule.location != null) {
                                                    Text(
                                                        "📍 ${schedule.location}",
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                            }
                                        }
                                        
                                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                            IconButton(onClick = { 
                                                editingSchedule = schedule
                                            }) {
                                                Icon(
                                                    Icons.Default.Edit,
                                                    "Edit",
                                                    tint = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                            
                                            IconButton(onClick = { schedules.remove(schedule) }) {
                                                Icon(
                                                    Icons.Default.Delete,
                                                    "Delete",
                                                    tint = MaterialTheme.colorScheme.error
                                                )
                                            }
                                        }
                                    }
                                }
                                
                                // Spacer handled by Column
                            }
                                }
                            }
                            item { Spacer(Modifier.height(7.dp)) }
                        }
                    }
                }
            }

            // Floating Add Button - Bottom Right with ZERO elevation
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.BottomEnd
            ) {
                val addClassInteraction = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
                Button(
                    onClick = { showAddSheet = true },
                    modifier = Modifier,
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 0.dp,
                        pressedElevation = 0.dp
                    ),
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp)
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Add Class",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Bottom Sheets
            if (showAddSheet) {
                TimetableInputBottomSheet(
                    onDismiss = { showAddSheet = false },
                    onSave = { day, start, end, loc ->
                        schedules.add(
                            ClassSchedule(
                                subjectId = subjectId,
                                dayOfWeek = day,
                                startTime = start,
                                endTime = end,
                                location = loc
                            )
                        )
                        showAddSheet = false
                    }
                )
            }

            if (editingSchedule != null) {
                val schedule = editingSchedule!!
                TimetableInputBottomSheet(
                    initialSchedule = schedule,
                    onDismiss = { editingSchedule = null },
                    onSave = { day, start, end, loc ->
                        val index = schedules.indexOf(schedule)
                        if (index != -1) {
                            schedules[index] = schedule.copy(
                                dayOfWeek = day,
                                startTime = start,
                                endTime = end,
                                location = loc
                            )
                        }
                        editingSchedule = null
                    }
                )
            }
        }
    }
}

private fun getDayName(day: Int) = when (day) {
    1 -> "Monday"
    2 -> "Tuesday"
    3 -> "Wednesday"
    4 -> "Thursday"
    5 -> "Friday"
    6 -> "Saturday"
    7 -> "Sunday"
    else -> ""
}
