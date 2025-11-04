package com.example.bookingcourt.presentation.owner.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.bookingcourt.core.common.Resource
import com.example.bookingcourt.presentation.owner.viewmodel.PendingBookingsViewModel

@Composable
fun PendingBookingsScreen(
    onNavigateBack: () -> Unit,
) {
    val vm: PendingBookingsViewModel = hiltViewModel()
    val state by vm.state.collectAsState()
    val action by vm.actionState.collectAsState()

    LaunchedEffect(Unit) { vm.load() }

    Scaffold(topBar = {
        TopAppBar(
            title = { Text("Booking chờ xác nhận") },
            navigationIcon = { IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) } }
        )
    }) { paddingValues ->
        when (val s = state) {
            is Resource.Loading -> Box(Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            is Resource.Error -> Box(Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) { Text(s.message ?: "Lỗi") }
            is Resource.Success -> {
                val list = s.data ?: emptyList()
                if (list.isEmpty()) {
                    Box(Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) { Text("Không có booking chờ") }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(paddingValues),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(list) { b ->
                            Card(Modifier.fillMaxWidth()) {
                                Column(Modifier.padding(12.dp)) {
                                    Text("#${b.id} - ${b.courtName ?: "Sân"}")
                                    Spacer(Modifier.height(4.dp))
                                    Text("Khách: ${b.userName ?: "Người dùng"}")
                                    Spacer(Modifier.height(8.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                        Button(onClick = { vm.accept(b.id) }) { Text("Chấp nhận") }
                                        OutlinedButton(onClick = { vm.reject(b.id, "Chưa nhận được tiền") }) { Text("Từ chối") }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (action is Resource.Loading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        }
    }
}

