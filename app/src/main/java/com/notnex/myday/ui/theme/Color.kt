package com.notnex.myday.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

// Палитра MyDay по мотивам приложения Burger King: бежевая страница, кремовые карточки,
// шоколадный текст, жёлтая главная кнопка, оранжевые и красные акценты, чёрные блоки.
// У оригинала нет тёмной темы; тёмная собрана по его же чёрным блокам: страница цвета эспрессо,
// карточки чуть светлее, текст кремовый, жёлтый и оранжевый остаются теми же.
// Здесь только тона, роли раздаёт Theme.kt. Число в имени — примерная светлота (0 — чёрный,
// 100 — белый). Все цвета непрозрачные: с альфой контраст зависел бы от фона под ними.

// Шоколад — весь текст. Приглушённый тон темнее, чем в оригинале: иначе подписи не проходят AA.
val Brown20: Color = Color(0xFF502314)
val Brown25: Color = Color(0xFF3A2117)
val Brown35: Color = Color(0xFF704C3D)
val Brown45: Color = Color(0xFF8A6A5C)

// Песок и крем — поверхности: страница, карточки и самое светлое (выбранное, чипы).
val Sand80: Color = Color(0xFFDCCDB2)
val Sand83: Color = Color(0xFFDDCFB4)
val Sand88: Color = Color(0xFFE9DDC5)
val Cream93: Color = Color(0xFFF4EBDC)
val Cream97: Color = Color(0xFFFBF6F0)

// Эспрессо и латте — поверхности и вторичный текст тёмной темы.
val Espresso6: Color = Color(0xFF0F0B09)
val Espresso9: Color = Color(0xFF16110E)
val Espresso15: Color = Color(0xFF2A211C)
val Espresso20: Color = Color(0xFF3A2E27)
val Latte50: Color = Color(0xFF94796D)
val Latte70: Color = Color(0xFFB8A194)
val Latte78: Color = Color(0xFFCDB8AA)

// Горчица — главная кнопка и FAB.
val Mustard70: Color = Color(0xFFFFAA00)

// Апельсин — голос ассистента. Яркий тон только как подложка, для текста — тёмный.
val Orange15: Color = Color(0xFF4A1E00)
val Orange35: Color = Color(0xFF943F07)
val Orange60: Color = Color(0xFFEB7A2A)
val Orange75: Color = Color(0xFFFFA766)

// Томат — акцентная заливка (акции, «активировать») и ошибки. Для текста ошибок — тёмный тон.
val Tomato20: Color = Color(0xFF5C1000)
val Tomato30: Color = Color(0xFF6E1200)
val Tomato33: Color = Color(0xFF8C1D00)
val Tomato35: Color = Color(0xFFA51B00)
val Tomato45: Color = Color(0xFFD62300)
val Tomato70: Color = Color(0xFFFF8F78)
val Tomato92: Color = Color(0xFFFADBD0)

// Огурец — успех. Темнее зелёного оригинала, чтобы годился и для текста.
val Pickle15: Color = Color(0xFF003914)
val Pickle25: Color = Color(0xFF0B4A1D)
val Pickle30: Color = Color(0xFF124A22)
val Pickle35: Color = Color(0xFF116329)
val Pickle70: Color = Color(0xFF5EC47B)
val Pickle90: Color = Color(0xFFC8F0D2)
val Pickle92: Color = Color(0xFFD6EDD8)

// Ночь — чёрные блоки с тёмными карточками внутри.
val Night0: Color = Color(0xFF000000)
val Night16: Color = Color(0xFF2B2A28)
val Night65: Color = Color(0xFFA79A92)

// Золото — градиент итоговой суммы.
val Gold70: Color = Color(0xFFD2A973)
val Gold85: Color = Color(0xFFEACE9C)

val White: Color = Color(0xFFFFFFFF)

/** Подложка и цвет текста поверх неё — всегда парой, чтобы контраст не разошёлся. */
@Immutable
data class Tint(
    val container: Color,
    val content: Color,
)

/**
 * Цвета сверх ролей Material 3. Берутся через `MyDayTheme.colors`.
 *
 * `promo` — красная заливка для акцентных действий, `night*` — чёрный блок с тёмными
 * карточками и выбранным кремовым сегментом, `goldStart`/`goldEnd` — концы градиента
 * для итоговой суммы, метки — пастель для тем, привычек и пунктов расписания.
 */
@Immutable
data class ExtendedColors(
    val success: Color,
    val onSuccess: Color,
    val successContainer: Color,
    val onSuccessContainer: Color,
    val promo: Color,
    val onPromo: Color,
    val night: Color,
    val onNight: Color,
    val nightContainer: Color,
    val onNightContainer: Color,
    val onNightVariant: Color,
    val nightSelected: Color,
    val onNightSelected: Color,
    val goldStart: Color,
    val goldEnd: Color,
    val onGold: Color,
    val orange: Tint,
    val yellow: Tint,
    val red: Tint,
    val green: Tint,
    val brown: Tint,
    val cream: Tint,
)

val LightExtendedColors: ExtendedColors = ExtendedColors(
    success = Pickle35,
    onSuccess = White,
    successContainer = Pickle92,
    onSuccessContainer = Pickle25,
    promo = Tomato45,
    onPromo = White,
    night = Night0,
    onNight = Cream97,
    nightContainer = Night16,
    onNightContainer = Cream97,
    onNightVariant = Night65,
    nightSelected = Cream97,
    onNightSelected = Brown20,
    goldStart = Gold70,
    goldEnd = Gold85,
    onGold = Brown20,
    orange = Tint(container = Color(0xFFFCDCC4), content = Color(0xFF6B2C00)),
    yellow = Tint(container = Color(0xFFFFE7B0), content = Color(0xFF5A3C00)),
    red = Tint(container = Color(0xFFF9D6CB), content = Color(0xFF7A1500)),
    green = Tint(container = Pickle92, content = Pickle25),
    brown = Tint(container = Color(0xFFE6D2C8), content = Brown20),
    cream = Tint(container = Cream97, content = Brown20),
)

// Чёрный блок и золото одинаковы в обеих темах: на странице эспрессо чёрный всё равно темнее.
val DarkExtendedColors: ExtendedColors = ExtendedColors(
    success = Pickle70,
    onSuccess = Pickle15,
    successContainer = Pickle30,
    onSuccessContainer = Pickle90,
    promo = Tomato45,
    onPromo = White,
    night = Night0,
    onNight = Cream97,
    nightContainer = Night16,
    onNightContainer = Cream97,
    onNightVariant = Night65,
    nightSelected = Cream97,
    onNightSelected = Brown20,
    goldStart = Gold70,
    goldEnd = Gold85,
    onGold = Brown20,
    orange = Tint(container = Color(0xFF5A2A0E), content = Color(0xFFFFD9BF)),
    yellow = Tint(container = Color(0xFF4A3500), content = Color(0xFFFFE3A3)),
    red = Tint(container = Color(0xFF5C1A0E), content = Color(0xFFFFD3C7)),
    green = Tint(container = Pickle30, content = Pickle90),
    brown = Tint(container = Color(0xFF3D2A20), content = Color(0xFFEAD6CB)),
    cream = Tint(container = Cream93, content = Brown20),
)
