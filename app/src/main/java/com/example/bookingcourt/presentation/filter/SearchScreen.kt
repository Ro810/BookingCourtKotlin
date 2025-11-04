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
import com.example.bookingcourt.domain.model.Venue
import com.example.bookingcourt.presentation.theme.Primary
import com.example.bookingcourt.presentation.theme.BookingCourtTheme
import com.example.bookingcourt.presentation.home.viewmodel.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onBackClick: () -> Unit,
    onVenueClick: (Venue) -> Unit, // Đổi từ onCourtClick -> onVenueClick
    viewModel: HomeViewModel = viewModel()
) {
    var searchQuery by remember { mutableStateOf("") }
    val homeState by viewModel.state.collectAsState()

    // Combine all venues from different lists
    val allVenues = remember(homeState) {
        homeState.featuredVenues + homeState.nearbyVenues + homeState.recommendedVenues
    }

    val filteredVenues = remember(searchQuery, allVenues) {
        if (searchQuery.isEmpty()) {
            allVenues
        } else {
            allVenues.filter {
                it.name.contains(searchQuery, ignoreCase = true) ||
                    it.address.getFullAddress().contains(searchQuery, ignoreCase = true) ||
                    (it.description?.contains(searchQuery, ignoreCase = true) ?: false)
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
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredVenues) { venue ->
                    VenueSearchCard(
                        venue = venue,
                        onClick = { onVenueClick(venue) }
                    )
                }

                if (filteredVenues.isEmpty() && searchQuery.isNotEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Không tìm thấy kết quả",
                                color = Color.Gray
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun VenueSearchCard(
    venue: Venue,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Venue icon/image placeholder
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .background(Primary.copy(alpha = 0.2f), CircleShape)
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Venue info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = venue.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = venue.address.getFullAddress(),
                    fontSize = 12.sp,
                    color = Color.Gray,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = Color(0xFFFFC107),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${venue.averageRating}",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "• ${venue.courtsCount} sân",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }

            // Price
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${venue.pricePerHour / 1000}k",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Primary
                )
                Text(
                    text = "/giờ",
                    fontSize = 10.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SearchScreenPreview() {
    BookingCourtTheme {
        SearchScreen(
            onBackClick = {},
            onVenueClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun VenueSearchCardPreview() {
    BookingCourtTheme {
        VenueSearchCard(
            venue = Venue(
                id = 1L,
                name = "Sân bóng đá ABC",
                description = "Sân bóng đá mini chất lượng cao",
                numberOfCourt = 3,
                address = com.example.bookingcourt.domain.model.Address(
                    id = 1L,
                    provinceOrCity = "TP. HCM",
                    district = "Quận 1",
                    detailAddress = "123 Đường XYZ"
                ),
                courtsCount = 3,
                pricePerHour = 200000,
                averageRating = 4.5f,
                totalReviews = 100
            ),
            onClick = {}
        )
    }
}
