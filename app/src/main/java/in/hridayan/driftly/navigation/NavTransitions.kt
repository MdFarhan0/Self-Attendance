package `in`.hridayan.driftly.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally

fun slideFadeInFromRight(): EnterTransition {
    return slideInHorizontally(
        initialOffsetX = { (it * 0.15f).toInt() },
        animationSpec = tween(250, easing = FastOutSlowInEasing)
    ) + fadeIn(
        initialAlpha = 0.5f,
        animationSpec = tween(200, delayMillis = 66, easing = LinearEasing)
    )
}

fun slideFadeOutToRight(): ExitTransition {
    return slideOutHorizontally(
        targetOffsetX = { (it * 0.10f).toInt() },
        animationSpec = tween(250, easing = FastOutSlowInEasing)
    ) + fadeOut(
        animationSpec = tween(50, easing = LinearEasing)
    )
}

fun slideFadeInFromLeft(): EnterTransition {
    return slideInHorizontally(
        initialOffsetX = { -(it * 0.15f).toInt() },
        animationSpec = tween(350, easing = FastOutSlowInEasing)
    ) + fadeIn(
        initialAlpha = 0.5f,
        animationSpec = tween(50, delayMillis = 66, easing = LinearEasing)
    )
}

fun slideFadeOutToLeft(): ExitTransition {
    return slideOutHorizontally(
        targetOffsetX = { -(it * 0.10f).toInt() },
        animationSpec = tween(250, easing = FastOutSlowInEasing)
    ) + fadeOut(
        animationSpec = tween(50, easing = LinearEasing)
    )
}

// Material 3 Shared Axis X animation (forward navigation - slide right)
fun sharedAxisXEnter(): EnterTransition {
    return slideInHorizontally(
        initialOffsetX = { fullWidth -> fullWidth / 10 },
        animationSpec = tween(
            durationMillis = 300,
            easing = FastOutSlowInEasing
        )
    ) + fadeIn(
        animationSpec = tween(
            durationMillis = 210,
            delayMillis = 90,
            easing = LinearEasing
        )
    )
}

// Material 3 Shared Axis X animation (forward exit - slide left)
fun sharedAxisXExit(): ExitTransition {
    return slideOutHorizontally(
        targetOffsetX = { fullWidth -> -fullWidth / 10 },
        animationSpec = tween(
            durationMillis = 300,
            easing = FastOutSlowInEasing
        )
    ) + fadeOut(
        animationSpec = tween(
            durationMillis = 90,
            easing = LinearEasing
        )
    )
}

// Material 3 Shared Axis X animation (back navigation enter - slide from left)
fun sharedAxisXPopEnter(): EnterTransition {
    return slideInHorizontally(
        initialOffsetX = { fullWidth -> -fullWidth / 10 },
        animationSpec = tween(
            durationMillis = 300,
            easing = FastOutSlowInEasing
        )
    ) + fadeIn(
        animationSpec = tween(
            durationMillis = 210,
            delayMillis = 90,
            easing = LinearEasing
        )
    )
}

// Material 3 Shared Axis X animation (back navigation exit - slide to right)
fun sharedAxisXPopExit(): ExitTransition {
    return slideOutHorizontally(
        targetOffsetX = { fullWidth -> fullWidth / 10 },
        animationSpec = tween(
            durationMillis = 300,
            easing = FastOutSlowInEasing
        )
    ) + fadeOut(
        animationSpec = tween(
            durationMillis = 90,
            easing = LinearEasing
        )
    )
}
