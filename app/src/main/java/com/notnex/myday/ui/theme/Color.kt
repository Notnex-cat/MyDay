package com.notnex.myday.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp

/**
 * Тона одной темы оформления в одном варианте (светлом или тёмном). Из них собираются все
 * роли Material 3, поэтому новая тема — это только новый набор значений.
 *
 * Каждая пара «текст — подложка» проходит WCAG AA; это проверяет ThemeContrastTest.
 */
@Immutable
data class Palette(
    /** Страница. */
    val background: Color,
    /** Карточки, листы, меню. */
    val card: Color,
    /** Блоки внутри карточек, выделение «сегодня». */
    val cardHigh: Color,
    val text: Color,
    val textVariant: Color,
    /** Рамки и неактивные элементы — должны отличаться от фона не меньше чем на 3:1. */
    val outline: Color,
    /** Разделители и линии, которые только намечают структуру. */
    val line: Color,
    /** Фирменный цвет: главная кнопка, «+», выбранный день. */
    val accent: Color,
    val onAccent: Color,
    /** Цвет текстовых кнопок, переключателей и курсора — должен читаться на странице. */
    val primary: Color,
    val onPrimary: Color,
    /** Голос ассистента. */
    val assistant: Color,
    val onAssistant: Color,
    /** Оценка 1 и 3; оценка 5 — фирменный accent. Между ними цвет плавно смешивается. */
    val ratingLow: Color,
    val ratingMid: Color,
)

// Ошибки одинаковы во всех темах: красный не должен зависеть от вкуса.
private val ErrorLight = Color(0xFFB3261E)
private val ErrorContainerLight = Color(0xFFFFDAD6)
private val OnErrorContainerLight = Color(0xFF410002)
private val ErrorDark = Color(0xFFFFB4AB)
private val OnErrorDark = Color(0xFF690005)
private val ErrorContainerDark = Color(0xFF93000A)
private val OnErrorContainerDark = Color(0xFFFFDAD6)

// Солнечный: тёплый графит и солнечно-жёлтый, ассистент голубой.
val SunnyLight: Palette = Palette(
    background = Color(0xFFFFF7E6),
    card = Color(0xFFFFFFFF),
    cardHigh = Color(0xFFFCEFCF),
    text = Color(0xFF1E1B16),
    textVariant = Color(0xFF5E574C),
    outline = Color(0xFF8A8070),
    line = Color(0xFFEADFC6),
    accent = Color(0xFFFFC52E),
    onAccent = Color(0xFF1E1B16),
    primary = Color(0xFF7A5200),
    onPrimary = Color(0xFFFFFFFF),
    assistant = Color(0xFFDDEEFF),
    onAssistant = Color(0xFF13324D),
    ratingLow = Color(0xFFE5483A),
    ratingMid = Color(0xFFFF8F2E),
)
val SunnyDark: Palette = Palette(
    background = Color(0xFF1A1815),
    card = Color(0xFF26231F),
    cardHigh = Color(0xFF332F29),
    text = Color(0xFFF5F0E6),
    textVariant = Color(0xFFBDB4A6),
    outline = Color(0xFF8C8477),
    line = Color(0xFF3E3932),
    accent = Color(0xFFFFC52E),
    onAccent = Color(0xFF1A1815),
    primary = Color(0xFFFFC52E),
    onPrimary = Color(0xFF1A1815),
    assistant = Color(0xFF1E3142),
    onAssistant = Color(0xFFCBE6FF),
    ratingLow = Color(0xFFFF6B57),
    ratingMid = Color(0xFFFF9A3D),
)

// Сумерки: вечернее небо и абрикосовое солнце, ассистент розово-коралловый.
val DuskLight: Palette = Palette(
    background = Color(0xFFF4F0EA),
    card = Color(0xFFFFFFFF),
    cardHigh = Color(0xFFECE6F2),
    text = Color(0xFF221D3A),
    textVariant = Color(0xFF5C5574),
    outline = Color(0xFF857D99),
    line = Color(0xFFD6CFE0),
    accent = Color(0xFFFFB547),
    onAccent = Color(0xFF2A1A00),
    primary = Color(0xFF4B3F8F),
    onPrimary = Color(0xFFFFFFFF),
    assistant = Color(0xFFF6E1E4),
    onAssistant = Color(0xFF5A2236),
    ratingLow = Color(0xFFD9466E),
    ratingMid = Color(0xFFFF7F66),
)
val DuskDark: Palette = Palette(
    background = Color(0xFF17142A),
    card = Color(0xFF231F3A),
    cardHigh = Color(0xFF2E2950),
    text = Color(0xFFF4EFE8),
    textVariant = Color(0xFFB9B2CC),
    outline = Color(0xFF8A82A6),
    line = Color(0xFF3D3760),
    accent = Color(0xFFFFB547),
    onAccent = Color(0xFF2A1A00),
    primary = Color(0xFFFFB547),
    onPrimary = Color(0xFF2A1A00),
    assistant = Color(0xFF3E2440),
    onAssistant = Color(0xFFFFD9D0),
    ratingLow = Color(0xFFF0597A),
    ratingMid = Color(0xFFFF8A6B),
)

