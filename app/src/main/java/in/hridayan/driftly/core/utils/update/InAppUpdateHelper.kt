package `in`.hridayan.driftly.core.utils.update

import android.content.Context
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

sealed interface UpdateStatus {
    object Idle : UpdateStatus
    object Checking : UpdateStatus
    object Available : UpdateStatus
    object Downloading : UpdateStatus
    data class Downloaded(val completeUpdate: () -> Unit) : UpdateStatus
    object Failed : UpdateStatus
}

class InAppUpdateHelper private constructor(context: Context) {
    private val appUpdateManager: AppUpdateManager = AppUpdateManagerFactory.create(context.applicationContext)

    private val _updateStatus = MutableStateFlow<UpdateStatus>(UpdateStatus.Idle)
    val updateStatus: StateFlow<UpdateStatus> = _updateStatus.asStateFlow()

    private val installStateUpdatedListener = InstallStateUpdatedListener { state ->
        when (state.installStatus()) {
            InstallStatus.DOWNLOADING -> {
                _updateStatus.value = UpdateStatus.Downloading
            }
            InstallStatus.DOWNLOADED -> {
                _updateStatus.value = UpdateStatus.Downloaded {
                    appUpdateManager.completeUpdate()
                }
            }
            InstallStatus.FAILED -> {
                _updateStatus.value = UpdateStatus.Failed
            }
            else -> {}
        }
    }

    fun registerListener() {
        appUpdateManager.registerListener(installStateUpdatedListener)
    }

    fun unregisterListener() {
        appUpdateManager.unregisterListener(installStateUpdatedListener)
    }

    fun checkUpdateAvailability(
        onUpdateAvailable: (AppUpdateInfo) -> Unit
    ) {
        _updateStatus.value = UpdateStatus.Checking
        appUpdateManager.appUpdateInfo.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                && appUpdateInfo.isUpdateTypeAllowed(com.google.android.play.core.install.model.AppUpdateType.FLEXIBLE)
            ) {
                _updateStatus.value = UpdateStatus.Available
                onUpdateAvailable(appUpdateInfo)
            } else if (appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED) {
                _updateStatus.value = UpdateStatus.Downloaded {
                    appUpdateManager.completeUpdate()
                }
            } else {
                _updateStatus.value = UpdateStatus.Idle
            }
        }.addOnFailureListener {
            _updateStatus.value = UpdateStatus.Failed
        }
    }

    fun resumeCheck() {
        appUpdateManager.appUpdateInfo.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED) {
                _updateStatus.value = UpdateStatus.Downloaded {
                    appUpdateManager.completeUpdate()
                }
            }
        }
    }

    fun startUpdateFlow(
        appUpdateInfo: AppUpdateInfo,
        launcher: androidx.activity.result.ActivityResultLauncher<androidx.activity.result.IntentSenderRequest>
    ) {
        val options = com.google.android.play.core.appupdate.AppUpdateOptions.newBuilder(com.google.android.play.core.install.model.AppUpdateType.FLEXIBLE).build()
        appUpdateManager.startUpdateFlowForResult(appUpdateInfo, launcher, options)
    }

    companion object {
        @Volatile
        private var INSTANCE: InAppUpdateHelper? = null

        fun getInstance(context: Context): InAppUpdateHelper {
            return INSTANCE ?: synchronized(this) {
                val instance = InAppUpdateHelper(context)
                INSTANCE = instance
                instance
            }
        }
    }
}
