@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)

package `in`.hridayan.driftly.home.presentation.components.dialog

import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import `in`.hridayan.driftly.core.data.model.SubjectEntity
import `in`.hridayan.driftly.core.presentation.components.card.adaptiveCardContainerColor
import `in`.hridayan.driftly.home.presentation.viewmodel.HomeViewModel
import `in`.hridayan.driftly.core.common.LocalWeakHaptic
import `in`.hridayan.driftly.core.presentation.components.button.BackButton
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.input.nestedscroll.nestedScroll
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

data class TimetableClass(
    val subject: SubjectEntity,
    val startTime: LocalTime,
    val endTime: LocalTime
)

@Composable
fun FullTimetableDialog(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel(),
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false),
    ) {
        FullTimetableContent(modifier = modifier, viewModel = viewModel, onDismiss = onDismiss)
    }
}

@Composable
fun FullTimetableContent(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel(),
    onDismiss: () -> Unit
) {
    val subjects by viewModel.subjectList.collectAsState(initial = emptyList())
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())
    val listState = rememberLazyListState()

    Scaffold(
        modifier = modifier.fillMaxSize(),
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
                        text = "Timetable",
                        maxLines = 1,
                        fontSize = fontSize,
                        style = MaterialTheme.typography.displaySmallEmphasized.copy(
                            fontWeight = FontWeight.ExtraBold
                        )
                    )
                },
                navigationIcon = {
                    Box(modifier = Modifier.padding(start = 5.dp, end = 6.dp)) {
                        BackButton(onClick = onDismiss)
                    }
                },
                scrollBehavior = scrollBehavior
            )
        }
    ) { paddingValues ->
        // Collect all classes
        val allSchedules = subjects.flatMap { subject ->
            viewModel.getSchedulesForSubject(subject.id)
                .collectAsState(initial = emptyList()).value
                .map { schedule ->
                    val start = runCatching {
                        LocalTime.parse(schedule.startTime, DateTimeFormatter.ofPattern("HH:mm"))
                    }.getOrElse { LocalTime.MIDNIGHT }
                    val end = runCatching {
                        LocalTime.parse(schedule.endTime, DateTimeFormatter.ofPattern("HH:mm"))
                    }.getOrElse { LocalTime.MIDNIGHT }
                    
                    schedule.dayOfWeek to TimetableClass(subject = subject, startTime = start, endTime = end)
                }
        }
        
        // Group by dayOfWeek (1=Mon..7=Sun)
        val groupedByDay = allSchedules.groupBy { it.first }
            .mapValues { entry -> entry.value.map { it.second }.sortedBy { it.startTime } }
            .toSortedMap()

        if (groupedByDay.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No classes scheduled 📝",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .nestedScroll(scrollBehavior.nestedScrollConnection),
                state = listState,
                contentPadding = PaddingValues(
                    top = paddingValues.calculateTopPadding(),
                    bottom = paddingValues.calculateBottomPadding() + 80.dp, // Extra padding for floating bar
                    start = 16.dp,
                    end = 16.dp
                ),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                item { Spacer(modifier = Modifier.height(4.dp)) }
                
                for (day in 1..7) {
                    val dayClasses = groupedByDay[day]
                    if (!dayClasses.isNullOrEmpty()) {
                        val dayName = when (day) {
                            1 -> "Monday"; 2 -> "Tuesday"; 3 -> "Wednesday"; 4 -> "Thursday"
                            5 -> "Friday"; 6 -> "Saturday"; 7 -> "Sunday"; else -> ""
                        }
                        
                        item {
                            DayTimetableGroup(dayName = dayName, classes = dayClasses)
                        }
                    }
                }
                
                item { Spacer(modifier = Modifier.height(40.dp)) }
            }
        }
    }
}

@Composable
private fun DayTimetableGroup(
    dayName: String,
    classes: List<TimetableClass>
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // Day Chip
        Surface(
            shape = RoundedCornerShape(100.dp),
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.padding(bottom = 12.dp)
        ) {
            Text(
                text = dayName,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
        
        // Subject cards
        val totalSize = classes.size
        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
            classes.forEachIndexed { index, classItem ->
                val shape = when {
                    totalSize == 1 -> RoundedCornerShape(16.dp)
                    index == 0 -> RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 4.dp, bottomEnd = 4.dp)
                    index == totalSize - 1 -> RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp, bottomStart = 16.dp, bottomEnd = 16.dp)
                    else -> RoundedCornerShape(4.dp)
                }

                TimetableClassCard(classItem = classItem, shape = shape)
            }
        }
    }
}

@Composable
private fun TimetableClassCard(
    classItem: TimetableClass,
    shape: RoundedCornerShape
) {
    val timeFmt = DateTimeFormatter.ofPattern("hh:mm a", Locale.getDefault())
 
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = shape,
        color = adaptiveCardContainerColor(),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
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
                    text = classItem.subject.subject,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${classItem.startTime.format(timeFmt)} - ${classItem.endTime.format(timeFmt)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
