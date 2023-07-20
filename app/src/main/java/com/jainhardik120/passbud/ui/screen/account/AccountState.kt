package com.jainhardik120.passbud.ui.screen.account

import androidx.biometric.BiometricPrompt
import com.jainhardik120.passbud.data.local.entities.Credential
import com.jainhardik120.passbud.data.local.entities.CredentialAccount
import com.jainhardik120.passbud.util.CryptoPurpose

data class AccountState(
    val credentials: List<Credential> = emptyList(),
    val account : CredentialAccount = CredentialAccount("", "", ""),
    val editCredentialKey : String = "",
    val editCredentialValue : String = "",
    val editCredentialType : Int = 0,
    val editCredentialEncrypted : Boolean = true,
    val editCredentialId : String ="",
    val authContext: AuthContext? = null
)


data class AuthContext(
    val purpose: CryptoPurpose,
    val cryptoObject: BiometricPrompt.CryptoObject
)
