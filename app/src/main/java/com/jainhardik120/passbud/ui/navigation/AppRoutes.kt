package com.jainhardik120.passbud.ui.navigation

import androidx.compose.runtime.Composable


sealed class AppRoutes(val route : String){
    object Home : AppRoutes(route = "home")
    object NewAccount : AppRoutes(route = "new_account")
    object AccountScreen : AppRoutes(route = "account_display")
    fun withArgs(vararg args: String):String{
        return buildString {
            append(route)
            args.forEach { arg->
                append("/$arg")
            }
        }
    }
}

