package com.example.bookingcourt.presentation.court.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bookingcourt.domain.model.Court
import com.example.bookingcourt.domain.model.SportType
import com.example.bookingcourt.domain.model.CourtType
import com.example.bookingcourt.domain.model.Amenity
import com.example.bookingcourt.presentation.theme.Primary
import kotlinx.datetime.LocalTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    court: Court,
    onBackClick: () -> Unit,
    onBookClick: (Court) -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Mô tả", "Hình ảnh", "Đánh giá")

    Scaffold(
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(Primary.copy(alpha = 0.3f))
            ) {
                // Action buttons overlay
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 56.dp, start = 20.dp, end = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color.White, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.Black
                        )
                    }

                    // Book Button in top right corner
                    Button(
                        onClick = { onBookClick(court) },
                        modifier = Modifier.height(40.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Primary)
                    ) {
                        Text("Đặt sân", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // Court Info Section
            Column(modifier = Modifier.padding(16.dp)) {
                // Court Name
                Text(
                    text = court.name,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Rating và giá
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = Color(0xFFFFA000),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${court.rating}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "(${court.totalReviews} đánh giá)",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = "${court.pricePerHour / 1000}k/giờ",
                        fontSize = 16.sp,
                        color = Primary,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Số lượng sân - lấy từ court.courtsCount
                InfoCard(
                    icon = Icons.Default.Stadium,
                    title = "Số lượng sân",
                    value = "${court.courtsCount} sân"
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Thời gian hoạt động
                InfoCard(
                    icon = Icons.Default.Schedule,
                    title = "Thời gian hoạt động",
                    value = "${court.openTime} - ${court.closeTime}"
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Số điện thoại
                InfoCard(
                    icon = Icons.Default.Phone,
                    title = "Số điện thoại",
                    value = "Liên hệ để biết thêm"
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Địa chỉ chi tiết
                InfoCard(
                    icon = Icons.Default.LocationOn,
                    title = "Địa chỉ",
                    value = court.address
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Trạng thái - kiểm tra theo thời gian thực, cập nhật mỗi phút
                var currentTime by remember {
                    mutableStateOf(
                        LocalTime(
                            java.time.LocalTime.now().hour,
                            java.time.LocalTime.now().minute
                        )
                    )
                }

                // Cập nhật currentTime mỗi 60 giây
                LaunchedEffect(Unit) {
                    while (true) {
                        kotlinx.coroutines.delay(60000) // 60 giây
                        val now = java.time.LocalTime.now()
                        currentTime = LocalTime(now.hour, now.minute)
                    }
                }

                val isOpenNow = currentTime >= court.openTime && currentTime < court.closeTime
                val statusText = when {
                    !court.isActive -> "Tạm đóng cửa"
                    court.courtsCount == 0 -> "Đã đóng cửa" // Không có sân nào
                    isOpenNow -> "Đang mở cửa"
                    else -> "Đã đóng cửa"
                }
                val statusIcon = if (court.isActive && isOpenNow && court.courtsCount > 0)
                    Icons.Default.CheckCircle
                else
                    Icons.Default.Cancel

                InfoCard(
                    icon = statusIcon,
                    title = "Trạng thái",
                    value = statusText
                )
            }

            Divider(
                modifier = Modifier.padding(horizontal = 16.dp),
                color = Color.Gray.copy(alpha = 0.2f)
            )

            // Tab Navigation - Full width evenly distributed
            TabRow(
                selectedTabIndex = selectedTab,
                modifier = Modifier.fillMaxWidth(),
                containerColor = Color.White,
                contentColor = Primary
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                text = title,
                                fontSize = 14.sp,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }
            }

            // Tab Content
            when (selectedTab) {
                0 -> DescriptionTabContent(court)
                1 -> ImagesTabContent(court)
                2 -> ReviewsTabContent(court)
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun InfoCard(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, value: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = Primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 12.sp,
                    color = Color.Gray
                )
                Text(
                    text = value,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black
                )
            }
        }
    }
}

@Composable
fun DescriptionTabContent(court: Court) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = court.description,
            fontSize = 14.sp,
            color = Color.Black,
            lineHeight = 20.sp
        )

        if (court.rules != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Quy định:",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = court.rules,
                fontSize = 14.sp,
                color = Color.Gray,
                lineHeight = 20.sp
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Tiện ích:",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
        Spacer(modifier = Modifier.height(8.dp))

        if (court.amenities.isEmpty()) {
            Text(
                text = "Chưa có thông tin tiện ích",
                fontSize = 14.sp,
                color = Color.Gray,
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
            )
        } else {
            court.amenities.forEach { amenity ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = amenity.name,
                        fontSize = 14.sp,
                        color = Color.Black
                    )
                }
            }
        }
    }
}

