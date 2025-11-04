package com.example.bookingcourt.presentation.notification.screen

import androidx.compose.foundation.clickable
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
import com.example.bookingcourt.presentation.notification.viewmodel.NotificationsViewModel

@Composable
fun NotificationsScreen(
    onNavigateBack: () -> Unit,
) {
    val vm: NotificationsViewModel = hiltViewModel()
    val state by vm.state.collectAsState()

    LaunchedEffect(Unit) { vm.load() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Thông báo") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
                actions = {
                    TextButton(onClick = { vm.markAllAsRead() }) { Text("Đọc tất cả") }
                }
            )
        }
    ) { paddingValues ->
        when (val s = state) {
            is Resource.Loading -> Box(Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            is Resource.Error -> Box(Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) { Text(s.message ?: "Lỗi") }
            is Resource.Success -> {
                val notifications = s.data ?: emptyList()
                if (notifications.isEmpty()) {
                    Box(Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) { Text("Không có thông báo") }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(paddingValues),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(notifications) { n ->
                            Card(modifier = Modifier.fillMaxWidth()) {
                                Column(Modifier.padding(12.dp)) {
                                    Text(n.title, style = MaterialTheme.typography.titleMedium)
                                    Spacer(Modifier.height(4.dp))
                                    Text(n.message, style = MaterialTheme.typography.bodySmall)
                                    Spacer(Modifier.height(8.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                        TextButton(onClick = { vm.markAsRead(n.id) }) { Text("Đánh dấu đã đọc") }
                                        TextButton(onClick = { vm.delete(n.id) }) { Text("Xóa") }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

