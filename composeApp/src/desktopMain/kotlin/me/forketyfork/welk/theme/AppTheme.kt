package me.forketyfork.welk.theme

import androidx.compose.material.MaterialTheme
import androidx.compose.material.Typography
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import me.forketyfork.welk.fonts.AppFonts
import me.forketyfork.welk.vm.ThemeViewModel
import org.koin.compose.viewmodel.koinViewModel

// Colors for the application
object AppColors {
    // Primary colors - violet-focused palette
    val primaryLight = Color(0xFF7C4DFF)
    val primaryDark = Color(0xFFBB86FC)
    val primaryVariantLight = Color(0xFF651FFF)
    val primaryVariantDark = Color(0xFF9E58FF)

    // Secondary colors
    val secondaryLight = Color(0xFFCE93D8)
    val secondaryDark = Color(0xFF8E24AA)
    val secondaryVariantLight = Color(0xFFAB47BC)
    val secondaryVariantDark = Color(0xFF6A1B9A)

    // Background and surface colors
    val backgroundLight = Color(0xFFF3E5F5)
    val backgroundDark = Color(0xFF121212)
    val surfaceLight = Color.White
    val surfaceDark = Color(0xFF1E1E1E)
    val onSurfaceLight = Color(0xFF121212)
    val onSurfaceDark = Color.White

    // Error colors
    val errorLight = Color(0xFFB00020)
    val errorDark = Color(0xFFCF6679)
    val onErrorLight = Color.White
    val onErrorDark = Color.Black

    // Grade button colors
    val gradeRedLight = Color.Red.copy(alpha = 0.2f)
    val gradeRedDark = Color(0xFF442222)
    val gradeYellowLight = Color.Yellow.copy(alpha = 0.2f)
    val gradeYellowDark = Color(0xFF443322)
    val gradeGreenLight = Color.Green.copy(alpha = 0.2f)
    val gradeGreenDark = Color(0xFF224422)
    val gradeBlueLight = Color.Blue.copy(alpha = 0.2f)
    val gradeBlueDark = Color(0xFF222244)

    // Status indicator colors
    val statusGreenLight = Color(0xFF2E7D32)
    val statusGreenDark = Color(0xFF4CAF50)
    val statusBlueLight = Color.Blue
    val statusBlueDark = Color(0xFF2196F3)

    // Animation colors
    val animationGreenLight = Color(0xFF2E7D32)
    val animationGreenDark = Color(0xFF4CAF50)
    val animationRedLight = Color.Red
    val animationRedDark = Color(0xFFF44336)

    // Transparent
    val transparent = Color.Transparent

    // Divider colors
    val dividerLight = Color(0xFF121212).copy(alpha = 0.12f)
    val dividerDark = Color.White.copy(alpha = 0.12f)

    // Selection colors
    val selectionLight = primaryLight.copy(alpha = 0.1f)
    val selectionDark = primaryDark.copy(alpha = 0.2f)

    // Text secondary colors
    val textSecondaryLight = onSurfaceLight.copy(alpha = 0.7f)
    val textSecondaryDark = onSurfaceDark.copy(alpha = 0.7f)
    val textDisabledLight = onSurfaceLight.copy(alpha = 0.6f)
    val textDisabledDark = onSurfaceDark.copy(alpha = 0.6f)
}

// Light theme colors
private val LightColors =
    lightColors(
        primary = AppColors.primaryLight,
        primaryVariant = AppColors.primaryVariantLight,
        secondary = AppColors.secondaryLight,
        secondaryVariant = AppColors.secondaryVariantLight,
        background = AppColors.backgroundLight,
        surface = AppColors.surfaceLight,
        onSurface = AppColors.onSurfaceLight,
        error = AppColors.errorLight,
        onError = AppColors.onErrorLight,
    )

private val DarkColors =
    darkColors(
        primary = AppColors.primaryDark,
        primaryVariant = AppColors.primaryVariantDark,
        secondary = AppColors.secondaryDark,
        secondaryVariant = AppColors.secondaryVariantDark,
        background = AppColors.backgroundDark,
        surface = AppColors.surfaceDark,
        onSurface = AppColors.onSurfaceDark,
        error = AppColors.errorDark,
        onError = AppColors.onErrorDark,
    )

