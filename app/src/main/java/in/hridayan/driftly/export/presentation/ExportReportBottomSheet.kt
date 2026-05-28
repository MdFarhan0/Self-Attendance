@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)

package `in`.hridayan.driftly.export.presentation

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import `in`.hridayan.driftly.core.common.LocalWeakHaptic
import `in`.hridayan.driftly.export.pdf.PdfUtils
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.runtime.saveable.rememberSaveable
import `in`.hridayan.driftly.core.presentation.theme.adaptiveModalScrimColor

@Composable
fun ExportReportBottomSheet(
    onDismiss: () -> Unit,
    viewModel: ExportViewModel = hiltViewModel()
) {
    val context      = LocalContext.current
    val weakHaptic   = LocalWeakHaptic.current
    val subjects     by viewModel.subjects.collectAsState()
    val selectedIds  by viewModel.selectedSubjectIds.collectAsState()
    val studentName  by viewModel.studentName.collectAsState()
    val isGenerating by viewModel.isGenerating.collectAsState()

    val canGenerate = selectedIds.isNotEmpty() && !isGenerating

    // Segmented selector state
    var exportAllSubjects by rememberSaveable { mutableStateOf(true) }
    var showSubjectDialog by remember { mutableStateOf(false) }

    // Automatically select all subjects when "All Subjects" mode is active
    LaunchedEffect(exportAllSubjects) {
        if (exportAllSubjects) {
            viewModel.selectAll()
        }
    }

    // Segmented selector animation weights/scales/alphas
    val allWeight by animateFloatAsState(targetValue = if (exportAllSubjects) 1.12f else 0.88f, label = "allWeight")
    val specificWeight by animateFloatAsState(targetValue = if (exportAllSubjects) 0.88f else 1.12f, label = "specificWeight")
    
    val allScale by animateFloatAsState(targetValue = if (exportAllSubjects) 1.02f else 0.98f, label = "allScale")
    val specificScale by animateFloatAsState(targetValue = if (exportAllSubjects) 0.98f else 1.02f, label = "specificScale")

    val allAlpha by animateFloatAsState(targetValue = if (exportAllSubjects) 1f else 0.6f, label = "allAlpha")
    val specificAlpha by animateFloatAsState(targetValue = if (exportAllSubjects) 1f else 0.6f, label = "specificAlpha")

    // Interaction sources for the animated buttons
    val cancelInteraction   = remember { MutableInteractionSource() }
    val generateInteraction = remember { MutableInteractionSource() }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        scrimColor = adaptiveModalScrimColor(),
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
                .padding(top = 24.dp, bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // ── Header Redesign ────────────────────────────────────────────────
            Text(
                text = "Export Report as PDF",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp)
            )

            // ── Segmented Selector ────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(RoundedCornerShape(30.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f), RoundedCornerShape(30.dp))
                    .padding(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Option 1: All Subjects
                Box(
                    modifier = Modifier
                        .weight(allWeight)
                        .fillMaxHeight()
                        .scale(allScale)
                        .clip(RoundedCornerShape(30.dp))
                        .background(
                            if (exportAllSubjects) MaterialTheme.colorScheme.primary
                            else Color.Transparent
                        )
                        .clickable {
                            if (!exportAllSubjects) {
                                weakHaptic()
                                exportAllSubjects = true
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "All Subjects",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = if (exportAllSubjects) FontWeight.Bold else FontWeight.Medium,
                        color = if (exportAllSubjects) MaterialTheme.colorScheme.onPrimary
                                else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.alpha(allAlpha)
                    )
                }

                // Option 2: Specific Subjects
                Box(
                    modifier = Modifier
                        .weight(specificWeight)
                        .fillMaxHeight()
                        .scale(specificScale)
                        .clip(RoundedCornerShape(30.dp))
                        .background(
                            if (!exportAllSubjects) MaterialTheme.colorScheme.primary
                            else Color.Transparent
                        )
                        .clickable {
                            if (exportAllSubjects) {
                                weakHaptic()
                                exportAllSubjects = false
                                showSubjectDialog = true
                            } else {
                                weakHaptic()
                                showSubjectDialog = true
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Specific Subjects",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = if (!exportAllSubjects) FontWeight.Bold else FontWeight.Medium,
                        color = if (!exportAllSubjects) MaterialTheme.colorScheme.onPrimary
                                else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.alpha(specificAlpha)
                    )
                }
            }

            // Small label indicating selected count under segmented selector
            androidx.compose.animation.AnimatedVisibility(
                visible = !exportAllSubjects,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Text(
                    text = "${selectedIds.size} of ${subjects.size} subjects selected (tap to edit)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .clickable { showSubjectDialog = true }
                )
            }

            Spacer(Modifier.height(28.dp))

            // ── Animated action buttons ──
            @Suppress("DEPRECATION")
            ButtonGroup(modifier = Modifier.fillMaxWidth()) {
                // Cancel
                OutlinedButton(
                    onClick = {
                        weakHaptic()
                        onDismiss()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .animateWidth(cancelInteraction),
                    interactionSource = cancelInteraction,
                    shapes = ButtonDefaults.shapes(),
                ) {
                    Text(
                        text = "Cancel",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 11.dp)
                    )
                }

                // Generate PDF
                Button(
                    onClick = {
                        if (canGenerate) {
                            weakHaptic()
                            viewModel.generatePdf(context) { file ->
                                if (file != null) {
                                    PdfUtils.sharePdf(context, file)
                                    onDismiss()
                                } else {
                                    Toast.makeText(
                                        context,
                                        "Failed to generate PDF",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        }
                    },
                    enabled = canGenerate,
                    modifier = Modifier
                        .weight(1f)
                        .animateWidth(generateInteraction),
                    interactionSource = generateInteraction,
                    shapes = ButtonDefaults.shapes(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Text(
                        text = if (isGenerating) "Generating…" else "Generate",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 11.dp)
                    )
                }
            }
        }
    }

    if (showSubjectDialog) {
        SubjectSelectionDialog(
            subjects = subjects,
            initialSelectedIds = selectedIds,
            onDismiss = { showSubjectDialog = false },
            onConfirm = { updatedIds ->
                viewModel.setSelectedSubjectIds(updatedIds)
                showSubjectDialog = false
            }
        )
    }
}

@Composable
private fun SubjectSelectionDialog(
    subjects: List<`in`.hridayan.driftly.core.data.model.SubjectEntity>,
    initialSelectedIds: Set<Int>,
    onDismiss: () -> Unit,
    onConfirm: (Set<Int>) -> Unit
) {
    val weakHaptic = LocalWeakHaptic.current
    var tempSelectedIds by remember { mutableStateOf(initialSelectedIds) }
    val interactionSources = remember { List(2) { MutableInteractionSource() } }

    Dialog(
        onDismissRequest = { onDismiss() },
        properties = DialogProperties(dismissOnClickOutside = true)
    ) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            tonalElevation = 8.dp,
            color = MaterialTheme.colorScheme.surfaceContainer
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .widthIn(min = 280.dp)
            ) {
                Text(
                    text = "Select Subjects",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Scrollable subjects list
                if (subjects.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No subjects found",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f, fill = false)
                            .heightIn(max = 280.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(subjects, key = { it.id }) { subject ->
                            val isSelected = tempSelectedIds.contains(subject.id)
                            val bgColor by animateColorAsState(
                                targetValue = if (isSelected)
                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                                else Color.Transparent,
                                animationSpec = spring(stiffness = Spring.StiffnessMedium),
                                label = "subjectBg"
                            )

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(bgColor)
                                    .clickable {
                                        weakHaptic()
                                        tempSelectedIds = if (isSelected) {
                                            tempSelectedIds - subject.id
                                        } else {
                                            tempSelectedIds + subject.id
                                        }
                                    }
                                    .padding(horizontal = 14.dp, vertical = 11.dp)
                            ) {
                                // Custom animated checkbox
                                Box(
                                    modifier = Modifier
                                        .size(22.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(
                                            if (isSelected) MaterialTheme.colorScheme.primary
                                            else Color.Transparent
                                        )
                                        .border(
                                            width = if (isSelected) 0.dp else 1.5.dp,
                                            color = if (isSelected) Color.Transparent
                                                    else MaterialTheme.colorScheme.outline,
                                            shape = RoundedCornerShape(6.dp)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    androidx.compose.animation.AnimatedVisibility(
                                        visible = isSelected,
                                        enter = fadeIn() + scaleIn(
                                            initialScale = 0.4f,
                                            animationSpec = spring(stiffness = Spring.StiffnessHigh)
                                        ),
                                        exit = fadeOut()
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.Check,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onPrimary,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }

                                Spacer(Modifier.width(12.dp))

                                Text(
                                    text = subject.subject,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                    color = if (isSelected) MaterialTheme.colorScheme.onSurface
                                            else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                @Suppress("DEPRECATION")
                ButtonGroup(modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = {
                            onDismiss()
                            weakHaptic()
                        },
                        shapes = ButtonDefaults.shapes(),
                        modifier = Modifier
                            .weight(1f)
                            .animateWidth(interactionSources[0]),
                        interactionSource = interactionSources[0],
                    ) {
                        Text(
                            text = "Cancel",
                            style = MaterialTheme.typography.labelLarge
                        )
                    }

                    Button(
                        onClick = {
                            onConfirm(tempSelectedIds)
                            weakHaptic()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        shapes = ButtonDefaults.shapes(),
                        modifier = Modifier
                            .weight(1f)
                            .animateWidth(interactionSources[1]),
                        interactionSource = interactionSources[1],
                    ) {
                        Text(
                            text = "Apply",
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }
        }
    }
}
