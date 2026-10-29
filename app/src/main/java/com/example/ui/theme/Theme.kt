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

private val DarkColorScheme = darkColorScheme(
    primary = TealLight,
    onPrimary = SlateDark,
    primaryContainer = TealDark,
    onPrimaryContainer = TealSubtle,
    secondary = AmberStar,
    onSecondary = SlateDark,
    secondaryContainer = AmberDark,
    onSecondaryContainer = AmberLight,
    tertiary = EmeraldSuccess,
    background = DarkSurfaceBackground,
    onBackground = Color(0xFFF8FAFC),
    surface = DarkSurfaceCard,
    onSurface = Color(0xFFF8FAFC),
    surfaceVariant = Color(0xFF334155),
    onSurfaceVariant = Color(0xFFCBD5E1),
    outline = Color(0xFF475569)
)

private val LightColorScheme = lightColorScheme(
    primary = ElectricTeal,
    onPrimary = SurfaceWhite,
    primaryContainer = TealSubtle,
    onPrimaryContainer = TealDark,
    secondary = AmberDark,
    onSecondary = SurfaceWhite,
    secondaryContainer = AmberLight,
    onSecondaryContainer = Color(0xFF78350F),
    tertiary = EmeraldSuccess,
    background = Color(0xFFF8FAFC),
    onBackground = SlateDark,
    surface = SurfaceWhite,
    onSurface = SlateDark,
    surfaceVariant = SlateLight,
    onSurfaceVariant = SlateMuted,
    outline = SlateBorder
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Use intentional branded palette for consistency
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
