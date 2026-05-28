package `in`.hridayan.driftly.core.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.remember
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import `in`.hridayan.driftly.BuildConfig
import `in`.hridayan.driftly.R
import `in`.hridayan.driftly.core.common.LocalSettings
import `in`.hridayan.driftly.core.presentation.components.bottomsheet.ChangelogBottomSheet
import `in`.hridayan.driftly.core.utils.update.InAppUpdateHelper
import `in`.hridayan.driftly.core.utils.update.UpdateStatus
import `in`.hridayan.driftly.navigation.Navigation
import `in`.hridayan.driftly.settings.data.local.SettingsKeys
import `in`.hridayan.driftly.settings.presentation.viewmodel.SettingsViewModel
import kotlinx.coroutines.delay

@Composable
fun AppEntry(
    settingsViewModel: SettingsViewModel = hiltViewModel()
) {
    var showChangelogSheet by rememberSaveable { mutableStateOf(false) }
    var hasTriggeredChangelog by rememberSaveable { mutableStateOf(false) }
    var showStartupSplash by rememberSaveable { mutableStateOf(true) }
    val savedVersionCode = LocalSettings.current.savedVersionCode

    var showWelcomeSheet by rememberSaveable { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val context = androidx.compose.ui.platform.LocalContext.current

    LaunchedEffect(Unit) {
        val updateHelper = InAppUpdateHelper.getInstance(context)
        updateHelper.updateStatus.collect { status ->
            if (status is UpdateStatus.Downloaded) {
                val result = snackbarHostState.showSnackbar(
                    message = "An update has been downloaded.",
                    actionLabel = "Restart",
                    duration = SnackbarDuration.Indefinite
                )
                if (result == SnackbarResult.ActionPerformed) {
                    status.completeUpdate()
                }
            }
        }
    }

    LaunchedEffect(savedVersionCode) {
        if (!hasTriggeredChangelog && savedVersionCode != -1) { // Assuming -1 or 0 as initial load state
            if (savedVersionCode == 0) {
                showWelcomeSheet = true
            } else if (savedVersionCode < BuildConfig.VERSION_CODE) {
                showChangelogSheet = true
            }
            hasTriggeredChangelog = true
        }
    }

    Surface {
        Box(modifier = Modifier.fillMaxSize()) {
            Navigation()
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }

        if (showWelcomeSheet) {
            `in`.hridayan.driftly.core.presentation.components.bottomsheet.WelcomeBottomSheet(
                onDismiss = {
                    showWelcomeSheet = false
                    settingsViewModel.setInt(
                        SettingsKeys.SAVED_VERSION_CODE,
                        BuildConfig.VERSION_CODE
                    )
                }
            )
        }

        if (showChangelogSheet) {
            ChangelogBottomSheet(
                onDismiss = {
                    showChangelogSheet = false
                    settingsViewModel.setInt(
                        SettingsKeys.SAVED_VERSION_CODE,
                        BuildConfig.VERSION_CODE
                    )
                }
            )
        }
    }
}
