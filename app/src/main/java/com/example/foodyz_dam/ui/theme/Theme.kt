package com.example.foodyz_dam.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

// Palette marque (jaune)
object BrandColors {
    val Yellow = Color(0xFFFFD54F)
    val Amber  = Color(0xFFFFB300)
    val Orange = Color(0xFFF59E0B)
    val Brown  = Color(0xFF8B5E00)

    val SurfaceSoft  = Color(0xFFFFF8E1)
    val SurfaceSoft2 = Color(0xFFFFF3C4)
    val TextPrimary   = Color(0xFF1F1F1F)
    val TextSecondary = Color(0xFF6B6B6B)
    val Error         = Color(0xFFEF4444)

    // Optionnel: dégradés prêts à l’emploi
    val BrandGradient = Brush.horizontalGradient(listOf(Yellow, Amber, Orange))
    val BgGradient    = Brush.verticalGradient(listOf(Color(0xFFFFFBF0), SurfaceSoft))
}

private val DarkColorScheme = darkColorScheme(
    primary = BrandColors.Amber,
    onPrimary = Color.Black,
    primaryContainer = BrandColors.Brown,
    onPrimaryContainer = Color.White,

    secondary = BrandColors.Orange,
    onSecondary = Color.Black,

    background = Color(0xFF121212),
    onBackground = Color(0xFFEDEDED),
    surface = Color(0xFF1C1C1C),
    onSurface = Color(0xFFEDEDED),
    surfaceVariant = Color(0xFF2A2A2A),
    onSurfaceVariant = Color(0xFFB5B5B5),

    error = BrandColors.Error,
    onError = Color.White,
    outline = Color(0xFF8A8A8A)
)

private val LightColorScheme = lightColorScheme(
    primary = BrandColors.Amber,
    onPrimary = Color.Black,
    primaryContainer = BrandColors.Yellow,
    onPrimaryContainer = BrandColors.Brown,

    secondary = BrandColors.Orange,
    onSecondary = Color.Black,
    secondaryContainer = BrandColors.SurfaceSoft2,
    onSecondaryContainer = BrandColors.TextPrimary,

    background = BrandColors.SurfaceSoft,
    onBackground = BrandColors.TextPrimary,
    surface = Color.White,
    onSurface = BrandColors.TextPrimary,
    surfaceVariant = BrandColors.SurfaceSoft2,
    onSurfaceVariant = BrandColors.TextSecondary,

    error = BrandColors.Error,
    onError = Color.White,
    outline = BrandColors.TextSecondary
)

@Composable
fun FoodyzDamTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Mets false pour forcer la palette jaune et ignorer Material You
    dynamicColor: Boolean = false,
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