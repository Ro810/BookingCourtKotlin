package com.example.bookingcourt.presentation.booking.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.bookingcourt.core.common.Resource
import com.example.bookingcourt.domain.model.Booking
import com.example.bookingcourt.presentation.booking.viewmodel.BookingViewModel

@Composable
fun BookingHistoryScreen(
    onNavigateBack: () -> Unit,
    onNavigateToBookingDetail: (String) -> Unit,
    viewModel: BookingViewModel = hiltViewModel()
) {
    val bookingsState by viewModel.bookingsState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.getUserBookings(page = 1, size = 50)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Lịch sử đặt sân") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                }
            )
        }
    ) { paddingValues ->
        when (val state = bookingsState) {
            is Resource.Loading -> {
                Box(Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is Resource.Error -> {
                Box(Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                    Text(state.message ?: "Đã xảy ra lỗi")
                }
            }
            is Resource.Success -> {
                val bookings: List<Booking> = state.data ?: emptyList()
                if (bookings.isEmpty()) {
                    Box(Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                        Text("Chưa có đặt sân nào")
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(bookings) { booking ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onNavigateToBookingDetail(booking.id) }
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(booking.courtName, style = MaterialTheme.typography.titleMedium)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "${booking.startTime} - ${booking.endTime}",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Trạng thái: ${booking.status}",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        }
                    }
                }
            }
            else -> {}
        }
    }
}
