package com.jpdev.appcontrolfinanciero.ui.navigation

import com.jpdev.appcontrolfinanciero.domain.EntryType

sealed class Screen(val route: String) {
    data object Dashboard : Screen("dashboard")
    data object Movimientos : Screen("movimientos")
    data object Categorias : Screen("categorias")
    data object Ajustes : Screen("ajustes")
    data object Agregar : Screen("agregar/{type}?editId={editId}") {
        const val ARG_TYPE = "type"
        const val ARG_EDIT_ID = "editId"
        fun routeFor(type: EntryType, editId: Long? = null) = "agregar/${type.name}?editId=${editId ?: -1L}"
    }
}
