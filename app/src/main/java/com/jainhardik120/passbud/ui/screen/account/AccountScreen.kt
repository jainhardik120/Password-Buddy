package com.jainhardik120.passbud.ui.screen.account

import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.jainhardik120.passbud.data.local.entities.Credential
import com.jainhardik120.passbud.domain.BankCard
import com.jainhardik120.passbud.domain.toBankCard
import com.jainhardik120.passbud.ui.PasswordGenerator
import com.jainhardik120.passbud.ui.biometrics.BiometricPromptContainer
import com.jainhardik120.passbud.ui.biometrics.rememberPromptContainerState
import com.jainhardik120.passbud.ui.navigation.AppRoutes
import com.jainhardik120.passbud.ui.screen.home.CustomAccountCreate
import com.jainhardik120.passbud.ui.screen.home.CustomTextField
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
    var isAccountEditShown by remember { mutableStateOf(false) }
    var isDeleteAccountShown by remember { mutableStateOf(false) }
    var isDeleteCredentialShown by remember { mutableStateOf(false) }
    val promptContainerState = rememberPromptContainerState()

    val dialogState = rememberCredentialPromptState(state.account.accountId)

    CredentialPromptContainer(dialogState) { credential ->
        viewModel.saveNewKey(credential)
    }

    LaunchedEffect(key1 = state.account, block = {
        dialogState.updateAccountId(state.account.accountId)
    })

    var toBeDeletedCredential by remember {
        mutableStateOf("")
    }


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
    if (isDeleteCredentialShown) {
        AlertDialog(
            onDismissRequest = { isDeleteCredentialShown = false },
            title = { Text(text = "Confirm Delete?") },
            text = {
                Text(text = "Are you sure you want to delete this credential?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        isDeleteCredentialShown = false
                        if (toBeDeletedCredential.isNotBlank()) {
                            viewModel.deleteResource(false, toBeDeletedCredential)
                        }
                    }) {
                    Text(text = "Yes")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    isDeleteCredentialShown = false
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
            var menuExpanded by remember { mutableStateOf(false) }
            Box(modifier = Modifier.wrapContentSize(Alignment.TopStart)) {
                IconButton(
                    onClick = { menuExpanded = !menuExpanded }
                ) {
                    Icon(Icons.Rounded.MoreVert, contentDescription = "More Icon")
                }
                DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                    DropdownMenuItem(text = { Text(text = "Delete Account") }, onClick = {
                        menuExpanded = false
                        isDeleteAccountShown = true
                    }, leadingIcon = {
                        Icon(Icons.Rounded.Delete, contentDescription = "Delete Icon")
                    })
                    DropdownMenuItem(text = { Text(text = "Edit Details") }, onClick = {
                        menuExpanded = false
                        isAccountEditShown = true
                    }, leadingIcon = {
                        Icon(Icons.Rounded.Edit, contentDescription = "Edit Icon")
                    })
                }
            }

        })
    }, floatingActionButton = {
        FloatingActionButton(onClick = { dialogState.showPrompt(true, 0, null) }) {
            Icon(Icons.Rounded.Add, contentDescription = "Add Icon")
        }
    }, snackbarHost = {
        SnackbarHost(
            hostState = hostState
        )
    }) { paddingValues ->
        Column(Modifier.padding(paddingValues)) {
            LazyColumn(content = {
                itemsIndexed(state.credentials) { index, item ->
                    Surface(
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .clickable(enabled = item.first.isEncrypted && !item.second) {
                                viewModel.decryptCredentialAt(index)
                            },
                        color = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp)
                    ) {
                        Row(Modifier.fillMaxWidth()) {

                            Column(
                                Modifier
                                    .padding(16.dp)
                                    .weight(1f)
                                    .fillMaxWidth()
                            ) {
                                Row(
                                    Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = item.first.credentialKey,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }
                                when {
                                    !item.first.isEncrypted -> {
                                        Text(text = item.first.credentialValue)
                                    }

                                    item.first.isEncrypted && !item.second -> {
                                        Text(text = "Tap to reveal")
                                    }

                                    item.first.isEncrypted && item.second -> {
                                        when (item.first.credentialType) {
                                            0 -> {
                                                Text(text = item.first.credentialValue)
                                            }

                                            1 -> {
                                                val card by remember(item.first.credentialValue) {
                                                    mutableStateOf(item.first.credentialValue.toBankCard())
                                                }
                                                card?.let { bankCard ->
                                                    Text(text = bankCard.cardNumber)
                                                    bankCard.validFrom?.let {
                                                        Text(text = it)
                                                    }
                                                    bankCard.validThru?.let {
                                                        Text(text = it)
                                                    }
                                                    bankCard.cvv?.let {
                                                        Text(text = it)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            val shouldShowOptions by remember(item) { mutableStateOf(!item.first.isEncrypted || item.second) }
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                var menuExpanded by remember { mutableStateOf(false) }
                                Box(modifier = Modifier.wrapContentSize(Alignment.TopStart)) {
                                    IconButton(enabled = shouldShowOptions, onClick = {
                                        menuExpanded = !menuExpanded
                                    }) {
                                        Icon(
                                            if (shouldShowOptions) {
                                                Icons.Rounded.MoreVert
                                            } else {
                                                Icons.Rounded.Lock
                                            },
                                            contentDescription = "Options Icon"
                                        )
                                    }
                                    DropdownMenu(
                                        expanded = menuExpanded,
                                        onDismissRequest = { menuExpanded = false }) {
                                        DropdownMenuItem(
                                            text = { Text(text = "Delete") },
                                            onClick = {
                                                menuExpanded = false
                                                toBeDeletedCredential = item.first.credentialId
                                                isDeleteCredentialShown = true
                                            },
                                            leadingIcon = {
                                                Icon(
                                                    Icons.Rounded.Delete,
                                                    contentDescription = "Delete Icon"
                                                )
                                            })
                                        DropdownMenuItem(text = { Text(text = "Edit") }, onClick = {
                                            menuExpanded = false
                                            dialogState.showPrompt(
                                                false,
                                                item.first.credentialType,
                                                item.first
                                            )
                                        }, leadingIcon = {
                                            Icon(
                                                Icons.Rounded.Edit,
                                                contentDescription = "Edit Icon"
                                            )
                                        })
                                    }
                                }

                            }
                        }
                    }
                }
            }
            )
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

@Composable
fun UsernamePasswordAccount(
    onCancel: () -> Unit,
    onConfirmCreate: (String, String, Boolean) -> Unit,
    initialName: String = "",
    initialPass: String = "",
    shouldShowEncryptButton: Boolean = true,
    confirmButtonText: String = "Create"
) {
    var username by remember { mutableStateOf(initialName) }
    var password by remember { mutableStateOf(initialPass) }
    var isEncrypted by remember {
        mutableStateOf(true)
    }
    val spacing = 20.dp
    Column(
        Modifier
            .padding(spacing)
            .fillMaxWidth()
    ) {
        if (shouldShowEncryptButton) {
            Switch(checked = isEncrypted, onCheckedChange = { isEncrypted = it })
        }
        CustomTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text(text = "Key") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(spacing))
        CustomTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text(text = "Value") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        PasswordGenerator(onPasswordGenerated = { password = it }, shouldGeneratePassword = false)
        Spacer(modifier = Modifier.height(spacing))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            OutlinedButton(onClick = { onCancel() }) {
                Text(text = "Cancel")
            }
            Spacer(modifier = Modifier.width(spacing))
            Button(
                onClick = {
                    onConfirmCreate(
                        username,
                        password,
                        isEncrypted
                    )
                },
                enabled = username.isNotEmpty() && password.isNotEmpty()
            ) {
                Text(text = confirmButtonText)
            }
        }
    }
}


@Composable
fun ATMCardCreate(
    onCancel: () -> Unit,
    onConfirmCreate: ((String, String) -> Unit),
    name: String = "",
    number: String = "",
    from: String = "",
    to: String = "",
    icvv: String = "",
    confirmButtonText: String = "Create"
) {
    var cardName by remember { mutableStateOf(name) }
    var cardNumber by remember { mutableStateOf(number) }
    var cvv by remember { mutableStateOf(icvv) }
    var validFrom by remember { mutableStateOf(from) }
    var validThru by remember { mutableStateOf(to) }
    val spacing = 20.dp
    val validInputRegex: Regex = "[0-9]+".toRegex()
    Column(
        Modifier
            .padding(spacing)
            .fillMaxWidth()
    ) {
        CustomTextField(
            value = cardName,
            onValueChange = { cardName = it },
            label = { Text(text = "Card Name") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(spacing))
        Row {
            CustomTextField(
                value = cardNumber,
                onValueChange = {
                    if (it.isEmpty() || (it.length <= 16 && validInputRegex.matches(it))) {
                        cardNumber = it
                    }
                },
                label = { Text(text = "Number") },
                modifier = Modifier
                    .weight(6f)
                    .fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
            )
            Spacer(modifier = Modifier.width(spacing))
            CustomTextField(
                value = cvv,
                onValueChange = {
                    if (it.isEmpty() || (it.length <= 3 && validInputRegex.matches(it))) {
                        cvv = it
                    }
                },
                label = { Text(text = "CVV") },
                modifier = Modifier
                    .weight(3f)
                    .fillMaxWidth(),
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                singleLine = true,
                placeholder = { Text(text = "123") }
            )
        }

        Spacer(modifier = Modifier.height(spacing))
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {

            CustomTextField(
                value = validFrom,
                onValueChange = {
                    if (it.isEmpty() || (it.length <= 4 && validInputRegex.matches(it))) {
                        validFrom = it
                    }
                },
                label = { Text(text = "Valid From") },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                singleLine = true,
                placeholder = { Text(text = "MMYY") }
            )
            Spacer(modifier = Modifier.width(spacing))
            CustomTextField(
                value = validThru,
                onValueChange = {
                    if (it.isEmpty() || (it.length <= 4 && validInputRegex.matches(it))) {
                        validThru = it
                    }
                },
                label = { Text(text = "Valid Thru") },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                singleLine = true,
                placeholder = { Text(text = "MMYY") }
            )

        }
        Spacer(modifier = Modifier.height(spacing))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            OutlinedButton(onClick = { onCancel() }) {
                Text(text = "Cancel")
            }
            Spacer(modifier = Modifier.width(spacing))
            Button(
                onClick = {
                    onConfirmCreate(
                        cardName,
                        BankCard(cardNumber, validFrom, validThru, cvv).formatToString()
                    )
                },
                enabled = cardName.isNotEmpty() && cardNumber.isNotEmpty()
            ) {
                Text(text = confirmButtonText)
            }
        }
    }
}


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



