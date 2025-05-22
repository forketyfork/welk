package me.forketyfork.welk.theme

import androidx.compose.material.MaterialTheme
import androidx.compose.material.Typography
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import me.forketyfork.welk.fonts.AppFonts

// Colors for the application
// Updated colors with a violet-focused palette
private val primaryColor = Color(0xFF7C4DFF)
private val primaryVariant = Color(0xFF651FFF)
private val secondaryColor = Color(0xFFCE93D8)
private val secondaryVariant = Color(0xFFAB47BC)
private val backgroundColor = Color(0xFFF3E5F5)
private val surfaceColor = Color.White
private val onSurfaceColor = Color(0xFF121212)

// Dark theme specific colors
private val darkBackgroundColor = Color(0xFF121212)
private val darkSurfaceColor = Color(0xFF1E1E1E)
private val darkOnSurfaceColor = Color.White

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
    secondaryVariant = secondaryVariant,
    background = darkBackgroundColor,
    surface = darkSurfaceColor,
    onSurface = darkOnSurfaceColor
)

// Typography settings using our custom fonts
private val appTypography = Typography(
    h1 = TextStyle(
        fontFamily = AppFonts.openSans,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp
    ),
    h2 = TextStyle(
        fontFamily = AppFonts.openSans,
        fontWeight = FontWeight.SemiBold,
        fontSize = 26.sp
    ),
    h3 = TextStyle(
        fontFamily = AppFonts.openSans,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp
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
        fontFamily = AppFonts.openSans,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp
    ),
    body2 = TextStyle(
        fontFamily = AppFonts.openSans,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp
    ),
    button = TextStyle(
        fontFamily = AppFonts.openSans,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp
    ),
    caption = TextStyle(
        fontFamily = AppFonts.openSans,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp
    )
)

@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colors = if (darkTheme) DarkColors else LightColors,
        typography = appTypography,
        content = content
    )
}