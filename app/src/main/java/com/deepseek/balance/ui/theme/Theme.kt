package com.deepseek.balance.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF4A90D9),
    onPrimary = Color.White,
    primaryContainer = Color(0xFF1A3A5C),
    onPrimaryContainer = Color(0xFFD0E4FF),
    secondary = Color(0xFF5AC8FA),
    surface = Color(0xFF121212),
    surfaceVariant = Color(0xFF1E1E1E),
    onSurface = Color(0xFFE0E0E0),
    onSurfaceVariant = Color(0xFFB0B0B0),
    error = Color(0xFFFF6B6B),
    background = Color(0xFF0D0D0D)
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF4A90D9),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD6E4FF),
    onPrimaryContainer = Color(0xFF001B3E),
    secondary = Color(0xFF0EA5E9),
    surface = Color(0xFFFAFBFC),
    surfaceVariant = Color(0xFFF1F5F9),
    onSurface = Color(0xFF1E293B),
    onSurfaceVariant = Color(0xFF64748B),
    error = Color(0xFFEF4444),
    background = Color(0xFFFFFFFF)
)

@Composable
fun DeepSeekBalanceTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography(),
        content = content
    )
}
