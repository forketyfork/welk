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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import me.forketyfork.welk.fonts.AppFonts
import me.forketyfork.welk.vm.ThemeViewModel
import org.koin.compose.viewmodel.koinViewModel

// Colors for the application
// Updated colors with a violet-focused palette
private val primaryColor = Color(0xFF7C4DFF)
private val primaryVariant = Color(0xFF651FFF)
private val secondaryColor = Color(0xFFCE93D8)
private val secondaryVariant = Color(0xFFAB47BC)
private val backgroundColorLight = Color(0xFFF3E5F5)
private val surfaceColorLight = Color.White
private val onSurfaceColorLight = Color(0xFF121212)

private val backgroundColorDark = Color(0xFF121212)
private val surfaceColorDark = Color(0xFF1E1E1E)
private val onSurfaceColorDark = Color.White

// Light theme colors
private val LightColors = lightColors(
    primary = primaryColor,
    primaryVariant = primaryVariant,
    secondary = secondaryColor,
    secondaryVariant = secondaryVariant,
    background = backgroundColorLight,
    surface = surfaceColorLight,
    onSurface = onSurfaceColorLight
)

private val DarkColors = darkColors(
    primary = primaryColor,
    primaryVariant = primaryVariant,
    secondary = secondaryColor,
    secondaryVariant = secondaryVariant,
    background = backgroundColorDark,
    surface = surfaceColorDark,
    onSurface = onSurfaceColorDark
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
    content: @Composable () -> Unit
) {
    val themeViewModel = koinViewModel<ThemeViewModel>()
    val themeMode = themeViewModel.themeMode.collectAsStateWithLifecycle()

    val darkTheme = themeMode.value.isDarkTheme()

    MaterialTheme(
        colors = if (darkTheme) DarkColors else LightColors,
        typography = appTypography,
        content = content
    )
}
