package me.forketyfork.welk.components

import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import java.time.Instant.ofEpochMilli
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.time.Clock
import kotlin.time.Instant

private val FORMATTER = DateTimeFormatter.ofPattern("MMM d, HH:mm")

@Composable
fun ReviewStatusIndicator(nextReview: Instant?) {
    val text: String
    val color: Color

    if (nextReview == null) {
        // Never reviewed
        text = "New"
        color = Color.Green
    } else {
        val now = Instant.fromEpochMilliseconds(Clock.System.now().toEpochMilliseconds())
        if (nextReview <= now) {
            text = "Due now"
            color = Color.Green
        } else {
            // Format the date and time for display
            val epochMillis = nextReview.toEpochMilliseconds()
            val date = LocalDateTime.ofInstant(ofEpochMilli(epochMillis), ZoneId.systemDefault())
            text = date.format(FORMATTER)
            color = Color.Blue
        }
    }

    Text(
        text = text,
        style = MaterialTheme.typography.caption,
        color = color,
        fontSize = 12.sp
    )
}
