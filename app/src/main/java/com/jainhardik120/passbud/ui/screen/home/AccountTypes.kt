package com.jainhardik120.passbud.ui.screen.home

sealed class AccountTypes(val displayName: String) {
    object UsernamePassword : AccountTypes("Username & Password")
    object ATMCard : AccountTypes("ATM Card")
    object Custom : AccountTypes("Custom")
}