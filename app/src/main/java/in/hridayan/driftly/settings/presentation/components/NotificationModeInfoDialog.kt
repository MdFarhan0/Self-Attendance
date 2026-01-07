package `in`.hridayan.driftly.settings.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import `in`.hridayan.driftly.core.domain.model.NotificationMode

@Composable
fun NotificationModeInfoDialog(
    mode: NotificationMode,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = if (mode == NotificationMode.STANDARD)
                            Icons.Default.Notifications
                        else
                            Icons.Default.NotificationsActive,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    
                    Text(
                        text = if (mode == NotificationMode.STANDARD)
                            "Standard Notifications"
                        else
                            "Persistent Notifications",
                        style = MaterialTheme.typography.titleLarge
                    )
                }

                Divider()

                // Content
                if (mode == NotificationMode.STANDARD) {
                    StandardModeInfo()
                } else {
                    PersistentModeInfo()
                }

                // Close Button
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Got it")
                }
            }
        }
    }
}

@Composable
private fun StandardModeInfo() {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "How it works:",
            style = MaterialTheme.typography.titleSmall
        )

        InfoPoint("View timetable and schedule for reference")
        InfoPoint("Manual attendance marking only")
        InfoPoint("Simple reminder notifications (optional)")
        InfoPoint("No automatic actions")
        InfoPoint("Full control over your attendance records")

        Text(
            text = "Best for:",
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.padding(top = 8.dp)
        )
        
        Text(
            "• Irregular schedules\n" +
            "• Self-paced tracking\n" +
            "• When you prefer manual control",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun PersistentModeInfo() {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "How it works:",
            style = MaterialTheme.typography.titleSmall
        )

        InfoPoint("Notification appears when class starts")
        InfoPoint("Quick Action buttons: Attended, Missed, Cancelled")
        InfoPoint("Notification stays until you take action")
        InfoPoint("Grace period after class ends")
        InfoPoint("Auto-marks as 'Missed' if no action taken")
        InfoPoint("Undo option available after auto-marking")

        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.tertiaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    "Example:",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
                Text(
                    "Class at 9:00 AM, grace period: 1 hour\n" +
                    "• Notification at 9:00 AM\n" +
                    "• You have until 11:00 AM to mark\n" +
                    "• Auto-marked 'Missed' at 11:00 AM if not marked",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
            }
        }

        Text(
            text = "Best for:",
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.padding(top = 8.dp)
        )
        
        Text(
            "• Fixed weekly schedules\n" +
            "• Automated tracking\n" +
            "• Ensuring no attendance is forgotten",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun InfoPoint(text: String) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            Icons.Default.CheckCircle,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
