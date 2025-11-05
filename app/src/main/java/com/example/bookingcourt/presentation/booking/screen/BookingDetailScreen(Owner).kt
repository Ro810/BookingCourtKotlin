package com.example.bookingcourt.presentation.booking.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bookingcourt.domain.model.Booking
import com.example.bookingcourt.domain.model.BookingStatus
import com.example.bookingcourt.domain.model.PaymentMethod
import com.example.bookingcourt.domain.model.PaymentStatus
import com.example.bookingcourt.presentation.theme.BookingCourtTheme
import kotlinx.datetime.LocalDateTime
import java.text.NumberFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingDetailScreen(
    bookingId: String,
    onNavigateBack: () -> Unit = {},
) {
    // Mock data - trong thực tế sẽ lấy từ ViewModel
    val booking = remember {
        Booking(
            id = bookingId,
            courtId = "court_1",
            courtName = "Star Club Badminton - Sân 3",
            userId = "user_1",
            userName = "Nguyễn Văn A",
            userPhone = "0123456789",
            startTime = LocalDateTime(2024, 1, 15, 14, 0),
            endTime = LocalDateTime(2024, 1, 15, 16, 0),
            totalPrice = 300000,
            status = BookingStatus.CONFIRMED,
            paymentStatus = PaymentStatus.PAID,
            paymentMethod = PaymentMethod.BANK_TRANSFER,
            notes = "Đặt sân cho buổi tập nhóm",
            createdAt = LocalDateTime(2024, 1, 10, 9, 30),
            updatedAt = LocalDateTime(2024, 1, 10, 9, 30),
            cancellationReason = null,
            qrCode = "QR123456789",
        )
    }

    val currencyFormat = remember { NumberFormat.getCurrencyInstance(Locale("vi", "VN")) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chi tiết đặt sân") },
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
        },
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFF5F5F5)),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Booking Status Card
            item {
                StatusCard(
                    bookingStatus = booking.status,
                    paymentStatus = booking.paymentStatus,
                )
            }

            // Customer Information Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                    ) {
                        Text(
                            "Thông tin khách hàng",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        InfoRow(
                            icon = Icons.Default.Person,
                            label = "Tên khách hàng",
                            value = booking.userName,
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        InfoRow(
                            icon = Icons.Default.Phone,
                            label = "Số điện thoại",
                            value = booking.userPhone,
                        )
                    }
                }
            }

            // Booking Details Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                    ) {
                        Text(
                            "Thông tin đặt sân",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        InfoRow(
                            icon = Icons.Default.LocationOn,
                            label = "Sân",
                            value = booking.courtName,
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        InfoRow(
                            icon = Icons.Default.DateRange,
                            label = "Ngày đặt",
                            value = "${booking.startTime.dayOfMonth}/${booking.startTime.monthNumber}/${booking.startTime.year}",
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        InfoRow(
                            icon = Icons.Default.Info,
                            label = "Thời gian",
                            value = "${String.format("%02d:%02d", booking.startTime.hour, booking.startTime.minute)} - ${String.format("%02d:%02d", booking.endTime.hour, booking.endTime.minute)}",
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        booking.notes?.let { notes ->
                            InfoRow(
                                icon = Icons.Default.Edit,
                                label = "Ghi chú",
                                value = notes,
                            )
                        }
                    }
                }
            }

            // Payment Information Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                    ) {
                        Text(
                            "Thông tin thanh toán",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                "Tổng tiền",
                                fontSize = 16.sp,
                                color = Color.Gray,
                            )
                            Text(
                                currencyFormat.format(booking.totalPrice),
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(12.dp))

                        InfoRow(
                            icon = Icons.Default.Settings,
                            label = "Phương thức",
                            value = getPaymentMethodName(booking.paymentMethod),
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = getPaymentStatusColor(booking.paymentStatus),
                                    modifier = Modifier.size(20.dp),
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "Trạng thái thanh toán",
                                    fontSize = 14.sp,
                                    color = Color.Gray,
                                )
                            }
                            Text(
                                getPaymentStatusName(booking.paymentStatus),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = getPaymentStatusColor(booking.paymentStatus),
                            )
                        }
                    }
                }
            }

            // QR Code Card (if available)
            booking.qrCode?.let { qrCode ->
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text(
                                "Mã QR check-in",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            // Placeholder for QR Code
                            Box(
                                modifier = Modifier
                                    .size(200.dp)
                                    .background(Color.White, RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    "QR Code",
                                    fontSize = 16.sp,
                                    color = Color.Gray,
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Mã: $qrCode",
                                fontSize = 12.sp,
                                color = Color.Gray,
                            )
                        }
                    }
                }
            }

            // Action Buttons
            if (booking.status == BookingStatus.CONFIRMED) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        OutlinedButton(
                            onClick = { /* TODO: Cancel booking */ },
                            modifier = Modifier.weight(1f),
                        ) {
                            Icon(Icons.Default.Close, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Hủy đặt")
                        }

                        Button(
                            onClick = { /* TODO: Check-in */ },
                            modifier = Modifier.weight(1f),
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Check-in")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatusCard(
    bookingStatus: BookingStatus,
    paymentStatus: PaymentStatus,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = getBookingStatusColor(bookingStatus),
        ),
        shape = RoundedCornerShape(12.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(
                    "Trạng thái đặt sân",
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 14.sp,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    getBookingStatusName(bookingStatus),
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
            Icon(
                getBookingStatusIcon(bookingStatus),
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(48.dp),
            )
        }
    }
}

