package com.example.jatpackcompose.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.jatpackcompose.ui.theme.JatpackComposeTheme
import java.text.NumberFormat
import java.util.Locale

// Data classes
data class CourtItem(
    val courtNumber: Int,
    val imageUri: Uri? = null, // URI của hình ảnh
    val name: String = "Sân $courtNumber"
)

data class TimeSlot(
    val id: String = java.util.UUID.randomUUID().toString(),
    val startTime: String,
    val endTime: String,
    val price: String
)

data class VenueEditData(
    val id: String,
    val name: String,
    var courtCount: Int,
    var address: String,
    var phone: String,
    var openTime: String,
    var closeTime: String,
    var timeSlots: List<TimeSlot>
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseEditScreen(
    venueName: String = "Star Club Badminton",
    onBackClick: () -> Unit = {},
    onSaveClick: (VenueEditData) -> Unit = {}
) {
    var courtCount by remember { mutableStateOf(8) }
    var address by remember { mutableStateOf("Số 181 P. Cầu Cốc, Tây Mỗ, Nam Từ Liêm, Hà Nội") }
    var phone by remember { mutableStateOf("0982201808") }
    var openTime by remember { mutableStateOf("05:00") }
    var closeTime by remember { mutableStateOf("23:00") }
    var logoUri by remember { mutableStateOf<Uri?>(null) }
    var courtImages by remember { mutableStateOf<Map<Int, Uri>>(emptyMap()) }
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

    // Launcher cho việc chọn logo
    val logoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { logoUri = it }
    }

    // Launcher cho việc chọn ảnh sân
    var selectedCourtNumber by remember { mutableStateOf<Int?>(null) }
    val courtImagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedCourtNumber?.let { courtNum ->
                courtImages = courtImages + (courtNum to uri)
            }
        }
    }

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
                        // Logo sân với khả năng thay đổi
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(
                                    color = Color.White.copy(alpha = 0.2f),
                                    shape = CircleShape
                                )
                                .clickable { logoPickerLauncher.launch("image/*") }
                                .border(
                                    width = 1.dp,
                                    color = Color.White.copy(alpha = 0.3f),
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (logoUri != null) {
                                AsyncImage(
                                    model = ImageRequest.Builder(LocalContext.current)
                                        .data(logoUri)
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = "Logo sân",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = "Logo sân",
                                    tint = Color.White,
                                    modifier = Modifier.size(30.dp)
                                )
                            }
                        }

                        // Tên sân
                        Text(
                            text = venueName,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
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
                    onSaveClick(
                        VenueEditData(
                            id = "VN001",
                            name = venueName,
                            courtCount = courtCount,
                            address = address,
                            phone = phone,
                            openTime = openTime,
                            closeTime = closeTime,
                            timeSlots = timeSlots
                        )
                    )
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
            // Số lượng sân
            item {
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
                            text = "Số sân hiện tại",
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
                            onValueChange = { phone = it },
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

            // Danh sách các sân với hình ảnh
            item {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Hình Ảnh Các Sân",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            items((1..courtCount).toList()) { courtNumber ->
                CourtImageCard(
                    courtNumber = courtNumber,
                    imageUri = courtImages[courtNumber],
                    onEditImageClick = {
                        selectedCourtNumber = courtNumber
                        courtImagePickerLauncher.launch("image/*")
                    }
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
            onAdd = { newSlot ->
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

@Composable
fun TimeSlotCard(
    timeSlot: TimeSlot,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            color = Color(0xFF123E62).copy(alpha = 0.1f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = null,
                        tint = Color(0xFF123E62),
                        modifier = Modifier.size(24.dp)
                    )
                }

                Column {
                    Text(
                        text = "${timeSlot.startTime} - ${timeSlot.endTime}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Text(
                        text = "${java.text.NumberFormat.getInstance(java.util.Locale("vi", "VN")).format(timeSlot.price.toIntOrNull() ?: 0)} đ/giờ",
                        fontSize = 14.sp,
                        color = Color(0xFF4CAF50),
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            IconButton(
                onClick = onDeleteClick,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Xóa",
                    tint = Color(0xFFFF5252)
                )
            }
        }
    }
}

@Composable
fun CourtImageCard(
    courtNumber: Int,
    imageUri: Uri? = null,
    onEditImageClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Hình ảnh sân
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        color = Color(0xFF123E62).copy(alpha = 0.1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .clickable { onEditImageClick() },
                contentAlignment = Alignment.Center
            ) {
                if (imageUri != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(imageUri)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Hình ảnh sân $courtNumber",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Hình ảnh sân $courtNumber",
                        tint = Color(0xFF123E62).copy(alpha = 0.3f),
                        modifier = Modifier.size(40.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Thông tin sân
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Sân $courtNumber",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                Text(
                    text = if (imageUri != null) "Đã có hình ảnh minh họa" else "Chưa có hình ảnh minh họa",
                    fontSize = 13.sp,
                    color = if (imageUri != null) Color(0xFF4CAF50) else Color.Gray
                )
            }

            // Nút chỉnh sửa hình ảnh
            IconButton(
                onClick = onEditImageClick,
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        color = Color(0xFF123E62).copy(alpha = 0.1f),
                        shape = CircleShape
                    )
            ) {
                Icon(
                    imageVector = if (imageUri != null) Icons.Default.Edit else Icons.Default.AddCircle,
                    contentDescription = if (imageUri != null) "Sửa hình ảnh" else "Thêm hình ảnh",
                    tint = Color(0xFF123E62),
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTimeSlotDialog(
    onDismiss: () -> Unit,
    onAdd: (TimeSlot) -> Unit
) {
    var startTime by remember { mutableStateOf("") }
    var endTime by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Thêm Khung Giờ",
                fontWeight = FontWeight.Bold,
                color = Color(0xFF123E62)
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = startTime,
                        onValueChange = { startTime = it },
                        label = { Text("Giờ Bắt Đầu") },
                        placeholder = { Text("HH:MM") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF123E62),
                            focusedLabelColor = Color(0xFF123E62)
                        )
                    )

                    OutlinedTextField(
                        value = endTime,
                        onValueChange = { endTime = it },
                        label = { Text("Giờ Kết Thúc") },
                        placeholder = { Text("HH:MM") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF123E62),
                            focusedLabelColor = Color(0xFF123E62)
                        )
                    )
                }

                OutlinedTextField(
                    value = price,
                    onValueChange = { price = it },
                    label = { Text("Giá (đ/giờ)") },
                    placeholder = { Text("VD: 80000") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF123E62),
                        focusedLabelColor = Color(0xFF123E62)
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (startTime.isNotBlank() && endTime.isNotBlank() && price.isNotBlank()) {
                        onAdd(TimeSlot(startTime = startTime, endTime = endTime, price = price))
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF123E62)
                ),
                enabled = startTime.isNotBlank() && endTime.isNotBlank() && price.isNotBlank()
            ) {
                Text("Thêm")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Hủy", color = Color.Gray)
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun CourseEditScreenPreview() {
    JatpackComposeTheme {
        CourseEditScreen()
    }
}