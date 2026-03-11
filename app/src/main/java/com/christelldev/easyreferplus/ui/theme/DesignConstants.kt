package com.christelldev.easyreferplus.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

// ==================== DISEÑO ELEGANTE - EasyRefer ====================

// Constantes de Cards
object DesignConstants {
    val CARD_CORNER_RADIUS = 20.dp
    val CARD_ELEVATION = 2.dp  // Reducido de 4dp a 2dp para look más limpio
    val CARD_MARGIN_HORIZONTAL = 16.dp
    val CARD_MIN_HEIGHT = 100.dp
    val SECTION_SPACING = 20.dp

    // Colores principales
    val PrimaryColor = Color(0xFF03A9F4)
    val PrimaryDark = Color(0xFF1976D2)
    val SecondaryColor = Color(0xFF2196F3)

    // Colores de estado
    val SuccessColor = Color(0xFF10B981)
    val WarningColor = Color(0xFFF59E0B)
    val ErrorColor = Color(0xFFF44336)
    val InfoColor = Color(0xFF03A9F4)

    // Gradientes
    val GradientPrimary = listOf(Color(0xFF03A9F4), Color(0xFF2196F3))
    val GradientSecondary = listOf(Color(0xFF06B6D4), Color(0xFF3B82F6))
    val GradientSuccess = listOf(Color(0xFF10B981), Color(0xFF34D399))
    val GradientWarning = listOf(Color(0xFFF59E0B), Color(0xFFFBBF24))
    val GradientPurple = listOf(Color(0xFF8B5CF6), Color(0xFFA78BFA))
    val GradientPink = listOf(Color(0xFFEC4899), Color(0xFFF472B6))
    val GradientBlue = listOf(Color(0xFF03A9F4), Color(0xFF2196F3))
    val GradientGreen = listOf(Color(0xFF10B981), Color(0xFF34D399))
    val GradientOrange = listOf(Color(0xFFF59E0B), Color(0xFFFBBF24))
    val GradientTeal = listOf(Color(0xFF14B8A6), Color(0xFF2DD4BF))
    val GradientRed = listOf(Color(0xFFEF4444), Color(0xFFF87171))

    // Colores de fondo - Modo Claro
    val SurfaceLight = Color(0xFFF8FAFC)
    val SurfaceCard = Color(0xFFFFFFFF)
    val BackgroundLight = Color(0xFFF1F5F9)

    // Colores de fondo - Modo Oscuro
    val SurfaceDark = Color(0xFF1E1E1E)
    val SurfaceCardDark = Color(0xFF2D2D2D)
    val BackgroundDark = Color(0xFF121212)

    // Colores de texto
    val TextPrimary = Color(0xFF1E293B)
    val TextSecondary = Color(0xFF64748B)
    val TextOnPrimary = Color(0xFFFFFFFF)
    
    // Colores de texto - Modo Oscuro
    val TextPrimaryDark = Color(0xFFE2E8F0)
    val TextSecondaryDark = Color(0xFF94A3B8)
}
