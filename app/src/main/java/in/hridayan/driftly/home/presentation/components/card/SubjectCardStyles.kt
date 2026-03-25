package `in`.hridayan.driftly.home.presentation.components.card

import android.annotation.SuppressLint
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import `in`.hridayan.driftly.core.presentation.components.canvas.VerticalProgressWave
import `in`.hridayan.driftly.core.presentation.components.progress.CircularProgressWithText
import `in`.hridayan.driftly.home.presentation.components.text.SubjectText

@Composable
fun CardStyleA(
    modifier: Modifier = Modifier,
    subject: String,
    subjectCode: String? = null,
    progress: Float,
    isLongClicked: Boolean,
    isTotalCountZero: Boolean,
    onEditButtonClicked: () -> Unit,
    onDeleteButtonClicked: () -> Unit,
    onErrorIconClicked: () -> Unit,
    onMoveUp: () -> Unit = {},
    onMoveDown: () -> Unit = {},
    onMoveTop: () -> Unit = {},
    onMoveBottom: () -> Unit = {},
) {
    var expanded by remember { mutableStateOf(false) }
    val subjectTextColor =
        if (isLongClicked) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurfaceVariant

    val backgroundColor =
        if (isLongClicked) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceContainer

    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 85.dp)
            .background(backgroundColor)
            .padding(vertical = 16.dp, horizontal = 20.dp)
            .animateContentSize(
                animationSpec = tween(
                    durationMillis = 500, easing = FastOutSlowInEasing
                )
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(15.dp)
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.Center
        ) {
            SubjectText(
                subject = subject,
                subjectCode = subjectCode,
                subjectTextColor = subjectTextColor
            )
        }

        if (isLongClicked) {
            UtilityRow(
                onEditButtonClicked = onEditButtonClicked,
                onDeleteButtonClicked = onDeleteButtonClicked
            )
        } else {
            if (isTotalCountZero) ErrorIcon(onClick = onErrorIconClicked)
            else {
                Box {
                        IconButton(onClick = { expanded = true }, modifier = Modifier.size(24.dp)) {
                            Icon(
                                imageVector = Icons.Rounded.MoreVert,
                                contentDescription = "Options",
                                tint = subjectTextColor.copy(alpha = 0.6f)
                            )
                        }
                    SubjectOptionsMenu(
                        expanded = expanded,
                        onDismiss = { expanded = false },
                        onMoveUp = onMoveUp,
                        onMoveDown = onMoveDown,
                        onMoveTop = onMoveTop,
                        onMoveBottom = onMoveBottom
                    )
                }
            }
        }
    }
}


@SuppressLint("DefaultLocale")
@Composable
fun CardStyleB(
    modifier: Modifier = Modifier,
    progress: Float,
    subject: String,
    subjectCode: String? = null,
    isLongClicked: Boolean,
    isTotalCountZero: Boolean,
    onEditButtonClicked: () -> Unit,
    onDeleteButtonClicked: () -> Unit,
    onErrorIconClicked: () -> Unit,
    onMoveUp: () -> Unit = {},
    onMoveDown: () -> Unit = {},
    onMoveTop: () -> Unit = {},
    onMoveBottom: () -> Unit = {},
) {
    val progressText = "${String.format("%.2f", progress * 100)}%"
    var expanded by remember { mutableStateOf(false) }

    var contentHeightPx by remember { mutableIntStateOf(0) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .background(MaterialTheme.colorScheme.surfaceContainer)
    ) {

        VerticalProgressWave(
            modifier = Modifier.height(with(LocalDensity.current) { contentHeightPx.toDp() }),
            progress = progress,
            waveSpeed = 4000
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .onSizeChanged { contentHeightPx = it.height },
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 85.dp)
                    .padding(vertical = 16.dp, horizontal = 20.dp)
                    .animateContentSize(
                        animationSpec = tween(
                            durationMillis = 500, easing = FastOutSlowInEasing
                        )
                    ),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(15.dp)
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.Center
                ) {
                    SubjectText(
                        subject = subject,
                        subjectCode = subjectCode
                    )
                }

                if (isLongClicked) {
                    UtilityRow(
                        onEditButtonClicked = onEditButtonClicked,
                        onDeleteButtonClicked = onDeleteButtonClicked
                    )
                } else {
                    if (isTotalCountZero) ErrorIcon(onClick = onErrorIconClicked)
                    else {
                        Box {
                            IconButton(onClick = { expanded = true }, modifier = Modifier.size(24.dp)) {
                                Icon(
                                    imageVector = Icons.Rounded.MoreVert,
                                    contentDescription = "Options",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                )
                            }
                            SubjectOptionsMenu(
                                expanded = expanded,
                                onDismiss = { expanded = false },
                                onMoveUp = onMoveUp,
                                onMoveDown = onMoveDown,
                                onMoveTop = onMoveTop,
                                onMoveBottom = onMoveBottom
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BaseCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp,
    customShape: androidx.compose.foundation.shape.RoundedCornerShape? = null,
    onClick: () -> Unit = {},
    onLongClick: () -> Unit,
    content: @Composable () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "card_press_animation"
    )
    
    // Reset pressed state after animation
    LaunchedEffect(isPressed) {
        if (isPressed) {
            kotlinx.coroutines.delay(150)
            isPressed = false
        }
    }
    
    val cardShape = customShape ?: RoundedCornerShape(20.dp)
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .widthIn(min = 380.dp)
            .scale(scale)
            .clip(cardShape)
            .combinedClickable(
                enabled = true, 
                onClick = {
                    isPressed = true
                    onClick()
                }, 
                onLongClick = onLongClick
            ),
        shape = cardShape,
    ) {
        content()
    }
}

@Composable
fun SubjectOptionsMenu(
    modifier: Modifier = Modifier,
    expanded: Boolean,
    onDismiss: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onMoveTop: () -> Unit,
    onMoveBottom: () -> Unit
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss,
        modifier = modifier
    ) {
        DropdownMenuItem(
            text = { Text("Move Up") },
            onClick = {
                onMoveUp()
                onDismiss()
            }
        )
        DropdownMenuItem(
            text = { Text("Move Down") },
            onClick = {
                onMoveDown()
                onDismiss()
            }
        )
        DropdownMenuItem(
            text = { Text("Move to Top") },
            onClick = {
                onMoveTop()
                onDismiss()
            }
        )
        DropdownMenuItem(
            text = { Text("Move to Bottom") },
            onClick = {
                onMoveBottom()
                onDismiss()
            }
        )
    }
}

