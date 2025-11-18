package com.example.bookingcourt.presentation.owner.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.bookingcourt.domain.model.BookingDetail
import com.example.bookingcourt.domain.model.BookingStatus
import com.example.bookingcourt.presentation.owner.viewmodel.OwnerBookingHistoryViewModel
import kotlinx.datetime.LocalDateTime

/**
 * Màn hình lịch sử booking cho chủ sân
 * Hiển thị 3 loại booking:
 * 1. Đã xác nhận (CONFIRMED)
 * 2. Chờ xác nhận (PAYMENT_UPLOADED)
 * 3. Đã từ chối (REJECTED)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OwnerBookingHistoryScreen(
    onNavigateBack: () -> Unit,
    onNavigateToBookingDetail: (String) -> Unit,
    viewModel: OwnerBookingHistoryViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }

    // Tabs: Đã xác nhận, Chờ xác nhận, Đã từ chối
    val tabs = listOf("Đã xác nhận", "Chờ xác nhận", "Đã từ chối")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Lịch sử đặt sân") },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Tab Row
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.White,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                text = title,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }
            }

            // Content based on selected tab
            when {
                state.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                state.error != null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Default.Error,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = Color.Red
                            )
                            Text(
                                text = state.error ?: "Có lỗi xảy ra",
                                color = MaterialTheme.colorScheme.error
                            )
                            Button(onClick = { viewModel.refresh() }) {
                                Text("Thử lại")
                            }
                        }
                    }
                }
                else -> {
                    val bookingsToShow = when (selectedTab) {
                        0 -> state.confirmedBookings
                        1 -> state.pendingBookings
                        2 -> state.rejectedBookings
                        else -> emptyList()
                    }

                    if (bookingsToShow.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    modifier = Modifier.size(64.dp),
                                    tint = Color.Gray
                                )
                                Text(
                                    text = when (selectedTab) {
                                        0 -> "Chưa có booking đã xác nhận"
                                        1 -> "Chưa có booking chờ xác nhận"
                                        2 -> "Chưa có booking đã từ chối"
                                        else -> "Không có dữ liệu"
                                    },
                                    color = Color.Gray
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(bookingsToShow) { booking ->
                                OwnerBookingHistoryItem(
                                    booking = booking,
                                    onClick = { onNavigateToBookingDetail(booking.id) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OwnerBookingHistoryItem(
    booking: BookingDetail,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header: Tên sân và Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Tên venue và court
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = booking.venue.name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = booking.getCourtsDisplayName(),
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }

                // Status badge
                StatusBadge(status = booking.status)
            }

            Spacer(modifier = Modifier.height(12.dp))
            Divider()
            Spacer(modifier = Modifier.height(12.dp))

            // Thông tin khách hàng
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = Color.Gray
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = booking.user.fullname,
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Số điện thoại
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Phone,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = Color.Gray
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = booking.user.phone ?: "N/A",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Thời gian
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Schedule,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = Color.Gray
                )
                Spacer(modifier = Modifier.width(6.dp))
                val timeDisplay = if (!booking.bookingItems.isNullOrEmpty()) {
                    val firstItem = booking.bookingItems.first()
                    formatDateTime(firstItem.startTime) + " - " + formatTime(firstItem.endTime)
                } else {
                    formatDateTime(booking.startTime) + " - " + formatTime(booking.endTime)
                }
                Text(
                    text = timeDisplay,
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }

            // Hiển thị lý do từ chối nếu status là REJECTED
            if (booking.status == BookingStatus.REJECTED && !booking.rejectionReason.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFFFEBEE)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = null,
                            tint = Color(0xFFD32F2F),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Lý do từ chối:",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFD32F2F)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = booking.rejectionReason,
                                fontSize = 12.sp,
                                color = Color(0xFFD32F2F)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Divider()
            Spacer(modifier = Modifier.height(12.dp))

            // Tổng tiền
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Tổng tiền:",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                Text(
                    text = "${booking.totalPrice.formatPrice()} đ",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun StatusBadge(status: BookingStatus) {
    val (text, backgroundColor, textColor) = when (status) {
        BookingStatus.PAYMENT_UPLOADED -> Triple(
            "Chờ xác nhận",
            Color(0xFFE3F2FD),
            Color(0xFF1976D2)
        )
        BookingStatus.CONFIRMED -> Triple(
            "Đã xác nhận",
            Color(0xFFE8F5E9),
            Color(0xFF388E3C)
        )
        BookingStatus.COMPLETED -> Triple(
            "Đã hoàn thành",
            Color(0xFFE0F2F1),
            Color(0xFF00796B)
        )
        BookingStatus.REJECTED -> Triple(
            "Đã từ chối",
            Color(0xFFFFEBEE),
            Color(0xFFD32F2F)
        )
        else -> Triple(
            status.name,
            Color(0xFFF5F5F5),
            Color(0xFF757575)
        )
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = backgroundColor
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = textColor
        )
    }
}

// Helper functions
private fun formatDateTime(dateTime: LocalDateTime): String {
    return "${dateTime.dayOfMonth}/${dateTime.monthNumber}/${dateTime.year} ${formatTime(dateTime)}"
}

private fun formatTime(dateTime: LocalDateTime): String {
    return "${dateTime.hour.toString().padStart(2, '0')}:${dateTime.minute.toString().padStart(2, '0')}"
}

private fun Long.formatPrice(): String {
    return "%,d".format(this).replace(',', '.')
}
