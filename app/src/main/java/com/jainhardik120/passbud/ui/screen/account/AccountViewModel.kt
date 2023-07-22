package com.jainhardik120.passbud.ui.screen.account

import androidx.biometric.BiometricPrompt
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jainhardik120.passbud.data.local.entities.Credential
import com.jainhardik120.passbud.data.local.entities.CredentialAccount
import com.jainhardik120.passbud.domain.CredentialsRepository
import com.jainhardik120.passbud.ui.snackbar.SnackbarManager
import com.jainhardik120.passbud.util.CryptoPurpose
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AccountViewModel @Inject constructor(
    private val credentialsRepository: CredentialsRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {



    private val _uiEvent = Channel<UiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()


    private fun sendUiEvent(event: UiEvent) {
        viewModelScope.launch {
            _uiEvent.send(event)
        }
    }

    private val _state = mutableStateOf(AccountState())
    val state: State<AccountState> = _state

    private var _decryptIndex = -1



    init {
        val accountId = savedStateHandle.get<String>("accountId")
        if (accountId == null) {
            sendUiEvent(UiEvent.NavigateUp)
        } else {
            credentialsRepository.getAccountDetails(accountId).onEach {
                if (it == null) {
                    sendUiEvent(UiEvent.NavigateUp)
                } else {
                    _state.value = _state.value.copy(account = it)
                }
            }.launchIn(viewModelScope)
            credentialsRepository.getAccountCredentials(accountId).onEach {
                _state.value = _state.value.copy(credentials = it)
            }.launchIn(viewModelScope)
        }
    }

    fun onEvent(event: AccountEvent) {
        when (event) {
            is AccountEvent.EditCredentialEncryptedChanged -> {
                _state.value = _state.value.copy(editCredentialEncrypted = event.encrypted)
            }

            is AccountEvent.EditCredentialKeyChanged -> {
                _state.value = _state.value.copy(editCredentialKey = event.key)
            }

            is AccountEvent.EditCredentialTypeChanged -> {
                _state.value = _state.value.copy(editCredentialType = event.type)
            }

            is AccountEvent.EditCredentialValueChanged -> {
                _state.value = _state.value.copy(editCredentialValue = event.value)
            }
        }
    }

    fun saveNewKey() {
        viewModelScope.launch {
            if (_state.value.editCredentialEncrypted) {
                try {
                    _state.value =
                        _state.value.copy(authContext = prepareAuthContext(CryptoPurpose.Encryption))
                } catch (e: Exception) {
                    showMessage(e.message ?: "Error Occurred")
                }
            } else {
                saveCredential()
            }
        }
    }

    fun deleteResource(isAccount: Boolean, resourceId: String) {
        _state.value =
            _state.value.copy(shouldShowDeletePrompt = DeleteRequest(isAccount, resourceId))
    }

    fun onAuthSucceeded(cryptoObject: BiometricPrompt.CryptoObject?) {
        val pendingAuthContext = _state.value.authContext
        val pendingDeleteRequest = _state.value.shouldShowDeletePrompt
        _state.value = _state.value.copy(authContext = null, shouldShowDeletePrompt = null)
        viewModelScope.launch {
            pendingAuthContext?.let { authContext ->
                if (authContext.purpose == CryptoPurpose.Encryption) {
                    saveCredential(cryptoObject)
                } else {
                    if (cryptoObject != null) {
                        decryptCredential(cryptoObject)
                    }
                    _decryptIndex = -1
                }
            }
            pendingDeleteRequest?.let {
                println("Delete Request Approved")
                if (it.isAccount) {
                    credentialsRepository.deleteAccount(it.deleteObjectId)
                } else {
                    credentialsRepository.deleteCredential(it.deleteObjectId)
                }
            }
        }
    }

    private suspend fun decryptCredential(cryptoObject: BiometricPrompt.CryptoObject) {
        try {
            val decrypted = credentialsRepository.decryptCredential(
                _state.value.credentials[_decryptIndex].credentialValue,
                cryptoObject
            )
            _state.value =
                _state.value.copy(credentials = _state.value.credentials.mapIndexed { index, credential ->
                    if (index != _decryptIndex) credential else credential.copy(credentialValue = decrypted)
                })
        } catch (e: Exception) {
            println(e.printStackTrace())
            showMessage(e.message ?: "Error Occurred")
        }
    }

    private suspend fun saveCredential(cryptoObject: BiometricPrompt.CryptoObject? = null) {
        try {
            val state = _state.value
            credentialsRepository.saveCredential(
                Credential(
                    accountId = state.account.accountId,
                    credentialId = state.editCredentialId,
                    credentialKey = state.editCredentialKey,
                    credentialValue = state.editCredentialValue,
                    credentialType = state.editCredentialType,
                    isEncrypted = state.editCredentialEncrypted,
                    encryptionIv = null
                ), cryptoObject
            )
        } catch (e: Exception) {
            showMessage(e.message ?: "Error Occurred")
        }
    }


    private suspend fun prepareAuthContext(
        purpose: CryptoPurpose,
        ivString: String? = null
    ): AuthContext {
        val cryptoObject = credentialsRepository.createCryptoObject(purpose, ivString)
        return AuthContext(
            purpose = purpose,
            cryptoObject = cryptoObject
        )
    }

    fun updateAccountDetails(name: String, description: String) {
        viewModelScope.launch {
            credentialsRepository.updateAccountDetails(
                _state.value.account.copy(
                    accountName = name,
                    accountDescription = description
                )
            )
        }
    }


    private fun showMessage(message: String) {
        SnackbarManager.showMessage(message)
    }

    fun onAuthError(errorCode: Int, errString: String) {
        when (errorCode) {
            BiometricPrompt.ERROR_USER_CANCELED,
            BiometricPrompt.ERROR_CANCELED,
            BiometricPrompt.ERROR_NEGATIVE_BUTTON -> {

            }

            else -> {
                showMessage(errString)
            }
        }
        if (_state.value.authContext?.purpose == CryptoPurpose.Decryption) {
            _decryptIndex = -1
        }
        _state.value = _state.value.copy(authContext = null, shouldShowDeletePrompt = null)
    }

    fun decryptCredentialAt(index: Int) {
        _decryptIndex = index
        viewModelScope.launch {
            try {
                _state.value =
                    _state.value.copy(
                        authContext = prepareAuthContext(
                            CryptoPurpose.Decryption,
                            _state.value.credentials[index].encryptionIv
                        )
                    )
            } catch (e: Exception) {
                e.printStackTrace()
                println(e.printStackTrace())
                showMessage(e.message ?: "Error Occurred")
            }
        }
    }
}


sealed class UiEvent {
    data class Navigate(val route: String) : UiEvent()
    object NavigateUp : UiEvent()
}

sealed class AccountEvent() {
    data class EditCredentialKeyChanged(val key: String) : AccountEvent()
    data class EditCredentialValueChanged(val value: String) : AccountEvent()
    data class EditCredentialTypeChanged(val type: Int) : AccountEvent()
    data class EditCredentialEncryptedChanged(val encrypted: Boolean) : AccountEvent()
}