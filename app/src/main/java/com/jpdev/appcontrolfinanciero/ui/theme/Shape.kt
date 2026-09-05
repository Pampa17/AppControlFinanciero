package com.jpdev.appcontrolfinanciero.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

// Po principle: "esquinas redondeadas comunican soy presionable" — rounder than M3's defaults
// so buttons/cards/fields read as touchable, not flat rectangles.
val PoShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(28.dp)
)
