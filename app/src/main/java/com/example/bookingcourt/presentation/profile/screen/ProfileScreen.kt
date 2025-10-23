package com.example.bookingcourt.presentation.profile.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.bookingcourt.domain.model.PlayingLevel
import com.example.bookingcourt.domain.model.User
import com.example.bookingcourt.domain.model.UserRole
import com.example.bookingcourt.presentation.theme.BookingCourtTheme
import kotlinx.datetime.LocalDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onNavigateBack: () -> Unit = {},
    onNavigateToEditProfile: () -> Unit = {},
    onNavigateToChangePassword: () -> Unit = {},
    onNavigateToBecomeOwner: () -> Unit = {},
    onNavigateToBecomeCustomer: () -> Unit = {}, // Thêm callback để chuyển về customer
    onLogout: () -> Unit = {},
    showBackButton: Boolean = true,
    showTopBar: Boolean = true,
    bottomPadding: Dp = 0.dp,
    currentUserRole: UserRole = UserRole.USER, // Thêm parameter để biết vai trò hiện tại
) {
    // Mock user data - trong thực tế sẽ lấy từ ViewModel
    val user = remember {
        User(
            id = "user_1",
            email = "nguyen.van.a@example.com",
            fullName = "Nguyễn Văn A",
            phoneNumber = "0123456789",
            avatar = null,
            role = currentUserRole, // Sử dụng role từ parameter
            isVerified = true,
            createdAt = LocalDateTime(2024, 1, 1, 0, 0),
            updatedAt = LocalDateTime(2024, 1, 1, 0, 0),
            favoriteCourtIds = listOf("1", "2", "3"),
            playingLevel = PlayingLevel.INTERMEDIATE,
            preferredSports = emptyList(),
        )
    }

    var showLogoutDialog by remember { mutableStateOf(false) }

    if (showTopBar) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Tài khoản") },
                    navigationIcon = {
                        if (showBackButton) {
                            IconButton(onClick = onNavigateBack) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại")
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = Color.White,
                        navigationIconContentColor = Color.White,
                    ),
                )
            },
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(Color(0xFFF5F5F5)),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                ProfileContent(
                    user = user,
                    onNavigateToEditProfile = onNavigateToEditProfile,
                    onNavigateToChangePassword = onNavigateToChangePassword,
                    onNavigateToBecomeOwner = onNavigateToBecomeOwner,
                    onNavigateToBecomeCustomer = onNavigateToBecomeCustomer,
                    onShowLogoutDialog = { showLogoutDialog = true },
                )
            }
        }
    } else {
        // No top bar - background gradient like home screen
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF8BB1F6), // Mid Blue - same as home
                            Color.White,
                        ),
                    ),
                ),
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = 48.dp, // Extra padding for status bar/notch
                    bottom = bottomPadding + 16.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                ProfileContent(
                    user = user,
                    onNavigateToEditProfile = onNavigateToEditProfile,
                    onNavigateToChangePassword = onNavigateToChangePassword,
                    onNavigateToBecomeOwner = onNavigateToBecomeOwner,
                    onNavigateToBecomeCustomer = onNavigateToBecomeCustomer,
                    onShowLogoutDialog = { showLogoutDialog = true },
                )
            }
        }
    }

    // Logout Confirmation Dialog
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = {
                Text("Đăng xuất")
            },
            text = {
                Text("Bạn có chắc chắn muốn đăng xuất không?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutDialog = false
                        onLogout()
                    },
                ) {
                    Text("Đăng xuất", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Hủy")
                }
            },
        )
    }
}

