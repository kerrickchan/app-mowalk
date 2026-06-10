package com.mowalk.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.NumberFormat

@Composable
fun StepCircle(
    currentSteps: Int,
    goal: Int,
    modifier: Modifier = Modifier,
    size: Dp = 180.dp
) {
    val progress by animateFloatAsState(if (goal > 0) currentSteps.toFloat() / goal else 0f, label = "progress")
    val targetColor = if (progress >= 1f) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary
    val circleColor by animateColorAsState(targetColor, label = "circleColor")

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        val arcSize = size
        Canvas(Modifier.fillMaxSize()) {
            val strokeWidth = arcSize.toPx() * 0.12f
            val totalAngle = 360f * minOf(progress, 1f)

            drawArc(
                color = circleColor.copy(alpha = 0.15f),
                startAngle = 0f,
                sweepAngle = 360f,
                style = Stroke(width = strokeWidth),
                size = this.size,
                useCenter = false
            )

            drawArc(
                color = circleColor,
                startAngle = -90f,
                sweepAngle = totalAngle,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                size = this.size,
                useCenter = false
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = currentSteps.toLocaleString(),
                style = MaterialTheme.typography.displayMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            )
            Text(
                text = "steps",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun StatRow(
    icon: @Composable () -> Unit,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        icon()
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

fun Int.toLocaleString(): String {
    return NumberFormat.getIntegerInstance().format(this)
}
