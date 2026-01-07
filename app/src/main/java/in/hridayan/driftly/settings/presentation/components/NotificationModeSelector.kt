package `in`.hridayan.driftly.settings.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import `in`.hridayan.driftly.core.domain.model.NotificationMode
import `in`.hridayan.driftly.core.presentation.theme.Shape

@Composable
fun NotificationModeSelector(
    currentMode: String,
    gracePeriodMinutes: Int,
    onModeChange: (String) -> Unit,
    onGracePeriodChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var showStandardInfo by remember { mutableStateOf(false) }
    var showPersistentInfo by remember { mutableStateOf(false) }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Notification Mode",
            style = MaterialTheme.typography.titleMedium
        )

        // Standard Mode Card
        NotificationModeCard(
            title = "Standard Notifications",
            description = "Display only - No automatic actions",
            isSelected = currentMode == "STANDARD",
            onSelect = { onModeChange("STANDARD") },
            onInfoClick = { showStandardInfo = true }
        )

        // Persistent Mode Card
        NotificationModeCard(
            title = "Persistent Notifications",
            description = "Auto-mark attendance with grace period",
            isSelected = currentMode == "PERSISTENT",
            onSelect = { onModeChange("PERSISTENT") },
            onInfoClick = { showPersistentInfo = true }
        )

        // Grace Period Selector (only visible in Persistent mode)
        if (currentMode == "PERSISTENT") {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Grace Period",
                        style = MaterialTheme.typography.labelLarge
                    )
                    
                    Text(
                        text = "Time after class ends before auto-marking as missed",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )

                    GracePeriodSelector(
                        selectedMinutes = gracePeriodMinutes,
                        onPeriodChange = onGracePeriodChange
                    )
                }
            }
        }
    }

    if (showStandardInfo) {
        NotificationModeInfoDialog(
            mode = NotificationMode.STANDARD,
            onDismiss = { showStandardInfo = false }
        )
    }

    if (showPersistentInfo) {
        NotificationModeInfoDialog(
            mode = NotificationMode.PERSISTENT,
            onDismiss = { showPersistentInfo = false }
        )
    }
}

@Composable
private fun NotificationModeCard(
    title: String,
    description: String,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onInfoClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(
                selected = isSelected,
                onClick = onSelect,
                role = Role.RadioButton
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = isSelected,
                onClick = null
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(onClick = onInfoClick) {
                Icon(
                    Icons.Default.Info,
                    contentDescription = "More info",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun GracePeriodSelector(
    selectedMinutes: Int,
    onPeriodChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val periods = listOf(
        30 to "30 min",
        60 to "1 hour",
        120 to "2 hours",
        180 to "3 hours"
    )

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        periods.forEach { (minutes, label) ->
            FilterChip(
                selected = selectedMinutes == minutes,
                onClick = { onPeriodChange(minutes) },
                label = { Text(label) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}
