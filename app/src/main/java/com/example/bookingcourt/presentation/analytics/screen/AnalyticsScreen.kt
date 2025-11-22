package com.example.bookingcourt.presentation.analytics.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.bookingcourt.domain.model.AnalyticsPeriod
import com.example.bookingcourt.presentation.analytics.components.*
import com.example.bookingcourt.presentation.analytics.viewmodel.AnalyticsViewModel

/**
 * Màn hình Analytics/Thống kê cho chủ sân
 * Hiển thị:
 * - Tổng quan metrics (doanh thu, booking, conversion rate)
 * - Doanh thu theo thời gian (bar chart)
 * - Phân bố booking theo status (pie chart)
 * - Hiệu suất venue
 * - Giờ đặt nhiều nhất (line chart)
 * - Top khách hàng
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    onNavigateBack: () -> Unit,
    viewModel: AnalyticsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text("Báo cáo & Thống kê") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.refresh() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Làm mới")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            MaterialTheme.colorScheme.background
                        )
                    )
                )
                .padding(paddingValues)
        ) {
            when {
                state.isLoading -> LoadingContent()
                state.error != null -> ErrorContent(
                    message = state.error ?: "Có lỗi xảy ra",
                    onRetry = { viewModel.refresh() }
                )
                state.analyticsData != null -> AnalyticsContent(
                    state = state,
                    onPeriodChange = { period -> viewModel.changePeriod(period) }
                )
                else -> EmptyContent()
            }
        }
    }
}

@Composable
private fun LoadingContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(16.dp))
            Text("Đang tải dữ liệu thống kê...")
        }
    }
}

@Composable
private fun ErrorContent(
    message: String,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Error,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onRetry) {
                Icon(Icons.Default.Refresh, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Thử lại")
            }
        }
    }
}

@Composable
private fun EmptyContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp)
        ) {
            Icon(
                imageVector = Icons.Default.BarChart,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Chưa có dữ liệu thống kê",
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@Composable
private fun AnalyticsContent(
    state: com.example.bookingcourt.presentation.analytics.viewmodel.AnalyticsState,
    onPeriodChange: (AnalyticsPeriod) -> Unit
) {
    val data = state.analyticsData ?: return

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Period selector
        item {
            PeriodSelector(
                selectedPeriod = state.selectedPeriod,
                onPeriodSelected = onPeriodChange
            )
        }

        // Metrics overview
        item {
            SectionHeader(
                title = "Tổng quan",
                icon = Icons.Default.Dashboard
            )
        }

        item {
            val conversionRateText = if (data.bookingStats.conversionRate.isNaN()) {
                "0%"
            } else {
                "${(data.bookingStats.conversionRate * 100).toInt()}%"
            }

            MetricsGrid(
                totalRevenue = formatCurrency(data.totalRevenue),
                totalBookings = formatNumber(data.totalBookings),
                averageValue = formatCurrency(data.averageBookingValue),
                conversionRate = conversionRateText
            )
        }

        // Revenue chart - Theo giờ nếu chọn "Hôm nay", theo ngày nếu chọn filter khác
        if (state.selectedPeriod == AnalyticsPeriod.DAY) {
            // Hôm nay: Hiển thị doanh thu theo giờ
            if (data.timeSlotStats.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    SectionHeader(
                        title = "Doanh thu theo giờ",
                        icon = Icons.Default.TrendingUp
                    )
                }

                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            HourlyRevenueLineChart(
                                data = data.timeSlotStats,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        } else {
            // Tuần/Tháng/Năm: Hiển thị doanh thu theo ngày
            if (data.revenueByDate.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    SectionHeader(
                        title = "Doanh thu theo ngày",
                        icon = Icons.Default.TrendingUp
                    )
                }

                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            RevenueLineChart(
                                data = data.revenueByDate,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }

        // Booking status distribution
        item {
            Spacer(modifier = Modifier.height(8.dp))
            SectionHeader(
                title = "Phân bố trạng thái",
                icon = Icons.Default.PieChart
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    BookingStatusPieChart(
                        confirmed = data.bookingStats.confirmedCount,
                        pending = data.bookingStats.pendingCount,
                        rejected = data.bookingStats.rejectedCount,
                        cancelled = data.bookingStats.cancelledCount,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // Venue performance
        if (data.venuePerformance.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                SectionHeader(
                    title = "Hiệu suất sân",
                    icon = Icons.Default.Stadium
                )
            }

            itemsIndexed(data.venuePerformance.take(5)) { index, venue ->
                VenuePerformanceCard(
                    venueName = venue.venueName,
                    bookingCount = venue.bookingCount,
                    revenue = formatCurrency(venue.revenue),
                    rank = index + 1,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // Top customers
        if (data.topCustomers.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                SectionHeader(
                    title = "Khách hàng thân thiết",
                    icon = Icons.Default.Star
                )
            }

            itemsIndexed(data.topCustomers.take(5)) { index, customer ->
                TopCustomerCard(
                    customerName = customer.userName,
                    bookingCount = customer.bookingCount,
                    totalSpent = formatCurrency(customer.totalSpent),
                    rank = index + 1,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // Bottom spacing
        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun PeriodSelector(
    selectedPeriod: AnalyticsPeriod,
    onPeriodSelected: (AnalyticsPeriod) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            PeriodButton(
                text = "Hôm nay",
                selected = selectedPeriod == AnalyticsPeriod.DAY,
                onClick = { onPeriodSelected(AnalyticsPeriod.DAY) }
            )
            PeriodButton(
                text = "Tuần",
                selected = selectedPeriod == AnalyticsPeriod.WEEK,
                onClick = { onPeriodSelected(AnalyticsPeriod.WEEK) }
            )
            PeriodButton(
                text = "Tháng",
                selected = selectedPeriod == AnalyticsPeriod.MONTH,
                onClick = { onPeriodSelected(AnalyticsPeriod.MONTH) }
            )
            PeriodButton(
                text = "Năm",
                selected = selectedPeriod == AnalyticsPeriod.YEAR,
                onClick = { onPeriodSelected(AnalyticsPeriod.YEAR) }
            )
        }
    }
}

@Composable
private fun PeriodButton(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (selected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            },
            contentColor = if (selected) {
                Color.White
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            }
        ),
        shape = RoundedCornerShape(12.dp),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
        modifier = Modifier.height(40.dp)
    ) {
        Text(
            text = text,
            maxLines = 1
        )
    }
}
