# Backup & Restore System - Complete Feature Coverage

## ✅ Updated Features

The backup and restore system has been updated to include **ALL app data**, including the newly added timetable feature.

## 📦 What Gets Backed Up

### 1. **Settings** (when "Settings Only" or "All Data" is selected)
- All app preferences and configurations
- Theme settings
- Notification preferences
- Display preferences
- Font settings
- All other user preferences

### 2. **Attendance Records** (when "Database Only" or "All Data" is selected)
- All attendance entries for all subjects
- Present/Absent status for each date
- Complete attendance history

### 3. **Subjects** (when "Database Only" or "All Data" is selected)
- All subject names
- Subject codes
- Histogram labels
- Subject metadata

### 4. **✨ Timetable Schedules** (NEW - when "Database Only" or "All Data" is selected)
- Weekly class schedules for all subjects
- Day of week, start time, end time
- Location information
- All timetable entries

## 🔄 Backup Options

The app offers three backup options:

### Option 1: **Backup Settings Only**
- Backs up only app preferences and settings
- Does NOT include attendance data, subjects, or timetables
- Fastest backup option
- Use when you want to transfer settings to a new device

### Option 2: **Backup App Database**
- Backs up attendance records, subjects, and **timetable schedules**
- Does NOT include app settings
- Use when you want to preserve your data but reset settings

### Option 3: **Backup All Data** ⭐ (Recommended)
- Backs up EVERYTHING:
  - Settings
  - Attendance records
  - Subjects
  - **Timetable schedules**
- Complete full backup
- Use for complete data migration or safety backup

## 🔒 Security

- All backups are **encrypted** using AES encryption
- Backup files use `.driftly` extension
- Data is secure even if the backup file is accessed by others

## 📂 Backup File Structure

```json
{
  "settings": {
    "theme": "dark",
    "notification_enabled": "true",
    // ... all other settings
  },
  "attendance": [
    {
      "subjectId": 1,
      "date": "2026-01-07",
      "status": "PRESENT"
    },
    // ... all attendance records
  ],
  "subjects": [
    {
      "id": 1,
      "subject": "Mathematics",
      "subjectCode": "MATH101",
      "histogramLabel": "Math"
    },
    // ... all subjects
  ],
  "classSchedules": [
    {
      "id": 1,
      "subjectId": 1,
      "dayOfWeek": 1,
      "startTime": "09:00",
      "endTime": "10:00",
      "location": "Room 101",
      "isEnabled": true
    },
    // ... all timetable schedules
  ],
  "backupTime": "07-01-2026 11:20"
}
```

## 🔄 Restore Process

When you restore a backup:

1. **All existing data is cleared** in the selected categories
2. **Backup data is imported** from the encrypted file
3. **Settings are reapplied** (if included in backup)
4. **Database is populated** with:
   - Attendance records
   - Subjects
   - **Timetable schedules** ✨

### ⚠️ Important Notes

- Restoring will **REPLACE ALL CURRENT DATA** in the selected categories
- Make sure you have a recent backup before restoring
- The app will show you the backup date/time before restoring

## 🚀 How to Use

### Creating a Backup

1. Open **Settings**
2. Navigate to **Backup & Restore**
3. Select backup option:
   - Settings Only
   - Database Only
   - All Data (recommended)
4. Tap **Create Backup**
5. Choose a location to save the `.driftly` file
6. Backup complete! ✅

### Restoring from Backup

1. Open **Settings**
2. Navigate to **Backup & Restore**
3. Tap **Restore Backup**
4. Select the `.driftly` backup file
5. Confirm the restoration
6. All data is restored! ✅

## 🔧 Technical Implementation

### Files Modified

1. **`BackupData.kt`**
   - Added `classSchedules: List<ClassScheduleEntity>?` field

2. **`ClassScheduleRepository.kt`**
   - Added `getAllSchedulesOnce()` method
   - Added `insertAllSchedules()` method

3. **`ClassScheduleRepositoryImpl.kt`**
   - Implemented backup methods

4. **`ClassScheduleDao.kt`**
   - Added `getAllSchedulesOnce()` query

5. **`BackupAndRestoreRepositoryImpl.kt`**
   - Injected `ClassScheduleRepository`
   - Updated `getBackupData()` to include timetable schedules
   - Updated `saveRestoredData()` to restore timetable schedules

### Code Snippets

#### Backup Implementation
```kotlin
private suspend fun getBackupData(option: BackupOption): BackupData {
    val settings = if (option == BackupOption.SETTINGS_ONLY || 
                      option == BackupOption.SETTINGS_AND_DATABASE)
        getSettingsMap() else null

    val attendance = if (option == BackupOption.DATABASE_ONLY || 
                         option == BackupOption.SETTINGS_AND_DATABASE)
        attendanceRepository.getAllAttendancesOnce() else null

    val subjects = if (option == BackupOption.DATABASE_ONLY || 
                       option == BackupOption.SETTINGS_AND_DATABASE)
        subjectRepository.getAllSubjectsOnce() else null

    val classSchedules = if (option == BackupOption.DATABASE_ONLY || 
                             option == BackupOption.SETTINGS_AND_DATABASE)
        classScheduleRepository.getAllSchedulesOnce() else null  // ✨ NEW

    return BackupData(settings, attendance, subjects, classSchedules, backupTime)
}
```

#### Restore Implementation
```kotlin
private suspend fun saveRestoredData(data: BackupData) {
    data.attendance?.let {
        attendanceRepository.deleteAllAttendances()
        attendanceRepository.insertAllAttendances(it)
    }
    data.subjects?.let {
        subjectRepository.deleteAllSubjects()
        subjectRepository.insertAllSubjects(it)
    }
    data.classSchedules?.let {  // ✨ NEW
        classScheduleRepository.deleteAllSchedules()
        classScheduleRepository.insertAllSchedules(it)
    }
    data.settings?.let { restoreSettings(it) }
}
```

## ✅ Verification Checklist

After restoring a backup, verify:

- [ ] All subjects are present
- [ ] Attendance records are correct
- [ ] **Timetable schedules are restored** ✨
- [ ] Settings are applied correctly
- [ ] **Timetable notifications are working** (if using AlarmManager)

## 🎯 Summary

The backup and restore system now includes **complete coverage** of all app features:

| Feature | Backed Up | Restored |
|---------|-----------|----------|
| Settings | ✅ | ✅ |
| Attendance Records | ✅ | ✅ |
| Subjects | ✅ | ✅ |
| **Timetable Schedules** | ✅ NEW | ✅ NEW |

Your weekly timetable is now safely backed up and can be restored along with all other app data!

---

**Note:** After implementing AlarmManager-based notifications, you may need to reschedule alarms after restore. This can be done by adding a call to `TimetableAlarmScheduler` after restoring class schedules.
