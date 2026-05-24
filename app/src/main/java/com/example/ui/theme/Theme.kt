package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
    darkColorScheme(
        primary = Color(0xFFD0BCFF),
        onPrimary = Color(0xFF381E72),
        primaryContainer = Color(0xFF4A4458),
        onPrimaryContainer = Color(0xFFEADDFF),
        secondary = Color(0xFFCCC4D0),
        onSecondary = Color(0xFF332D41),
        secondaryContainer = Color(0xFFEADDFF),
        onSecondaryContainer = Color(0xFF21005D),
        tertiary = Color(0xFFEFB8C8),
        onTertiary = Color(0xFF492532),
        background = Color(0xFF1C1B1F),
        onBackground = Color(0xFFE6E1E5),
        surface = Color(0xFF2B2930),
        onSurface = Color(0xFFE6E1E5),
        surfaceVariant = Color(0xFF4A4458),
        onSurfaceVariant = Color(0xFFCCC4D0),
        outline = Color(0xFF938F99),
        outlineVariant = Color(0xFF36343B),
        inverseOnSurface = Color(0xFF1C1B1F),
        inverseSurface = Color(0xFFE6E1E5),
        inversePrimary = Color(0xFF6750A4)
    )

private val LightColorScheme =
    lightColorScheme(
        primary = Color(0xFF6750A4),
        onPrimary = Color.White,
        primaryContainer = Color(0xFFEADDFF),
        onPrimaryContainer = Color(0xFF21005D),
        secondary = Color(0xFF625B71),
        onSecondary = Color.White,
        secondaryContainer = Color(0xFFE8DEF8),
        onSecondaryContainer = Color(0xFF1D192B),
        background = Color(0xFFFFFBFE),
        onBackground = Color(0xFF1C1B1F),
        surface = Color(0xFFFFFBFE),
        onSurface = Color(0xFF1C1B1F),
        surfaceVariant = Color(0xFFE7E0EC),
        onSurfaceVariant = Color(0xFF49454F),
        outline = Color(0xFF79747E)
    )

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Set dynamicColor to false by default to ensure the professional theme is displayed
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme =
        when {
            dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                val context = LocalContext.current
                if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            }

            darkTheme -> DarkColorScheme
            else -> DarkColorScheme // Use Dark theme as default to fit Sleek "Professional Polish" aesthetic
        }

    MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
