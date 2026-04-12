@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package `in`.hridayan.driftly.core.presentation.theme

import android.app.Activity
import android.os.Build
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import `in`.hridayan.driftly.core.common.LocalDarkMode
import `in`.hridayan.driftly.core.common.LocalSettings

@Composable
fun DriftlyTheme(
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    val context = LocalContext.current
    val darkTheme = LocalDarkMode.current
    val dynamicColor = LocalSettings.current.isDynamicColor
    val isHighContrastDarkTheme = LocalSettings.current.isHighContrastDarkMode
    val fontFamily = LocalSettings.current.fontFamily

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            val insetsController = WindowCompat.getInsetsController(window, view)
            insetsController.isAppearanceLightStatusBars = !darkTheme
            insetsController.isAppearanceLightNavigationBars = !darkTheme
        }
    }

    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme && isHighContrastDarkTheme) highContrastDynamicDarkColorScheme(context)
            else if (darkTheme) dynamicDarkColorScheme(context)
            else dynamicLightColorScheme(context)
        }

        darkTheme -> {
            if (isHighContrastDarkTheme) highContrastDarkColorSchemeFromSeed()
            else darkColorSchemeFromSeed()
        }

        else -> lightColorSchemeFromSeed()
    }

    MaterialExpressiveTheme(
        colorScheme = colorScheme,
        typography = appTypography(fontFamily),
        content = content
    )
}