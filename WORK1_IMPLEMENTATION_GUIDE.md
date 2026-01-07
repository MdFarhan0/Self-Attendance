# Work 1: Timetable & Notifications - Implementation Summary

## ✅ COMPLETED (35% Done)

### 1. Database Layer (100%)
- ✅ `ClassScheduleEntity.kt` - Database table entity
- ✅ `ClassScheduleDao.kt` - Database access operations
- ✅ `ClassScheduleRepository.kt` (interface) - Domain contract
- ✅ `ClassScheduleRepositoryImpl.kt` - Repository implementation
- ✅ `SubjectDatabase.kt` - Updated to v8 with new entity
- ✅ `Migrations.kt` - MIGRATION_7_8 creates class_schedules table
- ✅ `DatabaseModule.kt` - DI for ClassScheduleDao
- ✅ `RepositoryModule.kt` - DI for ClassScheduleRepository

### 2. Domain Models (100%)
- ✅ `ClassSchedule.kt` - Domain model with utility methods
- ✅ `NotificationMode.kt` - Enum for STANDARD/PERSISTENT
- ✅ `TimeUtils.kt` - Time formatting, parsing, validation utilities

### 3. Settings/Preferences (100%)
- ✅ `SettingsKeys.kt` - Added 3 new keys:
  - `ENABLE_TIMETABLE_NOTIFICATIONS`
  - `NOTIFICATION_MODE` 
  - `GRACE_PERIOD_MINUTES`

---

## 🔨 IN PROGRESS - UI Components (0%)

### Phase 2A: Add Subject Screen
**Location:** `app/src/main/java/in/hridayan/driftly/home/presentation/`

#### Files to Create/Modify:
1. **TimetableEntryDialog.kt** - Popup for adding weekly schedule
   - Shows Monday-Sunday list
   - Each day has "+ Add Class" button
   - Shows added classes with edit/delete options
   
2. **AddClassTimeDialog.kt** - Dialog for single class entry
   - Time picker for start/end (with AM/PM)
   - Location input (optional)
   - Duration auto-calculation
   
3. **TimePickerComponent.kt** - Reusable time picker
   - Hour selector (1-12)
   - Minute selector (00-59)
   - AM/PM toggle
   - Quick select chips (8AM, 9AM, etc.)

4. **AddSubjectScreen.kt** (MODIFY EXISTING)
   - Add timetable step after basic details
   - Show timetable summary before final creation
   
#### Components Needed:
```kotlin
@Composable
fun TimetableEntryDialog(
    onDismiss: () -> Unit,
    onSave: (List<ClassSchedule>) -> Unit
)

@Composable
fun AddClassTimeDialog(
    dayOfWeek: Int,
    onDismiss: () -> Unit,
    onSave: (ClassSchedule) -> Unit
)

@Composable
fun TimePickerField(
    label: String,
    time: String,
    onTimeChange: (String) -> Unit
)
```

---

### Phase 2B: Subject Details Screen
**Location:** `app/src/main/java/in/hridayan/driftly/calender/presentation/`

#### Files to Create:
1. **TimetableTab.kt** - New tab showing weekly schedule
   - Empty state: "No timetable set" with "+ Add Timetable" button
   - Filled state: List of classes grouped by day
   - Each class shows: Day, Time range, Location, Edit/Delete buttons
   
2. **TimetableViewModel.kt** - State management
   - Load schedules for subject
   - Add/Update/Delete operations
   - Validation logic

#### Modify Existing:
- **CalendarScreen.kt** - Add "Timetable" to tab row
  ```kotlin
  Tab(
      selected = selectedTab == 2,
      onClick = { selectedTab = 2 },
      text = { Text("Timetable") }
  )
  ```

---

### Phase 2C: Home Screen Cards
**Location:** `app/src/main/java/in/hridayan/driftly/home/presentation/components/`

#### Modify:
1. **SubjectCard.kt** - Add "Next class" display
   - Show next scheduled class if timetable exists
   - Format: "Next: Mon 8:45 AM"  
   - Show "+ Add Timetable" link if no schedule
   
2. **HomeViewModel.kt** - Load next class info
   - Get current day/time
   - Find next scheduled class across all subjects
   - Calculate "Next class" display

---

### Phase 2D: Settings - Notification Mode
**Location:** `app/src/main/java/in/hridayan/driftly/settings/presentation/`

#### Files to Create:
1. **NotificationModeSelector.kt** - Card-based radio selection
   ```
   ┌─────────────────────────┐
   │ Standard Notifications  │
   │ ○ Display only         │
   └─────────────────────────┘
   
   ┌─────────────────────────┐
   │ Persistent Notifications│
   │ ● Auto-mark enabled    │
   │ Grace: [1 hour ▼]      │
   └─────────────────────────┘
   ```

2. **NotificationModeInfoDialog.kt** - Explanation popups
   - Shows how Standard mode works (tap ⓘ icon)
   - Shows how Persistent mode works
   
3. **GracePeriodSelector.kt** - Chip selector
   - Options: 30min, 1hr, 2hr, 3hr
   - Only visible when Persistent mode selected

#### Modify:
- **NotificationSettingsScreen.kt** - Add new section
  ```kotlin
  // Add after existing notification settings
  NotificationModeSelector(
      currentMode = viewModel.notificationMode,
      gracePeriod = viewModel.gracePeriodMinutes,
      onModeChange = { viewModel.setNotificationMode(it) },
      onGracePeriodChange = { viewModel.setGracePeriod(it) }
  )
  ```

---

