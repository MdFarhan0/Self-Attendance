# ✏️ TIMETABLE EDIT FEATURE - IMPLEMENTED

## WHAT WAS ADDED:

You can now **edit existing timetable entries** directly from the Weekly Timetable dialog!

## HOW IT WORKS:

### 1. **Edit Icon Added** ✏️
- Each timetable card now has TWO buttons:
  - **Edit** (blue pencil icon) - Modify the entry
  - **Delete** (red trash icon) - Remove the entry

### 2. **Pre-filled Values When Editing**
When you click the Edit button:
- The `TimetableInputBottomSheet` opens
- All fields are **pre-filled** with existing values:
  - ✅ Day of week
  - ✅ Start time (hour, minute, AM/PM)
  - ✅ End time (hour, minute, AM/PM)
  - ✅ Location

### 3. **Auto-fill Logic Disabled in Edit Mode**
- When editing, the default time auto-fill is **disabled**
- Your existing times stay exactly as they are
- Manual changes work normally
- Validation still applies (end time must be after start time)

### 4. **Changes Saved to Same Entry**
- When you save edits, it **updates the existing entry**
- Preserves the entry's unique ID
- No duplicate entries created
- Notifications will reschedule automatically

## FILES MODIFIED:

1. **TimetableEntryDialog.kt**:
   - Added `Icons.Default.Edit` import
   - Added `editingSchedule` state to track which entry is being edited
   - Added Edit IconButton next to Delete button (line ~249)
   - Added edit sheet logic that opens TimetableInputBottomSheet with initialSchedule

2. **TimetableInputBottomSheet.kt**:
   - Added `initialSchedule` parameter (optional)
   - Added parsing logic to convert existing ClassSchedule to picker indices
   - Pre-fills all state values when editing
   - Disabled auto-fill defaults when initialSchedule is provided
   - Set `isEndTimeManuallySet = true` when editing to prevent auto-calculation

## HOW TO USE:

### To Edit a Timetable Entry:

1. Open the app
2. Go to a subject
3. Click to edit the subject (opens AddSubjectDialog or EditSubjectDialog)
4. Click "Weekly Timetable" button
5. You'll see your existing entries grouped by day
6. Click the **✏️ Edit icon** on any card
7. Modify the day, times, or location
8. Click "Add to Timetable"
9. Click "Save" to save all changes

### Example Flow:

```
Subject Screen
    → Edit Subject
        → Weekly Timetable
            → [Monday 9:00 AM - 10:00 AM] [✏️ Edit] [🗑️ Delete]
                → Click ✏️
                    → Bottom sheet opens with:
                        - Day: Monday (pre-selected)
                        - Start: 9:00 AM (pre-filled)
                        - End: 10:00 AM (pre-filled)
                        - Location: "Room 101" (pre-filled)
                    → Change to 9:30 AM - 10:30 AM
                    → Click "Add to Timetable"
                → Entry updated!
            → Click "Save"
        → Done!
```

## VALIDATION STILL APPLIES:

Even when editing, you cannot:
- ❌ Set an end time before the start time
- ❌ Set an end time equal to the start time

The "Add to Timetable" button will be disabled if invalid.

## NOTIFICATION RE-SCHEDULING:

When you edit a timetable entry and save:
- Old alarms are cancelled
- New alarms are scheduled based on new times
- WorkManager updates the periodic check
- Next time the check runs (within 15 minutes), it will use the new times

## TECHNICAL DETAILS:

### Time Parsing Logic:
```kotlin
// Converts "14:30" to:
// - Hour: 2 (12-hour format)
// - Minute: 30
// - AM/PM: PM
// - Picker indices for wheel pickers
```

### Update Logic:
```kotlin
val index = schedules.indexOf(schedule)
if (index != -1) {
    schedules[index] = schedule.copy(
        dayOfWeek = day,
        startTime = start,
        endTime = end,
        location = loc
    )
}
```

This preserves the schedule's ID while updating all fields.

---

**Build is running. The edit feature is fully implemented and ready to test!**

## UI PREVIEW:

```
┌─────────────────────────────────────┐
│ Monday                              │
├─────────────────────────────────────┤
│ 🕒 9:00 AM - 10:00 AM     [✏️] [🗑️] │
│ 📍 Room 101                         │
├─────────────────────────────────────┤
│ 🕒 2:00 PM - 3:00 PM      [✏️] [🗑️] │
│ 📍 Lab 3                            │
└─────────────────────────────────────┘
```

Click ✏️ → Edit times/location → Save → Done!
