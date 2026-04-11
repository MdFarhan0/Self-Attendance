# Driftly Animation PRD - Product Requirements Document

## Overview
This document details all animation implementations in the Driftly attendance tracking app, including the AddSubject dialog, Delete dialog, and SubjectCard expansion animations.

---

## 1. AddSubject Dialog Animation

### 1.1 Animation Type
**Button Width Expansion (Split Button Animation)**

### 1.2 Behavior Description
When user presses/hovers on either "Cancel" or "Add" button:
- The pressed button expands horizontally (increases width)
- The sibling button shrinks proportionally
- Both buttons remain within the same row
- Creates a "split" visual effect guiding user toward the active action

### 1.3 Source Code Location
`app/src/main/java/in/hridayan/driftly/home/presentation/components/dialog/AddSubjectDialog.kt`

### 1.4 Implementation Details

```kotlin
@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

// Required imports
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonGroup
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.OutlinedButton
import androidx.compose.runtime.remember
import androidx.compose.ui.hapticfeedback.HapticFeedbackType

@Composable
fun AddSubjectDialog(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel(),
    onDismiss: () -> Unit
) {
    // Create interaction sources for each button (REQUIRED for animateWidth)
    val interactionSources = remember { List(2) { MutableInteractionSource() } }

    Dialog(
        onDismissRequest = { onDismiss() }
    ) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .clip(Shape.cardCornerLarge)
                .background(MaterialTheme.colorScheme.surfaceContainer)
                .padding(25.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ... title and text field ...

            @Suppress("DEPRECATION")
            ButtonGroup(modifier = Modifier.fillMaxWidth()) {
                // Cancel Button - shrinks when Add is pressed
                OutlinedButton(
                    modifier = Modifier
                        .weight(1f)
                        .animateWidth(interactionSources[0]),  // KEY ANIMATION
                    shapes = ButtonDefaults.shapes(),
                    interactionSource = interactionSources[0],
                    onClick = withHaptic(HapticFeedbackType.Reject) {
                        viewModel.resetInputFields()
                        onDismiss()
                    },
                    content = { AutoResizeableText(text = stringResource(R.string.cancel)) }
                )

                // Add Button - expands when pressed
                Button(
                    modifier = Modifier
                        .weight(1f)
                        .animateWidth(interactionSources[1]),  // KEY ANIMATION
                    shapes = ButtonDefaults.shapes(),
                    interactionSource = interactionSources[1],
                    onClick = withHaptic(HapticFeedbackType.Confirm) {
                        viewModel.addSubject(
                            onSuccess = {
                                viewModel.resetInputFields()
                                onDismiss()
                            }
                        )
                    },
                    content = { AutoResizeableText(text = stringResource(R.string.add)) }
                )
            }
        }
    }
}
```

### 1.5 Required Dependencies
```kotlin
// In build.gradle.kts
implementation(libs.material3)  // Material3 for ButtonGroup and animateWidth

// Required opt-in at file level
@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)
```

### 1.6 Key Implementation Notes
| Aspect | Details |
|--------|---------|
| **Animation API** | `Modifier.animateWidth(interactionSource)` from Material3 Expressive |
| **Container** | `ButtonGroup` (deprecated but still functional) |
| **Interaction Source** | Required `MutableInteractionSource` per button |
| **Haptic Feedback** | `HapticFeedbackType.Confirm` for Add, `Reject` for Cancel |
| **Deprecation** | ButtonGroup is deprecated but no direct replacement exists yet |

---

## 2. Delete Subject Dialog Animation

### 2.1 Animation Type
**Button Width Expansion (Split Button Animation)** - Same pattern as AddSubject

### 2.2 Behavior Description
- Cancel button: Normal outlined style, shrinks when Confirm is pressed
- Confirm button: Error-themed (errorContainer background), expands when pressed
- Same split behavior as AddSubject dialog

