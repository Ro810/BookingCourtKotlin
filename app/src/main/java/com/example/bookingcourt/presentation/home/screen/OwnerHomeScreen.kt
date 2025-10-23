package com.example.bookingcourt.presentation.home.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.bookingcourt.domain.model.Court
import com.example.bookingcourt.domain.model.CourtType
import com.example.bookingcourt.domain.model.SportType
import com.example.bookingcourt.presentation.profile.screen.ProfileScreen
import com.example.bookingcourt.presentation.theme.BookingCourtTheme
import kotlinx.datetime.LocalTime
import java.util.*

enum class HomeTab {
    HOME,
    PROFILE
}

@Composable
fun HomeScreen(
    ownerName: String = "Trần Đỗ Lan Phương",
    onNavigateToCourtDetail: (String) -> Unit = {},
    onNavigateToEditProfile: () -> Unit = {},
    onLogout: () -> Unit = {},
) {
    var selectedTab by remember { mutableStateOf(HomeTab.HOME) }

    Scaffold(
        containerColor = Color.Transparent,
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 8.dp,
            ) {
                NavigationBarItem(
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Home,
                            contentDescription = "Trang chủ",
                            modifier = Modifier.size(32.dp),
                        )
                    },
                    label = { Text("Trang chủ") },
                    selected = selectedTab == HomeTab.HOME,
                    onClick = { selectedTab = HomeTab.HOME },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF123E62),
                        selectedTextColor = Color(0xFF123E62),
                        indicatorColor = Color.Transparent,
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray,
                    ),
                )
                NavigationBarItem(
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Tài khoản",
                            modifier = Modifier.size(32.dp),
                        )
                    },
                    label = { Text("Tài khoản") },
                    selected = selectedTab == HomeTab.PROFILE,
                    onClick = { selectedTab = HomeTab.PROFILE },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF123E62),
                        selectedTextColor = Color(0xFF123E62),
                        indicatorColor = Color.Transparent,
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray,
                    ),
                )
            }
        },
    ) { paddingValues ->
        when (selectedTab) {
            HomeTab.HOME -> {
                OwnerHomeContent(
                    ownerName = ownerName,
                    onNavigateToCourtDetail = onNavigateToCourtDetail,
                    bottomPadding = paddingValues.calculateBottomPadding(),
                )
            }
            HomeTab.PROFILE -> {
                ProfileScreen(
                    onNavigateBack = { selectedTab = HomeTab.HOME },
                    onNavigateToEditProfile = onNavigateToEditProfile,
                    onLogout = onLogout,
                    showBackButton = false,
                    showTopBar = false,
                    bottomPadding = paddingValues.calculateBottomPadding(),
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OwnerHomeContent(
    ownerName: String,
    onNavigateToCourtDetail: (String) -> Unit,
    bottomPadding: androidx.compose.ui.unit.Dp,
) {
    var venues by remember {
        mutableStateOf(
            listOf(
                Court(
                    id = "VN001",
                    name = "Star Club Badminton",
                    description = "Sân cầu lông chất lượng cao",
                    address = "Số 181 P. Cầu Cốc, Tây Mỗ, Nam Từ Liêm, Hà Nội",
                    latitude = 21.0159,
                    longitude = 105.7447,
                    images = emptyList(),
                    sportType = SportType.BADMINTON,
                    courtType = CourtType.INDOOR,
                    pricePerHour = 150000,
                    openTime = LocalTime(5, 0),
                    closeTime = LocalTime(23, 0),
                    amenities = emptyList(),
                    rules = "Không hút thuốc trong sân",
                    ownerId = "owner1",
                    rating = 4.5f,
                    totalReviews = 25,
                    isActive = true,
                    maxPlayers = 4,
                ),
                Court(
                    id = "VN002",
                    name = "MVP Fitness Badminton",
                    description = "Sân cầu lông hiện đại",
                    address = "Tầng 10, Toà F.Zone 4, Vinsmart Tây Mỗ",
                    latitude = 21.0200,
                    longitude = 105.7500,
                    images = emptyList(),
                    sportType = SportType.BADMINTON,
                    courtType = CourtType.INDOOR,
                    pricePerHour = 120000,
                    openTime = LocalTime(5, 30),
                    closeTime = LocalTime(21, 30),
                    amenities = emptyList(),
                    rules = "Giữ gìn vệ sinh chung",
                    ownerId = "owner2",
                    rating = 4.0f,
                    totalReviews = 18,
                    isActive = true,
                    maxPlayers = 4,
                ),
            ),
        )
    }

    var showAddEditDialog by remember { mutableStateOf(false) }
    var selectedVenue by remember { mutableStateOf<Court?>(null) }

    // Lấy thời gian hiện tại để chào
    val greeting = remember {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        when (hour) {
            in 0..11 -> "Chào buổi sáng"
            in 12..17 -> "Chào buổi chiều"
            else -> "Chào buổi tối"
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF8BB1F6), // Mid Blue
                        Color.White,
                    ),
                ),
            ),
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = bottomPadding),
        ) {
            item {
                // Header content
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top,
                    ) {
                        // Avatar và thông tin chủ sân
                        Row(
                            modifier = Modifier.weight(1f),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            // Avatar chủ sân
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .background(
                                        brush = Brush.linearGradient(
                                            colors = listOf(
                                                Color(0xFF123E62),
                                                Color(0xFF4A90E2),
                                            ),
                                        ),
                                        shape = CircleShape,
                                    ),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = ownerName.split(" ").takeLast(2).joinToString("") { it.first().toString() },
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                )
                            }

                            Column(
                                modifier = Modifier.weight(1f),
                            ) {
                                Text(
                                    text = "$greeting,",
                                    fontSize = 14.sp,
                                    color = Color(0xFF123E62), // Dark Blue
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = ownerName,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF123E62), // Dark Blue
                                )
                            }
                        }

                        // Icon thông báo với badge
                        Box {
                            IconButton(
                                onClick = { },
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(
                                        color = Color(0xFF123E62).copy(alpha = 0.2f),
                                        shape = CircleShape,
                                    ),
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Notifications,
                                    contentDescription = "Thông báo",
                                    tint = Color(0xFF123E62), // Dark Blue
                                    modifier = Modifier.size(24.dp),
                                )
                            }
                            Badge(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(top = 4.dp, end = 4.dp),
                                containerColor = Color(0xFFB71C1C),
                                contentColor = Color.White,
                            ) {
                                Text(
                                    text = "29",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                        }
                    }
                }
            }

            item {
                // Tiêu đề "Sân của bạn" với nút Tạo sân
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Sân của bạn",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                    )

                    CreateCourtButton(
                        onClick = {
                            selectedVenue = null
                            showAddEditDialog = true
                        },
                    )
                }
            }

            item {
                // Box tìm kiếm
                var searchQuery by remember { mutableStateOf("") }

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    placeholder = { Text("Tìm kiếm sân...") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Tìm kiếm",
                            tint = Color.Gray,
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Xóa",
                                    tint = Color.Gray,
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF123E62),
                        unfocusedBorderColor = Color.LightGray,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                    ),
                )
            }

            // Danh sách sân
            if (venues.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(40.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                modifier = Modifier.size(80.dp),
                                tint = Color.Gray,
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Chưa có sân nào",
                                fontSize = 18.sp,
                                color = Color.Gray,
                                fontWeight = FontWeight.Medium,
                            )
                        }
                    }
                }
            } else {
                items(venues) { venue ->
                    VenueCard(
                        venue = venue,
                        onClick = { onNavigateToCourtDetail(venue.id) },
                        onEditClick = {
                            selectedVenue = venue
                            showAddEditDialog = true
                        },
                        onDeleteClick = {
                            venues = venues.filter { it.id != venue.id }
                        },
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }

    // Dialog thêm/sửa sân
    if (showAddEditDialog) {
        AddEditVenueDialog(
            venue = selectedVenue,
            onDismiss = {
                showAddEditDialog = false
                selectedVenue = null
            },
            onSave = { venue: Court ->
                if (selectedVenue == null) {
                    venues = venues + venue
                } else {
                    venues = venues.map { existingVenue ->
                        if (existingVenue.id == venue.id) venue else existingVenue
                    }
                }
                showAddEditDialog = false
                selectedVenue = null
            },
        )
    }
}

@Composable
private fun VenueCard(
    venue: Court,
    onClick: () -> Unit = {},
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    // Tạo màu ngẫu nhiên cho mỗi card: cam, tím, xanh lá, vàng, đỏ
    val cardColors = remember {
        listOf(
            Color(0xFFFF6F3C), // Cam
            Color(0xFFAB47BC), // Tím
            Color(0xFF66BB6A), // Xanh lá
            Color(0xFFFFA726), // Vàng
            Color(0xFFEF5350), // Đỏ
        )
    }
    val cardColor = remember { cardColors.random() }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White,
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp,
        ),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
        ) {
            // 1/3 trên - Hình ảnh minh họa
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                cardColor,
                                cardColor.copy(alpha = 0.8f),
                            ),
                        ),
                        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                    ),
            ) {
                // Nút Edit và Delete ở góc trên phải
                Row(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    IconButton(
                        onClick = onEditClick,
                        modifier = Modifier.size(36.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Chỉnh sửa",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp),
                        )
                    }

                    IconButton(
                        onClick = { showDeleteDialog = true },
                        modifier = Modifier.size(36.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Xóa",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }
            }

            // 2/3 dưới - Nội dung sân
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
            ) {
                // Logo và tên sân
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .background(
                                color = Color(0xFF123E62).copy(alpha = 0.1f),
                                shape = CircleShape,
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Logo sân",
                            tint = Color(0xFF123E62),
                            modifier = Modifier.size(30.dp),
                        )
                    }

                    Column {
                        Text(
                            text = venue.name,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black,
                        )

                        Spacer(modifier = Modifier.height(2.dp))

                        Text(
                            text = "Sức chứa: ${venue.maxPlayers} người",
                            fontSize = 14.sp,
                            color = Color.Gray,
                            fontWeight = FontWeight.Medium,
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Thông tin chi tiết
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Row(
                        verticalAlignment = Alignment.Top,
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = Color(0xFF123E62),
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = venue.address,
                            fontSize = 14.sp,
                            color = Color.DarkGray,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = Color(0xFFFFA500),
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${venue.rating} (${venue.totalReviews} đánh giá)",
                            fontSize = 14.sp,
                            color = Color.DarkGray,
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = null,
                            tint = Color(0xFF123E62),
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "⏰ ${venue.openTime} - ${venue.closeTime}",
                            fontSize = 14.sp,
                            color = Color.DarkGray,
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Thông tin trạng thái
                Row(
                    modifier = Modifier
                        .background(
                            color = Color(0xFF4CAF50).copy(alpha = 0.1f),
                            shape = RoundedCornerShape(8.dp),
                        )
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = Color(0xFF4CAF50),
                        modifier = Modifier.size(16.dp),
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Đang hoạt động",
                        fontSize = 13.sp,
                        color = Color(0xFF4CAF50),
                        fontWeight = FontWeight.Medium,
                    )
                }
            }
        }
    }

    // Confirm delete dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Xác nhận xóa") },
            text = { Text("Bạn có chắc chắn muốn xóa sân \"${venue.name}\"?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteClick()
                        showDeleteDialog = false
                    },
                ) {
                    Text("Xóa", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteDialog = false },
                ) {
                    Text("Hủy")
                }
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddEditVenueDialog(
    venue: Court?,
    onDismiss: () -> Unit,
    onSave: (Court) -> Unit,
) {
    var venueId by remember { mutableStateOf(venue?.id ?: "") }
    var venueName by remember { mutableStateOf(venue?.name ?: "") }
    var venueAddress by remember { mutableStateOf(venue?.address ?: "") }
    var venueDescription by remember { mutableStateOf(venue?.description ?: "") }
    var openTime by remember { mutableStateOf(venue?.openTime?.toString() ?: "05:00") }
    var closeTime by remember { mutableStateOf(venue?.closeTime?.toString() ?: "23:00") }
    var subCourtCount by remember { mutableStateOf(venue?.maxPlayers?.toString() ?: "1") }
    var imageUrl by remember { mutableStateOf(venue?.images?.firstOrNull() ?: "") }
    var pricePerHour by remember { mutableStateOf(venue?.pricePerHour?.toString() ?: "0") }

    val primaryColor = Color(0xFF123E62)
    val isEditMode = venue != null

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White,
            ),
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 600.dp)
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                item {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = if (isEditMode) "Chỉnh Sửa Sân" else "Thêm Sân Mới",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = primaryColor,
                        )
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.size(32.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Đóng",
                                tint = Color.Gray,
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                }

                item {
                    // ID Sân
                    OutlinedTextField(
                        value = venueId,
                        onValueChange = { venueId = it },
                        label = { Text("ID Sân", fontSize = 14.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isEditMode,
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = primaryColor,
                            focusedLabelColor = primaryColor,
                        ),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = primaryColor,
                                modifier = Modifier.size(20.dp),
                            )
                        },
                    )
                }

                item {
                    // Tên sân
                    OutlinedTextField(
                        value = venueName,
                        onValueChange = { venueName = it },
                        label = { Text("Tên Sân", fontSize = 14.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = primaryColor,
                            focusedLabelColor = primaryColor,
                        ),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Home,
                                contentDescription = null,
                                tint = primaryColor,
                                modifier = Modifier.size(20.dp),
                            )
                        },
                    )
                }

                item {
                    // Địa chỉ
                    OutlinedTextField(
                        value = venueAddress,
                        onValueChange = { venueAddress = it },
                        label = { Text("Địa Chỉ", fontSize = 14.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 2,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = primaryColor,
                            focusedLabelColor = primaryColor,
                        ),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = primaryColor,
                                modifier = Modifier.size(20.dp),
                            )
                        },
                    )
                }

                item {
                    // Mô tả
                    OutlinedTextField(
                        value = venueDescription,
                        onValueChange = { venueDescription = it },
                        label = { Text("Mô tả", fontSize = 14.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = false,
                        maxLines = 2,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = primaryColor,
                            focusedLabelColor = primaryColor,
                        ),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = primaryColor,
                                modifier = Modifier.size(20.dp),
                            )
                        },
                    )
                }

                item {
                    // Giờ mở cửa, đóng cửa và Số sân con (3 cột)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        OutlinedTextField(
                            value = openTime,
                            onValueChange = { openTime = it },
                            label = { Text("Giờ Mở", fontSize = 12.sp) },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            placeholder = { Text("HH:MM", fontSize = 12.sp) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = primaryColor,
                                focusedLabelColor = primaryColor,
                            ),
                        )

                        OutlinedTextField(
                            value = closeTime,
                            onValueChange = { closeTime = it },
                            label = { Text("Giờ Đóng", fontSize = 12.sp) },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            placeholder = { Text("HH:MM", fontSize = 12.sp) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = primaryColor,
                                focusedLabelColor = primaryColor,
                            ),
                        )

                        OutlinedTextField(
                            value = subCourtCount,
                            onValueChange = { subCourtCount = it },
                            label = { Text("Số Sân", fontSize = 12.sp) },
                            modifier = Modifier.weight(0.8f),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = primaryColor,
                                focusedLabelColor = primaryColor,
                            ),
                        )
                    }
                }

                item {
                    // Giá
                    OutlinedTextField(
                        value = pricePerHour,
                        onValueChange = { pricePerHour = it },
                        label = { Text("Giá/giờ (VNĐ)", fontSize = 14.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = primaryColor,
                            focusedLabelColor = primaryColor,
                        ),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.ShoppingCart,
                                contentDescription = null,
                                tint = primaryColor,
                                modifier = Modifier.size(20.dp),
                            )
                        },
                    )
                }

                item {
                    // Hình ảnh sân
                    OutlinedTextField(
                        value = imageUrl,
                        onValueChange = { imageUrl = it },
                        label = { Text("Hình Ảnh Sân (URL)", fontSize = 14.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = primaryColor,
                            focusedLabelColor = primaryColor,
                        ),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.AccountBox,
                                contentDescription = null,
                                tint = primaryColor,
                                modifier = Modifier.size(20.dp),
                            )
                        },
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(8.dp))

                    // Action buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, Color.Gray),
                        ) {
                            Text("Hủy", color = Color.Gray, fontSize = 14.sp)
                        }

                        Button(
                            onClick = {
                                if (venueId.isNotBlank() && venueName.isNotBlank() &&
                                    venueAddress.isNotBlank() && venueDescription.isNotBlank() &&
                                    openTime.isNotBlank() && closeTime.isNotBlank()
                                ) {
                                    val openTimeParts = openTime.split(":")
                                    val closeTimeParts = closeTime.split(":")

                                    onSave(
                                        Court(
                                            id = venueId,
                                            name = venueName,
                                            description = venueDescription,
                                            address = venueAddress,
                                            latitude = 21.0,
                                            longitude = 105.0,
                                            images = if (imageUrl.isNotBlank()) listOf(imageUrl) else emptyList(),
                                            sportType = SportType.BADMINTON,
                                            courtType = CourtType.INDOOR,
                                            pricePerHour = pricePerHour.toLongOrNull() ?: 0,
                                            openTime = LocalTime(
                                                openTimeParts.getOrNull(0)?.toIntOrNull() ?: 5,
                                                openTimeParts.getOrNull(1)?.toIntOrNull() ?: 0,
                                            ),
                                            closeTime = LocalTime(
                                                closeTimeParts.getOrNull(0)?.toIntOrNull() ?: 23,
                                                closeTimeParts.getOrNull(1)?.toIntOrNull() ?: 0,
                                            ),
                                            amenities = emptyList(),
                                            rules = null,
                                            ownerId = "owner1",
                                            rating = 0f,
                                            totalReviews = 0,
                                            isActive = true,
                                            maxPlayers = subCourtCount.toIntOrNull() ?: 1,
                                        ),
                                    )
                                }
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = primaryColor,
                            ),
                            shape = RoundedCornerShape(10.dp),
                            enabled = venueId.isNotBlank() && venueName.isNotBlank() &&
                                venueAddress.isNotBlank() && venueDescription.isNotBlank() &&
                                openTime.isNotBlank() && closeTime.isNotBlank(),
                        ) {
                            Text(
                                if (isEditMode) "Cập Nhật" else "Thêm Sân",
                                fontSize = 14.sp,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    BookingCourtTheme {
        HomeScreen()
    }
}

@Composable
private fun CreateCourtButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF123E62), // Màu DarkBlue
        ),
        shape = RoundedCornerShape(24.dp),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp),
        modifier = modifier,
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = "Tạo sân",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun AddEditVenueDialogPreview() {
    BookingCourtTheme {
        AddEditVenueDialog(
            venue = null,
            onDismiss = { },
            onSave = { },
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun EditVenueDialogPreview() {
    BookingCourtTheme {
        AddEditVenueDialog(
            venue = Court(
                id = "VN001",
                name = "Star Club Badminton",
                description = "Sân cầu lông chất lượng cao",
                address = "Số 181 P. Cầu Cốc, Tây Mỗ, Nam Từ Liêm, Hà Nội",
                latitude = 21.0159,
                longitude = 105.7447,
                images = emptyList(),
                sportType = SportType.BADMINTON,
                courtType = CourtType.INDOOR,
                pricePerHour = 150000,
                openTime = LocalTime(5, 0),
                closeTime = LocalTime(23, 0),
                amenities = emptyList(),
                rules = "Không hút thuốc trong sân",
                ownerId = "owner1",
                rating = 4.5f,
                totalReviews = 25,
                isActive = true,
                maxPlayers = 4,
            ),
            onDismiss = { },
            onSave = { },
        )
    }
}
