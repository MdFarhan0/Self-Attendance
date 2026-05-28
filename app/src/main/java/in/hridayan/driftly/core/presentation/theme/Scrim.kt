package `in`.hridayan.driftly.core.presentation.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.MaterialTheme
import `in`.hridayan.driftly.core.common.LocalDarkMode

@Composable
fun adaptiveModalScrimColor(): Color {
    val isDarkMode = LocalDarkMode.current
    val baseScrim = MaterialTheme.colorScheme.scrim
    val alpha = if (isDarkMode) 0.68f else 0.52f
    return baseScrim.copy(alpha = alpha)
}

@Composable
fun adaptiveStrongScrimColor(): Color {
    val isDarkMode = LocalDarkMode.current
    val baseScrim = MaterialTheme.colorScheme.scrim
    val alpha = if (isDarkMode) 0.88f else 0.72f
    return baseScrim.copy(alpha = alpha)
}
