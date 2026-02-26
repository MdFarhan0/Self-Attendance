# Issues Report for Self Attendance App

This report documents the identified issues, potential crashes, and architectural flaws in the Self Attendance repository.

## 1. Logical & Data Model Issues

### 1.1. Multiple Attendance Limitation
- **Affected Files:** `AttendanceEntity.kt`, `AttendanceDao.kt`, `MarkAttendanceWorker.kt`
- **Issue:** The `attendance` table uses a composite primary key consisting of `(subjectId, date)`. Since the `date` is stored as a string in `YYYY-MM-DD` format, each subject can only have one attendance record per day.
- **Cause:** This design prevents the app from supporting subjects that have multiple classes on the same day (e.g., a lecture and a lab), as the second record would overwrite the first. This directly contradicts the "Multiple Attendance Entries" feature claimed in the README.

### 1.2. Alarm Rescheduling Bug
- **Affected Files:** `RescheduleAlarmsWorker.kt`
- **Issue:** The worker fails to cancel existing alarms for subjects that no longer have any schedules.
- **Cause:** The logic checks `if (schedules.isNotEmpty())` before calling `scheduleAllClassNotifications`. If all schedules for a subject are deleted, this check fails, and the old alarms remain active in the `AlarmManager`.

### 1.3. Next Occurrence Logic Error
- **Affected Files:** `ClassNotificationScheduler.kt`
- **Issue:** The `lookAheadFromToday` parameter in `calculateNextOccurrence` is defined but completely ignored in the implementation.
- **Cause:** A coding oversight where the logic intended to force scheduling for the following week (even if the class time hasn't passed today) was never integrated.

---

## 2. UI & User Experience Bugs

### 2.1. Calendar Month/Year Mismatch
- **Affected Files:** `CalendarCanvas.kt`, `CalendarViewModel.kt`
- **Issue:** As seen in screenshots, the calendar header (e.g., "Jan, 2026") sometimes mismatches the actual month shown in the dropdown or the date grid.
- **Cause:** Synchronization issues between the local state in `CalendarCanvas` and the `ViewModel`'s `selectedMonthYear`.

### 2.2. Duplicate Timetable Entries
- **Affected Files:** `TimetableEntryDialog.kt`, `TimetableInputBottomSheet.kt`
- **Issue:** The app allows users to add multiple identical or overlapping class entries for the same subject, day, and time.
- **Cause:** Lack of validation in the `TimetableEntryDialog` or `onSave` callback to check for existing schedules before adding a new one.

### 2.3. Restricted Click Area for Day Selection
- **Affected Files:** `TimetableInputBottomSheet.kt`
- **Issue:** The day selection pills (Mon, Tue, etc.) only respond to clicks when tapping directly on the text, not the entire pill background.
- **Cause:** The `Modifier.clickable` is incorrectly applied to the `Text` composable instead of the parent `Box` which provides the background and padding.

### 2.4. XML Syntax Error in Resources
- **Affected Files:** `app/src/main/res/values/strings.xml`
- **Issue:** An extra double quote (`"`) exists after the closing tag of the `no_subject_yet` string resource.
- **Cause:** A typo in the resource file that could potentially cause issues with some XML parsers or future resource processing.

---

## 3. Architectural & Reliability Issues

### 3.1. Non-Transactional Data Restoration
- **Affected Files:** `BackupAndRestoreRepositoryImpl.kt`
- **Issue:** The data restoration process performs multiple `deleteAll` and `insertAll` operations sequentially without a database transaction.
- **Cause:** If the process is interrupted (e.g., app crash or power loss) during restoration, the database could be left in a corrupted state where some tables are empty while others are populated.

### 3.2. Fragile Notification Rescheduling
- **Affected Files:** `ClassAlarmReceiver.kt`
- **Issue:** The receiver reschedules the next week's alarm using a background coroutine (`CoroutineScope(Dispatchers.IO).launch`) within the `onReceive` method.
- **Cause:** Android may terminate a `BroadcastReceiver`'s process shortly after `onReceive` returns. If the coroutine hasn't finished, the next alarm will not be scheduled, causing the notification chain to break.

### 3.3. Midnight Boundary Limitation
- **Affected Files:** `TimetableInputBottomSheet.kt`
- **Issue:** Users cannot schedule classes that cross the midnight boundary (e.g., 11:00 PM to 1:00 AM).
- **Cause:** The validation logic uses a simple `endTotalMinutes > startTotalMinutes` check which doesn't account for classes spanning two days.

---

## 4. Security & Maintenance Issues

### 4.1. Hardcoded Encryption Key
- **Affected Files:** `EncryptionHelper.kt`
- **Issue:** The AES encryption key used for backups is hardcoded as a string in the source code.
- **Cause:** While documented as an intentional trade-off for simplicity and portability, it makes the "encryption" trivial to bypass for anyone with access to the APK.

### 4.2. Deprecated Room API Usage
- **Affected Files:** `SubjectDatabase.kt`
- **Issue:** Uses the deprecated `fallbackToDestructiveMigration()` without parameters.
- **Cause:** Failure to update to the newer Room API which requires explicit confirmation on whether all tables should be dropped.

### 4.3. Redundant Code
- **Affected Files:** `MainActivity.kt`
- **Issue:** Usage of `Modifier.Companion.fillMaxSize()` instead of the standard `Modifier.fillMaxSize()`.
- **Cause:** Minor coding style inconsistency.
