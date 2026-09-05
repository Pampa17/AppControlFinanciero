package com.jpdev.appcontrolfinanciero.ui.components

import androidx.compose.ui.text.AnnotatedString
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MoneyFormatTest {

    @Test
    fun `formats with dot grouping`() {
        assertEquals("11.000", 11000L.formatMoney())
        assertEquals("100", 100L.formatMoney())
        assertEquals("1.000.000", 1_000_000L.formatMoney())
        assertEquals("-4.200", (-4200L).formatMoney())
    }

    @Test
    fun `offset mapping never goes out of range, including exact multiples of 3`() {
        // The exact crash this guards: digit counts of 3, 6, 9... used to make
        // originalToTransformed return -1 and take the app down.
        for (n in 0..12) {
            val digits = "1".repeat(n)
            val transformed = ThousandsVisualTransformation.filter(AnnotatedString(digits))
            val transformedLength = transformed.text.length
            for (offset in 0..n) {
                val t = transformed.offsetMapping.originalToTransformed(offset)
                assertTrue("n=$n offset=$offset -> $t out of [0,$transformedLength]", t in 0..transformedLength)
            }
        }
    }
}
