package `in`.hridayan.driftly.settings.presentation.components.item

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ChangelogItemLayout(
    modifier: Modifier = Modifier,
    versionName: String,
    changelog: List<String>,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(3.dp) // Gap between cards in a group: 3dp
    ) {
        // Group Header Card (Version Name)
        val totalItems = changelog.size + 1 // Version + Lines
        
        // Version Header Card
        ChangelogGroupCard(
            isFirst = true,
            isLast = changelog.isEmpty(),
            isOnly = totalItems == 1,
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Version $versionName",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold
                )
                
                Surface(
                    shape = RoundedCornerShape(50.dp),
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.15f),
                ) {
                    Text(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        text = "LATEST",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp
                    )
                }
            }
        }

        // Changelog Lines as continuation of the group
        changelog.forEachIndexed { index, item ->
            val isLastLine = index == changelog.size - 1
            ChangelogGroupCard(
                isFirst = false,
                isLast = isLastLine,
                isOnly = false,
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Box(
                        modifier = Modifier
                            .padding(top = 8.dp)
                            .size(6.dp)
                            .clip(RoundedCornerShape(50.dp))
                            .background(MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f))
                    )

                    Text(
                        text = item,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f),
                        lineHeight = 20.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun ChangelogGroupCard(
    isFirst: Boolean,
    isLast: Boolean,
    isOnly: Boolean,
    containerColor: androidx.compose.ui.graphics.Color,
    contentColor: androidx.compose.ui.graphics.Color,
    content: @Composable () -> Unit
) {
    val shape = when {
        isOnly -> RoundedCornerShape(15.dp)
        isFirst -> RoundedCornerShape(topStart = 15.dp, topEnd = 15.dp, bottomStart = 3.dp, bottomEnd = 3.dp)
        isLast -> RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp, bottomStart = 15.dp, bottomEnd = 15.dp)
        else -> RoundedCornerShape(3.dp)
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = shape,
        color = containerColor,
        contentColor = contentColor,
        tonalElevation = 0.dp
    ) {
        Box(modifier = Modifier.padding(16.dp)) {
            content()
        }
    }
}