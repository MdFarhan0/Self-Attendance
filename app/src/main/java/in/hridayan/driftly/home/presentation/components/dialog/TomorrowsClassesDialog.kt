@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)

package `in`.hridayan.driftly.home.presentation.components.dialog

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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.input.nestedscroll.nestedScroll
import `in`.hridayan.driftly.core.presentation.components.button.BackButton
import `in`.hridayan.driftly.core.presentation.components.card.adaptiveCardContainerColor
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun TomorrowsClassesDialog(
    modifier: Modifier = Modifier,
    tomorrowsClasses: List<TodayClass>,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false),
    ) {
        TomorrowsClassesContent(modifier = modifier, tomorrowsClasses = tomorrowsClasses, onDismiss = onDismiss)
    }
}

@Composable
fun TomorrowsClassesContent(
    modifier: Modifier = Modifier,
    tomorrowsClasses: List<TodayClass>,
    onDismiss: () -> Unit
) {
    val tomorrow = LocalDate.now().plusDays(1)
    val dayOfWeek = tomorrow.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault())
    val dayAndDate = tomorrow.format(DateTimeFormatter.ofPattern("MMMM d", Locale.getDefault()))
    val classCount = tomorrowsClasses.size

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

                    Column {
                        Text(
                            modifier = Modifier.basicMarquee(),
                            text = "Tomorrow's Classes",
                            maxLines = 1,
                            fontSize = fontSize,
                            style = MaterialTheme.typography.displaySmallEmphasized.copy(
                                fontWeight = FontWeight.ExtraBold
                            )
                        )
                        if (collapsedFraction < 0.5f) {
                            Text(
                                text = "$dayOfWeek, $dayAndDate • $classCount ${if (classCount == 1) "Class" else "Classes"}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
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
        if (tomorrowsClasses.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No classes scheduled for tomorrow 🎉",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            val sorted = tomorrowsClasses.sortedBy { it.startTime }

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
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                item { Spacer(modifier = Modifier.height(4.dp)) }

                val totalSize = sorted.size
                itemsIndexed(sorted) { index, classItem ->
                    val shape = when {
                        totalSize == 1 -> RoundedCornerShape(16.dp)
                        index == 0 -> RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 4.dp, bottomEnd = 4.dp)
                        index == totalSize - 1 -> RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp, bottomStart = 16.dp, bottomEnd = 16.dp)
                        else -> RoundedCornerShape(4.dp)
                    }

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

                item { Spacer(modifier = Modifier.height(40.dp)) }
            }
        }
    }
}
