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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.bookingcourt.presentation.owner.viewmodel.OwnerHomeViewModel
import com.example.bookingcourt.presentation.profile.screen.ProfileScreen
import com.example.bookingcourt.presentation.theme.BookingCourtTheme

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

    // Lấy tên user từ state, nếu null thì dùng tên mặc định
    val ownerName = state.currentUser?.fullName ?: "Chủ sân"

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
    ownerName: String,
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
                            // Avatar mặc định
                            Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = "Avatar",
                                modifier = Modifier.size(56.dp),
                                tint = Color(0xFF123E62),
                            )

                            Column(
                                modifier = Modifier.weight(1f),
                            ) {
                                Text(
                                    text = ownerName,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF123E62),
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
                            onNavigateToCreateVenue()
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
                    description = "",
                    phoneNumber = "",
                    email = "",
                    provinceOrCity = updatedVenue.address.provinceOrCity,
                    district = updatedVenue.address.district,
                    detailAddress = updatedVenue.address.detailAddress
                )
            }
        )
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

                // Thông tin trạng thái (Giả định luôn hoạt ��ộng)
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
}

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
    var openingTime by remember { mutableStateOf(venue.openingTime ?: "") }
    var closingTime by remember { mutableStateOf(venue.closingTime ?: "") }
    var imageUrls by remember { mutableStateOf(venue.images ?: emptyList()) }
    var newImageUrl by remember { mutableStateOf("") }
    var showAddImageDialog by remember { mutableStateOf(false) }

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

                    // Giờ mở cửa
                    OutlinedTextField(
                        value = openingTime,
                        onValueChange = { openingTime = it },
                        label = { Text("Giờ mở cửa", fontSize = 14.sp) },
                        placeholder = { Text("6 hoặc 06:00", fontSize = 14.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        enabled = !isLoading,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = primaryColor,
                            focusedLabelColor = primaryColor,
                        ),
                    )

                    // Giờ đóng cửa
                    OutlinedTextField(
                        value = closingTime,
                        onValueChange = { closingTime = it },
                        label = { Text("Giờ đóng cửa", fontSize = 14.sp) },
                        placeholder = { Text("23 hoặc 23:00", fontSize = 14.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        enabled = !isLoading,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = primaryColor,
                            focusedLabelColor = primaryColor,
                        ),
                    )

                    // Images section
                    if (imageUrls.isNotEmpty()) {
                        Text(
                            "Hình ảnh",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = primaryColor
                        )
                        imageUrls.forEachIndexed { index, url ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = url,
                                    fontSize = 12.sp,
                                    maxLines = 1,
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(
                                    onClick = { imageUrls = imageUrls.filterIndexed { i, _ -> i != index } },
                                    enabled = !isLoading
                                ) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = "Xóa",
                                        tint = Color.Red,
                                        modifier = Modifier.size(18.dp)
                                    )
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
                                openingTime = openingTime.trim().takeIf { it.isNotEmpty() },
                                closingTime = closingTime.trim().takeIf { it.isNotEmpty() },
                                images = imageUrls.takeIf { it.isNotEmpty() }
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
}

@Preview(showBackground = true)
@Composable
fun OwnerHomeScreenPreview() {
    BookingCourtTheme {
        OwnerHomeScreen()
    }
}
