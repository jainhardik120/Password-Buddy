package com.jainhardik120.passbud.ui.screen.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jainhardik120.passbud.ui.PasswordGenerator

@Composable
fun UsernamePasswordAccount(
    onCancel: () -> Unit,
    onConfirmCreate: (String, String, String, String) -> Unit
) {
    var accountName by remember { mutableStateOf("") }
    var accountDescription by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val spacing = 20.dp
    Column(
        Modifier.Companion
            .padding(spacing)
            .fillMaxWidth()
    ) {
        CustomTextField(
            value = accountName,
            onValueChange = { accountName = it },
            label = { Text(text = "Account Name") },
            modifier = Modifier.Companion.fillMaxWidth(),
            singleLine = true
        )
        Spacer(modifier = Modifier.Companion.height(spacing))
        CustomTextField(
            value = accountDescription,
            onValueChange = { accountDescription = it },
            label = { Text(text = "Description (optional)") },
            modifier = Modifier.Companion.fillMaxWidth()
        )
        Spacer(modifier = Modifier.Companion.height(spacing))
        CustomTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text(text = "Username") },
            modifier = Modifier.Companion.fillMaxWidth(),
            singleLine = true
        )
        Spacer(modifier = Modifier.Companion.height(spacing))
        CustomTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text(text = "Password") },
            modifier = Modifier.Companion.fillMaxWidth(),
            singleLine = true
        )
        PasswordGenerator(onPasswordGenerated = { password = it })
        Spacer(modifier = Modifier.Companion.height(spacing))
        Row(Modifier.Companion.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            OutlinedButton(onClick = { onCancel() }) {
                Text(text = "Cancel")
            }
            Spacer(modifier = Modifier.Companion.width(spacing))
            Button(
                onClick = {
                    onConfirmCreate(
                        accountName,
                        accountDescription,
                        username,
                        password
                    )
                },
                enabled = accountName.isNotEmpty() && username.isNotEmpty() && password.isNotEmpty()
            ) {
                Text(text = "Create")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun UsernamePasswordPreview() {
    UsernamePasswordAccount(onCancel = {}, onConfirmCreate = { _, _, _, _ -> })
}
