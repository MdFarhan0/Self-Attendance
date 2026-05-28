package `in`.hridayan.driftly.home.presentation.screens

import android.annotation.SuppressLint
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccessTime
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Analytics
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.DateRange
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingActionButtonMenu
import androidx.compose.material3.FloatingActionButtonMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.animateFloat
import androidx.compose.material3.IconButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
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
import `in`.hridayan.driftly.core.presentation.components.text.AutoResizeableText
import `in`.hridayan.driftly.core.presentation.theme.adaptiveStrongScrimColor
import `in`.hridayan.driftly.home.presentation.components.dialog.TodayClass
import `in`.hridayan.driftly.home.presentation.components.card.SubjectCard
import `in`.hridayan.driftly.home.presentation.components.dialog.AddSubjectDialog
import `in`.hridayan.driftly.home.presentation.components.dialog.FullTimetableDialog
import `in`.hridayan.driftly.home.presentation.components.dialog.BunkDetailsDialog
import `in`.hridayan.driftly.home.presentation.components.dialog.TodaysClassesDialog
import `in`.hridayan.driftly.home.presentation.components.dialog.TomorrowsClassesDialog
import `in`.hridayan.driftly.home.presentation.components.dialog.SecondaryTab
import `in`.hridayan.driftly.home.presentation.components.dialog.SecondaryPagesDialog

