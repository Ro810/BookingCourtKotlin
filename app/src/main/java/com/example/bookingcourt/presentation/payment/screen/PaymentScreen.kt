package com.example.bookingcourt.presentation.payment.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bookingcourt.domain.model.BookingData
import com.example.bookingcourt.domain.model.CourtTimeSlot
import com.example.bookingcourt.presentation.theme.BookingCourtTheme
import com.example.bookingcourt.presentation.theme.Primary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentScreen(
    bookingId: String,
    onNavigateBack: () -> Unit,
    onPaymentSuccess: () -> Unit,
) {
    // For now, using dummy data. In real implementation, fetch booking data by bookingId
    val bookingData = BookingData(
        courtId = bookingId,
        courtName = "Star Club Badminton",
        courtAddress = "Số 181 P. Cầu Cốc, Tây Mỗ, Nam Từ Liêm, Hà Nội",
        selectedDate = "19/10/2025",
        selectedSlots = setOf(
            CourtTimeSlot(1, "08:00"),
            CourtTimeSlot(1, "08:30")
        ),
        playerName = "Nguyễn Văn A",
        phoneNumber = "0123456789",
        pricePerHour = 150000,
        totalPrice = 300000
    )

    BookingConfirmationScreenContent(
        bookingData = bookingData,
        onNavigateBack = onNavigateBack,
        onConfirmPayment = onPaymentSuccess
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingConfirmationScreenContent(
    bookingData: BookingData,
    onNavigateBack: () -> Unit,
    onConfirmPayment: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Xác nhận đặt sân", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Primary,
                    titleContentColor = Color.Black,
                    navigationIconContentColor = Color.Black
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Header Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Primary.copy(alpha = 0.1f)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color(0xFF4CAF50),
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Xác nhận thông tin đặt sân",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Vui lòng kiểm tra kỹ thông tin trước khi thanh toán",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Thông tin sân
            InfoSection(
                title = "Thông tin sân",
                icon = Icons.Default.Place
            ) {
                InfoRow(label = "Tên sân:", value = bookingData.courtName)
                InfoRow(label = "Địa chỉ:", value = bookingData.courtAddress)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Thông tin đặt sân
            InfoSection(
                title = "Thông tin đặt sân",
                icon = Icons.Default.DateRange
            ) {
                InfoRow(label = "Ngày đặt:", value = bookingData.selectedDate)

                Spacer(modifier = Modifier.height(12.dp))

                // Hiển thị chi tiết các sân và giờ đã chọn
                Text(
                    text = "Sân và giờ đã chọn:",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Nhóm theo sân
                bookingData.selectedSlots.groupBy { it.courtNumber }.forEach { (courtNum, slots) ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFF5F5F5)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "Sân $courtNum",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Primary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = slots.map { it.timeSlot }.sorted().joinToString(" • "),
                                fontSize = 13.sp,
                                color = Color.DarkGray
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                InfoRow(
                    label = "Tổng số giờ:",
                    value = "${bookingData.selectedSlots.size} giờ",
                    valueColor = Primary
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Thông tin người đặt
            InfoSection(
                title = "Thông tin người đặt",
                icon = Icons.Default.Person
            ) {
                InfoRow(label = "Họ và tên:", value = bookingData.playerName)
                InfoRow(label = "Số điện thoại:", value = bookingData.phoneNumber)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Thông tin thanh toán
            InfoSection(
                title = "Thông tin thanh toán",
                icon = Icons.Default.Payment
            ) {
                InfoRow(
                    label = "Giá/giờ:",
                    value = "${bookingData.pricePerHour / 1000}.000 VNĐ"
                )
                InfoRow(
                    label = "Số giờ:",
                    value = "${bookingData.selectedSlots.size} giờ"
                )

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(thickness = 1.dp, color = Color.LightGray)
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Tổng thanh toán:",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Text(
                        text = "${bookingData.totalPrice / 1000}.000 VNĐ",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Ghi chú
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFFFF3E0)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = Color(0xFFFF9800),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Sau khi thanh toán, bạn sẽ nhận được mã đặt sân qua SMS. Vui lòng mang theo mã này khi đến sân.",
                        fontSize = 13.sp,
                        color = Color(0xFF6D4C41),
                        lineHeight = 18.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onNavigateBack,
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Primary
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Quay lại", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                }

                Button(
                    onClick = onConfirmPayment,
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Primary
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Payment,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Thanh toán",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun InfoSection(
    title: String,
    icon: ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }
            content()
        }
    }
}

@Composable
fun InfoRow(
    label: String,
    value: String,
    valueColor: Color = Color.Black
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = Color.Gray,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = valueColor,
            modifier = Modifier.weight(1.5f),
            textAlign = androidx.compose.ui.text.style.TextAlign.End
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PaymentScreenPreview() {
    BookingCourtTheme {
        PaymentScreen(
            bookingId = "VN001",
            onNavigateBack = {},
            onPaymentSuccess = {}
        )
    }
}

