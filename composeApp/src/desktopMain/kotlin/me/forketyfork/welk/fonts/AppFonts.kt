package me.forketyfork.welk.fonts

import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.platform.Font

object AppFonts {
    // Open Sans is a free-to-use font from Google Fonts
    val openSans = FontFamily(
        Font(
            resource = "font/opensans_regular.ttf",
            weight = FontWeight.Normal,
            style = FontStyle.Normal
        ),
        Font(
            resource = "font/opensans_bold.ttf",
            weight = FontWeight.Bold,
            style = FontStyle.Normal
        )
    )
}