package com.example.tibuapp.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Colores que cambian dinámicamente
data class TibuColores(
    val acento: Color,
    val acentoSuave: Color,
    val fondo: Color,
    val superficie1: Color,
    val superficie2: Color,
    val superficie3: Color,
    val borde: Color,
    val bordeSubtle: Color,
    val textoPrimario: Color,
    val textoSecundario: Color,
    val textoTerciario: Color,
    val esNoche: Boolean
)

val LocalTibuColores = compositionLocalOf {
    TibuColores(
        acento = AzulApple,
        acentoSuave = AzulSuave,
        fondo = Gris1L,
        superficie1 = BlancoL,
        superficie2 = Gris1L,
        superficie3 = Gris2L,
        borde = Gris3L,
        bordeSubtle = Gris2L,
        textoPrimario = OscuroL,
        textoSecundario = Gris5L,
        textoTerciario = Gris4L,
        esNoche = false
    )
}

@Composable
fun TibuappTheme(
    colorAcento: String = "#007AFF",
    modoNoche: Boolean = false,
    content: @Composable () -> Unit
) {
    val acento = hexAColor(colorAcento)
    val acentoSuave = colorSuave(acento)

    val tibuColores = if (modoNoche) {
        TibuColores(
            acento = acento,
            acentoSuave = acentoSuave,
            fondo = Negro1D,
            superficie1 = Gris1D,
            superficie2 = Gris2D,
            superficie3 = Gris3D,
            borde = Gris4D,
            bordeSubtle = Gris3D,
            textoPrimario = BlancoD,
            textoSecundario = Gris6D,
            textoTerciario = Gris5D,
            esNoche = true
        )
    } else {
        TibuColores(
            acento = acento,
            acentoSuave = acentoSuave,
            fondo = Gris1L,
            superficie1 = BlancoL,
            superficie2 = Gris1L,
            superficie3 = Gris2L,
            borde = Gris3L,
            bordeSubtle = Gris2L,
            textoPrimario = OscuroL,
            textoSecundario = Gris5L,
            textoTerciario = Gris4L,
            esNoche = false
        )
    }

    val colorScheme = if (modoNoche) {
        darkColorScheme(
            primary          = acento,
            onPrimary        = BlancoD,
            primaryContainer = acentoSuave,
            onPrimaryContainer = acento,
            secondary        = Gris6D,
            onSecondary      = BlancoD,
            secondaryContainer = Gris2D,
            onSecondaryContainer = BlancoD,
            background       = Negro1D,
            onBackground     = BlancoD,
            surface          = Gris1D,
            onSurface        = BlancoD,
            surfaceVariant   = Gris2D,
            onSurfaceVariant = Gris6D,
            outline          = Gris4D,
            error            = Rojo800D,
            onError          = BlancoD,
            errorContainer   = Rojo100D,
            onErrorContainer = Rojo800D
        )
    } else {
        lightColorScheme(
            primary          = acento,
            onPrimary        = BlancoL,
            primaryContainer = acentoSuave,
            onPrimaryContainer = acento,
            secondary        = Gris6L,
            onSecondary      = BlancoL,
            secondaryContainer = Gris1L,
            onSecondaryContainer = OscuroL,
            background       = Gris1L,
            onBackground     = OscuroL,
            surface          = BlancoL,
            onSurface        = OscuroL,
            surfaceVariant   = Gris1L,
            onSurfaceVariant = Gris5L,
            outline          = Gris3L,
            error            = Rojo800L,
            onError          = BlancoL,
            errorContainer   = Rojo100L,
            onErrorContainer = Rojo800L
        )
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = if (modoNoche) Gris1D.toArgb() else acento.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !modoNoche
        }
    }

    CompositionLocalProvider(LocalTibuColores provides tibuColores) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = TibuTypography,
            shapes = TibuShapes,
            content = content
        )
    }
}

// Acceso fácil desde cualquier Composable
val MaterialTheme.tibu: TibuColores
    @Composable get() = LocalTibuColores.current