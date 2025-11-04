package com.example.bookingcourt.presentation.court.screen

import android.util.Log
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.bookingcourt.domain.model.Venue
import com.example.bookingcourt.presentation.theme.Primary
import com.example.bookingcourt.presentation.venue.viewmodel.VenueDetailViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    venueId: String,
    onBackClick: () -> Unit,
    onBookClick: (Venue) -> Unit,
    viewModel: VenueDetailViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Mô tả", "Hình ảnh", "Đánh giá")

    // ✅ Gọi API GET /venues/{id} khi screen load
    LaunchedEffect(venueId) {
        viewModel.loadVenueDetail(venueId.toLongOrNull() ?: 0L)
    }

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

                    // Book Button - chỉ hiển thị khi đã load xong venue
                    if (state.venue != null) {
                        Button(
                            onClick = { state.venue?.let { onBookClick(it) } },
                            modifier = Modifier.height(40.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Primary)
                        ) {
                            Text("Đặt sân", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                // Loading state
                state.isLoading -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(color = Primary)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Đang tải thông tin sân...", color = Color.Gray)
                    }
                }

                // Error state
                state.error != null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "⚠️ ${state.error}",
                            color = Color.Red,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { viewModel.retry(venueId.toLongOrNull() ?: 0L) },
                            colors = ButtonDefaults.buttonColors(containerColor = Primary)
                        ) {
                            Text("Thử lại")
                        }
                    }
                }

                // Success state - hiển thị venue details
                state.venue != null -> {
                    DetailScreenContent(
                        venue = state.venue!!,
                        selectedTab = selectedTab,
                        tabs = tabs,
                        onTabSelected = { selectedTab = it }
                    )
                }
            }
        }
    }
}

@Composable
private fun DetailScreenContent(
    venue: Venue,
    selectedTab: Int,
    tabs: List<String>,
    onTabSelected: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Venue Info Section
        Column(modifier = Modifier.padding(16.dp)) {
            // Venue Name
            Text(
                text = venue.name,
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
                    text = "${venue.averageRating}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "(${venue.totalReviews} đánh giá)",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "${venue.pricePerHour / 1000}k/giờ",
                    fontSize = 16.sp,
                    color = Primary,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Số lượng sân
            InfoCard(
                icon = Icons.Default.Stadium,
                title = "Số lượng sân",
                value = "${venue.courtsCount} sân"
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Thời gian hoạt động
            if (venue.openingTime != null && venue.closingTime != null) {
                InfoCard(
                    icon = Icons.Default.Schedule,
                    title = "Thời gian hoạt động",
                    value = "${venue.openingTime} - ${venue.closingTime}"
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Số điện thoại
            val phoneNumber = venue.phoneNumber ?: venue.ownerPhone ?: "Liên hệ để biết thêm"
            InfoCard(
                icon = Icons.Default.Phone,
                title = "Số điện thoại",
                value = phoneNumber
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Địa chỉ chi tiết
            InfoCard(
                icon = Icons.Default.LocationOn,
                title = "Địa chỉ",
                value = venue.address.getFullAddress()
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Trạng thái
            val isOpen = isVenueOpen(venue.openingTime, venue.closingTime)
            val hasCourtAvailable = venue.courtsCount > 0

            val statusText = when {
                !hasCourtAvailable -> "Chưa có sân"
                !isOpen -> "Ngoài giờ hoạt động"
                else -> "Đang hoạt động"
            }

            val statusIcon = when {
                !hasCourtAvailable -> Icons.Default.Cancel
                !isOpen -> Icons.Default.Schedule
                else -> Icons.Default.CheckCircle
            }

            val statusColor = when {
                !hasCourtAvailable -> Color.Red
                !isOpen -> Color(0xFFFFA000) // Orange
                else -> Primary
            }

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
                        imageVector = statusIcon,
                        contentDescription = "Trạng thái",
                        tint = statusColor,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Trạng thái",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                        Text(
                            text = statusText,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.Black
                        )
                    }
                }
            }
        }

        HorizontalDivider(
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
                    onClick = { onTabSelected(index) }, // ✅ Sử dụng callback thay vì gán trực tiếp
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
            0 -> DescriptionTabContent(venue)
            1 -> ImagesTabContent(venue)
            2 -> ReviewsTabContent(venue)
        }

        Spacer(modifier = Modifier.height(16.dp))
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
fun DescriptionTabContent(venue: Venue) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = venue.description ?: "Chưa có mô tả",
            fontSize = 14.sp,
            color = Color.Black,
            lineHeight = 20.sp
        )

        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Thông tin:",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
        Spacer(modifier = Modifier.height(8.dp))

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
                text = "${venue.courtsCount} sân có sẵn",
                fontSize = 14.sp,
                color = Color.Black
            )
        }

        if (venue.phoneNumber != null || venue.ownerPhone != null) {
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
                    text = "Có thể liên hệ trực tiếp",
                    fontSize = 14.sp,
                    color = Color.Black
                )
            }
        }

        if (venue.email != null) {
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
                    text = "Email: ${venue.email}",
                    fontSize = 14.sp,
                    color = Color.Black
                )
            }
        }
    }
}

