package com.notnex.myday.ui.theme

import androidx.annotation.StringRes
import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.FontWeight
import com.notnex.myday.R

/**
 * Тема оформления: цвета и шрифты. Расположение элементов во всех темах одно.
 * В настройках хранится имя константы, поэтому переименование сбросит выбор на тему по умолчанию.
 */
enum class ThemeStyle(
    @param:StringRes val title: Int,
    val light: Palette,
    val dark: Palette,
) {
    SUNNY(R.string.theme_sunny, SunnyLight, SunnyDark),
    DUSK(R.string.theme_dusk, DuskLight, DuskDark),
    PINE(R.string.theme_pine, PineLight, PineDark);

    fun palette(dark: Boolean): Palette = if (dark) this.dark else light

    // Лениво: шкала строится только для темы, которую действительно выбрали.
    val typography: Typography by lazy {
        when (this) {
            SUNNY -> themeTypography(Unbounded, FontWeight.Bold, FontWeight.SemiBold, Onest, headingScale = 0.82f)
            DUSK -> themeTypography(Literata, FontWeight.Bold, FontWeight.SemiBold, Onest)
            PINE -> themeTypography(Manrope, FontWeight.ExtraBold, FontWeight.Bold, Manrope)
        }
    }

    companion object {
        val Default: ThemeStyle = SUNNY

        fun fromName(name: String?): ThemeStyle = entries.firstOrNull { it.name == name } ?: Default
    }
}