## 🔔 TO DO - Notification System (0%)

### Phase 3: Notification Implementation
**Location:** `app/src/main/java/in/hridayan/driftly/notification/`

#### Files to Create:
1. **ClassNotificationScheduler.kt** - WorkManager setup
   - Schedule notifications for all timetabled classes
   - Cancel/reschedule on timetable changes
   - Handle timezone changes
   
2. **ClassNotificationWorker.kt** - Worker that runs at class start
   - Check notification mode from settings
   - If STANDARD: Show simple dismissible notification
   - If PERSISTENT: Show non-dismissible with action buttons
   - Schedule grace period worker

3. **GracePeriodWorker.kt** - Runs after class + grace period
   - Check if attendance already marked
   - If not: Auto-mark as MISSED
   - Send confirmation notification with Undo option

4. **AttendanceActionReceiver.kt** - Handle button clicks
   - ACTION_ATTENDED → Mark present
   - ACTION_MISSED → Mark absent
   - ACTION_CANCELLED → Mark cancelled
   - Dismiss notification after action

5. **NotificationBuilder.kt** - Notification creation
   ```kotlin
   fun buildStandardNotification(subject: String, time: String): Notification
   fun buildPersistentNotification(subject: String, time: String): Notification
   fun buildGracePeriodNotification(subject: String, deadline: String): Notification
   fun buildConfirmationNotification(subject: String, status: String): Notification
   ```

---

## 📋 Integration Checklist

### Home Screen Integration
- [ ] Inject `ClassScheduleRepository` into `HomeViewModel`
- [ ] Load next class for each subject
- [ ] Display on subject cards
- [ ] Handle click on "+ Add Timetable"

### Subject Creation Flow
- [ ] Add timetable step to multi-step dialog
- [ ] Save schedules when subject created
- [ ] Schedule notifications if enabled in settings

### Calendar Screen
- [ ] Show scheduled classes on calendar dates
- [ ] Mark future dates with schedule indicator
- [ ] Show time when date selected

### Settings Screen  
- [ ] Add notification mode section
- [ ] Save mode to DataStore
- [ ] Reschedule notifications when mode changes

### Notification System
- [ ] Request notification permission
- [ ] Schedule on app start
- [ ] Reschedule on device reboot
- [ ] Cancel when subject deleted

---

## 🎯 Implementation Order (Recommended)

### Week 1: UI Foundation
1. Time picker components
2. Add class dialog
3. Timetable entry dialog
4. Subject creation flow

### Week 2: Display & Settings
5. Timetable tab in subject details
6. Home screen "next class" display
7. Settings notification mode selector
8. Info dialogs

### Week 3: Notifications
9. WorkManager scheduler
10. Notification builders
11. Action receivers
12. Grace period logic

### Week 4: Polish & Testing
13. Edge cases (overlapping classes, holidays)
14. Performance optimization
15. UI polish
16. End-to-end testing

---

## 🗂️ File Structure (Complete)

```
app/src/main/java/in/hridayan/driftly/
├── core/
│   ├── data/
│   │   ├── database/
│   │   │   ├── ClassScheduleDao.kt ✅
│   │   │   ├── SubjectDatabase.kt ✅
│   │   │   └── Migrations.kt ✅
│   │   ├── model/
│   │   │   └── ClassScheduleEntity.kt ✅
│   │   └── repository/
│   │       └── ClassScheduleRepositoryImpl.kt ✅
│   ├── domain/
│   │   ├── model/
│   │   │   ├── ClassSchedule.kt ✅
│   │   │   └── NotificationMode.kt ✅
│   │   └── repository/
│   │       └── ClassScheduleRepository.kt ✅
│   ├── utils/
│   │   └── TimeUtils.kt ✅
│   └── di/
│       ├── DatabaseModule.kt ✅
│       └── RepositoryModule.kt ✅
├── home/
│   └── presentation/
│       ├── components/
│       │   ├── SubjectCard.kt (MODIFY) ⬜
│       │   ├── TimetableEntryDialog.kt ⬜
│       │   ├── AddClassTimeDialog.kt ⬜
│       │   └── TimePickerComponent.kt ⬜
│       └── viewmodel/
│           └── HomeViewModel.kt (MODIFY) ⬜
├── calender/
│   └── presentation/
│       ├── screens/
│       │   ├── CalendarScreen.kt (MODIFY) ⬜
│       │   └── TimetableTab.kt ⬜
│       └── viewmodel/
│           └── TimetableViewModel.kt ⬜
├── settings/
│   ├── data/local/
│   │   └── SettingsKeys.kt ✅
│   └── presentation/
│       ├── components/
│       │   ├── NotificationModeSelector.kt ⬜
│       │   ├── NotificationModeInfoDialog.kt ⬜
│       │   └── GracePeriodSelector.kt ⬜
│       └── screens/
│           └── NotificationSettingsScreen.kt (MODIFY) ⬜
└── notification/
    ├── ClassNotificationScheduler.kt ⬜
    ├── ClassNotificationWorker.kt ⬜
    ├── GracePeriodWorker.kt ⬜
    ├── AttendanceActionReceiver.kt ⬜
    └── NotificationBuilder.kt ⬜
```

**Legend:**
- ✅ Completed
- ⬜ To Do
- (MODIFY) Modify existing file

---

## 🚀 Next Steps

**Ready to continue with Phase 2A: UI Components!**

Should I start building:
1. Time Picker Component
2. Add Class Dialog
3. Timetable Entry Dialog
4. Subject Creation Integration

Which would you like me to tackle first?
