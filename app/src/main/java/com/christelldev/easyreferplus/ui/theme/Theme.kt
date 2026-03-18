package com.christelldev.easyreferplus.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// ==================== COLORES UNIFICADOS (MODERNO 2026) ====================
val LightPrimary = Color(0xFF00AEEF)          // Azul celeste principal
val LightOnPrimary = Color(0xFFFFFFFF)
val LightPrimaryContainer = Color(0xFFB3E5FC)
val LightOnPrimaryContainer = Color(0xFF001F29)

val LightSecondary = Color(0xFF03DAC6)           // Verde azulado
val LightOnSecondary = Color(0xFFFFFFFF)
val LightSecondaryContainer = Color(0xFFB2F2EC)
val LightOnSecondaryContainer = Color(0xFF00201D)

val LightTertiary = Color(0xFFFF6B6B)           // Coral suave
val LightOnTertiary = Color(0xFFFFFFFF)
val LightTertiaryContainer = Color(0xFFFFDAD6)
val LightOnTertiaryContainer = Color(0xFF410002)

val LightBackground = Color(0xFFF1F5F9)         // Gris azulado muy claro
val LightOnBackground = Color(0xFF1B1B1F)
val LightSurface = Color(0xFFFFFFFF)
val LightOnSurface = Color(0xFF1B1B1F)
val LightSurfaceVariant = Color(0xFFF1F5F9)
val LightOnSurfaceVariant = Color(0xFF49454F)

val LightError = Color(0xFFBA1A1A)
val LightOnError = Color(0xFFFFFFFF)
val LightErrorContainer = Color(0xFFFFDAD6)
val LightOnErrorContainer = Color(0xFF410002)

val LightOutline = Color(0xFF79747E)
val LightOutlineVariant = Color(0xFFCAC4D0)

// ==================== COLORES MODO OSCURO ====================
val DarkPrimary = Color(0xFF6DD5FA)              // Azul celeste claro
val DarkOnPrimary = Color(0xFF00354A)
val DarkPrimaryContainer = Color(0xFF004D64)
val DarkOnPrimaryContainer = Color(0xFFB3E5FC)

val DarkSecondary = Color(0xFF4FFFD6)            // Verde menta
val DarkOnSecondary = Color(0xFF003731)
val DarkSecondaryContainer = Color(0xFF005047)
val DarkOnSecondaryContainer = Color(0xFFB2F2EC)

val DarkTertiary = Color(0xFFFFB4AB)             // Coral claro
val DarkOnTertiary = Color(0xFF690005)
val DarkTertiaryContainer = Color(0xFF93000A)
val DarkOnTertiaryContainer = Color(0xFFFFDAD6)

val DarkBackground = Color(0xFF0F172A)            // Azul marino muy oscuro
val DarkOnBackground = Color(0xFFE2E8F0)
val DarkSurface = Color(0xFF1E293B)               // Azul marino oscuro
val DarkOnSurface = Color(0xFFE2E8F0)
val DarkSurfaceVariant = Color(0xFF334155)
val DarkOnSurfaceVariant = Color(0xFFCAC4D0)

val DarkError = Color(0xFFFFB4AB)
val DarkOnError = Color(0xFF690005)
val DarkErrorContainer = Color(0xFF93000A)
val DarkOnErrorContainer = Color(0xFFFFDAD6)

val DarkOutline = Color(0xFF938F99)
val DarkOutlineVariant = Color(0xFF44474E)

// ==================== ESQUEMAS DE COLOR ====================
private val LightColorScheme = lightColorScheme(
    primary = LightPrimary,
    onPrimary = LightOnPrimary,
    primaryContainer = LightPrimaryContainer,
    onPrimaryContainer = LightOnPrimaryContainer,
    secondary = LightSecondary,
    onSecondary = LightOnSecondary,
    secondaryContainer = LightSecondaryContainer,
    onSecondaryContainer = LightOnSecondaryContainer,
    tertiary = LightTertiary,
    onTertiary = LightOnTertiary,
    tertiaryContainer = LightTertiaryContainer,
    onTertiaryContainer = LightOnTertiaryContainer,
    background = LightBackground,
    onBackground = LightOnBackground,
    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightOnSurfaceVariant,
    error = LightError,
    onError = LightOnError,
    errorContainer = LightErrorContainer,
    onErrorContainer = LightOnErrorContainer,
    outline = LightOutline,
    outlineVariant = LightOutlineVariant
)

private val DarkColorScheme = darkColorScheme(
    primary = DarkPrimary,
    onPrimary = DarkOnPrimary,
    primaryContainer = DarkPrimaryContainer,
    onPrimaryContainer = DarkOnPrimaryContainer,
    secondary = DarkSecondary,
    onSecondary = DarkOnSecondary,
    secondaryContainer = DarkSecondaryContainer,
    onSecondaryContainer = DarkOnSecondaryContainer,
    tertiary = DarkTertiary,
    onTertiary = DarkOnTertiary,
    tertiaryContainer = DarkTertiaryContainer,
    onTertiaryContainer = DarkOnTertiaryContainer,
    background = DarkBackground,
    onBackground = DarkOnBackground,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,
    error = DarkError,
    onError = DarkOnError,
    errorContainer = DarkErrorContainer,
    onErrorContainer = DarkOnErrorContainer,
    outline = DarkOutline,
    outlineVariant = DarkOutlineVariant
)

// ==================== TEMA PRINCIPAL ====================
@Composable
fun EasyReferPlusTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
