package com.deepseek.balance.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.deepseek.balance.R
import com.deepseek.balance.data.db.DailyUsageSummary
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun TrendLineChart(
    dailyData: List<DailyUsageSummary>,
    modifier: Modifier = Modifier
) {
    val visibleData = rememberLast30DaysData(dailyData)
    val total = visibleData.sumOf { it.costAmount }
    val avg = if (visibleData.isNotEmpty()) total / visibleData.size else 0.0
    val peak = visibleData.maxByOrNull { it.costAmount }
    val peakCost = peak?.costAmount ?: 0.0

    GlassPanel(modifier = modifier.fillMaxWidth(), radius = 22) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    stringResource(R.string.trend_title),
                    color = Color(0xFF1A1A1A),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.weight(1f))
                Text(
                    stringResource(R.string.trend_total, String.format("%.2f", total)),
                    color = Color(0xFF333333),
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Spacer(Modifier.height(8.dp))

            val maxCost = visibleData.maxOfOrNull { it.costAmount }?.coerceAtLeast(0.01) ?: 0.01

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
            ) {
                val chartHeight = size.height - 16f
                val chartWidth = size.width
                val stepX = chartWidth / (visibleData.size - 1).coerceAtLeast(1)

                // 网格线
                repeat(3) { line ->
                    val y = 8f + line * (chartHeight / 3f)
                    drawLine(
                        color = Color.Black.copy(alpha = 0.08f),
                        start = Offset(0f, y),
                        end = Offset(chartWidth, y),
                        strokeWidth = 1f
                    )
                }

                if (visibleData.size < 2) return@Canvas

                // 折线路径
                val linePath = Path()
                val fillPath = Path()
                val points = visibleData.mapIndexed { index, item ->
                    val x = index * stepX
                    val y = chartHeight - (item.costAmount.toFloat() / maxCost.toFloat() * (chartHeight - 16f)) + 8f
                    Offset(x, y)
                }

                linePath.moveTo(points.first().x, points.first().y)
                fillPath.moveTo(points.first().x, chartHeight + 8f)
                fillPath.lineTo(points.first().x, points.first().y)

                for (i in 1 until points.size) {
                    linePath.lineTo(points[i].x, points[i].y)
                    fillPath.lineTo(points[i].x, points[i].y)
                }
                fillPath.lineTo(points.last().x, chartHeight + 8f)
                fillPath.close()

                // 渐变填充
                drawPath(
                    path = fillPath,
                    brush = Brush.verticalGradient(
                        listOf(Color(0xFF4D6BFE).copy(alpha = 0.25f), Color.Transparent)
                    )
                )

                // 折线
                drawPath(
                    path = linePath,
                    color = Color(0xFF4D6BFE),
                    style = Stroke(
                        width = 3f,
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )

                // 数据点
                points.forEach { point ->
                    drawCircle(color = Color(0xFF4D6BFE), radius = 4f, center = point)
                    drawCircle(color = Color.White, radius = 2f, center = point)
                }

                // X轴标签（每隔5天）
                val labelColor = android.graphics.Color.argb(150, 100, 100, 100)
                drawContext.canvas.nativeCanvas.apply {
                    val paint = android.graphics.Paint().apply {
                        color = labelColor
                        textSize = 22f
                        textAlign = android.graphics.Paint.Align.CENTER
                        isAntiAlias = true
                    }
                    visibleData.forEachIndexed { index, item ->
                        if (index % 5 == 0 || index == visibleData.size - 1) {
                            val monthDay = item.date.takeLast(5)
                            drawText(monthDay, index * stepX, size.height - 0f, paint)
                        }
                    }
                }
            }

            Spacer(Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    stringResource(R.string.trend_daily_avg, String.format("%.2f", avg)),
                    color = Color(0xFF666666),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                if (peak != null) {
                    Text(
                        stringResource(R.string.trend_peak, peak.date.takeLast(5), String.format("%.2f", peakCost)),
                        color = Color(0xFFFF6B6B),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

private fun rememberLast30DaysData(dailyData: List<DailyUsageSummary>): List<DailyUsageSummary> {
    val formatter = DateTimeFormatter.ISO_LOCAL_DATE
    val byDate = dailyData.associateBy { it.date }
    val today = LocalDate.now()

    return (29 downTo 0).map { daysAgo ->
        val date = today.minusDays(daysAgo.toLong()).format(formatter)
        byDate[date] ?: DailyUsageSummary(date = date, totalTokens = 0, costAmount = 0.0)
    }
}
