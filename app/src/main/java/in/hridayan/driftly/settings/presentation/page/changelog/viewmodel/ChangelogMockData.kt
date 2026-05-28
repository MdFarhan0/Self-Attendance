package `in`.hridayan.driftly.settings.presentation.page.changelog.viewmodel

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.ui.graphics.vector.ImageVector

data class ChangelogFeature(
    val icon: ImageVector,
    val title: String,
    val description: String
)

data class ChangelogEntry(
    val version: String,
    val features: List<ChangelogFeature>
)

object ChangelogMockData {
    val changelogs = listOf(
        ChangelogEntry(
            version = "2.4.0",
            features = listOf(
                ChangelogFeature(Icons.Rounded.PictureAsPdf, "PDF Export", "Generate precise and professional offline PDF attendance reports with a clean and polished layout."),
                ChangelogFeature(Icons.Rounded.AutoAwesome, "Stunning UI", "Introduced multiple under-the-hood UI improvements and refined the overall experience closer to Material 3 Expressive design."),
                ChangelogFeature(Icons.Rounded.DeleteOutline, "Removed Bloat", "Cleaned up unused and unnecessary code, reducing the APK size from 6 MB to 3.8 MB for a lighter and more optimized app experience."),
                ChangelogFeature(Icons.Rounded.Animation, "Micro Animations", "Added smooth micro animations across the app, including buttons and interactions, for a more fluid and responsive feel."),
                ChangelogFeature(Icons.Rounded.Tune, "More Customization & Features", "Explore Settings to discover under-the-hood customizations, improvements, and newly added features.")
            )
        ),
        ChangelogEntry(
            version = "2.3.0",
            features = listOf(
                ChangelogFeature(Icons.Rounded.Layers, "Freeform Bottom Sheets", "Visual overhaul of sheets to float beautifully off screen borders."),
                ChangelogFeature(Icons.Rounded.Lock, "Lock Attendance", "Added security toggle to lock attendance logging for past days."),
                ChangelogFeature(Icons.Rounded.DarkMode, "AMOLED Dark Theme", "Granular adjustments to absolute dark colors for M3 compliance.")
            )
        ),
        ChangelogEntry(
            version = "2.2.0",
            features = listOf(
                ChangelogFeature(Icons.Rounded.CalendarMonth, "Calendar Overhaul", "Refined custom calendar rendering class logs as dots."),
                ChangelogFeature(Icons.Rounded.Sensors, "Tactile Haptics", "Subtle weak haptic feedback integrated on every key click."),
                ChangelogFeature(Icons.Rounded.Percent, "Target Percentages", "Configure customized attendance goals for specific subjects.")
            )
        ),
        ChangelogEntry(
            version = "2.1.0",
            features = listOf(
                ChangelogFeature(Icons.Rounded.NotificationAdd, "Smart Reminders", "Get daily local reminders to log attendance after scheduled classes."),
                ChangelogFeature(Icons.Rounded.DashboardCustomize, "Home Widgets", "Interactive homescreen widgets to log attendance directly."),
                ChangelogFeature(Icons.Rounded.FilterList, "Subject Filters", "Quickly filter subjects by name, code, or low attendance states.")
            )
        ),
        ChangelogEntry(
            version = "2.0.0",
            features = listOf(
                ChangelogFeature(Icons.Rounded.Code, "Jetpack Compose Rewrite", "Rewrote the app codebase in modern Jetpack Compose for smooth 120Hz scrolling."),
                ChangelogFeature(Icons.Rounded.BackupTable, "CSV Export / Import", "Backup and share your historic logs in raw spreadsheet CSV layout."),
                ChangelogFeature(Icons.Rounded.Storage, "Local Room SQLite", "Highly secure local SQLite DB for instant loading speeds.")
            )
        ),
        ChangelogEntry(
            version = "1.9.0",
            features = listOf(
                ChangelogFeature(Icons.Rounded.Archive, "Subject Archive", "Hide completed courses dynamically without deleting past logs."),
                ChangelogFeature(Icons.Rounded.CardGiftcard, "Holiday Mode Safety", "Pause schedule reminders during breaks to prevent logging errors."),
                ChangelogFeature(Icons.Rounded.RestartAlt, "Bulk Reset Controls", "Easily clear logs for a single subject or semester safely.")
            )
        ),
        ChangelogEntry(
            version = "1.8.0",
            features = listOf(
                ChangelogFeature(Icons.Rounded.Pin, "Subject Codes", "Optional subject/course codes support displayed on home cards."),
                ChangelogFeature(Icons.Rounded.Memory, "Performance Tuning", "Reduced memory usage during database updates by over 40%."),
                ChangelogFeature(Icons.Rounded.PowerSettingsNew, "Granular Toggles", "Individually toggle schedule alerts or class notifications.")
            )
        ),
        ChangelogEntry(
            version = "1.7.0",
            features = listOf(
                ChangelogFeature(Icons.Rounded.Calculate, "Bunk Calculator", "Smart math tool showing classes you can miss to stay above targets."),
                ChangelogFeature(Icons.Rounded.Palette, "Color Palette Themes", "Custom themes including Ocean Blue, Emerald Green, and Lavender."),
                ChangelogFeature(Icons.Rounded.SwapHoriz, "Swipe Actions", "Quick horizontal swipe motions to remove or edit log entries.")
            )
        ),
        ChangelogEntry(
            version = "1.6.0",
            features = listOf(
                ChangelogFeature(Icons.Rounded.TabletAndroid, "Responsive Layouts", "Fluid configurations supporting tablets and foldable displays."),
                ChangelogFeature(Icons.Rounded.Timeline, "History Timeline", "A complete chronological feed of all logged entries."),
                ChangelogFeature(Icons.Rounded.NoteAdd, "Attached Notes", "Add custom short descriptions to individual class logs.")
            )
        ),
        ChangelogEntry(
            version = "1.5.0",
            features = listOf(
                ChangelogFeature(Icons.Rounded.Schedule, "Timetable Automation", "Home schedule dynamically updates based on system clock."),
                ChangelogFeature(Icons.Rounded.SettingsAccessibility, "Accessibility Support", "Full screen reader accessibility and color-blind ratios."),
                ChangelogFeature(Icons.Rounded.AdsClick, "Quick Actions Menu", "Popup helper controls to log state changes in one tap.")
            )
        ),
        ChangelogEntry(
            version = "1.4.0",
            features = listOf(
                ChangelogFeature(Icons.Rounded.TrendingUp, "Streak Tracker", "Visual metrics showing consecutive present class streaks."),
                ChangelogFeature(Icons.Rounded.CloudUpload, "Auto Cloud Exports", "Option to export automatic data backups silently in the background."),
                ChangelogFeature(Icons.Rounded.Animation, "Page Transitions", "Smooth animations when switching between calendar and settings.")
            )
        ),
        ChangelogEntry(
            version = "1.3.0",
            features = listOf(
                ChangelogFeature(Icons.Rounded.ViewWeek, "Weekly Schedule Grid", "A comprehensive overview of your weekly course timetables."),
                ChangelogFeature(Icons.Rounded.OfflineBolt, "100% Offline Mode", "Zero internet connections required. Your database stays yours."),
                ChangelogFeature(Icons.Rounded.ShowChart, "Detail Analytics", "Deep graphical insights into attendance analytics on click.")
            )
        ),
        ChangelogEntry(
            version = "1.2.0",
            features = listOf(
                ChangelogFeature(Icons.Rounded.Colorize, "Subject Color Tags", "Assign distinct colors to different dashboard cards."),
                ChangelogFeature(Icons.Rounded.Dns, "Multi-Slot Logs", "Log multiple lab sessions or double lectures sequentially."),
                ChangelogFeature(Icons.Rounded.FastForward, "Cold Startup Slashing", "Optimized database structures for instant startup under 300ms.")
            )
        ),
        ChangelogEntry(
            version = "1.1.0",
            features = listOf(
                ChangelogFeature(Icons.Rounded.NotificationsActive, "Push Alerts", "Periodic local pushes prompting you to log attendance."),
                ChangelogFeature(Icons.Rounded.CalendarViewWeek, "Semester Setups", "Define custom semester boundaries to align your trackers."),
                ChangelogFeature(Icons.Rounded.Warning, "Safety Dialogs", "Confirmation alerts to prevent accidental subject deletions.")
            )
        ),
        ChangelogEntry(
            version = "1.0.0",
            features = listOf(
                ChangelogFeature(Icons.Rounded.RocketLaunch, "Initial Launch", "Birth of Self Attendance tracker app. Create and log subjects."),
                ChangelogFeature(Icons.Rounded.CalendarToday, "Daily Logger View", "Standard monthly calendar logs with easy calendar taps."),
                ChangelogFeature(Icons.Rounded.QueryStats, "Target Standard Alert", "Red warnings for subjects dropping below 75% thresholds.")
            )
        )
    )
}
