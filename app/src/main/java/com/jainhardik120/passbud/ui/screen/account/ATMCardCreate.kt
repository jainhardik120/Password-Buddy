package com.jainhardik120.passbud.ui.screen.account

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.jainhardik120.passbud.domain.BankCard
import com.jainhardik120.passbud.ui.screen.home.CustomTextField

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
        Modifier.Companion
            .padding(spacing)
            .fillMaxWidth()
    ) {
        CustomTextField(
            value = cardName,
            onValueChange = { cardName = it },
            label = { Text(text = "Card Name") },
            modifier = Modifier.Companion.fillMaxWidth(),
            singleLine = true
        )
        Spacer(modifier = Modifier.Companion.height(spacing))
        Row {
            CustomTextField(
                value = cardNumber,
                onValueChange = {
                    if (it.isEmpty() || (it.length <= 16 && validInputRegex.matches(it))) {
                        cardNumber = it
                    }
                },
                label = { Text(text = "Number") },
                modifier = Modifier.Companion
                    .weight(6f)
                    .fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions.Companion.Default.copy(keyboardType = KeyboardType.Companion.Number)
            )
            Spacer(modifier = Modifier.Companion.width(spacing))
            CustomTextField(
                value = cvv,
                onValueChange = {
                    if (it.isEmpty() || (it.length <= 3 && validInputRegex.matches(it))) {
                        cvv = it
                    }
                },
                label = { Text(text = "CVV") },
                modifier = Modifier.Companion
                    .weight(3f)
                    .fillMaxWidth(),
                keyboardOptions = KeyboardOptions.Companion.Default.copy(keyboardType = KeyboardType.Companion.Number),
                singleLine = true,
                placeholder = { Text(text = "123") }
            )
        }

        Spacer(modifier = Modifier.Companion.height(spacing))
        Row(
            Modifier.Companion.fillMaxWidth(),
            verticalAlignment = Alignment.Companion.CenterVertically
        ) {

            CustomTextField(
                value = validFrom,
                onValueChange = {
                    if (it.isEmpty() || (it.length <= 4 && validInputRegex.matches(it))) {
                        validFrom = it
                    }
                },
                label = { Text(text = "Valid From") },
                modifier = Modifier.Companion
                    .weight(1f)
                    .fillMaxWidth(),
                keyboardOptions = KeyboardOptions.Companion.Default.copy(keyboardType = KeyboardType.Companion.Number),
                singleLine = true,
                placeholder = { Text(text = "MMYY") }
            )
            Spacer(modifier = Modifier.Companion.width(spacing))
            CustomTextField(
                value = validThru,
                onValueChange = {
                    if (it.isEmpty() || (it.length <= 4 && validInputRegex.matches(it))) {
                        validThru = it
                    }
                },
                label = { Text(text = "Valid Thru") },
                modifier = Modifier.Companion
                    .weight(1f)
                    .fillMaxWidth(),
                keyboardOptions = KeyboardOptions.Companion.Default.copy(keyboardType = KeyboardType.Companion.Number),
                singleLine = true,
                placeholder = { Text(text = "MMYY") }
            )

        }
        Spacer(modifier = Modifier.Companion.height(spacing))
        Row(Modifier.Companion.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            OutlinedButton(onClick = { onCancel() }) {
                Text(text = "Cancel")
            }
            Spacer(modifier = Modifier.Companion.width(spacing))
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