### 2.3 Source Code Location
`app/src/main/java/in/hridayan/driftly/core/presentation/components/dialog/ConfirmDeleteDialog.kt`

### 2.4 Implementation Details

```kotlin
@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

@Composable
fun ConfirmDeleteDialog(
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    val interactionSources = remember { List(2) { MutableInteractionSource() } }

    Dialog(
        onDismissRequest = { onDismiss() },
        properties = DialogProperties(dismissOnClickOutside = true)
    ) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            tonalElevation = 8.dp,
            color = MaterialTheme.colorScheme.surfaceContainer
        ) {
            Column(
                modifier = modifier
                    .padding(24.dp)
                    .widthIn(min = 280.dp)
            ) {
                // Title: "Delete Subject"
                AutoResizeableText(
                    text = stringResource(R.string.delete_subject),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Message: "Are you sure you want to delete?"
                Text(
                    text = stringResource(R.string.delete_confirmation),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(24.dp))

                @Suppress("DEPRECATION")
                ButtonGroup(modifier = Modifier.fillMaxWidth()) {
                    // Cancel Button
                    OutlinedButton(
                        onClick = withHaptic(HapticFeedbackType.Reject) {
                            onDismiss()
                        },
                        shapes = ButtonDefaults.shapes(),
                        modifier = Modifier
                            .weight(1f)
                            .animateWidth(interactionSources[0]),
                        interactionSource = interactionSources[0],
                    ) {
                        AutoResizeableText(
                            text = stringResource(R.string.cancel),
                            style = MaterialTheme.typography.labelLarge
                        )
                    }

                    // Confirm Button with Error Colors
                    Button(
                        onClick = withHaptic(HapticFeedbackType.Confirm) {
                            onConfirm()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .animateWidth(interactionSources[1]),
                        interactionSource = interactionSources[1],
                        shapes = ButtonDefaults.shapes(),
                    ) {
                        AutoResizeableText(text = stringResource(R.string.confirm))
                    }
                }
            }
        }
    }
}
```

### 2.5 Visual Specifications
| Button | Normal State | Pressed State | Colors |
|--------|-------------|---------------|--------|
| Cancel | Outlined, equal width | Shrinks | `outline` border, `onSurface` text |
| Confirm | Filled, equal width | Expands | `errorContainer` bg, `onErrorContainer` text |

---

## 3. SubjectCard Expansion Animation

### 3.1 Animation Type
**Content Size Animation (Expand/Collapse)**

### 3.2 Behavior Description
When user long-presses a subject card:
- Card background color changes (surfaceContainer → secondaryContainer)
- Text color changes (onSurfaceVariant → onSecondaryContainer)
- Card content area expands horizontally
- Progress indicator (or error icon) gets replaced by action buttons (Edit/Delete)
- Animation uses FastOutSlowIn easing for natural feel

### 3.3 Source Code Location
`app/src/main/java/in/hridayan/driftly/home/presentation/components/card/SubjectCardStyles.kt`

### 3.4 Implementation Details - CardStyleA

```kotlin
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween

@Composable
fun CardStyleA(
    modifier: Modifier = Modifier,
    subject: String,
    progress: Float,
    isLongClicked: Boolean,  // KEY: Controls animation state
    isTotalCountZero: Boolean,
    onEditButtonClicked: () -> Unit,
    onDeleteButtonClicked: () -> Unit,
    onErrorIconClicked: () -> Unit,
) {
    // Dynamic colors based on long-press state
    val subjectTextColor = if (isLongClicked) 
        MaterialTheme.colorScheme.onSecondaryContainer 
    else 
        MaterialTheme.colorScheme.onSurfaceVariant

    val backgroundColor = if (isLongClicked) 
        MaterialTheme.colorScheme.secondaryContainer 
    else 
        MaterialTheme.colorScheme.surfaceContainer

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .padding(horizontal = 20.dp, vertical = 10.dp)
            .animateContentSize(  // KEY ANIMATION
                animationSpec = tween(
                    durationMillis = 500, 
                    easing = FastOutSlowInEasing
                )
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(15.dp)
    ) {
        SubjectText(
            modifier = Modifier.weight(1f), 
            subject = subject, 
            subjectTextColor = subjectTextColor
        )

        // Content switch based on isLongClicked
        if (isLongClicked) {
            UtilityRow(
                onEditButtonClicked = onEditButtonClicked,
                onDeleteButtonClicked = onDeleteButtonClicked
            )
        } else {
            if (isTotalCountZero) 
                ErrorIcon(onClick = onErrorIconClicked)
            else 
                CircularProgressWithText(progress = progress)
        }
    }
}
```

