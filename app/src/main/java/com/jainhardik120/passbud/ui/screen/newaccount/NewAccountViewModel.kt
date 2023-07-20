package com.jainhardik120.passbud.ui.screen.newaccount

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jainhardik120.passbud.domain.CredentialsRepository
import com.jainhardik120.passbud.ui.snackbar.SnackbarManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NewAccountViewModel @Inject constructor(
    private val credentialsRepository: CredentialsRepository
) : ViewModel() {
    fun createAccount(name: String, description: String) {
        if (name.isBlank() || description.isBlank()) {
            SnackbarManager.showMessage("Name and description is required")
            return
        }
        viewModelScope.launch {
            credentialsRepository.createAccount(name, description)
        }
    }
}