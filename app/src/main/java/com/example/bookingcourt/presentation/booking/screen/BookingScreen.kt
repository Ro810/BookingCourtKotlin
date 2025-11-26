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
import androidx.compose.material.icons.filled.Lock
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
import com.example.bookingcourt.domain.model.BookingItemData
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
    // Venue object - reactive to court parameter changes
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

    // State cho booked slots
    val bookedSlotsState by bookingViewModel.bookedSlotsState.collectAsState()
    val bookedSlots = remember { mutableStateOf<List<com.example.bookingcourt.domain.model.BookedSlot>>(emptyList()) }

    // Thêm coroutineScope để gọi suspend functions
    val coroutineScope = rememberCoroutineScope()

    // Khai báo selectedDate với ngày hiện tại ngay từ đầu để tự động fetch booked slots
    var selectedDate by remember {
        mutableStateOf(SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date()))
    }
    var selectedSlots by remember { mutableStateOf(setOf<CourtTimeSlot>()) }
    var playerName by remember(currentUser) { mutableStateOf(currentUser?.fullName ?: "") }
    var phoneNumber by remember(currentUser) { mutableStateOf(currentUser?.phoneNumber ?: "") }
    var showDatePicker by remember { mutableStateOf(false) }

    // State để hiển thị error message
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    // Lấy ngày hiện tại để so sánh
    val today = remember {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        cal.timeInMillis
    }

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = System.currentTimeMillis(),
        // Chặn không cho chọn ngày trong quá khứ
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                // Chuyển UTC time thành local date để so sánh
                val selectedCal = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
                selectedCal.timeInMillis = utcTimeMillis

                val todayCal = Calendar.getInstance()

                // So sánh năm, tháng, ngày
                return selectedCal.get(Calendar.YEAR) > todayCal.get(Calendar.YEAR) ||
                       (selectedCal.get(Calendar.YEAR) == todayCal.get(Calendar.YEAR) &&
                        selectedCal.get(Calendar.DAY_OF_YEAR) >= todayCal.get(Calendar.DAY_OF_YEAR))
            }

            override fun isSelectableYear(year: Int): Boolean {
                val currentYear = Calendar.getInstance().get(Calendar.YEAR)
                return year >= currentYear
            }
        }
    )

    // Reset DatePicker về ngày hiện tại mỗi khi mở dialog
    LaunchedEffect(showDatePicker) {
        if (showDatePicker) {
            // Tính toán timestamp UTC chính xác cho ngày hiện tại
            // DatePicker hoạt động với UTC timezone, nên phải convert đúng cách
            val localCalendar = Calendar.getInstance()

            // Tạo calendar với UTC timezone
            val utcCalendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
            utcCalendar.set(Calendar.YEAR, localCalendar.get(Calendar.YEAR))
            utcCalendar.set(Calendar.MONTH, localCalendar.get(Calendar.MONTH))
            utcCalendar.set(Calendar.DAY_OF_MONTH, localCalendar.get(Calendar.DAY_OF_MONTH))
            utcCalendar.set(Calendar.HOUR_OF_DAY, 0)
            utcCalendar.set(Calendar.MINUTE, 0)
            utcCalendar.set(Calendar.SECOND, 0)
            utcCalendar.set(Calendar.MILLISECOND, 0)

            datePickerState.selectedDateMillis = utcCalendar.timeInMillis

            Log.d("BookingScreen", "DatePicker reset to: ${SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(localCalendar.time)}")
            Log.d("BookingScreen", "UTC timestamp: ${utcCalendar.timeInMillis}")
        }
    }

    // Fetch courts when screen is first composed
    LaunchedEffect(venue.id) {
        bookingViewModel.getCourtsByVenueId(venue.id)
    }

    // Fetch booked slots khi selectedDate thay đổi
    LaunchedEffect(selectedDate, venue.id) {
        if (selectedDate.isNotEmpty()) {
            // Convert date from dd/MM/yyyy to yyyy-MM-dd for API
            val parts = selectedDate.split("/")
            if (parts.size == 3) {
                val apiDate = "${parts[2]}-${parts[1].padStart(2, '0')}-${parts[0].padStart(2, '0')}"
                Log.d("BookingScreen", "Fetching booked slots for venue ${venue.id} on $apiDate")
                bookingViewModel.getBookedSlots(venue.id, apiDate)
            }
        }
    }

    // Update realCourts when courtsState changes
    LaunchedEffect(courtsState) {
        when (courtsState) {
            is Resource.Success -> {
                realCourts.value = (courtsState as Resource.Success<List<CourtDetail>>).data ?: emptyList()
                Log.d("BookingScreen", "Loaded ${realCourts.value.size} real courts for venue ${venue.id}")

                // DETAILED LOG: Show all courts with their IDs
                Log.d("BookingScreen", "========== AVAILABLE COURTS FOR VENUE ${venue.id} ==========")
                realCourts.value.forEachIndexed { index, court ->
                    Log.d("BookingScreen", "  Court ${index + 1}: ID=${court.id}, Description='${court.description}'")
                }
                Log.d("BookingScreen", "=========================================================")
            }
            is Resource.Error -> {
                Log.e("BookingScreen", "Error loading courts: ${(courtsState as Resource.Error).message}")
                Log.w("BookingScreen", "Will use fallback: sequential court numbers")
                // Fallback: Không có courts từ API, sẽ dùng số thứ tự
            }
            is Resource.Loading -> {
                Log.d("BookingScreen", "Loading courts for venue ${venue.id}...")
            }
            else -> {}
        }
    }

    // Update booked slots khi bookedSlotsState thay đổi
    LaunchedEffect(bookedSlotsState) {
        when (bookedSlotsState) {
            is Resource.Success -> {
                bookedSlots.value = (bookedSlotsState as Resource.Success<List<com.example.bookingcourt.domain.model.BookedSlot>>).data ?: emptyList()
                Log.d("BookingScreen", "Loaded ${bookedSlots.value.size} booked slots")
                bookedSlots.value.forEach { slot ->
                    Log.d("BookingScreen", "  Slot: Court ${slot.courtNumber}, ${slot.startTime} - ${slot.endTime}, Status: ${slot.status}")
                }
            }
            is Resource.Error -> {
                Log.e("BookingScreen", "Error loading booked slots: ${(bookedSlotsState as Resource.Error).message}")
            }
            is Resource.Loading -> {
                Log.d("BookingScreen", "Loading booked slots...")
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
        Log.d("BookingScreen", "Opening time: ${venue.openingTime} -> Parsed: ${result.first}:${result.second}")
        result
    }

    val closingTime = remember(venue.closingTime) {
        val result = venue.closingTime?.split(":")?.let { parts ->
            if (parts.size >= 2) Pair(parts[0].toIntOrNull() ?: 22, parts[1].toIntOrNull() ?: 0)
            else Pair(22, 0)
        } ?: Pair(22, 0)
        Log.d("BookingScreen", "Closing time: ${venue.closingTime} -> Parsed: ${result.first}:${result.second}")
        result
    }

    // Tạo danh sách khung giờ - mỗi 30 phút
    val timeSlots = remember(openingTime, closingTime) {
        val slots = mutableListOf<String>()
        var currentHour = openingTime.first
        var currentMinute = openingTime.second

        var closeHour = closingTime.first
        var closeMinute = closingTime.second

        // Special case: Nếu thời gian là 00:00 - 00:00 → Hiểu là mở cả ngày (00:00 - 23:59)
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
        Log.d("BookingScreen", "Venue: ${venue.name} (ID: ${venue.id})")
        Log.d("BookingScreen", "Venue courtsCount: ${venue.courtsCount}")
        Log.d("BookingScreen", "Venue numberOfCourt: ${venue.numberOfCourt}")
        Log.d("BookingScreen", "Actual number of courts: $actualNumberOfCourts")
        Log.d("BookingScreen", "Opening time: ${venue.openingTime}")
        Log.d("BookingScreen", "Closing time: ${venue.closingTime}")
        Log.d("BookingScreen", "Time slots count: ${timeSlots.size}")
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
                            // Kiểm tra court có bị khóa không
                            val courtIndex = courtNum - 1
                            val sortedCourts = realCourts.value.sortedBy { it.id }
                            val court = sortedCourts.getOrNull(courtIndex)
                            val isCourtLocked = court?.isActive == false

                            Box(
                                modifier = Modifier
                                    .width(70.dp)
                                    .height(45.dp)
                                    .border(1.dp, Color.Gray)
                                    .background(
                                        if (isCourtLocked) Color(0xFFFFEBEE) // Light red for locked
                                        else Color(0xFFF5F5F5)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    if (isCourtLocked) {
                                        Icon(
                                            Icons.Default.Lock,
                                            contentDescription = "Đang khóa",
                                            modifier = Modifier.size(10.dp),
                                            tint = Color(0xFFFF5722)
                                        )
                                        Spacer(modifier = Modifier.width(2.dp))
                                    }
                                    Text(
                                        text = "Sân $courtNum",
                                        fontSize = if (isCourtLocked) 11.sp else 13.sp,
                                        fontWeight = FontWeight.Medium,
                                        textAlign = TextAlign.Center,
                                        color = if (isCourtLocked) Color(0xFFFF5722) else Color.Black
                                    )
                                }
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

                                    // Kiểm tra xem slot này đã qua giờ chưa (chỉ áp dụng cho ngày hôm nay)
                                    val isPastTime = remember(selectedDate, time) {
                                        val selectedDateParsed = try {
                                            SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(selectedDate)
                                        } catch (e: Exception) {
                                            Date()
                                        }

                                        val todayDate = Calendar.getInstance()
                                        todayDate.set(Calendar.HOUR_OF_DAY, 0)
                                        todayDate.set(Calendar.MINUTE, 0)
                                        todayDate.set(Calendar.SECOND, 0)
                                        todayDate.set(Calendar.MILLISECOND, 0)

                                        val selectedCal = Calendar.getInstance()
                                        selectedCal.time = selectedDateParsed ?: Date()
                                        selectedCal.set(Calendar.HOUR_OF_DAY, 0)
                                        selectedCal.set(Calendar.MINUTE, 0)
                                        selectedCal.set(Calendar.SECOND, 0)
                                        selectedCal.set(Calendar.MILLISECOND, 0)

                                        // Chỉ check past time nếu là ngày hôm nay
                                        if (selectedCal.timeInMillis == todayDate.timeInMillis) {
                                            val now = Calendar.getInstance()
                                            val timeParts = time.split(":")
                                            val slotHour = timeParts[0].toIntOrNull() ?: 0
                                            val slotMinute = timeParts[1].toIntOrNull() ?: 0

                                            val slotStartTime = Calendar.getInstance()
                                            slotStartTime.set(Calendar.HOUR_OF_DAY, slotHour)
                                            slotStartTime.set(Calendar.MINUTE, slotMinute)
                                            slotStartTime.set(Calendar.SECOND, 0)
                                            slotStartTime.set(Calendar.MILLISECOND, 0)

                                            // Slot đã qua nếu thời gian BẮT ĐẦU của slot <= hiện tại
                                            // Ví dụ: Nếu bây giờ là 12:35, thì slot 12:30 đã qua (vì 12:30 < 12:35)
                                            // và slot 12:00 cũng đã qua
                                            slotStartTime.before(now) || slotStartTime.equals(now)
                                        } else {
                                            false
                                        }
                                    }

                                    // Map courtNum (UI index) sang courtId thực tế để so sánh với bookedSlots
                                    val courtIndex = courtNum - 1
                                    val realCourtId = if (courtIndex >= 0 && courtIndex < realCourts.value.size) {
                                        val sortedCourts = realCourts.value.sortedBy { it.id }
                                        sortedCourts.getOrNull(courtIndex)?.id?.toInt()
                                    } else null

                                    // Kiểm tra slot đã đặt - so sánh với courtId thực tế
                                    val isBooked = bookedSlots.value.any { bookedSlot ->
                                        // So sánh với courtId thực tế (bookedSlot.courtId) thay vì courtNumber
                                        if (bookedSlot.courtId.toInt() != realCourtId) {
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

                                            // FIX: Check for overlap instead of exact match
                                            // Two time ranges overlap if:
                                            // - Slot start < Booked end AND
                                            // - Slot end > Booked start
                                            val overlaps = slotStartTime < bookedEnd && slotEndTime > bookedStart

                                            if (overlaps) {
                                                Log.d("BookingScreen", "🔒 Slot blocked: Court $courtNum (ID=$realCourtId), Time $time ($slotStartTime-$slotEndTime) overlaps with booked slot (ID=${bookedSlot.courtId}, $bookedStart-$bookedEnd)")
                                            }

                                            overlaps
                                        }
                                    }

                                    // Kiểm tra court có bị khóa không
                                    val sortedCourtsForSlot = realCourts.value.sortedBy { it.id }
                                    val courtForSlot = sortedCourtsForSlot.getOrNull(courtIndex)
                                    val isCourtLocked = courtForSlot?.isActive == false

                                    Box(
                                        modifier = Modifier
                                            .width(80.dp)
                                            .height(45.dp)
                                            .border(1.dp, Color.Gray)
                                            .background(
                                                when {
                                                    isSelected -> Primary
                                                    isCourtLocked -> Color(0xFFE0E0E0) // Màu xám cho sân khóa
                                                    isPastTime -> Color(0xFFBDBDBD) // Màu xám cho slot đã qua
                                                    isBooked -> Color(0xFFFFCDD2) // Màu đỏ nhạt cho slot đã đặt
                                                    else -> Color.White
                                                }
                                            )
                                            .clickable {
                                                when {
                                                    isCourtLocked -> {
                                                        // Thông báo khi click vào sân đã khóa
                                                        coroutineScope.launch {
                                                            snackbarHostState.showSnackbar(
                                                                message = "Sân này đang tạm khóa, không thể đặt",
                                                                duration = SnackbarDuration.Short
                                                            )
                                                        }
                                                    }
                                                    isPastTime -> {
                                                        // Thông báo khi click vào slot đã qua giờ
                                                        coroutineScope.launch {
                                                            snackbarHostState.showSnackbar(
                                                                message = "Không thể đặt khung giờ đã qua",
                                                                duration = SnackbarDuration.Short
                                                            )
                                                        }
                                                    }
                                                    isBooked -> {
                                                        // Thông báo khi click vào slot đã đặt
                                                        coroutineScope.launch {
                                                            snackbarHostState.showSnackbar(
                                                                message = "Khung giờ này đã có người đặt",
                                                                duration = SnackbarDuration.Short
                                                            )
                                                        }
                                                    }
                                                    else -> {
                                                        selectedSlots = if (isSelected) {
                                                            selectedSlots - slot
                                                        } else {
                                                            selectedSlots + slot
                                                        }
                                                    }
                                                }
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        when {
                                            isSelected -> {
                                                Text(
                                                    text = "✓",
                                                    color = Color.White,
                                                    fontSize = 18.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                            isCourtLocked -> {
                                                // Hiển thị icon khóa cho sân đã khóa
                                                Icon(
                                                    Icons.Default.Lock,
                                                    contentDescription = "Sân khóa",
                                                    modifier = Modifier.size(16.dp),
                                                    tint = Color(0xFF757575)
                                                )
                                            }
                                            isPastTime -> {
                                                // Hiển thị text cho ô đã qua giờ
                                                Text(
                                                    text = "Đã qua",
                                                    color = Color.Gray,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Medium,
                                                    textAlign = TextAlign.Center
                                                )
                                            }
                                            isBooked -> {
                                                // FIX: Hiển thị text "Đã đặt" cho ô đã được đặt
                                                Text(
                                                    text = "Đã đặt",
                                                    color = Color(0xFFD32F2F),
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Medium,
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
                            val timeRanges = groupConsecutiveTimeSlots(slots.map { it.timeSlot })
                            Text(
                                text = "• Sân $courtNum: ${timeRanges.joinToString(", ")}",
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
            // Disable button nếu courts chưa được load hoặc đang loading
            val courtsLoaded = courtsState is Resource.Success &&
                               (courtsState as? Resource.Success<List<CourtDetail>>)?.data?.isNotEmpty() == true

            Button(
                onClick = {
                    if (selectedSlots.isNotEmpty()) {
                        // NHÓM SLOTS THEO SÂN để xử lý đúng khi đặt nhiều sân
                        val slotsByCourtNumber = selectedSlots.groupBy { it.courtNumber }

                        Log.d("BookingScreen", "=".repeat(60))
                        Log.d("BookingScreen", "========== PROCESSING BOOKING ==========")
                        Log.d("BookingScreen", "📋 Total slots selected: ${selectedSlots.size}")
                        Log.d("BookingScreen", "🏟️ Number of courts: ${slotsByCourtNumber.size}")
                        slotsByCourtNumber.forEach { (courtNum, slots) ->
                            Log.d("BookingScreen", "  Court $courtNum: ${slots.size} slots")
                        }

                        // Lấy courts từ state
                        val availableCourts = when (courtsState) {
                            is Resource.Success -> (courtsState as Resource.Success<List<CourtDetail>>).data ?: emptyList()
                            else -> emptyList()
                        }

                        if (availableCourts.isEmpty()) {
                            Log.e("BookingScreen", "❌ No courts loaded!")
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar(
                                    message = "Lỗi: Chưa tải được danh sách sân. Vui lòng thử lại.",
                                    duration = SnackbarDuration.Long
                                )
                            }
                            return@Button
                        }

                        val sortedCourts = availableCourts.sortedBy { it.id }

                        // XỬ LÝ TỪNG SÂN - Nhóm slots liên tục và tạo booking items
                        // Tạo nhiều bookingItems cho mỗi khoảng thời gian không liên tục
                        val allCourtBookings = mutableListOf<Triple<String, Pair<String, String>, String>>() // (courtId, startTime-endTime, courtName)

                        slotsByCourtNumber.forEach { (courtNumber, courtSlots) ->
                            val courtIndex = courtNumber - 1

                            if (courtIndex >= 0 && courtIndex < sortedCourts.size) {
                                val selectedCourt = sortedCourts[courtIndex]
                                val formattedCourtId = "${venue.id}_${selectedCourt.id}"
                                val courtName = "Sân số $courtNumber"

                                // Nhóm các slots liên tục thành các khoảng thời gian riêng biệt
                                val timeSlots = courtSlots.map { it.timeSlot }.sorted()
                                val consecutiveGroups = groupConsecutiveSlotsForBooking(timeSlots)

                                Log.d("BookingScreen", "✅ Court $courtNumber has ${consecutiveGroups.size} time ranges:")

                                consecutiveGroups.forEach { group ->
                                    val firstTimeSlot = group.first()
                                    val lastTimeSlot = group.last()

                                    val selectedDateFormatted = selectedDate.ifEmpty {
                                        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
                                    }

                                    val dateForApi = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                                        .parse(selectedDateFormatted)
                                    val calendar = Calendar.getInstance()
                                    calendar.time = dateForApi ?: Date()

                                    val firstTimeParts = firstTimeSlot.split(":")
                                    val firstHour = firstTimeParts[0].toInt()
                                    val firstMinute = firstTimeParts[1].toInt()

                                    calendar.set(Calendar.HOUR_OF_DAY, firstHour)
                                    calendar.set(Calendar.MINUTE, firstMinute)
                                    calendar.set(Calendar.SECOND, 0)

                                    val now = Calendar.getInstance()
                                    if (calendar.before(now)) {
                                        calendar.add(Calendar.DAY_OF_MONTH, 1)
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

                                    // Thêm bookingItem cho khoảng thời gian này
                                    allCourtBookings.add(Triple(formattedCourtId, Pair(startTime, endTime), courtName))

                                    Log.d("BookingScreen", "  • ${group.first()}-${calculateEndTime(group.last())} → $startTime to $endTime (${group.size} slots)")
                                }
                            }
                        }

                        if (allCourtBookings.isEmpty()) {
                            Log.e("BookingScreen", "❌ Cannot map any courts!")
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar(
                                    message = "Lỗi: Không tìm thấy sân. Vui lòng thử lại.",
                                    duration = SnackbarDuration.Long
                                )
                            }
                            return@Button
                        }

                        // Sử dụng booking đầu tiên để tạo BookingData
                        // (Backend sẽ tính giá chính xác cho TẤT CẢ slots)
                        val firstBooking = allCourtBookings.first()
                        val firstCourtId = firstBooking.first
                        val (startTime, endTime) = firstBooking.second

                        val selectedDateFormatted = selectedDate.ifEmpty {
                            SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
                        }

                        Log.d("BookingScreen", "📝 Final booking request:")
                        Log.d("BookingScreen", "  Court ID: $firstCourtId")
                        Log.d("BookingScreen", "  Start: $startTime")
                        Log.d("BookingScreen", "  End: $endTime")
                        Log.d("BookingScreen", "  Total slots: ${selectedSlots.size}")
                        Log.d("BookingScreen", "  Total courts: ${slotsByCourtNumber.size}")

                        // Tạo danh sách BookingItemData cho tất cả các sân
                        // Sử dụng courtNumber từ Triple thay vì parse từ courtId
                        val bookingItems = allCourtBookings.map { (courtId, times, courtName) ->
                            // Dùng courtNumber từ UI (đã lưu trong Triple) để lấy đúng slots
                            val courtSlots = slotsByCourtNumber[courtName.split(" ")[2].toInt()] ?: emptyList()
                            val courtPrice = (venue.pricePerHour * courtSlots.size * 0.5).toLong()

                            BookingItemData(
                                courtId = courtId,
                                courtName = courtName,
                                startTime = times.first,  // Thời gian bắt đầu của sân này
                                endTime = times.second,   // Thời gian kết thúc của sân này
                                price = courtPrice
                            )
                        }

                        Log.d("BookingScreen", "Created ${bookingItems.size} booking items:")
                        bookingItems.forEachIndexed { index, item ->
                            Log.d("BookingScreen", "  [$index] ${item.courtName} (${item.courtId})")
                            Log.d("BookingScreen", "       Time: ${item.startTime} - ${item.endTime}")
                            Log.d("BookingScreen", "       Price: ${item.price} VNĐ")
                        }

                        // Tạo BookingData với danh sách tất cả các sân
                        val bookingData = BookingData(
                            courtId = firstCourtId,
                            courtName = if (slotsByCourtNumber.size == 1) {
                                "${venue.name} - Sân ${slotsByCourtNumber.keys.first()}"
                            } else {
                                "${venue.name} - ${slotsByCourtNumber.size} sân"
                            },
                            courtAddress = venue.address.getFullAddress(),
                            selectedDate = selectedDateFormatted,
                            selectedSlots = selectedSlots,
                            playerName = playerName,
                            phoneNumber = phoneNumber,
                            pricePerHour = venue.pricePerHour,
                            // TÍNH GIÁ DỰ KIẾN cho TẤT CẢ slots (backend sẽ tính chính xác)
                            totalPrice = (venue.pricePerHour * selectedSlots.size * 0.5).toLong(),
                            ownerBankInfo = null,
                            expireTime = null,
                            startTime = startTime,
                            endTime = endTime,
                            bookingItems = bookingItems  // THÊM DANH SÁCH TẤT CẢ CÁC SÂN
                        )

                        Log.d("BookingScreen", "✅ Booking data prepared:")
                        Log.d("BookingScreen", "  Estimated total: ${bookingData.totalPrice} VNĐ")
                        Log.d("BookingScreen", "  Total booking items: ${bookingData.bookingItems?.size}")
                        Log.d("BookingScreen", "  (Backend will calculate exact price)")
                        Log.d("BookingScreen", "  Navigating to payment...")
                        Log.d("BookingScreen", "=".repeat(60))

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
                         courtsLoaded
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

/**
 * Nhóm các time slots liên tục thành các khoảng thời gian
 * Ví dụ: ["8:00", "8:30", "10:00", "10:30", "11:00"] -> ["8:00-9:00", "10:00-11:30"]
 */
private fun groupConsecutiveTimeSlots(timeSlots: List<String>): List<String> {
    if (timeSlots.isEmpty()) return emptyList()

    val sortedSlots = timeSlots.sorted()
    val result = mutableListOf<String>()

    var rangeStart = sortedSlots[0]
    var previousSlot = sortedSlots[0]

    for (i in 1 until sortedSlots.size) {
        val currentSlot = sortedSlots[i]

        // Kiểm tra xem currentSlot có liên tục với previousSlot không (cách nhau 30 phút)
        if (!isConsecutive(previousSlot, currentSlot)) {
            // Kết thúc range hiện tại
            val rangeEnd = calculateEndTime(previousSlot)
            result.add("$rangeStart-$rangeEnd")

            // Bắt đầu range mới
            rangeStart = currentSlot
        }

        previousSlot = currentSlot
    }

    // Thêm range cuối cùng
    val rangeEnd = calculateEndTime(previousSlot)
    result.add("$rangeStart-$rangeEnd")

    return result
}

/**
 * Nhóm các slots liên tục thành các khoảng thời gian cho việc đặt sân
 * Ví dụ: ["8:00", "8:30", "9:00", "10:00", "10:30"] -> [["8:00", "9:00"], ["10:00", "10:30"]]
 */
private fun groupConsecutiveSlotsForBooking(timeSlots: List<String>): List<List<String>> {
    if (timeSlots.isEmpty()) return emptyList()

    val sortedSlots = timeSlots.sorted()
    val result = mutableListOf<MutableList<String>>()

    var currentGroup = mutableListOf<String>()
    currentGroup.add(sortedSlots[0])

    for (i in 1 until sortedSlots.size) {
        val previousSlot = sortedSlots[i - 1]
        val currentSlot = sortedSlots[i]

        // Kiểm tra xem currentSlot có liên tục với previousSlot không (cách nhau 30 phút)
        if (isConsecutive(previousSlot, currentSlot)) {
            currentGroup.add(currentSlot)
        } else {
            // Kết thúc nhóm hiện tại và bắt đầu nhóm mới
            result.add(currentGroup)
            currentGroup = mutableListOf()
            currentGroup.add(currentSlot)
        }
    }

    // Thêm nhóm cuối cùng
    result.add(currentGroup)

    return result
}

/**
 * Kiểm tra xem 2 time slots có liên tục không (cách nhau 30 phút)
 */
private fun isConsecutive(slot1: String, slot2: String): Boolean {
    val parts1 = slot1.split(":")
    val parts2 = slot2.split(":")

    if (parts1.size != 2 || parts2.size != 2) return false

    val hour1 = parts1[0].toIntOrNull() ?: return false
    val minute1 = parts1[1].toIntOrNull() ?: return false
    val hour2 = parts2[0].toIntOrNull() ?: return false
    val minute2 = parts2[1].toIntOrNull() ?: return false

    val totalMinutes1 = hour1 * 60 + minute1
    val totalMinutes2 = hour2 * 60 + minute2

    return (totalMinutes2 - totalMinutes1) == 30
}
