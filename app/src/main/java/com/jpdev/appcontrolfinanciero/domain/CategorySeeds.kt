package com.jpdev.appcontrolfinanciero.domain

/**
 * Seed data for the categories tables (inserted once per type, on first DB creation) and the
 * keyword table VoiceParser matches against. Categories themselves are dynamic/user-editable
 * from here on (see data.local.CategoryEntity) — this is just the starting set, not a fixed enum.
 */
object CategorySeeds {
    data class Seed(val name: String, val keywords: List<String> = emptyList())

    val EXPENSE = listOf(
        Seed("Alimentación", listOf("comida", "almuerzo", "desayuno", "cena", "mercado", "restaurante")),
        Seed("Transporte", listOf("transporte", "bus", "taxi", "uber", "gasolina", "pasaje")),
        Seed("Entretenimiento", listOf("entretenimiento", "cine", "netflix", "salida", "fiesta")),
        Seed("Salud", listOf("salud", "farmacia", "medico", "médico", "droguería", "drogueria")),
        Seed("Educación", listOf("educación", "educacion", "libros", "matrícula", "matricula", "curso")),
        Seed("Otro")
    )

    val INCOME = listOf(
        Seed("Beca"),
        Seed("Mesada"),
        Seed("Trabajo"),
        Seed("Otro")
    )

    const val RESERVED_NAME = "Sin categoría"
}
