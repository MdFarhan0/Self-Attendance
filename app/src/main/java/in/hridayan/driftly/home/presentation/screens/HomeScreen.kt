package `in`.hridayan.driftly.home.presentation.screens

import android.annotation.SuppressLint
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccessTime
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Analytics
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.FloatingActionButtonMenu
import androidx.compose.material3.FloatingActionButtonMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.Text
import androidx.compose.material3.animateFloatingActionButton
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import `in`.hridayan.driftly.R
import `in`.hridayan.driftly.core.common.LocalSettings
import `in`.hridayan.driftly.core.common.LocalWeakHaptic
import `in`.hridayan.driftly.core.domain.model.SubjectAttendance
import `in`.hridayan.driftly.core.domain.model.TotalAttendance
import `in`.hridayan.driftly.core.presentation.components.dialog.NotificationPermDialog
import `in`.hridayan.driftly.core.presentation.components.progress.AnimatedHalfCircleProgress
import `in`.hridayan.driftly.notification.isNotificationPermissionGranted
import `in`.hridayan.driftly.home.presentation.components.card.SubjectCard
import `in`.hridayan.driftly.home.presentation.components.dialog.AddSubjectDialog
import `in`.hridayan.driftly.home.presentation.components.drawer.SmartAttendanceDrawer
import `in`.hridayan.driftly.home.presentation.components.bottomsheet.BunkDetailsBottomSheet
import `in`.hridayan.driftly.home.presentation.components.bottomsheet.TodaysClassesBottomSheet
import `in`.hridayan.driftly.home.presentation.components.bottomsheet.TomorrowsClassesBottomSheet
import `in`.hridayan.driftly.home.presentation.components.bottomsheet.TodayClass
import `in`.hridayan.driftly.home.presentation.components.histogram.AttendanceHistogramCard
import `in`.hridayan.driftly.home.presentation.components.histogram.SubjectHistogramData
import `in`.hridayan.driftly.home.presentation.components.image.UndrawRelaxedReading
import `in`.hridayan.driftly.home.presentation.components.label.Label
import `in`.hridayan.driftly.home.presentation.viewmodel.HomeViewModel
import `in`.hridayan.driftly.navigation.CalendarScreen
import `in`.hridayan.driftly.navigation.LocalNavController
import `in`.hridayan.driftly.navigation.SettingsScreen
import `in`.hridayan.driftly.settings.data.local.SettingsKeys
import `in`.hridayan.driftly.settings.presentation.event.SettingsUiEvent
import `in`.hridayan.driftly.settings.presentation.viewmodel.SettingsViewModel
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import androidx.compose.material3.ExperimentalMaterial3Api

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@SuppressLint("DefaultLocale")
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel(),
    settingsViewModel: SettingsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val weakHaptic = LocalWeakHaptic.current
    val navController = LocalNavController.current
    // val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed) // Drawer removed
    val scope = rememberCoroutineScope()
    
    val subjects by viewModel.subjectList.collectAsState(initial = emptyList())
    val subjectCount by viewModel.subjectCount.collectAsState(initial = 0)
    var isDialogOpen by rememberSaveable { mutableStateOf(false) }
    val totalAttendance by viewModel.getTotalAttendanceCounts()
        .collectAsState(initial = TotalAttendance())
    val totalPresent = totalAttendance.totalPresent
    val totalAbsent = totalAttendance.totalAbsent
    val totalCount = totalAttendance.totalCount
    val totalProgress = totalPresent.toFloat() / totalCount.toFloat()
    val totalProgressText = "${String.format("%.2f", totalProgress * 100)}%"



    val progressColor = lerp(
        start = MaterialTheme.colorScheme.error,
        stop = MaterialTheme.colorScheme.primary,
        fraction = totalProgress.coerceIn(0f, 1f)
    )

    val subjectCardCornerRadius = LocalSettings.current.subjectCardCornerRadius
    var selectedCardsCount by rememberSaveable { mutableIntStateOf(0) }
    var showNotificationPermissionDialog by rememberSaveable { mutableStateOf(false) }
    var showBunkBottomSheet by rememberSaveable { mutableStateOf(false) }
    var showTodayBottomSheet by rememberSaveable { mutableStateOf(false) }
    var showTomorrowBottomSheet by rememberSaveable { mutableStateOf(false) }
    var isFabMenuExpanded by rememberSaveable { mutableStateOf(false) }
    var showTimetableInputSheet by rememberSaveable { mutableStateOf(false) }

    // Build today's class list from all subjects' timetables
    val todayDayOfWeek = LocalDate.now().dayOfWeek.value // 1=Mon … 7=Sun
    val todaysClasses = subjects.flatMap { subject ->
        viewModel.getSchedulesForSubject(subject.id)
            .collectAsState(initial = emptyList()).value
            .filter { it.dayOfWeek == todayDayOfWeek }
            .map { schedule ->
                val start = runCatching {
                    LocalTime.parse(schedule.startTime, DateTimeFormatter.ofPattern("HH:mm"))
                }.getOrElse { LocalTime.MIDNIGHT }
                val end = runCatching {
                    LocalTime.parse(schedule.endTime, DateTimeFormatter.ofPattern("HH:mm"))
                }.getOrElse { LocalTime.MIDNIGHT }
                val durationMin = java.time.Duration.between(start, end).toMinutes()
                TodayClass(
                    subject = subject,
                    startTime = start,
                    endTime = end,
                    duration = durationMin
                )
            }
    }.sortedBy { it.startTime }

    // Build tomorrow's class list
    val tomorrowLocalDate = LocalDate.now().plusDays(1)
    val tomorrowDayOfWeek = tomorrowLocalDate.dayOfWeek.value
    val tomorrowsClasses = subjects.flatMap { subject ->
        viewModel.getSchedulesForSubject(subject.id)
            .collectAsState(initial = emptyList()).value
            .filter { it.dayOfWeek == tomorrowDayOfWeek }
            .map { schedule ->
                val start = runCatching {
                    LocalTime.parse(schedule.startTime, DateTimeFormatter.ofPattern("HH:mm"))
                }.getOrElse { LocalTime.MIDNIGHT }
                val end = runCatching {
                    LocalTime.parse(schedule.endTime, DateTimeFormatter.ofPattern("HH:mm"))
                }.getOrElse { LocalTime.MIDNIGHT }
                val durationMin = java.time.Duration.between(start, end).toMinutes()
                TodayClass(
                    subject = subject,
                    startTime = start,
                    endTime = end,
                    duration = durationMin
                )
            }
    }.sortedBy { it.startTime }
    
    // Track scroll state for FAB visibility
    val listState = rememberLazyListState()
    val fabVisible by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex == 0 || 
            listState.firstVisibleItemScrollOffset < 100
        }
    }
    // Interaction source removed
    val notificationsEnabled by rememberSaveable {
        mutableStateOf(
            isNotificationPermissionGranted(
                context
            )
        )
    }

    val notificationPreference = LocalSettings.current.notificationPreference

    val launcherReqPerm = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->

            settingsViewModel.setBoolean(
                key = SettingsKeys.ENABLE_NOTIFICATIONS,
                value = isGranted || notificationPreference
            )

            settingsViewModel.refreshNotificationPermissionState()
        }
    )

    val launcherIntent = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
        onResult = {
            val isGranted = isNotificationPermissionGranted(context)

            settingsViewModel.setBoolean(
                key = SettingsKeys.ENABLE_NOTIFICATIONS,
                value = isGranted || notificationPreference
            )

            settingsViewModel.refreshNotificationPermissionState()
        }
    )

    val notificationPermDialogShown = LocalSettings.current.notificationPermissionDialogShown

    LaunchedEffect(notificationsEnabled, notificationPermDialogShown, totalCount) {
        showNotificationPermissionDialog =
            !notificationsEnabled && !notificationPermDialogShown && totalCount != 0
    }

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is SettingsUiEvent.RequestPermission -> launcherReqPerm.launch(event.permission)

                is SettingsUiEvent.LaunchIntent -> launcherIntent.launch(event.intent)

                else -> {}
            }
        }
    }

    BackHandler(enabled = selectedCardsCount > 0 || isFabMenuExpanded) {
        if (selectedCardsCount > 0) selectedCardsCount = 0
        if (isFabMenuExpanded) isFabMenuExpanded = false
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
    ) { innerPadding ->

        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(5.dp), // Changed from 15.dp to 5.dp for grouped appearance
                contentPadding = PaddingValues(
                    top = innerPadding.calculateTopPadding(),
                    bottom = innerPadding.calculateBottomPadding() + 85.dp, // Space for button
                    start = 0.dp,
                    end = 0.dp
                ),
            ) {
                item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 25.dp, end = 25.dp, top = 35.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.app_name),
                        style = MaterialTheme.typography.displaySmallEmphasized.copy(
                            fontWeight = FontWeight.ExtraBold
                        ),
                        modifier = Modifier.alpha(0.95f)
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    Surface(
                        onClick = {
                            navController.navigate(SettingsScreen)
                            weakHaptic()
                        },
                        shape = CircleShape,
                        color = androidx.compose.ui.graphics.Color.Transparent,
                        contentColor = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(42.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Outlined.Settings,
                                contentDescription = "Settings",
                                modifier = Modifier.size(26.dp)
                            )
                        }
                    }

                }
            }

            if (subjectCount == 0 || totalCount == 0) {
                item {
                    Box(
                        modifier
                            .fillMaxWidth()
                            .padding(horizontal = 25.dp)
                            .then(
                                if (subjectCount == 0) Modifier.height(400.dp)
                                else Modifier.padding(vertical = 20.dp)
                            ), contentAlignment = Alignment.Center
                    ) {
                        UndrawRelaxedReading()
                    }
                }
            }

            if (subjectCount == 0) {
                item {
                    Text(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 25.dp)
                            .alpha(0.75f),
                        text = stringResource(R.string.no_subject_yet),
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center
                    )
                }
            }

            if (subjectCount != 0 && totalCount != 0) {
                item {
                    // Prepare histogram data
                    val histogramData = subjects.mapNotNull { subject ->
                        val counts = viewModel.getSubjectAttendanceCounts(subject.id)
                            .collectAsState(initial = SubjectAttendance()).value
                        
                        if (counts.totalCount > 0) {
                            val percentage = (counts.presentCount.toFloat() / counts.totalCount.toFloat()) * 100
                            SubjectHistogramData(
                                subject = subject,
                                percentage = percentage
                            )
                        } else null
                    }

                    AttendanceHistogramCard(
                        modifier = Modifier.padding(top = 35.dp, bottom = 20.dp),
                        histogramData = histogramData
                    )
                }
            }

            items(subjects.size, key = { index -> subjects[index].id }) { index ->

                val counts by viewModel.getSubjectAttendanceCounts(subjects[index].id)
                    .collectAsState(initial = SubjectAttendance())

                val progress = counts.presentCount.toFloat() / counts.totalCount.toFloat()
                
                // Calculate grouped card corner radius
                val isFirst = index == 0
                val isLast = index == subjects.size - 1
                val isOnly = subjects.size == 1
                
                val cornerRadius = when {
                    isOnly -> 25.dp // Single card - all corners rounded
                    isFirst -> 25.dp // First card - will use custom shape
                    isLast -> 25.dp // Last card - will use custom shape
                    else -> 10.dp // Middle cards - small corners
                }
                
                // Determine the shape based on position
                val cardShape = when {
                    isOnly -> RoundedCornerShape(20.dp)
                    isFirst -> RoundedCornerShape(
                        topStart = 20.dp,
                        topEnd = 20.dp,
                        bottomStart = 5.dp,
                        bottomEnd = 5.dp
                    )
                    isLast -> RoundedCornerShape(
                        topStart = 5.dp,
                        topEnd = 5.dp,
                        bottomStart = 20.dp,
                        bottomEnd = 20.dp
                    )
                    else -> RoundedCornerShape(5.dp)
                }

                SubjectCard(
                    modifier = Modifier
                        .padding(horizontal = 15.dp)
                        .animateItem(),
                    cardStyle = LocalSettings.current.subjectCardStyle,
                    cornerRadius = 20.dp,
                    customShape = cardShape,
                    subjectId = subjects[index].id,
                    subject = subjects[index].subject,
                    subjectCode = subjects[index].subjectCode,
                    progress = progress,
                    isTotalCountZero = counts.totalCount == 0,
                    selectedCardsCount = selectedCardsCount,
                    navigate = {
                        navController.navigate(
                            CalendarScreen(
                                subjectId = subjects[index].id,
                                subject = subjects[index].subject
                            )
                        )
                    },
                    onLongClicked = { isLongClicked ->
                        if (isLongClicked) selectedCardsCount++ else selectedCardsCount--
                    },
                    onMoveUp = {
                        if (index > 0) {
                            val newList = subjects.toMutableList()
                            val temp = newList[index]
                            newList[index] = newList[index - 1]
                            newList[index - 1] = temp
                            viewModel.updateSubjectsOrder(newList)
                        }
                    },
                    onMoveDown = {
                        if (index < subjects.size - 1) {
                            val newList = subjects.toMutableList()
                            val temp = newList[index]
                            newList[index] = newList[index + 1]
                            newList[index + 1] = temp
                            viewModel.updateSubjectsOrder(newList)
                        }
                    },
                    onMoveTop = {
                        if (index > 0) {
                            val newList = subjects.toMutableList()
                            val item = newList.removeAt(index)
                            newList.add(0, item)
                            viewModel.updateSubjectsOrder(newList)
                        }
                    },
                    onMoveBottom = {
                        if (index < subjects.size - 1) {
                            val newList = subjects.toMutableList()
                            val item = newList.removeAt(index)
                            newList.add(item)
                            viewModel.updateSubjectsOrder(newList)
                        }
                    }
                )
            }

                item {
                    Spacer(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                    )
                }
            }

            // Backdrop for FAB Menu focus and dismissal
            AnimatedVisibility(
                visible = isFabMenuExpanded && selectedCardsCount == 0,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.32f))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            isFabMenuExpanded = false
                        }
                )
            }

            // M3 Expressive FAB Menu
            AnimatedVisibility(
                visible = selectedCardsCount == 0,
                enter = fadeIn(animationSpec = tween(250)) +
                        slideInVertically(
                            initialOffsetY = { it / 2 },
                            animationSpec = tween(250)
                        ),
                exit = fadeOut(animationSpec = tween(250)) +
                        slideOutVertically(
                            targetOffsetY = { it / 2 },
                            animationSpec = tween(250)
                        ),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = 15.dp, end = 15.dp) // Adjusted padding for larger FAB
            ) {
                val fabSize by animateDpAsState(
                    targetValue = if (isFabMenuExpanded) 56.dp else 80.dp,
                    animationSpec = spring(dampingRatio = 0.75f),
                    label = "FabSize"
                )
                val fabShape = if (isFabMenuExpanded) CircleShape else RoundedCornerShape(16.dp)

                FloatingActionButtonMenu(
                    expanded = isFabMenuExpanded,
                    button = {
                        FloatingActionButton(
                            onClick = {
                                isFabMenuExpanded = !isFabMenuExpanded
                                weakHaptic()
                            },
                            modifier = Modifier
                                .animateFloatingActionButton(
                                    visible = fabVisible, // Removed redundant isFabMenuExpanded
                                    alignment = Alignment.BottomEnd
                                )
                                .size(fabSize),
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                            shape = fabShape,
                            elevation = FloatingActionButtonDefaults.bottomAppBarFabElevation(0.dp)
                        ) {
                            val imageVector = if (isFabMenuExpanded) Icons.Rounded.Close else Icons.Rounded.Add
                            Icon(
                                imageVector = imageVector,
                                contentDescription = if (isFabMenuExpanded) "Close" else "Add",
                                modifier = Modifier.size(if (isFabMenuExpanded) 24.dp else 32.dp)
                            )
                        }
                    }
                ) {
                    // Add Subject Action
                    FloatingActionButtonMenuItem(
                        onClick = {
                            isFabMenuExpanded = false
                            isDialogOpen = true
                            weakHaptic()
                        },
                        icon = {
                            Icon(Icons.Rounded.Add, contentDescription = null)
                        },
                        text = {
                            Text(text = "Add Subject")
                        }
                    )
                    
                    // Today's Class Action
                    FloatingActionButtonMenuItem(
                        onClick = {
                            isFabMenuExpanded = false
                            showTodayBottomSheet = true
                            weakHaptic()
                        },
                        icon = {
                            Icon(Icons.Rounded.AccessTime, contentDescription = null)
                        },
                        text = {
                            Text(text = "Today's Class")
                        }
                    )
                    
                    // Tomorrow's Class Action
                    FloatingActionButtonMenuItem(
                        onClick = {
                            isFabMenuExpanded = false
                            showTomorrowBottomSheet = true
                            weakHaptic()
                        },
                        icon = {
                            Icon(Icons.Rounded.CalendarMonth, contentDescription = null)
                        },
                        text = {
                            Text(text = "Tomorrow's Class")
                        }
                    )
                    
                    // Bunk Details Action
                    FloatingActionButtonMenuItem(
                        onClick = {
                            isFabMenuExpanded = false
                            showBunkBottomSheet = true
                            weakHaptic()
                        },
                        icon = {
                            Icon(Icons.Rounded.Analytics, contentDescription = null)
                        },
                        text = {
                            Text(text = "Bunk Details")
                        }
                    )
                }
            }
        }

    if (isDialogOpen) {
        AddSubjectDialog(
            onDismiss = {
                isDialogOpen = false
            })
    }

    if (showBunkBottomSheet) {
        BunkDetailsBottomSheet(
            onDismiss = { showBunkBottomSheet = false }
        )
    }

    if (showTodayBottomSheet) {
        TodaysClassesBottomSheet(
            todaysClasses = todaysClasses,
            onDismiss = { showTodayBottomSheet = false }
        )
    }

    if (showTomorrowBottomSheet) {
        TomorrowsClassesBottomSheet(
            tomorrowsClasses = tomorrowsClasses,
            onDismiss = { showTomorrowBottomSheet = false }
        )
    }

    if (showNotificationPermissionDialog) {
        NotificationPermDialog(
            onDismiss = {
                showNotificationPermissionDialog = false
                settingsViewModel.setBoolean(
                    SettingsKeys.NOTIFICATION_PERMISSION_DIALOG_SHOWN,
                    true
                )
            },
            onConfirm = {
                viewModel.requestNotificationPermission()
                showNotificationPermissionDialog = false
                settingsViewModel.setBoolean(
                    SettingsKeys.NOTIFICATION_PERMISSION_DIALOG_SHOWN,
                    true
                )
            })
    }
    }
}
