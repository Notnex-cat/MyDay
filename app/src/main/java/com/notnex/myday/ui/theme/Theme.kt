package com.notnex.myday.ui.theme

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat

// Material 3 сам раздаёт эти ключи компонентам: extraSmall — поля ввода, меню и снекбар,
// small — чипы, medium — карточки, large — FAB и боковое меню, extraLarge — диалоги и нижние
// листы. Кнопки Shapes не читают: они по умолчанию «таблетки», это и нужно.
val MyDayShapes: Shapes = Shapes(
    extraSmall = RoundedCornerShape(12.dp),
    small = RoundedCornerShape(percent = 50),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp),
)

/** Шкала отступов с шагом 4 dp. Берётся через `MyDayTheme.spacing`. */
@Immutable
data class Spacing(
    val xxs: Dp = 4.dp,
    val xs: Dp = 8.dp,
    val sm: Dp = 12.dp,
    val md: Dp = 16.dp,
    val lg: Dp = 20.dp,
    val xl: Dp = 24.dp,
    val xxl: Dp = 32.dp,
    val xxxl: Dp = 40.dp,
)

private val LocalSpacing: ProvidableCompositionLocal<Spacing> = staticCompositionLocalOf { Spacing() }
private val LocalPalette: ProvidableCompositionLocal<Palette> = staticCompositionLocalOf { SunnyDark }

/** То, чего нет в MaterialTheme: отступы и тона текущей темы (например, шкала цвета оценки). */
object MyDayTheme {
    val spacing: Spacing
        @Composable @ReadOnlyComposable get() = LocalSpacing.current

    val palette: Palette
        @Composable @ReadOnlyComposable get() = LocalPalette.current
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

/**
 * Тема MyDay. Светлый или тёмный вариант идёт вслед за системой. Тем оформления три
 * (ThemeStyle), но выбора в настройках пока нет — всегда тема по умолчанию.
 * Цвета обоев (dynamic color) не используются: с ними собственная палитра темы не была бы видна.
 */
@Composable
fun MyDayTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val style = ThemeStyle.Default
    val palette = style.palette(darkTheme)
    val colorScheme = remember(palette, darkTheme) { palette.toColorScheme(darkTheme) }

    // enableEdgeToEdge() красит значки системных панелей по теме системы, а тему можно
    // передать и вручную, поэтому цвет значков берётся из той же darkTheme.
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = view.context.findActivity()?.window ?: return@SideEffect
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    CompositionLocalProvider(
        LocalSpacing provides Spacing(),
        LocalPalette provides palette,
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = style.typography,
            shapes = MyDayShapes,
            content = content,
        )
    }
}
