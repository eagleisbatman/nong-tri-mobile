package com.nongtri.app.ui.theme

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.nongtri.app.R

/**
 * Be Vietnam Pro - Vietnamese-optimized font family
 *
 * Specifically designed for Vietnamese typography with:
 * - Clear, properly-sized diacritics
 * - Excellent readability for Vietnamese text
 * - Proper kerning for Vietnamese characters
 * - Neo-Grotesk style suitable for modern UI/UX
 */
private val BeVietnamProFontFamily = FontFamily(
    Font(R.font.be_vietnam_pro_regular, FontWeight.Normal),
    Font(R.font.be_vietnam_pro_medium, FontWeight.Medium),
    Font(R.font.be_vietnam_pro_semibold, FontWeight.SemiBold),
    Font(R.font.be_vietnam_pro_bold, FontWeight.Bold)
)

/**
 * Actual implementation of AppFontFamily for Android
 * Uses Be Vietnam Pro font optimized for Vietnamese text
 */
actual val AppFontFamily: FontFamily = BeVietnamProFontFamily
