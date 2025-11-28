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
import com.example.bookingcourt.domain.model.MonthlyRevenue
import com.example.bookingcourt.domain.model.TimeSlotStats
import com.example.bookingcourt.domain.model.WeeklyRevenue
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.min

/**
 * Line chart hiển thị doanh thu theo ngày
 */
@Composable
fun RevenueLineChart(
    data: List<DailyRevenue>,
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

    val sortedData = data.sortedBy { it.date }
    val maxRevenue = sortedData.maxOfOrNull { it.revenue } ?: 1L

    val animatedProgress = remember { Animatable(0f) }

    LaunchedEffect(data) {
        animatedProgress.animateTo(1f, animationSpec = tween(1000))
    }

    Canvas(modifier = modifier.fillMaxWidth().height(250.dp).padding(16.dp)) {
        val chartHeight = size.height - 50.dp.toPx()
        val stepX = size.width / (sortedData.size - 1).coerceAtLeast(1)

        // Draw grid lines
        for (i in 0..4) {
            val y = i * chartHeight / 4
            drawLine(
                color = Color.LightGray.copy(alpha = 0.5f),
                start = Offset(0f, y),
                end = Offset(size.width, y),
                strokeWidth = 1.dp.toPx()
            )
        }

        // Draw line chart
        sortedData.forEachIndexed { index, dailyRevenue ->
            if (index < sortedData.size - 1) {
                val nextRevenue = sortedData[index + 1]

                val y1 = chartHeight - (dailyRevenue.revenue.toFloat() / maxRevenue * chartHeight * animatedProgress.value)
                val y2 = chartHeight - (nextRevenue.revenue.toFloat() / maxRevenue * chartHeight * animatedProgress.value)

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
            val y = chartHeight - (dailyRevenue.revenue.toFloat() / maxRevenue * chartHeight * animatedProgress.value)
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

            // Draw revenue label (màu đỏ phía trên điểm)
            if (dailyRevenue.revenue > 0) {
                val revenueText = formatShortCurrency(dailyRevenue.revenue)
                drawContext.canvas.nativeCanvas.apply {
                    drawText(
                        revenueText,
                        x,
                        y - 10.dp.toPx(),
                        android.graphics.Paint().apply {
                            color = android.graphics.Color.RED
                            textSize = 9.sp.toPx()
                            textAlign = android.graphics.Paint.Align.CENTER
                            isFakeBoldText = true
                        }
                    )
                }
            }

            // Draw date label
            val dateText = "${dailyRevenue.date.dayOfMonth}/${dailyRevenue.date.monthNumber}"
            drawContext.canvas.nativeCanvas.apply {
                drawText(
                    dateText,
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

/**
 * Line chart hiển thị doanh thu theo giờ (cho filter Hôm nay)
 */
@Composable
fun HourlyRevenueLineChart(
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
    val maxRevenue = sortedData.maxOfOrNull { it.revenue } ?: 1L

    val animatedProgress = remember { Animatable(0f) }

    LaunchedEffect(data) {
        animatedProgress.animateTo(1f, animationSpec = tween(1000))
    }

    Canvas(modifier = modifier.fillMaxWidth().height(250.dp).padding(16.dp)) {
        val chartHeight = size.height - 50.dp.toPx()
        val stepX = size.width / (sortedData.size - 1).coerceAtLeast(1)

        // Draw grid lines
        for (i in 0..4) {
            val y = i * chartHeight / 4
            drawLine(
                color = Color.LightGray.copy(alpha = 0.5f),
                start = Offset(0f, y),
                end = Offset(size.width, y),
                strokeWidth = 1.dp.toPx()
            )
        }

        // Draw line chart
        sortedData.forEachIndexed { index, hourStats ->
            if (index < sortedData.size - 1) {
                val nextStats = sortedData[index + 1]

                val y1 = chartHeight - (hourStats.revenue.toFloat() / maxRevenue * chartHeight * animatedProgress.value)
                val y2 = chartHeight - (nextStats.revenue.toFloat() / maxRevenue * chartHeight * animatedProgress.value)

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
            val y = chartHeight - (hourStats.revenue.toFloat() / maxRevenue * chartHeight * animatedProgress.value)
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

            // Draw revenue label (màu đỏ phía trên điểm)
            if (hourStats.revenue > 0) {
                val revenueText = formatShortCurrency(hourStats.revenue)
                drawContext.canvas.nativeCanvas.apply {
                    drawText(
                        revenueText,
                        x,
                        y - 10.dp.toPx(),
                        android.graphics.Paint().apply {
                            color = android.graphics.Color.RED
                            textSize = 9.sp.toPx()
                            textAlign = android.graphics.Paint.Align.CENTER
                            isFakeBoldText = true
                        }
                    )
                }
            }

            // Draw hour label
            val hourText = "${hourStats.hour}h"
            drawContext.canvas.nativeCanvas.apply {
                drawText(
                    hourText,
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

/**
 * Simple pie chart hiển thị phân bố booking theo status
 * Chỉ hiển thị 3 trạng thái: Đã duyệt, Hoàn thành, Từ chối
 */
@Composable
fun BookingStatusPieChart(
    confirmed: Int,
    completed: Int,
    rejected: Int,
    modifier: Modifier = Modifier
) {
    val total = confirmed + completed + rejected

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
    val completedColor = Color(0xFF2196F3) // Blue
    val rejectedColor = Color(0xFFF44336) // Red

    val data = listOf(
        "Đã duyệt" to Pair(confirmed, confirmedColor),
        "Hoàn thành" to Pair(completed, completedColor),
        "Từ chối" to Pair(rejected, rejectedColor)
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
 * Line chart hiển thị doanh thu theo tuần trong tháng
 */
@Composable
fun WeeklyRevenueLineChart(
    data: List<WeeklyRevenue>,
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

    val sortedData = data.sortedBy { it.weekNumber }
    val maxRevenue = sortedData.maxOfOrNull { it.revenue } ?: 1L

    val animatedProgress = remember { Animatable(0f) }

    LaunchedEffect(data) {
        animatedProgress.animateTo(1f, animationSpec = tween(1000))
    }

    Canvas(modifier = modifier.fillMaxWidth().height(250.dp).padding(16.dp)) {
        val chartHeight = size.height - 50.dp.toPx()
        val stepX = size.width / (sortedData.size - 1).coerceAtLeast(1)

        // Draw grid lines
        for (i in 0..4) {
            val y = i * chartHeight / 4
            drawLine(
                color = Color.LightGray.copy(alpha = 0.5f),
                start = Offset(0f, y),
                end = Offset(size.width, y),
                strokeWidth = 1.dp.toPx()
            )
        }

        // Draw line chart
        sortedData.forEachIndexed { index, weeklyRevenue ->
            if (index < sortedData.size - 1) {
                val nextRevenue = sortedData[index + 1]

                val y1 = chartHeight - (weeklyRevenue.revenue.toFloat() / maxRevenue * chartHeight * animatedProgress.value)
                val y2 = chartHeight - (nextRevenue.revenue.toFloat() / maxRevenue * chartHeight * animatedProgress.value)

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
            val y = chartHeight - (weeklyRevenue.revenue.toFloat() / maxRevenue * chartHeight * animatedProgress.value)
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

            // Draw revenue label (màu đỏ phía trên điểm)
            val revenueText = formatShortCurrency(weeklyRevenue.revenue)
            drawContext.canvas.nativeCanvas.apply {
                drawText(
                    revenueText,
                    x,
                    y - 10.dp.toPx(),
                    android.graphics.Paint().apply {
                        color = android.graphics.Color.RED
                        textSize = 9.sp.toPx()
                        textAlign = android.graphics.Paint.Align.CENTER
                        isFakeBoldText = true
                    }
                )
            }

            // Draw week label
            drawContext.canvas.nativeCanvas.apply {
                drawText(
                    weeklyRevenue.weekLabel,
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

/**
 * Line chart hiển thị doanh thu theo tháng trong năm
 */
@Composable
fun MonthlyRevenueLineChart(
    data: List<MonthlyRevenue>,
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

    val sortedData = data.sortedBy { it.monthNumber }
    val maxRevenue = sortedData.maxOfOrNull { it.revenue } ?: 1L

    val animatedProgress = remember { Animatable(0f) }

    LaunchedEffect(data) {
        animatedProgress.animateTo(1f, animationSpec = tween(1000))
    }

    Canvas(modifier = modifier.fillMaxWidth().height(250.dp).padding(16.dp)) {
        val chartHeight = size.height - 50.dp.toPx()
        val stepX = size.width / (sortedData.size - 1).coerceAtLeast(1)

        // Draw grid lines
        for (i in 0..4) {
            val y = i * chartHeight / 4
            drawLine(
                color = Color.LightGray.copy(alpha = 0.5f),
                start = Offset(0f, y),
                end = Offset(size.width, y),
                strokeWidth = 1.dp.toPx()
            )
        }

        // Draw line chart
        sortedData.forEachIndexed { index, monthlyRevenue ->
            if (index < sortedData.size - 1) {
                val nextRevenue = sortedData[index + 1]

                val y1 = chartHeight - (monthlyRevenue.revenue.toFloat() / maxRevenue * chartHeight * animatedProgress.value)
                val y2 = chartHeight - (nextRevenue.revenue.toFloat() / maxRevenue * chartHeight * animatedProgress.value)

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
            val y = chartHeight - (monthlyRevenue.revenue.toFloat() / maxRevenue * chartHeight * animatedProgress.value)
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

            // Draw revenue label (màu đỏ phía trên điểm)
            val revenueText = formatShortCurrency(monthlyRevenue.revenue)
            drawContext.canvas.nativeCanvas.apply {
                drawText(
                    revenueText,
                    x,
                    y - 10.dp.toPx(),
                    android.graphics.Paint().apply {
                        color = android.graphics.Color.RED
                        textSize = 9.sp.toPx()
                        textAlign = android.graphics.Paint.Align.CENTER
                        isFakeBoldText = true
                    }
                )
            }

            // Draw month label
            if (index % 2 == 0) { // Hiển thị mỗi 2 tháng để tránh chồng chéo
                drawContext.canvas.nativeCanvas.apply {
                    drawText(
                        monthlyRevenue.monthLabel,
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
 * Format number to currency (VND)
 */
fun formatCurrency(amount: Long): String {
    val formatter = NumberFormat.getNumberInstance(Locale("vi", "VN"))
    return "${formatter.format(amount)}đ"
}

/**
 * Format number with separator
 */
fun formatNumber(number: Int): String {
    return NumberFormat.getNumberInstance(Locale("vi", "VN")).format(number)
}

/**
 * Format currency ngắn gọn cho label trên chart
 * VD: 1.200.000 -> "1,2tr"
 */
fun formatShortCurrency(amount: Long): String {
    val vnLocale = Locale("vi", "VN")
    return when {
        amount >= 1_000_000_000 -> "${String.format(vnLocale, "%.1f", amount / 1_000_000_000.0)}tỷ"
        amount >= 1_000_000 -> "${String.format(vnLocale, "%.1f", amount / 1_000_000.0)}tr"
        amount >= 1_000 -> "${String.format(vnLocale, "%.0f", amount / 1_000.0)}k"
        else -> "${amount}đ"
    }
}