import `in`.hridayan.driftly.home.presentation.components.image.UndrawRelaxedReading
import `in`.hridayan.driftly.home.presentation.viewmodel.HomeViewModel
import `in`.hridayan.driftly.navigation.CalendarScreen
import `in`.hridayan.driftly.navigation.LocalNavController
import `in`.hridayan.driftly.navigation.SettingsScreen
import `in`.hridayan.driftly.notification.isNotificationPermissionGranted
import `in`.hridayan.driftly.settings.data.local.SettingsKeys
import `in`.hridayan.driftly.settings.presentation.event.SettingsUiEvent
import `in`.hridayan.driftly.settings.presentation.viewmodel.SettingsViewModel
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@SuppressLint("DefaultLocale")
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel(),
    settingsViewModel: SettingsViewModel = hiltViewModel()
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val weakHaptic = LocalWeakHaptic.current
    val navController = LocalNavController.current

    val subjects by viewModel.subjectList.collectAsState(initial = emptyList())
    val subjectCount by viewModel.subjectCount.collectAsState(initial = -1)
    var isDialogOpen by rememberSaveable { mutableStateOf(false) }

    val totalAttendance by viewModel.getTotalAttendanceCounts()
        .collectAsState(initial = TotalAttendance())
    val totalPresent = totalAttendance.totalPresent
    val totalAbsent = totalAttendance.totalAbsent
    val totalCount = totalAttendance.totalCount

    val subjectCardCornerRadius = LocalSettings.current.subjectCardCornerRadius
    var selectedCardsCount by rememberSaveable { mutableIntStateOf(0) }

    var showNotificationPermissionDialog by rememberSaveable { mutableStateOf(false) }
    var activeSecondaryTab by rememberSaveable { mutableStateOf<SecondaryTab?>(null) }
    var isFabMenuExpanded by rememberSaveable { mutableStateOf(false) }

    // Build today's class list from all subjects' timetables
    val todayDayOfWeek = LocalDate.now().dayOfWeek.value
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
                TodayClass(subject = subject, startTime = start, endTime = end, duration = durationMin)
            }
    }.sortedBy { it.startTime }

    // Build tomorrow's class list
    val tomorrowDayOfWeek = LocalDate.now().plusDays(1).dayOfWeek.value
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
                TodayClass(subject = subject, startTime = start, endTime = end, duration = durationMin)
            }
    }.sortedBy { it.startTime }

    val listState = rememberLazyListState()

    val notificationsEnabled by rememberSaveable {
        mutableStateOf(isNotificationPermissionGranted(context))
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

    // ── Driftly homescreen architecture ────────────────────────────────────
    Scaffold(
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->

        Box(modifier = Modifier.fillMaxSize()) {

            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(15.dp),
                contentPadding = innerPadding,
            ) {

                // ── Driftly header ──────────────────────────────────────────
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 25.dp, end = 25.dp, top = 35.dp, bottom = 15.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.app_name),
                            style = MaterialTheme.typography.displaySmallEmphasized,
                            modifier = Modifier.alpha(0.95f)
                        )

                        Spacer(modifier = Modifier.weight(1f))

                        // Infinite rotation animation for the settings icon
                        val rotationTransition = rememberInfiniteTransition(label = "settings_rotation_transition")
                        val rotationAngle by rotationTransition.animateFloat(
                            initialValue = 0f,
                            targetValue = 360f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(durationMillis = 3000, easing = LinearEasing),
                                repeatMode = RepeatMode.Restart
                            ),
                            label = "settings_rotation_angle"
                        )

                        FilledTonalIconButton(
                            onClick = {
                                navController.navigate(SettingsScreen)
                                weakHaptic()
                            },
                            shapes = IconButtonDefaults.shapes(),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_settings),
                                contentDescription = "Settings",
                                modifier = Modifier
                                    .size(24.dp)
                                    .graphicsLayer(rotationZ = rotationAngle)
                            )
                        }
                    }
                }

                // ── Empty state ─────────────────────────────────────────────
                if (subjectCount == 0 || (subjectCount > 0 && totalCount == 0)) {
                    item {
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .animateItem()
                                .padding(horizontal = 25.dp)
                                .then(
                                    if (subjectCount == 0) Modifier.height(400.dp)
                                    else Modifier.padding(vertical = 20.dp)
                                ),
                            contentAlignment = Alignment.Center
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
                                .animateItem()
                                .padding(horizontal = 25.dp)
                                .alpha(0.75f),
                            text = stringResource(R.string.no_subject_yet),
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center
                        )
                    }
                }


                // ── Subject cards (Driftly card dimensions + SA data) ───────
                items(subjects.size, key = { index -> subjects[index].id }) { index ->

                    val counts by viewModel.getSubjectAttendanceCounts(subjects[index].id)
                        .collectAsState(initial = SubjectAttendance())

                    val progress = counts.presentCount.toFloat() / counts.totalCount.toFloat()
                    val cardShape = RoundedCornerShape(subjectCardCornerRadius.dp)

                    SubjectCard(
                        modifier = Modifier
                            .padding(horizontal = 15.dp)
                            .animateItem(),
                        cardStyle = LocalSettings.current.subjectCardStyle,
                        cornerRadius = subjectCardCornerRadius.dp,
                        customShape = cardShape,
                        subjectId = subjects[index].id,
                        subject = subjects[index].subject,
                        subjectCode = subjects[index].subjectCode,
                        lecturerName = subjects[index].histogramLabel,
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
                            .height(25.dp)
                    )
                }
            }

            // ── FAB backdrop overlay ────────────────────────────────────────
            AnimatedVisibility(
                visible = isFabMenuExpanded && selectedCardsCount == 0,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(adaptiveStrongScrimColor())
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            isFabMenuExpanded = false
                        }
                )
            }

            // ── M3 Expressive FAB Menu — preserved exactly ──────────────────
            AnimatedVisibility(
                visible = selectedCardsCount == 0,
                enter = fadeIn(animationSpec = tween(250)) +
                        slideInVertically(initialOffsetY = { it / 2 }, animationSpec = tween(250)),
                exit = fadeOut(animationSpec = tween(250)) +
                        slideOutVertically(targetOffsetY = { it / 2 }, animationSpec = tween(250)),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = 43.dp, end = 14.dp)
            ) {
                FloatingActionButtonMenu(
                    expanded = isFabMenuExpanded,
                    button = {
                        DriftlyMorphingFab(
                            isExpanded = isFabMenuExpanded,
                            isVisible = true,
                            onToggle = {
                                isFabMenuExpanded = !isFabMenuExpanded
                                weakHaptic()
                            }
                        )
                    }
                ) {
                    FloatingActionButtonMenuItem(
                        onClick = {
                            isFabMenuExpanded = false
                            isDialogOpen = true
                            weakHaptic()
                        },
                        icon = { Icon(Icons.Rounded.Add, contentDescription = null) },
                        text = { Text(text = "Add Subject") },
                        modifier = Modifier.height(67.dp)
                    )

                    FloatingActionButtonMenuItem(
                        onClick = {
                            isFabMenuExpanded = false
                            activeSecondaryTab = SecondaryTab.Timetable
                            weakHaptic()
                        },
                        icon = { Icon(Icons.Rounded.DateRange, contentDescription = null) },
                        text = { Text(text = "Timetable") },
                        modifier = Modifier.height(67.dp)
                    )

                    FloatingActionButtonMenuItem(
                        onClick = {
                            isFabMenuExpanded = false
                            activeSecondaryTab = SecondaryTab.Today
                            weakHaptic()
                        },
                        icon = { Icon(Icons.Rounded.AccessTime, contentDescription = null) },
                        text = { Text(text = "Today's Class") },
                        modifier = Modifier.height(67.dp)
                    )

                    FloatingActionButtonMenuItem(
                        onClick = {
                            isFabMenuExpanded = false
                            activeSecondaryTab = SecondaryTab.Tomorrow
                            weakHaptic()
                        },
                        icon = { Icon(Icons.Rounded.CalendarMonth, contentDescription = null) },
                        text = { Text(text = "Tomorrow's Class") },
                        modifier = Modifier.height(67.dp)
                    )

                    FloatingActionButtonMenuItem(
                        onClick = {
                            isFabMenuExpanded = false
                            activeSecondaryTab = SecondaryTab.Bunk
                            weakHaptic()
                        },
                        icon = { Icon(Icons.Rounded.Analytics, contentDescription = null) },
                        text = { Text(text = "Bunk Details") },
                        modifier = Modifier.height(67.dp)
                    )
                }
            }
        }
    }

    // ── Dialogs & bottom sheets ─────────────────────────────────────────────
    if (isDialogOpen) {
        AddSubjectDialog(onDismiss = { isDialogOpen = false })
    }

    activeSecondaryTab?.let { tab ->
        SecondaryPagesDialog(
            initialTab = tab,
            todaysClasses = todaysClasses,
            tomorrowsClasses = tomorrowsClasses,
            onDismiss = { activeSecondaryTab = null }
        )
    }

    if (showNotificationPermissionDialog) {
        NotificationPermDialog(
            onDismiss = {
                showNotificationPermissionDialog = false
                settingsViewModel.setBoolean(SettingsKeys.NOTIFICATION_PERMISSION_DIALOG_SHOWN, true)
            },
            onConfirm = {
                viewModel.requestNotificationPermission()
                showNotificationPermissionDialog = false
                settingsViewModel.setBoolean(SettingsKeys.NOTIFICATION_PERMISSION_DIALOG_SHOWN, true)
            }
        )
    }
}

