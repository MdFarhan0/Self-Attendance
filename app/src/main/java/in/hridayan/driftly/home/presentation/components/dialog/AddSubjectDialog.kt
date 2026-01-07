@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package `in`.hridayan.driftly.home.presentation.components.dialog

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonGroup
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
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
fun AddSubjectDialog(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel(),
    onDismiss: () -> Unit
) {
    val subject by viewModel.subject.collectAsState()
    val subjectCode by viewModel.subjectCode.collectAsState()
    val histogramLabel by viewModel.histogramLabel.collectAsState()
    val subjectError by viewModel.subjectError.collectAsState()
    val weakHaptic = LocalWeakHaptic.current

    var showTimetableDialog by remember { mutableStateOf(false) }
    var timetableSchedules by remember { mutableStateOf<List<ClassSchedule>>(emptyList()) }

    val interactionSources = remember { List(2) { MutableInteractionSource() } }

    Dialog(
        onDismissRequest = {
            viewModel.resetInputFields()
            onDismiss()
        },
        content = {
            Column(
                modifier = modifier
                    .fillMaxWidth()
                    .clip(Shape.cardCornerLarge)
                    .background(MaterialTheme.colorScheme.surfaceContainer)
                    .padding(25.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                val label = when (subjectError) {
                    SubjectError.Empty -> stringResource(R.string.field_blank_error)
                    SubjectError.AlreadyExists -> stringResource(R.string.subject_already_exists)
                    is SubjectError.Unknown -> stringResource(R.string.unknown_error)
                    else -> stringResource(R.string.enter_subject_name)
                }

                AutoResizeableText(
                    text = stringResource(R.string.add_subject),
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleLarge,
                )

                OutlinedTextField(
                    value = subject,
                    onValueChange = {
                        viewModel.onSubjectChange(it)
                    },
                    isError = subjectError != SubjectError.None,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(text = label) },
                )

                OutlinedTextField(
                    value = subjectCode,
                    onValueChange = {
                        viewModel.onSubjectCodeChange(it)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(text = stringResource( R.string.enter_subject_code_optional)) },
                )

                OutlinedTextField(
                    value = histogramLabel,
                    onValueChange = {
                        viewModel.onHistogramLabelChange(it)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(text = "Short name for chart (max 5 chars)") },
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
                                text = "Timetable (Optional)",
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
                        
                        Text(
                            text = "Add weekly class schedule for notifications",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        val timetableButtonInteraction = remember { MutableInteractionSource() }
                        FilledTonalButton(
                            onClick = { showTimetableDialog = true },
                            modifier = Modifier
                                .fillMaxWidth(),
                            ) {
                            Icon(
                                painter = painterResource(id = android.R.drawable.ic_menu_my_calendar),
                                contentDescription = null
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(if (timetableSchedules.isEmpty()) "Add Timetable" else "Edit Timetable")
                        }
                    }
                }

                @Suppress("DEPRECATION")
                ButtonGroup(modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        modifier = Modifier.weight(1f),
                        onClick = {
                            weakHaptic()
                            viewModel.resetInputFields()
                            onDismiss()
                        },
                        content = { AutoResizeableText(text = stringResource(R.string.cancel)) }
                    )

                    Button(
                        modifier = Modifier.weight(1f),
                        onClick = {
                            weakHaptic()
                            viewModel.viewModelScope.launch {
                                // First add the subject
                                val subjectName = subject.trim()
                                viewModel.addSubject(
                                    onSuccess = {
                                        // Wait a bit for the subject to be fully added
                                        viewModel.viewModelScope.launch {
                                            kotlinx.coroutines.delay(100) // Small delay to ensure DB write completes
                                            val subjects = viewModel.subjectList.first()
                                            val newSubject = subjects.find { it.subject == subjectName }
                                            newSubject?.let { subj ->
                                                // Save timetable if any schedules were added
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
                            }
                        },
                        content = { AutoResizeableText(text = stringResource(R.string.add)) }
                    )
                }
            }
        }
    )

    if (showTimetableDialog) {
        TimetableEntryDialog(
            subjectId = 0, // Temporary, will be updated when subject is created
            initialSchedules = timetableSchedules,
            onDismiss = { showTimetableDialog = false },
            onSave = {
                timetableSchedules = it
                showTimetableDialog = false
            }
        )
    }
}
