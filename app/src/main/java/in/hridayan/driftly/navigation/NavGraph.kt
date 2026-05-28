package `in`.hridayan.driftly.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavDestination.Companion.hasRoute
import `in`.hridayan.driftly.calender.presentation.screens.CalendarScreen
import `in`.hridayan.driftly.home.presentation.screens.HomeScreen
import `in`.hridayan.driftly.settings.presentation.page.about.screens.AboutScreen
import `in`.hridayan.driftly.settings.presentation.page.attendancewrapped.screens.AttendanceWrappedScreen
import `in`.hridayan.driftly.settings.presentation.page.backup.screens.BackupAndRestoreScreen
import `in`.hridayan.driftly.settings.presentation.page.behavior.screens.BehaviorScreen
import `in`.hridayan.driftly.settings.presentation.page.customisation.screens.CustomisationScreen
import `in`.hridayan.driftly.settings.presentation.page.features.screens.FeaturesScreen as FeaturesScreenComposable
import `in`.hridayan.driftly.settings.presentation.page.lookandfeel.screens.DarkThemeScreen
import `in`.hridayan.driftly.settings.presentation.page.lookandfeel.screens.LookAndFeelScreen
import `in`.hridayan.driftly.settings.presentation.page.mainscreen.screen.SettingsScreen
import `in`.hridayan.driftly.settings.presentation.page.notification.screens.NotificationScreen
import kotlinx.serialization.Serializable

@Composable
fun Navigation() {
    val navController = rememberNavController()
    CompositionLocalProvider(LocalNavController provides navController) {
        NavHost(
            navController = navController, startDestination = HomeScreen
        ) {
            composable<HomeScreen>(
                exitTransition = {
                    if (targetState.destination.hasRoute<CalendarScreen>()) slideFadeOutToLeft()
                    else tomatoForwardExit()
                },
                popEnterTransition = {
                    if (initialState.destination.hasRoute<CalendarScreen>()) slideFadeInFromLeft()
                    else tomatoPopEnter()
                }
            ) {
                HomeScreen()
            }

            composable<CalendarScreen>(
                enterTransition = { slideFadeInFromRight() },
                popExitTransition = { slideFadeOutToLeft() }
            ) {
                CalendarScreen()
            }

            composable<SettingsScreen>(
                enterTransition = { tomatoForwardEnter() },
                exitTransition = { tomatoForwardExit() },
                popEnterTransition = { tomatoPopEnter() },
                popExitTransition = { tomatoPopExit() }
            ) {
                SettingsScreen()
            }

            composable<LookAndFeelScreen>(
                enterTransition = { tomatoForwardEnter() },
                exitTransition = { tomatoForwardExit() },
                popEnterTransition = { tomatoPopEnter() },
                popExitTransition = { tomatoPopExit() }
            ) {
                LookAndFeelScreen()
            }

            composable<CustomisationScreen>(
                enterTransition = { tomatoForwardEnter() },
                exitTransition = { tomatoForwardExit() },
                popEnterTransition = { tomatoPopEnter() },
                popExitTransition = { tomatoPopExit() }
            ) {
                CustomisationScreen()
            }

            composable<FeaturesScreen>(
                enterTransition = { tomatoForwardEnter() },
                exitTransition = { tomatoForwardExit() },
                popEnterTransition = { tomatoPopEnter() },
                popExitTransition = { tomatoPopExit() }
            ) {
                FeaturesScreenComposable()
            }

            composable<AttendanceWrappedScreen>(
                enterTransition = { tomatoForwardEnter() },
                popExitTransition = { tomatoPopExit() }
            ) {
                AttendanceWrappedScreen()
            }

            composable<DarkThemeScreen>(
                enterTransition = { tomatoForwardEnter() },
                popExitTransition = { tomatoPopExit() }
            ) {
                DarkThemeScreen()
            }

            composable<BehaviorScreen>(
                enterTransition = { tomatoForwardEnter() },
                exitTransition = { tomatoForwardExit() },
                popEnterTransition = { tomatoPopEnter() },
                popExitTransition = { tomatoPopExit() }
            ) {
                BehaviorScreen()
            }

            composable<AboutScreen>(
                enterTransition = { tomatoForwardEnter() },
                exitTransition = { tomatoForwardExit() },
                popEnterTransition = { tomatoPopEnter() },
                popExitTransition = { tomatoPopExit() }
            ) {
                AboutScreen()
            }

            composable<ChangelogScreen>(
                enterTransition = { tomatoForwardEnter() },
                exitTransition = { tomatoForwardExit() },
                popEnterTransition = { tomatoPopEnter() },
                popExitTransition = { tomatoPopExit() }
            ) {
                `in`.hridayan.driftly.settings.presentation.page.changelog.screens.ChangelogScreen()
            }







            composable<BackupAndRestoreScreen>(
                enterTransition = { tomatoForwardEnter() },
                popExitTransition = { tomatoPopExit() }
            ) {
                BackupAndRestoreScreen()
            }

            composable<NotificationScreen>(
                enterTransition = { tomatoForwardEnter() },
                popExitTransition = { tomatoPopExit() }
            ) {
                NotificationScreen()
            }
        }
    }
}

@Serializable
object HomeScreen

@Serializable
data class CalendarScreen(
    val subjectId: Int, val subject: String
)

@Serializable
object SettingsScreen

@Serializable
object LookAndFeelScreen

@Serializable
object CustomisationScreen

@Serializable
object FeaturesScreen

@Serializable
object AttendanceWrappedScreen

@Serializable
object DarkThemeScreen

@Serializable
object AboutScreen

@Serializable
object ChangelogScreen



@Serializable
object BehaviorScreen

@Serializable
object BackupAndRestoreScreen

@Serializable
object NotificationScreen
