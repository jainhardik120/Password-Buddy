package com.jainhardik120.passbud.ui.screen.account

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
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.jainhardik120.passbud.ui.PasswordGenerator
import com.jainhardik120.passbud.ui.screen.home.CustomTextField

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
        Modifier.Companion
            .padding(spacing)
            .fillMaxWidth()
    ) {
        CustomTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text(text = "Key") },
            modifier = Modifier.Companion.fillMaxWidth(),
            singleLine = true
        )
        Spacer(modifier = Modifier.Companion.height(spacing))
        CustomTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text(text = "Value") },
            modifier = Modifier.Companion.fillMaxWidth(),
            singleLine = true
        )
        if (shouldShowEncryptButton) {
            Row(
                Modifier.Companion
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.Companion.CenterVertically
            ) {
                Text(text = "Encrypt")
                Spacer(
                    modifier = Modifier.Companion
                        .weight(1f)
                        .fillMaxWidth()
                )
                Switch(checked = isEncrypted, onCheckedChange = { isEncrypted = it })

            }
        }
        PasswordGenerator(onPasswordGenerated = { password = it }, shouldGeneratePassword = false)
        Spacer(modifier = Modifier.Companion.height(spacing))
        Row(Modifier.Companion.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            OutlinedButton(onClick = { onCancel() }) {
                Text(text = "Cancel")
            }
            Spacer(modifier = Modifier.Companion.width(spacing))
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