// Typography settings using our custom fonts
private val appTypography =
    Typography(
        h1 =
            TextStyle(
                fontFamily = AppFonts.openSans,
                fontWeight = FontWeight.Bold,
                fontSize = 32.sp,
            ),
        h2 =
            TextStyle(
                fontFamily = AppFonts.openSans,
                fontWeight = FontWeight.SemiBold,
                fontSize = 26.sp,
            ),
        h3 =
            TextStyle(
                fontFamily = AppFonts.openSans,
                fontWeight = FontWeight.SemiBold,
                fontSize = 22.sp,
            ),
        h4 =
            TextStyle(
                fontFamily = AppFonts.openSans,
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp,
            ),
        h5 =
            TextStyle(
                fontFamily = AppFonts.openSans,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
            ),
        h6 =
            TextStyle(
                fontFamily = AppFonts.openSans,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
            ),
        body1 =
            TextStyle(
                fontFamily = AppFonts.openSans,
                fontWeight = FontWeight.Normal,
                fontSize = 16.sp,
            ),
        body2 =
            TextStyle(
                fontFamily = AppFonts.openSans,
                fontWeight = FontWeight.Normal,
                fontSize = 14.sp,
            ),
        button =
            TextStyle(
                fontFamily = AppFonts.openSans,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
            ),
        caption =
            TextStyle(
                fontFamily = AppFonts.openSans,
                fontWeight = FontWeight.Normal,
                fontSize = 12.sp,
            ),
    )

// Extension properties for custom colors
data class ExtendedColors(
    val gradeRed: Color,
    val gradeYellow: Color,
    val gradeGreen: Color,
    val gradeBlue: Color,
    val statusGreen: Color,
    val statusBlue: Color,
    val animationGreen: Color,
    val animationRed: Color,
    val transparent: Color,
    val divider: Color,
    val selection: Color,
    val textSecondary: Color,
    val textDisabled: Color,
    val onSurface: Color,
    val error: Color,
    val primary: Color,
    val secondary: Color,
)

object AppTheme {
    val colors: ExtendedColors
        @Composable
        get() = LocalExtendedColors.current

    val typography: Typography
        @Composable
        get() = MaterialTheme.typography
}

private val LocalExtendedColors =
    staticCompositionLocalOf {
        ExtendedColors(
            gradeRed = AppColors.gradeRedLight,
            gradeYellow = AppColors.gradeYellowLight,
            gradeGreen = AppColors.gradeGreenLight,
            gradeBlue = AppColors.gradeBlueLight,
            statusGreen = AppColors.statusGreenLight,
            statusBlue = AppColors.statusBlueLight,
            animationGreen = AppColors.animationGreenLight,
            animationRed = AppColors.animationRedLight,
            transparent = AppColors.transparent,
            divider = AppColors.dividerLight,
            selection = AppColors.selectionLight,
            textSecondary = AppColors.textSecondaryLight,
            textDisabled = AppColors.textDisabledLight,
            onSurface = AppColors.onSurfaceLight,
            error = AppColors.errorLight,
            primary = AppColors.primaryLight,
            secondary = AppColors.secondaryLight,
        )
    }

@Composable
fun AppTheme(content: @Composable () -> Unit) {
    val themeViewModel = koinViewModel<ThemeViewModel>()
    val themeMode = themeViewModel.themeMode.collectAsStateWithLifecycle()

    val darkTheme = themeMode.value.isDarkTheme()

    val extendedColors =
        if (darkTheme) {
            ExtendedColors(
                gradeRed = AppColors.gradeRedDark,
                gradeYellow = AppColors.gradeYellowDark,
                gradeGreen = AppColors.gradeGreenDark,
                gradeBlue = AppColors.gradeBlueDark,
                statusGreen = AppColors.statusGreenDark,
                statusBlue = AppColors.statusBlueDark,
                animationGreen = AppColors.animationGreenDark,
                animationRed = AppColors.animationRedDark,
                transparent = AppColors.transparent,
                divider = AppColors.dividerDark,
                selection = AppColors.selectionDark,
                textSecondary = AppColors.textSecondaryDark,
                textDisabled = AppColors.textDisabledDark,
                onSurface = AppColors.onSurfaceDark,
                error = AppColors.errorDark,
                primary = AppColors.primaryDark,
                secondary = AppColors.secondaryDark,
            )
        } else {
            ExtendedColors(
                gradeRed = AppColors.gradeRedLight,
                gradeYellow = AppColors.gradeYellowLight,
                gradeGreen = AppColors.gradeGreenLight,
                gradeBlue = AppColors.gradeBlueLight,
                statusGreen = AppColors.statusGreenLight,
                statusBlue = AppColors.statusBlueLight,
                animationGreen = AppColors.animationGreenLight,
                animationRed = AppColors.animationRedLight,
                transparent = AppColors.transparent,
                divider = AppColors.dividerLight,
                selection = AppColors.selectionLight,
                textSecondary = AppColors.textSecondaryLight,
                textDisabled = AppColors.textDisabledLight,
                onSurface = AppColors.onSurfaceLight,
                error = AppColors.errorLight,
                primary = AppColors.primaryLight,
                secondary = AppColors.secondaryLight,
            )
        }

    CompositionLocalProvider(
        LocalExtendedColors provides extendedColors,
    ) {
        MaterialTheme(
            colors = if (darkTheme) DarkColors else LightColors,
            typography = appTypography,
            content = content,
        )
    }
}
