@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)

package `in`.hridayan.driftly.calender.presentation.components.bottomsheet

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ButtonGroup
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import kotlinx.coroutines.launch
import `in`.hridayan.driftly.calender.presentation.viewmodel.CalendarViewModel
import `in`.hridayan.driftly.core.common.LocalWeakHaptic
import `in`.hridayan.driftly.core.data.model.AttendanceEntity
import `in`.hridayan.driftly.core.domain.model.AttendanceStatus
import `in`.hridayan.driftly.core.presentation.components.dialog.ConfirmDeleteDialog
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun DailyAttendanceBottomSheet(
    subjectId: Int,
    dateString: String,
    onDismiss: () -> Unit,
    viewModel: CalendarViewModel = hiltViewModel()
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    val weakHaptic = LocalWeakHaptic.current
    val scrollState = rememberScrollState()

    val entries by viewModel.getEntriesForDate(subjectId, dateString)
        .collectAsState(initial = emptyList())

    val date = runCatching { LocalDate.parse(dateString) }.getOrNull()
    val dayName = date?.dayOfWeek?.getDisplayName(TextStyle.FULL, Locale.getDefault()) ?: ""
    val formattedDate = date?.format(
        DateTimeFormatter.ofPattern("MMM d", Locale.getDefault())
    ) ?: dateString

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
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp, top = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header: "Friday, Mar 27"
            Text(
                text = "$dayName, $formattedDate",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 20.dp)
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState)
                    .weight(1f, fill = false),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                val totalEntries = entries.size
                entries.forEachIndexed { index, entry ->
                    val shape = when {
                        totalEntries == 1 -> RoundedCornerShape(10.dp)
                        index == 0 -> RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp, bottomStart = 2.dp, bottomEnd = 2.dp)
                        index == totalEntries - 1 -> RoundedCornerShape(topStart = 2.dp, topEnd = 2.dp, bottomStart = 10.dp, bottomEnd = 10.dp)
                        else -> RoundedCornerShape(2.dp)
                    }

                    var showDeleteDialog by remember { mutableStateOf(false) }

                    AttendanceEntryCard(
                        classNumber = index + 1,
                        entry = entry,
                        shape = shape,
                        onDelete = {
                            showDeleteDialog = true
                        }
                    )

                    if (showDeleteDialog) {
                        ConfirmDeleteDialog(
                            title = "Delete Class Entry",
                            message = "Are you sure you want to delete this class entry? This action is irreversible.",
                            onDismiss = { showDeleteDialog = false },
                            onConfirm = {
                                viewModel.deleteClassEntry(entry.id)
                                showDeleteDialog = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(25.dp))

            // Bottom action buttons: Absent | Present
            val actionInteractionSources = remember { List(2) { MutableInteractionSource() } }

            @Suppress("DEPRECATION")
            ButtonGroup(modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = {
                        weakHaptic()
                        viewModel.addClassEntry(subjectId, dateString, AttendanceStatus.ABSENT)
                        scope.launch {
                            sheetState.hide()
                            onDismiss()
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .animateWidth(actionInteractionSources[0]),
                    interactionSource = actionInteractionSources[0],
                    shapes = ButtonDefaults.shapes(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    )
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Absent",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 11.dp)
                    )
                }

                Button(
                    onClick = {
                        weakHaptic()
                        viewModel.addClassEntry(subjectId, dateString, AttendanceStatus.PRESENT)
                        scope.launch {
                            sheetState.hide()
                            onDismiss()
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .animateWidth(actionInteractionSources[1]),
                    interactionSource = actionInteractionSources[1],
                    shapes = ButtonDefaults.shapes(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Check,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Present",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 11.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun AttendanceEntryCard(
    classNumber: Int,
    entry: AttendanceEntity,
    shape: androidx.compose.ui.graphics.Shape,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isPresent = entry.status == AttendanceStatus.PRESENT

    val containerColor = if (isPresent)
        MaterialTheme.colorScheme.primaryContainer
    else
        MaterialTheme.colorScheme.errorContainer

    val contentColor = if (isPresent)
        MaterialTheme.colorScheme.onPrimaryContainer
    else
        MaterialTheme.colorScheme.onErrorContainer

    val trashColor = if (isPresent)
        MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
    else
        MaterialTheme.colorScheme.error

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = shape,
        color = containerColor,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 21.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Status icon + label
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = if (isPresent) Icons.Rounded.Check else Icons.Rounded.Close,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = if (isPresent) "Attended Class $classNumber" else "Bunked Class $classNumber",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = contentColor
                )
            }

            // Delete icon
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Rounded.Delete,
                    contentDescription = "Delete",
                    tint = trashColor,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
