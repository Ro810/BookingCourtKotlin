package com.example.bookingcourt.presentation.profile.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.bookingcourt.presentation.theme.BookingCourtTheme
import com.example.bookingcourt.presentation.profile.viewmodel.EditProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    onNavigateBack: () -> Unit,
    viewModel: EditProfileViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    // Local state for form fields
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var avatarUrl by remember { mutableStateOf<String?>(null) }

    // Initialize form fields from current user
    LaunchedEffect(state.currentUser) {
        state.currentUser?.let { user ->
            fullName = user.fullName
            email = user.email
            phoneNumber = user.phoneNumber
            avatarUrl = user.avatar
        }
    }

    val snackbarHostState = remember { SnackbarHostState() }

    // Show success message
    LaunchedEffect(state.saveSuccess) {
        if (state.saveSuccess) {
            snackbarHostState.showSnackbar(
                message = "Cập nhật thông tin thành công",
                duration = SnackbarDuration.Short
            )
            viewModel.clearSaveSuccess()
            // Navigate back after short delay
            kotlinx.coroutines.delay(500)
            onNavigateBack()
        }
    }

    // Show error message
    LaunchedEffect(state.error) {
        state.error?.let { error ->
            snackbarHostState.showSnackbar(
                message = error,
                duration = SnackbarDuration.Short
            )
            viewModel.clearError()
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Chỉnh sửa hồ sơ") },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        enabled = !state.isSaving
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Quay lại"
                        )
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            viewModel.updateProfile(
                                fullName = fullName.trim(),
                                email = email.trim(),
                                phoneNumber = phoneNumber.trim()
                            )
                        },
                        enabled = !state.isSaving && fullName.isNotBlank() && phoneNumber.isNotBlank()
                    ) {
                        if (state.isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                "Lưu",
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF8BB1F6), // Mid Blue - đồng bộ với OwnerHomeScreen
                            Color.White,
                        ),
                    ),
                ),
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
            // Avatar Section
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(120.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer)
                                .border(3.dp, MaterialTheme.colorScheme.primary, CircleShape)
                                .clickable {
                                    // TODO: Mở image picker
                                },
                            contentAlignment = Alignment.Center,
                        ) {
                            if (avatarUrl != null) {
                                AsyncImage(
                                    model = avatarUrl,
                                    contentDescription = "Avatar",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop,
                                )
                            } else {
                                Icon(
                                    Icons.Default.Person,
                                    contentDescription = null,
                                    modifier = Modifier.size(60.dp),
                                    tint = MaterialTheme.colorScheme.primary,
                                )
                            }

                            // Camera icon overlay
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    Icons.Default.Edit,
                                    contentDescription = "Đổi ảnh",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp),
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            "Nhấn để thay đổi ảnh đại diện",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray,
                        )
                    }
                }
            }

            // Personal Information Section
            item {
                Text(
                    "Thông tin cá nhân",
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
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        // Full Name
                        OutlinedTextField(
                            value = fullName,
                            onValueChange = { fullName = it },
                            label = { Text("Họ và tên") },
                            leadingIcon = {
                                Icon(Icons.Default.Person, contentDescription = null)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            enabled = !state.isSaving,
                        )

                        // Phone Number
                        OutlinedTextField(
                            value = phoneNumber,
                            onValueChange = { phoneNumber = it },
                            label = { Text("Số điện thoại") },
                            leadingIcon = {
                                Icon(Icons.Default.Phone, contentDescription = null)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            enabled = !state.isSaving,
                        )

                        // Email
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("Email") },
                            leadingIcon = {
                                Icon(Icons.Default.Email, contentDescription = null)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            enabled = !state.isSaving,
                        )
                    }
                }
            }

            // Delete Account Section
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                    ),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { /* TODO: Show delete account dialog */ }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            "Xóa tài khoản",
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Medium,
                        )
                    }
                }
            }

            // Bottom spacing
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun EditProfileScreenPreview() {
    BookingCourtTheme {
        EditProfileScreen(
            onNavigateBack = {},
        )
    }
}
