package com.jainhardik120.passbud.ui.screen.home

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jainhardik120.passbud.data.local.entities.CredentialAccount
import com.jainhardik120.passbud.domain.BiometricAuthStatus
import com.jainhardik120.passbud.domain.BiometricInfo
import com.jainhardik120.passbud.domain.CredentialsRepository
import com.jainhardik120.passbud.domain.KeyStatus
import com.jainhardik120.passbud.ui.snackbar.SnackbarManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val credentialsRepository: CredentialsRepository
) : ViewModel() {

    private val _state = mutableStateOf(HomeState())
    val state: State<HomeState> = _state

    init {
        credentialsRepository.retrieveAccountsList().onEach {
            _state.value = state.value.copy(accounts = it)
        }.launchIn(viewModelScope)
        viewModelScope.launch {
            val biometricInfo = credentialsRepository.getBiometricInfo()
            reduceBiometricInfo(biometricInfo)
        }
    }

    private fun reduceBiometricInfo(info: BiometricInfo) {
        _state.value =
            _state.value.copy(
                appStatus = if (info.biometricAuthStatus == BiometricAuthStatus.READY && info.keyStatus != KeyStatus.NOT_READY) {
                    AppStatus.READY
                } else if (info.biometricAuthStatus == BiometricAuthStatus.NOT_AVAILABLE) {
                    AppStatus.UNAVAILABLE
                } else {
                    AppStatus.NOT_READY
                }
            )
        if (info.keyStatus == KeyStatus.INVALIDATED) {
            SnackbarManager.showMessage("Your key was invalidated and app data is reset")
        }
    }
}

data class HomeState(
    val accounts: List<CredentialAccount> = emptyList(),
    val appStatus: AppStatus = AppStatus.READY
)

enum class AppStatus {
    READY,
    NOT_READY,
    UNAVAILABLE
}