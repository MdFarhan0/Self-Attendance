@file:OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)

package `in`.hridayan.driftly.home.presentation.components.dialog

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.viewModelScope
import `in`.hridayan.driftly.R
import `in`.hridayan.driftly.core.common.LocalWeakHaptic
import `in`.hridayan.driftly.core.domain.model.ClassSchedule
import `in`.hridayan.driftly.core.domain.model.SubjectError
import `in`.hridayan.driftly.core.utils.TimeUtils
import `in`.hridayan.driftly.home.presentation.viewmodel.HomeViewModel
import `in`.hridayan.driftly.core.presentation.components.dialog.ConfirmDeleteDialog
import `in`.hridayan.driftly.core.presentation.components.button.BackButton
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@Composable
fun AddSubjectDialog(
    modifier: Modifier = Modifier,
    subjectId: Int? = null,
    initialSubject: String? = null,
    initialSubjectCode: String? = null,
    viewModel: HomeViewModel = hiltViewModel(),
    onDismiss: () -> Unit
) {
    val subject by viewModel.subject.collectAsState()
    val subjectCode by viewModel.subjectCode.collectAsState()
    val histogramLabel by viewModel.histogramLabel.collectAsState()
    val attendedCount by viewModel.attendedCount.collectAsState()
    val missedCount by viewModel.missedCount.collectAsState()
    val subjectError by viewModel.subjectError.collectAsState()
    val weakHaptic = LocalWeakHaptic.current

    var timetableSchedules by remember { mutableStateOf<List<ClassSchedule>>(emptyList()) }

    LaunchedEffect(subjectId) {
        if (subjectId != null) {
            initialSubject?.let { viewModel.onSubjectChange(it) }
            initialSubjectCode?.let { viewModel.onSubjectCodeChange(it) }
            val entity = viewModel.getSubjectById(subjectId).first()
            entity?.let {
                viewModel.onSubjectChange(it.subject)
                viewModel.onSubjectCodeChange(it.subjectCode ?: "")
                viewModel.onHistogramLabelChange(it.histogramLabel ?: "")
                viewModel.onAttendedCountChange(it.attendedCount.toString())
                viewModel.onMissedCountChange(it.missedCount.toString())
            }
            timetableSchedules = viewModel.getSchedulesForSubject(subjectId).first()
        }
    }

    Dialog(
        onDismissRequest = {
            viewModel.resetInputFields()
            onDismiss()
        },
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false),
    ) {
        val scrollState = rememberScrollState()
        val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())

        Scaffold(
            modifier = Modifier.fillMaxSize().nestedScroll(scrollBehavior.nestedScrollConnection),
            // Match HomeScreen: use surface (pure black in AMOLED dark)
            containerColor = MaterialTheme.colorScheme.surface,
            topBar = {
                LargeTopAppBar(
                    title = {
                        val collapsedFraction = scrollBehavior.state.collapsedFraction
                        val expandedFontSize = MaterialTheme.typography.displaySmallEmphasized.fontSize
                        val collapsedFontSize = MaterialTheme.typography.headlineSmall.fontSize

                        val fontSize = lerp(expandedFontSize, collapsedFontSize, collapsedFraction)

                        Text(
                            modifier = Modifier.basicMarquee(),
                            text = if (subjectId == null) stringResource(R.string.add_subject) else "Update Subject",
                            maxLines = 1,
                            fontSize = fontSize,
                            style = MaterialTheme.typography.displaySmallEmphasized.copy(
                                fontWeight = FontWeight.ExtraBold
                            )
                        )
                    },
                    navigationIcon = {
                        Box(modifier = Modifier.padding(start = 5.dp, end = 6.dp)) {
                            BackButton(
                                onClick = {
                                    viewModel.resetInputFields()
                                    onDismiss()
                                }
                            )
                        }
                    },
                    actions = {
                        // Save/Update button in top bar as a filled tonal button
                        FilledTonalButton(
                            onClick = {
                                weakHaptic()
                                viewModel.viewModelScope.launch {
                                    val subjectName = subject.trim()
                                    if (subjectId == null) {
                                        viewModel.addSubject(
                                            onSuccess = {
                                                viewModel.viewModelScope.launch {
                                                    kotlinx.coroutines.delay(100)
                                                    val subjects = viewModel.subjectList.first()
                                                    val newSubject = subjects.find { it.subject == subjectName }
                                                    newSubject?.let { subj ->
                                                        if (timetableSchedules.isNotEmpty()) {
                                                            viewModel.saveSchedulesForSubject(subj.id, timetableSchedules)
                                                        }
                                                    }
                                                    viewModel.resetInputFields()
                                                    timetableSchedules = emptyList()
                                                    onDismiss()
                                                }
                                            }
                                        )
                                    } else {
                                        viewModel.updateSubject(
                                            subjectId = subjectId,
                                            onSuccess = {
                                                viewModel.viewModelScope.launch {
                                                    viewModel.saveSchedulesForSubject(subjectId, timetableSchedules)
                                                    viewModel.resetInputFields()
                                                    timetableSchedules = emptyList()
                                                    onDismiss()
                                                }
                                            }
                                        )
                                    }
                                }
                            },
                            shapes = ButtonDefaults.shapes(),
                            modifier = Modifier.padding(end = 12.dp),
                            elevation = ButtonDefaults.filledTonalButtonElevation(defaultElevation = 0.dp)
                        ) {
                            Text(if (subjectId == null) "Save" else "Update", fontWeight = FontWeight.Bold)
                        }
                    },
                    scrollBehavior = scrollBehavior,
                    colors = TopAppBarDefaults.largeTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            }
        ) { paddingValues ->
            var showScheduleDialog by remember { mutableStateOf(false) }
            var schedulesToDelete by remember { mutableStateOf<List<ClassSchedule>?>(null) }

            Box(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .verticalScroll(scrollState)
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {

                    // ── Subject Info Group ──────────────────────────────────────
                    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        val label = when (subjectError) {
                            SubjectError.Empty -> stringResource(R.string.field_blank_error)
                            SubjectError.AlreadyExists -> stringResource(R.string.subject_already_exists)
                            is SubjectError.Unknown -> stringResource(R.string.unknown_error)
                            else -> stringResource(R.string.enter_subject_name)
                        }

                        // Subject Name
                        Surface(
                            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomStart = 5.dp, bottomEnd = 5.dp),
                            color = MaterialTheme.colorScheme.surfaceContainer,
                            modifier = Modifier.fillMaxWidth().height(80.dp)
                        ) {
                            TextField(
                                value = subject,
                                onValueChange = { viewModel.onSubjectChange(it) },
                                isError = subjectError != SubjectError.None,
                                modifier = Modifier.fillMaxWidth(),
                                label = { 
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.labelLarge,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                    ) 
                                },
                                textStyle = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.SemiBold
                                ),
                                singleLine = true,
                                colors = TextFieldDefaults.colors(
                                    unfocusedIndicatorColor = Color.Transparent,
                                    focusedIndicatorColor = Color.Transparent,
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent
                                ),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }

                        // Subject Code
                        Surface(
                            shape = RoundedCornerShape(5.dp),
                            color = MaterialTheme.colorScheme.surfaceContainer,
                            modifier = Modifier.fillMaxWidth().height(80.dp)
                        ) {
                            TextField(
                                value = subjectCode,
                                onValueChange = { viewModel.onSubjectCodeChange(it) },
                                modifier = Modifier.fillMaxWidth(),
                                label = { 
                                    Text(
                                        text = stringResource(R.string.enter_subject_code_optional),
                                        style = MaterialTheme.typography.labelLarge,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                    ) 
                                },
                                textStyle = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.SemiBold
                                ),
                                singleLine = true,
                                colors = TextFieldDefaults.colors(
                                    unfocusedIndicatorColor = Color.Transparent,
                                    focusedIndicatorColor = Color.Transparent,
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent
                                ),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }

                        // Chart Label
                        Surface(
                            shape = RoundedCornerShape(topStart = 5.dp, topEnd = 5.dp, bottomStart = 20.dp, bottomEnd = 20.dp),
                            color = MaterialTheme.colorScheme.surfaceContainer,
                            modifier = Modifier.fillMaxWidth().height(80.dp)
                        ) {
                            TextField(
                                value = histogramLabel,
                                onValueChange = { viewModel.onHistogramLabelChange(it) },
                                modifier = Modifier.fillMaxWidth(),
                                label = { 
                                    Text(
                                        text = "Chart label (max 8 chars)",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                    ) 
                                },
                                textStyle = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.SemiBold
                                ),
                                singleLine = true,
                                colors = TextFieldDefaults.colors(
                                    unfocusedIndicatorColor = Color.Transparent,
                                    focusedIndicatorColor = Color.Transparent,
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent
                                ),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    }

                    // ── Attendance Seed Group ───────────────────────────────────
                    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Surface(
                            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomStart = 5.dp, bottomEnd = 5.dp),
                            color = MaterialTheme.colorScheme.surfaceContainer,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Initial Data (Optional)",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "Seed your existing attendance data below.",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(topStart = 5.dp, topEnd = 5.dp, bottomStart = 20.dp, bottomEnd = 20.dp),
                            color = MaterialTheme.colorScheme.surfaceContainer,
                            modifier = Modifier.fillMaxWidth().height(80.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TextField(
                                    value = attendedCount,
                                    onValueChange = { viewModel.onAttendedCountChange(it) },
                                    modifier = Modifier.weight(1f),
                                    label = { 
                                        Text(
                                            text = "Attended",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                        ) 
                                    },
                                    textStyle = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.SemiBold
                                    ),
                                    placeholder = { Text("0") },
                                    singleLine = true,
                                    leadingIcon = {
                                        Text(
                                            "✓",
                                            color = MaterialTheme.colorScheme.primary,
                                            style = MaterialTheme.typography.titleMedium,
                                            modifier = Modifier.padding(start = 4.dp)
                                        )
                                    },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    colors = TextFieldDefaults.colors(
                                        unfocusedIndicatorColor = Color.Transparent,
                                        focusedIndicatorColor = Color.Transparent,
                                        focusedContainerColor = Color.Transparent,
                                        unfocusedContainerColor = Color.Transparent
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                )

                                TextField(
                                    value = missedCount,
                                    onValueChange = { viewModel.onMissedCountChange(it) },
                                    modifier = Modifier.weight(1f),
                                    label = { 
                                        Text(
                                            text = "Missed",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                        ) 
                                    },
                                    textStyle = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.SemiBold
                                    ),
                                    placeholder = { Text("0") },
                                    singleLine = true,
                                    leadingIcon = {
                                        Text(
                                            "✗",
                                            color = MaterialTheme.colorScheme.error,
                                            style = MaterialTheme.typography.titleMedium,
                                            modifier = Modifier.padding(start = 4.dp)
                                        )
                                    },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    colors = TextFieldDefaults.colors(
                                        unfocusedIndicatorColor = Color.Transparent,
                                        focusedIndicatorColor = Color.Transparent,
                                        focusedContainerColor = Color.Transparent,
                                        unfocusedContainerColor = Color.Transparent
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                )
                            }
                        }
                    }

                    // ── Schedule Section Header ─────────────────────────────────
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Weekly Schedule",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                        if (timetableSchedules.isNotEmpty()) {
                            Text(
                                text = "Optional",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // ── Schedule Cards ──────────────────────────────────────────
                    if (timetableSchedules.isNotEmpty()) {
                        val groupedList = timetableSchedules.groupBy { it.startTime to it.endTime }.toList()
                        val totalSize = groupedList.size

                        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            groupedList.forEachIndexed { index, (timePair, schedules) ->
                                val (start, end) = timePair
                                val sortedDays = schedules.map { it.dayOfWeek }.sorted()
                                val daysStr = sortedDays.joinToString(" · ") { day ->
                                    when (day) {
                                        1 -> "Mon"; 2 -> "Tue"; 3 -> "Wed"; 4 -> "Thu"
                                        5 -> "Fri"; 6 -> "Sat"; 7 -> "Sun"; else -> ""
                                    }
                                }

                                val shape = when {
                                    totalSize == 1 -> RoundedCornerShape(16.dp)
                                    index == 0 -> RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 4.dp, bottomEnd = 4.dp)
                                    index == totalSize - 1 -> RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp, bottomStart = 16.dp, bottomEnd = 16.dp)
                                    else -> RoundedCornerShape(4.dp)
                                }

                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = shape,
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                                    ),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp, vertical = 14.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = daysStr,
                                                style = MaterialTheme.typography.labelMedium,
                                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                                                fontWeight = FontWeight.Medium
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = "${TimeUtils.format24To12Hour(start)} – ${TimeUtils.format24To12Hour(end)}",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                        Row(horizontalArrangement = Arrangement.spacedBy(0.dp)) {
                                            var isEditing by remember { mutableStateOf(false) }
                                            IconButton(onClick = { isEditing = true }) {
                                                Icon(
                                                    Icons.Default.Edit,
                                                    "Edit",
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                            IconButton(onClick = {
                                                schedulesToDelete = schedules
                                            }) {
                                                Icon(
                                                    Icons.Default.Delete,
                                                    "Delete",
                                                    tint = MaterialTheme.colorScheme.error,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                            if (isEditing) {
                                                InlineScheduleEditor(
                                                    initialDays = sortedDays.toSet(),
                                                    initialStart = start,
                                                    initialEnd = end,
                                                    onSave = { newDays, newStart, newEnd ->
                                                        timetableSchedules = timetableSchedules.filterNot { it in schedules } + newDays.map { day ->
                                                            ClassSchedule(subjectId = 0, dayOfWeek = day, startTime = newStart, endTime = newEnd)
                                                        }
                                                        isEditing = false
                                                    },
                                                    onCancel = { isEditing = false }
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (showScheduleDialog) {
                        CustomTimeDialog(
                            onDismiss = { showScheduleDialog = false },
                            onConfirm = { days, start, end ->
                                val newSchedules = days.map { day ->
                                    ClassSchedule(subjectId = 0, dayOfWeek = day, startTime = start, endTime = end)
                                }
                                timetableSchedules = timetableSchedules + newSchedules
                                showScheduleDialog = false
                            }
                        )
                    }

                    if (schedulesToDelete != null) {
                        ConfirmDeleteDialog(
                            title = "Delete Schedule",
                            message = "Are you sure you want to delete this schedule? This will remove these classes from your weekly timetable.",
                            onDismiss = { schedulesToDelete = null },
                            onConfirm = {
                                schedulesToDelete?.let { toDelete ->
                                    timetableSchedules = timetableSchedules.filterNot { it in toDelete }
                                }
                                schedulesToDelete = null
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(120.dp))
                }

                // ── Add Schedule FAB pinned at bottom ──────────────────────────
                if (!showScheduleDialog) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 15.dp, start = 35.dp, end = 35.dp)
                            .navigationBarsPadding()
                    ) {
                        Button(
                            onClick = { showScheduleDialog = true; weakHaptic() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(64.dp),
                            shapes = ButtonDefaults.shapes(),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        ) {
                            Icon(Icons.Default.Add, null, modifier = Modifier.size(24.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "Add Schedule",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * A surface-like card grouping for input fields — no elevation, soft container background.
 */
@Composable
private fun SectionCard(
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            content = content
        )
    }
}

@Composable
fun CustomTimeDialog(
    onDismiss: () -> Unit,
    onConfirm: (Set<Int>, String, String) -> Unit
) {
    val selectedDays = remember { mutableStateListOf<Int>() }

    val startState = rememberTimePickerState(initialHour = 8, initialMinute = 0, is24Hour = false)
    val endState = rememberTimePickerState(initialHour = 9, initialMinute = 0, is24Hour = false)

    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    Dialog(
        onDismissRequest = { },
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
    ) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            tonalElevation = 0.dp,
            shadowElevation = 0.dp,
            color = MaterialTheme.colorScheme.surfaceContainer,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Add Schedule",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                // Day selector — single selection per operation
                val topRowDays = listOf("Mon" to 1, "Tue" to 2, "Wed" to 3, "Thu" to 4)
                val bottomRowDays = listOf("Fri" to 5, "Sat" to 6, "Sun" to 7)

                val baseWeight = 1f
                val selectedBonus = 0.35f

                @Composable
                fun rowWeight(dayIdx: Int, rowSize: Int): Float {
                    val bonus = selectedBonus / (rowSize - 1)
                    val w = when {
                        selectedDays.contains(dayIdx) -> baseWeight + selectedBonus
                        selectedDays.isNotEmpty() -> baseWeight - bonus
                        else -> baseWeight
                    }
                    val animatedW by animateFloatAsState(
                        targetValue = w,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioLowBouncy,
                            stiffness = Spring.StiffnessLow
                        ),
                        label = "weight_$dayIdx"
                    )
                    return animatedW
                }

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    // Row 1: Mon Tue Wed Thu
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        topRowDays.forEach { (label, dayIdx) ->
                            AnimatedDayChip(
                                day = label,
                                isSelected = selectedDays.contains(dayIdx),
                                modifier = Modifier.weight(rowWeight(dayIdx, topRowDays.size)),
                                onToggle = {
                                    if (selectedDays.contains(dayIdx)) selectedDays.remove(dayIdx)
                                    else { selectedDays.clear(); selectedDays.add(dayIdx) }
                                }
                            )
                        }
                    }
                    // Row 2: Fri Sat Sun
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        bottomRowDays.forEach { (label, dayIdx) ->
                            AnimatedDayChip(
                                day = label,
                                isSelected = selectedDays.contains(dayIdx),
                                modifier = Modifier.weight(rowWeight(dayIdx, bottomRowDays.size)),
                                onToggle = {
                                    if (selectedDays.contains(dayIdx)) selectedDays.remove(dayIdx)
                                    else { selectedDays.clear(); selectedDays.add(dayIdx) }
                                }
                            )
                        }
                    }
                }

                // Time pickers
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = "Start time",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            MaterialTheme(
                                typography = MaterialTheme.typography.copy(
                                    labelSmall = MaterialTheme.typography.labelSmall.copy(fontSize = 0.sp)
                                )
                            ) {
                                TimeInput(
                                    state = startState,
                                    modifier = Modifier.focusRequester(focusRequester)
                                )
                            }
                        }
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = "End time",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            MaterialTheme(
                                typography = MaterialTheme.typography.copy(
                                    labelSmall = MaterialTheme.typography.labelSmall.copy(fontSize = 0.sp)
                                )
                            ) {
                                TimeInput(state = endState)
                            }
                        }
                    }
                }

                // Action buttons
                val dialogBtnSources = remember { List(2) { MutableInteractionSource() } }
                @Suppress("DEPRECATION")
                ButtonGroup(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shapes = ButtonDefaults.shapes(),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .animateWidth(dialogBtnSources[0]),
                        interactionSource = dialogBtnSources[0],
                    ) {
                        Text("Cancel", fontWeight = FontWeight.SemiBold)
                    }
                    Button(
                        onClick = {
                            val startStr = String.format("%02d:%02d", startState.hour, startState.minute)
                            val endStr = String.format("%02d:%02d", endState.hour, endState.minute)
                            onConfirm(selectedDays.toSet(), startStr, endStr)
                        },
                        enabled = selectedDays.isNotEmpty(),
                        shapes = ButtonDefaults.shapes(),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .animateWidth(dialogBtnSources[1]),
                        interactionSource = dialogBtnSources[1],
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                    ) {
                        Text("Add", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun AnimatedDayChip(
    day: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onToggle: () -> Unit
) {
    // 40dp corner radius = fully rounded pill when selected; rectangular (8dp) when not
    val cornerRadius by animateDpAsState(
        targetValue = if (isSelected) 40.dp else 8.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "chip_corner_$day"
    )

    val bgColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primaryContainer
        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMedium),
        label = "chip_bg_$day"
    )

    val textColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
        else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMedium),
        label = "chip_text_$day"
    )

    Box(
        modifier = modifier
            .height(44.dp)
            .clip(RoundedCornerShape(cornerRadius))
            .background(bgColor)
            .clickable { onToggle() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = day,
            color = textColor,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
            maxLines = 1
        )
    }
}

// Legacy DayChip kept for backward compat
@Composable
fun DayChip(
    day: String,
    isSelected: Boolean,
    onToggle: () -> Unit
) {
    AnimatedDayChip(day = day, isSelected = isSelected, onToggle = onToggle)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InlineScheduleEditor(
    initialDays: Set<Int> = emptySet(),
    initialStart: String = "08:00",
    initialEnd: String = "09:00",
    onSave: (Set<Int>, String, String) -> Unit,
    onCancel: () -> Unit
) {
    var isDialogVisible by remember { mutableStateOf(true) }
    if (isDialogVisible) {
        CustomTimeDialog(
            onDismiss = { onCancel(); isDialogVisible = false },
            onConfirm = { days, start, end ->
                onSave(days, start, end)
                isDialogVisible = false
            }
        )
    }
}

@Composable
fun TimePickerDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    content: @Composable () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
        confirmButton = { TextButton(onClick = onConfirm) { Text("OK") } },
        text = { content() }
    )
}
