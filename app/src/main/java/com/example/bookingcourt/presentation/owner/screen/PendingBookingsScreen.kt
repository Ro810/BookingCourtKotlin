package com.example.bookingcourt.presentation.owner.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.bookingcourt.core.common.Resource
import com.example.bookingcourt.domain.model.BookingDetail
import com.example.bookingcourt.presentation.owner.viewmodel.PendingBookingsViewModel
import com.example.bookingcourt.presentation.theme.Primary
import kotlinx.datetime.LocalDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PendingBookingsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToApproval: (String) -> Unit,
    viewModel: PendingBookingsViewModel = hiltViewModel()
) {
    val pendingBookings by viewModel.pendingBookings.collectAsState()

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text("Booking chờ xác nhận") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.refreshPendingBookings() }) {
                        Icon(Icons.Default.Refresh, "Refresh")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Primary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF8BB1F6), // Mid Blue - đồng bộ với OwnerHomeScreen
                            Color.White,
                        ),
                    ),
                ),
        ) {
            when (val state = pendingBookings) {
            is Resource.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            is Resource.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.Error,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = Color.Red
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = state.message ?: "Lỗi tải dữ liệu",
                            color = Color.Red
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.refreshPendingBookings() }) {
                            Text("Thử lại")
                        }
                    }
                }
            }
            is Resource.Success -> {
                val bookings = state.data ?: emptyList()
                if (bookings.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = Color.Gray
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Không có booking chờ xác nhận",
                                color = Color.Gray
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(bookings) { booking ->
                            PendingBookingItem(
                                booking = booking,
                                onClick = { onNavigateToApproval(booking.id) }
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
private fun PendingBookingItem(
    booking: BookingDetail,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // ✅ Sử dụng helper method để lấy tên sân (hỗ trợ nhiều sân)
                Text(
                    text = booking.getCourtsDisplayName(),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Surface(
                    color = Color(0xFFFF9800),
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = "Chờ xác nhận",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        color = Color.White,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = Color.Gray
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = booking.user.fullname,
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Phone,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = Color.Gray
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = booking.user.phone ?: "N/A",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // ✅ Hiển thị thời gian cho tất cả sân hoặc sân đầu tiên
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Schedule,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = Color.Gray
                )
                Spacer(modifier = Modifier.width(4.dp))

                val timeDisplay = if (!booking.bookingItems.isNullOrEmpty()) {
                    // Nếu có nhiều sân, hiển thị thời gian của sân đầu tiên
                    val firstItem = booking.bookingItems.first()
                    formatDateTime(firstItem.startTime) + " - " + formatTime(firstItem.endTime)
                } else {
                    // Legacy: 1 sân duy nhất
                    formatDateTime(booking.startTime) + " - " + formatTime(booking.endTime)
                }

                Text(
                    text = timeDisplay,
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }

            // ✅ Hiển thị số lượng sân nếu đặt nhiều sân
            if (booking.getCourtsCount() > 1) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.SportsSoccer,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = Color.Gray
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${booking.getCourtsCount()} sân",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Divider()

            Spacer(modifier = Modifier.height(8.dp))

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
                    color = Primary
                )
            }

            if (booking.paymentProofUploaded) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Image,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = Color(0xFF4CAF50)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Đã upload chứng minh chuyển khoản",
                        fontSize = 12.sp,
                        color = Color(0xFF4CAF50)
                    )
                }
            }
        }
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
