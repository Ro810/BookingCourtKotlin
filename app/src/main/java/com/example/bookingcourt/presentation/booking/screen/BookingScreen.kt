package com.example.bookingcourt.presentation.booking.screen

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
import com.example.bookingcourt.domain.model.Court
import com.example.bookingcourt.domain.model.CourtType
import com.example.bookingcourt.domain.model.SportType
import com.example.bookingcourt.domain.model.User
import com.example.bookingcourt.domain.model.BookingData
import com.example.bookingcourt.domain.model.CourtTimeSlot
import com.example.bookingcourt.presentation.theme.BookingCourtTheme
import com.example.bookingcourt.presentation.theme.Primary
import kotlinx.datetime.LocalTime
import java.text.SimpleDateFormat
import java.util.*
import com.google.gson.Gson
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingScreen(
    courtId: String,
    numberOfCourts: Int = 1, // Deprecated: sẽ sử dụng court.courtsCount
    // Optional: prefer providing full court object and current user from caller
    court: Court? = null,
    currentUser: User? = null,
    onNavigateBack: () -> Unit,
    onNavigateToPayment: (String) -> Unit
) {
    // Use provided court if available; otherwise, fallback to a placeholder court object
    val court = court ?: Court(
        id = courtId,
        name = "Sân Cầu Lông ABC",
        description = "Sân cầu lông chất lượng cao",
        address = "123 Đường Lê Lợi, Quận 1, TP.HCM",
        latitude = 10.7769,
        longitude = 106.7009,
        images = emptyList(),
        sportType = SportType.BADMINTON,
        courtType = CourtType.INDOOR,
        pricePerHour = 150,
        openTime = LocalTime(6, 0),
        closeTime = LocalTime(22, 0),
        amenities = emptyList(),
        rules = "Không hút thuốc trong sân",
        ownerId = "owner1",
        rating = 4.5f,
        totalReviews = 120,
        isActive = true,
        maxPlayers = 4,
        courtsCount = 3
    )

    // Số lượng sân từ API
    val actualNumberOfCourts = court.courtsCount

    var selectedDate by remember { mutableStateOf("") }
    var selectedSlots by remember { mutableStateOf(setOf<CourtTimeSlot>()) } // Lưu các ô đã chọn
    // Initialize player info from provided currentUser when available
    var playerName by remember(currentUser) { mutableStateOf(currentUser?.fullName ?: "") }
    var phoneNumber by remember(currentUser) { mutableStateOf(currentUser?.phoneNumber ?: "") }
    var showDatePicker by remember { mutableStateOf(false) }

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = System.currentTimeMillis()
    )

    // Tạo danh sách khung giờ dựa trên openTime và closeTime từ API - mỗi 30 phút
    val timeSlots = remember(court.openTime, court.closeTime) {
        val slots = mutableListOf<String>()
        var currentHour = court.openTime.hour
        var currentMinute = court.openTime.minute

        val closeHour = court.closeTime.hour
        val closeMinute = court.closeTime.minute

        while (currentHour < closeHour || (currentHour == closeHour && currentMinute < closeMinute)) {
            slots.add(String.format("%02d:%02d", currentHour, currentMinute))

            // Tăng 30 phút
            currentMinute += 30
            if (currentMinute >= 60) {
                currentMinute = 0
                currentHour++
            }
        }

        slots
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
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Court Info
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Primary.copy(alpha = 0.1f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = court.name,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = court.address,
                        fontSize = 14.sp,
                        color = Color.Black
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
                    // Fixed Column - Tên sân (cố định, không cuộn)
                    Column {
                        // Header cell - "Sân"
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

                        // Các hàng tên sân
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

                    // Scrollable Column - Các khung giờ (có thể cuộn ngang)
                    Column(
                        modifier = Modifier.horizontalScroll(rememberScrollState())
                    ) {
                        // Header Row - Các khung giờ
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

                        // Data Rows - Các ô chọn giờ
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

            // Thông tin đã chọn
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
                            text = "${court.pricePerHour / 1000}.000 VNĐ",
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
                                text = "${(court.pricePerHour * selectedSlots.size * 0.5).toLong() / 1000}.000 VNĐ",
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
                    // Create BookingData object with all booking information
                    val bookingData = BookingData(
                        courtId = court.id,
                        courtName = court.name,
                        courtAddress = court.address,
                        selectedDate = selectedDate.ifEmpty {
                            SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
                        },
                        selectedSlots = selectedSlots,
                        playerName = playerName,
                        phoneNumber = phoneNumber,
                        pricePerHour = court.pricePerHour,
                        totalPrice = (court.pricePerHour * selectedSlots.size * 0.5).toLong()
                    )
                    // Serialize to JSON for navigation
                    val gson = Gson()
                    val bookingDataJson = gson.toJson(bookingData)
                    // URL encode to prevent navigation errors
                    val encodedJson = URLEncoder.encode(bookingDataJson, StandardCharsets.UTF_8.toString())
                    onNavigateToPayment(encodedJson)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Primary),
                enabled = selectedSlots.isNotEmpty() && playerName.isNotEmpty() && phoneNumber.isNotEmpty()
            ) {
                Text("Xác nhận đặt sân", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun BookingScreenPreview() {
    BookingCourtTheme {
        BookingScreen(
            courtId = "VN001",
            numberOfCourts = 3,
            onNavigateBack = {},
            onNavigateToPayment = {}
        )
    }
}
