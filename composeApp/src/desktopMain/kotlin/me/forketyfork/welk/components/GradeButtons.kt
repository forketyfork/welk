package me.forketyfork.welk.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.forketyfork.welk.domain.ReviewGrade

@Composable
fun GradeButtons(
    onGrade: (ReviewGrade) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Button(
            onClick = { onGrade(ReviewGrade.AGAIN) },
            modifier = Modifier.weight(1f).defaultMinSize(minWidth = 100.dp),
            colors = ButtonDefaults.buttonColors(
                backgroundColor = Color.Red.copy(alpha = 0.2f),
                contentColor = MaterialTheme.colors.onSurface
            )
        ) {
            Text("Again (1)", maxLines = 1, fontSize = 14.sp)
        }
        Button(
            onClick = { onGrade(ReviewGrade.HARD) },
            modifier = Modifier.weight(1f).defaultMinSize(minWidth = 100.dp),
            colors = ButtonDefaults.buttonColors(
                backgroundColor = Color.Yellow.copy(alpha = 0.2f),
                contentColor = MaterialTheme.colors.onSurface
            )
        ) {
            Text("Hard (2)", maxLines = 1, fontSize = 14.sp)
        }
        Button(
            onClick = { onGrade(ReviewGrade.GOOD) },
            modifier = Modifier.weight(1f).defaultMinSize(minWidth = 100.dp),
            colors = ButtonDefaults.buttonColors(
                backgroundColor = Color.Green.copy(alpha = 0.2f),
                contentColor = MaterialTheme.colors.onSurface
            )
        ) {
            Text("Good (3)", maxLines = 1, fontSize = 14.sp)
        }
        Button(
            onClick = { onGrade(ReviewGrade.EASY) },
            modifier = Modifier.weight(1f).defaultMinSize(minWidth = 100.dp),
            colors = ButtonDefaults.buttonColors(
                backgroundColor = Color.Blue.copy(alpha = 0.2f),
                contentColor = MaterialTheme.colors.onSurface
            )
        ) {
            Text("Easy (4)", maxLines = 1, fontSize = 14.sp)
        }
    }
}

