package com.jpdev.appcontrolfinanciero.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
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

private data class BottomNavItem(val route: String, val label: String, val icon: ImageVector)

// Po's "icon + label always visible" requirement, met with material icons (already resolved
// via material3/navigation-compose — no new dependency needed).
private val bottomNavItems = listOf(
    BottomNavItem(Screen.Dashboard.route, "Inicio", Icons.Filled.Home),
    BottomNavItem(Screen.Movimientos.route, "Movimientos", Icons.AutoMirrored.Filled.List),
    BottomNavItem(Screen.Agregar.routeFor(EntryType.EGRESO), "Agregar", Icons.Filled.Add),
    BottomNavItem(Screen.Ajustes.route, "Ajustes", Icons.Filled.Settings)
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
                            // ponytail: no saveState/restoreState here — Agregar carries a required
                            // (non-default) `type` arg and is reached by a plain push from Dashboard/
                            // Movimientos, not the singleTop tab switch below. That combo makes
                            // NavController's save/restore bookkeeping silently no-op navigate() while
                            // Agregar is on top (reproduced on-device: onClick fires, navigate() returns,
                            // back stack is untouched) — the reported "Inicio button does nothing" bug.
                            // Plain popUpTo+launchSingleTop pops back to the tab correctly.
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.findStartDestination().id) { inclusive = false }
                                launchSingleTop = true
                            }
                        },
                        icon = { Icon(item.icon, contentDescription = null) },
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
