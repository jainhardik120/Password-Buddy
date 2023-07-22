package com.jainhardik120.passbud.ui.screen.account

import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.jainhardik120.passbud.ui.biometrics.BiometricPromptContainer
import com.jainhardik120.passbud.ui.biometrics.rememberPromptContainerState
import com.jainhardik120.passbud.ui.navigation.AppRoutes
import com.jainhardik120.passbud.ui.screen.home.CustomAccountCreate
import com.jainhardik120.passbud.util.CryptoPurpose

fun NavGraphBuilder.addAccountScreen(
    hostState: SnackbarHostState,
    navController: NavHostController
) {
    composable(
        AppRoutes.AccountScreen.route + "/{accountId}", arguments = listOf(
            navArgument("accountId") {
                type = NavType.StringType
                nullable = false
            }
        )
    ) {
        val viewModel = hiltViewModel<AccountViewModel>()
        AccountScreen(viewModel = viewModel, hostState, navController)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountScreen(
    viewModel: AccountViewModel,
    hostState: SnackbarHostState,
    navController: NavHostController
) {

    LaunchedEffect(key1 = Unit, block = {
        viewModel.uiEvent.collect {
            when (it) {
                is UiEvent.Navigate -> {
                    navController.navigate(it.route)
                }

                UiEvent.NavigateUp -> {
                    navController.navigateUp()
                }
            }
        }
    })

    val state by viewModel.state
    var bottomSheetExpanded by remember { mutableStateOf(false) }
    var isAccountEditShown by remember { mutableStateOf(false) }
    var isDeleteAccountShown by remember { mutableStateOf(false) }
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

    if (isDeleteAccountShown) {
        AlertDialog(
            onDismissRequest = { isDeleteAccountShown = false },
            title = { Text(text = "Confirm Delete?") },
            text = {
                Text(text = "Deleting this account would also delete ${state.credentials.size} saved credentials. Are you sure you want to delete this account?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        isDeleteAccountShown = false
                        viewModel.deleteResource(true, state.account.accountId)
                    }) {
                    Text(text = "Yes")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    isDeleteAccountShown = false
                }) {
                    Text(text = "Cancel")
                }
            })
    }

    if (isAccountEditShown) {
        AlertDialog(
            onDismissRequest = {
                isAccountEditShown = false
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
                    CustomAccountCreate(
                        onCancel = { isAccountEditShown = false },
                        onConfirmCreate = { name, desc ->
                            isAccountEditShown = false
                            viewModel.updateAccountDetails(name, desc)
                        },
                        confirmButtonText = "Save",
                        initialName = state.account.accountName,
                        initialDescription = state.account.accountDescription
                    )
                }
            }
        }
    }

    state.authContext?.let { auth ->
        LaunchedEffect(key1 = auth) {
            val promptInfo = createPromptInfo(auth.purpose)
            promptContainerState.authenticate(promptInfo, auth.cryptoObject)
        }
    }

    state.shouldShowDeletePrompt?.let { request ->
        LaunchedEffect(key1 = request) {
            val promptInfo = BiometricPrompt.PromptInfo.Builder()
                .setTitle("Biometric verification")
                .setSubtitle(
                    "Verify yourself to delete this ${
                        if (request.isAccount) {
                            "account"
                        } else {
                            "credential"
                        }
                    }"
                )
                .setNegativeButtonText("Cancel")
                .build()
            promptContainerState.authenticate(promptInfo, null)
        }
    }



    Scaffold(topBar = {
        CenterAlignedTopAppBar(title = { Text(text = state.account.accountName) }, actions = {
            IconButton(onClick = {
                isAccountEditShown = true
            }) {
                Icon(Icons.Rounded.Edit, contentDescription = "Edit Icon")
            }
            IconButton(onClick = {
                isDeleteAccountShown = true
            }) {
                Icon(Icons.Rounded.Delete, contentDescription = "Delete Icon")
            }
        })
    }, floatingActionButton = {
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