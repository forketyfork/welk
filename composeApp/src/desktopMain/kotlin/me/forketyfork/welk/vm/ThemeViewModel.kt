package me.forketyfork.welk.vm

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.SettingsBrightness
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class ThemeMode(
    val icon: ImageVector,
    val contentDescription: String,
) {
    SYSTEM(
        icon = Icons.Default.SettingsBrightness,
        contentDescription = "System theme (click to change)",
    ) {
        @Composable
        override fun isDarkTheme(): Boolean = isSystemInDarkTheme()
    },
    LIGHT(
        icon = Icons.Default.LightMode,
        contentDescription = "Light theme (click to change)",
    ) {
        @Composable
        override fun isDarkTheme(): Boolean = false
    },
    DARK(
        icon = Icons.Default.DarkMode,
        contentDescription = "Dark theme (click to change)",
    ) {
        @Composable
        override fun isDarkTheme(): Boolean = true
    }, ;

    @Composable
    abstract fun isDarkTheme(): Boolean

    fun next(): ThemeMode =
        when (this) {
            SYSTEM -> LIGHT
            LIGHT -> DARK
            DARK -> SYSTEM
        }
}

/**
 * ViewModel for managing theme preferences.
 */
class ThemeViewModel : ViewModel() {
    private val _themeMode = MutableStateFlow(ThemeMode.SYSTEM)
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    /**
     * Toggles between theme modes in the order: SYSTEM -> LIGHT -> DARK -> SYSTEM
     */
    fun toggleThemeMode() {
        _themeMode.value = _themeMode.value.next()
    }
}
