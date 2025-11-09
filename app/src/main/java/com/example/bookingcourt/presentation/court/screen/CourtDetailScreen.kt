package com.example.bookingcourt.presentation.court.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bookingcourt.presentation.theme.BookingCourtTheme
import java.text.NumberFormat
import java.util.*
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.text.style.TextAlign
import com.example.bookingcourt.domain.model.CourtTimeSlot
import java.text.SimpleDateFormat
import android.util.Log
import kotlinx.coroutines.launch

data class CheckInSchedule(
    val bookingId: String,
    val courtNumber: Int,
    val customerName: String,
    val time: String,
    val duration: String,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourtDetailScreen(
    courtId: String = "",
    venueName: String = "Star Club Badminton",
    courtCount: Int = 8,
    onNavigateBack: () -> Unit = {},
    onNavigateToBooking: () -> Unit = {},
    onNavigateToBookingDetail: (String) -> Unit = {},
    viewModel: com.example.bookingcourt.presentation.court.viewmodel.CourtDetailViewModel = androidx.hilt.navigation.compose.hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()

    val todayRevenue = state.todayRevenue
    val venue = state.venue
    val actualVenueName = venue?.name ?: venueName
    val actualCourtCount = venue?.courtsCount ?: courtCount

    // State cho booking grid
    var selectedDate by remember { mutableStateOf("") }
    var selectedSlots by remember { mutableStateOf(setOf<CourtTimeSlot>()) }
    var showDatePicker by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = System.currentTimeMillis()
    )

    // Fetch booked slots và courts availability khi selectedDate thay đổi
    LaunchedEffect(selectedDate, venue?.id) {
        Log.d("CourtDetailScreen", "🔄 LaunchedEffect triggered - venue: ${venue?.id}, selectedDate: $selectedDate")

        if (venue != null) {
            val currentDate = if (selectedDate.isNotEmpty()) {
                selectedDate
            } else {
                SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
            }

            // Convert date from dd/MM/yyyy to yyyy-MM-dd for API
            val parts = currentDate.split("/")
            if (parts.size == 3) {
                val apiDate = "${parts[2]}-${parts[1].padStart(2, '0')}-${parts[0].padStart(2, '0')}"
                Log.d("CourtDetailScreen", "🔍 Fetching booked slots for venue ${venue.id} on $apiDate")
                viewModel.getBookedSlots(venue.id, apiDate)

                // Fetch courts availability cho cả ngày
                Log.d("CourtDetailScreen", "🔍 Fetching courts availability for venue ${venue.id} on $apiDate")
                viewModel.getCourtsAvailabilityForWholeDay(venue.id, apiDate)

                // Cập nhật doanh thu theo ngày được chọn
                viewModel.updateSelectedDateRevenue(currentDate)
            } else {
                Log.e("CourtDetailScreen", "❌ Failed to parse date: $currentDate")
            }
        } else {
            Log.w("CourtDetailScreen", "⚠️ Venue is null, cannot fetch courts availability")
        }
    }

    // Parse opening and closing time from venue
    val openingTime = remember(venue?.openingTime) {
        venue?.openingTime?.split(":")?.let { parts ->
            if (parts.size >= 2) Pair(parts[0].toIntOrNull() ?: 6, parts[1].toIntOrNull() ?: 0)
            else Pair(6, 0)
        } ?: Pair(6, 0)
    }

    val closingTime = remember(venue?.closingTime) {
        venue?.closingTime?.split(":")?.let { parts ->
            if (parts.size >= 2) Pair(parts[0].toIntOrNull() ?: 22, parts[1].toIntOrNull() ?: 0)
            else Pair(22, 0)
        } ?: Pair(22, 0)
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
            closeHour = 23
            closeMinute = 59
        }

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

    val checkInSchedules = remember {
        listOf(
            CheckInSchedule("booking_1", 1, "Nguyễn Văn A", "14:00", "1 giờ"),
            CheckInSchedule("booking_2", 3, "Trần Thị B", "14:30", "2 giờ"),
            CheckInSchedule("booking_3", 5, "Lê Văn C", "15:00", "1.5 giờ"),
            CheckInSchedule("booking_4", 2, "Phạm Thị D", "15:30", "1 giờ"),
        )
    }

    val currencyFormat = remember { NumberFormat.getCurrencyInstance(Locale("vi", "VN")) }

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
                title = { Text(actualVenueName) },
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            } else if (state.error != null) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = state.error ?: "Đã xảy ra lỗi",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { viewModel.handleIntent(com.example.bookingcourt.presentation.court.viewmodel.CourtDetailIntent.Refresh) }) {
                        Text("Thử lại")
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFFF5F5F5)),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
            // Date Selection
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
                            "Chọn ngày xem tình trạng sân",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                        )
                        Spacer(modifier = Modifier.height(12.dp))

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
                    }
                }
            }

            // Revenue Card
            item {
                val displayDate = selectedDate.ifEmpty { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date()) }
                val revenueAmount = state.selectedDateRevenue

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primary,
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
                                "Doanh thu ngày $displayDate",
                                color = Color.White.copy(alpha = 0.9f),
                                fontSize = 14.sp,
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                currencyFormat.format(revenueAmount),
                                color = Color.White,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                        Icon(
                            Icons.Default.DateRange,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(48.dp),
                        )
                    }
                }
            }

            // Booking Grid Table
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
                            "Tình trạng sân và giờ",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        // Grid Table
                        val courtsToDisplay = if (state.courtsAvailability.isNotEmpty()) {
                            state.courtsAvailability
                        } else {
                            emptyList()
                        }

                        if (courtsToDisplay.isEmpty()) {
                            // Hiển thị loading hoặc empty state
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Đang tải thông tin sân...",
                                    fontSize = 14.sp,
                                    color = Color.Gray
                                )
                            }
                        } else {
                            Row(
                                modifier = Modifier.padding(8.dp)
                            ) {
                                // Fixed Column - Tên sân
                                Column {
                                    // Header cell
                                    Box(
                                        modifier = Modifier
                                            .width(90.dp)
                                            .height(50.dp)
                                            .border(1.dp, Color.Gray)
                                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
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

                                    // Court rows - Sử dụng tên sân từ API
                                    courtsToDisplay.forEach { court ->
                                        Box(
                                            modifier = Modifier
                                                .width(90.dp)
                                                .height(45.dp)
                                                .border(1.dp, Color.Gray)
                                                .background(Color(0xFFF5F5F5)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = court.courtName,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Medium,
                                                textAlign = TextAlign.Center,
                                                color = Color.Black,
                                                maxLines = 2
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
                                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
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

                                    // Data Rows - Sử dụng courtsAvailability
                                    courtsToDisplay.forEach { court ->
                                        Row {
                                            timeSlots.forEach { time ->
                                                val slotStartTime = timeSlotToStartTime(time)
                                                val slotEndTime = timeSlotToEndTime(time)

                                                // Kiểm tra slot có bị overlap bởi bất kỳ booking nào không
                                                val isBooked = court.bookedSlots.any { bookedSlot ->
                                                    timeRangesOverlap(
                                                        slotStartTime, slotEndTime,
                                                        bookedSlot.startTime, bookedSlot.endTime
                                                    )
                                                }

                                                Box(
                                                    modifier = Modifier
                                                        .width(80.dp)
                                                        .height(45.dp)
                                                        .border(1.dp, Color.Gray)
                                                        .background(
                                                            if (isBooked) Color(0xFFFFCDD2) else Color.White
                                                        ),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    if (isBooked) {
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
                    }
                }
            }

            // Check-in Schedule
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
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                "Lịch check-in sắp tới",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                            )
                            TextButton(onClick = { /* TODO */ }) {
                                Text("Xem tất cả")
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        checkInSchedules.forEach { schedule ->
                            CheckInItem(
                                schedule = schedule,
                                onCustomerClick = {
                                    onNavigateToBookingDetail(schedule.bookingId)
                                },
                            )
                            if (schedule != checkInSchedules.last()) {
                                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                            }
                        }
                    }
                }
            }
                }
            }
        }
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

// Helper function để check xem 2 time ranges có overlap không
// Format: "HH:mm:ss"
private fun timeRangesOverlap(
    start1: String,
    end1: String,
    start2: String,
    end2: String
): Boolean {
    // Convert time strings to minutes for easier comparison
    fun timeToMinutes(time: String): Int {
        val parts = time.split(":")
        if (parts.size < 2) return 0
        val hour = parts[0].toIntOrNull() ?: 0
        val minute = parts[1].toIntOrNull() ?: 0
        return hour * 60 + minute
    }

    val start1Minutes = timeToMinutes(start1)
    val end1Minutes = timeToMinutes(end1)
    val start2Minutes = timeToMinutes(start2)
    val end2Minutes = timeToMinutes(end2)

    // Two ranges overlap if: start1 < end2 AND start2 < end1
    return start1Minutes < end2Minutes && start2Minutes < end1Minutes
}

@Composable
fun CheckInItem(
    schedule: CheckInSchedule,
    onCustomerClick: () -> Unit = {},
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .weight(1f)
                .clickable { onCustomerClick() },
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        shape = CircleShape,
                    ),
            ) {
                Text(
                    text = schedule.courtNumber.toString(),
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = schedule.customerName,
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp,
                )
                Text(
                    text = "${schedule.time} - ${schedule.duration}",
                    fontSize = 14.sp,
                    color = Color.Gray,
                )
            }
        }

        IconButton(onClick = { /* TODO */ }) {
            Icon(
                Icons.Default.CheckCircle,
                contentDescription = "Check-in",
                tint = Color(0xFF4CAF50),
            )
        }
    }
}

@Composable
fun QuickActionButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .padding(8.dp),
    ) {
        FilledIconButton(
            onClick = onClick,
            modifier = Modifier.size(56.dp),
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
            ),
        ) {
            Icon(
                icon,
                contentDescription = label,
                tint = MaterialTheme.colorScheme.primary,
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color.Gray,
        )
    }
}


@Preview(showBackground = true)
@Composable
fun CourseDetailScreenPreview() {
    BookingCourtTheme {
        CourtDetailScreen()
    }
}
