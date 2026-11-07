package com.notnex.myday.ui.theme

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat

// Заданы все роли: пропущенная взялась бы из базовой фиолетовой схемы Material 3.
// Пары цветов проверены по WCAG AA, таблица — в DESIGN.md.
private val LightColors: ColorScheme = lightColorScheme(
    // Главный цвет — шоколад, а не жёлтый: primary служит и цветом текста (текстовые кнопки,
    // курсор, переключатели), а жёлтый на бежевом не читается. Жёлтая кнопка — primaryContainer.
    primary = Brown20,
    onPrimary = Cream97,
    primaryContainer = Mustard70,
    onPrimaryContainer = Brown20,
    inversePrimary = Mustard70,
    secondary = Brown35,
    onSecondary = Cream97,
    // Светлый крем, а не бежевый: на бежевой странице бежевая плашка исчезла бы.
    secondaryContainer = Cream97,
    onSecondaryContainer = Brown20,
    tertiary = Orange35,
    onTertiary = Cream97,
    tertiaryContainer = Orange60,
    onTertiaryContainer = Brown20,
    background = Sand88,
    onBackground = Brown20,
    surface = Sand88,
    onSurface = Brown20,
    // Бежевые блоки внутри кремовых карточек.
    surfaceVariant = Sand88,
    onSurfaceVariant = Brown35,
    // Совпадает с фоном: приподнятые поверхности не тонируются, всё плоское.
    surfaceTint = Sand88,
    inverseSurface = Night0,
    inverseOnSurface = Cream97,
    error = Tomato35,
    onError = White,
    errorContainer = Tomato92,
    onErrorContainer = Tomato30,
    outline = Brown45,
    outlineVariant = Sand80,
    scrim = Night0,
    // Поверхностей по сути три: бежевая страница, крем для карточек, листов и меню,
    // светлый крем для самого светлого.
    surfaceBright = Cream97,
    surfaceContainer = Cream93,
    surfaceContainerHigh = Cream93,
    surfaceContainerHighest = Cream93,
    surfaceContainerLow = Cream93,
    surfaceContainerLowest = Cream97,
    surfaceDim = Sand83,
)

// Тёмная тема повторяет устройство светлой: страница, карточки чуть светлее, внутри карточки
// снова цвет страницы. Шоколад и крем меняются местами, жёлтая кнопка и оранжевый остаются.
private val DarkColors: ColorScheme = darkColorScheme(
    // Крем вместо шоколада: тот же «цвет чернил», только для тёмного фона.
    primary = Cream93,
    onPrimary = Brown20,
    primaryContainer = Mustard70,
    onPrimaryContainer = Brown20,
    inversePrimary = Brown20,
    secondary = Latte78,
    onSecondary = Brown25,
    secondaryContainer = Espresso20,
    onSecondaryContainer = Cream93,
    tertiary = Orange75,
    onTertiary = Orange15,
    tertiaryContainer = Orange60,
    onTertiaryContainer = Brown20,
    background = Espresso9,
    onBackground = Cream93,
    surface = Espresso9,
    onSurface = Cream93,
    surfaceVariant = Espresso9,
    onSurfaceVariant = Latte70,
    surfaceTint = Espresso9,
    inverseSurface = Cream93,
    inverseOnSurface = Brown20,
    error = Tomato70,
    onError = Tomato20,
    errorContainer = Tomato33,
    onErrorContainer = Tomato92,
    outline = Latte50,
    outlineVariant = Espresso20,
    scrim = Night0,
    surfaceBright = Espresso20,
    surfaceContainer = Espresso15,
    surfaceContainerHigh = Espresso15,
    surfaceContainerHighest = Espresso15,
    surfaceContainerLow = Espresso15,
    surfaceContainerLowest = Espresso6,
    surfaceDim = Espresso9,
)

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
private val LocalExtendedColors: ProvidableCompositionLocal<ExtendedColors> =
    staticCompositionLocalOf { LightExtendedColors }

/** То, чего нет в MaterialTheme: отступы и дополнительные цвета. */
object MyDayTheme {
    val spacing: Spacing
        @Composable @ReadOnlyComposable get() = LocalSpacing.current

    val colors: ExtendedColors
        @Composable @ReadOnlyComposable get() = LocalExtendedColors.current
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

@Composable
fun MyDayTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Цвета обоев доступны с Android 12, но выключены по умолчанию: иначе на телефоне
    // с Android 12+ собственная палитра MyDay не видна вообще.
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ->
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)

        darkTheme -> DarkColors
        else -> LightColors
    }
    // Дополнительные цвета не подстраиваются под обои: метки и статусы должны
    // одинаково читаться при любой схеме.
    val extendedColors = if (darkTheme) DarkExtendedColors else LightExtendedColors

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
        LocalExtendedColors provides extendedColors,
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = MyDayTypography,
            shapes = MyDayShapes,
            content = content,
        )
    }
}