private fun LazyListScope.ProfileContent(
    user: User,
    onNavigateToEditProfile: () -> Unit,
    onNavigateToChangePassword: () -> Unit,
    onNavigateToBecomeOwner: () -> Unit,
    onNavigateToBecomeCustomer: () -> Unit,
    onShowLogoutDialog: () -> Unit,
) {
    // User Profile Header
    item {
        UserProfileHeader(
            user = user,
            onEditClick = onNavigateToEditProfile,
        )
    }

    // Account Stats
    item {
        AccountStatsCard(
            bookingsCount = 12,
            favoritesCount = user.favoriteCourtIds.size,
            reviewsCount = 5,
        )
    }

    // Become Owner Button - Only show for USER role
    if (user.role == UserRole.USER) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF4CAF50).copy(alpha = 0.1f),
                ),
            ) {
                MenuItemRow(
                    icon = Icons.Default.Business,
                    title = "Trở thành chủ sân",
                    subtitle = "Đăng ký để quản lý sân của bạn",
                    iconTint = Color(0xFF4CAF50),
                    titleColor = Color(0xFF4CAF50),
                    onClick = onNavigateToBecomeOwner,
                )
            }
        }
    }

    // Become Customer Button - Only show for OWNER role
    if (user.role == UserRole.OWNER) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF2196F3).copy(alpha = 0.1f),
                ),
            ) {
                MenuItemRow(
                    icon = Icons.Default.Person,
                    title = "Trở thành khách đặt sân",
                    subtitle = "Chuyển sang chế độ đặt sân",
                    iconTint = Color(0xFF2196F3),
                    titleColor = Color(0xFF2196F3),
                    onClick = onNavigateToBecomeCustomer,
                )
            }
        }
    }

    // Account Section
    item {
        Text(
            "Tài khoản",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Gray,
            modifier = Modifier.padding(horizontal = 4.dp),
        )
    }

    item {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
        ) {
            Column {
                MenuItemRow(
                    icon = Icons.Default.Edit,
                    title = "Chỉnh sửa hồ sơ",
                    onClick = onNavigateToEditProfile,
                )
                HorizontalDivider()
                MenuItemRow(
                    icon = Icons.Default.Lock,
                    title = "Đổi mật khẩu",
                    onClick = onNavigateToChangePassword,
                )
                HorizontalDivider()
                MenuItemRow(
                    icon = Icons.Default.Favorite,
                    title = "Sân yêu thích",
                    subtitle = "${user.favoriteCourtIds.size} sân",
                    onClick = { /* TODO */ },
                )
            }
        }
    }

    // Booking Section
    item {
        Text(
            "Đặt sân",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Gray,
            modifier = Modifier.padding(horizontal = 4.dp),
        )
    }

    item {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
        ) {
            Column {
                MenuItemRow(
                    icon = Icons.Default.DateRange,
                    title = "Lịch sử đặt sân",
                    onClick = { /* TODO */ },
                )
                HorizontalDivider()
                MenuItemRow(
                    icon = Icons.Default.Star,
                    title = "Đánh giá của tôi",
                    onClick = { /* TODO */ },
                )
            }
        }
    }

    // Support Section
    item {
        Text(
            "Hỗ trợ",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Gray,
            modifier = Modifier.padding(horizontal = 4.dp),
        )
    }

    item {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
        ) {
            Column {
                MenuItemRow(
                    icon = Icons.Default.Settings,
                    title = "Cài đặt",
                    onClick = { /* TODO */ },
                )
                HorizontalDivider()
                MenuItemRow(
                    icon = Icons.Default.Info,
                    title = "Về chúng tôi",
                    onClick = { /* TODO */ },
                )
                HorizontalDivider()
                MenuItemRow(
                    icon = Icons.Default.Share,
                    title = "Chia sẻ ứng dụng",
                    onClick = { /* TODO */ },
                )
                HorizontalDivider()
                MenuItemRow(
                    icon = Icons.Default.Email,
                    title = "Liên hệ & Hỗ trợ",
                    onClick = { /* TODO */ },
                )
            }
        }
    }

    // Logout Button
    item {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer,
            ),
        ) {
            MenuItemRow(
                icon = Icons.AutoMirrored.Filled.ExitToApp,
                title = "Đăng xuất",
                iconTint = MaterialTheme.colorScheme.error,
                titleColor = MaterialTheme.colorScheme.error,
                showArrow = false,
                onClick = onShowLogoutDialog,
            )
        }
    }

    // App Version
    item {
        Text(
            "Phiên bản 1.0.0",
            fontSize = 12.sp,
            color = Color.Gray,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
        )
    }
}

