package com.notnex.myday.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.notnex.myday.R

// Оба шрифта лежат в приложении переменными файлами: работают без сети и без сервисов Google.
// Без явной оси веса Android взял бы из файла только обычное начертание и рисовал жирное
// искусственно, поэтому вес задаётся у каждого Font.
@OptIn(ExperimentalTextApi::class)
private fun variableFont(resId: Int, weight: FontWeight): Font = Font(
    resId = resId,
    weight = weight,
    variationSettings = FontVariation.Settings(weight, FontStyle.Normal),
)

/** Заголовки и крупные числа. Свободная замена фирменному Flame: мягкие засечки, плотный вес. */
val Alegreya: FontFamily = FontFamily(
    variableFont(R.font.alegreya_variable, FontWeight.Bold),
    variableFont(R.font.alegreya_variable, FontWeight.ExtraBold),
    variableFont(R.font.alegreya_variable, FontWeight.Black),
)

/** Текст, подписи, кнопки. */
val Onest: FontFamily = FontFamily(
    variableFont(R.font.onest_variable, FontWeight.Normal),
    variableFont(R.font.onest_variable, FontWeight.Medium),
    variableFont(R.font.onest_variable, FontWeight.SemiBold),
    variableFont(R.font.onest_variable, FontWeight.Bold),
)

// У Alegreya по умолчанию «старинные» цифры разной высоты; даты и суммы с ними прыгают.
private const val LiningFigures: String = "lnum"

// Стили строятся из стандартных Material 3, чтобы сохранить их выравнивание строки по центру.
private fun TextStyle.heading(
    weight: FontWeight,
    size: TextUnit,
    lineHeight: TextUnit,
    letterSpacing: TextUnit = 0.sp,
): TextStyle = copy(
    fontFamily = Alegreya,
    fontWeight = weight,
    fontSize = size,
    lineHeight = lineHeight,
    letterSpacing = letterSpacing,
    fontFeatureSettings = LiningFigures,
)

private fun TextStyle.text(
    weight: FontWeight,
    size: TextUnit,
    lineHeight: TextUnit,
    letterSpacing: TextUnit = 0.sp,
): TextStyle = copy(
    fontFamily = Onest,
    fontWeight = weight,
    fontSize = size,
    lineHeight = lineHeight,
    letterSpacing = letterSpacing,
)

private val Baseline: Typography = Typography()

// Заголовки — Alegreya 900 и 800 с плотными строками, как у вывесок. Всё, что читают
// подряд, и всё, на что нажимают, — Onest: длинный текст мягкими засечками читается тяжело.
val MyDayTypography: Typography = Typography(
    displayLarge = Baseline.displayLarge.heading(FontWeight.Black, 52.sp, 56.sp, (-0.5).sp),
    displayMedium = Baseline.displayMedium.heading(FontWeight.Black, 44.sp, 48.sp, (-0.5).sp),
    displaySmall = Baseline.displaySmall.heading(FontWeight.Black, 36.sp, 40.sp, (-0.25).sp),
    headlineLarge = Baseline.headlineLarge.heading(FontWeight.Black, 32.sp, 36.sp),
    headlineMedium = Baseline.headlineMedium.heading(FontWeight.Black, 28.sp, 32.sp),
    headlineSmall = Baseline.headlineSmall.heading(FontWeight.Black, 24.sp, 28.sp),
    titleLarge = Baseline.titleLarge.heading(FontWeight.ExtraBold, 22.sp, 26.sp),
    titleMedium = Baseline.titleMedium.heading(FontWeight.ExtraBold, 18.sp, 22.sp),
    titleSmall = Baseline.titleSmall.text(FontWeight.SemiBold, 15.sp, 20.sp),
    bodyLarge = Baseline.bodyLarge.text(FontWeight.Normal, 16.sp, 24.sp),
    bodyMedium = Baseline.bodyMedium.text(FontWeight.Normal, 14.sp, 20.sp),
    bodySmall = Baseline.bodySmall.text(FontWeight.Normal, 13.sp, 18.sp),
    labelLarge = Baseline.labelLarge.text(FontWeight.Medium, 15.sp, 20.sp),
    labelMedium = Baseline.labelMedium.text(FontWeight.Medium, 13.sp, 16.sp),
    labelSmall = Baseline.labelSmall.text(FontWeight.SemiBold, 11.sp, 14.sp, 0.25.sp),
)
