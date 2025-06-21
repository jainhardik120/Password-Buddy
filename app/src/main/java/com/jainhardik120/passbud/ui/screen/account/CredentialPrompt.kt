package com.jainhardik120.passbud.ui.screen.account

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.jainhardik120.passbud.data.local.entities.Credential
import com.jainhardik120.passbud.domain.BankCard
import com.jainhardik120.passbud.domain.toBankCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CredentialPromptContainer(state: CredentialPrompt, onConfirm: (Credential) -> Unit) {
    if (state.isPromptToShow.value) {
        AlertDialog(
            onDismissRequest = {
                state.resetShowFlag()
            },
            properties = DialogProperties(
                dismissOnBackPress = false,
                dismissOnClickOutside = false,
                usePlatformDefaultWidth = false
            ),
            modifier = Modifier.padding(14.dp)
        ) {
            Surface(
                shape = AlertDialogDefaults.shape,
                color = AlertDialogDefaults.containerColor,
                tonalElevation = AlertDialogDefaults.TonalElevation,
            ) {
                Column {
                    when (state.type) {
                        0 -> {
                            UsernamePasswordAccount(
                                onCancel = { state.resetShowFlag() },
                                onConfirmCreate = { key, value, isEncrypted ->
                                    if (!state.isNew) {
                                        state.credential?.let {
                                            onConfirm(
                                                it.copy(
                                                    credentialKey = key,
                                                    credentialValue = value
                                                )
                                            )
                                        }
                                    } else {
                                        onConfirm(
                                            Credential(
                                                credentialType = 0,
                                                credentialKey = key,
                                                credentialValue = value,
                                                accountId = state.accountId,
                                                credentialId = "",
                                                isEncrypted = isEncrypted,
                                                encryptionIv = null
                                            )
                                        )
                                    }
                                    state.resetShowFlag()
                                },
                                initialName = if (state.isNew) {
                                    ""
                                } else {
                                    state.credential?.credentialKey ?: ""
                                },
                                initialPass = if (state.isNew) {
                                    ""
                                } else {
                                    state.credential?.credentialValue ?: ""
                                },
                                confirmButtonText = if (state.isNew) {
                                    "Create"
                                } else {
                                    "Save"
                                }, shouldShowEncryptButton = state.isNew
                            )
                        }

                        1 -> {
                            val bankCard = remember {
                                if (!state.isNew) {
                                    state.credential?.credentialValue?.toBankCard() ?: BankCard(
                                        "",
                                        "",
                                        "",
                                        ""
                                    )
                                } else {
                                    BankCard("", "", "", "")
                                }
                            }
                            ATMCardCreate(
                                onCancel = { state.resetShowFlag() },
                                onConfirmCreate = { key, value ->
                                    state.credential?.let {
                                        onConfirm(
                                            it.copy(
                                                credentialKey = key,
                                                credentialValue = value
                                            )
                                        )
                                    }
                                    if (state.isNew) {
                                        onConfirm(
                                            Credential(
                                                credentialType = 1,
                                                credentialKey = key,
                                                credentialValue = value,
                                                accountId = state.accountId,
                                                credentialId = "",
                                                isEncrypted = true,
                                                encryptionIv = null
                                            )
                                        )
                                    }
                                    state.resetShowFlag()
                                },
                                name = if (state.isNew) {
                                    ""
                                } else {
                                    state.credential?.credentialKey ?: ""
                                },
                                number = bankCard.cardNumber,
                                from = bankCard.validFrom ?: "",
                                to = bankCard.validThru ?: "",
                                icvv = bankCard.cvv ?: "",
                                confirmButtonText = if (state.isNew) {
                                    "Create"
                                } else {
                                    "Save"
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

class CredentialPrompt(var accountId: String) {
    private var _isNew: Boolean = true
    private var _type: Int = 0
    private var _credential: Credential? = null

    var isNew: Boolean
        get() = _isNew
        private set(value) {
            _isNew = value
        }

    var type: Int
        get() = _type
        private set(value) {
            _type = value
        }

    var credential: Credential?
        get() = _credential
        private set(value) {
            _credential = value
        }

    private val _isPromptToShow = mutableStateOf(false)
    val isPromptToShow: State<Boolean> = _isPromptToShow

    fun updateAccountId(accountId: String) {
        this.accountId = accountId
    }

    fun showPrompt(isNew: Boolean, type: Int, credential: Credential? = null) {
        this.isNew = isNew
        this.type = type
        this.credential = credential
        _isPromptToShow.value = true
    }

    fun resetShowFlag() {
        _isPromptToShow.value = false
    }
}

@Composable
fun rememberCredentialPromptState(accountId: String): CredentialPrompt = remember {
    CredentialPrompt(accountId)
}