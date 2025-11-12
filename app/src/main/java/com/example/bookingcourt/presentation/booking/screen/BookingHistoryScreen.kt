package com.example.bookingcourt.presentation.booking.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.example.bookingcourt.presentation.booking.viewmodel.BookingHistoryViewModel
import kotlinx.datetime.toJavaLocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingHistoryScreen(
    onNavigateBack: () -> Unit,
    onNavigateToBookingDetail: (String) -> Unit,
    viewModel: BookingHistoryViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Lịch sử đặt sân") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                ),
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                state.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                state.error != null -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = state.error ?: "Có lỗi xảy ra",
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { viewModel.refresh() }) {
                            Text("Thử lại")
                        }
                    }
                }
                state.bookings.isEmpty() -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Chưa có lịch sử đặt sân",
                            fontSize = 16.sp,
                            color = Color.Gray
                        )
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(state.bookings) { booking ->
                            BookingHistoryItem(
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

@Composable
fun BookingHistoryItem(
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
            // Venue Name
            Text(
                text = booking.venue.name,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Booking Items hoặc Court Info
            if (booking.bookingItems != null && booking.bookingItems.isNotEmpty()) {
                // Hiển thị danh sách sân
                booking.bookingItems.forEach { item ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "🏟️ ${item.courtName}",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                        Text(
                            text = formatPrice(item.price),
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                    }
                    // ✅ FIX: item.startTime và item.endTime là LocalDateTime, không phải String
                    Text(
                        text = "   ${formatDateTime(item.startTime)} - ${formatTime(item.endTime)}",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }
            } else {
                // Legacy: hiển thị thông tin sân đơn
                Text(
                    text = "🏟️ ${booking.court?.description ?: "Sân"}",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                Text(
                    text = "${formatDateTime(booking.startTime)} - ${formatTime(booking.endTime)}",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Total Price and Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Tổng: ${formatPrice(booking.totalPrice)}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                BookingStatusChip(status = booking.status, rejectionReason = booking.rejectionReason)
            }
        }
    }
}

@Composable
fun BookingStatusChip(
    status: BookingStatus,
    rejectionReason: String? = null
) {
    val (text, backgroundColor, textColor) = when (status) {
        BookingStatus.PENDING_PAYMENT -> Triple(
            "Chờ thanh toán",
            Color(0xFFFFF3E0),
            Color(0xFFF57C00)
        )
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
        BookingStatus.REJECTED -> Triple(
            "Từ chối",
            Color(0xFFFFEBEE),
            Color(0xFFD32F2F)
        )
        BookingStatus.CANCELLED -> Triple(
            "Đã hủy",
            Color(0xFFF5F5F5),
            Color(0xFF757575)
        )
        BookingStatus.COMPLETED -> Triple(
            "Hoàn thành",
            Color(0xFFE8F5E9),
            Color(0xFF4CAF50)
        )
        BookingStatus.NO_SHOW -> Triple(
            "Không đến",
            Color(0xFFFFEBEE),
            Color(0xFFD32F2F)
        )
        // Bỏ PENDING - không hiển thị nữa
        else -> Triple(
            "Chờ thanh toán",
            Color(0xFFFFF3E0),
            Color(0xFFF57C00)
        )
    }

    Column(horizontalAlignment = Alignment.End) {
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

        // Hiển thị lý do từ chối nếu có
        if (status == BookingStatus.REJECTED && !rejectionReason.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Lý do: $rejectionReason",
                fontSize = 10.sp,
                color = Color(0xFFD32F2F),
                modifier = Modifier.widthIn(max = 150.dp)
            )
        }
    }
}

private fun formatDateTime(dateTime: kotlinx.datetime.LocalDateTime): String {
    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
    return dateTime.toJavaLocalDateTime().format(formatter)
}

private fun formatTime(dateTime: kotlinx.datetime.LocalDateTime): String {
    val formatter = DateTimeFormatter.ofPattern("HH:mm")
    return dateTime.toJavaLocalDateTime().format(formatter)
}

private fun formatPrice(price: Long): String {
    return String.format(Locale.getDefault(), "%,d VNĐ", price)
}
