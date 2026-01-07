# Self Attendance

**Self Attendance** is a comprehensive, privacy-focused academic tracking tool designed for effortless attendance management. Built with modern Android technologies, it revolutionizes how students manage their class attendance by moving beyond simple counters to a **calendar-first approach**.

Unlike traditional apps that only show percentages, Self Attendance provides an intuitive visual calendar interface where you can mark attendance for specific dates, view patterns at a glance, and get smart **AI-powered insights** about your bunk allowance.

---

## 🚀 New in v2.0.0: Timetable Notification System

The latest update introduces a highly requested, fully automated notification engine.

*   **Exact-Time Alerts**: Notifications trigger precisely at the start of your class using Android's `AlarmManager` API, bypassing standard battery optimization delays.
*   **Interactive Actions**: Mark your attendance directly from the notification shade. Options for **"Attended"** and **"Missed"** allow for instant database updates without unlocking the phone or opening the app.
*   **Reliable Persistence**: The system automatically reschedules all alarms upon device reboot or app updates, ensuring no class is ever forgotten.
*   **Conflict Handling**: Intelligently manages overlapping schedules and delivers distinct alerts for simultaneous events.

---

## Why Self Attendance Stands Out

Most attendance trackers are just simple counters with no context. Self Attendance solves this:

| Traditional Apps ❌ | Self Attendance ✅ |
| :--- | :--- |
| **No Date Association**: Can't see *when* you attended. | **Date-Specific Records**: Every attendance is tied to a calendar date. |
| **Just Numbers**: No visual history. | **Visual Calendar**: See streaks, gaps, and monthly patterns instantly. |
| **Manual Math**: You figure out bunking logic. | **AI Insights**: Smart calculations tell you *exactly* how many classes you can bunk. |
| **Limited History**: Can't view past months. | **Monthly Navigation**: Browse complete attendance history. |

---

## Key Features

### 📅 Calendar-Based Attendance
*   **Visual Overview**: See your entire month's attendance at a glance.
*   **Color-Coded**: Dates are marked Green (Present) or Red (Absent) for instant feedback.
*   **Streak Tracking**: Monitor your longest present or absent streaks.

### 🎯 Smart Attendance Insights
The app doesn't just show a percentage. It tells you what to do:
*   *"You can bunk **3** more classes and still stay above 75%."*
*   *"You are safe at 75%. Don't miss the next class!"*
*   *"Attend the next **2** classes to reach your target."*

### 🔔 Intelligent Timetable Notifications (New!)
*   **Exact Alarms**: Alerts fire precisely at class time using `AlarmManager`.
*   **Interactive**: Mark status directly from the notification.
*   **Resilient**: Alarms auto-restore after device reboots.

### 🎨 Modern Material You Design
*   **Dynamic Theming**: Adapts to your device wallpaper.
*   **Expressive UI**: Fluid animations and clean typography (One UI Sans).
*   **Dark Mode**: Fully optimized OLED-friendly dark theme.

### 💾 Backup & Restore
*   **Full Data Export**: Save your database and settings locally.
*   **Easy Restore**: Seamlessly migrate data to a new device.

---

## App Screenshots

| | | |
|:---:|:---:|:---:|
| <img src="./src/WhatsApp%20Image%202026-01-07%20at%201.33.10%20PM.jpeg" width="250" /> | <img src="./src/WhatsApp%20Image%202026-01-07%20at%201.33.10%20PM%20(1).jpeg" width="250" /> | <img src="./src/WhatsApp%20Image%202026-01-07%20at%201.33.11%20PM.jpeg" width="250" /> |
| <img src="./src/WhatsApp%20Image%202026-01-07%20at%201.33.11%20PM%20(1).jpeg" width="250" /> | <img src="./src/WhatsApp%20Image%202026-01-07%20at%201.33.11%20PM%20(2).jpeg" width="250" /> | <img src="./src/WhatsApp%20Image%202026-01-07%20at%201.33.12%20PM.jpeg" width="250" /> |
| <img src="./src/WhatsApp%20Image%202026-01-07%20at%201.33.12%20PM%20(2).jpeg" width="250" /> | <img src="./src/WhatsApp%20Image%202026-01-07%20at%201.33.13%20PM.jpeg" width="250" /> | <img src="./src/WhatsApp%20Image%202026-01-07%20at%201.33.13%20PM%20(1).jpeg" width="250" /> |

---

## 🛠️ Usage

### Quick Start
1.  **Add a Subject**: Tap the "+" button and enter subject details.
2.  **Set Target**: Choose your desired attendance percentage (default: 75%).
3.  **Mark Attendance**: Click any date on the calendar to toggle Present/Absent/Holiday.
4.  **View Insights**: Read the smart messages cards to know your bunk status.

### Pro Tips
*   **Sidebar**: Use the sidebar (rotating gear icon) for a quick summary of all subjects.
*   **Notifications**: Enable "Timetable Notifications" in settings to get class alerts.
*   **Backup**: Regular backups ensure you never lose your academic history.

---

## 🔧 Technical Specifications

*   **Language**: Kotlin
*   **UI Framework**: Jetpack Compose (Material 3)
*   **Architecture**: MVVM + Clean Architecture + Dependency Injection (Hilt)
*   **Database**: Room Database (Offline-first)
*   **Background Tasks**: WorkManager & AlarmManager

---

## Installation

You can download the latest APK from the [Releases](https://github.com/mhdfarhanhere/Driftly/releases) section.

1.  Download `app-arm64-v8a-release.apk` (Recommended).
2.  Install on your Android device.
3.  Grant Notification & Alarm permissions when prompted.

---

## Contributing & Support

If you find this app useful, please:
*   ⭐ **Star this repository** on GitHub.
*   🐛 **Report bugs** in the Issues section.
*   💡 **Suggest features** you'd like to see.

**Built with ❤️ by Md Farhan**
