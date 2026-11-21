package com.notnex.myday.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.notnex.myday.R

// Шрифты лежат в приложении переменными файлами: работают без сети и без сервисов Google.
// Без явной оси веса Android взял бы из файла только обычное начертание и рисовал жирное
// искусственно, поэтому вес задаётся у каждого Font.
@OptIn(ExperimentalTextApi::class)
private fun variableFont(resId: Int, weight: FontWeight): Font = Font(
    resId = resId,
    weight = weight,
    variationSettings = FontVariation.Settings(weight, FontStyle.Normal),
)

private fun variableFamily(resId: Int, vararg weights: FontWeight): FontFamily =
    FontFamily(weights.map { variableFont(resId, it) })

/** Заголовки «Солнечного»: круглый и широкий. */
val Unbounded: FontFamily = variableFamily(R.font.unbounded_variable, FontWeight.SemiBold, FontWeight.Bold)

/** Заголовки «Сумерек»: книжные засечки, как в дневнике. */
val Literata: FontFamily = variableFamily(R.font.literata_variable, FontWeight.SemiBold, FontWeight.Bold)

/** «Хвойный» набран им целиком. */
val Manrope: FontFamily = variableFamily(
    R.font.manrope_variable,
    FontWeight.Normal, FontWeight.Medium, FontWeight.SemiBold, FontWeight.Bold, FontWeight.ExtraBold,
)

/** Текст, подписи, кнопки в «Солнечном» и «Сумерках». */
val Onest: FontFamily = variableFamily(
    R.font.onest_variable,
    FontWeight.Normal, FontWeight.Medium, FontWeight.SemiBold, FontWeight.Bold,
)

// С цифрами одной высоты даты и суммы не прыгают.
private const val LiningFigures: String = "lnum"

private val Baseline: Typography = Typography()

private const val HeadingLineHeight: Float = 1.6f

// Высота строки во всех темах одна и та же, от шрифта не зависит: темы меняют вид, а не раскладку.
// Без этого Android добавлял бы к строке собственные отступы шрифта, а у Literata и Manrope
// они больше, чем у Unbounded, — карточки в разных темах получались разной высоты.
private val FixedLineBox = PlatformTextStyle(includeFontPadding = false)
private val CenteredLine = LineHeightStyle(
    alignment = LineHeightStyle.Alignment.Center,
    trim = LineHeightStyle.Trim.None,
)

/**
 * Шкала текста темы оформления. Стили строятся из стандартных Material 3, чтобы сохранить их
 * выравнивание строки по центру.
 *
 * @param headingScale широкие шрифты (Unbounded) при тех же кеглях выглядят крупнее, их уменьшаем.
 *   Уменьшается только кегль, высота строки остаётся общей для всех тем.
 */
fun themeTypography(
    heading: FontFamily,
    headingWeight: FontWeight,
    titleWeight: FontWeight,
    body: FontFamily,
    headingScale: Float = 1f,
): Typography {
    // Высота строки заголовка — с запасом в 1,6 кегля под самый высокий из шрифтов тем (Literata).
    // Если буквы не помещаются в строку, Compose раздвигает её, и тогда высота зависела бы от темы.
    fun TextStyle.heading(weight: FontWeight, size: Int, letterSpacing: TextUnit = 0.sp) = copy(
        fontFamily = heading,
        fontWeight = weight,
        fontSize = (size * headingScale).sp,
        lineHeight = (size * HeadingLineHeight).sp,
        letterSpacing = letterSpacing,
        fontFeatureSettings = LiningFigures,
        platformStyle = FixedLineBox,
        lineHeightStyle = CenteredLine,
    )

    fun TextStyle.text(weight: FontWeight, size: Int, lineHeight: Int, letterSpacing: TextUnit = 0.sp) = copy(
        fontFamily = body,
        fontWeight = weight,
        fontSize = size.sp,
        lineHeight = lineHeight.sp,
        letterSpacing = letterSpacing,
        platformStyle = FixedLineBox,
        lineHeightStyle = CenteredLine,
    )

    // Заголовки громкие, всё, что читают подряд и на что нажимают, — спокойным шрифтом текста.
    return Typography(
        displayLarge = Baseline.displayLarge.heading(headingWeight, 52, (-0.5).sp),
        displayMedium = Baseline.displayMedium.heading(headingWeight, 44, (-0.5).sp),
        displaySmall = Baseline.displaySmall.heading(headingWeight, 36, (-0.25).sp),
        headlineLarge = Baseline.headlineLarge.heading(headingWeight, 32),
        headlineMedium = Baseline.headlineMedium.heading(headingWeight, 28),
        headlineSmall = Baseline.headlineSmall.heading(headingWeight, 24),
        titleLarge = Baseline.titleLarge.heading(titleWeight, 22),
        titleMedium = Baseline.titleMedium.heading(titleWeight, 18),
        titleSmall = Baseline.titleSmall.text(FontWeight.SemiBold, 15, 20),
        bodyLarge = Baseline.bodyLarge.text(FontWeight.Normal, 16, 24),
        bodyMedium = Baseline.bodyMedium.text(FontWeight.Normal, 14, 20),
        bodySmall = Baseline.bodySmall.text(FontWeight.Normal, 13, 18),
        labelLarge = Baseline.labelLarge.text(FontWeight.Medium, 15, 20),
        labelMedium = Baseline.labelMedium.text(FontWeight.Medium, 13, 16),
        labelSmall = Baseline.labelSmall.text(FontWeight.SemiBold, 11, 16, 0.25.sp),
    )
}
