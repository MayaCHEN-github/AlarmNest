package edu.cuhk.csci3310.csci3310project.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val ColorScheme = darkColorScheme(
    background = AppBackground,
    onBackground = AppText,
    surface = AppSurface,
    onSurface = AppOnSurface,
    primary = AppSurface,
    onPrimary = AppOnSurface,
    secondary = AppSurface,
    onSecondary = AppOnSurface,
    tertiary = AppSurface,
    onTertiary = AppOnSurface
)

@Composable
fun CSCI3310ProjectTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = ColorScheme,
        typography = Typography,
        content = content
    )
}