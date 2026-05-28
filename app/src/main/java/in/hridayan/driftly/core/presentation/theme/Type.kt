@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package `in`.hridayan.driftly.core.presentation.theme

import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable

@Composable
fun appTypography(): Typography {
    val base = MaterialTheme.typography

    return Typography(
        displayLarge = base.displayLarge,
        displayMedium = base.displayMedium,
        displaySmall = base.displaySmall,

        headlineLarge = base.headlineLarge,
        headlineMedium = base.headlineMedium,
        headlineSmall = base.headlineSmall,

        titleLarge = base.titleLarge,
        titleMedium = base.titleMedium,
        titleSmall = base.titleSmall,

        bodyLarge = base.bodyLarge,
        bodyMedium = base.bodyMedium,
        bodySmall = base.bodySmall,

        labelLarge = base.labelLarge,
        labelMedium = base.labelMedium,
        labelSmall = base.labelSmall,

        // Emphasized variants
        displayLargeEmphasized = base.displayLargeEmphasized,
        displayMediumEmphasized = base.displayMediumEmphasized,
        displaySmallEmphasized = base.displaySmallEmphasized,

        headlineLargeEmphasized = base.headlineLargeEmphasized,
        headlineMediumEmphasized = base.headlineMediumEmphasized,
        headlineSmallEmphasized = base.headlineSmallEmphasized,

        titleLargeEmphasized = base.titleLargeEmphasized,
        titleMediumEmphasized = base.titleMediumEmphasized,
        titleSmallEmphasized = base.titleSmallEmphasized,

        bodyLargeEmphasized = base.bodyLargeEmphasized,
        bodyMediumEmphasized = base.bodyMediumEmphasized,
        bodySmallEmphasized = base.bodySmallEmphasized,

        labelLargeEmphasized = base.labelLargeEmphasized,
        labelMediumEmphasized = base.labelMediumEmphasized,
        labelSmallEmphasized = base.labelSmallEmphasized,
    )
}
