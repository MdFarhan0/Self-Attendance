# 🎬 Adding Button Animations - Implementation Plan

## Buttons to Update:

### ✅ 1. Home Screen - Add Subject Button (Line 385-410)
**File:** `HomeScreen.kt`
**Current:** Regular Button
**Change:** Add `.animateWidth()` with MutableInteractionSource

### ✅ 2. Side Drawer - Settings Button
**File:** `SmartAttendanceDrawer.kt` (Line 101-135)
**Current:** Regular Card with onClick
**Change:** Convert to Button with `.animateWidth()`

### ✅ 3. Add Subject Dialog - Add Timetable Button
**File:** `AddSubjectDialog.kt`
**Current:** FilledTonalButton
**Change:** Add animateWidth to existing button

### ✅ 4. Weekly Timetable Dialog - Add Class + Save Buttons
**File:** `TimetableEntryDialog.kt`
**Current:** ExtendedFloatingActionButton + Button
**Change:** Add `.animateWidth()` to both

### ✅ 5. Add Class Bottom Sheet - Add to Timetable Button
**File:** `TimetableInputBottomSheet.kt`
**Current:** FilledTonalButton
**Change:** Add `.animateWidth()`

### ✅ 6. Calendar Screen Bottom Sheets
**Files:** Check what bottom sheets exist in CalendarScreen

---

## Implementation Steps:

For each button:
1. Add `@OptIn(ExperimentalMaterial3ExpressiveApi::class)` at file level
2. Create `val interactionSource = remember { MutableInteractionSource() }`
3. Add `.animateWidth(interactionSource)` to modifier
4. Add `shapes = ButtonDefaults.shapes()` parameter
5. Add `interactionSource = interactionSource` parameter

---

## Code Template:

```kotlin
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun YourComponent() {
    val interactionSource = remember { MutableInteractionSource() }
    
    Button(
        modifier = Modifier.animateWidth(interactionSource),
        shapes = ButtonDefaults.shapes(),
        interactionSource = interactionSource,
        onClick = { /* ... */ }
    ) {
        Text("Button")
    }
}
```

For multiple buttons in same composable:
```kotlin
val interactionSources = remember { List(2) { MutableInteractionSource() } }

ButtonGroup {
    Button(
        modifier = Modifier.animateWidth(interactionSources[0]),
        shapes = ButtonDefaults.shapes(),
        interactionSource = interactionSources[0],
        ...
    )
    Button(
        modifier = Modifier.animateWidth(interactionSources[1]),
        shapes = ButtonDefaults.shapes(),
        interactionSource = interactionSources[1],
        ...
    )
}
```
