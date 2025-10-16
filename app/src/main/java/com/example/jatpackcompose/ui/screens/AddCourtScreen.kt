package com.example.jatpackcompose.ui.screens

  import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.jatpackcompose.ui.theme.JatpackComposeTheme

// Data class for new venue
data class NewVenueData(
    val name: String,
    val courtCount: Int,
    val address: String,
    val phone: String,
    val openTime: String,
    val closeTime: String,
    val timeSlots: List<TimeSlot>
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCourtScreen(
    onBackClick: () -> Unit = {},
    onSaveClick: (NewVenueData) -> Unit = {}
) {
    var venueName by remember { mutableStateOf("") }
    var courtCount by remember { mutableStateOf(1) }
    var address by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var openTime by remember { mutableStateOf("05:00") }
    var closeTime by remember { mutableStateOf("23:00") }
    var timeSlots by remember {
        mutableStateOf(
            listOf(
                TimeSlot(startTime = "05:00", endTime = "08:00", price = "80000"),
                TimeSlot(startTime = "08:00", endTime = "16:00", price = "60000"),
                TimeSlot(startTime = "16:00", endTime = "23:00", price = "100000")
            )
        )
    }

    var showAddTimeSlotDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf<TimeSlot?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Logo sân
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .background(
                                    color = Color.White.copy(alpha = 0.2f),
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Thêm sân",
                                tint = Color.White,
                                modifier = Modifier.size(30.dp)
                            )
                        }

                        // Tiêu đề
                        Text(
                            text = "Thêm Sân Mới",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Quay lại",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF123E62)
                ),
                modifier = Modifier.height(80.dp)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (venueName.isNotBlank() && address.isNotBlank() && phone.isNotBlank()) {
                        onSaveClick(
                            NewVenueData(
                                name = venueName,
                                courtCount = courtCount,
                                address = address,
                                phone = phone,
                                openTime = openTime,
                                closeTime = closeTime,
                                timeSlots = timeSlots
                            )
                        )
                    }
                },
                containerColor = Color(0xFF123E62),
                contentColor = Color.White,
                modifier = Modifier
                    .height(56.dp)
                    .widthIn(min = 56.dp)
            ) {
                Text(
                    text = "LƯU",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F5F5))
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Tên sân
            item {
                Text(
                    text = "Tên Sân",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    OutlinedTextField(
                        value = venueName,
                        onValueChange = { venueName = it },
                        label = { Text("Nhập tên sân") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = Color(0xFF123E62)
                            )
                        },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF123E62),
                            focusedLabelColor = Color(0xFF123E62),
                            unfocusedBorderColor = Color.Transparent
                        )
                    )
                }
            }

            // Số lượng sân
            item {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Số Lượng Sân",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Số sân",
                            fontSize = 16.sp,
                            color = Color.Black
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = { if (courtCount > 1) courtCount-- },
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(
                                        color = Color(0xFFFF5252).copy(alpha = 0.1f),
                                        shape = CircleShape
                                    )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Giảm",
                                    tint = Color(0xFFFF5252)
                                )
                            }

                            Text(
                                text = "$courtCount",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF123E62),
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )

                            IconButton(
                                onClick = { courtCount++ },
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(
                                        color = Color(0xFF4CAF50).copy(alpha = 0.1f),
                                        shape = CircleShape
                                    )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Tăng",
                                    tint = Color(0xFF4CAF50)
                                )
                            }
                        }
                    }
                }
            }

            // Thông tin cơ bản
            item {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Thông Tin Cơ Bản",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Địa chỉ
                        OutlinedTextField(
                            value = address,
                            onValueChange = { address = it },
                            label = { Text("Địa Chỉ") },
                            modifier = Modifier.fillMaxWidth(),
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = Color(0xFF123E62)
                                )
                            },
                            maxLines = 2,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF123E62),
                                focusedLabelColor = Color(0xFF123E62)
                            )
                        )

                        // Số điện thoại
                        OutlinedTextField(
                            value = phone,
                            onValueChange = {
                                if (it.length <= 10) phone = it
                            },
                            label = { Text("Số Điện Thoại") },
                            modifier = Modifier.fillMaxWidth(),
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Phone,
                                    contentDescription = null,
                                    tint = Color(0xFF123E62)
                                )
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            prefix = { Text("+84 ", color = Color.Gray) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF123E62),
                                focusedLabelColor = Color(0xFF123E62)
                            )
                        )

                        // Giờ hoạt động
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedTextField(
                                value = openTime,
                                onValueChange = { openTime = it },
                                label = { Text("Giờ Mở") },
                                modifier = Modifier.weight(1f),
                                placeholder = { Text("HH:MM") },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF123E62),
                                    focusedLabelColor = Color(0xFF123E62)
                                )
                            )

                            OutlinedTextField(
                                value = closeTime,
                                onValueChange = { closeTime = it },
                                label = { Text("Giờ Đóng") },
                                modifier = Modifier.weight(1f),
                                placeholder = { Text("HH:MM") },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF123E62),
                                    focusedLabelColor = Color(0xFF123E62)
                                )
                            )
                        }
                    }
                }
            }

            // Giá theo khung giờ
            item {
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Giá Theo Khung Giờ",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )

                    IconButton(
                        onClick = { showAddTimeSlotDialog = true },
                        modifier = Modifier
                            .size(36.dp)
                            .background(
                                color = Color(0xFF123E62),
                                shape = CircleShape
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Thêm khung giờ",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            items(timeSlots) { slot ->
                TimeSlotCard(
                    timeSlot = slot,
                    onDeleteClick = { showDeleteConfirmDialog = slot }
                )
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }

    // Dialog thêm khung giờ
    if (showAddTimeSlotDialog) {
        AddTimeSlotDialog(
            onDismiss = { showAddTimeSlotDialog = false },
            onAdd = { newSlot: TimeSlot ->
                timeSlots = timeSlots + newSlot
                showAddTimeSlotDialog = false
            }
        )
    }

    // Dialog xác nhận xóa
    showDeleteConfirmDialog?.let { slot ->
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = null },
            title = { Text("Xác Nhận Xóa") },
            text = { Text("Bạn có chắc chắn muốn xóa khung giờ ${slot.startTime} - ${slot.endTime}?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        timeSlots = timeSlots.filter { it.id != slot.id }
                        showDeleteConfirmDialog = null
                    }
                ) {
                    Text("Xóa", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = null }) {
                    Text("Hủy")
                }
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun AddCourtScreenPreview() {
    JatpackComposeTheme {
        AddCourtScreen()
    }
}