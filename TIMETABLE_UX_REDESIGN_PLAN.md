# 🎯 TIMETABLE UX REDESIGN - COMPLETE IMPLEMENTATION PLAN

## ✅ COMPLETED (Just Now):

### 1. Redesigned TimetableEntryDialog ✅
**File:** `TimetableEntryDialog.kt` (OVERWRITTEN)

**Changes Made:**
- ✅ Single screen with day selector (tabs for Mon-Sun)
- ✅ Keyboard time input (no time picker popup)
- ✅ Format: "9:00 AM" style input
- ✅ Shows current day's classes
- ✅ "Add First Class" button increased height (52dp = +4mm)
- ✅ Duration auto-calculates
- ✅ Location field optional
- ✅ No nested popups!

---

## 📋 REMAINING TASKS:

### 2. Show Timing on Subject Cards (HOME SCREEN)
**File to Modify:** `SubjectCardStyles.kt`

**What to Add:**
```kotlin
// In CardStyleA and CardStyleB, after subject code:

val schedules by homeViewModel
    .getSchedulesForSubject(subjectId)
    .collectAsState(initial = emptyList())

if (schedules.isNotEmpty()) {
    val nextClass = ScheduleUtils.getNextScheduledClass(schedules)
    if (nextClass != null) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                Icons.Default.Schedule,
                modifier = Modifier.size(12.dp),
               tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = ScheduleUtils.getNextClassDisplayText(schedules) ?: "",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
```

---

### 3. Enable Subject Editing
**File to Create:** `EditSubjectDialog.kt`

**Features Needed:**
- Edit subject name
- Edit subject code
- Edit histogram label
- Edit timetable (opens TimetableEntryDialog)
- Save/Cancel buttons

**File to Modify:** `HomeScreen.kt` or `SubjectCard.kt`
- Add long-press or menu to open EditSubjectDialog

---

### 4. Grouped Timetable Cards in Calendar Screen
**File to Modify:** `CalendarScreen.kt`

**Add below Smart Bunk Card:**

```kotlin
// Get schedules for current subject
val schedules by viewModel.getSchedulesForSubject(subjectId)
    .collectAsState(initial = emptyList())

if (schedules.isNotEmpty()) {
    GroupedTimetableCards(schedules = schedules)
}
```

**File to Create:** `GroupedTimetableCards.kt`

**Special Corner Rounding Logic:**
```kotlin
@Composable
fun GroupedTimetableCards(schedules: List<ClassSchedule>) {
    val grouped = schedules.groupBy { it.dayOfWeek to it.startTime }
        .values
        .sortedBy { it.first().startTime }
    
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Text("Weekly Schedule", style = MaterialTheme.typography.titleMedium)
        
        Spacer(Modifier.height(8.dp))
        
        grouped.forEachIndexed { index, group ->
            val isFirst = index == 0
            val isLast = index == grouped.size - 1
            val isOnly = grouped.size == 1
            
            val shape = when {
                isOnly -> RoundedCornerShape(25.dp)
                isFirst -> RoundedCornerShape(
                    topStart = 25.dp,
                    topEnd = 25.dp,
                    bottomStart = 2.dp,
                    bottomEnd = 2.dp
                )
                isLast -> RoundedCornerShape(
                    topStart = 2.dp,
                    topEnd = 2.dp,
                    bottomStart = 25.dp,
                    bottomEnd = 25.dp
                )
                else -> RoundedCornerShape(2.dp)
            }
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = shape,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                // Card content
                Column(Modifier.padding(16.dp)) {
                    group.forEach { schedule ->
                        Text("${schedule.getDayName()}: ${TimeUtils.format24To12Hour(schedule.startTime)} - ${TimeUtils.format24To12Hour(schedule.endTime)}")
                    }
                }
            }
            
            if (!isLast) {
                Spacer(Modifier.height(1.dp)) // 1mm gap
            }
        }
    }
}
```

---

### 5. Add Persistent Notification Toggle
**File to Modify:** `NotificationSettingsScreen.kt`

**What to Add:**
```kotlin
// Add this switch
Card {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text("Persistent Notifications")
            Text(
                "Keep notifications until you take action",
                style = MaterialTheme.typography.bodySmall
            )
        }
        Switch(
            checked = persistentNotificationsEnabled,
            onCheckedChange = { viewModel.setPersistentNotifications(it) }
        )
    }
}
```

**ViewModel method needed:**
```kotlin
// In NotificationSettingsViewModel
val persistentNotificationsEnabled = settingsDataStore
    .booleanFlow(SettingsKeys.PERSISTENT_NOTIFICATIONS)
    .stateIn(viewModelScope, SharingStarted.Eagerly, false)

fun setPersistentNotifications(enabled: Boolean) {
    viewModelScope.launch {
        settingsDataStore.setBoolean(SettingsKeys.PERSISTENT_NOTIFICATIONS, enabled)
    }
}
```

add key to `SettingsKeys.kt`:
```kotlin
PERSISTENT_NOTIFICATIONS(false),
```

---

## 🎯 QUICK SUMMARY

| Task | Status | File | Complexity |
|------|--------|------|------------|
| 1. Redesigned Timetable Dialog | ✅ DONE | TimetableEntryDialog.kt | High |
| 2. Show timing on cards | ⏳ TODO | SubjectCardStyles.kt | Low |
| 3. Edit subject | ⏳ TODO | EditSubjectDialog.kt (new) | Medium |
| 4. Grouped timetable cards | ⏳ TODO | GroupedTimetableCards.kt (new) | High |
| 5. Persistent notification toggle | ⏳ TODO | NotificationSettingsScreen.kt | Low |

---

## 🚀 NEXT STEPS

**Option A: I continue implementing (4-5 more turns)**
- I'll create each file one by one
- Test after each change

**Option B: You review the redesigned dialog first**
- Build and test the new TimetableEntryDialog
- Give feedback
- Then I continue with rest

**Option C: I provide all code templates**
- You copy-paste manually
- Faster but needs your manual work

---

## 💡 KEY DESIGN DECISIONS

1. **Time Input Format:** Users type "9:00 AM" - the code converts to 24-hour format internally
2. **Day Selector:** Tabs (like browser tabs) for quick day switching  
3. **No Nested Popups:** Everything happens in the main timetable dialog
4. **Grouped Cards:** Smart rounding based on position in group
5. **Edit vs Create:** Separate dialog for editing existing subjects

---

**The redesigned TimetableEntryDialog is ready! Should I continue with the remaining 4 tasks?** 🚀