@Composable
fun InfoRow(
    icon: ImageVector,
    label: String,
    value: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp),
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                label,
                fontSize = 14.sp,
                color = Color.Gray,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                value,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}

fun getBookingStatusName(status: BookingStatus): String {
    return when (status) {
        BookingStatus.PENDING -> "Chờ xác nhận"
        BookingStatus.PENDING_PAYMENT -> "Chờ thanh toán"
        BookingStatus.PAYMENT_UPLOADED -> "Đã gửi chứng minh"
        BookingStatus.CONFIRMED -> "Đã xác nhận"
        BookingStatus.REJECTED -> "Bị từ chối"
        BookingStatus.CANCELLED -> "Đã hủy"
        BookingStatus.COMPLETED -> "Hoàn thành"
        BookingStatus.NO_SHOW -> "Không đến"
    }
}

fun getBookingStatusColor(status: BookingStatus): Color {
    return when (status) {
        BookingStatus.PENDING -> Color(0xFFFFA500)
        BookingStatus.PENDING_PAYMENT -> Color(0xFFFFA500)
        BookingStatus.PAYMENT_UPLOADED -> Color(0xFFFF9800)
        BookingStatus.CONFIRMED -> Color(0xFF4CAF50)
        BookingStatus.REJECTED -> Color(0xFFFF5252)
        BookingStatus.CANCELLED -> Color(0xFFFF5252)
        BookingStatus.COMPLETED -> Color(0xFF2196F3)
        BookingStatus.NO_SHOW -> Color(0xFF9E9E9E)
    }
}

fun getBookingStatusIcon(status: BookingStatus): ImageVector {
    return when (status) {
        BookingStatus.PENDING -> Icons.Default.DateRange
        BookingStatus.PENDING_PAYMENT -> Icons.Default.DateRange
        BookingStatus.PAYMENT_UPLOADED -> Icons.Default.Info
        BookingStatus.CONFIRMED -> Icons.Default.CheckCircle
        BookingStatus.REJECTED -> Icons.Default.Close
        BookingStatus.CANCELLED -> Icons.Default.Close
        BookingStatus.COMPLETED -> Icons.Default.Check
        BookingStatus.NO_SHOW -> Icons.Default.Clear
    }
}

fun getPaymentStatusName(status: PaymentStatus): String {
    return when (status) {
        PaymentStatus.PENDING -> "Chờ thanh toán"
        PaymentStatus.PAID -> "Đã thanh toán"
        PaymentStatus.FAILED -> "Thất bại"
        PaymentStatus.REFUNDED -> "Đã hoàn tiền"
    }
}

fun getPaymentStatusColor(status: PaymentStatus): Color {
    return when (status) {
        PaymentStatus.PENDING -> Color(0xFFFFA500)
        PaymentStatus.PAID -> Color(0xFF4CAF50)
        PaymentStatus.FAILED -> Color(0xFFFF5252)
        PaymentStatus.REFUNDED -> Color(0xFF2196F3)
    }
}

fun getPaymentMethodName(method: PaymentMethod?): String {
    return when (method) {
        PaymentMethod.CASH -> "Tiền mặt"
        PaymentMethod.BANK_TRANSFER -> "Chuyển khoản"
        PaymentMethod.E_WALLET -> "Ví điện tử"
        PaymentMethod.CREDIT_CARD -> "Thẻ tín dụng"
        null -> "Chưa thanh toán"
    }
}

@Preview(showBackground = true)
@Composable
fun BookingDetailScreenPreview() {
    BookingCourtTheme {
        BookingDetailScreen(bookingId = "booking_123")
    }
}
