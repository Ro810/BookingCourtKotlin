package com.example.bookingcourt.presentation.analytics.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bookingcourt.domain.model.DailyRevenue
import com.example.bookingcourt.domain.model.TimeSlotStats
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.min

/**
 * Bar chart hiển thị doanh thu theo ngày
 */
@Composable
fun RevenueBarChart(
    data: List<DailyRevenue>,
    modifier: Modifier = Modifier,
    barColor: Color = MaterialTheme.colorScheme.primary
) {
    if (data.isEmpty()) {
        Box(
            modifier = modifier.fillMaxWidth().height(200.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("Chưa có dữ liệu", style = MaterialTheme.typography.bodyMedium)
        }
        return
    }

    val maxRevenue = data.maxOfOrNull { it.revenue } ?: 1L

    val animatedProgress = remember { Animatable(0f) }

    LaunchedEffect(data) {
        animatedProgress.animateTo(1f, animationSpec = tween(1000))
    }

    Canvas(modifier = modifier.fillMaxWidth().height(250.dp).padding(16.dp)) {
        val barWidth = size.width / (data.size * 1.5f)
        val spacing = barWidth * 0.5f
        val chartHeight = size.height - 50.dp.toPx()

        data.forEachIndexed { index, dailyRevenue ->
            val barHeight = (dailyRevenue.revenue.toFloat() / maxRevenue) * chartHeight * animatedProgress.value
            val x = index * (barWidth + spacing) + spacing

            // Draw bar
            drawRoundRect(
                color = barColor,
                topLeft = Offset(x, size.height - 40.dp.toPx() - barHeight),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
            )

            // Draw date label
            val dateText = "${dailyRevenue.date.dayOfMonth}/${dailyRevenue.date.monthNumber}"

            drawContext.canvas.nativeCanvas.apply {
                drawText(
                    dateText,
                    x + barWidth / 2,
                    size.height - 10.dp.toPx(),
                    android.graphics.Paint().apply {
                        color = android.graphics.Color.GRAY
                        textSize = 10.sp.toPx()
                        textAlign = android.graphics.Paint.Align.CENTER
                    }
                )
            }
        }
    }
}

/**
 * Simple pie chart hiển thị phân bố booking theo status
 */
@Composable
fun BookingStatusPieChart(
    confirmed: Int,
    pending: Int,
    rejected: Int,
    cancelled: Int,
    modifier: Modifier = Modifier
) {
    val total = confirmed + pending + rejected + cancelled

    if (total == 0) {
        Box(
            modifier = modifier.fillMaxWidth().height(200.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("Chưa có dữ liệu", style = MaterialTheme.typography.bodyMedium)
        }
        return
    }

    val confirmedColor = Color(0xFF4CAF50) // Green
    val pendingColor = Color(0xFFFFC107) // Amber
    val rejectedColor = Color(0xFFF44336) // Red
    val cancelledColor = Color(0xFF9E9E9E) // Gray

    val data = listOf(
        "Đã duyệt" to Pair(confirmed, confirmedColor),
        "Chờ duyệt" to Pair(pending, pendingColor),
        "Từ chối" to Pair(rejected, rejectedColor),
        "Đã hủy" to Pair(cancelled, cancelledColor)
    ).filter { it.second.first > 0 }

    val animatedProgress = remember { Animatable(0f) }

    LaunchedEffect(total) {
        animatedProgress.animateTo(1f, animationSpec = tween(1000))
    }

    Column(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .padding(16.dp)
        ) {
            val radius = min(size.width, size.height) / 2.5f
            val center = Offset(size.width / 2, size.height / 2)

            var currentAngle = -90f // Start from top

            data.forEach { (_, pair) ->
                val (count, color) = pair
                val sweepAngle = (count.toFloat() / total) * 360f * animatedProgress.value

                drawArc(
                    color = color,
                    startAngle = currentAngle,
                    sweepAngle = sweepAngle,
                    useCenter = true,
                    topLeft = Offset(center.x - radius, center.y - radius),
                    size = Size(radius * 2, radius * 2)
                )

                currentAngle += sweepAngle
            }
        }

        // Legend
        Column(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            data.forEach { (label, pair) ->
                val (count, color) = pair
                val percentage = (count.toFloat() / total * 100).toInt()

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            modifier = Modifier.size(16.dp),
                            color = color,
                            shape = RoundedCornerShape(4.dp)
                        ) {}
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = label,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    Text(
                        text = "$count ($percentage%)",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

/**
 * Line chart đơn giản cho time slot stats
 */
@Composable
fun TimeSlotLineChart(
    data: List<TimeSlotStats>,
    modifier: Modifier = Modifier,
    lineColor: Color = MaterialTheme.colorScheme.primary
) {
    if (data.isEmpty()) {
        Box(
            modifier = modifier.fillMaxWidth().height(200.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("Chưa có dữ liệu", style = MaterialTheme.typography.bodyMedium)
        }
        return
    }

    val sortedData = data.sortedBy { it.hour }
    val maxBookings = sortedData.maxOfOrNull { it.bookingCount } ?: 1

    val animatedProgress = remember { Animatable(0f) }

    LaunchedEffect(data) {
        animatedProgress.animateTo(1f, animationSpec = tween(1000))
    }

    Canvas(modifier = modifier.fillMaxWidth().height(200.dp).padding(16.dp)) {
        val chartHeight = size.height - 40.dp.toPx()
        val stepX = size.width / (sortedData.size - 1).coerceAtLeast(1)

        // Draw grid lines
        for (i in 0..4) {
            val y = i * chartHeight / 4
            drawLine(
                color = Color.LightGray,
                start = Offset(0f, y),
                end = Offset(size.width, y),
                strokeWidth = 1.dp.toPx()
            )
        }

        // Draw line chart
        sortedData.forEachIndexed { index, stats ->
            if (index < sortedData.size - 1) {
                val nextStats = sortedData[index + 1]

                val y1 = chartHeight - (stats.bookingCount.toFloat() / maxBookings * chartHeight * animatedProgress.value)
                val y2 = chartHeight - (nextStats.bookingCount.toFloat() / maxBookings * chartHeight * animatedProgress.value)

                val x1 = index * stepX
                val x2 = (index + 1) * stepX

                drawLine(
                    color = lineColor,
                    start = Offset(x1, y1),
                    end = Offset(x2, y2),
                    strokeWidth = 3.dp.toPx()
                )
            }

            // Draw data points
            val y = chartHeight - (stats.bookingCount.toFloat() / maxBookings * chartHeight * animatedProgress.value)
            val x = index * stepX

            drawCircle(
                color = lineColor,
                radius = 6.dp.toPx(),
                center = Offset(x, y)
            )

            drawCircle(
                color = Color.White,
                radius = 3.dp.toPx(),
                center = Offset(x, y)
            )

            // Draw hour label
            if (index % 3 == 0) { // Show every 3 hours
                drawContext.canvas.nativeCanvas.apply {
                    drawText(
                        "${stats.hour}h",
                        x,
                        size.height - 5.dp.toPx(),
                        android.graphics.Paint().apply {
                            color = android.graphics.Color.GRAY
                            textSize = 10.sp.toPx()
                            textAlign = android.graphics.Paint.Align.CENTER
                        }
                    )
                }
            }
        }
    }
}

/**
 * Format number to currency
 */
fun formatCurrency(amount: Long): String {
    val formatter = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))
    return formatter.format(amount)
}

/**
 * Format number with separator
 */
fun formatNumber(number: Int): String {
    return NumberFormat.getNumberInstance(Locale("vi", "VN")).format(number)
}
