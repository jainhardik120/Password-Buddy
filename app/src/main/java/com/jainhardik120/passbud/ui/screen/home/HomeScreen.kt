package com.jainhardik120.passbud.ui.screen.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
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
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = {navigate(AppRoutes.NewAccount.route)}) {
                Icon(Icons.Rounded.Add, contentDescription = "Add Icon")
            }
        }, snackbarHost = {
            SnackbarHost(
                hostState = hostState
            )
        }) {
        Column(Modifier.padding(it)) {
            val state by viewModel.state
            when (state.appStatus) {
                AppStatus.READY -> {
                    LazyColumn(content = {
                        itemsIndexed(state.accounts) { _, item ->
                            Column(Modifier.clickable {
                                navigate(AppRoutes.AccountScreen.withArgs(item.accountId))
                            }) {
                                Text(text = item.accountName)
                                Text(text = item.accountDescription)
                                Divider()
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