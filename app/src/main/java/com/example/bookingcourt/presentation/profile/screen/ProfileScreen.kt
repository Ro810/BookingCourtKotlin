package com.example.bookingcourt.presentation.profile.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bookingcourt.BuildConfig
import com.example.bookingcourt.presentation.theme.BookingCourtTheme
import com.example.bookingcourt.presentation.theme.Primary
import com.example.bookingcourt.presentation.theme.PrimaryLight

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onNavigateBack: () -> Unit,
    onNavigateToEditProfile: () -> Unit,
    onLogout: () -> Unit,
) {
    // Mock data - sau này sẽ lấy từ ViewModel
    val userName = "Nguyễn Văn A"
    val userPhone = "0912345678"
    val userEmail = "nguyenvana@example.com"
    val userAvatar: String? = null

    var showLogoutDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tài khoản") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Primary,
                    titleContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFF5F5F5))
        ) {
            // Header - User Info Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Avatar
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(PrimaryLight.copy(alpha = 0.3f))
                                .border(2.dp, Primary, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            if (userAvatar == null) {
                                Text(
                                    text = userName.first().toString(),
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Primary
                                )
                            }
                            // Có thể thêm Coil Image cho avatar thật
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        // User Info
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = userName,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF212121)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Phone,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = Color.Gray
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = userPhone,
                                    fontSize = 14.sp,
                                    color = Color.Gray
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Email,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = Color.Gray
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = userEmail,
                                    fontSize = 14.sp,
                                    color = Color.Gray
                                )
                            }
                        }

                        // Edit Icon
                        IconButton(
                            onClick = onNavigateToEditProfile
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit Profile",
                                tint = Primary
                            )
                        }
                    }
                }
            }

            // Body - Menu Items
            item {
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Lịch sử đặt sân
            item {
                MenuItemCard(
                    icon = Icons.Default.History,
                    title = "Lịch sử đặt sân",
                    onClick = { /* Navigate to booking history */ }
                )
            }

            // Cài đặt
            item {
                MenuItemCard(
                    icon = Icons.Default.Settings,
                    title = "Cài đặt",
                    onClick = { /* Navigate to settings */ }
                )
            }

            // Thông tin phiên bản
            item {
                MenuItemCard(
                    icon = Icons.Default.Info,
                    title = "Thông tin phiên bản",
                    subtitle = "Phiên bản ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})",
                    onClick = { }
                )
            }

            // Điều khoản và chính sách
            item {
                MenuItemCard(
                    icon = Icons.Default.Policy,
                    title = "Điều khoản và chính sách",
                    onClick = { /* Navigate to terms and policy */ }
                )
            }

            // Đăng xuất
            item {
                Spacer(modifier = Modifier.height(8.dp))
                MenuItemCard(
                    icon = Icons.Default.Logout,
                    title = "Đăng xuất",
                    titleColor = Color.Red,
                    iconTint = Color.Red,
                    onClick = { showLogoutDialog = true }
                )
            }

            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }

    // Logout Confirmation Dialog
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.Logout,
                    contentDescription = null,
                    tint = Primary
                )
            },
            title = {
                Text(
                    text = "Đăng xuất",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text("Bạn có chắc chắn muốn đăng xuất khỏi tài khoản?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutDialog = false
                        onLogout()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Red
                    )
                ) {
                    Text("Đăng xuất")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showLogoutDialog = false }
                ) {
                    Text("Hủy")
                }
            }
        )
    }
}

@Composable
fun MenuItemCard(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    titleColor: Color = Color(0xFF212121),
    iconTint: Color = Primary,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(iconTint.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = iconTint,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Title & Subtitle
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = titleColor
                )
                if (subtitle != null) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = subtitle,
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }

            // Chevron Right
            if (subtitle == null) { // Không hiển thị mũi tên cho item thông tin phiên bản
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = Color.Gray
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ProfileScreenPreview() {
    BookingCourtTheme {
        ProfileScreen(
            onNavigateBack = {},
            onNavigateToEditProfile = {},
            onLogout = {}
        )
    }
}
