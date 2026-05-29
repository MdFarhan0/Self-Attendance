# 📅 Self Attendance
<p align="center">
  <img src="screenshots/img_1.jpeg" width="31%" style="border-radius: 20px; margin: 6px;" />
  <img src="screenshots/img_2.jpeg" width="31%" style="border-radius: 20px; margin: 6px;" />
  <img src="screenshots/img_3.jpeg" width="31%" style="border-radius: 20px; margin: 6px;" />
</p>
<p align="center">
  <img src="screenshots/img_4.jpeg" width="31%" style="border-radius: 20px; margin: 6px;" />
  <img src="screenshots/img_5.jpeg" width="31%" style="border-radius: 20px; margin: 6px;" />
  <img src="screenshots/img_6.jpeg" width="31%" style="border-radius: 20px; margin: 6px;" />
</p>
<p align="center">
  <img src="screenshots/img_7.jpeg" width="31%" style="border-radius: 20px; margin: 6px;" />
  <img src="screenshots/img_8.jpeg" width="31%" style="border-radius: 20px; margin: 6px;" />
  <img src="screenshots/img_9.jpeg" width="31%" style="border-radius: 20px; margin: 6px;" />
</p>
<p align="center">
  <img src="screenshots/img_10.jpeg" width="31%" style="border-radius: 20px; margin: 6px;" />
  <img src="screenshots/img_11.jpeg" width="31%" style="border-radius: 20px; margin: 6px;" />
  <img src="screenshots/img_12.jpeg" width="31%" style="border-radius: 20px; margin: 6px;" />
</p>
<p align="center">
  <a href="https://github.com/MdFarhan0/Self-Attendance/releases/latest">
    <img src="https://img.shields.io/github/v/release/MdFarhan0/Self-Attendance?include_prereleases&logo=github&style=for-the-badge&label=Latest%20Release" alt="Latest Release" />
  </a>
  <a href="https://github.com/MdFarhan0/Self-Attendance/releases">
    <img src="https://img.shields.io/github/downloads/MdFarhan0/Self-Attendance/total?logo=github&style=for-the-badge" alt="Total Downloads" />
  </a>
  <img src="https://img.shields.io/badge/Android-10%2B-green?style=for-the-badge&logo=android" alt="Android 10+" />
  <img src="https://img.shields.io/badge/Kotlin-100%25-purple?style=for-the-badge&logo=kotlin" alt="Kotlin" />
</p>
**Self Attendance** is a comprehensive, privacy-first academic tracking tool designed for effortless attendance management. Built with modern Android technologies, it revolutionizes how students manage their class attendance by moving beyond simple counters to an intuitive **calendar-first approach**.
Unlike traditional apps that only show percentages, Self Attendance provides an interactive visual calendar interface where you can mark attendance for specific dates, view streaks at a glance, and get smart **AI-powered bunk insights**.
---
## 🚀 New in v2.4.0: Attendance Automation & Play Updates
The latest version introduces significant functional upgrades, modern compliance, and an extremely lightweight optimized build:
* 🤖 **Auto Handle Unmarked Days**: Never forget to mark attendance again! If enabled, the app automatically logs past scheduled classes based on your selected behavior (`Attended`, `Absent`, or `Do Nothing`) using a custom horizontal Material 3 segmented selector.
* 🔄 **Flexible In-App Updates**: Officially integrated Google Play In-App Updates using the modern `StartIntentSenderForResult` API. Resumes interrupted background downloads on resume and alerts you via a floating Snackbar when ready to install.
* 🎯 **Target SDK 36 (Android 16)**: Built with up-to-date SDK configurations to guarantee full future compatibility.
* ⚡ **APK Footprint Slashed**: Removed unused code and redundant assets, dramatically shrinking the app installation size from **6 MB to 3.6 MB** for a rapid, lightweight download experience.
* 🌊 **Enhanced Splash Wave Animation**: Welcome sheet wave motion is now thicker, stretches lengthier across the display width, and features smoother frequency transitions.
---
## Why Self Attendance Stands Out
Most attendance trackers are just simple counter clickers with no context. Self Attendance solves this:
|
 Traditional Apps ❌ 
|
 Self Attendance ✅ 
|
|
:---
|
:---
|
|
**
No Date Association
**
: Can't see 
*
when
*
 you attended. 
|
**
Date-Specific Records
**
: Every attendance is logged to a calendar date. 
|
|
**
Just Numbers
**
: Simple counters with no visual logs. 
|
**
Visual Calendar
**
: See streaks, gaps, and monthly patterns instantly. 
|
|
**
Manual Math
**
: You figure out bunking logic yourself. 
|
**
AI Insights
**
: Smart cards calculate 
*
exactly
*
 how many classes you can bunk. 
|
|
**
Limited History
**
: Can't view past months or schedules. 
|
**
Monthly Navigation
**
: Browse complete historic logs easily. 
|
---
## Key Features
### 📅 Calendar-Based Attendance
* **Visual Overview**: See your entire month's attendance at a glance.
* **Color-Coded Status**: Dates are marked Green (Present) or Red (Absent) for instant visual feedback.
* **Streak Tracking**: Monitor your consecutive present or absent streaks automatically.
### 🎯 Smart Attendance Insights
The app doesn't just show a percentage. It tells you what to do:
* *"You can bunk **3** more classes and still stay above 75%."*
* *"You are safe at 75%. Don't miss the next class!"*
* *"Attend the next **2** classes to reach your target."*
### 🔔 Intelligent Timetable Notifications
* **Exact Alarms**: Alerts fire precisely at class time using `AlarmManager`.
* **Quick Logging**: Mark your status directly from the notification widget.
* **Resilient**: Alarms automatically restore after device reboots.
### 🎨 Modern Material You Design
* **Dynamic Color Theming**: App colors dynamically adapt to match your active wallpaper.
* **Expressive UI**: Premium backdrop blur glassmorphism and clean typography.
### 💾 Local Backup & Restore
* **Full Data Export**: Export and save your database and settings locally.
* **Seamless Import**: Migrate logs to a new device instantly.
---
## 🛠️ Usage
### Quick Start
1. **Add a Subject**: Tap the "+" button and enter subject details.
2. **Set Target**: Choose your desired target attendance percentage (default: 75%).
3. **Mark Attendance**: Click any date on the calendar to toggle Present/Absent/Holiday.
4. **View Insights**: Read the smart messages cards to know your bunk status.
### Pro Tips
* **Floating Navigation Menu**: Smoothly switch between Bunk details, Today's schedule, Tomorrow's class, and the Full Timetable directly from the HomeScreen menu.
* **Automation**: Toggle "Auto Handle Unmarked Days" in settings to let the app automatically manage unmarked classes.
* **Weekly Grid**: Check the Full Timetable dialog to review your entire weekly schedule at a glance.
---
## 🔧 Technical Specifications
* **Language**: Kotlin (100%)
* **UI Framework**: Jetpack Compose (Material 3 Expressive)
* **Architecture**: MVVM + Clean Architecture + Hilt Dependency Injection
* **Database**: Room SQLite (100% Offline-first)
* **Background Tasks**: WorkManager & AlarmManager
---
## Installation
You can download the latest APK from the [Releases](https://github.com/MdFarhan0/Self-Attendance/releases) section.
1. Download `app-release.apk` (Recommended).
2. Install on your Android device.
3. Grant Notification & Alarm permissions when prompted.
---
## Contributing & Support
If you find this app useful, please:
* ⭐ **Star this repository** on GitHub.
* 🐛 **Report bugs** in the Issues section.
* 💡 **Suggest features** you'd like to see.
**Built with ❤️ by Md Farhan**