### 3.5 Implementation Details - CardStyleB

```kotlin
@SuppressLint("DefaultLocale")
@Composable
fun CardStyleB(
    modifier: Modifier = Modifier,
    progress: Float,
    subject: String,
    isLongClicked: Boolean,
    isTotalCountZero: Boolean,
    onEditButtonClicked: () -> Unit,
    onDeleteButtonClicked: () -> Unit,
    onErrorIconClicked: () -> Unit,
) {
    val progressText = "${String.format("%.0f", progress * 100)}%"
    var contentHeightPx by remember { mutableIntStateOf(0) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .background(MaterialTheme.colorScheme.surfaceContainer)
    ) {
        // Animated wave background
        VerticalProgressWave(
            modifier = Modifier.height(
                with(LocalDensity.current) { contentHeightPx.toDp() }
            ),
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
                    .padding(20.dp)
                    .animateContentSize(  // KEY ANIMATION
                        animationSpec = tween(
                            durationMillis = 500, 
                            easing = FastOutSlowInEasing
                        )
                    ),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(15.dp)
            ) {
                SubjectText(
                    modifier = Modifier.weight(1f), 
                    subject = subject
                )

                // Content switch
                if (isLongClicked) {
                    UtilityRow(
                        onEditButtonClicked = onEditButtonClicked,
                        onDeleteButtonClicked = onDeleteButtonClicked
                    )
                } else {
                    if (isTotalCountZero) 
                        ErrorIcon(onClick = onErrorIconClicked)
                    else 
                        Text(
                            text = progressText,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                }
            }
        }
    }
}
```

### 3.6 State Management (SubjectCard)

```kotlin
@Composable
fun SubjectCard(
    subjectId: Int,
    subject: String,
    progress: Float,
    isTotalCountZero: Boolean = false,
    selectedCardsCount: Int = 0,
    navigate: () -> Unit = {},
    onClick: () -> Unit = {},
    onLongClicked: (Boolean) -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel(),
) {
    var isLongClicked by rememberSaveable { mutableStateOf(false) }
    var isDeleteDialogVisible by rememberSaveable { mutableStateOf(false) }
    var isUpdateDialogVisible by rememberSaveable { mutableStateOf(false) }

    // Reset when selection mode is cleared globally
    if (selectedCardsCount == 0 && isLongClicked) isLongClicked = false

    val handleLongClick = withHaptic(HapticFeedbackType.LongPress) {
        isLongClicked = !isLongClicked
        onLongClicked(isLongClicked)
    }

    val handleClick = withHaptic {
        if (isLongClicked || selectedCardsCount != 0) {
            handleLongClick()
        } else {
            navigate()
        }
    }

    BaseCard(
        modifier = modifier,
        cornerRadius = cornerRadius,
        onClick = handleClick,
        onLongClick = handleLongClick,
    ) {
        when (cardStyle) {
            SubjectCardStyle.CARD_STYLE_A -> CardStyleA(...)
            SubjectCardStyle.CARD_STYLE_B -> CardStyleB(...)
        }
    }

    // Dialogs
    if (isDeleteDialogVisible) {
        ConfirmDeleteDialog(
            onDismiss = { isDeleteDialogVisible = false },
            onConfirm = handleDeleteConfirmation
        )
    }
    // ...
}
```

