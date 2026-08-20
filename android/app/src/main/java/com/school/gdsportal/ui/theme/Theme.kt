package com.school.gdsportal.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val BaseColorScheme = lightColorScheme(
    primary = AccentAdministrator, // Default to admin amber for unauthenticated state (login screen, loading)
    secondary = TextSecondary,
    background = BackgroundColor,
    surface = BackgroundColor,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    error = ErrorColor
)

@Composable
fun GDSPortalTheme(
    // Lets callers give each role its own brand color (e.g. AccentAdministrator vs
    // AccentPrincipal) without duplicating the whole color scheme. Since MaterialTheme.colorScheme.primary
    // is used throughout (buttons, active nav items, dashboard stat numbers, quick-action icons),
    // switching this one value re-skins the whole app for that role.
    primaryColor: Color = AccentAdministrator,
    content: @Composable () -> Unit
) {
    val colorScheme = BaseColorScheme.copy(primary = primaryColor) // We force light theme per design system background
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = colorScheme.background.toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = true
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}