@file:OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)

package `in`.hridayan.driftly.home.presentation.components.dialog

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.TrackChanges
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.DateRange
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.TrackChanges
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingToolbarDefaults
import androidx.compose.material3.HorizontalFloatingToolbar
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import `in`.hridayan.driftly.home.presentation.viewmodel.HomeViewModel

enum class SecondaryTab(
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    Bunk("Bunk", Icons.Rounded.TrackChanges, Icons.Outlined.TrackChanges),
    Tomorrow("Tomorrow", Icons.Rounded.DateRange, Icons.Outlined.DateRange),
    Today("Today", Icons.Rounded.CalendarMonth, Icons.Outlined.CalendarMonth),
    Timetable("Timetable", Icons.Rounded.School, Icons.Outlined.School)
}

@Composable
fun SecondaryPagesDialog(
    modifier: Modifier = Modifier,
    initialTab: SecondaryTab,
    todaysClasses: List<TodayClass>,
    tomorrowsClasses: List<TodayClass>,
    viewModel: HomeViewModel = hiltViewModel(),
    onDismiss: () -> Unit
) {
    var currentTab by rememberSaveable { mutableStateOf(initialTab) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false),
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Render the active full screen screen content
            when (currentTab) {
                SecondaryTab.Bunk -> {
                    BunkDetailsContent(
                        viewModel = viewModel,
                        onDismiss = onDismiss
                    )
                }
                SecondaryTab.Tomorrow -> {
                    TomorrowsClassesContent(
                        tomorrowsClasses = tomorrowsClasses,
                        onDismiss = onDismiss
                    )
                }
                SecondaryTab.Today -> {
                    TodaysClassesContent(
                        todaysClasses = todaysClasses,
                        onDismiss = onDismiss
                    )
                }
                SecondaryTab.Timetable -> {
                    FullTimetableContent(
                        viewModel = viewModel,
                        onDismiss = onDismiss
                    )
                }
            }

            // Floating Navigation Bar (HorizontalFloatingToolbar)
            HorizontalFloatingToolbar(
                expanded = true,
                colors = FloatingToolbarDefaults.vibrantFloatingToolbarColors(
                    toolbarContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    toolbarContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 56.dp)
            ) {
                SecondaryTab.values().forEach { tabItem ->
                    val selected = currentTab == tabItem

                    ToggleButton(
                        checked = selected,
                        onCheckedChange = {
                            if (!selected) {
                                currentTab = tabItem
                            }
                        },
                        colors = ToggleButtonDefaults.toggleButtonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            checkedContainerColor = MaterialTheme.colorScheme.primary,
                            checkedContentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        shapes = ToggleButtonDefaults.shapes(
                            CircleShape,
                            CircleShape,
                            CircleShape
                        ),
                        modifier = Modifier
                            .align(Alignment.CenterVertically)
                            .then(
                                if (selected) Modifier.height(56.dp)
                                else Modifier.size(40.dp)
                            ),
                        contentPadding = if (selected) ToggleButtonDefaults.ContentPadding else PaddingValues(0.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                             Icon(
                                 imageVector = if (selected) tabItem.selectedIcon else tabItem.unselectedIcon,
                                 contentDescription = tabItem.label,
                                 modifier = Modifier.size(24.dp)
                             )
                            AnimatedVisibility(
                                visible = selected,
                                enter = expandHorizontally(),
                                exit = shrinkHorizontally()
                            ) {
                                Text(
                                    text = tabItem.label,
                                    fontSize = 15.sp,
                                    lineHeight = 22.sp,
                                    maxLines = 1,
                                    softWrap = false,
                                    overflow = TextOverflow.Clip,
                                    modifier = Modifier.padding(start = 8.dp),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
