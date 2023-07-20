package com.jainhardik120.passbud.ui.screen.account

import android.content.res.Resources
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.jainhardik120.passbud.ui.biometrics.BiometricPromptContainer
import com.jainhardik120.passbud.ui.biometrics.rememberPromptContainerState
import com.jainhardik120.passbud.ui.navigation.AppRoutes
import com.jainhardik120.passbud.util.CryptoPurpose

fun NavGraphBuilder.addAccountScreen(hostState: SnackbarHostState) {
    composable(
        AppRoutes.AccountScreen.route + "/{accountId}", arguments = listOf(
            navArgument("accountId") {
                type = NavType.StringType
                nullable = false
            }
        )
    ) {
        val viewModel = hiltViewModel<AccountViewModel>()
        AccountScreen(viewModel = viewModel, hostState)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountScreen(viewModel: AccountViewModel, hostState: SnackbarHostState) {
    val state by viewModel.state
    var bottomSheetExpanded by remember { mutableStateOf(false) }


    val promptContainerState = rememberPromptContainerState()
    BiometricPromptContainer(
        promptContainerState,
        onAuthSucceeded = { cryptoObj ->
            viewModel.onAuthSucceeded(cryptoObj)
        },
        onAuthError = { authErr ->
            viewModel.onAuthError(authErr.errorCode, authErr.errString)
        }
    )

    state.authContext?.let { auth ->
        LaunchedEffect(key1 = auth) {
            val promptInfo = createPromptInfo(auth.purpose)
            promptContainerState.authenticate(promptInfo, auth.cryptoObject)
        }
    }

    Scaffold(floatingActionButton = {
        FloatingActionButton(onClick = { bottomSheetExpanded = true }) {
            Icon(Icons.Rounded.Add, contentDescription = "Add Icon")
        }
    }, snackbarHost = {
        SnackbarHost(
            hostState = hostState
        )
    }) {
        Column(Modifier.padding(it)) {
            LazyColumn(content = {
                itemsIndexed(state.credentials) { index, item ->
                    Column(Modifier.clickable {
                        viewModel.decryptCredentialAt(index)
                    }) {

                    Text(text = item.credentialKey)
                    Text(text = item.credentialType.toString())
                    Text(text = item.credentialValue)
                    }
                }
            })
        }
    }
    if (bottomSheetExpanded) {
        ModalBottomSheet(onDismissRequest = { bottomSheetExpanded = false }) {
            OutlinedTextField(value = state.editCredentialKey, onValueChange = {
                viewModel.onEvent(
                    AccountEvent.EditCredentialKeyChanged(it)
                )
            })
            OutlinedTextField(value = state.editCredentialValue, onValueChange = {
                viewModel.onEvent(
                    AccountEvent.EditCredentialValueChanged(it)
                )
            })
            Switch(
                checked = state.editCredentialEncrypted,
                onCheckedChange = { viewModel.onEvent(AccountEvent.EditCredentialEncryptedChanged(it)) })
            Button(onClick = {
                viewModel.saveNewKey()
            }) {
                Text(text = "Save")
            }
        }
    }
}

fun createPromptInfo(purpose: CryptoPurpose): BiometricPrompt.PromptInfo {
    return if (purpose == CryptoPurpose.Encryption) {
        BiometricPrompt.PromptInfo.Builder()
            .setTitle("Biometric verification")
            .setSubtitle("Verify yourself to encrypt and store data")
            .setNegativeButtonText("Cancel")
            .build()
    } else {
        BiometricPrompt.PromptInfo.Builder()
            .setTitle("Biometric verification")
            .setSubtitle("Verify yourself to view credentials")
            .setNegativeButtonText("Cancel")
            .build()
    }
}