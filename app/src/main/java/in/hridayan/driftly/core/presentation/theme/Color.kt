@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package `in`.hridayan.driftly.core.presentation.theme

import android.content.Context
import android.os.Build
import androidx.annotation.FloatRange
import androidx.annotation.RequiresApi
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.expressiveLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.ColorUtils
import `in`.hridayan.driftly.core.utils.a1
import `in`.hridayan.driftly.core.utils.a2
import `in`.hridayan.driftly.core.utils.a3
import `in`.hridayan.driftly.core.utils.n1
import `in`.hridayan.driftly.core.utils.n2

@Composable
fun lightColorSchemeFromSeed(): ColorScheme {
    return expressiveLightColorScheme().copy(
        // Custom button colors
        primary = Color(0xFF163B7A), // Dark blue for buttons
        primaryContainer = Color(0xFFD7E2F8), // Light blue for button containers/selectors
        onPrimary = Color.White, // White text on buttons
        onPrimaryContainer = Color(0xFF163B7A), // Dark blue text on light blue containers
        inversePrimary = 80.a1,

        secondary = 40.a2.harmonizeWithPrimary(0.1f),
        secondaryContainer = 90.a2.harmonizeWithPrimary(0.1f),
        onSecondary = 100.a2.harmonizeWithPrimary(0.1f),
        onSecondaryContainer = 10.a2.harmonizeWithPrimary(0.1f),

        tertiary = 40.a3.harmonizeWithPrimary(0.1f),
        tertiaryContainer = 90.a3.harmonizeWithPrimary(0.1f),
        onTertiary = 100.a3.harmonizeWithPrimary(0.1f),
        onTertiaryContainer = 10.a3.harmonizeWithPrimary(0.1f),

        // Custom background color
        background = Color(0xFFF2F6E3), // Light greenish cream
        onBackground = 10.n1,

        // Custom surface colors
        surface = Color(0xFFF2F6E3), // Light greenish cream
        onSurface = 10.n1,
        surfaceVariant = 90.n2,
        onSurfaceVariant = 30.n2,
        surfaceDim = 87.n1,
        surfaceBright = Color(0xFFF2F6E3),
        
        // Pure white for all cards
        surfaceContainerLowest = Color.White,
        surfaceContainerLow = Color.White,
        surfaceContainer = Color.White, // Main card color
        surfaceContainerHigh = Color.White,
        surfaceContainerHighest = Color.White,
        
        inverseSurface = 20.n1,
        inverseOnSurface = 95.n1,

        outline = 50.n2,
        outlineVariant = 80.n2,
    )
}

@Composable
fun darkColorSchemeFromSeed(): ColorScheme {
    return darkColorScheme(
        primary = Color.White, // Pure white for buttons
        primaryContainer = Color(0xFF2C2C2C), // #2c2c2c for cards
        onPrimary = Color.Black,
        onPrimaryContainer = 90.a1,
        inversePrimary = 40.a1,

        secondary = 80.a2.harmonizeWithPrimary(0.1f),
        secondaryContainer = Color(0xFF2C2C2C), // #2c2c2c for cards
        onSecondary = 20.a2.harmonizeWithPrimary(0.1f),
        onSecondaryContainer = 90.a2.harmonizeWithPrimary(0.1f),

        tertiary = 80.a3.harmonizeWithPrimary(0.1f),
        tertiaryContainer = Color(0xFF2C2C2C), // #2c2c2c for cards
        onTertiary = 20.a3.harmonizeWithPrimary(0.1f),
        onTertiaryContainer = 90.a3.harmonizeWithPrimary(0.1f),

        background = Color.Black, // Pure black background
        onBackground = 90.n1,

        surface = Color.Black, // Pure black surface
        onSurface = 90.n1,
        surfaceVariant = Color(0xFF2C2C2C), // #2c2c2c for cards
        onSurfaceVariant = 80.n2,
        surfaceDim = Color.Black,
        surfaceBright = Color(0xFF2C2C2C),
        surfaceContainerLowest = Color.Black,
        surfaceContainerLow = Color(0xFF2C2C2C), // #2c2c2c for cards
        surfaceContainer = Color(0xFF2C2C2C), // #2c2c2c for cards
        surfaceContainerHigh = Color(0xFF2C2C2C), // #2c2c2c for cards
        surfaceContainerHighest = Color(0xFF2C2C2C), // #2c2c2c for cards
        inverseSurface = 90.n1,
        inverseOnSurface = 20.n1,

        outline = 60.n2,
        outlineVariant = 30.n2,
    )
}

@Composable
fun highContrastDarkColorSchemeFromSeed(): ColorScheme {
    return darkColorSchemeFromSeed().copy(
        background = Color.Black,
        surface = Color.Black,
        surfaceContainerLowest = Color.Black,
        surfaceContainerLow = 6.n1,
        surfaceContainer = 10.n1,
        surfaceContainerHigh = 12.n1,
        surfaceContainerHighest = 17.n1,
    )
}

@RequiresApi(Build.VERSION_CODES.S)
fun highContrastDynamicDarkColorScheme(context: Context): ColorScheme {
    return dynamicDarkColorScheme(context = context).copy(
        background = Color.Black,
        surface = Color.Black,
        surfaceContainerLowest = Color.Black,
        surfaceContainerLow = dynamicDarkColorScheme(context).surfaceContainerLowest,
        surfaceContainer = dynamicDarkColorScheme(context).surfaceContainerLow,
        surfaceContainerHigh = dynamicDarkColorScheme(context).surfaceContainer,
        surfaceContainerHighest = dynamicDarkColorScheme(context).surfaceContainerHigh,
    )
}

@Composable
fun Color.harmonizeWithPrimary(
    @FloatRange(from = 0.0, to = 1.0) fraction: Float = 0.1f
): Color = blend(MaterialTheme.colorScheme.primary, fraction)


fun Color.blend(
    color: Color,
    @FloatRange(from = 0.0, to = 1.0) fraction: Float = 0.1f
): Color = Color(ColorUtils.blendARGB(this.toArgb(), color.toArgb(), fraction))
