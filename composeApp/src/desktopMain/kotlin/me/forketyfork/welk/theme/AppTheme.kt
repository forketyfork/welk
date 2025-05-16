package me.forketyfork.welk.theme

import androidx.compose.material.MaterialTheme
import androidx.compose.material.Typography
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import me.forketyfork.welk.fonts.AppFonts

// Colors for the application
private val primaryColor = Color(0xFF6200EE)
private val primaryVariant = Color(0xFF3700B3)
private val secondaryColor = Color(0xFF03DAC5)
private val secondaryVariant = Color(0xFF018786)
private val backgroundColor = Color(0xFFF5F5F5)
private val surfaceColor = Color.White
private val onSurfaceColor = Color(0xFF121212)

// Light theme colors
private val LightColors = lightColors(
    primary = primaryColor,
    primaryVariant = primaryVariant,
    secondary = secondaryColor,
    secondaryVariant = secondaryVariant,
    background = backgroundColor,
    surface = surfaceColor,
    onSurface = onSurfaceColor
)

// Dark theme colors - not used yet but prepared for future
private val DarkColors = darkColors(
    primary = primaryColor,
    primaryVariant = primaryVariant,
    secondary = secondaryColor,
    secondaryVariant = secondaryVariant
)

// Typography settings using our custom fonts
private val appTypography = Typography(
    h1 = TextStyle(
        fontFamily = AppFonts.openSans,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp
    ),
    h2 = TextStyle(
        fontFamily = AppFonts.openSans,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp
    ),
    h3 = TextStyle(
        fontFamily = AppFonts.openSans,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp
    ),
    h4 = TextStyle(
        fontFamily = AppFonts.openSans,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp
    ),
    h5 = TextStyle(
        fontFamily = AppFonts.openSans,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp
    ),
    h6 = TextStyle(
        fontFamily = AppFonts.openSans,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp
    ),
    body1 = TextStyle(
        fontFamily = AppFonts.roboto,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp
    ),
    body2 = TextStyle(
        fontFamily = AppFonts.roboto,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp
    ),
    button = TextStyle(
        fontFamily = AppFonts.roboto,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp
    )
)

@Composable
fun AppTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colors = LightColors,
        typography = appTypography,
        content = content
    )
}