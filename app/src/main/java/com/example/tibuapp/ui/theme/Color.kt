package com.example.tibuapp.ui.theme

import androidx.compose.ui.graphics.Color

// ── Azul Apple (predeterminado) ──────────────────────────────
val AzulApple     = Color(0xFF007AFF)
val AzulSuave     = Color(0xFFE8F3FF)

// ── Otros colores de acento ──────────────────────────────────
val AmbarApple    = Color(0xFFFF9500)
val VerdeApple    = Color(0xFF34C759)
val MoradoApple   = Color(0xFFAF52DE)
val RosaApple     = Color(0xFFFF2D55)
val IndigoApple   = Color(0xFF5856D6)
val TealApple     = Color(0xFF00C7BE)
val NegroApple    = Color(0xFF1C1C1E)


val Blanco = Color(0xFFFFFFFF)
val Gris200 = Color(0xFFE0E0E0)
val Gris500 = Color(0xFF9E9E9E)
val Gris900 = Color(0xFF212121)
val Naranja100 = Color(0xFFFFE0B2)
val Naranja500 = Color(0xFFFF9800)

// ── Neutros estilo Apple ─────────────────────────────────────
// Light mode
val BlancoL       = Color(0xFFFFFFFF)
val Gris1L        = Color(0xFFF2F2F7)  // Fondo de pantalla
val Gris2L        = Color(0xFFE5E5EA)  // Separadores
val Gris3L        = Color(0xFFD1D1D6)  // Bordes
val Gris4L        = Color(0xFFAEAEB2)  // Texto terciario
val Gris5L        = Color(0xFF8E8E93)  // Texto secundario
val Gris6L        = Color(0xFF636366)  // Texto
val OscuroL       = Color(0xFF1C1C1E)  // Texto principal

// Dark mode
val Negro1D       = Color(0xFF000000)  // Fondo
val Gris1D        = Color(0xFF1C1C1E)  // Superficie 1
val Gris2D        = Color(0xFF2C2C2E)  // Superficie 2
val Gris3D        = Color(0xFF3A3A3C)  // Superficie 3
val Gris4D        = Color(0xFF48484A)  // Bordes
val Gris5D        = Color(0xFF636366)  // Texto terciario
val Gris6D        = Color(0xFF8E8E93)  // Texto secundario
val BlancoD       = Color(0xFFFFFFFF)  // Texto principal

// ── Semánticos ────────────────────────────────────────────────
val Verde500      = Color(0xFF34C759)
val Verde100L     = Color(0xFFE8FFF0)
val Verde800L     = Color(0xFF1A7A3C)
val Verde100D     = Color(0xFF0A2E18)
val Verde800D     = Color(0xFF4CD964)

val Rojo500       = Color(0xFFFF3B30)
val Rojo100L      = Color(0xFFFFEEED)
val Rojo800L      = Color(0xFFCC2B22)
val Rojo100D      = Color(0xFF2E0A09)
val Rojo800D      = Color(0xFFFF6961)

val Amarillo500   = Color(0xFFFFCC00)
val Amarillo100L  = Color(0xFFFFFDE0)
val Amarillo800L  = Color(0xFF8A7000)
val Amarillo100D  = Color(0xFF2E2900)
val Amarillo800D  = Color(0xFFFFD60A)

// Convierte un string hex a Color de Compose
fun hexAColor(hex: String): Color {
    return try {
        Color(android.graphics.Color.parseColor(hex))
    } catch (e: Exception) {
        AzulApple
    }
}

// Genera un color suave (10% de opacidad) para fondos de íconos
fun colorSuave(color: Color): Color {
    return color.copy(alpha = 0.12f)
}