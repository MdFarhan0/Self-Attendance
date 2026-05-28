@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)

package `in`.hridayan.driftly.settings.presentation.page.features.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.FileOpen
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import `in`.hridayan.driftly.R
import `in`.hridayan.driftly.core.presentation.components.card.RoundedCornerCard
import `in`.hridayan.driftly.export.presentation.ExportReportBottomSheet
import `in`.hridayan.driftly.settings.domain.model.PreferenceGroup
import `in`.hridayan.driftly.settings.presentation.components.item.PreferenceItemView
import `in`.hridayan.driftly.settings.presentation.components.scaffold.SettingsScaffold
import `in`.hridayan.driftly.settings.presentation.components.shape.CardCornerShape
import `in`.hridayan.driftly.settings.presentation.components.shape.CardCornerShape.getRoundedShape
import `in`.hridayan.driftly.settings.presentation.viewmodel.SettingsViewModel
import `in`.hridayan.driftly.core.common.LocalWeakHaptic
import `in`.hridayan.driftly.settings.data.local.SettingsKeys
import androidx.compose.runtime.collectAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.draw.clip

@Composable
fun FeaturesScreen(
    modifier: Modifier = Modifier,
    settingsViewModel: SettingsViewModel = hiltViewModel(),
) {
    val settings  = settingsViewModel.featuresPageList
    val listState = rememberLazyListState()
    var showExportSheet by remember { mutableStateOf(false) }

    SettingsScaffold(
        modifier = modifier,
        listState = listState,
        topBarTitle = stringResource(R.string.features),
        content = { innerPadding, topBarScrollBehavior ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .nestedScroll(topBarScrollBehavior.nestedScrollConnection),
                state = listState,
                contentPadding = innerPadding
            ) {
                itemsIndexed(settings) { index, group ->
                    when (group) {
                        is PreferenceGroup.Category -> {
                            Text(
                                text = stringResource(group.categoryNameResId),
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .animateItem()
                                    .padding(
                                        start = 20.dp,
                                        end = 20.dp,
                                        top = 30.dp,
                                        bottom = 10.dp
                                    )
                            )
                            val visibleItems = group.items.filter { it.isLayoutVisible }
                            visibleItems.forEachIndexed { i, item ->
                                val shape = getRoundedShape(i, visibleItems.size)
                                PreferenceItemView(
                                    item = item,
                                    modifier = Modifier.animateItem(),
                                    roundedShape = shape
                                )
                                if (item.key == SettingsKeys.AUTO_HANDLE_UNMARKED_DAYS) {
                                    val isEnabled by settingsViewModel.getBoolean(SettingsKeys.AUTO_HANDLE_UNMARKED_DAYS)
                                        .collectAsState(initial = false)
                                    val selectedBehavior by settingsViewModel.getInt(SettingsKeys.AUTO_HANDLE_UNMARKED_DAYS_BEHAVIOR)
                                        .collectAsState(initial = 2)
                                    val weakHaptic = LocalWeakHaptic.current

                                    androidx.compose.animation.AnimatedVisibility(
                                        visible = isEnabled,
                                        enter = androidx.compose.animation.expandVertically() + androidx.compose.animation.fadeIn(),
                                        exit = androidx.compose.animation.shrinkVertically() + androidx.compose.animation.fadeOut(),
                                        modifier = Modifier.animateItem()
                                    ) {
                                        RoundedCornerCard(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(top = 8.dp),
                                            roundedCornerShape = CardCornerShape.SINGLE_CARD,
                                            paddingValues = androidx.compose.foundation.layout.PaddingValues(vertical = 1.5.dp, horizontal = 15.dp)
                                        ) {
                                            Column(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(horizontal = 20.dp, vertical = 16.dp),
                                                verticalArrangement = Arrangement.spacedBy(12.dp)
                                            ) {
                                                Text(
                                                    text = "Automation Behavior",
                                                    fontWeight = FontWeight.Bold,
                                                    style = MaterialTheme.typography.bodyLarge,
                                                    modifier = Modifier.alpha(0.95f)
                                                )
                                                SegmentedSelector(
                                                    options = listOf(
                                                        stringResource(R.string.attended),
                                                        stringResource(R.string.absent),
                                                        stringResource(R.string.do_nothing)
                                                    ),
                                                    selectedOptionIndex = when (selectedBehavior) {
                                                        0 -> 0
                                                        1 -> 1
                                                        else -> 2
                                                    },
                                                    onOptionSelected = { index ->
                                                        val newBehavior = when (index) {
                                                            0 -> 0
                                                            1 -> 1
                                                            else -> 2
                                                        }
                                                        settingsViewModel.setInt(SettingsKeys.AUTO_HANDLE_UNMARKED_DAYS_BEHAVIOR, newBehavior)
                                                        weakHaptic()
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        is PreferenceGroup.Items -> {
                            val visibleItems = group.items.filter { it.isLayoutVisible }
                            visibleItems.forEachIndexed { i, item ->
                                val shape = getRoundedShape(i, visibleItems.size)
                                PreferenceItemView(
                                    item = item,
                                    modifier = Modifier.animateItem(),
                                    roundedShape = shape
                                )
                            }
                        }

                        else -> {}
                    }
                }

                // ── Reports section ───────────────────────────────────────────
                item {
                    Text(
                        text = "Reports",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 30.dp, bottom = 10.dp)
                    )
                }

                item {
                    RoundedCornerCard(
                        modifier = Modifier.fillMaxWidth(),
                        roundedCornerShape = CardCornerShape.SINGLE_CARD,
                        onClick = { showExportSheet = true }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 17.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(15.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.FileOpen,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(7.dp)
                            ) {
                                Text(
                                    text = "Export Attendance Report",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodyLarge,
                                    modifier = Modifier.alpha(0.95f)
                                )
                                Text(
                                    text = "Generate PDF document for your subjects",
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.alpha(0.6f)
                                )
                            }
                        }
                    }
                }

                item {
                    Spacer(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(25.dp)
                    )
                }
            }
        }
    )

    if (showExportSheet) {
        ExportReportBottomSheet(onDismiss = { showExportSheet = false })
    }
}

@Composable
private fun SegmentedSelector(
    options: List<String>,
    selectedOptionIndex: Int,
    onOptionSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    androidx.compose.foundation.layout.Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        options.forEachIndexed { index, option ->
            val isSelected = index == selectedOptionIndex
            val backgroundColor by androidx.compose.animation.animateColorAsState(
                targetValue = if (isSelected) MaterialTheme.colorScheme.primary else androidx.compose.ui.graphics.Color.Transparent,
                label = "bgColor"
            )
            val contentColor by androidx.compose.animation.animateColorAsState(
                targetValue = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                label = "contentColor"
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(CircleShape)
                    .background(backgroundColor)
                    .clickable { onOptionSelected(index) }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = option,
                    color = contentColor,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
