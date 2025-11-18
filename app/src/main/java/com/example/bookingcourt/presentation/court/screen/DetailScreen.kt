package com.example.bookingcourt.presentation.court.screen

import android.content.Intent
import android.net.Uri
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.bookingcourt.domain.model.Venue
import com.example.bookingcourt.presentation.theme.Primary
import com.example.bookingcourt.presentation.review.viewmodel.ReviewViewModel
import com.example.bookingcourt.presentation.review.components.ReviewCard
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    venue: Venue,
    onBackClick: () -> Unit,
    onBookClick: (Venue) -> Unit,
    reviewViewModel: ReviewViewModel = hiltViewModel() // Thêm ReviewViewModel
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Mô tả", "Hình ảnh", "Đánh giá")

    // Load reviews khi màn hình được mở
    val reviewsState by reviewViewModel.venueReviewsState.collectAsState()

    LaunchedEffect(venue.id) {
        reviewViewModel.loadVenueReviews(venue.id)
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

                    // Book Button
                    Button(
                        onClick = { onBookClick(venue) },
                        modifier = Modifier.height(40.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Primary)
                    ) {
                        Text("Đặt sân", fontSize = 14.sp, fontWeight = FontWeight.Bold)
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
            DetailScreenContent(
                venue = venue,
                selectedTab = selectedTab,
                tabs = tabs,
                onTabSelected = { selectedTab = it },
                reviewsState = reviewsState // Truyền reviewsState
            )
        }
    }
}

@Composable
private fun DetailScreenContent(
    venue: Venue,
    selectedTab: Int,
    tabs: List<String>,
    onTabSelected: (Int) -> Unit,
    reviewsState: com.example.bookingcourt.presentation.review.viewmodel.VenueReviewsState
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

            // Số điện thoại chủ sân
            if (!venue.ownerPhone.isNullOrBlank()) {
                val context = LocalContext.current
                InfoCardClickable(
                    icon = Icons.Default.ContactPhone,
                    title = "Số điện thoại chủ sân",
                    value = venue.ownerPhone,
                    onClick = {
                        val intent = Intent(Intent.ACTION_DIAL).apply {
                            data = Uri.parse("tel:${venue.ownerPhone}")
                        }
                        context.startActivity(intent)
                    }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

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
                    onClick = { onTabSelected(index) }, // Sử dụng callback thay vì gán trực tiếp
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
            2 -> ReviewsTabContent(venue, reviewsState) // Truyền reviewsState vào ReviewsTabContent
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
fun InfoCardClickable(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    value: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        onClick = onClick
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
            Icon(
                imageVector = Icons.Default.Phone,
                contentDescription = "Call",
                tint = Primary,
                modifier = Modifier.size(20.dp)
            )
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

        if (venue.ownerPhone != null) {
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
            // Display venue images with Coil
            venue.images.forEachIndexed { index, imageUrl ->
                // Build full image URL if needed
                val fullImageUrl = if (imageUrl.startsWith("http")) {
                    imageUrl
                } else if (imageUrl.startsWith("/api/")) {
                    // API trả về path đầy đủ như /api/files/venue-images/...
                    val baseUrl = com.example.bookingcourt.core.utils.Constants.BASE_URL
                        .removeSuffix("/api/")
                        .removeSuffix("/")
                    "$baseUrl$imageUrl"
                } else {
                    // Chỉ có filename, build full URL
                    val baseUrl = com.example.bookingcourt.core.utils.Constants.BASE_URL
                        .removeSuffix("/api/")
                        .removeSuffix("/")
                    "$baseUrl/files/venue-images/$imageUrl"
                }

                android.util.Log.d("DetailScreen", "Image $index: $imageUrl -> $fullImageUrl")

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    AsyncImage(
                        model = fullImageUrl,
                        contentDescription = "Ảnh sân ${index + 1}",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(250.dp),
                        contentScale = ContentScale.Crop,
                        placeholder = painterResource(
                            android.R.drawable.ic_menu_gallery
                        ),
                        error = painterResource(
                            android.R.drawable.ic_menu_report_image
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun ReviewsTabContent(
    venue: Venue,
    reviewsState: com.example.bookingcourt.presentation.review.viewmodel.VenueReviewsState
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // Sử dụng dữ liệu từ reviewsState thay vì venue
        val averageRating = reviewsState.averageRating
        val totalReviews = reviewsState.totalReviews
        val reviews = reviewsState.reviews

        // Tính toán phân bố rating từ danh sách reviews thực tế
        val ratingDistribution = if (reviews.isNotEmpty()) {
            reviews.groupBy { it.rating }
                .mapValues { (_, list) -> list.size.toFloat() / totalReviews }
        } else {
            emptyMap()
        }

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
                    text = String.format("%.1f", averageRating),
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    repeat(5) { index ->
                        Icon(
                            imageVector = if (index < averageRating.toInt()) Icons.Default.Star else Icons.Default.StarOutline,
                            contentDescription = null,
                            tint = Color(0xFFFFA000),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "$totalReviews đánh giá",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                // Hiển thị đúng phân bố rating từ 5 sao đến 1 sao
                repeat(5) { index ->
                    val starRating = 5 - index
                    val progress = ratingDistribution[starRating] ?: 0f

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 3.dp)
                    ) {
                        Text(
                            text = "$starRating",
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
                            progress = { progress },
                            modifier = Modifier
                                .weight(1f)
                                .height(8.dp)
                                .clip(androidx.compose.foundation.shape.RoundedCornerShape(4.dp)),
                            color = Color(0xFFFFA000),
                            trackColor = Color.Gray.copy(alpha = 0.2f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${(progress * 100).toInt()}%",
                            fontSize = 11.sp,
                            color = Color.Gray,
                            modifier = Modifier.width(35.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        HorizontalDivider(color = Color.Gray.copy(alpha = 0.2f))
        Spacer(modifier = Modifier.height(16.dp))

        // Hiển thị trạng thái loading
        if (reviewsState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
        // Hiển thị lỗi nếu có
        else if (reviewsState.error != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Error,
                        contentDescription = null,
                        tint = Color.Red,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = reviewsState.error ?: "Có lỗi xảy ra",
                        fontSize = 14.sp,
                        color = Color.Red
                    )
                }
            }
        }
        // Hiển thị thông báo nếu chưa có đánh giá
        else if (reviews.isEmpty()) {
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
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Hãy là người đầu tiên đánh giá sân này!",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
            }
        }
        // Hiển thị danh sách đánh giá thực tế
        else {
            Text(
                text = "Đánh giá từ khách hàng",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Sử dụng Column thay vì LazyColumn vì đã ở trong scroll view
            reviews.forEach { review ->
                ReviewCard(
                    review = review,
                    showVenueName = false,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            }
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
fun DetailScreenPreview(
    @PreviewParameter(SampleVenueProvider::class) venue: Venue
) {
    DetailScreen(
        venue = venue,
        onBackClick = {},
        onBookClick = {}
    )
}
