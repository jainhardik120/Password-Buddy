package com.jainhardik120.passbud.ui.screen.home

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun AccountCreationDialog(
    isShown: Boolean,
    changeVisibility: (Boolean) -> Unit,
    createAccount: (String, String) -> Unit,
    createAccountAndCredentials: (String, String, String, String) -> Unit,
    createAccountAndCard: (String, String, String) -> Unit
) {
    if (isShown) {
        var selectedAccountType: AccountTypes by remember { mutableStateOf(AccountTypes.UsernamePassword) }
        val horizontalPagerState = rememberPagerState(pageCount = { 2 })
        val scope = rememberCoroutineScope()

        @Composable
        fun CustomButton(shape: RoundedCornerShape, accountType: AccountTypes) {
            Button(onClick = {
                selectedAccountType = accountType
                scope.launch {
                    horizontalPagerState.animateScrollToPage(1)
                }
            }, shape = shape, modifier = Modifier.Companion.fillMaxWidth()) {
                Text(text = accountType.displayName)
            }
        }

        fun hideSheet() {
            changeVisibility(false)
        }
        AlertDialog(
            onDismissRequest = {
                changeVisibility(false)
            },
            properties = DialogProperties(
                dismissOnBackPress = horizontalPagerState.currentPage == 0,
                dismissOnClickOutside = horizontalPagerState.currentPage == 0,
                usePlatformDefaultWidth = false
            ),
            modifier = Modifier.Companion.padding(14.dp)
        ) {
            Surface(
                shape = AlertDialogDefaults.shape,
                color = AlertDialogDefaults.containerColor,
                tonalElevation = AlertDialogDefaults.TonalElevation,
            ) {
                Column {
                    Row(
                        Modifier.Companion.padding(vertical = 24.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.Companion.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier.Companion.fillMaxWidth(),
                            contentAlignment = Alignment.Companion.Center
                        ) {
                            Text(
                                text = "New Account",
                                style = MaterialTheme.typography.titleLarge,
                                modifier = Modifier.Companion.fillMaxWidth(),
                                textAlign = TextAlign.Companion.Center
                            )
                        }
                    }
                    HorizontalPager(
                        state = horizontalPagerState,
                        modifier = Modifier.Companion.fillMaxWidth(),
                        userScrollEnabled = false
                    ) { page ->
                        when (page) {
                            0 -> {
                                Column(
                                    Modifier.Companion
                                        .padding(horizontal = 32.dp)
                                        .padding(bottom = 20.dp)
                                ) {
                                    CustomButton(
                                        shape = RoundedCornerShape(
                                            topStart = 16.dp,
                                            topEnd = 16.dp,
                                            bottomStart = 4.dp,
                                            bottomEnd = 4.dp
                                        ), accountType = AccountTypes.UsernamePassword
                                    )
                                    CustomButton(
                                        shape = androidx.compose.foundation.shape.RoundedCornerShape(
                                            4.dp
                                        ),
                                        accountType = AccountTypes.ATMCard
                                    )
                                    CustomButton(
                                        shape = RoundedCornerShape(
                                            bottomEnd = 16.dp,
                                            bottomStart = 16.dp,
                                            topEnd = 4.dp,
                                            topStart = 4.dp
                                        ), accountType = AccountTypes.Custom
                                    )
                                }
                            }

                            1 -> {
                                when (selectedAccountType) {
                                    AccountTypes.ATMCard -> {
                                        ATMCardCreate(onCancel = {
                                            hideSheet()
                                        }, onConfirmCreate = { n1, n2, c ->
                                            createAccountAndCard(n1, n2, c)
                                            hideSheet()
                                        })
                                    }

                                    AccountTypes.Custom -> {
                                        CustomAccountCreate(
                                            onCancel = {
                                                hideSheet()
                                            },
                                            onConfirmCreate = { name, desc ->
                                                createAccount(name, desc)
                                                hideSheet()
                                            }
                                        )
                                    }

                                    AccountTypes.UsernamePassword -> {
                                        UsernamePasswordAccount(
                                            onCancel = { hideSheet() },
                                            onConfirmCreate = { name, desc, username, pass ->
                                                createAccountAndCredentials(
                                                    name,
                                                    desc,
                                                    username,
                                                    pass
                                                )
                                                hideSheet()
                                            }
                                        )
                                    }
                                }
                            }

                            else -> {

                            }
                        }
                    }
                }
            }
        }
    }
}