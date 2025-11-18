package com.example.bookingcourt.presentation.home.screen
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.platform.LocalContext
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import com.example.bookingcourt.core.util.FileUtils
import com.example.bookingcourt.domain.model.Address
import com.example.bookingcourt.domain.model.Venue
import com.example.bookingcourt.domain.model.User
import com.example.bookingcourt.presentation.owner.viewmodel.OwnerHomeViewModel
import com.example.bookingcourt.presentation.profile.screen.ProfileScreen
import com.example.bookingcourt.presentation.theme.BookingCourtTheme
import com.example.bookingcourt.presentation.theme.Primary

enum class HomeTab {
    HOME,
    PROFILE
}

@Composable
fun OwnerHomeScreen(
    onNavigateToCourtDetail: (String) -> Unit = {},
    onNavigateToEditProfile: () -> Unit = {},
    onNavigateToChangePassword: () -> Unit = {},
    onNavigateToBecomeCustomer: () -> Unit = {},
    onNavigateToCreateVenue: () -> Unit = {},
    onNavigateToPendingBookings: () -> Unit = {},
    onNavigateToOwnerBookingHistory: () -> Unit = {}, // Navigation cho lịch sử booking chủ sân
    onLogout: () -> Unit = {},
    viewModel: OwnerHomeViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    var selectedTab by remember { mutableStateOf(HomeTab.HOME) }

    // ✅ Thêm state để track số lượng pending bookings
    val pendingBookingsCount by viewModel.pendingBookingsCount.collectAsState()

    // Snackbar host for error messages
    val snackbarHostState = remember { SnackbarHostState() }

    // ✅ Load pending bookings count khi screen được tạo
    LaunchedEffect(Unit) {
        viewModel.loadPendingBookingsCount()
    }

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
                    snackbarHostState = snackbarHostState,
                    onNavigateToPendingBookings = onNavigateToPendingBookings,
                    pendingBookingsCount = pendingBookingsCount // ✅ Pass count
                )
            }
            HomeTab.PROFILE -> {
                ProfileScreen(
                    onNavigateBack = { selectedTab = HomeTab.HOME },
                    onNavigateToEditProfile = onNavigateToEditProfile,
                    onNavigateToChangePassword = onNavigateToChangePassword,
                    onNavigateToBecomeCustomer = onNavigateToBecomeCustomer,
                    onNavigateToBookingHistory = onNavigateToOwnerBookingHistory, // Navigate đến lịch sử booking cho owner
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
    snackbarHostState: SnackbarHostState,
    onNavigateToPendingBookings: () -> Unit, // New parameter for navigation
    pendingBookingsCount: Int = 0, // ✅ Add count parameter
    viewModel: OwnerHomeViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()

    // State for edit dialog
    var showEditDialog by remember { mutableStateOf(false) }
    var venueToEdit by remember { mutableStateOf<Venue?>(null) }

    // Lấy venues từ ViewModel thay vì hardcode
    val venues = state.venues

    // Pull-to-refresh state
    val swipeRefreshState = rememberSwipeRefreshState(isRefreshing = state.isRefreshing)

    // Show success message when update succeeds and venues reloaded
    LaunchedEffect(state.updateSuccess, state.isLoadingVenues) {
        if (state.updateSuccess && !state.isLoadingVenues) {
            // ✅ Update thành công VÀ venues đã reload xong → Đóng dialog
            showEditDialog = false
            venueToEdit = null
            viewModel.clearUpdateSuccess()
        }
    }

    // Show success message when delete succeeds
    LaunchedEffect(state.deleteSuccess) {
        if (state.deleteSuccess) {
            snackbarHostState.showSnackbar(
                message = "Đã xóa sân thành công",
                duration = SnackbarDuration.Short
            )
            viewModel.clearDeleteSuccess()
        }
    }

    SwipeRefresh(
        state = swipeRefreshState,
        onRefresh = { viewModel.refresh() },
    ) {
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
                                    .background(Color.White.copy(alpha = 0.3f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = getInitials(state.currentUser?.fullName),
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF123E62)
                                )
                            }

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

                    // Button "Booking chờ xác nhận" - NEW with count badge
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateToPendingBookings() },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFFF9800) // Orange color for attention
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // ✅ Icon with badge
                                Box {
                                    Icon(
                                        imageVector = Icons.Default.Notifications,
                                        contentDescription = "Booking chờ xác nhận",
                                        tint = Color.White,
                                        modifier = Modifier.size(32.dp)
                                    )
                                    // ✅ Badge hiển thị số lượng
                                    if (pendingBookingsCount > 0) {
                                        Box(
                                            modifier = Modifier
                                                .offset(x = 20.dp, y = (-4).dp)
                                                .size(20.dp)
                                                .background(Color.Red, CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = if (pendingBookingsCount > 9) "9+" else "$pendingBookingsCount",
                                                color = Color.White,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                                Column {
                                    Text(
                                        text = "Booking chờ xác nhận",
                                        color = Color.White,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = if (pendingBookingsCount > 0) {
                                            "$pendingBookingsCount booking cần xử lý"
                                        } else {
                                            "Xem và duyệt đặt sân"
                                        },
                                        color = Color.White.copy(alpha = 0.9f),
                                        fontSize = 13.sp
                                    )
                                }
                            }
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowRight,
                                contentDescription = "Xem chi tiết",
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
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
                            onNavigateToCourtDetail(venue.id.toString())
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
    }

    // Edit venue dialog
    if (showEditDialog && venueToEdit != null) {
        // Lấy venue mới nhất từ state.venues để đảm bảo UI được update sau khi delete ảnh
        val currentVenue = state.venues.find { it.id == venueToEdit!!.id } ?: venueToEdit!!

        EditVenueDialog(
            venue = currentVenue,
            currentUser = state.currentUser,
            isLoading = state.isUpdatingVenue,
            onDismiss = {
                showEditDialog = false
                venueToEdit = null
            },
            onSave = { updatedVenue, selectedNewImages ->
                viewModel.updateVenue(
                    venueId = updatedVenue.id,
                    name = updatedVenue.name,
                    description = updatedVenue.description,
                    phoneNumber = updatedVenue.phoneNumber ?: "",
                    email = updatedVenue.email ?: "",
                    numberOfCourt = updatedVenue.numberOfCourt,
                    provinceOrCity = updatedVenue.address.provinceOrCity,
                    district = updatedVenue.address.district,
                    detailAddress = updatedVenue.address.detailAddress,
                    pricePerHour = updatedVenue.pricePerHour.toDouble(),
                    openingTime = updatedVenue.openingTime,
                    closingTime = updatedVenue.closingTime,
                    images = updatedVenue.images
                )

                // Upload new images if any
                if (selectedNewImages.isNotEmpty()) {
                    viewModel.uploadVenueImages(updatedVenue.id, selectedNewImages)
                }
            },
            onDeleteImage = { venueId, imageUrl ->
                viewModel.deleteVenueImage(venueId, imageUrl)
            }
        )
    }
}
@Composable
private fun VenueCard(
    venue: Venue,
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
                            text = "Số sân: ${venue.courtsCount}",
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
                        Text(venue.address.getFullAddress(), fontSize = 14.sp, color = Color.DarkGray, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    }

                    // Mô tả
                    venue.description?.let { desc ->
                        if (desc.isNotBlank()) {
                            Row(verticalAlignment = Alignment.Top) {
                                Icon(Icons.Default.Info, null, tint = Color(0xFF123E62), modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(desc, fontSize = 14.sp, color = Color.DarkGray, maxLines = 2, overflow = TextOverflow.Ellipsis)
                            }
                        }
                    }
                } // Đóng Column chứa thông tin chi tiết

                Spacer(modifier = Modifier.height(12.dp))

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
    venue: Venue?,
    onDismiss: () -> Unit,
    onSave: (Venue) -> Unit,
) {
    var venueId by remember { mutableStateOf(venue?.id?.toString() ?: "") }
    var venueName by remember { mutableStateOf(venue?.name ?: "") }
    var venueProvinceOrCity by remember { mutableStateOf(venue?.address?.provinceOrCity ?: "") }
    var venueDistrict by remember { mutableStateOf(venue?.address?.district ?: "") }
    var venueDetailAddress by remember { mutableStateOf(venue?.address?.detailAddress ?: "") }
    var venueDescription by remember { mutableStateOf(venue?.description ?: "") }
    var openTime by remember { mutableStateOf(venue?.openingTime ?: "06:00:00") }
    var closeTime by remember { mutableStateOf(venue?.closingTime ?: "23:00:00") }
    var courtsCount by remember { mutableStateOf(venue?.courtsCount?.toString() ?: "1") }
    var imageUrl by remember { mutableStateOf(venue?.images?.firstOrNull() ?: "") }
    var pricePerHour by remember { mutableStateOf(venue?.pricePerHour?.toString() ?: "0") }
    var phoneNumber by remember { mutableStateOf(venue?.phoneNumber ?: "") }
    var email by remember { mutableStateOf(venue?.email ?: "") }

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
                        // Tỉnh/Thành phố
                        OutlinedTextField(
                            value = venueProvinceOrCity,
                            onValueChange = { venueProvinceOrCity = it },
                            label = { Text("Tỉnh/Thành phố", fontSize = 14.sp) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
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
                        // Quận/Huyện
                        OutlinedTextField(
                            value = venueDistrict,
                            onValueChange = { venueDistrict = it },
                            label = { Text("Quận/Huyện", fontSize = 14.sp) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
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
                        // Địa chỉ chi tiết
                        OutlinedTextField(
                            value = venueDetailAddress,
                            onValueChange = { venueDetailAddress = it },
                            label = { Text("Địa chỉ chi tiết", fontSize = 14.sp) },
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
                                value = courtsCount,
                                onValueChange = { courtsCount = it },
                                label = { Text("Số Sân", fontSize = 12.sp) },
                                modifier = Modifier.weight(0.8f),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
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
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
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
                        // Số điện thoại
                        OutlinedTextField(
                            value = phoneNumber,
                            onValueChange = { phoneNumber = it },
                            label = { Text("Số điện thoại", fontSize = 14.sp) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
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
                    }

                    item {
                        // Email
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("Email", fontSize = 14.sp) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
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
                            if (venueName.isNotBlank() && venueDetailAddress.isNotBlank() &&
                                venueProvinceOrCity.isNotBlank() && venueDistrict.isNotBlank()
                            ) {
                                onSave(
                                    Venue(
                                        id = venueId.toLongOrNull() ?: 0L,
                                        name = venueName,
                                        description = venueDescription.ifBlank { null },
                                        numberOfCourt = courtsCount.toIntOrNull() ?: 1,
                                        address = Address(
                                            id = venue?.address?.id ?: 0L,
                                            provinceOrCity = venueProvinceOrCity,
                                            district = venueDistrict,
                                            detailAddress = venueDetailAddress
                                        ),
                                        courtsCount = courtsCount.toIntOrNull() ?: 1,
                                        pricePerHour = pricePerHour.toLongOrNull() ?: 0,
                                        openingTime = openTime,
                                        closingTime = closeTime,
                                        phoneNumber = phoneNumber.ifBlank { null },
                                        email = email.ifBlank { null },
                                        images = if (imageUrl.isNotBlank()) listOf(imageUrl) else null,
                                        averageRating = venue?.averageRating ?: 0f,
                                        totalReviews = venue?.totalReviews ?: 0
                                    ),
                                )
                            }
                        },
                        modifier = Modifier.weight(1f).height(50.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = primaryColor,
                        ),
                        shape = RoundedCornerShape(12.dp),
                        enabled = venueName.isNotBlank() && venueDetailAddress.isNotBlank() &&
                            venueProvinceOrCity.isNotBlank() && venueDistrict.isNotBlank(),
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
    venue: Venue,
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
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF5F9FF) // Màu nền xanh nhạt
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            // Nội dung chính
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Logo và tên sân
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        Color(0xFF123E62),
                                        Color(0xFF1E5A8E)
                                    )
                                ),
                                shape = CircleShape,
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Logo sân",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp),
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = venue.name,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF123E62),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "Số sân: ${venue.courtsCount}",
                            fontSize = 13.sp,
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
                    // Số điện thoại (ưu tiên ownerPhone, fallback về phoneNumber)
                    val displayPhone = venue.ownerPhone ?: venue.phoneNumber
                    if (!displayPhone.isNullOrBlank()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Phone, null, tint = Color(0xFF4CAF50), modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = displayPhone,
                                fontSize = 14.sp,
                                color = Color(0xFF2C2C2C),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    // Địa chỉ
                    Row(verticalAlignment = Alignment.Top) {
                        Icon(Icons.Default.LocationOn, null, tint = Color(0xFFFF6F3C), modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = venue.address.getFullAddress(),
                            fontSize = 14.sp,
                            color = Color(0xFF2C2C2C),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Thông tin trạng thái
                val isOpen = isVenueOpen(venue.openingTime, venue.closingTime)
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(
                        modifier = Modifier
                            .background(
                                color = if (isOpen) Color(0xFF4CAF50).copy(alpha = 0.15f) else Color(0xFFFF9800).copy(alpha = 0.15f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = if (isOpen) Icons.Default.CheckCircle else Icons.Default.Lock,
                            contentDescription = null,
                            tint = if (isOpen) Color(0xFF4CAF50) else Color(0xFFFF9800),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isOpen) "Đang mở" else "Đã đóng",
                            fontSize = 12.sp,
                            color = if (isOpen) Color(0xFF4CAF50) else Color(0xFFFF9800),
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Text(
                        text = "${venue.pricePerHour / 1000}k/giờ",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFF6F3C)
                    )
                }
            }

            // Nút Edit và Delete ở góc trên phải
            Row(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                IconButton(
                    onClick = onEditClick,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(Icons.Default.Edit, "Chỉnh sửa", tint = Color(0xFF123E62), modifier = Modifier.size(20.dp))
                }
                IconButton(
                    onClick = { showDeleteDialog = true },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(Icons.Default.Delete, "Xóa", tint = Color(0xFFEF5350), modifier = Modifier.size(20.dp))
                }
            }
        }
    }

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
    venue: Venue,
    currentUser: User?,
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onSave: (Venue, List<java.io.File>) -> Unit,
    onDeleteImage: (Long, String) -> Unit,
) {
    val context = LocalContext.current

    var venueName by remember(venue) { mutableStateOf(venue.name) }
    var description by remember(venue) { mutableStateOf(venue.description ?: "") }
    var phoneNumber by remember(venue) { mutableStateOf(venue.phoneNumber ?: venue.ownerPhone ?: currentUser?.phoneNumber ?: "") }
    var provinceOrCity by remember(venue) { mutableStateOf(venue.address.provinceOrCity) }
    var district by remember(venue) { mutableStateOf(venue.address.district) }
    var detailAddress by remember(venue) { mutableStateOf(venue.address.detailAddress) }
    var pricePerHour by remember(venue) { mutableStateOf(venue.pricePerHour.toString()) }

    // State for new images to upload
    var selectedNewImages by remember { mutableStateOf<List<java.io.File>>(emptyList()) }

    // State for deleting images
    var imageToDelete by remember { mutableStateOf<String?>(null) }
    var showDeleteImageDialog by remember { mutableStateOf(false) }

    // Parse opening/closing time để lấy giờ và phút
    val (openHour, openMinute) = remember(venue.openingTime) {
        parseTime(venue.openingTime ?: "06:00")
    }
    val (closeHour, closeMinute) = remember(venue.closingTime) {
        parseTime(venue.closingTime ?: "23:00")
    }

    var openingHour by remember(venue.openingTime) { mutableStateOf(openHour) }
    var openingMinute by remember(venue.openingTime) { mutableStateOf(openMinute) }
    var closingHour by remember(venue.closingTime) { mutableStateOf(closeHour) }
    var closingMinute by remember(venue.closingTime) { mutableStateOf(closeMinute) }
    var showOpeningTimePicker by remember { mutableStateOf(false) }
    var showClosingTimePicker by remember { mutableStateOf(false) }

    // Image picker launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            val imageFile = FileUtils.uriToFile(context, uri)
            if (imageFile != null) {
                selectedNewImages = selectedNewImages + imageFile
            }
        }
    }

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

                    // Hình ảnh sân section
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                "Hình ảnh sân",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = primaryColor
                            )
                            OutlinedButton(
                                onClick = { if (!isLoading) imagePickerLauncher.launch("image/*") },
                                modifier = Modifier.height(36.dp),
                                enabled = !isLoading,
                                border = BorderStroke(1.dp, primaryColor.copy(alpha = 0.5f)),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(
                                    Icons.Default.AddPhotoAlternate,
                                    contentDescription = "Thêm ảnh",
                                    modifier = Modifier.size(16.dp),
                                    tint = primaryColor
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Thêm ảnh", fontSize = 12.sp, color = primaryColor)
                            }
                        }

                        // Display existing images
                        if (!venue.images.isNullOrEmpty()) {
                            Text(
                                "Ảnh hiện tại (${venue.images.size})",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                itemsIndexed(venue.images) { index, imageUrl ->
                                    val fullImageUrl = if (imageUrl.startsWith("http")) {
                                        imageUrl
                                    } else if (imageUrl.startsWith("/api/")) {
                                        // API trả về path đầy đủ như /api/files/venue-images/...
                                        val baseUrl = com.example.bookingcourt.core.utils.Constants.BASE_URL
                                            .removeSuffix("/api/")
                                            .removeSuffix("/")
                                        "$baseUrl$imageUrl"
                                    } else {
                                        // Chỉ có filename, build full URL
                                        val baseUrl = com.example.bookingcourt.core.utils.Constants.BASE_URL
                                            .removeSuffix("/api/")
                                            .removeSuffix("/")
                                        "$baseUrl/files/venue-images/$imageUrl"
                                    }

                                    Box(
                                        modifier = Modifier
                                            .size(80.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp))
                                    ) {
                                        AsyncImage(
                                            model = fullImageUrl,
                                            contentDescription = "Ảnh ${index + 1}",
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )

                                        // Delete button
                                        IconButton(
                                            onClick = {
                                                imageToDelete = imageUrl
                                                showDeleteImageDialog = true
                                            },
                                            modifier = Modifier
                                                .align(Alignment.TopEnd)
                                                .size(20.dp)
                                                .background(
                                                    Color.Red.copy(alpha = 0.7f),
                                                    RoundedCornerShape(10.dp)
                                                )
                                        ) {
                                            Icon(
                                                Icons.Default.Delete,
                                                contentDescription = "Xóa ảnh",
                                                tint = Color.White,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Display newly selected images
                        if (selectedNewImages.isNotEmpty()) {
                            Text(
                                "Ảnh mới sẽ thêm (${selectedNewImages.size})",
                                fontSize = 12.sp,
                                color = primaryColor,
                                fontWeight = FontWeight.Medium
                            )
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                itemsIndexed(selectedNewImages) { index, imageFile ->
                                    Box(
                                        modifier = Modifier
                                            .size(80.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                    ) {
                                        Image(
                                            painter = rememberAsyncImagePainter(model = imageFile),
                                            contentDescription = "Ảnh mới ${index + 1}",
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )

                                        // Remove button
                                        IconButton(
                                            onClick = {
                                                selectedNewImages = selectedNewImages.filterIndexed { i, _ -> i != index }
                                            },
                                            modifier = Modifier
                                                .align(Alignment.TopEnd)
                                                .size(20.dp)
                                                .background(
                                                    Color.Black.copy(alpha = 0.6f),
                                                    RoundedCornerShape(10.dp)
                                                )
                                        ) {
                                            Icon(
                                                Icons.Default.Close,
                                                contentDescription = "Xóa",
                                                tint = Color.White,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

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
                                address = venue.address.copy(
                                    provinceOrCity = provinceOrCity.trim(),
                                    district = district.trim(),
                                    detailAddress = detailAddress.trim()
                                ),
                                pricePerHour = pricePerHour.trim().toLongOrNull() ?: venue.pricePerHour,
                                openingTime = String.format("%02d:%02d:00", openingHour, openingMinute),
                                closingTime = String.format("%02d:%02d:00", closingHour, closingMinute)
                            )
                            onSave(updatedVenue, selectedNewImages)
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

    // Delete Image Confirmation Dialog
    if (showDeleteImageDialog && imageToDelete != null) {
        AlertDialog(
            onDismissRequest = { showDeleteImageDialog = false },
            title = {
                Text(
                    text = "Xóa ảnh",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF123E62)
                )
            },
            text = {
                Text("Bạn có chắc chắn muốn xóa ảnh này không?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        imageToDelete?.let { imageUrl ->
                            onDeleteImage(venue.id, imageUrl)
                        }
                        showDeleteImageDialog = false
                        imageToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Red
                    )
                ) {
                    Text("Xóa")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        showDeleteImageDialog = false
                        imageToDelete = null
                    }
                ) {
                    Text("Hủy", color = Color.Gray)
                }
            }
        )
    }
}

// Helper function to parse time string
private fun parseTime(timeString: String): Pair<Int, Int> {
    return try {
        val parts = timeString.split(":")
        if (parts.size >= 2) {
            // Xử lý cả format "HH:MM" và "HH:MM:SS"
            val hour = parts[0].toIntOrNull() ?: 0
            val minute = parts[1].toIntOrNull() ?: 0
            Pair(hour, minute)
        } else if (parts.size == 1) {
            // Chỉ có giờ, không có phút
            val hour = parts[0].toIntOrNull() ?: 0
            Pair(hour, 0)
        } else {
            Pair(0, 0)
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

// Helper function to get user initials
private fun getInitials(fullName: String?): String {
    if (fullName.isNullOrBlank()) return "U"

    val words = fullName.trim().split(" ").filter { it.isNotEmpty() }
    return when {
        words.isEmpty() -> "U"
        words.size == 1 -> words[0].take(2).uppercase()
        else -> {
            // Lấy chữ cái đầu của từ đầu và từ cuối
            "${words.first().first()}${words.last().first()}".uppercase()
        }
    }
}

// Helper function to check if venue is currently open
private fun isVenueOpen(openingTime: String?, closingTime: String?): Boolean {
    if (openingTime == null || closingTime == null) return true

    return try {
        val calendar = java.util.Calendar.getInstance()
        val currentHour = calendar.get(java.util.Calendar.HOUR_OF_DAY)
        val currentMinute = calendar.get(java.util.Calendar.MINUTE)
        val currentTimeInMinutes = currentHour * 60 + currentMinute

        // Parse opening time
        val openParts = openingTime.split(":")
        val openHour = openParts.getOrNull(0)?.toIntOrNull() ?: 0
        val openMinute = openParts.getOrNull(1)?.toIntOrNull() ?: 0
        val openTimeInMinutes = openHour * 60 + openMinute

        // Parse closing time
        val closeParts = closingTime.split(":")
        val closeHour = closeParts.getOrNull(0)?.toIntOrNull() ?: 23
        val closeMinute = closeParts.getOrNull(1)?.toIntOrNull() ?: 59
        val closeTimeInMinutes = closeHour * 60 + closeMinute

        // Check if current time is within opening hours
        currentTimeInMinutes in openTimeInMinutes..closeTimeInMinutes
    } catch (e: Exception) {
        true // Default to open if parsing fails
    }
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
            venue = Venue(
                id = 1L,
                name = "Star Club Badminton",
                description = "Sân cầu lông chất lượng cao",
                numberOfCourt = 4,
                address = Address(
                    id = 1L,
                    provinceOrCity = "Hà Nội",
                    district = "Nam Từ Liêm",
                    detailAddress = "Số 181 P. Cầu Cốc, Tây Mỗ"
                ),
                courtsCount = 4,
                pricePerHour = 150000,
                openingTime = "06:00:00",
                closingTime = "23:00:00",
                phoneNumber = "0123456789",
                email = "starclub@example.com",
                images = listOf("https://example.com/image.jpg"),
                averageRating = 4.5f,
                totalReviews = 25
            ),
            onDismiss = { },
            onSave = { },
        )
    }
}
