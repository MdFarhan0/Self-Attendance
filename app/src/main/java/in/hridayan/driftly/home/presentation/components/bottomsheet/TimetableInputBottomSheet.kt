package `in`.hridayan.driftly.home.presentation.components.bottomsheet

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import `in`.hridayan.driftly.core.common.LocalWeakHaptic
import `in`.hridayan.driftly.core.presentation.components.picker.WheelPicker
import `in`.hridayan.driftly.settings.data.local.SettingsKeys
import `in`.hridayan.driftly.settings.data.local.provider.settingsDataStore
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.datastore.preferences.core.intPreferencesKey
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TimetableInputBottomSheet(
    initialSchedule: `in`.hridayan.driftly.core.domain.model.ClassSchedule? = null,
    onDismiss: () -> Unit,
    onSave: (dayOfWeek: Int, startTime: String, endTime: String, location: String?) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)  // Open fully!
    val scope = rememberCoroutineScope()
    val weakHaptic = LocalWeakHaptic.current
    
    // Data Sources
    val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    val hours = (1..12).map { it.toString() }
    val minutes = (0..59 step 5).map { it.toString().padStart(2, '0') }
    val amPm = listOf("AM", "PM")

    // Define a local data class for initial state to avoid destructuring issues
    data class InitialState(
        val dayIndex: Int,
        val startHourIdx: Int,
        val startMinIdx: Int,
        val startAmPm: Int,
        val endHourIdx: Int,
        val endMinIdx: Int,
        val endAmPm: Int,
        val location: String
    )

    // Parse initial schedule if editing
    val initialState = remember(initialSchedule) {
        if (initialSchedule != null) {
            // Parse initial data
            val dayIndex = initialSchedule.dayOfWeek - 1 // dayOfWeek is 1-7, index 0-6
            
            // Parse start time (format: "HH:mm")
            val startParts = initialSchedule.startTime.split(":")
            val startHour24 = startParts[0].toInt()
            val startMinute = startParts[1].toInt()
            val startAmPmValue = if (startHour24 >= 12) 1 else 0
            val startHour12 = when {
                startHour24 == 0 -> 12
                startHour24 > 12 -> startHour24 - 12
                else -> startHour24
            }
            val startHourIdx = startHour12 - 1
            val startMinIdx = minutes.indexOfFirst { it.toInt() == startMinute }.coerceAtLeast(0)
            
            // Parse end time
            val endParts = initialSchedule.endTime.split(":")
            val endHour24 = endParts[0].toInt()
            val endMinute = endParts[1].toInt()
            val endAmPmValue = if (endHour24 >= 12) 1 else 0
            val endHour12 = when {
                endHour24 == 0 -> 12
                endHour24 > 12 -> endHour24 - 12
                else -> endHour24
            }
            val endHourIdx = endHour12 - 1
            val endMinIdx = minutes.indexOfFirst { it.toInt() == endMinute }.coerceAtLeast(0)
            
            InitialState(dayIndex, startHourIdx, startMinIdx, startAmPmValue, endHourIdx, endMinIdx, endAmPmValue, initialSchedule.location ?: "")
        } else {
            InitialState(0, 8, 0, 0, 9, 0, 0, "")
        }
    }

    // State
    var selectedDayIndex by remember { mutableIntStateOf(initialState.dayIndex) }
    var startHourIndex by remember { mutableIntStateOf(initialState.startHourIdx) }
    var startMinuteIndex by remember { mutableIntStateOf(initialState.startMinIdx) }
    var startAmPmIndex by remember { mutableIntStateOf(initialState.startAmPm) }
    var endHourIndex by remember { mutableIntStateOf(initialState.endHourIdx) }
    var endMinuteIndex by remember { mutableIntStateOf(initialState.endMinIdx) }
    var endAmPmIndex by remember { mutableIntStateOf(initialState.endAmPm) }
    var location by remember { mutableStateOf(initialState.location) }
    
    // Auto-fill Logic State (disable if editing)
    var isEndTimeManuallySet by remember { mutableStateOf(initialSchedule != null) }

    // Settings
    val context = LocalContext.current
    val settings = context.settingsDataStore.data.collectAsState(initial = null).value
    val defaultDuration = settings?.get(intPreferencesKey(SettingsKeys.DEFAULT_CLASS_DURATION.name)) ?: 60
    val defaultStartMin = settings?.get(intPreferencesKey(SettingsKeys.DEFAULT_START_MINUTE.name)) ?: 45
    
    // Initialize defaults on first load (only if NOT editing)
    LaunchedEffect(Unit) {
        if (initialSchedule == null) {
            // Find close match for default Start Minute (e.g. 45 -> index of "45")
            // minute list is 00, 05, ...
            val targetMin = defaultStartMin.toString().padStart(2, '0')
            val minIndex = minutes.indexOf(targetMin)
            if (minIndex != -1) {
                 startMinuteIndex = minIndex
            }
        }
    }

    // Auto-Calculate End Time when Start Time changes (if not manually set)
    LaunchedEffect(startHourIndex, startMinuteIndex, startAmPmIndex) {
        if (!isEndTimeManuallySet) {
            // Convert Start to Minutes
            val sHr = hours[startHourIndex].toInt()
            val sMin = minutes[startMinuteIndex].toInt()
            val sIsPm = startAmPmIndex == 1
            val sHr24 = when {
                sIsPm && sHr != 12 -> sHr + 12
                !sIsPm && sHr == 12 -> 0
                else -> sHr
            }
            val startTotalMin = sHr24 * 60 + sMin
            
            // Add Duration
            val endTotalMin = startTotalMin + defaultDuration
            
            // Convert back to Index
            val eHr24 = (endTotalMin / 60) % 24
            val eMin = endTotalMin % 60
            
            val eIsPm = eHr24 >= 12
            val eHr12 = when {
                eHr24 == 0 -> 12
                eHr24 > 12 -> eHr24 - 12
                else -> eHr24
            }
            
            // Map to indices
            val hIndex = hours.indexOf(eHr12.toString())
            // Find closest 5-step minute
            val mStr = (eMin / 5 * 5).toString().padStart(2, '0') // round down to nearest 5
            val mIndex = minutes.indexOf(mStr)
            
            if (hIndex != -1) endHourIndex = hIndex
            if (mIndex != -1) endMinuteIndex = mIndex
            endAmPmIndex = if (eIsPm) 1 else 0
        }
    }

    val mainScrollState = rememberScrollState()

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
                .verticalScroll(mainScrollState)
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp, top = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Large Title
            Text(
                text = "Add Class",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )

            // Day Selector — Row 1: Mon Tue Wed Thu (ButtonGroup + animateWidth)
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // First Row: Mon, Tue, Wed, Thu
                val dayRowOneSources = remember { List(4) { MutableInteractionSource() } }
                @Suppress("DEPRECATION")
                ButtonGroup(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    days.take(4).forEachIndexed { i, day ->
                        val isSelected = selectedDayIndex == i
                        Button(
                            onClick = {
                                weakHaptic()
                                selectedDayIndex = i
                            },
                            modifier = Modifier
                                .weight(1f)
                                .animateWidth(dayRowOneSources[i]),
                            interactionSource = dayRowOneSources[i],
                            shapes = ButtonDefaults.shapes(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        ) {
                            Text(
                                text = day,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(vertical = 6.dp)
                            )
                        }
                    }
                }

                // Second Row: Fri, Sat, Sun
                val dayRowTwoSources = remember { List(3) { MutableInteractionSource() } }
                @Suppress("DEPRECATION")
                ButtonGroup(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    days.drop(4).forEachIndexed { i, day ->
                        val index = i + 4
                        val isSelected = selectedDayIndex == index
                        Button(
                            onClick = {
                                weakHaptic()
                                selectedDayIndex = index
                            },
                            modifier = Modifier
                                .weight(1f)
                                .animateWidth(dayRowTwoSources[i]),
                            interactionSource = dayRowTwoSources[i],
                            shapes = ButtonDefaults.shapes(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        ) {
                            Text(
                                text = day,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(vertical = 6.dp)
                            )
                        }
                    }
                }
            }

            // Time Pickers Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // FROM Time
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "FROM",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp,
                        letterSpacing = 0.5.sp
                    )
                    
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                            .padding(vertical = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(140.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            WheelPicker(
                                items = hours,
                                initialIndex = startHourIndex,
                                visibleItemsCount = 3,
                                itemHeight = 46.dp,
                                onSelectionChanged = { startHourIndex = it },
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                ":",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold
                            )
                            WheelPicker(
                                items = minutes,
                                initialIndex = startMinuteIndex,
                                visibleItemsCount = 3,
                                itemHeight = 46.dp,
                                onSelectionChanged = { startMinuteIndex = it },
                                modifier = Modifier.weight(1f)
                            )
                            WheelPicker(
                                items = amPm,
                                initialIndex = startAmPmIndex,
                                visibleItemsCount = 3,
                                itemHeight = 46.dp,
                                onSelectionChanged = { startAmPmIndex = it },
                                modifier = Modifier.weight(0.9f)
                            )
                        }
                    }
                }
                
                // TO Time
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "TO",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp,
                        letterSpacing = 0.5.sp
                    )
                    
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                            .padding(vertical = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(140.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            WheelPicker(
                                items = hours,
                                initialIndex = endHourIndex,
                                visibleItemsCount = 3,
                                itemHeight = 46.dp,
                                onSelectionChanged = { 
                                    endHourIndex = it 
                                    isEndTimeManuallySet = true
                                },
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                ":",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold
                            )
                            WheelPicker(
                                items = minutes,
                                initialIndex = endMinuteIndex,
                                visibleItemsCount = 3,
                                itemHeight = 46.dp,
                                onSelectionChanged = { 
                                    endMinuteIndex = it 
                                    isEndTimeManuallySet = true
                                },
                                modifier = Modifier.weight(1f)
                            )
                            WheelPicker(
                                items = amPm,
                                initialIndex = endAmPmIndex,
                                visibleItemsCount = 3,
                                itemHeight = 46.dp,
                                onSelectionChanged = { 
                                    endAmPmIndex = it 
                                    isEndTimeManuallySet = true
                                },
                                modifier = Modifier.weight(0.9f)
                            )
                        }
                    }
                }
            }
            
            // ========== TIME CONVERSION: 12-hour to 24-hour ==========
            // This conversion is TIMEZONE-AGNOSTIC - it only deals with integers
            // No Date/Calendar objects are used to prevent timezone issues
            
            // START TIME CONVERSION
            val startHr = hours[startHourIndex].toInt()  // 1-12
            val startMin = minutes[startMinuteIndex].toInt()  // 0-59
            val startIsPm = startAmPmIndex == 1
            val startHour24 = when {
                startIsPm && startHr != 12 -> startHr + 12  // 1 PM = 13, 11 PM = 23
                !startIsPm && startHr == 12 -> 0            // 12 AM = 00
                else -> startHr                              // 1-11 AM = 1-11, 12 PM = 12
            }
            val startTotalMinutes = startHour24 * 60 + startMin

            // END TIME CONVERSION
            val endHr = hours[endHourIndex].toInt()  // 1-12
            val endMin = minutes[endMinuteIndex].toInt()  // 0-59
            val endIsPm = endAmPmIndex == 1
            val endHour24 = when {
                endIsPm && endHr != 12 -> endHr + 12  // 1 PM = 13, 11 PM = 23
                !endIsPm && endHr == 12 -> 0          // 12 AM = 00
                else -> endHr                          // 1-11 AM = 1-11, 12 PM = 12
            }
            val endTotalMinutes = endHour24 * 60 + endMin
            
            // VALIDATION: End time must be different from start time
            val isValid = endTotalMinutes != startTotalMinutes

            // Add to Timetable Button (with animateWidth)
            val addBtnSource = remember { MutableInteractionSource() }
            @Suppress("DEPRECATION")
            ButtonGroup(modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = {
                        weakHaptic()
                        val dayOfWeek = selectedDayIndex + 1
                        val startTime = String.format("%02d:%02d", startHour24, startMin)
                        val endTime = String.format("%02d:%02d", endHour24, endMin)
                        scope.launch {
                            onSave(dayOfWeek, startTime, endTime, null)
                            sheetState.hide()
                            onDismiss()
                        }
                    },
                    enabled = isValid,
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                        .animateWidth(addBtnSource),
                    interactionSource = addBtnSource,
                    shapes = ButtonDefaults.shapes(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = if (isValid) "Add to Timetable" else "Check Times",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        if (isValid) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DayPill(
    day: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.height(48.dp),
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
        contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
        tonalElevation = 0.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = day,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