@Composable
fun UserProfileHeader(
    user: User,
    onEditClick: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Avatar
                Box(
                    modifier = Modifier.size(80.dp),
                ) {
                    if (user.avatar != null) {
                        AsyncImage(
                            model = user.avatar,
                            contentDescription = "Avatar",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                                .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape),
                            contentScale = ContentScale.Crop,
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = user.fullName.firstOrNull()?.toString() ?: "U",
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                            )
                        }
                    }

                    // Verified Badge
                    if (user.isVerified) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = "Verified",
                            tint = Color(0xFF4CAF50),
                            modifier = Modifier
                                .size(24.dp)
                                .align(Alignment.BottomEnd)
                                .background(Color.White, CircleShape),
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                // User Info
                Column(
                    modifier = Modifier.weight(1f),
                ) {
                    Text(
                        user.fullName,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        user.email,
                        fontSize = 14.sp,
                        color = Color.Gray,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        user.phoneNumber,
                        fontSize = 14.sp,
                        color = Color.Gray,
                    )
                }

                // Edit Button
                IconButton(onClick = onEditClick) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Chỉnh sửa",
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
            }

            // User Role & Level
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                // Role Badge
                BadgeChip(
                    label = getUserRoleName(user.role),
                    icon = Icons.Default.AccountCircle,
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                )

                // Playing Level Badge
                user.playingLevel?.let { level ->
                    BadgeChip(
                        label = getPlayingLevelName(level),
                        icon = Icons.Default.Star,
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    )
                }
            }
        }
    }
}

@Composable
fun AccountStatsCard(
    bookingsCount: Int,
    favoritesCount: Int,
    reviewsCount: Int,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            StatItem(
                icon = Icons.Default.DateRange,
                value = bookingsCount.toString(),
                label = "Đặt sân",
            )
            VerticalDivider(modifier = Modifier.height(50.dp))
            StatItem(
                icon = Icons.Default.Favorite,
                value = favoritesCount.toString(),
                label = "Yêu thích",
            )
            VerticalDivider(modifier = Modifier.height(50.dp))
            StatItem(
                icon = Icons.Default.Star,
                value = reviewsCount.toString(),
                label = "Đánh giá",
            )
        }
    }
}

@Composable
fun StatItem(
    icon: ImageVector,
    value: String,
    label: String,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp),
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            value,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
        )
        Text(
            label,
            fontSize = 12.sp,
            color = Color.Gray,
        )
    }
}

@Composable
fun MenuItemRow(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    iconTint: Color = MaterialTheme.colorScheme.primary,
    titleColor: Color = Color.Unspecified,
    showArrow: Boolean = true,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(24.dp),
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                title,
                fontSize = 16.sp,
                color = titleColor,
            )
            subtitle?.let {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    it,
                    fontSize = 12.sp,
                    color = Color.Gray,
                )
            }
        }
        if (showArrow) {
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = Color.Gray,
            )
        }
    }
}

@Composable
fun BadgeChip(
    label: String,
    icon: ImageVector,
    containerColor: Color,
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = containerColor,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                label,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}

fun getUserRoleName(role: UserRole): String {
    return when (role) {
        UserRole.USER -> "Người dùng"
        UserRole.OWNER -> "Chủ sân"
        UserRole.ADMIN -> "Quản trị viên"
    }
}

fun getPlayingLevelName(level: PlayingLevel): String {
    return when (level) {
        PlayingLevel.BEGINNER -> "Mới bắt đầu"
        PlayingLevel.INTERMEDIATE -> "Trung bình"
        PlayingLevel.ADVANCED -> "Nâng cao"
        PlayingLevel.PROFESSIONAL -> "Chuyên nghiệp"
    }
}

@Preview(showBackground = true)
@Composable
fun ProfileScreenPreview() {
    BookingCourtTheme {
        ProfileScreen()
    }
}
