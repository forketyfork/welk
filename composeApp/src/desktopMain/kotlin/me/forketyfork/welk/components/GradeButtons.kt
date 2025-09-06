package me.forketyfork.welk.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.forketyfork.welk.domain.ReviewGrade
import me.forketyfork.welk.theme.AppTheme

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
                backgroundColor = AppTheme.colors.gradeRed,
                contentColor = AppTheme.colors.textSecondary
            )
        ) {
            Text("Again (1)", maxLines = 1, fontSize = 14.sp)
        }
        Button(
            onClick = { onGrade(ReviewGrade.HARD) },
            modifier = Modifier.weight(1f).defaultMinSize(minWidth = 100.dp),
            colors = ButtonDefaults.buttonColors(
                backgroundColor = AppTheme.colors.gradeYellow,
                contentColor = AppTheme.colors.textSecondary
            )
        ) {
            Text("Hard (2)", maxLines = 1, fontSize = 14.sp)
        }
        Button(
            onClick = { onGrade(ReviewGrade.GOOD) },
            modifier = Modifier.weight(1f).defaultMinSize(minWidth = 100.dp),
            colors = ButtonDefaults.buttonColors(
                backgroundColor = AppTheme.colors.gradeGreen,
                contentColor = AppTheme.colors.textSecondary
            )
        ) {
            Text("Good (3)", maxLines = 1, fontSize = 14.sp)
        }
        Button(
            onClick = { onGrade(ReviewGrade.EASY) },
            modifier = Modifier.weight(1f).defaultMinSize(minWidth = 100.dp),
            colors = ButtonDefaults.buttonColors(
                backgroundColor = AppTheme.colors.gradeBlue,
                contentColor = AppTheme.colors.textSecondary
            )
        ) {
            Text("Easy (4)", maxLines = 1, fontSize = 14.sp)
        }
    }
}

