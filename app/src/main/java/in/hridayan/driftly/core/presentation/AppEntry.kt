package `in`.hridayan.driftly.core.presentation

import android.util.Log
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import `in`.hridayan.driftly.BuildConfig
import `in`.hridayan.driftly.core.common.LocalSettings
import `in`.hridayan.driftly.core.presentation.components.bottomsheet.ChangelogBottomSheet
import `in`.hridayan.driftly.navigation.Navigation
import `in`.hridayan.driftly.settings.data.local.SettingsKeys
import `in`.hridayan.driftly.settings.presentation.viewmodel.SettingsViewModel

@Composable
fun AppEntry(
    settingsViewModel: SettingsViewModel = hiltViewModel()
) {
    var showChangelogSheet by rememberSaveable { mutableStateOf(false) }
    val savedVersionCode = LocalSettings.current.savedVersionCode

    LaunchedEffect(savedVersionCode) {
        showChangelogSheet = savedVersionCode < BuildConfig.VERSION_CODE
    }

    Surface {
        Navigation()

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
