package com.example.bookingcourt.presentation.court.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.bookingcourt.domain.model.Amenity
import com.example.bookingcourt.domain.model.Court
import com.example.bookingcourt.domain.model.CourtType
import com.example.bookingcourt.domain.model.SportType
import com.example.bookingcourt.presentation.theme.*
import kotlinx.datetime.LocalTime
import java.text.NumberFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourtListScreen(
    sportType: String?,
    onNavigateBack: () -> Unit,
    onNavigateToCourtDetail: (String) -> Unit,
) {
    // Mock data - Replace with ViewModel when ready
    val courts = remember {
        listOf(
            Court(
                id = "court_1",
                name = "Star Club Badminton",
                address = "123 Đường ABC, Quận 1, TP.HCM",
                latitude = 10.762622,
                longitude = 106.660172,
                sportType = SportType.BADMINTON,
                courtType = CourtType.INDOOR,
                pricePerHour = 150000,
                description = "Sân cầu lông chất lượng cao với hệ thống chiếu sáng hiện đại",
                images = listOf("https://images.unsplash.com/photo-1626224583764-f87db24ac4ea?w=400"),
                rating = 4.5f,
                totalReviews = 120,
                openTime = LocalTime(6, 0),
                closeTime = LocalTime(22, 0),
                amenities = listOf(
                    Amenity("1", "Wifi", "wifi"),
                    Amenity("2", "Parking", "parking"),
                    Amenity("3", "Shower", "shower"),
                ),
                rules = "Không hút thuốc trong sân",
                ownerId = "owner_1",
                isActive = true,
                maxPlayers = 4,
            ),
            Court(
                id = "court_2",
                name = "Victory Badminton Center",
                address = "456 Đường XYZ, Quận 3, TP.HCM",
                latitude = 10.776889,
                longitude = 106.695313,
                sportType = SportType.BADMINTON,
                courtType = CourtType.INDOOR,
                pricePerHour = 200000,
                description = "Sân cầu lông hiện đại với không gian rộng rãi",
                images = listOf("https://images.unsplash.com/photo-1554068865-24cecd4e34b8?w=400"),
                rating = 4.8f,
                totalReviews = 95,
                openTime = LocalTime(6, 0),
                closeTime = LocalTime(23, 0),
                amenities = listOf(
                    Amenity("1", "Wifi", "wifi"),
                    Amenity("2", "Parking", "parking"),
                    Amenity("3", "Shower", "shower"),
                    Amenity("4", "Cafeteria", "cafeteria"),
                ),
                rules = "Vui lòng giữ gìn vệ sinh chung",
                ownerId = "owner_2",
                isActive = true,
                maxPlayers = 4,
            ),
        )
    }

    val filteredCourts = remember(sportType) {
        if (sportType != null) {
            courts.filter { it.sportType.name == sportType }
        } else {
            courts
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = when (sportType) {
                            SportType.BADMINTON.name -> "Sân Cầu Lông"
                            SportType.TENNIS.name -> "Sân Tennis"
                            SportType.FOOTBALL.name -> "Sân Bóng Đá"
                            SportType.BASKETBALL.name -> "Sân Bóng Rổ"
                            SportType.TABLE_TENNIS.name -> "Sân Bóng Bàn"
                            SportType.VOLLEYBALL.name -> "Sân Bóng Chuyền"
                            else -> "Danh Sách Sân"
                        }
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Primary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                ),
            )
        },
    ) { paddingValues ->
        if (filteredCourts.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.SearchOff,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = TextSecondary,
                    )
                    Text(
                        text = "Không tìm thấy sân nào",
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextSecondary,
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                items(filteredCourts) { court ->
                    CourtListItem(
                        court = court,
                        onClick = { onNavigateToCourtDetail(court.id) },
                    )
                }
            }
        }
    }
}

@Composable
private fun CourtListItem(
    court: Court,
    onClick: () -> Unit,
) {
    val currencyFormat = remember { NumberFormat.getCurrencyInstance(Locale("vi", "VN")) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column {
            // Court Image
            if (court.images.isNotEmpty()) {
                AsyncImage(
                    model = court.images.first(),
                    contentDescription = court.name,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)),
                    contentScale = ContentScale.Crop,
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Default.Image,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = TextSecondary.copy(alpha = 0.3f),
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = court.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = Color(0xFFFFC107),
                        modifier = Modifier.size(16.dp),
                    )
                    Text(
                        text = "${court.rating}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                    )
                    Text(
                        text = "(${court.totalReviews} đánh giá)",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.Top,
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = Primary,
                        modifier = Modifier.size(16.dp),
                    )
                    Text(
                        text = court.address,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        tint = Primary,
                        modifier = Modifier.size(16.dp),
                    )
                    Text(
                        text = "${court.openTime} - ${court.closeTime}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "${currencyFormat.format(court.pricePerHour)}/giờ",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Primary,
                    )
                    Button(
                        onClick = onClick,
                        colors = ButtonDefaults.buttonColors(containerColor = Primary),
                    ) {
                        Text("Đặt sân")
                    }
                }
            }
        }
    }
}
