@file:OptIn(ExperimentalMaterial3Api::class)

package `in`.hridayan.driftly.settings.presentation.page.changelog.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import `in`.hridayan.driftly.R
import `in`.hridayan.driftly.settings.presentation.components.scaffold.SettingsScaffold
import `in`.hridayan.driftly.settings.presentation.page.changelog.viewmodel.ChangelogMockData
import `in`.hridayan.driftly.core.presentation.components.card.adaptiveCardContainerColor

@Composable
fun ChangelogScreen(
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()

    SettingsScaffold(
        modifier = modifier,
        listState = listState,
        topBarTitle = stringResource(R.string.changelogs),
        content = { innerPadding, topBarScrollBehavior ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .nestedScroll(topBarScrollBehavior.nestedScrollConnection),
                state = listState,
                contentPadding = innerPadding
            ) {
                val changelogs = ChangelogMockData.changelogs

                items(changelogs.size) { index ->
                    val entry = changelogs[index]

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .padding(top = if (index == 0) 16.dp else 24.dp, bottom = 8.dp),
                        horizontalAlignment = Alignment.Start
                    ) {
                        // Version Chip (Timetable UI style)
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.padding(bottom = 16.dp)
                        ) {
                            Text(
                                text = "v${entry.version}",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
                            )
                        }

                        // Feature Cards (WelcomeBottomSheet style)
                        Column(
                            verticalArrangement = Arrangement.spacedBy(3.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            val features = entry.features
                            features.forEachIndexed { featureIndex, feature ->
                                val isFirst = featureIndex == 0
                                val isLast = featureIndex == features.lastIndex

                                val shape = when {
                                    isFirst && isLast -> RoundedCornerShape(15.dp)
                                    isFirst -> RoundedCornerShape(topStart = 15.dp, topEnd = 15.dp, bottomStart = 3.dp, bottomEnd = 3.dp)
                                    isLast -> RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp, bottomStart = 15.dp, bottomEnd = 15.dp)
                                    else -> RoundedCornerShape(3.dp)
                                }

                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = shape,
                                    color = adaptiveCardContainerColor(),
                                    contentColor = MaterialTheme.colorScheme.onSurface,
                                    tonalElevation = 0.dp
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(
                                            text = feature.title,
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = feature.description,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            lineHeight = 20.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    Spacer(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp)
                    )
                }
            }
        },
    )
}
