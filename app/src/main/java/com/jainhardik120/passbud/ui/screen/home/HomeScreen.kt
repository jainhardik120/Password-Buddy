package com.jainhardik120.passbud.ui.screen.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.jainhardik120.passbud.R
import com.jainhardik120.passbud.ui.biometrics.BiometricPromptContainer
import com.jainhardik120.passbud.ui.biometrics.createPromptInfo
import com.jainhardik120.passbud.ui.biometrics.rememberPromptContainerState
import com.jainhardik120.passbud.ui.navigation.AppRoutes

fun NavGraphBuilder.addHomeScreen(hostState: SnackbarHostState, navigate: (String) -> Unit) {
    composable(
        AppRoutes.Home.route
    ) {
        val viewModel = hiltViewModel<HomeViewModel>()
        HomeScreen(viewModel = viewModel, navigate, hostState)
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(viewModel: HomeViewModel, navigate: (String) -> Unit, hostState: SnackbarHostState) {
    var sheetExpanded by remember { mutableStateOf(false) }
    val state by viewModel.state
    val promptContainerState = rememberPromptContainerState()
    BiometricPromptContainer(promptContainerState, onAuthSucceeded = { cryptoObj ->
        viewModel.onAuthSucceeded(cryptoObj)
    }, onAuthError = { authErr ->
        viewModel.onAuthError(authErr.errorCode, authErr.errString)
    })

    state.authContext?.let { auth ->
        LaunchedEffect(key1 = auth) {
            promptContainerState.authenticate(createPromptInfo(auth.purpose), auth.cryptoObject)
        }
    }

    val topAppBarScrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    Scaffold(
        topBar = {

            LargeTopAppBar(title = {
                Text(
                    text = LocalContext.current.resources.getString(
                        R.string.app_name
                    )
                )
            }, scrollBehavior = topAppBarScrollBehavior)
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { sheetExpanded = !sheetExpanded }) {
                Icon(Icons.Rounded.Add, contentDescription = "Add Icon")
            }
        },
        snackbarHost = {
            SnackbarHost(
                hostState = hostState
            )
        },
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(topAppBarScrollBehavior.nestedScrollConnection)
    ) { paddingValues ->
        Column(Modifier.padding(paddingValues)) {
            AccountCreationDialog(
                isShown = sheetExpanded,
                changeVisibility = { sheetExpanded = it },
                viewModel::createAccount,
                viewModel::createAccountAndCredentials,
                viewModel::createAccountAndCard
            )
            when (state.appStatus) {
                AppStatus.READY -> {
                    LazyColumn(content = {
                        itemsIndexed(state.accounts) { _, item ->
                            Surface(
                                Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .clickable {
                                        navigate(AppRoutes.AccountScreen.withArgs(item.accountId))
                                    }, color = MaterialTheme.colorScheme.secondaryContainer
                            ) {
                                Column(
                                    Modifier
                                        .padding(16.dp)
                                        .fillMaxWidth()
                                ) {
                                    Text(
                                        text = item.accountName,
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))

                                    fun buildAccompanyingText(
                                        encryptedCount: Int, nonEncryptedCount: Int, cardsCount: Int
                                    ): String {
                                        val counts = mutableListOf<String>()
                                        if (encryptedCount > 0) {
                                            counts.add("$encryptedCount Locked Value")
                                        }
                                        if (nonEncryptedCount > 0) {
                                            counts.add("$nonEncryptedCount Open Value")
                                        }
                                        if (cardsCount > 0) {
                                            counts.add("$cardsCount Bank Card")
                                        }
                                        if (counts.isEmpty()) {
                                            return "No Credentials"
                                        }
                                        return counts.joinToString(", ")
                                    }

                                    val accompanyingText by remember(
                                        item.encryptedCount, item.nonEncryptedCount, item.cardsCount
                                    ) {
                                        mutableStateOf(
                                            buildAccompanyingText(
                                                item.encryptedCount,
                                                item.nonEncryptedCount,
                                                item.cardsCount
                                            )
                                        )
                                    }
                                    Text(accompanyingText)
                                }
                            }
                        }
                    })
                }

                AppStatus.NOT_READY -> {
                    Text(text = "Service not available at the moment")
                }

                AppStatus.UNAVAILABLE -> {
                    Text(text = "Service not available in your device")
                }
            }

        }
    }
}