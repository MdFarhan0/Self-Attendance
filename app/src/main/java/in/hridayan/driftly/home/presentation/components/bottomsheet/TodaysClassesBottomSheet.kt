package `in`.hridayan.driftly.home.presentation.components.bottomsheet

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable  
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import `in`.hridayan.driftly.core.data.model.SubjectEntity
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

data class TodayClass(
    val subject: SubjectEntity,
    val startTime: LocalTime,
    val endTime: LocalTime,
    val duration: Long // in minutes
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodaysClassesBottomSheet(
    todaysClasses: List<TodayClass>,
    onDismiss: () -> Unit,
    sheetState: SheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = false
    )  
) {
    val today = LocalDate.now()
    val dayOfWeek = today.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault())
    val dayAndDate = today.format(DateTimeFormatter.ofPattern("MMMM dd", Locale.getDefault()))
    val weekNumber = today.format(DateTimeFormatter.ofPattern("w", Locale.getDefault()))
    
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .width(40.dp)
                        .height(4.dp)
                        .background(
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                            RoundedCornerShape(2.dp)
                        )
                )
            }
        }
    ) {
        // Dynamic height calculation: 
        // Up to 8 cards show without scrolling, more than 8 cards need scrolling
        val numClasses = todaysClasses.size.coerceAtMost(8)
        val heightFraction = when {
            numClasses <= 3 -> 0.55f
            numClasses <= 5 -> 0.70f
            numClasses <= 8 -> 0.90f
            else -> 0.95f // 9+ cards need scrolling
        }
        
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(heightFraction)
                .padding(horizontal = 24.dp)
        ) {
            // Header - just title, no week badge
            Text(
                text = "Today's Classes",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Day and date subtitle
            Text(
                text = "$dayOfWeek, $dayAndDate • ${todaysClasses.size} Classes",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Classes list
            if (todaysClasses.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "No classes scheduled for today",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    itemsIndexed(todaysClasses) { index, classItem ->
                        val isFirst = index == 0
                        val isLast = index == todaysClasses.size - 1
                        val isOnly = todaysClasses.size == 1
                        
                        val shape = when {
                            isOnly -> RoundedCornerShape(20.dp)
                            isFirst -> RoundedCornerShape(
                                topStart = 20.dp,
                                topEnd = 20.dp,
                                bottomStart = 6.dp,
                                bottomEnd = 6.dp
                            )
                            isLast -> RoundedCornerShape(
                                topStart = 6.dp,
                                topEnd = 6.dp,
                                bottomStart = 20.dp,
                                bottomEnd = 20.dp
                            )
                            else -> RoundedCornerShape(6.dp)
                        }
                        
                        ClassCard(
                            classItem = classItem,
                            shape = shape
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun ClassCard(
    classItem: TodayClass,
    shape: RoundedCornerShape
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = shape,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Subject name
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = classItem.subject.subject,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp
                    ),
                    color = MaterialTheme.colorScheme.primary
                )
                
                if (!classItem.subject.subjectCode.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = classItem.subject.subjectCode,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Time and duration
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = classItem.startTime.format(DateTimeFormatter.ofPattern("hh:mm a")),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(2.dp))
                
                // Duration
                val durationText = when {
                    classItem.duration >= 60 -> "${classItem.duration / 60}h ${classItem.duration % 60}m"
                    else -> "${classItem.duration}m"
                }
                
                Text(
                    text = durationText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
