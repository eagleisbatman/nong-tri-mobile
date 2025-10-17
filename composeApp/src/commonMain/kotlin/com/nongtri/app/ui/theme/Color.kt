package com.nongtri.app.ui.theme

import androidx.compose.ui.graphics.Color

// Brand Colors - Nông Trí Green
object NongTriColors {
    // Primary Green - represents agriculture, growth
    val Primary = Color(0xFF2E7D32) // Dark green
    val PrimaryVariant = Color(0xFF1B5E20) // Darker green
    val PrimaryLight = Color(0xFF4CAF50) // Light green

    // Secondary Colors
    val Secondary = Color(0xFF66BB6A) // Medium green
    val SecondaryVariant = Color(0xFF81C784) // Lighter green

    // Accent
    val Accent = Color(0xFF8BC34A) // Light lime green
}

// Light Theme Colors
object LightColors {
    val Background = Color(0xFFFAFAFA)
    val Surface = Color(0xFFFFFFFF)
    val SurfaceVariant = Color(0xFFF5F5F5)

    val OnBackground = Color(0xFF1C1C1C)
    val OnSurface = Color(0xFF1C1C1C)
    val OnSurfaceVariant = Color(0xFF666666)

    val Primary = NongTriColors.Primary
    val OnPrimary = Color(0xFFFFFFFF)

    val Secondary = NongTriColors.Secondary
    val OnSecondary = Color(0xFFFFFFFF)

    // Message bubbles
    val UserMessageBubble = NongTriColors.Primary
    val UserMessageText = Color(0xFFFFFFFF)
    val AiMessageBubble = Color(0xFFF0F0F0)
    val AiMessageText = Color(0xFF1C1C1C)

    // Borders and dividers
    val Border = Color(0xFFE0E0E0)
    val Divider = Color(0xFFE0E0E0)

    // Status colors
    val Success = Color(0xFF4CAF50)
    val Error = Color(0xFFE53935)
    val Warning = Color(0xFFFF9800)
    val Info = Color(0xFF2196F3)
}

// Dark Theme Colors
object DarkColors {
    val Background = Color(0xFF121212)
    val Surface = Color(0xFF1E1E1E)
    val SurfaceVariant = Color(0xFF2C2C2C)

    val OnBackground = Color(0xFFE0E0E0)
    val OnSurface = Color(0xFFE0E0E0)
    val OnSurfaceVariant = Color(0xFFB0B0B0)

    val Primary = NongTriColors.PrimaryLight
    val OnPrimary = Color(0xFF000000)

    val Secondary = NongTriColors.SecondaryVariant
    val OnSecondary = Color(0xFF000000)

    // Message bubbles
    val UserMessageBubble = NongTriColors.PrimaryLight
    val UserMessageText = Color(0xFF000000)
    val AiMessageBubble = Color(0xFF2C2C2C)
    val AiMessageText = Color(0xFFE0E0E0)

    // Borders and dividers
    val Border = Color(0xFF3C3C3C)
    val Divider = Color(0xFF3C3C3C)

    // Status colors
    val Success = Color(0xFF66BB6A)
    val Error = Color(0xFFEF5350)
    val Warning = Color(0xFFFFB74D)
    val Info = Color(0xFF42A5F5)
}
