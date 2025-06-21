package com.jainhardik120.passbud.ui.screen.home

import com.jainhardik120.passbud.data.local.AccountWithCredentialCount
import com.jainhardik120.passbud.ui.biometrics.AuthContext

data class HomeState(
    val accounts: List<AccountWithCredentialCount> = emptyList(),
    val appStatus: AppStatus = AppStatus.READY,
    val authContext: AuthContext? = null
)