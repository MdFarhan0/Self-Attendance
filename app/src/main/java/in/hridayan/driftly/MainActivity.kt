package `in`.hridayan.driftly

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import android.os.Build
import android.view.WindowManager
import android.view.Window
import dagger.hilt.android.AndroidEntryPoint
import `in`.hridayan.driftly.core.common.CompositionLocals
import `in`.hridayan.driftly.core.common.LocalSeedColor
import `in`.hridayan.driftly.core.domain.provider.SeedColorProvider
import `in`.hridayan.driftly.core.presentation.AppEntry
import `in`.hridayan.driftly.core.presentation.theme.DriftlyTheme
import `in`.hridayan.driftly.settings.presentation.viewmodel.SettingsViewModel
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val settingsViewModel: SettingsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Handle cutout mode for Edge-to-Edge display
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window.attributes.layoutInDisplayCutoutMode = if (Build.VERSION.SDK_INT >= 35) {
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS
            } else {
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            }
        }

        // Disable contrast enforcement for truly transparent navigation bar on API 29+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }

        enableEdgeToEdge()
        setContent {
            CompositionLocals {
                SeedColorProvider.setSeedColor(LocalSeedColor.current)

                DriftlyTheme {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        AppEntry()
                    }
                }
            }
        }
    }
}