### 3.7 Animation Specifications

| Property | Value |
|----------|-------|
| **Duration** | 500ms |
| **Easing** | FastOutSlowInEasing |
| **Trigger** | `isLongClicked` state change |
| **Animated Property** | Content size (width/height) |

### 3.8 Color Transition Reference

| State | Background | Text | Container |
|-------|-----------|------|-----------|
| Normal | `surfaceContainer` | `onSurfaceVariant` | - |
| Long-Pressed | `secondaryContainer` | `onSecondaryContainer` | Selected |

---

## 4. Utility Components

### 4.1 UtilityRow (Edit/Delete Buttons)

```kotlin
@Composable
fun UtilityRow(
    modifier: Modifier = Modifier,
    onEditButtonClicked: () -> Unit = {},
    onDeleteButtonClicked: () -> Unit = {}
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onEditButtonClicked,
            shapes = IconButtonDefaults.shapes(),
            colors = IconButtonDefaults.iconButtonColors(
                contentColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Icon(
                imageVector = Icons.Rounded.Edit,
                contentDescription = null,
            )
        }

        IconButton(
            onClick = onDeleteButtonClicked,
            shapes = IconButtonDefaults.shapes(),
            colors = IconButtonDefaults.iconButtonColors(
                contentColor = MaterialTheme.colorScheme.error
            )
        ) {
            Icon(
                imageVector = Icons.Rounded.Delete,
                contentDescription = null,
            )
        }
    }
}
```

---

## 5. How to Implement in Your Next App

### 5.1 Prerequisites

Add to `build.gradle.kts`:
```kotlin
dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.material3)
    implementation(libs.androidx.compose.animation)
    implementation(libs.material.icons.extended)
}
```

### 5.2 Step-by-Step Implementation

#### Step 1: Button Split Animation
```kotlin
@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.animateWidth
import androidx.compose.foundation.interaction.MutableInteractionSource

@Composable
fun SplitButtonExample() {
    val interactionSources = remember { List(2) { MutableInteractionSource() } }
    
    Row(modifier = Modifier.fillMaxWidth()) {
        Button(
            modifier = Modifier
                .weight(1f)
                .animateWidth(interactionSources[0]),
            interactionSource = interactionSources[0],
            onClick = { /* action */ }
        ) { Text("Cancel") }
        
        Button(
            modifier = Modifier
                .weight(1f)
                .animateWidth(interactionSources[1]),
            interactionSource = interactionSources[1],
            onClick = { /* action */ }
        ) { Text("Confirm") }
    }
}
```

#### Step 2: Card Expansion Animation
```kotlin
@Composable
fun ExpandableCard() {
    var isExpanded by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { isExpanded = !isExpanded }
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .animateContentSize(
                    animationSpec = tween(
                        durationMillis = 500,
                        easing = FastOutSlowInEasing
                    )
                )
        ) {
            Text("Title", modifier = Modifier.weight(1f))
            
            if (isExpanded) {
                // Expanded content (buttons, etc.)
                Row {
                    IconButton(onClick = { }) { Icon(Icons.Default.Edit, null) }
                    IconButton(onClick = { }) { Icon(Icons.Default.Delete, null) }
                }
            } else {
                // Collapsed content (icon, progress, etc.)
                Icon(Icons.Default.ChevronRight, null)
            }
        }
    }
}
```

#### Step 3: Haptic Integration
```kotlin
// Create extension function
fun withHaptic(
    type: HapticFeedbackType = HapticFeedbackType.TextHandleMove,
    action: () -> Unit
): () -> Unit = {
    val haptic = LocalHapticFeedback.current
    haptic.performHapticFeedback(type)
    action()
}

// Usage
Button(
    onClick = withHaptic(HapticFeedbackType.Confirm) {
        // Your action
    }
) { Text("Confirm") }
```