@Composable
fun ImagesTabContent(court: Court) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        if (court.images.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(
                        Primary.copy(alpha = 0.1f),
                        androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Image,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Chưa có hình ảnh",
                        fontSize = 16.sp,
                        color = Color.Gray
                    )
                }
            }
        } else {
            // TODO: Implement image gallery with Coil
            court.images.forEach { imageUrl ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .padding(vertical = 8.dp)
                        .background(
                            Primary.copy(alpha = 0.2f),
                            androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
                        )
                ) {
                    // AsyncImage(model = imageUrl, ...)
                }
            }
        }
    }
}

@Composable
fun ReviewsTabContent(court: Court) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // Rating Summary
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(end = 24.dp)
            ) {
                Text(
                    text = "${court.rating}",
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    repeat(5) { index ->
                        Icon(
                            imageVector = if (index < court.rating.toInt()) Icons.Default.Star else Icons.Default.StarOutline,
                            contentDescription = null,
                            tint = Color(0xFFFFA000),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${court.totalReviews} đánh giá",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                repeat(5) { index ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 3.dp)
                    ) {
                        Text(
                            text = "${5 - index}",
                            fontSize = 12.sp,
                            color = Color.Gray,
                            modifier = Modifier.width(20.dp)
                        )
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = Color(0xFFFFA000),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        LinearProgressIndicator(
                            progress = if (court.totalReviews > 0) 0.1f else 0f,
                            modifier = Modifier
                                .weight(1f)
                                .height(8.dp)
                                .clip(androidx.compose.foundation.shape.RoundedCornerShape(4.dp)),
                            color = Color(0xFFFFA000),
                            trackColor = Color.Gray.copy(alpha = 0.2f)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Divider(color = Color.Gray.copy(alpha = 0.2f))
        Spacer(modifier = Modifier.height(24.dp))

        if (court.totalReviews == 0) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.RateReview,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Chưa có đánh giá nào",
                        fontSize = 16.sp,
                        color = Color.Gray
                    )
                }
            }
        } else {
            // TODO: Implement actual reviews list when API is ready
            Text(
                text = "Danh sách đánh giá chi tiết sẽ được cập nhật sau",
                fontSize = 14.sp,
                color = Color.Gray,
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
            )
        }
    }
}

// Preview Provider for Court
class SampleCourtProvider : PreviewParameterProvider<Court> {
    override val values = sequenceOf(
        Court(
            id = "1",
            name = "Sân cầu lông Thống Nhất",
            description = "Sân cầu lông chất lượng cao với đầy đủ tiện nghi, không gian thoáng mát, giá cả phải chăng.",
            address = "123 Nguyễn Văn Linh, Quận 7, TP.HCM",
            latitude = 10.7553411,
            longitude = 106.7423709,
            images = listOf("https://example.com/court1.jpg"),
            sportType = SportType.BADMINTON,
            courtType = CourtType.INDOOR,
            pricePerHour = 150000,
            openTime = LocalTime(7, 0),
            closeTime = LocalTime(22, 0),
            amenities = listOf(
                Amenity(id = "1", name = "Bãi đỗ xe", icon = "parking"),
                Amenity(id = "2", name = "Phòng tắm", icon = "shower")
            ),
            rules = "Vui lòng mang giày đúng quy định",
            ownerId = "owner1",
            rating = 4.5f,
            totalReviews = 120,
            isActive = true,
            maxPlayers = 4
        )
    )
}

@Preview(showBackground = true)
@Composable
fun DetailScreenPreview(
    @PreviewParameter(SampleCourtProvider::class) court: Court
) {
    DetailScreen(
        court = court,
        onBackClick = {},
        onBookClick = {}
    )
}