// Хвойный: тёмная хвоя и абрикосовый, ассистент серо-голубой.
val PineLight: Palette = Palette(
    background = Color(0xFFF1F4EE),
    card = Color(0xFFFFFFFF),
    cardHigh = Color(0xFFE3EBE3),
    text = Color(0xFF14231E),
    textVariant = Color(0xFF4E6159),
    outline = Color(0xFF7A8C84),
    line = Color(0xFFCFDAD2),
    accent = Color(0xFFFFA45B),
    onAccent = Color(0xFF2B1400),
    primary = Color(0xFF1C5A49),
    onPrimary = Color(0xFFFFFFFF),
    assistant = Color(0xFFE1EEF4),
    onAssistant = Color(0xFF173847),
    ratingLow = Color(0xFFD9473F),
    ratingMid = Color(0xFFF2774F),
)
val PineDark: Palette = Palette(
    background = Color(0xFF0F1A17),
    card = Color(0xFF172622),
    cardHigh = Color(0xFF20332D),
    text = Color(0xFFEEF3EE),
    textVariant = Color(0xFFA9BDB4),
    outline = Color(0xFF7F968D),
    line = Color(0xFF2A4038),
    accent = Color(0xFFFFA45B),
    onAccent = Color(0xFF2B1400),
    primary = Color(0xFFFFA45B),
    onPrimary = Color(0xFF2B1400),
    assistant = Color(0xFF1E2F3A),
    onAssistant = Color(0xFFCFE6F2),
    ratingLow = Color(0xFFE5584D),
    ratingMid = Color(0xFFF2804F),
)

/**
 * Все 36 ролей Material 3: пропущенная взялась бы из базовой фиолетовой схемы.
 * Интерфейс плоский: surfaceTint совпадает с фоном, приподнятые поверхности не тонируются.
 */
fun Palette.toColorScheme(dark: Boolean): ColorScheme {
    val base = if (dark) darkColorScheme() else lightColorScheme()
    return base.copy(
        primary = primary,
        onPrimary = onPrimary,
        // Главная кнопка, FAB и выбранный день — фирменный цвет.
        primaryContainer = accent,
        onPrimaryContainer = onAccent,
        inversePrimary = accent,
        secondary = textVariant,
        onSecondary = background,
        secondaryContainer = cardHigh,
        onSecondaryContainer = text,
        tertiary = onAssistant,
        onTertiary = assistant,
        tertiaryContainer = assistant,
        onTertiaryContainer = onAssistant,
        background = background,
        onBackground = text,
        surface = background,
        onSurface = text,
        surfaceVariant = cardHigh,
        onSurfaceVariant = textVariant,
        surfaceTint = background,
        inverseSurface = text,
        inverseOnSurface = background,
        error = if (dark) ErrorDark else ErrorLight,
        onError = if (dark) OnErrorDark else Color.White,
        errorContainer = if (dark) ErrorContainerDark else ErrorContainerLight,
        onErrorContainer = if (dark) OnErrorContainerDark else OnErrorContainerLight,
        outline = outline,
        outlineVariant = line,
        scrim = Color.Black,
        surfaceBright = cardHigh,
        surfaceContainer = card,
        surfaceContainerHigh = card,
        surfaceContainerHighest = cardHigh,
        surfaceContainerLow = card,
        surfaceContainerLowest = background,
        surfaceDim = background,
    )
}

/**
 * Цвет оценки от 1 до 5: красный оттенок темы → средний → фирменный. Не оценено — null,
 * тогда рисуется пустой цвет. Используется и для дня, и для пунктов расписания.
 */
fun Palette.ratingColor(score: Double): Color? = when {
    score <= 0.0 -> null
    score <= 3.0 -> lerp(ratingLow, ratingMid, ((score - 1.0) / 2.0).toFloat().coerceIn(0f, 1f))
    else -> lerp(ratingMid, accent, ((score - 3.0) / 2.0).toFloat().coerceIn(0f, 1f))
}