@Composable
fun ImagesTabContent(venue: Venue) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        if (venue.images.isNullOrEmpty()) {
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
            venue.images.forEach { imageUrl ->
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
fun ReviewsTabContent(venue: Venue) {
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
                    text = "${venue.averageRating}",
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    repeat(5) { index ->
                        Icon(
                            imageVector = if (index < venue.averageRating.toInt()) Icons.Default.Star else Icons.Default.StarOutline,
                            contentDescription = null,
                            tint = Color(0xFFFFA000),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${venue.totalReviews} đánh giá",
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
                            progress = { if (venue.totalReviews > 0) 0.1f else 0f },
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
        HorizontalDivider(color = Color.Gray.copy(alpha = 0.2f))
        Spacer(modifier = Modifier.height(24.dp))

        if (venue.totalReviews == 0) {
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

// Helper function to check if venue is currently open
private fun isVenueOpen(openingTime: String?, closingTime: String?): Boolean {
    if (openingTime == null || closingTime == null) return true // Assume open if no time specified

    return try {
        val currentTime = Calendar.getInstance()
        val currentHour = currentTime.get(Calendar.HOUR_OF_DAY)
        val currentMinute = currentTime.get(Calendar.MINUTE)

        val openParts = openingTime.split(":")
        val closeParts = closingTime.split(":")

        val openHour = openParts[0].toInt()
        val openMinute = if (openParts.size > 1) openParts[1].toInt() else 0

        val closeHour = closeParts[0].toInt()
        val closeMinute = if (closeParts.size > 1) closeParts[1].toInt() else 0

        val currentMinutes = currentHour * 60 + currentMinute
        val openMinutes = openHour * 60 + openMinute
        val closeMinutes = closeHour * 60 + closeMinute

        // Handle case where closing time is after midnight
        if (closeMinutes < openMinutes) {
            currentMinutes >= openMinutes || currentMinutes < closeMinutes
        } else {
            currentMinutes in openMinutes..closeMinutes
        }
    } catch (_: Exception) {
        true // If parsing fails, assume open
    }
}

// Preview Provider for Venue
class SampleVenueProvider : PreviewParameterProvider<Venue> {
    override val values = sequenceOf(
        Venue(
            id = 1L,
            name = "Sân cầu lông Thống Nhất",
            description = "Sân cầu lông chất lượng cao với đầy đủ tiện nghi, không gian thoáng mát, giá cả phải chăng.",
            numberOfCourt = 5,
            address = com.example.bookingcourt.domain.model.Address(
                id = 1L,
                provinceOrCity = "TP.HCM",
                district = "Quận 7",
                detailAddress = "123 Nguyễn Văn Linh"
            ),
            courtsCount = 5,
            pricePerHour = 150000,
            averageRating = 4.5f,
            totalReviews = 120,
            openingTime = "07:00",
            closingTime = "22:00",
            phoneNumber = "0123456789",
            email = "contact@thongnhat.com",
            images = listOf("https://example.com/court1.jpg")
        )
    )
}

@Preview(showBackground = true)
@Composable
fun DetailScreenPreview() {
    // Preview không thể gọi API thực, nên để venueId = "1" cho demo
    DetailScreen(
        venueId = "1",
        onBackClick = {},
        onBookClick = {}
    )
}
