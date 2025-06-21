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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun CustomAccountCreate(
    onCancel: () -> Unit,
    onConfirmCreate: ((String, String) -> Unit),
    confirmButtonText: String = "Create",
    initialName: String = "",
    initialDescription: String = "",
) {
    var accountName by remember { mutableStateOf("") }
    var accountDescription by remember { mutableStateOf("") }
    LaunchedEffect(key1 = Unit, block = {
        accountName = initialName
        accountDescription = initialDescription
    })
    val spacing = 20.dp
    Column(
        Modifier.Companion
            .padding(spacing)
            .fillMaxWidth()
    ) {
        CustomTextField(
            value = accountName,
            onValueChange = { accountName = it },
            label = { Text(text = "Name") },
            modifier = Modifier.Companion.fillMaxWidth()
        )
        Spacer(modifier = Modifier.Companion.height(spacing))
        CustomTextField(
            value = accountDescription,
            onValueChange = { accountDescription = it },
            label = { Text(text = "Description (optional)") },
            modifier = Modifier.Companion.fillMaxWidth()
        )
        Spacer(modifier = Modifier.Companion.height(spacing))
        Row(Modifier.Companion.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            OutlinedButton(onClick = { onCancel() }) {
                Text(text = "Cancel")
            }
            Spacer(modifier = Modifier.Companion.width(spacing))
            Button(
                onClick = { onConfirmCreate(accountName, accountDescription) },
                enabled = accountName.isNotEmpty()
            ) {
                Text(text = confirmButtonText)
            }
        }
    }

}

@Preview(showBackground = true)
@Composable
fun CustomAccountPreview() {
    CustomAccountCreate(onCancel = {}, onConfirmCreate = { _, _ -> })
}