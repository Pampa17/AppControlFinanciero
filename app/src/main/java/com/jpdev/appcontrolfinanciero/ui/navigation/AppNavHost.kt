package com.jpdev.appcontrolfinanciero.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.jpdev.appcontrolfinanciero.ui.agregar.AddEntryScreen
import com.jpdev.appcontrolfinanciero.ui.ajustes.SettingsScreen
import com.jpdev.appcontrolfinanciero.ui.dashboard.DashboardScreen
import com.jpdev.appcontrolfinanciero.ui.movimientos.MovimientosScreen

private data class BottomNavItem(val route: String, val label: String, val emoji: String)

// No icon library dependency: Po's "icon + label always visible" requirement, met with emoji.
private val bottomNavItems = listOf(
    BottomNavItem(Screen.Dashboard.route, "Inicio", "🏠"),
    BottomNavItem(Screen.Movimientos.route, "Movimientos", "📋"),
    BottomNavItem(Screen.Agregar.routeFor(EntryType.EGRESO), "Agregar", "➕"),
    BottomNavItem(Screen.Ajustes.route, "Ajustes", "⚙")
)

@Composable
fun AppNavHost() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            NavigationBar {
                bottomNavItems.forEach { item ->
                    val isAgregarTab = item.route.startsWith("agregar")
                    val selected = if (isAgregarTab) currentRoute?.startsWith("agregar") == true else currentRoute == item.route
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Text(item.emoji) },
                        label = { Text(item.label) }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Dashboard.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Dashboard.route) {
                DashboardScreen(
                    onAddEntry = { type -> navController.navigate(Screen.Agregar.routeFor(type)) }
                )
            }
            composable(Screen.Movimientos.route) {
                MovimientosScreen(
                    onEditExpense = { id -> navController.navigate(Screen.Agregar.routeFor(EntryType.EGRESO, id)) }
                )
            }
            composable(Screen.Ajustes.route) {
                SettingsScreen()
            }
            composable(
                route = Screen.Agregar.route,
                arguments = listOf(
                    navArgument(Screen.Agregar.ARG_TYPE) { type = NavType.StringType },
                    navArgument(Screen.Agregar.ARG_EDIT_ID) { type = NavType.LongType; defaultValue = -1L }
                )
            ) { backStack ->
                val type = EntryType.valueOf(backStack.arguments?.getString(Screen.Agregar.ARG_TYPE) ?: EntryType.EGRESO.name)
                val editId = backStack.arguments?.getLong(Screen.Agregar.ARG_EDIT_ID) ?: -1L
                AddEntryScreen(
                    type = type,
                    editId = editId,
                    onSaved = { navController.popBackStack() }
                )
            }
        }
    }
}