### 5.3 File Structure for Your App
```
app/
├── presentation/
│   ├── components/
│   │   ├── dialog/
│   │   │   ├── AddItemDialog.kt      # Uses animateWidth
│   │   │   ├── ConfirmDeleteDialog.kt # Uses animateWidth with error colors
│   │   │   └── EditItemDialog.kt     # Uses animateWidth
│   │   └── card/
│   │       ├── ItemCard.kt           # Uses animateContentSize
│   │       └── ItemCardStyles.kt     # Style variants
│   └── screens/
│       └── HomeScreen.kt             # Integrates all components
└── ...
```

---

## 6. Complete Reusable Components

### 6.1 AnimatedButtonPair (Reusable)

```kotlin
@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

@Composable
fun AnimatedButtonPair(
    modifier: Modifier = Modifier,
    leftText: String,
    rightText: String,
    onLeftClick: () -> Unit,
    onRightClick: () -> Unit,
    rightIsDestructive: Boolean = false
) {
    val interactionSources = remember { List(2) { MutableInteractionSource() } }

    @Suppress("DEPRECATION")
    ButtonGroup(modifier = modifier.fillMaxWidth()) {
        OutlinedButton(
            onClick = withHaptic(HapticFeedbackType.Reject) { onLeftClick() },
            modifier = Modifier
                .weight(1f)
                .animateWidth(interactionSources[0]),
            interactionSource = interactionSources[0],
            shapes = ButtonDefaults.shapes()
        ) { Text(leftText) }

        Button(
            onClick = withHaptic(HapticFeedbackType.Confirm) { onRightClick() },
            modifier = Modifier
                .weight(1f)
                .animateWidth(interactionSources[1]),
            interactionSource = interactionSources[1],
            shapes = ButtonDefaults.shapes(),
            colors = if (rightIsDestructive) {
                ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                )
            } else ButtonDefaults.buttonColors()
        ) { Text(rightText) }
    }
}
```

### 6.2 ExpandableCard (Reusable)

```kotlin
@Composable
fun <T> ExpandableCard(
    modifier: Modifier = Modifier,
    item: T,
    isExpanded: Boolean,
    onExpandChange: (Boolean) -> Unit,
    collapsedContent: @Composable (T) -> Unit,
    expandedContent: @Composable (T) -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = { if (isExpanded) onExpandChange(false) },
                onLongClick = withHaptic(HapticFeedbackType.LongPress) {
                    onExpandChange(!isExpanded)
                }
            )
    ) {
        Box(
            modifier = Modifier
                .padding(16.dp)
                .animateContentSize(
                    animationSpec = tween(500, easing = FastOutSlowInEasing)
                )
        ) {
            if (isExpanded) expandedContent(item)
            else collapsedContent(item)
        }
    }
}
```

---

## 7. Summary Table

| Animation | Location | API | Duration | Easing |
|-----------|----------|-----|----------|--------|
| Button Split (Add/Cancel) | AddSubjectDialog.kt | `animateWidth()` | Default | Material default |
| Button Split (Delete) | ConfirmDeleteDialog.kt | `animateWidth()` | Default | Material default |
| Card Expand | SubjectCardStyles.kt | `animateContentSize()` | 500ms | FastOutSlowIn |
| FAB Rotate | HomeScreen.kt | `animateFloatAsState()` | 300ms | Linear |

---

## 8. Testing Checklist

- [ ] Buttons expand/shrink smoothly on press
- [ ] Haptic feedback works on all actions
- [ ] Card expands without jank (profile with Layout Inspector)
- [ ] Color transitions are visible in both light/dark themes
- [ ] Animation completes even if user releases quickly
- [ ] Multiple cards can be expanded simultaneously (if designed)
- [ ] State persists across configuration changes (use `rememberSaveable`)

---

*Document generated for Driftly v1.9.0*
