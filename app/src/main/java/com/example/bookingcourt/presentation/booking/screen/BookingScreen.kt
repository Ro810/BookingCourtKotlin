package com.example.bookingcourt.presentation.booking.screen

import android.util.Log
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.border
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.bookingcourt.core.common.Resource
import com.example.bookingcourt.domain.model.Venue
import com.example.bookingcourt.domain.model.Address
import com.example.bookingcourt.domain.model.User
import com.example.bookingcourt.domain.model.BookingData
import com.example.bookingcourt.domain.model.CourtTimeSlot
import com.example.bookingcourt.presentation.booking.viewmodel.BookingViewModel
import com.example.bookingcourt.presentation.theme.BookingCourtTheme
import com.example.bookingcourt.presentation.theme.Primary
import java.text.SimpleDateFormat
import java.util.*
import com.google.gson.Gson
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingScreen(
    courtId: String, // Thực ra là venueId
    numberOfCourts: Int = 1, // Deprecated parameter
    court: Venue? = null, // Parameter giữ tên "court" để backward compatible, nhưng thực chất là Venue
    currentUser: User? = null,
    onNavigateBack: () -> Unit,
    onNavigateToPayment: (String) -> Unit,
    bookingViewModel: BookingViewModel = hiltViewModel()
) {
    // Venue object - có thể null nếu không được truyền vào
    val venue = court ?: Venue(
        id = courtId.toLongOrNull() ?: 0L,
        name = "Sân Cầu Lông ABC",
        description = "Sân cầu lông chất lượng cao",
        numberOfCourt = 3,
        address = Address(
            id = 1L,
            provinceOrCity = "TP.HCM",
            district = "Quận 1",
            detailAddress = "123 Đường Lê Lợi"
        ),
        courtsCount = 3,
        pricePerHour = 150000,
        averageRating = 4.5f,
        totalReviews = 120,
        openingTime = "06:00:00",
        closingTime = "22:00:00",
        phoneNumber = "0123456789",
        email = "contact@abc.com"
    )

    // Số lượng sân con trong venue này
    val actualNumberOfCourts = venue.courtsCount

    var selectedDate by remember { mutableStateOf("") }
    var selectedSlots by remember { mutableStateOf(setOf<CourtTimeSlot>()) }
    var playerName by remember(currentUser) { mutableStateOf(currentUser?.fullName ?: "") }
    var phoneNumber by remember(currentUser) { mutableStateOf(currentUser?.phoneNumber ?: "") }
    var showDatePicker by remember { mutableStateOf(false) }

    // State để hiển thị error message
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    // ⚠️ QUAN TRỌNG: State để observe kết quả tạo booking với thông tin ngân hàng
    val createBookingState by bookingViewModel.createBookingState.collectAsState()

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = System.currentTimeMillis()
    )

    // Parse opening and closing time from venue
    val openingTime = venue.openingTime?.split(":")?.let { parts ->
        if (parts.size >= 2) Pair(parts[0].toIntOrNull() ?: 6, parts[1].toIntOrNull() ?: 0)
        else Pair(6, 0)
    } ?: Pair(6, 0)

    val closingTime = venue.closingTime?.split(":")?.let { parts ->
        if (parts.size >= 2) Pair(parts[0].toIntOrNull() ?: 22, parts[1].toIntOrNull() ?: 0)
        else Pair(22, 0)
    } ?: Pair(22, 0)

    // Tạo danh sách khung giờ - mỗi 30 phút
    val timeSlots = remember(openingTime, closingTime) {
        val slots = mutableListOf<String>()
        var currentHour = openingTime.first
        var currentMinute = openingTime.second

        val closeHour = closingTime.first
        val closeMinute = closingTime.second

        while (currentHour < closeHour || (currentHour == closeHour && currentMinute < closeMinute)) {
            slots.add(String.format("%02d:%02d", currentHour, currentMinute))

            currentMinute += 30
            if (currentMinute >= 60) {
                currentMinute = 0
                currentHour++
            }
        }

        slots
    }

    // Handle create booking state
    LaunchedEffect(createBookingState) {
        when (val state = createBookingState) {
            is Resource.Success -> {
                val bookingWithBankInfo = state.data
                if (bookingWithBankInfo != null) {
                    // Truyền thông tin booking + bank info sang PaymentScreen
                    val bookingData = BookingData(
                        courtId = bookingWithBankInfo.court.id,
                        courtName = "${bookingWithBankInfo.venue.name} - ${bookingWithBankInfo.court.description}",
                        courtAddress = venue.address.getFullAddress(),
                        selectedDate = selectedDate.ifEmpty {
                            SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
                        },
                        selectedSlots = selectedSlots,
                        playerName = playerName,
                        phoneNumber = phoneNumber,
                        pricePerHour = venue.pricePerHour,
                        totalPrice = bookingWithBankInfo.totalPrice,
                        ownerBankInfo = bookingWithBankInfo.ownerBankInfo,
                        expireTime = bookingWithBankInfo.expireTime.toString()
                    )

                    // Serialize to JSON for navigation
                    val gson = Gson()
                    val bookingDataJson = gson.toJson(bookingData)
                    val encodedJson = URLEncoder.encode(bookingDataJson, StandardCharsets.UTF_8.toString())

                    // Reset state trước khi navigate
                    bookingViewModel.resetCreateBookingState()

                    onNavigateToPayment(encodedJson)
                }
            }
            is Resource.Error -> {
                // Hiển thị error message
                errorMessage = state.message ?: "Đã xảy ra lỗi khi tạo booking"
                snackbarHostState.showSnackbar(
                    message = errorMessage ?: "Đã xảy ra lỗi",
                    duration = SnackbarDuration.Long
                )
                bookingViewModel.resetCreateBookingState()
            }
            else -> { /* Loading or null */ }
        }
    }

    // DatePickerDialog
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                        selectedDate = dateFormat.format(Date(millis))
                    }
                    showDatePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Hủy")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Đặt sân", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
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
            // Venue Info Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Primary.copy(alpha = 0.1f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = venue.name,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = venue.address.getFullAddress(),
                        fontSize = 14.sp,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Có ${venue.courtsCount} sân",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Date Selection
            Text(
                text = "Chọn ngày",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = selectedDate.ifEmpty { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date()) },
                onValueChange = {},
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = {
                    Icon(Icons.Default.CalendarToday, contentDescription = "Date")
                },
                placeholder = { Text("Chọn ngày") },
                readOnly = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent
                ),
                interactionSource = remember { MutableInteractionSource() }
                    .also { interactionSource ->
                        LaunchedEffect(interactionSource) {
                            interactionSource.interactions.collect {
                                if (it is PressInteraction.Release) {
                                    showDatePicker = true
                                }
                            }
                        }
                    }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Booking Grid Table
            Text(
                text = "Chọn sân và giờ (nhấn vào ô trong bảng)",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Grid Table with fixed first column
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier.padding(8.dp)
                ) {
                    // Fixed Column - Tên sân
                    Column {
                        // Header cell
                        Box(
                            modifier = Modifier
                                .width(70.dp)
                                .height(50.dp)
                                .border(1.dp, Color.Gray)
                                .background(Primary.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Sân",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                textAlign = TextAlign.Center,
                                color = Color.Black
                            )
                        }

                        // Court rows
                        for (courtNum in 1..actualNumberOfCourts) {
                            Box(
                                modifier = Modifier
                                    .width(70.dp)
                                    .height(45.dp)
                                    .border(1.dp, Color.Gray)
                                    .background(Color(0xFFF5F5F5)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Sân $courtNum",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    textAlign = TextAlign.Center,
                                    color = Color.Black
                                )
                            }
                        }
                    }

                    // Scrollable Column - Time slots
                    Column(
                        modifier = Modifier.horizontalScroll(rememberScrollState())
                    ) {
                        // Header Row
                        Row {
                            timeSlots.forEach { time ->
                                Box(
                                    modifier = Modifier
                                        .width(80.dp)
                                        .height(50.dp)
                                        .border(1.dp, Color.Gray)
                                        .background(Primary.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = time,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        textAlign = TextAlign.Center,
                                        color = Color.Black
                                    )
                                }
                            }
                        }

                        // Data Rows
                        for (courtNum in 1..actualNumberOfCourts) {
                            Row {
                                timeSlots.forEach { time ->
                                    val slot = CourtTimeSlot(courtNum, time)
                                    val isSelected = selectedSlots.contains(slot)

                                    Box(
                                        modifier = Modifier
                                            .width(80.dp)
                                            .height(45.dp)
                                            .border(1.dp, Color.Gray)
                                            .background(
                                                if (isSelected) Primary
                                                else Color.White
                                            )
                                            .clickable {
                                                selectedSlots = if (isSelected) {
                                                    selectedSlots - slot
                                                } else {
                                                    selectedSlots + slot
                                                }
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (isSelected) {
                                            Text(
                                                text = "✓",
                                                color = Color.White,
                                                fontSize = 18.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Selected slots info
            if (selectedSlots.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Primary.copy(alpha = 0.1f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Đã chọn ${selectedSlots.size} ô:",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color.Black
                        )
                        Spacer(modifier = Modifier.height(4.dp))

                        // Nhóm theo sân
                        selectedSlots.groupBy { it.courtNumber }.forEach { (courtNum, slots) ->
                            Text(
                                text = "• Sân $courtNum: ${slots.map { it.timeSlot }.sorted().joinToString(", ")}",
                                fontSize = 12.sp,
                                color = Color.Black
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Player Info
            Text(
                text = "Thông tin người đặt",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = playerName,
                onValueChange = { playerName = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Họ và tên") },
                placeholder = { Text("Nhập họ và tên") }
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = phoneNumber,
                onValueChange = { phoneNumber = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Số điện thoại") },
                placeholder = { Text("Nhập số điện thoại") }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Price Summary
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Giá sân/giờ:", color = Color.Black)
                        Text(
                            text = "${venue.pricePerHour / 1000}.000 VNĐ",
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    }

                    if (selectedSlots.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Tổng số giờ đã chọn:", color = Color.Black)
                            Text(
                                text = "${selectedSlots.size * 0.5} giờ",
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Tổng tiền:",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                            Text(
                                text = "${(venue.pricePerHour * selectedSlots.size * 0.5).toLong() / 1000}.000 VNĐ",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Confirm Button
            Button(
                onClick = {
                    if (selectedSlots.isNotEmpty()) {
                        val firstSlot = selectedSlots.first()
                        val courtNumber = firstSlot.courtNumber

                        // ⚠️ TẠM THỜI: Giả định courtId = "venueId_courtNumber"
                        val realCourtId = "${venue.id}_$courtNumber"

                        val selectedDateFormatted = selectedDate.ifEmpty {
                            SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
                        }

                        val dateForApi = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                            .parse(selectedDateFormatted)
                        val calendar = Calendar.getInstance()
                        calendar.time = dateForApi ?: Date()

                        val sortedSlots = selectedSlots.sortedBy { it.timeSlot }
                        val firstTimeSlot = sortedSlots.first().timeSlot
                        val lastTimeSlot = sortedSlots.last().timeSlot

                        val firstTimeParts = firstTimeSlot.split(":")
                        val firstHour = firstTimeParts[0].toInt()
                        val firstMinute = firstTimeParts[1].toInt()

                        calendar.set(Calendar.HOUR_OF_DAY, firstHour)
                        calendar.set(Calendar.MINUTE, firstMinute)
                        calendar.set(Calendar.SECOND, 0)

                        val now = Calendar.getInstance()
                        if (calendar.before(now)) {
                            calendar.add(Calendar.DAY_OF_MONTH, 1)
                            Log.w("BookingScreen", "Thời gian đã qua, tự động chuyển sang ngày mai")
                        }

                        val apiDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                        val startTime = apiDateFormat.format(calendar.time)

                        val endTimeSlot = calculateEndTime(lastTimeSlot)
                        val endTimeParts = endTimeSlot.split(":")
                        val endHour = endTimeParts[0].toInt()
                        val endMinute = endTimeParts[1].toInt()

                        calendar.set(Calendar.HOUR_OF_DAY, endHour)
                        calendar.set(Calendar.MINUTE, endMinute)
                        val endTime = apiDateFormat.format(calendar.time)

                        bookingViewModel.createBooking(
                            courtId = realCourtId,
                            startTime = startTime,
                            endTime = endTime,
                            notes = "Đặt sân cho ${selectedSlots.size} khung giờ",
                            paymentMethod = "BANK_TRANSFER"
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Primary),
                enabled = selectedSlots.isNotEmpty() && playerName.isNotEmpty() && phoneNumber.isNotEmpty() && createBookingState !is Resource.Loading
            ) {
                if (createBookingState is Resource.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Xác nhận đặt sân", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }

        // Show loading overlay when creating booking - ĐẶT SAU Column để che đúng
        if (createBookingState is Resource.Loading) {
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

// Helper function để tính end time
private fun calculateEndTime(timeSlot: String): String {
    val parts = timeSlot.split(":")
    if (parts.size != 2) return timeSlot

    val hour = parts[0].toIntOrNull() ?: return timeSlot
    val minute = parts[1].toIntOrNull() ?: return timeSlot

    val totalMinutes = hour * 60 + minute + 30
    val endHour = (totalMinutes / 60) % 24
    val endMinute = totalMinutes % 60

    return String.format("%02d:%02d", endHour, endMinute)
}

@Preview(showBackground = true)
@Composable
fun BookingScreenPreview() {
    BookingCourtTheme {
        BookingScreen(
            courtId = "1",
            numberOfCourts = 3,
            onNavigateBack = {},
            onNavigateToPayment = {}
        )
    }
}
