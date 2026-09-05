package com.jpdev.appcontrolfinanciero.ui.components

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

/** Colombian-style grouping: "." every 3 digits from the right (11000 -> "11.000"). */
fun Long.formatMoney(): String {
    val sign = if (this < 0) "-" else ""
    return sign + kotlin.math.abs(this).toString().reversed().chunked(3).joinToString(".").reversed()
}

private fun groupedOffsetFor(digitCount: Int, offset: Int): Int {
    val totalDots = (digitCount - 1) / 3
    // ponytail: plain (digitCount-offset)/3 overcounts when digitCount is an exact multiple
    // of 3 (e.g. "100" claims a dot that doesn't exist, driving the result negative) — clamp
    // to totalDots so it never counts more boundaries than actually exist.
    val dotsRight = minOf(totalDots, (digitCount - offset) / 3)
    return offset + (totalDots - dotsRight)
}

/** Groups a raw digit string as the user types (e.g. "11000" shown as "11.000") without touching the underlying value. */
val ThousandsVisualTransformation = VisualTransformation { text ->
    val digits = text.text
    val n = digits.length
    val grouped = digits.reversed().chunked(3).joinToString(".").reversed()
    val offsetMapping = object : OffsetMapping {
        override fun originalToTransformed(offset: Int) = groupedOffsetFor(n, offset.coerceIn(0, n))
        override fun transformedToOriginal(offset: Int): Int {
            for (o in 0..n) if (groupedOffsetFor(n, o) >= offset) return o
            return n
        }
    }
    TransformedText(AnnotatedString(grouped), offsetMapping)
}
