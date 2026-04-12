package `in`.hridayan.driftly.core.common

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.runtime.compositionLocalOf
import `in`.hridayan.driftly.core.domain.model.SubjectCardStyle
import `in`.hridayan.driftly.core.domain.provider.SeedColorProvider
import `in`.hridayan.driftly.settings.domain.model.CustomFontFamily
import `in`.hridayan.driftly.settings.domain.model.SettingsState

val LocalSettings = compositionLocalOf {
    SettingsState(
        themeMode = AppCompatDelegate.MODE_NIGHT_NO,
        isHighContrastDarkMode = false,
        seedColor = SeedColorProvider.seed,
        isDynamicColor = false,
        isHapticEnabled = true,
        subjectCardCornerRadius = 8f,
        subjectCardStyle = SubjectCardStyle.CARD_STYLE_A,
        savedVersionCode = 0,
        showAttendanceStreaks = true,
        rememberCalendarMonthYear = false,
        startWeekOnMonday = true,
        notificationPreference = true,
        notificationPermissionDialogShown = false,
        fontFamily = CustomFontFamily.SIRIN_STENCIL
    )
}