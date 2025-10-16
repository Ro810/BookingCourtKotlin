package com.example.bookingcourt.presentation.court.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.bookingcourt.domain.model.Court
import com.example.bookingcourt.domain.model.CourtType
import com.example.bookingcourt.domain.model.SportType
import com.example.bookingcourt.presentation.theme.BookingCourtTheme
import kotlinx.datetime.LocalTime
import java.text.NumberFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourtListScreen(
    sportType: String? = null,
    onNavigateBack: () -> Unit = {},
    onNavigateToCourtDetail: (String) -> Unit = {},
    onNavigateToSearch: () -> Unit = {},
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedSportType by remember {
        mutableStateOf(
            sportType?.let {
                try {
                    SportType.valueOf(it.uppercase())
                } catch (e: Exception) {
                    null
                }
            },
        )
    }

    val courts = remember {
        listOf(
            Court(
                id = "1",
                name = "Star Club Badminton",
                description = "Sân cầu lông chuyên nghiệp",
                address = "181 P. Cầu Cốc, Nam Từ Liêm, Hà Nội",
                latitude = 21.0159,
                longitude = 105.7447,
                images = listOf("https://example.com/court1.jpg"),
                sportType = SportType.BADMINTON,
                courtType = CourtType.INDOOR,
                pricePerHour = 150000,
                openTime = LocalTime(5, 0),
                closeTime = LocalTime(23, 0),
                amenities = emptyList(),
                rules = "Không hút thuốc",
                ownerId = "owner1",
                rating = 4.5f,
                totalReviews = 127,
                isActive = true,
                maxPlayers = 4,
            ),
            Court(
                id = "2",
                name = "MVP Fitness Badminton",
                description = "Sân hiện đại, thoáng mát",
                address = "Tầng 10, Toà F.Zone 4, Vinsmart Tây Mỗ",
                latitude = 21.0200,
                longitude = 105.7500,
                images = listOf("https://example.com/court2.jpg"),
                sportType = SportType.BADMINTON,
                courtType = CourtType.INDOOR,
                pricePerHour = 120000,
                openTime = LocalTime(5, 30),
                closeTime = LocalTime(21, 30),
                amenities = emptyList(),
                rules = "Giữ gìn vệ sinh chung",
                ownerId = "owner2",
                rating = 4.2f,
                totalReviews = 85,
                isActive = true,
                maxPlayers = 4,
            ),
        )
    }

    val filteredCourts = courts.filter { court ->
        (searchQuery.isEmpty() || court.name.contains(searchQuery, ignoreCase = true)) &&
            (selectedSportType == null || court.sportType == selectedSportType)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Danh sách sân") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại")
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToSearch) {
                        Icon(Icons.Default.Search, contentDescription = "Tìm kiếm")
                    }
                },
            )
        },
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Search Bar
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Tìm kiếm sân...") },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null)
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Xóa")
                            }
                        }
                    },
                    singleLine = true,
                )
            }

            // Sport Type Filter
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    item {
                        FilterChip(
                            selected = selectedSportType == null,
                            onClick = { selectedSportType = null },
                            label = { Text("Tất cả") },
                        )
                    }
                    items(SportType.values().toList()) { sport ->
                        FilterChip(
                            selected = selectedSportType == sport,
                            onClick = {
                                selectedSportType = if (selectedSportType == sport) null else sport
                            },
                            label = { Text(getSportTypeName(sport)) },
                        )
                    }
                }
            }

            // Court List
            items(filteredCourts) { court ->
                CourtCard(
                    court = court,
                    onClick = { onNavigateToCourtDetail(court.id) },
                )
            }

            // Empty state
            if (filteredCourts.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Icon(
                                Icons.Default.Info,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = Color.Gray,
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Không tìm thấy sân nào",
                                color = Color.Gray,
                                fontSize = 16.sp,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CourtCard(
    court: Court,
    onClick: () -> Unit,
) {
    val currencyFormat = remember { NumberFormat.getCurrencyInstance(Locale("vi", "VN")) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
        ) {
            // Court Image
            AsyncImage(
                model = court.images.firstOrNull() ?: "https://via.placeholder.com/100",
                contentDescription = court.name,
                modifier = Modifier
                    .size(100.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.LightGray),
                contentScale = ContentScale.Crop,
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Court Info
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceBetween,
            ) {
                Column {
                    Text(
                        text = court.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = court.address,
                        fontSize = 14.sp,
                        color = Color.Gray,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom,
                ) {
                    Column {
                        Text(
                            text = currencyFormat.format(court.pricePerHour) + "/giờ",
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = "${court.openTime} - ${court.closeTime}",
                            fontSize = 12.sp,
                            color = Color.Gray,
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            Icons.Default.Star,
                            contentDescription = null,
                            tint = Color(0xFFFFA500),
                            modifier = Modifier.size(16.dp),
                        )
                        Text(
                            text = "${court.rating}",
                            fontSize = 14.sp,
                            modifier = Modifier.padding(horizontal = 4.dp),
                        )
                        Text(
                            text = "(${court.totalReviews})",
                            fontSize = 12.sp,
                            color = Color.Gray,
                        )
                    }
                }
            }
        }
    }
}

fun getSportTypeName(sport: SportType): String {
    return when (sport) {
        SportType.BADMINTON -> "Cầu lông"
        SportType.TABLE_TENNIS -> "Bóng bàn"
        SportType.TENNIS -> "Tennis"
        SportType.FOOTBALL -> "Bóng đá"
        SportType.BASKETBALL -> "Bóng rổ"
        SportType.VOLLEYBALL -> "Bóng chuyền"
    }
}

@Preview(showBackground = true)
@Composable
fun CourtListScreenPreview() {
    BookingCourtTheme {
        CourtListScreen()
    }
}