// ── Driftly Morphing FAB — preserved exactly ───────────────────────────────
@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun DriftlyMorphingFab(
    isExpanded: Boolean,
    isVisible: Boolean,
    onToggle: () -> Unit
) {
    var renderGate by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { renderGate = true }

    if (!renderGate) {
        Spacer(modifier = Modifier.size(80.dp))
        return
    }

    val transition = updateTransition(targetState = isExpanded, label = "fab_transition")

    val primary = MaterialTheme.colorScheme.primary
    val primaryContainer = MaterialTheme.colorScheme.primaryContainer
    val onPrimary = MaterialTheme.colorScheme.onPrimary
    val onPrimaryContainer = MaterialTheme.colorScheme.onPrimaryContainer

    val fabColor = remember(isExpanded, primary, primaryContainer) {
        if (isExpanded) primary else primaryContainer
    }
    val contentColor = remember(isExpanded, onPrimary, onPrimaryContainer) {
        if (isExpanded) onPrimary else onPrimaryContainer
    }

    val fabWidth by transition.animateDp(
        label = "fab_width",
        transitionSpec = { spring(dampingRatio = 0.7f, stiffness = 500f) }
    ) { if (it) 75.dp else 170.dp }

    val cornerRadius by transition.animateDp(
        label = "corner_radius",
        transitionSpec = { spring(dampingRatio = 0.7f, stiffness = 500f) }
    ) { if (it) 37.5.dp else 16.dp }

    Surface(
        modifier = Modifier.size(width = fabWidth, height = 75.dp),
        shape = RoundedCornerShape(cornerRadius),
        color = fabColor,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        onClick = onToggle
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut()
            ) {
                Icon(imageVector = Icons.Rounded.Close, contentDescription = null, tint = contentColor)
            }

            AnimatedVisibility(
                visible = !isExpanded,
                enter = fadeIn() + expandHorizontally(),
                exit = fadeOut() + shrinkHorizontally()
            ) {
                AutoResizeableText(
                    text = "+ Add Class",
                    color = contentColor,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
            }
        }
    }
}
