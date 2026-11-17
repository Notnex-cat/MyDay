package com.notnex.myday.ui.theme

import androidx.compose.material.icons.materialIcon
import androidx.compose.material.icons.materialPath
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Иконки Material, которых нет в базовом наборе material-icons-core. Контуры перенесены один
 * в один из material-icons-extended: ради шести иконок тянуть библиотеку на несколько тысяч
 * незачем — в debug-сборке она не вырезается и весит десятки мегабайт.
 *
 * Иконка строится при первом обращении (lazy) и дальше переиспользуется.
 * Новая иконка: взять контур на fonts.google.com/icons и добавить свойство по образцу.
 */
object AppIcons {

    val Key: ImageVector by lazy {
        materialIcon(name = "Filled.Key") {
            materialPath {
                moveTo(21.0f, 10.0f)
                horizontalLineToRelative(-8.35f)
                curveTo(11.83f, 7.67f, 9.61f, 6.0f, 7.0f, 6.0f)
                curveToRelative(-3.31f, 0.0f, -6.0f, 2.69f, -6.0f, 6.0f)
                reflectiveCurveToRelative(2.69f, 6.0f, 6.0f, 6.0f)
                curveToRelative(2.61f, 0.0f, 4.83f, -1.67f, 5.65f, -4.0f)
                horizontalLineTo(13.0f)
                lineToRelative(2.0f, 2.0f)
                lineToRelative(2.0f, -2.0f)
                lineToRelative(2.0f, 2.0f)
                lineToRelative(4.0f, -4.04f)
                lineTo(21.0f, 10.0f)
                close()
                moveTo(7.0f, 15.0f)
                curveToRelative(-1.65f, 0.0f, -3.0f, -1.35f, -3.0f, -3.0f)
                curveToRelative(0.0f, -1.65f, 1.35f, -3.0f, 3.0f, -3.0f)
                reflectiveCurveToRelative(3.0f, 1.35f, 3.0f, 3.0f)
                curveTo(10.0f, 13.65f, 8.65f, 15.0f, 7.0f, 15.0f)
                close()
            }
        }
    }

    val Schedule: ImageVector by lazy {
        materialIcon(name = "Filled.Schedule") {
            materialPath {
                moveTo(11.99f, 2.0f)
                curveTo(6.47f, 2.0f, 2.0f, 6.48f, 2.0f, 12.0f)
                reflectiveCurveToRelative(4.47f, 10.0f, 9.99f, 10.0f)
                curveTo(17.52f, 22.0f, 22.0f, 17.52f, 22.0f, 12.0f)
                reflectiveCurveTo(17.52f, 2.0f, 11.99f, 2.0f)
                close()
                moveTo(12.0f, 20.0f)
                curveToRelative(-4.42f, 0.0f, -8.0f, -3.58f, -8.0f, -8.0f)
                reflectiveCurveToRelative(3.58f, -8.0f, 8.0f, -8.0f)
                reflectiveCurveToRelative(8.0f, 3.58f, 8.0f, 8.0f)
                reflectiveCurveToRelative(-3.58f, 8.0f, -8.0f, 8.0f)
                close()
            }
            materialPath {
                moveTo(12.5f, 7.0f)
                horizontalLineTo(11.0f)
                verticalLineToRelative(6.0f)
                lineToRelative(5.25f, 3.15f)
                lineToRelative(0.75f, -1.23f)
                lineToRelative(-4.5f, -2.67f)
                close()
            }
        }
    }

    val Alarm: ImageVector by lazy {
        materialIcon(name = "Filled.Alarm") {
            materialPath {
                moveTo(22.0f, 5.72f)
                lineToRelative(-4.6f, -3.86f)
                lineToRelative(-1.29f, 1.53f)
                lineToRelative(4.6f, 3.86f)
                lineTo(22.0f, 5.72f)
                close()
                moveTo(7.88f, 3.39f)
                lineTo(6.6f, 1.86f)
                lineTo(2.0f, 5.71f)
                lineToRelative(1.29f, 1.53f)
                lineToRelative(4.59f, -3.85f)
                close()
                moveTo(12.5f, 8.0f)
                lineTo(11.0f, 8.0f)
                verticalLineToRelative(6.0f)
                lineToRelative(4.75f, 2.85f)
                lineToRelative(0.75f, -1.23f)
                lineToRelative(-4.0f, -2.37f)
                lineTo(12.5f, 8.0f)
                close()
                moveTo(12.0f, 4.0f)
                curveToRelative(-4.97f, 0.0f, -9.0f, 4.03f, -9.0f, 9.0f)
                reflectiveCurveToRelative(4.02f, 9.0f, 9.0f, 9.0f)
                curveToRelative(4.97f, 0.0f, 9.0f, -4.03f, 9.0f, -9.0f)
                reflectiveCurveToRelative(-4.03f, -9.0f, -9.0f, -9.0f)
                close()
                moveTo(12.0f, 20.0f)
                curveToRelative(-3.87f, 0.0f, -7.0f, -3.13f, -7.0f, -7.0f)
                reflectiveCurveToRelative(3.13f, -7.0f, 7.0f, -7.0f)
                reflectiveCurveToRelative(7.0f, 3.13f, 7.0f, 7.0f)
                reflectiveCurveToRelative(-3.13f, 7.0f, -7.0f, 7.0f)
                close()
            }
        }
    }

