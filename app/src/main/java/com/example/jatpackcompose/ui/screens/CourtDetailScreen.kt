package com.example.jatpackcompose.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import com.example.jatpackcompose.ui.theme.JatpackComposeTheme
import java.text.NumberFormat
import java.util.*

// Data classes
data class CheckInSchedule(
    val courtNumber: Int,
    val customerName: String,
    val time: String,
    val duration: String
)

data class CourtStatus(
    val courtNumber: Int,
    val isAvailable: Boolean,
    val currentBooking: String? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseDetailScreen(
    venueName: String = "Star Club Badminton",
    courtCount: Int = 8,
    onBackClick: () -> Unit = {}
) {
    // Sample data
    val todayRevenue = 2500000
    val checkInSchedules = remember {
        listOf(
            CheckInSchedule(1, "Nguyễn Văn A", "14:00", "1 giờ"),
            CheckInSchedule(3, "Trần Thị B", "14:30", "2 giờ"),
            CheckInSchedule(5, "Lê Văn C", "15:00", "1.5 giờ"),
            CheckInSchedule(2, "Phạm Thị D", "15:30", "1 giờ")
        )
    }

    val courtStatuses = remember {
        (1..courtCount).map { courtNum ->
            CourtStatus(
                courtNumber = courtNum,
                isAvailable = courtNum % 3 != 0,
                currentBooking = if (courtNum % 3 == 0) "Đang sử dụng" else null
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Logo sân
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .background(
                                    color = Color.White.copy(alpha = 0.2f),
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "Logo sân",
                                tint = Color.White,
                                modifier = Modifier.size(30.dp)
                            )
                        }

                        // Thông tin sân
                        Column(
                            modifier = Modifier.padding(top = 10.dp)
                        ) {
                            Text(
                                text = venueName,
                                fontSize = 21.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "$courtCount sân",
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.padding(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Quay lại",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { /* TODO: Settings */ },
                        modifier = Modifier.padding(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Cài đặt",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF123E62)
                ),
                modifier = Modifier.height(80.dp)
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F5F5))
                .padding(paddingValues)
        ) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Dashboard Overview Cards
            item {
                Text(
                    text = "Tổng Quan",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            // Revenue Card
            item {
                DashboardCard(
                    title = "Doanh Thu Hôm Nay",
                    value = NumberFormat.getInstance(Locale("vi", "VN")).format(todayRevenue) + " đ",
                    icon = Icons.Default.ShoppingCart,
                    backgroundColor = Color(0xFF4CAF50),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            // Upcoming Check-ins
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Lịch Check-in Sắp Tới",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    ),
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = 4.dp
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        if (checkInSchedules.isEmpty()) {
                            Text(
                                text = "Không có lịch check-in",
                                fontSize = 14.sp,
                                color = Color.Gray,
                                modifier = Modifier.padding(16.dp)
                            )
                        } else {
                            checkInSchedules.take(4).forEach { schedule ->
                                CheckInItem(schedule = schedule)
                                if (schedule != checkInSchedules.last()) {
                                    Divider(
                                        modifier = Modifier.padding(vertical = 8.dp),
                                        color = Color.LightGray.copy(alpha = 0.5f)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Court Status
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Trạng Thái Sân",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    ),
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = 4.dp
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        // Summary
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            StatusSummaryItem(
                                count = courtStatuses.count { it.isAvailable },
                                label = "Sân trống",
                                color = Color(0xFF4CAF50)
                            )
                            StatusSummaryItem(
                                count = courtStatuses.count { !it.isAvailable },
                                label = "Đang sử dụng",
                                color = Color(0xFFFF9800)
                            )
                        }

                        Divider(
                            modifier = Modifier.padding(vertical = 12.dp),
                            color = Color.LightGray.copy(alpha = 0.5f)
                        )

                        // Court Grid
                        CourtStatusGrid(courtStatuses = courtStatuses)
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

@Composable
fun DashboardCard(
    title: String,
    value: String,
    icon: ImageVector,
    backgroundColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.9f),
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = value,
                    fontSize = 24.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }

            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(
                        color = Color.White.copy(alpha = 0.2f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}

@Composable
fun CheckInItem(schedule: CheckInSchedule) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        color = Color(0xFF123E62).copy(alpha = 0.1f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${schedule.courtNumber}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF123E62)
                )
            }

            Column {
                Text(
                    text = schedule.customerName,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black
                )
                Text(
                    text = "${schedule.time} • ${schedule.duration}",
                    fontSize = 13.sp,
                    color = Color.Gray
                )
            }
        }

        Icon(
            imageVector = Icons.Default.DateRange,
            contentDescription = null,
            tint = Color(0xFF123E62),
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
fun StatusSummaryItem(
    count: Int,
    label: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "$count",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            fontSize = 13.sp,
            color = Color.Gray
        )
    }
}

@Composable
fun CourtStatusGrid(courtStatuses: List<CourtStatus>) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        courtStatuses.chunked(4).forEach { rowCourts ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rowCourts.forEach { court ->
                    CourtStatusItem(
                        courtStatus = court,
                        modifier = Modifier.weight(1f)
                    )
                }
                // Fill empty spaces if row is not complete
                repeat(4 - rowCourts.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
fun CourtStatusItem(
    courtStatus: CourtStatus,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (courtStatus.isAvailable)
                Color(0xFF4CAF50).copy(alpha = 0.1f)
            else
                Color(0xFFFF9800).copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Sân ${courtStatus.courtNumber}",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = if (courtStatus.isAvailable)
                    Color(0xFF4CAF50)
                else
                    Color(0xFFFF9800)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = if (courtStatus.isAvailable) "Trống" else "Đang dùng",
                fontSize = 11.sp,
                color = Color.Gray
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CourseDetailScreenPreview() {
    JatpackComposeTheme {
        CourseDetailScreen()
    }
}
