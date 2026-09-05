package com.jpdev.appcontrolfinanciero.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PieChart
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
import com.jpdev.appcontrolfinanciero.domain.EntryType
import com.jpdev.appcontrolfinanciero.ui.agregar.AddEntryScreen
import com.jpdev.appcontrolfinanciero.ui.ajustes.SettingsScreen
import com.jpdev.appcontrolfinanciero.ui.categorias.CategoriasScreen
import com.jpdev.appcontrolfinanciero.ui.dashboard.DashboardScreen
import com.jpdev.appcontrolfinanciero.ui.movimientos.MovimientosScreen

private data class BottomNavItem(val route: String, val label: String, val icon: ImageVector)

// Po's "icon + label always visible" requirement, met with material icons (already resolved
// via material3/navigation-compose — no new dependency needed).
// "Agregar" was dropped from here: redundant with the quick-add buttons on Dashboard, and the
// slot is more useful as "Categorías" (breakdown of money per category, the actual new ask).
private val bottomNavItems = listOf(
    BottomNavItem(Screen.Dashboard.route, "Inicio", Icons.Filled.Home),
    BottomNavItem(Screen.Movimientos.route, "Movimientos", Icons.AutoMirrored.Filled.List),
    BottomNavItem(Screen.Categorias.route, "Categorías", Icons.Filled.PieChart),
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
                    NavigationBarItem(
                        selected = currentRoute == item.route,
                        onClick = {
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
            composable(Screen.Categorias.route) {
                CategoriasScreen()
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
