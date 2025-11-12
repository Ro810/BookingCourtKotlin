package com.example.bookingcourt.presentation.payment.screen

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.background
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
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.bookingcourt.core.common.Resource
import com.example.bookingcourt.domain.model.BookingData
import com.example.bookingcourt.domain.model.BookingItemData
import com.example.bookingcourt.domain.model.CourtTimeSlot
import com.example.bookingcourt.presentation.payment.viewmodel.PaymentViewModel
import com.example.bookingcourt.presentation.theme.BookingCourtTheme
import com.example.bookingcourt.presentation.theme.Primary
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import com.google.gson.Gson

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentScreen(
    bookingId: String,
    onNavigateBack: () -> Unit,
    onPaymentSuccess: (String) -> Unit,
    paymentViewModel: PaymentViewModel = hiltViewModel()
) {
    // Decode URL-encoded JSON string and deserialize BookingData
    val gson = Gson()
    val initialBookingData = try {
        // Decode URL-encoded string first
        val decodedJson = URLDecoder.decode(bookingId, StandardCharsets.UTF_8.toString())
        gson.fromJson(decodedJson, BookingData::class.java)
    } catch (e: Exception) {
        // Fallback to dummy data if deserialization fails
        BookingData(
            courtId = "error",
            courtName = "Lỗi tải dữ liệu",
            courtAddress = "Vui lòng thử lại",
            selectedDate = "",
            selectedSlots = emptySet(),
            playerName = "",
            phoneNumber = "",
            pricePerHour = 0,
            totalPrice = 0
        )
    }

    // State để lưu bookingData có thể cập nhật từ API
    var bookingData by remember { mutableStateOf(initialBookingData) }

    // Observe create booking state
    val createBookingState by paymentViewModel.createBookingState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Handle create booking result
    LaunchedEffect(createBookingState) {
        when (val state = createBookingState) {
            is Resource.Success -> {
                val bookingWithBankInfo = state.data
                if (bookingWithBankInfo != null) {
                    // ✅ LOG CHI TIẾT để debug vấn đề thông tin sân bị nhầm
                    Log.d("PaymentScreen", "========== BOOKING CREATED SUCCESSFULLY ==========")
                    Log.d("PaymentScreen", "  📋 Booking ID: ${bookingWithBankInfo.id}")
                    // ✅ Hỗ trợ cả bookingItems và court legacy
                    if (!bookingWithBankInfo.bookingItems.isNullOrEmpty()) {
                        Log.d("PaymentScreen", "  🏟️ Booking Items (${bookingWithBankInfo.bookingItems.size} courts):")
                        bookingWithBankInfo.bookingItems.forEach { item ->
                            Log.d("PaymentScreen", "     - Court ID: ${item.courtId}, Name: ${item.courtName}")
                        }
                    } else {
                        Log.d("PaymentScreen", "  🏟️ Court ID: ${bookingWithBankInfo.court?.id}")
                        Log.d("PaymentScreen", "  🏟️ Court Name: ${bookingWithBankInfo.court?.description}")
                    }
                    Log.d("PaymentScreen", "  🏢 Venue ID: ${bookingWithBankInfo.venue.id}")
                    Log.d("PaymentScreen", "  🏢 Venue Name: ${bookingWithBankInfo.venue.name}")
                    Log.d("PaymentScreen", "  💰 Total Price (from API): ${bookingWithBankInfo.totalPrice}")
                    Log.d("PaymentScreen", "  💰 Total Price (client calculated): ${bookingData.totalPrice}")
                    Log.d("PaymentScreen", "  🏦 Bank Name: ${bookingWithBankInfo.ownerBankInfo.bankName}")
                    Log.d("PaymentScreen", "  🏦 Account Number: ${bookingWithBankInfo.ownerBankInfo.bankAccountNumber}")
                    Log.d("PaymentScreen", "  🏦 Account Name: ${bookingWithBankInfo.ownerBankInfo.bankAccountName}")
                    Log.d("PaymentScreen", "  ⏰ Start Time: ${bookingWithBankInfo.startTime}")
                    Log.d("PaymentScreen", "  ⏰ End Time: ${bookingWithBankInfo.endTime}")
                    Log.d("PaymentScreen", "====================================================")

                    // ✅ CẬP NHẬT bookingData với GIÁ CHÍNH XÁC từ API (không dùng giá tính ở client)
                    // ✅ Sử dụng helper method để lấy tên sân (hỗ trợ cả bookingItems và court legacy)
                    val courtsDisplayName = bookingWithBankInfo.getCourtsDisplayName()
                    bookingData = bookingData.copy(
                        courtName = "${bookingWithBankInfo.venue.name} - $courtsDisplayName",
                        totalPrice = bookingWithBankInfo.totalPrice, // ✅ SỬ DỤNG GIÁ TỪ API
                        ownerBankInfo = bookingWithBankInfo.ownerBankInfo,
                        expireTime = bookingWithBankInfo.expireTime.toString()
                    )

                    Log.d("PaymentScreen", "✅ Updated totalPrice: ${bookingData.totalPrice} VNĐ")

                    snackbarHostState.showSnackbar(
                        message = "Đặt sân thành công!",
                        duration = SnackbarDuration.Short
                    )
                    // Reset state và navigate with bookingId
                    paymentViewModel.resetCreateBookingState()
                    onPaymentSuccess(bookingWithBankInfo.id)
                }
            }
            is Resource.Error -> {
                Log.e("PaymentScreen", "❌ Error creating booking: ${state.message}")
                snackbarHostState.showSnackbar(
                    message = state.message ?: "Đã xảy ra lỗi khi đặt sân",
                    duration = SnackbarDuration.Long
                )
                paymentViewModel.resetCreateBookingState()
            }
            else -> { /* Loading or null */ }
        }
    }

    BookingConfirmationScreenContent(
        bookingData = bookingData,
        onNavigateBack = onNavigateBack,
        onConfirmPayment = {
            // ✅ Gọi API tạo booking với tất cả các sân đã chọn
            Log.d("PaymentScreen", "📝 Calling API to create booking:")

            // Sử dụng bookingItems nếu có, nếu không fallback về legacy mode
            val items = bookingData.bookingItems ?: listOf(
                BookingItemData(
                    courtId = bookingData.courtId,
                    courtName = bookingData.courtName,
                    startTime = bookingData.startTime,
                    endTime = bookingData.endTime,
                    price = bookingData.totalPrice
                )
            )

            Log.d("PaymentScreen", "  Total booking items: ${items.size}")
            items.forEachIndexed { index, item ->
                Log.d("PaymentScreen", "  [$index] ${item.courtName} (${item.courtId})")
                Log.d("PaymentScreen", "       Time: ${item.startTime} - ${item.endTime}")
                Log.d("PaymentScreen", "       Price: ${item.price} VNĐ")
            }
            Log.d("PaymentScreen", "  Total Price: ${bookingData.totalPrice} VNĐ")

            paymentViewModel.createBookingWithItems(bookingItems = items)
        },
        isLoading = createBookingState is Resource.Loading,
        snackbarHostState = snackbarHostState
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingConfirmationScreenContent(
    bookingData: BookingData,
    onNavigateBack: () -> Unit,
    onConfirmPayment: () -> Unit,
    isLoading: Boolean = false,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Xác nhận đặt sân", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack, enabled = !isLoading) {
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
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
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
                // Loại bỏ phần "- Sân X" khỏi tên sân vì đã có chi tiết ở dưới
                val venueName = bookingData.courtName.substringBefore(" - Sân").trim()
                InfoRow(label = "Tên sân:", value = venueName)
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

                // ✅ FIX: Hiển thị thời gian từ bookingItems nếu có, nếu không fallback về selectedSlots
                if (!bookingData.bookingItems.isNullOrEmpty()) {
                    // ✅ Hiển thị từ bookingItems - có thời gian chính xác cho từng sân
                    bookingData.bookingItems.forEach { item ->
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
                                    text = item.courtName,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Primary
                                )
                                Spacer(modifier = Modifier.height(4.dp))

                                // Hiển thị thời gian từ startTime và endTime
                                val startTime = formatDateTime(item.startTime, "HH:mm")
                                val endTime = formatDateTime(item.endTime, "HH:mm")
                                Text(
                                    text = "• $startTime - $endTime",
                                    fontSize = 13.sp,
                                    color = Color.DarkGray,
                                    modifier = Modifier.padding(vertical = 2.dp)
                                )
                            }
                        }
                    }
                } else {
                    // ✅ Fallback: Hiển thị từ selectedSlots (legacy mode)
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

                                // Gộp các slot liên tiếp thành khung giờ tổng hợp
                                val sortedSlots = slots.map { it.timeSlot }.sorted()
                                if (sortedSlots.isNotEmpty()) {
                                    val startTime = sortedSlots.first()
                                    val endTime = calculateEndTimeFromSlots(sortedSlots.last())
                                    Text(
                                        text = "• $startTime - $endTime",
                                        fontSize = 13.sp,
                                        color = Color.DarkGray,
                                        modifier = Modifier.padding(vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                InfoRow(
                    label = "Tổng số giờ:",
                    value = "${bookingData.selectedSlots.size * 0.5} giờ",
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
                    value = "${bookingData.pricePerHour.formatPrice()} VNĐ"
                )
                InfoRow(
                    label = "Số giờ:",
                    value = "${bookingData.selectedSlots.size * 0.5} giờ"
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
                        text = "${bookingData.totalPrice.formatPrice()} VNĐ",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Thông tin ngân hàng của chủ sân (nếu có)
            bookingData.ownerBankInfo?.let { bankInfo ->
                InfoSection(
                    title = "Thông tin chuyển khoản",
                    icon = Icons.Default.AccountBalance
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFF5F5F5)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            InfoRow(
                                label = "Ngân hàng:",
                                value = bankInfo.bankName
                            )
                            InfoRow(
                                label = "Số tài khoản:",
                                value = bankInfo.bankAccountNumber
                            )
                            InfoRow(
                                label = "Chủ tài khoản:",
                                value = bankInfo.bankAccountName
                            )
                            InfoRow(
                                label = "Số tiền:",
                                value = "${bookingData.totalPrice.formatPrice()} VNĐ",
                                valueColor = Primary
                            )
                        }
                    }

                    bookingData.expireTime?.let { expireTime ->
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccessTime,
                                contentDescription = null,
                                tint = Color(0xFFFF9800),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Vui lòng thanh toán trước: $expireTime",
                                fontSize = 12.sp,
                                color = Color(0xFFFF9800),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

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
                    shape = RoundedCornerShape(12.dp),
                    enabled = !isLoading
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
                    shape = RoundedCornerShape(12.dp),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Payment,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Xác nhận",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Show loading overlay when creating booking
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(Color.Black.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CircularProgressIndicator(color = Color.White)
                    Text(
                        text = "Đang tạo booking...",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
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
            color = Color.Gray
        )
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = valueColor
        )
    }
}

// ✅ Helper function để format giá tiền đúng
private fun Long.formatPrice(): String {
    return "%,d".format(this).replace(',', '.')
}

// ✅ Helper function để format datetime từ ISO format sang định dạng mong muốn
private fun formatDateTime(dateTimeString: String, pattern: String = "HH:mm"): String {
    return try {
        // Input format: "2025-11-11T04:00:00" hoặc "04:00:00"
        if (dateTimeString.contains("T")) {
            // ISO format: "2025-11-11T04:00:00"
            dateTimeString.substring(11, 16) // Lấy "04:00"
        } else if (dateTimeString.length >= 5) {
            // Time only: "04:00:00"
            dateTimeString.substring(0, 5) // Lấy "04:00"
        } else {
            dateTimeString
        }
    } catch (e: Exception) {
        dateTimeString
    }
}

// ✅ Helper function để tính thời gian kết thúc từ slot cuối cùng
private fun calculateEndTimeFromSlots(lastSlot: String): String {
    val parts = lastSlot.split(":")
    if (parts.size < 2) return lastSlot

    val hour = parts[0].toIntOrNull() ?: return lastSlot
    val minute = parts[1].toIntOrNull() ?: return lastSlot

    // Thêm 30 phút vào slot cuối cùng
    val totalMinutes = hour * 60 + minute + 30
    val endHour = (totalMinutes / 60) % 24
    val endMinute = totalMinutes % 60

    return String.format("%02d:%02d", endHour, endMinute)
}

private fun formatEndTime(timeSlot: String): String {
    val parts = timeSlot.split(":")
    if (parts.size < 2) return timeSlot

    val hour = parts[0].toIntOrNull() ?: return timeSlot
    val minute = parts[1].toIntOrNull() ?: return timeSlot

    val totalMinutes = hour * 60 + minute + 30
    val endHour = (totalMinutes / 60) % 24
    val endMinute = totalMinutes % 60

    return String.format("%02d:%02d", endHour, endMinute)
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
