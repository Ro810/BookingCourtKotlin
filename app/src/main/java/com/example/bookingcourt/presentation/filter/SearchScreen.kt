package com.example.bookingcourt.presentation.filter

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bookingcourt.domain.model.Court
import com.example.bookingcourt.domain.model.SportType
import com.example.bookingcourt.domain.model.CourtType
import com.example.bookingcourt.presentation.theme.Primary
import com.example.bookingcourt.presentation.theme.BookingCourtTheme
import com.example.bookingcourt.presentation.home.viewmodel.HomeViewModel
import kotlinx.datetime.LocalTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onBackClick: () -> Unit,
    onCourtClick: (Court) -> Unit,
    viewModel: HomeViewModel = viewModel()
) {
    var searchQuery by remember { mutableStateOf("") }
    val homeState by viewModel.state.collectAsState()

    // Combine all courts from different lists
    val allCourts = remember(homeState) {
        homeState.featuredCourts + homeState.nearbyCourts + homeState.recommendedCourts
    }

    val filteredCourts = remember(searchQuery, allCourts) {
        if (searchQuery.isEmpty()) {
            allCourts
        } else {
            allCourts.filter {
                it.name.contains(searchQuery, ignoreCase = true) ||
                    it.address.contains(searchQuery, ignoreCase = true) ||
                    it.description.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tìm kiếm") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Primary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = { Text("Tìm kiếm sân...") },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = "Search")
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(28.dp)
            )

            // Results
            if (filteredCourts.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.SearchOff,
                            contentDescription = "No results",
                            modifier = Modifier.size(64.dp),
                            tint = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Không tìm thấy kết quả",
                            color = Color.Gray,
                            fontSize = 16.sp
                        )
                    }
                }
            } else {
                LazyColumn {
                    items(filteredCourts) { court ->
                        SearchResultItem(
                            court = court,
                            onCourtClick = { onCourtClick(court) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SearchResultItem(
    court: Court,
    onCourtClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { onCourtClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .background(Primary.copy(alpha = 0.3f), CircleShape)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = court.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    text = court.address,
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Go",
                tint = Color.Gray
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SearchScreenPreview() {
    BookingCourtTheme {
        SearchScreen(
            onBackClick = {},
            onCourtClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SearchResultItemPreview() {
    BookingCourtTheme {
        SearchResultItem(
            court = Court(
                id = "preview1",
                name = "Star Club Badminton",
                description = "Sân cầu lông chất lượng cao với thiết bị hiện đại",
                address = "Số 181 P. Cầu Cốc, Tây Mỗ, Nam Từ Liêm, Hà Nội",
                latitude = 21.0159,
                longitude = 105.7447,
                images = emptyList(),
                sportType = SportType.BADMINTON,
                courtType = CourtType.INDOOR,
                pricePerHour = 150,
                openTime = LocalTime(5, 0),
                closeTime = LocalTime(23, 0),
                amenities = emptyList(),
                rules = "Không hút thuốc trong sân",
                ownerId = "owner1",
                rating = 4.5f,
                totalReviews = 128,
                isActive = true,
                maxPlayers = 4,
            ),
            onCourtClick = {}
        )
    }
}
