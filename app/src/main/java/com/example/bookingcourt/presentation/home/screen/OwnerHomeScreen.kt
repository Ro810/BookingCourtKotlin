package com.example.bookingcourt.presentation.home.screen
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.bookingcourt.domain.model.Court
import com.example.bookingcourt.domain.model.CourtType
import com.example.bookingcourt.domain.model.SportType
import com.example.bookingcourt.presentation.owner.viewmodel.OwnerHomeViewModel
import com.example.bookingcourt.presentation.profile.screen.ProfileScreen
import com.example.bookingcourt.presentation.theme.BookingCourtTheme
import com.example.bookingcourt.presentation.theme.Primary
import kotlinx.datetime.LocalTime

enum class HomeTab {
    HOME,
    PROFILE
}

@Composable
fun OwnerHomeScreen(
    onNavigateToCourtDetail: (String) -> Unit = {},
    onNavigateToEditProfile: () -> Unit = {},
    onNavigateToBecomeCustomer: () -> Unit = {},
    onNavigateToCreateVenue: () -> Unit = {},
    onLogout: () -> Unit = {},
    viewModel: OwnerHomeViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    var selectedTab by remember { mutableStateOf(HomeTab.HOME) }

    // Snackbar host for error messages
    val snackbarHostState = remember { SnackbarHostState() }

    // Show error message if any
    LaunchedEffect(state.error) {
        state.error?.let { error ->
            snackbarHostState.showSnackbar(
                message = error,
                duration = SnackbarDuration.Short
            )
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        snackbarHost = { SnackbarHost(snackbarHostState) },
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
                    onNavigateToCourtDetail = onNavigateToCourtDetail,
                    onNavigateToCreateVenue = onNavigateToCreateVenue,
                    bottomPadding = paddingValues.calculateBottomPadding(),
                )
            }
            HomeTab.PROFILE -> {
                ProfileScreen(
                    onNavigateBack = { selectedTab = HomeTab.HOME },
                    onNavigateToEditProfile = onNavigateToEditProfile,
                    onNavigateToBecomeCustomer = onNavigateToBecomeCustomer,
                    onLogout = onLogout,
                    showBackButton = false,
                    showTopBar = false,
                    bottomPadding = paddingValues.calculateBottomPadding(),
                    isOwnerMode = true, // QUAN TRỌNG: Báo cho ProfileScreen biết đang ở chế độ OWNER
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OwnerHomeContent(
    onNavigateToCourtDetail: (String) -> Unit,
    onNavigateToCreateVenue: () -> Unit,
    bottomPadding: androidx.compose.ui.unit.Dp,
    viewModel: OwnerHomeViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()

    // State for edit dialog
    var showEditDialog by remember { mutableStateOf(false) }
    var venueToEdit by remember { mutableStateOf<com.example.bookingcourt.domain.model.Venue?>(null) }

    // Lấy venues từ ViewModel thay vì hardcode
    val venues = state.venues

    // Show success message when update succeeds
    LaunchedEffect(state.updateSuccess) {
        if (state.updateSuccess) {
            // Show success and hide dialog
            showEditDialog = false
            venueToEdit = null
            viewModel.clearUpdateSuccess()
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
                        .statusBarsPadding()
                        .padding(start = 16.dp, end = 18.dp, top = 8.dp, bottom = 16.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        // Avatar và thông tin người dùng
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            // Avatar
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.3f))
                            )

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Text(
                                    text = "Xin chào, ${state.currentUser?.fullName ?: "Người dùng"}",
                                    color = Color.Black,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Icon thông báo
                        IconButton(
                            onClick = { },
                            modifier = Modifier
                                .size(36.dp)
                                .background(Color.White, CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Thông báo",
                                tint = Primary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Search Bar
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .clickable { /* TODO: Navigate to search */ },
                        shape = RoundedCornerShape(28.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Tìm kiếm",
                                tint = Color(0xFF123E62)
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            Text(
                                text = "Tìm kiếm sân của bạn...",
                                color = Color.Gray,
                                fontSize = 16.sp
                            )
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
                            onNavigateToCreateVenue()
                        },
                    )
                }
            }

            // Hiển thị loading khi đang tải venues
            if (state.isLoadingVenues) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(40.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator(
                            color = Color(0xFF123E62)
                        )
                    }
                }
            } else if (venues.isEmpty()) {
                // Danh sách sân trống
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
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Hãy tạo sân đầu tiên của bạn!",
                                fontSize = 14.sp,
                                color = Color.Gray,
                            )
                        }
                    }
                }
            } else {
                // Hiển thị danh sách venues từ API
                items(venues.size) { index ->
                    val venue = venues[index]
                    VenueCardFromVenue(
                        venue = venue,
                        onClick = {
                            // TODO: Navigate to venue detail
                        },
                        onEditClick = {
                            venueToEdit = venue
                            showEditDialog = true
                        },
                        onDeleteClick = {
                            viewModel.deleteVenue(venue.id)
                        },
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }

    // Edit venue dialog
    if (showEditDialog && venueToEdit != null) {
        EditVenueDialog(
            venue = venueToEdit!!,
            isLoading = state.isUpdatingVenue,
            onDismiss = {
                showEditDialog = false
                venueToEdit = null
            },
            onSave = { updatedVenue ->
                viewModel.updateVenue(
                    venueId = updatedVenue.id,
                    name = updatedVenue.name,
                    description = updatedVenue.description,
                    phoneNumber = updatedVenue.phoneNumber ?: "",
                    email = updatedVenue.email ?: "",
                    provinceOrCity = updatedVenue.address.provinceOrCity,
                    district = updatedVenue.address.district,
                    detailAddress = updatedVenue.address.detailAddress,
                    pricePerHour = updatedVenue.pricePerHour.toDouble(),
                    openingTime = updatedVenue.openingTime,
                    closingTime = updatedVenue.closingTime,
                    images = updatedVenue.images
                )
            }
        )
    }
}
@Composable
private fun VenueCard(
    venue: Court, // Đã đổi tên biến từ venue thành court cho khớp data class
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
            // 1/3 trên - Hình ảnh minh họa và nút
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
                    ) // Dấu ')' của background bị thiếu ở đây
            ) { // Dấu '{' của Box bị thiếu ở đây
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
            } // Đóng Box chứa hình ảnh và nút

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
                            imageVector = Icons.Default.Star, // Đã đổi thành Star cho khác biệt
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
                } // Đóng Row chứa Logo và tên sân

                Spacer(modifier = Modifier.height(16.dp))

                // Thông tin chi tiết
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    // Địa chỉ
                    Row(verticalAlignment = Alignment.Top) {
                        Icon(Icons.Default.LocationOn, null, tint = Color(0xFF123E62), modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(venue.address, fontSize = 14.sp, color = Color.DarkGray, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    }

                    // Mô tả
                    if (venue.description.isNotBlank()) {
                        Row(verticalAlignment = Alignment.Top) {
                            Icon(Icons.Default.Info, null, tint = Color(0xFF123E62), modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(venue.description, fontSize = 14.sp, color = Color.DarkGray, maxLines = 2, overflow = TextOverflow.Ellipsis)
                        }
                    }
                } // Đóng Column chứa thông tin chi tiết

                Spacer(modifier = Modifier.height(12.dp))

                // Thông tin trạng thái
                Row(
                    modifier = Modifier
                        .background(
                            color = if (venue.isActive) Color(0xFF4CAF50).copy(alpha = 0.1f) else Color.Gray.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(8.dp),
                        )
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle, // Đổi icon cho trạng thái
                        contentDescription = null,
                        tint = if (venue.isActive) Color(0xFF4CAF50) else Color.Gray,
                        modifier = Modifier.size(16.dp),
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (venue.isActive) "Đang hoạt động" else "Ngừng hoạt động",
                        fontSize = 13.sp,
                        color = if (venue.isActive) Color(0xFF4CAF50) else Color.Gray,
                        fontWeight = FontWeight.Medium,
                    )
                } // Đóng Row chứa thông tin trạng thái
            } // Đóng Column chứa nội dung 2/3 dưới
        } // Đóng Column chính trong Card
    } // Đóng Card

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
    } // Đóng if cho AlertDialog
} // Đóng Composable VenueCard

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
                .fillMaxHeight(0.9f)
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White,
            ),
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Header - không scroll
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
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

                // Form fields - scrollable
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
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
                            label = { Text("Hình ảnh Sân (URL)", fontSize = 14.sp) },
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
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }

                // Action buttons - cố định ở dưới, không scroll
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f).height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.5.dp, Color.Gray),
                    ) {
                        Text(
                            "Hủy",
                            color = Color.Gray,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium
                        )
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
                        modifier = Modifier.weight(1f).height(50.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = primaryColor,
                        ),
                        shape = RoundedCornerShape(12.dp),
                        enabled = venueId.isNotBlank() && venueName.isNotBlank() &&
                            venueAddress.isNotBlank() && venueDescription.isNotBlank() &&
                            openTime.isNotBlank() && closeTime.isNotBlank(),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            "Hoàn thành",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
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
            containerColor = Color(0xFF123E62),
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

// Composable để hiển thị Venue từ API
@Composable
private fun VenueCardFromVenue(
    venue: com.example.bookingcourt.domain.model.Venue,
    onClick: () -> Unit = {},
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    val cardColors = remember {
        listOf(
            Color(0xFFFF6F3C), Color(0xFFAB47BC), Color(0xFF66BB6A),
            Color(0xFFFFA726), Color(0xFFEF5350),
        )
    }
    val cardColor = remember { cardColors.random() }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Box chứa hình nền và các nút
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(cardColor, cardColor.copy(alpha = 0.8f)),
                        ),
                        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                    ) // Dấu ')' của background ở đây
                // Khối lệnh lambda cho nội dung Box phải bắt đầu ở đây
            ) { // <- Dấu '{' bị thiếu đã được thêm vào đúng vị trí
                Row(
                    modifier = Modifier.align(Alignment.TopEnd).padding(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    IconButton(onClick = onEditClick, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Default.Edit, "Chỉnh sửa", tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                    IconButton(onClick = { showDeleteDialog = true }, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Default.Delete, "Xóa", tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                }
            } // <- Dấu '}' đóng Box

            // Phần nội dung bên dưới
            Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
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
                            text = "Số sân: ${venue.courtsCount}",
                            fontSize = 14.sp,
                            color = Color.Gray,
                            fontWeight = FontWeight.Medium,
                        )
                    }
                } // Đóng Row logo và tên

                Spacer(modifier = Modifier.height(16.dp))

                // Thông tin chi tiết
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    // Địa chỉ
                    Row(verticalAlignment = Alignment.Top) {
                        Icon(Icons.Default.LocationOn, null, tint = Color(0xFF123E62), modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(venue.address.getFullAddress(), fontSize = 14.sp, color = Color.DarkGray, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    }

                    // Số điện thoại (ưu tiên ownerPhone, fallback về phoneNumber)
                    val displayPhone = venue.ownerPhone ?: venue.phoneNumber
                    if (!displayPhone.isNullOrBlank()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Phone, null, tint = Color(0xFF123E62), modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(displayPhone, fontSize = 14.sp, color = Color.DarkGray)
                        }
                    }

                    // Email
                    if (!venue.email.isNullOrBlank()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Email, null, tint = Color(0xFF123E62), modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(venue.email, fontSize = 14.sp, color = Color.DarkGray, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                } // Đóng Column thông tin chi tiết

                Spacer(modifier = Modifier.height(12.dp))

                // Thông tin trạng thái (Giả định luôn hoạt động)
                Row(
                    modifier = Modifier
                        .background(Color(0xFF4CAF50).copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF4CAF50), modifier = Modifier.size(16.dp)) // Đổi icon
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Đang hoạt động", fontSize = 13.sp, color = Color(0xFF4CAF50), fontWeight = FontWeight.Medium)
                } // Đóng Row trạng thái
            } // Đóng Column nội dung chính
        } // Đóng Column trong Card
    } // Đóng Card

    // Dialog xác nhận xóa
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Xác nhận xóa") },
            // Lưu ý: Biến đầu vào là 'venue' nhưng text lại ghi 'sân' (court)
            text = { Text("Bạn có chắc chắn muốn xóa sân \"${venue.name}\"?") },
            confirmButton = {
                TextButton(onClick = { onDeleteClick(); showDeleteDialog = false }) {
                    Text("Xóa", color = Color.Red)
                }
            },
            dismissButton = { TextButton(onClick = { showDeleteDialog = false }) { Text("Hủy") } }
        )
    } // Đóng if dialog
} // Đóng Composable
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditVenueDialog(
    venue: com.example.bookingcourt.domain.model.Venue,
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onSave: (com.example.bookingcourt.domain.model.Venue) -> Unit,
) {
    var venueName by remember { mutableStateOf(venue.name) }
    var description by remember { mutableStateOf(venue.description ?: "") }
    var phoneNumber by remember { mutableStateOf(venue.phoneNumber ?: "") }
    var email by remember { mutableStateOf(venue.email ?: "") }
    var provinceOrCity by remember { mutableStateOf(venue.address.provinceOrCity) }
    var district by remember { mutableStateOf(venue.address.district) }
    var detailAddress by remember { mutableStateOf(venue.address.detailAddress) }
    var pricePerHour by remember { mutableStateOf(venue.pricePerHour.toString()) }

    // Parse opening/closing time để lấy giờ và phút
    val (openHour, openMinute) = parseTime(venue.openingTime ?: "06:00")
    val (closeHour, closeMinute) = parseTime(venue.closingTime ?: "23:00")

    var openingHour by remember { mutableStateOf(openHour) }
    var openingMinute by remember { mutableStateOf(openMinute) }
    var closingHour by remember { mutableStateOf(closeHour) }
    var closingMinute by remember { mutableStateOf(closeMinute) }
    var showOpeningTimePicker by remember { mutableStateOf(false) }
    var showClosingTimePicker by remember { mutableStateOf(false) }

    var imageUrls by remember { mutableStateOf(venue.images ?: emptyList()) }
    var newImageUrl by remember { mutableStateOf("") }
    var showAddImageDialog by remember { mutableStateOf(false) }
    var numberOfCourts by remember { mutableStateOf(venue.courtsCount.toString()) }

    val primaryColor = Color(0xFF123E62)

    Dialog(onDismissRequest = { if (!isLoading) onDismiss() }) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White,
            ),
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Chỉnh Sửa Sân",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = primaryColor,
                    )
                    if (!isLoading) {
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
                }

                // Form fields
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 400.dp)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    // Tên sân
                    OutlinedTextField(
                        value = venueName,
                        onValueChange = { venueName = it },
                        label = { Text("Tên Sân *", fontSize = 14.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        enabled = !isLoading,
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

                    // Mô tả
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Mô tả", fontSize = 14.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 2,
                        enabled = !isLoading,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = primaryColor,
                            focusedLabelColor = primaryColor,
                        ),
                    )

                    // Số điện thoại
                    OutlinedTextField(
                        value = phoneNumber,
                        onValueChange = { phoneNumber = it },
                        label = { Text("Số điện thoại *", fontSize = 14.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        enabled = !isLoading,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = primaryColor,
                            focusedLabelColor = primaryColor,
                        ),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Phone,
                                contentDescription = null,
                                tint = primaryColor,
                                modifier = Modifier.size(20.dp),
                            )
                        },
                    )

                    // Email
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email *", fontSize = 14.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        enabled = !isLoading,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = primaryColor,
                            focusedLabelColor = primaryColor,
                        ),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Email,
                                contentDescription = null,
                                tint = primaryColor,
                                modifier = Modifier.size(20.dp),
                            )
                        },
                    )

                    // Tỉnh/Thành phố
                    OutlinedTextField(
                        value = provinceOrCity,
                        onValueChange = { provinceOrCity = it },
                        label = { Text("Tỉnh/Thành phố", fontSize = 14.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        enabled = !isLoading,
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

                    // Quận/Huyện
                    OutlinedTextField(
                        value = district,
                        onValueChange = { district = it },
                        label = { Text("Quận/Huyện", fontSize = 14.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        enabled = !isLoading,
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

                    // Địa chỉ chi tiết
                    OutlinedTextField(
                        value = detailAddress,
                        onValueChange = { detailAddress = it },
                        label = { Text("Địa chỉ chi tiết *", fontSize = 14.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 2,
                        enabled = !isLoading,
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

                    // Giá/1 giờ
                    OutlinedTextField(
                        value = pricePerHour,
                        onValueChange = { pricePerHour = it },
                        label = { Text("Giá/1 giờ (VNĐ)", fontSize = 14.sp) },
                        placeholder = { Text("100000", fontSize = 14.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        enabled = !isLoading,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = primaryColor,
                            focusedLabelColor = primaryColor,
                        ),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = primaryColor,
                                modifier = Modifier.size(20.dp),
                            )
                        },
                    )

                    // Giờ hoạt động
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Giờ mở cửa
                        OutlinedCard(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, if (!isLoading) primaryColor.copy(alpha = 0.5f) else Color.Gray),
                            colors = CardDefaults.outlinedCardColors(
                                containerColor = Color.Transparent
                            ),
                            onClick = { if (!isLoading) showOpeningTimePicker = true }
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    "Giờ mở cửa",
                                    fontSize = 12.sp,
                                    color = primaryColor,
                                    fontWeight = FontWeight.Medium
                                )
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        Icons.Default.DateRange,
                                        contentDescription = null,
                                        tint = primaryColor,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text(
                                        String.format("%02d:%02d", openingHour, openingMinute),
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Black
                                    )
                                }
                            }
                        }

                        // Giờ đóng cửa
                        OutlinedCard(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, if (!isLoading) primaryColor.copy(alpha = 0.5f) else Color.Gray),
                            colors = CardDefaults.outlinedCardColors(
                                containerColor = Color.Transparent
                            ),
                            onClick = { if (!isLoading) showClosingTimePicker = true }
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    "Giờ đóng cửa",
                                    fontSize = 12.sp,
                                    color = primaryColor,
                                    fontWeight = FontWeight.Medium
                                )
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        Icons.Default.DateRange,
                                        contentDescription = null,
                                        tint = primaryColor,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text(
                                        String.format("%02d:%02d", closingHour, closingMinute),
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Black
                                    )
                                }
                            }
                        }
                    }

                    // Số lượng sân con
                    OutlinedTextField(
                        value = numberOfCourts,
                        onValueChange = {
                            // Chỉ cho phép nhập số
                            if (it.isEmpty() || it.all { char -> char.isDigit() }) {
                                numberOfCourts = it
                            }
                        },
                        label = { Text("Số lượng sân", fontSize = 14.sp) },
                        placeholder = { Text("Nhập số lượng sân", fontSize = 14.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        enabled = !isLoading,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
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
                        supportingText = {
                            Text(
                                "Số lượng sân con (hiển thị cho khách hàng)",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }
                    )

                    // Images section
                    if (imageUrls.isNotEmpty()) {
                        Text(
                            "Hình ảnh sân (${imageUrls.size})",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = primaryColor
                        )
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            imageUrls.forEachIndexed { index, url ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = Color(0xFFF5F5F5)
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(8.dp),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Image preview
                                        Box(
                                            modifier = Modifier
                                                .size(60.dp)
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(Color(0xFFF0F0F0))
                                        ) {
                                            AsyncImage(
                                                model = url,
                                                contentDescription = "Ảnh ${index + 1}",
                                                modifier = Modifier.fillMaxSize(),
                                                contentScale = ContentScale.Crop
                                            )
                                        }

                                        // URL text
                                        Text(
                                            text = url,
                                            fontSize = 11.sp,
                                            maxLines = 2,
                                            color = Color.DarkGray,
                                            modifier = Modifier.weight(1f)
                                        )

                                        // Delete button
                                        IconButton(
                                            onClick = { imageUrls = imageUrls.filterIndexed { i, _ -> i != index } },
                                            enabled = !isLoading
                                        ) {
                                            Icon(
                                                Icons.Default.Delete,
                                                contentDescription = "Xóa",
                                                tint = if (isLoading) Color.Gray else Color.Red,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Add image button
                    OutlinedButton(
                        onClick = { showAddImageDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isLoading,
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = primaryColor
                        )
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Thêm ảnh", fontSize = 13.sp)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Action buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f).height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.5.dp, Color.Gray),
                        enabled = !isLoading,
                    ) {
                        Text(
                            "Hủy",
                            color = if (isLoading) Color.Gray.copy(alpha = 0.5f) else Color.Gray,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Button(
                        onClick = {
                            val updatedVenue = venue.copy(
                                name = venueName.trim(),
                                description = description.trim().takeIf { it.isNotEmpty() },
                                phoneNumber = phoneNumber.trim().takeIf { it.isNotEmpty() },
                                email = email.trim().takeIf { it.isNotEmpty() },
                                address = venue.address.copy(
                                    provinceOrCity = provinceOrCity.trim(),
                                    district = district.trim(),
                                    detailAddress = detailAddress.trim()
                                ),
                                pricePerHour = pricePerHour.trim().toLongOrNull() ?: venue.pricePerHour,
                                openingTime = String.format("%02d:%02d", openingHour, openingMinute),
                                closingTime = String.format("%02d:%02d", closingHour, closingMinute),
                                images = imageUrls.takeIf { it.isNotEmpty() },
                                courtsCount = numberOfCourts.trim().toIntOrNull() ?: venue.courtsCount,
                                numberOfCourt = numberOfCourts.trim().toIntOrNull() ?: venue.numberOfCourt
                            )
                            onSave(updatedVenue)
                        },
                        modifier = Modifier.weight(1f).height(50.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = primaryColor,
                        ),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !isLoading && venueName.isNotBlank() &&
                            provinceOrCity.isNotBlank() && district.isNotBlank() &&
                            detailAddress.isNotBlank(),
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "Lưu",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }

    // Add Image Dialog
    if (showAddImageDialog) {
        AlertDialog(
            onDismissRequest = { showAddImageDialog = false },
            title = { Text("Thêm URL hình ảnh", color = primaryColor, fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = newImageUrl,
                    onValueChange = { newImageUrl = it },
                    label = { Text("URL hình ảnh", fontSize = 14.sp) },
                    placeholder = { Text("https://example.com/image.jpg", fontSize = 12.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = primaryColor,
                        focusedLabelColor = primaryColor,
                    )
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newImageUrl.isNotBlank()) {
                            imageUrls = imageUrls + newImageUrl.trim()
                            newImageUrl = ""
                            showAddImageDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
                ) {
                    Text("Thêm")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showAddImageDialog = false }) {
                    Text("Hủy", color = Color.Gray)
                }
            }
        )
    }

    // Opening Time Picker Dialog
    if (showOpeningTimePicker) {
        TimePickerDialog(
            title = "Chọn giờ mở cửa",
            initialHour = openingHour,
            initialMinute = openingMinute,
            onConfirm = { hour, minute ->
                openingHour = hour
                openingMinute = minute
                showOpeningTimePicker = false
            },
            onDismiss = { showOpeningTimePicker = false }
        )
    }

    // Closing Time Picker Dialog
    if (showClosingTimePicker) {
        TimePickerDialog(
            title = "Chọn giờ đóng cửa",
            initialHour = closingHour,
            initialMinute = closingMinute,
            onConfirm = { hour, minute ->
                closingHour = hour
                closingMinute = minute
                showClosingTimePicker = false
            },
            onDismiss = { showClosingTimePicker = false }
        )
    }
}

// Helper function to parse time string
private fun parseTime(timeString: String): Pair<Int, Int> {
    return try {
        val parts = timeString.split(":")
        if (parts.size == 2) {
            val hour = parts[0].toIntOrNull() ?: 0
            val minute = parts[1].toIntOrNull() ?: 0
            Pair(hour, minute)
        } else {
            // Chỉ có giờ, không có phút
            val hour = timeString.toIntOrNull() ?: 0
            Pair(hour, 0)
        }
    } catch (e: Exception) {
        Pair(0, 0)
    }
}

// Time Picker Dialog Component
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimePickerDialog(
    title: String,
    initialHour: Int,
    initialMinute: Int,
    onConfirm: (Int, Int) -> Unit,
    onDismiss: () -> Unit
) {
    val timePickerState = rememberTimePickerState(
        initialHour = initialHour,
        initialMinute = initialMinute,
        is24Hour = true
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF123E62)
            )
        },
        text = {
            TimePicker(
                state = timePickerState,
                colors = TimePickerDefaults.colors(
                    clockDialColor = Color(0xFFE3F2FD),
                    selectorColor = Color(0xFF123E62),
                    timeSelectorSelectedContainerColor = Color(0xFF123E62),
                    timeSelectorUnselectedContainerColor = Color(0xFFE3F2FD),
                )
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(timePickerState.hour, timePickerState.minute)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF123E62)
                )
            ) {
                Text("Xác nhận")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Hủy", color = Color.Gray)
            }
        }
    )
}

// Helper function to get greeting message based on time of day
private fun getGreetingMessage(): String {
    val calendar = java.util.Calendar.getInstance()
    val hour = calendar.get(java.util.Calendar.HOUR_OF_DAY)

    return when (hour) {
        in 0..4 -> "Đêm khuya rồi"
        in 5..10 -> "Chào buổi sáng"
        in 11..12 -> "Chào buổi trưa"
        in 13..17 -> "Chào buổi chiều"
        in 18..21 -> "Chào buổi tối"
        else -> "Chúc ngủ ngon"
    }
}

@Preview(showBackground = true)
@Composable
fun OwnerHomeScreenPreview() {
    BookingCourtTheme {
        OwnerHomeScreen()
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
