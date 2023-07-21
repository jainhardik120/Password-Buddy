package com.jainhardik120.passbud.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavBackStackEntry
import androidx.navigation.compose.currentBackStackEntryAsState
import com.jainhardik120.passbud.ui.navigation.AppRoutes
import com.jainhardik120.passbud.ui.navigation.PassbudAppNavHost
import com.jainhardik120.passbud.ui.theme.PassbudTheme


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App() {
    PassbudTheme {
        val appState = rememberAppState()
        val currentBackStack by appState.navController.currentBackStackEntryAsState()
        val appDestination = resolveAppDestination(currentBackStack)
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            PassbudAppNavHost(
                navController = appState.navController,
                startDestination = AppRoutes.Home.route,
                hostState = appState.snackBarHostState
            )
        }
    }
}


fun resolveAppDestination(currentBackStack: NavBackStackEntry?): AppRoutes {
    val route = currentBackStack?.destination?.route
    val listOfRoutes = listOf(AppRoutes.Home, AppRoutes.NewAccount, AppRoutes.AccountScreen)
    return listOfRoutes.firstOrNull {
        route?.contains(it.route) == true
    } ?: AppRoutes.Home
}