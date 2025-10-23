package com.example.bookingcourt.presentation.court.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.bookingcourt.presentation.theme.BookingCourtTheme
import java.text.NumberFormat
import java.util.*

data class CheckInSchedule(
    val bookingId: String,
    val courtNumber: Int,
    val customerName: String,
    val time: String,
    val duration: String,
)

data class CourtStatus(
    val courtNumber: Int,
    val isAvailable: Boolean,
    val currentBooking: String? = null,
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
) {
    val todayRevenue = 2500000
    val checkInSchedules = remember {
        listOf(
            CheckInSchedule("booking_1", 1, "Nguyễn Văn A", "14:00", "1 giờ"),
            CheckInSchedule("booking_2", 3, "Trần Thị B", "14:30", "2 giờ"),
            CheckInSchedule("booking_3", 5, "Lê Văn C", "15:00", "1.5 giờ"),
            CheckInSchedule("booking_4", 2, "Phạm Thị D", "15:30", "1 giờ"),
        )
    }

    val courtStatuses = remember {
        (1..courtCount).map { courtNum ->
            CourtStatus(
                courtNumber = courtNum,
                isAvailable = courtNum % 3 != 0,
                currentBooking = if (courtNum % 3 == 0) "Đã đặt: 14:00 - 16:00" else null,
            )
        }
    }

    val courtImages = remember {
        listOf(
            "https://images.unsplash.com/photo-1626224583764-f87db24ac4ea?w=400",
            "https://images.unsplash.com/photo-1554068865-24cecd4e34b8?w=400",
            "https://images.unsplash.com/photo-1609710228159-0fa9bd7c0827?w=400",
            "https://images.unsplash.com/photo-1622163642998-1ea32b0bbc67?w=400",
        )
    }

    var selectedImage by remember { mutableStateOf<String?>(null) }

    val currencyFormat = remember { NumberFormat.getCurrencyInstance(Locale("vi", "VN")) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(venueName) },
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
            // Revenue Card
            item {
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
                                "Doanh thu hôm nay",
                                color = Color.White.copy(alpha = 0.9f),
                                fontSize = 14.sp,
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                currencyFormat.format(todayRevenue),
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

            // Court Images Card
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
                            "Hình ảnh sân",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            modifier = Modifier.heightIn(max = 500.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            items(courtImages) { imageUrl ->
                                AsyncImage(
                                    model = imageUrl,
                                    contentDescription = "Hình ảnh sân",
                                    modifier = Modifier
                                        .aspectRatio(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { selectedImage = imageUrl }
                                        .background(Color.LightGray),
                                    contentScale = ContentScale.Crop,
                                )
                            }
                        }
                    }
                }
            }

            // Court Status Grid
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
                            "Tình trạng sân",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        // Court grid
                        val chunkedCourts = courtStatuses.chunked(4)
                        chunkedCourts.forEach { row ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                            ) {
                                row.forEach { court ->
                                    CourtStatusItem(court)
                                }
                                // Fill empty spaces
                                repeat(4 - row.size) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
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

        // Image Preview Dialog
        selectedImage?.let { imageUrl ->
            ImagePreviewDialog(
                imageUrl = imageUrl,
                onDismiss = { selectedImage = null },
            )
        }
    }
}

@Composable
fun CourtStatusItem(court: CourtStatus) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(4.dp),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(60.dp)
                .background(
                    color = if (court.isAvailable) Color(0xFF4CAF50) else Color(0xFFFF5252),
                    shape = RoundedCornerShape(8.dp),
                ),
        ) {
            Text(
                text = "Sân ${court.courtNumber}",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = if (court.isAvailable) "Trống" else "Đã đặt",
            fontSize = 12.sp,
            color = if (court.isAvailable) Color(0xFF4CAF50) else Color(0xFFFF5252),
        )
    }
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

@Composable
fun ImagePreviewDialog(
    imageUrl: String,
    onDismiss: () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
        ),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.9f))
                .clickable { onDismiss() },
            contentAlignment = Alignment.Center,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = "Preview hình ảnh",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Fit,
                )

                Spacer(modifier = Modifier.height(16.dp))

                IconButton(
                    onClick = onDismiss,
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = Color.White.copy(alpha = 0.2f),
                    ),
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Đóng",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp),
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CourseDetailScreenPreview() {
    BookingCourtTheme {
        CourtDetailScreen()
    }
}
