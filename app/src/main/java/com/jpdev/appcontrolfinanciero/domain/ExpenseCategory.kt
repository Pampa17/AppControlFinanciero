package com.jpdev.appcontrolfinanciero.domain

enum class ExpenseCategory(val label: String, val keywords: List<String>) {
    ALIMENTACION("Alimentación", listOf("comida", "almuerzo", "desayuno", "cena", "mercado", "restaurante")),
    TRANSPORTE("Transporte", listOf("transporte", "bus", "taxi", "uber", "gasolina", "pasaje")),
    ENTRETENIMIENTO("Entretenimiento", listOf("entretenimiento", "cine", "netflix", "salida", "fiesta")),
    SALUD("Salud", listOf("salud", "farmacia", "medico", "médico", "droguería", "drogueria")),
    EDUCACION("Educación", listOf("educación", "educacion", "libros", "matrícula", "matricula", "curso")),
    OTRO("Otro", emptyList())
}
