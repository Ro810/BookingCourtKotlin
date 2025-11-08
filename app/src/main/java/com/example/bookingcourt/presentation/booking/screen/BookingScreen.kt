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
import com.example.bookingcourt.domain.model.CourtDetail
import com.example.bookingcourt.presentation.booking.viewmodel.BookingViewModel
import com.example.bookingcourt.presentation.theme.BookingCourtTheme
import com.example.bookingcourt.presentation.theme.Primary
import java.text.SimpleDateFormat
import java.util.*
import com.google.gson.Gson
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import kotlinx.coroutines.launch

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
    // ✅ Venue object - reactive to court parameter changes
    val venue = remember(court, courtId) {
        court ?: Venue(
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
    }

    // Fetch real courts for this venue
    val courtsState by bookingViewModel.courtsState.collectAsState()
    val realCourts = remember { mutableStateOf<List<CourtDetail>>(emptyList()) }

    // ✅ State cho booked slots
    val bookedSlotsState by bookingViewModel.bookedSlotsState.collectAsState()
    val bookedSlots = remember { mutableStateOf<List<com.example.bookingcourt.domain.model.BookedSlot>>(emptyList()) }

    // ✅ Thêm coroutineScope để gọi suspend functions
    val coroutineScope = rememberCoroutineScope()

    // ✅ Khai báo selectedDate sớm hơn để dùng trong LaunchedEffect
    var selectedDate by remember { mutableStateOf("") }
    var selectedSlots by remember { mutableStateOf(setOf<CourtTimeSlot>()) }
    var playerName by remember(currentUser) { mutableStateOf(currentUser?.fullName ?: "") }
    var phoneNumber by remember(currentUser) { mutableStateOf(currentUser?.phoneNumber ?: "") }
    var showDatePicker by remember { mutableStateOf(false) }

    // State để hiển thị error message
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = System.currentTimeMillis()
    )

    // Fetch courts when screen is first composed
    LaunchedEffect(venue.id) {
        bookingViewModel.getCourtsByVenueId(venue.id)
    }

    // ✅ Fetch booked slots khi selectedDate thay đổi
    LaunchedEffect(selectedDate, venue.id) {
        if (selectedDate.isNotEmpty()) {
            // Convert date from dd/MM/yyyy to yyyy-MM-dd for API
            val parts = selectedDate.split("/")
            if (parts.size == 3) {
                val apiDate = "${parts[2]}-${parts[1].padStart(2, '0')}-${parts[0].padStart(2, '0')}"
                Log.d("BookingScreen", "🔍 Fetching booked slots for venue ${venue.id} on $apiDate")
                bookingViewModel.getBookedSlots(venue.id, apiDate)
            }
        }
    }

    // Update realCourts when courtsState changes
    LaunchedEffect(courtsState) {
        when (courtsState) {
            is Resource.Success -> {
                realCourts.value = (courtsState as Resource.Success<List<CourtDetail>>).data ?: emptyList()
                Log.d("BookingScreen", "✅ Loaded ${realCourts.value.size} real courts for venue ${venue.id}")

                // ✅ DETAILED LOG: Show all courts with their IDs
                Log.d("BookingScreen", "========== AVAILABLE COURTS FOR VENUE ${venue.id} ==========")
                realCourts.value.forEachIndexed { index, court ->
                    Log.d("BookingScreen", "  Court ${index + 1}: ID=${court.id}, Description='${court.description}'")
                }
                Log.d("BookingScreen", "=========================================================")
            }
            is Resource.Error -> {
                Log.e("BookingScreen", "❌ Error loading courts: ${(courtsState as Resource.Error).message}")
                Log.w("BookingScreen", "⚠️ Will use fallback: sequential court numbers")
                // Fallback: Không có courts từ API, sẽ dùng số thứ tự
            }
            is Resource.Loading -> {
                Log.d("BookingScreen", "⏳ Loading courts for venue ${venue.id}...")
            }
            else -> {}
        }
    }

    // ✅ Update booked slots khi bookedSlotsState thay đổi
    LaunchedEffect(bookedSlotsState) {
        when (bookedSlotsState) {
            is Resource.Success -> {
                bookedSlots.value = (bookedSlotsState as Resource.Success<List<com.example.bookingcourt.domain.model.BookedSlot>>).data ?: emptyList()
                Log.d("BookingScreen", "✅ Loaded ${bookedSlots.value.size} booked slots")
                bookedSlots.value.forEach { slot ->
                    Log.d("BookingScreen", "  📅 Slot: Court ${slot.courtNumber}, ${slot.startTime} - ${slot.endTime}, Status: ${slot.status}")
                }
            }
            is Resource.Error -> {
                Log.e("BookingScreen", "❌ Error loading booked slots: ${(bookedSlotsState as Resource.Error).message}")
            }
            is Resource.Loading -> {
                Log.d("BookingScreen", "⏳ Loading booked slots...")
            }
            else -> {}
        }
    }

    // Số lượng sân con trong venue này - sử dụng số sân thực tế từ API hoặc fallback
    val actualNumberOfCourts = remember(realCourts.value.size, venue.courtsCount) {
        if (realCourts.value.isNotEmpty()) {
            realCourts.value.size
        } else {
            venue.courtsCount
        }
    }

    // Parse opening and closing time from venue
    val openingTime = remember(venue.openingTime) {
        val result = venue.openingTime?.split(":")?.let { parts ->
            if (parts.size >= 2) Pair(parts[0].toIntOrNull() ?: 6, parts[1].toIntOrNull() ?: 0)
            else Pair(6, 0)
        } ?: Pair(6, 0)
        Log.d("BookingScreen", "📍 Opening time: ${venue.openingTime} → Parsed: ${result.first}:${result.second}")
        result
    }

    val closingTime = remember(venue.closingTime) {
        val result = venue.closingTime?.split(":")?.let { parts ->
            if (parts.size >= 2) Pair(parts[0].toIntOrNull() ?: 22, parts[1].toIntOrNull() ?: 0)
            else Pair(22, 0)
        } ?: Pair(22, 0)
        Log.d("BookingScreen", "📍 Closing time: ${venue.closingTime} → Parsed: ${result.first}:${result.second}")
        result
    }

    // Tạo danh sách khung giờ - mỗi 30 phút
    val timeSlots = remember(openingTime, closingTime) {
        val slots = mutableListOf<String>()
        var currentHour = openingTime.first
        var currentMinute = openingTime.second

        var closeHour = closingTime.first
        var closeMinute = closingTime.second

        // ✅ Special case: Nếu thời gian là 00:00 - 00:00 → Hiểu là mở cả ngày (00:00 - 23:59)
        if (currentHour == 0 && currentMinute == 0 && closeHour == 0 && closeMinute == 0) {
            Log.d("BookingScreen", "📍 Detected 00:00 - 00:00 → Treating as FULL DAY (00:00 - 23:59)")
            closeHour = 23
            closeMinute = 59
        }

        Log.d("BookingScreen", "📍 Generating time slots from ${currentHour}:${currentMinute} to ${closeHour}:${closeMinute}")

        while (currentHour < closeHour || (currentHour == closeHour && currentMinute < closeMinute)) {
            slots.add(String.format("%02d:%02d", currentHour, currentMinute))

            currentMinute += 30
            if (currentMinute >= 60) {
                currentMinute = 0
                currentHour++
            }
        }

        Log.d("BookingScreen", "📍 Generated ${slots.size} time slots: ${slots.take(5)}...")
        slots
    }

    // Log venue and court info
    LaunchedEffect(venue, actualNumberOfCourts) {
        Log.d("BookingScreen", "========== BOOKING SCREEN DEBUG ==========")
        Log.d("BookingScreen", "📍 Venue: ${venue.name} (ID: ${venue.id})")
        Log.d("BookingScreen", "📍 Venue courtsCount: ${venue.courtsCount}")
        Log.d("BookingScreen", "📍 Venue numberOfCourt: ${venue.numberOfCourt}")
        Log.d("BookingScreen", "📍 Actual number of courts: $actualNumberOfCourts")
        Log.d("BookingScreen", "📍 Opening time: ${venue.openingTime}")
        Log.d("BookingScreen", "📍 Closing time: ${venue.closingTime}")
        Log.d("BookingScreen", "📍 Time slots count: ${timeSlots.size}")
        Log.d("BookingScreen", "==========================================")
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

                                    // ✅ Cải thiện logic kiểm tra slot đã đặt
                                    val isBooked = bookedSlots.value.any { bookedSlot ->
                                        if (bookedSlot.courtNumber != courtNum) {
                                            false
                                        } else {
                                            // So sánh startTime và endTime với format chính xác
                                            val slotStartTime = timeSlotToStartTime(time)
                                            val slotEndTime = timeSlotToEndTime(time)

                                            // Extract HH:mm:ss from ISO datetime if needed
                                            val bookedStart = if (bookedSlot.startTime.contains("T")) {
                                                bookedSlot.startTime.substring(11, 19) // "HH:mm:ss"
                                            } else {
                                                bookedSlot.startTime
                                            }

                                            val bookedEnd = if (bookedSlot.endTime.contains("T")) {
                                                bookedSlot.endTime.substring(11, 19) // "HH:mm:ss"
                                            } else {
                                                bookedSlot.endTime
                                            }

                                            val matches = (slotStartTime == bookedStart && slotEndTime == bookedEnd)

                                            if (matches) {
                                                Log.d("BookingScreen", "🔒 Slot blocked: Court $courtNum, Time $time ($slotStartTime-$slotEndTime) matches booked slot ($bookedStart-$bookedEnd)")
                                            }

                                            matches
                                        }
                                    }

                                    Box(
                                        modifier = Modifier
                                            .width(80.dp)
                                            .height(45.dp)
                                            .border(1.dp, Color.Gray)
                                            .background(
                                                when {
                                                    isSelected -> Primary
                                                    isBooked -> Color(0xFFFFCDD2) // Màu đỏ nhạt cho slot đã đặt
                                                    else -> Color.White
                                                }
                                            )
                                            .clickable {
                                                if (!isBooked) {
                                                    selectedSlots = if (isSelected) {
                                                        selectedSlots - slot
                                                    } else {
                                                        selectedSlots + slot
                                                    }
                                                } else {
                                                    // ✅ Thông báo khi click vào slot đã đặt
                                                    coroutineScope.launch {
                                                        snackbarHostState.showSnackbar(
                                                            message = "Khung giờ này đã có người đặt",
                                                            duration = SnackbarDuration.Short
                                                        )
                                                    }
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
                                        } else if (isBooked) {
                                            Text(
                                                text = "Đã đặt",
                                                color = Color.Black,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                textAlign = TextAlign.Center
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
                                color = Primary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Confirm Button
            // ✅ Disable button nếu courts chưa được load hoặc đang loading
            val courtsLoaded = courtsState is Resource.Success &&
                               (courtsState as? Resource.Success<List<CourtDetail>>)?.data?.isNotEmpty() == true

            Button(
                onClick = {
                    if (selectedSlots.isNotEmpty()) {
                        val firstSlot = selectedSlots.first()
                        val courtNumber = firstSlot.courtNumber

                        // ✅ Xác định court ID - Sử dụng thứ tự trong list (KHÔNG parse description)
                        // ⚠️ QUAN TRỌNG: Backend yêu cầu format "venueId_courtId" (ví dụ: "5_6")
                        // Strategy: Court Number từ UI (1, 2, 3...) → Index trong list (0, 1, 2...) → Court ID thực tế
                        Log.d("BookingScreen", "=".repeat(60))
                        Log.d("BookingScreen", "========== COURT ID MAPPING ==========")
                        Log.d("BookingScreen", "🎯 User selected Court Number (UI): $courtNumber")
                        Log.d("BookingScreen", "🏢 Venue ID: ${venue.id}")

                        // ✅ Lấy courts trực tiếp từ courtsState (KHÔNG dùng realCourts.value)
                        val availableCourts = when (courtsState) {
                            is Resource.Success -> (courtsState as Resource.Success<List<CourtDetail>>).data ?: emptyList()
                            else -> emptyList()
                        }

                        val realCourtId: String? = if (availableCourts.isNotEmpty()) {
                            // ✅ Sort courts theo ID để đảm bảo thứ tự nhất quán
                            val sortedCourts = availableCourts.sortedBy { it.id }
                            val courtIndex = courtNumber - 1 // Court 1 → index 0, Court 2 → index 1, ...

                            Log.d("BookingScreen", "📋 Available courts: ${sortedCourts.size}")
                            Log.d("BookingScreen", "📋 Mapping strategy: Court Number $courtNumber → Index $courtIndex")

                            // Log tất cả courts để debug
                            sortedCourts.forEachIndexed { index, court ->
                                Log.d("BookingScreen", "  [$index] Court ID=${court.id}, Description='${court.description}'")
                            }

                            if (courtIndex >= 0 && courtIndex < sortedCourts.size) {
                                val selectedCourt = sortedCourts[courtIndex]
                                Log.d("BookingScreen", "✅ COURT FOUND!")
                                Log.d("BookingScreen", "  UI Court Number: $courtNumber")
                                Log.d("BookingScreen", "  Array Index: $courtIndex")
                                Log.d("BookingScreen", "  Real Court ID: ${selectedCourt.id}")
                                Log.d("BookingScreen", "  Description: '${selectedCourt.description}'")

                                // ✅ FORMAT: "venueId_courtId" như backend yêu cầu
                                val formattedCourtId = "${venue.id}_${selectedCourt.id}"
                                Log.d("BookingScreen", "  ✅ Formatted: ${venue.id}_${selectedCourt.id} = $formattedCourtId")
                                formattedCourtId
                            } else {
                                Log.e("BookingScreen", "❌ INDEX OUT OF BOUNDS!")
                                Log.e("BookingScreen", "  Court Number: $courtNumber")
                                Log.e("BookingScreen", "  Calculated Index: $courtIndex")
                                Log.e("BookingScreen", "  Available Courts: ${sortedCourts.size}")
                                // Show error và return null để không tiếp tục
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar(
                                        message = "Lỗi: Không tìm thấy sân. Vui lòng thử lại.",
                                        duration = SnackbarDuration.Long
                                    )
                                }
                                null
                            }
                        } else {
                            Log.e("BookingScreen", "❌ NO COURTS LOADED!")
                            Log.e("BookingScreen", "  Venue ID: ${venue.id}")
                            Log.e("BookingScreen", "  CourtsState: $courtsState")
                            Log.e("BookingScreen", "  Cannot map court number $courtNumber without court list")
                            // Show error và return null để không tiếp tục
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar(
                                    message = "Lỗi: Chưa tải được danh sách sân. Vui lòng thử lại.",
                                    duration = SnackbarDuration.Long
                                )
                            }
                            null
                        }

                        // ✅ Kiểm tra nếu realCourtId là null thì dừng lại, không thực hiện booking
                        if (realCourtId == null) {
                            Log.e("BookingScreen", "❌ Cannot proceed with booking - Court ID is null")
                            return@Button
                        }

                        Log.d("BookingScreen", "🔑 FINAL Court ID to send (format: venueId_courtId): $realCourtId")
                        Log.d("BookingScreen", "=".repeat(60))

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

                        Log.d("BookingScreen", "📝 Final booking request:")
                        Log.d("BookingScreen", "  Court ID: $realCourtId")
                        Log.d("BookingScreen", "  Start: $startTime")
                        Log.d("BookingScreen", "  End: $endTime")

                        // Chỉ chuẩn bị dữ liệu và navigate sang PaymentScreen
                        val bookingData = BookingData(
                            courtId = realCourtId,
                            courtName = "${venue.name} - Sân $courtNumber",
                            courtAddress = venue.address.getFullAddress(),
                            selectedDate = selectedDateFormatted,
                            selectedSlots = selectedSlots,
                            playerName = playerName,
                            phoneNumber = phoneNumber,
                            pricePerHour = venue.pricePerHour,
                            totalPrice = (venue.pricePerHour * selectedSlots.size * 0.5).toLong(),
                            ownerBankInfo = null, // Sẽ nhận được từ API khi gọi ở PaymentScreen
                            expireTime = null, // Sẽ nhận được từ API khi gọi ở PaymentScreen
                            startTime = startTime, // Thêm startTime cho API
                            endTime = endTime // Thêm endTime cho API
                        )

                        // Serialize to JSON for navigation
                        val gson = Gson()
                        val bookingDataJson = gson.toJson(bookingData)
                        val encodedJson = URLEncoder.encode(bookingDataJson, StandardCharsets.UTF_8.toString())

                        // Navigate to PaymentScreen
                        onNavigateToPayment(encodedJson)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Primary),
                enabled = selectedSlots.isNotEmpty() &&
                         playerName.isNotEmpty() &&
                         phoneNumber.isNotEmpty() &&
                         courtsLoaded  // ✅ Chỉ enable khi courts đã được load
            ) {
                Text("Xác nhận đặt sân", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
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

// Helper function để chuyển đổi time slot thành start time cho booked slot
private fun timeSlotToStartTime(timeSlot: String): String {
    val parts = timeSlot.split(":")
    if (parts.size != 2) return timeSlot

    val hour = parts[0].toIntOrNull() ?: return timeSlot
    val minute = parts[1].toIntOrNull() ?: return timeSlot

    return String.format("%02d:%02d:00", hour, minute)
}

// Helper function để chuyển đổi time slot thành end time cho booked slot
private fun timeSlotToEndTime(timeSlot: String): String {
    val parts = timeSlot.split(":")
    if (parts.size != 2) return timeSlot

    val hour = parts[0].toIntOrNull() ?: return timeSlot
    val minute = parts[1].toIntOrNull() ?: return timeSlot

    val totalMinutes = hour * 60 + minute + 30
    val endHour = (totalMinutes / 60) % 24
    val endMinute = totalMinutes % 60

    return String.format("%02d:%02d:00", endHour, endMinute)
}
