@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package `in`.hridayan.driftly.home.presentation.components.dialog

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.viewModelScope
import `in`.hridayan.driftly.R
import `in`.hridayan.driftly.core.common.LocalWeakHaptic
import `in`.hridayan.driftly.core.domain.model.ClassSchedule
import `in`.hridayan.driftly.core.domain.model.SubjectError
import `in`.hridayan.driftly.core.presentation.components.text.AutoResizeableText
import `in`.hridayan.driftly.core.presentation.theme.Shape
import `in`.hridayan.driftly.home.presentation.viewmodel.HomeViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@Composable
fun EditSubjectDialog(
    modifier: Modifier = Modifier,
    subjectId: Int,
    subject: String,
    subjectCode: String? = null,
    viewModel: HomeViewModel = hiltViewModel(),
    onDismiss: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var showTimetableDialog by remember { mutableStateOf(false) }
    var timetableSchedules by remember { mutableStateOf<List<ClassSchedule>>(emptyList()) }
    
    // Load existing timetable
    LaunchedEffect(subjectId) {
        viewModel.setSubjectNamePlaceholder(subject)
        subjectCode?.let { viewModel.onSubjectCodeChange(it) }
        
        // Load existing histogram label
        val subjectEntity = viewModel.getSubjectById(subjectId).first()
        subjectEntity?.histogramLabel?.let { viewModel.onHistogramLabelChange(it) }
        
        // Load existing schedules
        timetableSchedules = viewModel.getSchedulesForSubject(subjectId).first()
    }

    val weakHaptic = LocalWeakHaptic.current
    val currentSubject by viewModel.subject.collectAsState()
    val currentSubjectCode by viewModel.subjectCode.collectAsState()
    val histogramLabel by viewModel.histogramLabel.collectAsState()
    val subjectError by viewModel.subjectError.collectAsState()

    Dialog(
        onDismissRequest = {
            viewModel.resetInputFields()
            onDismiss()
        }
    ) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .clip(Shape.cardCornerLarge)
                .background(MaterialTheme.colorScheme.surfaceContainer)
                .padding(25.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val label = when (subjectError) {
                SubjectError.Empty -> stringResource(R.string.field_blank_error)
                SubjectError.AlreadyExists -> stringResource(R.string.subject_already_exists)
                is SubjectError.Unknown -> stringResource(R.string.unknown_error)
                else -> stringResource(R.string.enter_new_name)
            }

            AutoResizeableText(
                text = stringResource(R.string.update_subject),
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleLarge
            )

            OutlinedTextField(
                value = currentSubject,
                onValueChange = { viewModel.onSubjectChange(it) },
                isError = subjectError != SubjectError.None,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(text = label) }
            )

            OutlinedTextField(
                value = currentSubjectCode,
                onValueChange = { viewModel.onSubjectCodeChange(it) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(text = stringResource(R.string.enter_subject_code_optional)) }
            )

            OutlinedTextField(
                value = histogramLabel,
                onValueChange = { viewModel.onHistogramLabelChange(it) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Short name for chart (max 5 chars)") },
                supportingText = { Text("${histogramLabel.length}/5") }
            )

            // Timetable Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Timetable",
                            style = MaterialTheme.typography.labelLarge
                        )
                        if (timetableSchedules.isNotEmpty()) {
                            Text(
                                text = "${timetableSchedules.size} classes",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    
                    val timetableButtonInteraction = remember { MutableInteractionSource() }
                    FilledTonalButton(
                        onClick = { showTimetableDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),  // Make it thicker
                        ) {
                        Icon(
                            Icons.Default.Schedule,
                            contentDescription = null
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(if (timetableSchedules.isEmpty()) "Add Timetable" else "Edit Timetable")
                    }
                }
            }

            // Buttons with Animation
            val buttonInteractionSources = remember { List(2) { MutableInteractionSource() } }
            
            @Suppress("DEPRECATION")
            ButtonGroup(modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(
                    onClick = {
                        weakHaptic()
                        viewModel.resetInputFields()
                        onDismiss()
                    },
                    modifier = Modifier
                        .weight(1f),
                    ) {
                    AutoResizeableText(text = stringResource(R.string.cancel))
                }

                Button(
                    onClick = {
                        weakHaptic()
                        viewModel.updateSubject(
                            subjectId = subjectId,
                            onSuccess = {
                                // Save timetable changes
                                coroutineScope.launch {
                                    viewModel.saveSchedulesForSubject(subjectId, timetableSchedules)
                                }
                                viewModel.resetInputFields()
                                onDismiss()
                            }
                        )
                    },
                    modifier = Modifier
                        .weight(1f),
                    ) {
                    AutoResizeableText(text = stringResource(R.string.update))
                }
            }
        }
    }

    if (showTimetableDialog) {
        TimetableEntryDialog(
            subjectId = subjectId,
            initialSchedules = timetableSchedules,
            onDismiss = { showTimetableDialog = false },
            onSave = {
                timetableSchedules = it
                showTimetableDialog = false
            }
        )
    }
}