    // autoMirror: в языках с письмом справа налево иконка отражается.
    val MenuOpen: ImageVector by lazy {
        materialIcon(name = "AutoMirrored.Filled.MenuOpen", autoMirror = true) {
            materialPath {
                moveTo(3.0f, 18.0f)
                horizontalLineToRelative(13.0f)
                verticalLineToRelative(-2.0f)
                lineTo(3.0f, 16.0f)
                verticalLineToRelative(2.0f)
                close()
                moveTo(3.0f, 13.0f)
                horizontalLineToRelative(10.0f)
                verticalLineToRelative(-2.0f)
                lineTo(3.0f, 11.0f)
                verticalLineToRelative(2.0f)
                close()
                moveTo(3.0f, 6.0f)
                verticalLineToRelative(2.0f)
                horizontalLineToRelative(13.0f)
                lineTo(16.0f, 6.0f)
                lineTo(3.0f, 6.0f)
                close()
                moveTo(21.0f, 15.59f)
                lineTo(17.42f, 12.0f)
                lineTo(21.0f, 8.41f)
                lineTo(19.59f, 7.0f)
                lineToRelative(-5.0f, 5.0f)
                lineToRelative(5.0f, 5.0f)
                lineTo(21.0f, 15.59f)
                close()
            }
        }
    }

    val StarHalf: ImageVector by lazy {
        materialIcon(name = "AutoMirrored.Rounded.StarHalf", autoMirror = true) {
            materialPath {
                moveTo(19.65f, 9.04f)
                lineToRelative(-4.84f, -0.42f)
                lineToRelative(-1.89f, -4.45f)
                curveToRelative(-0.34f, -0.81f, -1.5f, -0.81f, -1.84f, 0.0f)
                lineTo(9.19f, 8.63f)
                lineToRelative(-4.83f, 0.41f)
                curveToRelative(-0.88f, 0.07f, -1.24f, 1.17f, -0.57f, 1.75f)
                lineToRelative(3.67f, 3.18f)
                lineToRelative(-1.1f, 4.72f)
                curveToRelative(-0.2f, 0.86f, 0.73f, 1.54f, 1.49f, 1.08f)
                lineToRelative(4.15f, -2.5f)
                lineToRelative(4.15f, 2.51f)
                curveToRelative(0.76f, 0.46f, 1.69f, -0.22f, 1.49f, -1.08f)
                lineToRelative(-1.1f, -4.73f)
                lineToRelative(3.67f, -3.18f)
                curveToRelative(0.67f, -0.58f, 0.32f, -1.68f, -0.56f, -1.75f)
                close()
                moveTo(12.0f, 15.4f)
                verticalLineTo(6.1f)
                lineToRelative(1.71f, 4.04f)
                lineToRelative(4.38f, 0.38f)
                lineToRelative(-3.32f, 2.88f)
                lineToRelative(1.0f, 4.28f)
                lineTo(12.0f, 15.4f)
                close()
            }
        }
    }

    val StarOutline: ImageVector by lazy {
        materialIcon(name = "Rounded.StarOutline") {
            materialPath {
                moveTo(19.65f, 9.04f)
                lineToRelative(-4.84f, -0.42f)
                lineToRelative(-1.89f, -4.45f)
                curveToRelative(-0.34f, -0.81f, -1.5f, -0.81f, -1.84f, 0.0f)
                lineTo(9.19f, 8.63f)
                lineToRelative(-4.83f, 0.41f)
                curveToRelative(-0.88f, 0.07f, -1.24f, 1.17f, -0.57f, 1.75f)
                lineToRelative(3.67f, 3.18f)
                lineToRelative(-1.1f, 4.72f)
                curveToRelative(-0.2f, 0.86f, 0.73f, 1.54f, 1.49f, 1.08f)
                lineToRelative(4.15f, -2.5f)
                lineToRelative(4.15f, 2.51f)
                curveToRelative(0.76f, 0.46f, 1.69f, -0.22f, 1.49f, -1.08f)
                lineToRelative(-1.1f, -4.73f)
                lineToRelative(3.67f, -3.18f)
                curveToRelative(0.67f, -0.58f, 0.32f, -1.68f, -0.56f, -1.75f)
                close()
                moveTo(12.0f, 15.4f)
                lineToRelative(-3.76f, 2.27f)
                lineToRelative(1.0f, -4.28f)
                lineToRelative(-3.32f, -2.88f)
                lineToRelative(4.38f, -0.38f)
                lineTo(12.0f, 6.1f)
                lineToRelative(1.71f, 4.04f)
                lineToRelative(4.38f, 0.38f)
                lineToRelative(-3.32f, 2.88f)
                lineToRelative(1.0f, 4.28f)
                lineTo(12.0f, 15.4f)
                close()
            }
        }
    }
}
