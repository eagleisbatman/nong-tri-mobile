package com.nongtri.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = LightColors.Primary,
    onPrimary = LightColors.OnPrimary,
    secondary = LightColors.Secondary,
    onSecondary = LightColors.OnSecondary,
    background = LightColors.Background,
    onBackground = LightColors.OnBackground,
    surface = LightColors.Surface,
    onSurface = LightColors.OnSurface,
    surfaceVariant = LightColors.SurfaceVariant,
    onSurfaceVariant = LightColors.OnSurfaceVariant,
    error = LightColors.Error,
    outline = LightColors.Border
)

private val DarkColorScheme = darkColorScheme(
    primary = DarkColors.Primary,
    onPrimary = DarkColors.OnPrimary,
    secondary = DarkColors.Secondary,
    onSecondary = DarkColors.OnSecondary,
    background = DarkColors.Background,
    onBackground = DarkColors.OnBackground,
    surface = DarkColors.Surface,
    onSurface = DarkColors.OnSurface,
    surfaceVariant = DarkColors.SurfaceVariant,
    onSurfaceVariant = DarkColors.OnSurfaceVariant,
    error = DarkColors.Error,
    outline = DarkColors.Border
)

@Composable
fun NongTriTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = NongTriTypography,
        content = content
    )
}
