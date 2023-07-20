package com.jainhardik120.passbud.ui.screen.newaccount

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.jainhardik120.passbud.ui.navigation.AppRoutes

fun NavGraphBuilder.addNewAccountScreen(hostState: SnackbarHostState) {
    composable(
        AppRoutes.NewAccount.route
    ) {
        val viewModel = hiltViewModel<NewAccountViewModel>()
        NewAccountScreen(viewModel = viewModel)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewAccountScreen(viewModel : NewAccountViewModel) {
    Column {
        var accountName by remember { mutableStateOf("") }
        var accountDescription by remember { mutableStateOf("") }

        OutlinedTextField(value = accountName, onValueChange = { accountName = it })
        OutlinedTextField(
            value = accountDescription,
            onValueChange = { accountDescription = it })
        Button(onClick = {
            viewModel.createAccount(accountName, accountDescription)
        }) {
            Text(text = "Create account")
        }
    }
}