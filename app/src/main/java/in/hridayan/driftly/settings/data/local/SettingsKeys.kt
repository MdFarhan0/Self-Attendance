package `in`.hridayan.driftly.settings.data.local

import androidx.appcompat.app.AppCompatDelegate
import `in`.hridayan.driftly.core.domain.model.SubjectCardStyle
import `in`.hridayan.driftly.core.domain.provider.SeedColorProvider

enum class SettingsKeys(val default: Any?) {
    LOOK_AND_FEEL(null),
    ABOUT(null),
    BEHAVIOR(null),
    STREAK_MODIFIER(true),
    LANGUAGE(null),
    THEME_MODE(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM),
    DARK_THEME(null),
    HIGH_CONTRAST_DARK_MODE(false),
    PRIMARY_SEED(SeedColorProvider.primary),
    SECONDARY_SEED(SeedColorProvider.secondary),
    TERTIARY_SEED(SeedColorProvider.tertiary),
    DYNAMIC_COLORS(true),
    HAPTICS_AND_VIBRATION(true),
    VERSION(null),
    CHANGELOGS(null),
    REPORT(null),
    FEATURE_REQUEST(null),
    PRIVACY_POLICY(null),
    GITHUB(null),
    LICENSE(null),
    CUSTOMISATION(null),
    FEATURES(null),
    LOCK_ATTENDANCE(false),
    SUBJECT_CARD_CORNER_RADIUS(12f),
    AUTO_HANDLE_UNMARKED_DAYS(false),
    AUTO_HANDLE_UNMARKED_DAYS_BEHAVIOR(2),

    SUBJECT_CARD_STYLE(SubjectCardStyle.CARD_STYLE_A),
    HOME_SUBJECT_CARD_LAYOUT_MODE(0),
    SAVED_VERSION_CODE(0),
    LAST_MIGRATED_VERSION(0),
    REMEMBER_CALENDAR_MONTH_YEAR(false),
    START_WEEK_ON_MONDAY(false),
    BACKUP_AND_RESTORE(null),
    BACKUP_APP_SETTINGS(null),
    BACKUP_APP_DATABASE(null),
    BACKUP_APP_DATA(null),
    RESTORE_APP_DATA(null),
    RESET_APP_SETTINGS(null),
    LAST_BACKUP_TIME(""),
    NOTIFICATION_SETTINGS(null),
    REMINDER_MARK_ATTENDANCE(true),
    NOTIFY_MISSED_ATTENDANCE(true),
    ENABLE_NOTIFICATIONS(true),
    NOTIFICATION_PERMISSION_DIALOG_SHOWN(false),
    SHOW_GITHUB_WARNING_DIALOG(true),
    ENABLE_TIMETABLE_NOTIFICATIONS(false),
    PERSISTENT_NOTIFICATIONS(false),  // Keep notifications until user acts
    GRACE_PERIOD_MINUTES(60),  // Default: 1 hour
    DEFAULT_START_MINUTE(45), // Default start minute (e.g., 9:45)
    DEFAULT_CLASS_DURATION(60), // Default duration in minutes
    FONT_FAMILY(null)
}
