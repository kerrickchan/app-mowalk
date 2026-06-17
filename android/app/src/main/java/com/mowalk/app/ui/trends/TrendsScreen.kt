package com.mowalk.app.ui.trends

import android.app.Application
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mowalk.app.R
import com.mowalk.app.ui.components.EmptyStateCard
import com.mowalk.app.ui.components.toLocaleString

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrendsScreen(
    onBackClick: () -> Unit,
    viewModel: TrendsViewModel
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.trends_title), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            SegmentedControl(state.period) { period ->
                viewModel.setPeriod(period)
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (state.dataPoints.isEmpty()) {
                EmptyStateCard(
                    title = stringResource(R.string.empty_state_title),
                    description = stringResource(R.string.empty_state_description)
                )
            } else {
                LineChart(
                    dataPoints = state.dataPoints,
                    onPointClick = { day ->
                        viewModel.onSelectDay(day)
                    }
                )
            }

            val selectedDay = state.selectedDay
            if (selectedDay != null) {
                DetailBottomSheet(selectedDay) {
                    viewModel.onDismissDetail()
                }
            }
        }
    }
}

@Composable
private fun SegmentedControl(
    currentPeriod: TrendsPeriod,
    onPeriodChange: (TrendsPeriod) -> Unit
) {
    val options = listOf(TrendsPeriod.WEEK, TrendsPeriod.MONTH)
    val labels = listOf(stringResource(R.string.period_week), stringResource(R.string.period_month))
    val selectedIndex = options.indexOf(currentPeriod)

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            options.forEachIndexed { index, period ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onPeriodChange(period) }
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = labels[index],
                        style = MaterialTheme.typography.labelLarge,
                        color = if (index == selectedIndex) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        fontWeight = if (index == selectedIndex) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }
    }
}

@Composable
private fun LineChart(
    dataPoints: List<com.mowalk.app.data.local.DailyStepEntity>,
    onPointClick: (com.mowalk.app.data.local.DailyStepEntity) -> Unit
) {
    var selectedIndex by remember { mutableIntStateOf(-1) }
    val gridColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f)
    val primaryColor = MaterialTheme.colorScheme.primary
    val surfaceColor = MaterialTheme.colorScheme.onSurface

    Column(modifier = Modifier.fillMaxWidth()) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        ) {
            if (dataPoints.isEmpty()) return@Canvas

            val padding = 40f
            val chartWidth = size.width - padding * 2
            val chartHeight = size.height - padding * 2

            val maxSteps = dataPoints.maxOfOrNull { it.steps }?.toFloat() ?: 1f
            val minSteps = 0f

            val points = dataPoints.mapIndexed { index, entity ->
                val x = padding + (index.toFloat() / maxOf(dataPoints.size - 1, 1) * chartWidth)
                val y = padding + chartHeight - ((entity.steps.toFloat() - minSteps) / (maxSteps - minSteps) * chartHeight)
                Offset(x, y)
            }

            // Grid lines
            for (i in 0..4) {
                val y = padding + (i.toFloat() / 4 * chartHeight)
                drawLine(
                    color = gridColor,
                    start = Offset(padding, y),
                    end = Offset(size.width - padding, y),
                    strokeWidth = 1f
                )
            }

            // Line
            if (points.size > 1) {
                val path = Path().apply {
                    moveTo(points[0].x, points[0].y)
                    for (i in 1 until points.size) {
                        lineTo(points[i].x, points[i].y)
                    }
                }
                drawPath(path, primaryColor, style = Stroke(width = 3f))
            }

            // Points
            points.forEachIndexed { index, point ->
                val isSelected = index == selectedIndex
                val radius = if (isSelected) 8f else 5f
                drawCircle(
                    color = if (isSelected) primaryColor else surfaceColor,
                    radius = radius,
                    center = point
                )

                // Day label
                val dayLabel = dataPoints.getOrNull(index)?.date?.substring(8) ?: ""
                drawContext.canvas.nativeCanvas.apply {
                    val paint = android.graphics.Paint().apply {
                        color = android.graphics.Color.parseColor(if (isSelected) "#006C4C" else "#666666")
                        textSize = if (isSelected) 14f else 11f
                        textAlign = android.graphics.Paint.Align.CENTER
                    }
                    drawText(dayLabel, point.x, size.height - 8f, paint)
                }
            }
        }

        if (selectedIndex >= 0 && selectedIndex < dataPoints.size) {
            Spacer(modifier = Modifier.height(8.dp))
            val day = dataPoints[selectedIndex]
            Text(
                text = "${day.date}: ${day.steps.toLocaleString()} steps",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DetailBottomSheet(
    day: com.mowalk.app.data.local.DailyStepEntity,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = day.date,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "${day.steps.toLocaleString()} steps",
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = "%.1f km".format(day.distance / 1000f),
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = "${day.calories.toInt()} kcal",
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onDismiss) {
                Text("Close")
            }
        }
    }
}
