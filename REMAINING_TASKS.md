# Remaining Tasks for Self Attendance App

## Completed ✅
1. ✅ Fixed About page - Added SupportMeCard directly
2. ✅ Fixed attendance card roundness - Changed from 15dp to 25dp
3. ✅ Increased minimum width of attendance card - Added widthIn(min = 380.dp)

## Still To Do 🔄

### 4. Font Family Settings
- Remove boldness/underline/italic/strikethrough options from font family selector
- Add more font families to choose from
- Keep only family selection, remove text style options

### 5. Default to Light Mode
- Add theme mode selector in Look & Feel (like dark theme subsystem)
- Set default to Light Mode instead of Dark/System

### 6. Dynamic Color Changes
- Remove system dynamic color toggle
- Add manual color picker with ONLY these shade groups:
  - Green shades
  - Sky blue shades
  - Blue shades
  - Navy blue shades (add if not present)
- Remove all other color shades

## Files to Modify
- Font settings: Check RadioGroupOptionsProvider and font family settings
- Theme settings: SettingsProvider.kt - lookAndFeelPageList
- Color picker: AppSeedColors or color palette provider
- Default theme: SettingsKeys.kt default values

## Build Status
- Last build: ✅ SUCCESS
- APK Location: app\build\outputs\apk\debug\app-debug.apk
