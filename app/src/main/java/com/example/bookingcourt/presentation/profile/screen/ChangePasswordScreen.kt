package com.example.bookingcourt.presentation.profile.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.bookingcourt.presentation.profile.viewmodel.ChangePasswordState
import com.example.bookingcourt.presentation.profile.viewmodel.ChangePasswordViewModel
import com.example.bookingcourt.presentation.theme.BookingCourtTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangePasswordScreen(
    onNavigateBack: () -> Unit = {},
    onPasswordChanged: () -> Unit = {},
    viewModel: ChangePasswordViewModel = hiltViewModel(),
) {
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var currentPasswordVisible by remember { mutableStateOf(false) }
    var newPasswordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    var currentPasswordError by remember { mutableStateOf<String?>(null) }
    var newPasswordError by remember { mutableStateOf<String?>(null) }
    var confirmPasswordError by remember { mutableStateOf<String?>(null) }

    val changePasswordState by viewModel.changePasswordState.collectAsState()
    var showSuccessDialog by remember { mutableStateOf(false) }
    var showErrorDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    // Lắng nghe state từ ViewModel
    LaunchedEffect(changePasswordState) {
        when (changePasswordState) {
            is ChangePasswordState.Success -> {
                showSuccessDialog = true
            }
            is ChangePasswordState.Error -> {
                errorMessage = (changePasswordState as ChangePasswordState.Error).message
                showErrorDialog = true
            }
            else -> {}
        }
    }

    val isLoading = changePasswordState is ChangePasswordState.Loading

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Đổi mật khẩu") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Quay lại"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Hướng dẫn
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        "Yêu cầu mật khẩu mới:",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        "• Tối thiểu 8 ký tự",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    Text(
                        "• Bao gồm chữ hoa, chữ thường và số",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    Text(
                        "• Không trùng với mật khẩu cũ",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Mật khẩu hiện tại
            OutlinedTextField(
                value = currentPassword,
                onValueChange = {
                    currentPassword = it
                    currentPasswordError = null
                },
                label = { Text("Mật khẩu hiện tại") },
                placeholder = { Text("Nhập mật khẩu hiện tại") },
                visualTransformation = if (currentPasswordVisible)
                    VisualTransformation.None
                else
                    PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { currentPasswordVisible = !currentPasswordVisible }) {
                        Icon(
                            if (currentPasswordVisible)
                                Icons.Default.VisibilityOff
                            else
                                Icons.Default.Visibility,
                            contentDescription = if (currentPasswordVisible)
                                "Ẩn mật khẩu"
                            else
                                "Hiện mật khẩu"
                        )
                    }
                },
                isError = currentPasswordError != null,
                supportingText = currentPasswordError?.let { { Text(it) } },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = !isLoading
            )

            // Mật khẩu mới
            OutlinedTextField(
                value = newPassword,
                onValueChange = {
                    newPassword = it
                    newPasswordError = null
                },
                label = { Text("Mật khẩu mới") },
                placeholder = { Text("Nhập mật khẩu mới") },
                visualTransformation = if (newPasswordVisible)
                    VisualTransformation.None
                else
                    PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { newPasswordVisible = !newPasswordVisible }) {
                        Icon(
                            if (newPasswordVisible)
                                Icons.Default.VisibilityOff
                            else
                                Icons.Default.Visibility,
                            contentDescription = if (newPasswordVisible)
                                "Ẩn mật khẩu"
                            else
                                "Hiện mật khẩu"
                        )
                    }
                },
                isError = newPasswordError != null,
                supportingText = newPasswordError?.let { { Text(it) } },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = !isLoading
            )

            // Xác nhận mật khẩu mới
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = {
                    confirmPassword = it
                    confirmPasswordError = null
                },
                label = { Text("Xác nhận mật khẩu mới") },
                placeholder = { Text("Nhập lại mật khẩu mới") },
                visualTransformation = if (confirmPasswordVisible)
                    VisualTransformation.None
                else
                    PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                        Icon(
                            if (confirmPasswordVisible)
                                Icons.Default.VisibilityOff
                            else
                                Icons.Default.Visibility,
                            contentDescription = if (confirmPasswordVisible)
                                "Ẩn mật khẩu"
                            else
                                "Hiện mật khẩu"
                        )
                    }
                },
                isError = confirmPasswordError != null,
                supportingText = confirmPasswordError?.let { { Text(it) } },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = !isLoading
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Nút đổi mật khẩu
            Button(
                onClick = {
                    // Validate
                    var hasError = false

                    if (currentPassword.isEmpty()) {
                        currentPasswordError = "Vui lòng nhập mật khẩu hiện tại"
                        hasError = true
                    }

                    if (newPassword.isEmpty()) {
                        newPasswordError = "Vui lòng nhập mật khẩu mới"
                        hasError = true
                    } else if (newPassword.length < 8) {
                        newPasswordError = "Mật khẩu phải có ít nhất 8 ký tự"
                        hasError = true
                    } else if (!newPassword.any { it.isUpperCase() } ||
                               !newPassword.any { it.isLowerCase() } ||
                               !newPassword.any { it.isDigit() }) {
                        newPasswordError = "Mật khẩu phải bao gồm chữ hoa, chữ thường và số"
                        hasError = true
                    } else if (newPassword == currentPassword) {
                        newPasswordError = "Mật khẩu mới phải khác mật khẩu cũ"
                        hasError = true
                    }

                    if (confirmPassword.isEmpty()) {
                        confirmPasswordError = "Vui lòng xác nhận mật khẩu mới"
                        hasError = true
                    } else if (confirmPassword != newPassword) {
                        confirmPasswordError = "Mật khẩu xác nhận không khớp"
                        hasError = true
                    }

                    if (!hasError) {
                        // Gọi API thông qua ViewModel
                        viewModel.changePassword(currentPassword, newPassword, confirmPassword)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White
                    )
                } else {
                    Text("Đổi mật khẩu", fontSize = 16.sp)
                }
            }

            // Nút hủy
            OutlinedButton(
                onClick = onNavigateBack,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled = !isLoading
            ) {
                Text("Hủy", fontSize = 16.sp)
            }
        }
    }

    // Dialog thành công
    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { },
            title = { Text("Thành công") },
            text = { Text("Mật khẩu của bạn đã được thay đổi thành công!") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showSuccessDialog = false
                        viewModel.resetState()
                        onPasswordChanged()
                        onNavigateBack()
                    }
                ) {
                    Text("Đóng")
                }
            }
        )
    }

    // Dialog lỗi
    if (showErrorDialog) {
        AlertDialog(
            onDismissRequest = {
                showErrorDialog = false
                viewModel.resetState()
            },
            title = { Text("Lỗi") },
            text = { Text(errorMessage) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showErrorDialog = false
                        viewModel.resetState()
                    }
                ) {
                    Text("Đóng")
                }
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ChangePasswordScreenPreview() {
    BookingCourtTheme {
        ChangePasswordScreen()
    }
}
