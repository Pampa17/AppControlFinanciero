package com.jpdev.appcontrolfinanciero.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

/**
 * The exam requires 3 selectable accent palettes. AZUL and VERDE are Po's own brand palettes
 * (this is the "adopt Po's tokens" work), MORADO reuses the template's existing Purple values
 * instead of inventing a new off-brand color — one requirement, one piece of work.
 */
enum class AccentPalette(val label: String, val swatch: Color) {
    AZUL("Azul", PoBlue700),
    VERDE("Verde", PoGreen400),
    MORADO("Morado", Purple40)
}

fun AccentPalette.toColorScheme(darkTheme: Boolean): ColorScheme = when (this) {
    AccentPalette.AZUL -> if (darkTheme) {
        darkColorScheme(
            primary = PoBlue300,
            onPrimary = PoBlue900,
            secondary = PoGreen300,
            tertiary = PoBlue100,
            error = PoError
        )
    } else {
        lightColorScheme(
            primary = PoBlue800,
            onPrimary = Color.White,
            secondary = PoGreen600,
            tertiary = PoBlue600,
            error = PoError
        )
    }

    AccentPalette.VERDE -> if (darkTheme) {
        darkColorScheme(
            primary = PoGreen200,
            onPrimary = PoGreen900,
            secondary = PoBlue300,
            tertiary = PoGreen100,
            error = PoError
        )
    } else {
        lightColorScheme(
            primary = PoGreen600,
            onPrimary = Color.White,
            secondary = PoBlue700,
            tertiary = PoGreen400,
            error = PoError
        )
    }

    AccentPalette.MORADO -> if (darkTheme) {
        darkColorScheme(
            primary = Purple80,
            secondary = PurpleGrey80,
            tertiary = Pink80,
            error = PoError
        )
    } else {
        lightColorScheme(
            primary = Purple40,
            secondary = PurpleGrey40,
            tertiary = Pink40,
            error = PoError
        )
    }
}
