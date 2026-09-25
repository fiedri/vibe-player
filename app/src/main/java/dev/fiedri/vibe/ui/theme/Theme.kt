package dev.fiedri.vibe.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf

val LocalVibeColors = staticCompositionLocalOf { VibeDarkColors }
val LocalVibeTypography = staticCompositionLocalOf { VibeTypography }

// Mapeo a Material 3 para componentes estándar de Material
val MaterialTypography = Typography(
    displayLarge = VibeTypography.display,
    titleLarge = VibeTypography.titleLarge,
    titleMedium = VibeTypography.titleMedium,
    bodyLarge = VibeTypography.bodyLarge,
    bodyMedium = VibeTypography.bodyMedium,
    labelSmall = VibeTypography.caption
)

private val VibeDarkColorScheme = darkColorScheme(
    background = VibeDarkColors.background,
    onBackground = VibeDarkColors.foreground,
    surface = VibeDarkColors.cards,
    onSurface = VibeDarkColors.foreground,
    surfaceVariant = VibeDarkColors.muted,
    onSurfaceVariant = VibeDarkColors.mutedForeground,
    primary = VibeDarkColors.primary,
    onPrimary = VibeDarkColors.foreground,
    secondary = VibeDarkColors.secondary,
    onSecondary = VibeDarkColors.foreground,
    error = VibeDarkColors.destructive,
    onError = VibeDarkColors.foreground,
    outline = VibeDarkColors.border,
    outlineVariant = VibeDarkColors.border
)

private val VibeLightColorScheme = lightColorScheme(
    background = VibeLightColors.background,
    onBackground = VibeLightColors.foreground,
    surface = VibeLightColors.cards,
    onSurface = VibeLightColors.foreground,
    surfaceVariant = VibeLightColors.muted,
    onSurfaceVariant = VibeLightColors.mutedForeground,
    primary = VibeLightColors.primary,
    onPrimary = VibeLightColors.foreground,
    secondary = VibeLightColors.secondary,
    onSecondary = VibeLightColors.foreground,
    error = VibeLightColors.destructive,
    onError = VibeLightColors.foreground,
    outline = VibeLightColors.border,
    outlineVariant = VibeLightColors.border
)

@Composable
fun VibeTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    //val colors = if (darkTheme) VibeDarkColors else VibeLightColors
val colors = VibeDarkColors
    CompositionLocalProvider(
        LocalVibeColors provides colors,
        LocalVibeTypography provides VibeTypography,
        LocalContentColor provides colors.foreground

    ) {
        MaterialTheme(
            colorScheme = if (darkTheme) VibeDarkColorScheme else VibeLightColorScheme,
            typography = MaterialTypography,
            content = content
        )
    }
}

object VibeTheme {
    val colors: VibeColors
        @Composable
        @ReadOnlyComposable
        get() = LocalVibeColors.current

    val typography: VibeTypography
        @Composable
        @ReadOnlyComposable
        get() = LocalVibeTypography.current
}