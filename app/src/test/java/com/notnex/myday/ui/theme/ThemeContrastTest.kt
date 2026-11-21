package com.notnex.myday.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import org.junit.Assert.assertTrue
import org.junit.Test

/** Каждая пара «текст — подложка» во всех темах и обоих вариантах проходит WCAG AA. */
class ThemeContrastTest {

    private fun contrast(a: Color, b: Color): Double {
        val la = a.luminance() + 0.05
        val lb = b.luminance() + 0.05
        return (maxOf(la, lb) / minOf(la, lb)).toDouble()
    }

    private fun check(name: String, fg: Color, bg: Color, min: Double) {
        val ratio = contrast(fg, bg)
        assertTrue("$name: %.2f < $min".format(ratio), ratio >= min)
    }

    @Test
    fun allPalettesPassAA() {
        ThemeStyle.entries.forEach { style ->
            listOf(false, true).forEach { dark ->
                val p = style.palette(dark)
                val tag = "$style ${if (dark) "dark" else "light"}"
                // Текст — 4.5:1.
                check("$tag text/background", p.text, p.background, 4.5)
                check("$tag text/card", p.text, p.card, 4.5)
                check("$tag text/cardHigh", p.text, p.cardHigh, 4.5)
                check("$tag textVariant/background", p.textVariant, p.background, 4.5)
                check("$tag textVariant/card", p.textVariant, p.card, 4.5)
                check("$tag onAccent/accent", p.onAccent, p.accent, 4.5)
                check("$tag onPrimary/primary", p.onPrimary, p.primary, 4.5)
                check("$tag primary/background", p.primary, p.background, 4.5)
                check("$tag onAssistant/assistant", p.onAssistant, p.assistant, 4.5)
                // Рамки и неактивные элементы интерфейса — 3:1.
                check("$tag outline/background", p.outline, p.background, 3.0)
                check("$tag outline/card", p.outline, p.card, 3.0)
            }
        }
    }
}
