package com.example.bookingcourt.presentation.auth.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.bookingcourt.R
import com.example.bookingcourt.presentation.auth.viewmodel.RegisterViewModel
import com.example.bookingcourt.presentation.theme.DarkBlue
import com.example.bookingcourt.presentation.theme.LightBlue
import com.example.bookingcourt.presentation.theme.MidBlue
import com.example.bookingcourt.presentation.theme.BookingCourtTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    onNavigateBack: () -> Unit,
    onRegisterSuccess: () -> Unit,
    viewModel: RegisterViewModel = hiltViewModel(),
) {
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    val registerState by viewModel.registerState.collectAsState()
    val scope = rememberCoroutineScope()

    var showLoadingDialog by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var showErrorDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    // Handle register state changes
    LaunchedEffect(registerState) {
        when (registerState) {
            is RegisterViewModel.RegisterState.Loading -> {
                showLoadingDialog = true
                showErrorDialog = false
                showSuccessDialog = false
            }
            is RegisterViewModel.RegisterState.Success -> {
                showLoadingDialog = false
                showSuccessDialog = true
                // Tự động quay về trang đăng nhập sau 2 giây
                delay(2000)
                viewModel.resetState()
                onNavigateBack()
            }
            is RegisterViewModel.RegisterState.Error -> {
                showLoadingDialog = false
                errorMessage = (registerState as RegisterViewModel.RegisterState.Error).message
                showErrorDialog = true
            }
            is RegisterViewModel.RegisterState.Idle -> {
                showLoadingDialog = false
            }
        }
    }

    val blueGradient = Brush.verticalGradient(
        colors = listOf(
            Color.White,
            LightBlue,
            MidBlue,
        ),
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = blueGradient)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Image(
            painter = painterResource(id = R.drawable.logo1),
            contentDescription = "Hình ảnh quả cầu lông",
            modifier = Modifier
                .size(200.dp)
                .padding(bottom = 8.dp),
        )

        Text(
            text = "Đăng ký",
            style = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 36.sp,
            ),
            color = DarkBlue,
            modifier = Modifier.padding(bottom = 24.dp),
        )

        // Full name field
        OutlinedTextField(
            value = fullName,
            onValueChange = { fullName = it },
            label = {
                Text(
                    text = "Họ và tên",
                    color = DarkBlue,
                    style = MaterialTheme.typography.bodyLarge,
                )
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.colors(
                focusedIndicatorColor = MidBlue,
                unfocusedIndicatorColor = DarkBlue,
                cursorColor = MidBlue,
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black,
                focusedLabelColor = MidBlue,
                unfocusedLabelColor = DarkBlue,
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
            ),
            enabled = registerState !is RegisterViewModel.RegisterState.Loading,
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Email field
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = {
                Text(
                    text = "Email",
                    color = DarkBlue,
                    style = MaterialTheme.typography.bodyLarge,
                )
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.colors(
                focusedIndicatorColor = MidBlue,
                unfocusedIndicatorColor = DarkBlue,
                cursorColor = MidBlue,
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black,
                focusedLabelColor = MidBlue,
                unfocusedLabelColor = DarkBlue,
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
            ),
            enabled = registerState !is RegisterViewModel.RegisterState.Loading,
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Phone number field
        OutlinedTextField(
            value = phoneNumber,
            onValueChange = {
                if (it.length <= 10) phoneNumber = it
            },
            label = {
                Text(
                    text = "Số điện thoại",
                    color = DarkBlue,
                    style = MaterialTheme.typography.bodyLarge,
                )
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            prefix = { Text("+84 ", color = DarkBlue) },
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.colors(
                focusedIndicatorColor = MidBlue,
                unfocusedIndicatorColor = DarkBlue,
                cursorColor = MidBlue,
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black,
                focusedLabelColor = MidBlue,
                unfocusedLabelColor = DarkBlue,
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
            ),
            enabled = registerState !is RegisterViewModel.RegisterState.Loading,
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Password field
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = {
                Text(
                    text = "Mật khẩu",
                    color = DarkBlue,
                    style = MaterialTheme.typography.bodyLarge,
                )
            },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.colors(
                focusedIndicatorColor = MidBlue,
                unfocusedIndicatorColor = DarkBlue,
                cursorColor = MidBlue,
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black,
                focusedLabelColor = MidBlue,
                unfocusedLabelColor = DarkBlue,
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
            ),
            enabled = registerState !is RegisterViewModel.RegisterState.Loading,
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Confirm password field
        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = {
                Text(
                    text = "Xác nhận mật khẩu",
                    color = DarkBlue,
                    style = MaterialTheme.typography.bodyLarge,
                )
            },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.colors(
                focusedIndicatorColor = MidBlue,
                unfocusedIndicatorColor = DarkBlue,
                cursorColor = MidBlue,
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black,
                focusedLabelColor = MidBlue,
                unfocusedLabelColor = DarkBlue,
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
            ),
            enabled = registerState !is RegisterViewModel.RegisterState.Loading,
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                if (password == confirmPassword) {
                    scope.launch {
                        viewModel.register(fullName, email, "+84$phoneNumber", password)
                    }
                }
            },
            modifier = Modifier
                .width(200.dp)
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = DarkBlue,
                contentColor = Color.White,
            ),
            enabled = registerState !is RegisterViewModel.RegisterState.Loading &&
                fullName.isNotBlank() && email.isNotBlank() &&
                phoneNumber.isNotBlank() &&
                password.isNotBlank() && confirmPassword.isNotBlank() &&
                password == confirmPassword,
        ) {
            if (registerState is RegisterViewModel.RegisterState.Loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = Color.White,
                    strokeWidth = 2.dp,
                )
            } else {
                Text(
                    text = "Đăng ký",
                    style = MaterialTheme.typography.titleMedium,
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Đã có tài khoản? ",
                color = DarkBlue,
            )
            TextButton(
                onClick = onNavigateBack,
                enabled = registerState !is RegisterViewModel.RegisterState.Loading,
            ) {
                Text(
                    text = "Đăng nhập",
                    color = DarkBlue,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }

    // Loading Dialog
    if (showLoadingDialog) {
        AlertDialog(
            onDismissRequest = { },
            title = { Text("Đang đăng ký...") },
            text = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                ) {
                    CircularProgressIndicator()
                }
            },
            confirmButton = { },
        )
    }

    // Success Dialog
    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { },
            title = { Text("Đăng ký thành công!") },
            text = { Text("Tài khoản của bạn đã được tạo thành công. Đang chuyển về trang đăng nhập...") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.resetState()
                    onNavigateBack()
                }) {
                    Text("OK")
                }
            },
        )
    }

    // Error Dialog
    if (showErrorDialog) {
        AlertDialog(
            onDismissRequest = {
                showErrorDialog = false
                viewModel.resetState()
            },
            title = { Text("Đăng ký thất bại") },
            text = { Text(errorMessage) },
            confirmButton = {
                TextButton(onClick = {
                    showErrorDialog = false
                    viewModel.resetState()
                }) {
                    Text("OK")
                }
            },
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun RegisterScreenPreview() {
    BookingCourtTheme {
        RegisterScreenContent()
    }
}

@Composable
private fun RegisterScreenContent() {
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    val blueGradient = Brush.verticalGradient(
        colors = listOf(
            Color.White,
            LightBlue,
            MidBlue,
        ),
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = blueGradient)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Image(
            painter = painterResource(id = R.drawable.logo1),
            contentDescription = "Hình ảnh quả cầu lông",
            modifier = Modifier
                .size(200.dp)
                .padding(bottom = 8.dp),
        )

        Text(
            text = "Đăng ký",
            style = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 36.sp,
            ),
            color = DarkBlue,
            modifier = Modifier.padding(bottom = 24.dp),
        )

        // Full name field
        OutlinedTextField(
            value = fullName,
            onValueChange = { fullName = it },
            label = {
                Text(
                    text = "Họ và tên",
                    color = DarkBlue,
                    style = MaterialTheme.typography.bodyLarge,
                )
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.colors(
                focusedIndicatorColor = MidBlue,
                unfocusedIndicatorColor = DarkBlue,
                cursorColor = MidBlue,
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black,
                focusedLabelColor = MidBlue,
                unfocusedLabelColor = DarkBlue,
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
            ),
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Email field
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = {
                Text(
                    text = "Email",
                    color = DarkBlue,
                    style = MaterialTheme.typography.bodyLarge,
                )
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.colors(
                focusedIndicatorColor = MidBlue,
                unfocusedIndicatorColor = DarkBlue,
                cursorColor = MidBlue,
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black,
                focusedLabelColor = MidBlue,
                unfocusedLabelColor = DarkBlue,
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
            ),
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Phone number field
        OutlinedTextField(
            value = phoneNumber,
            onValueChange = {
                if (it.length <= 10) phoneNumber = it
            },
            label = {
                Text(
                    text = "Số điện thoại",
                    color = DarkBlue,
                    style = MaterialTheme.typography.bodyLarge,
                )
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            prefix = { Text("+84 ", color = DarkBlue) },
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.colors(
                focusedIndicatorColor = MidBlue,
                unfocusedIndicatorColor = DarkBlue,
                cursorColor = MidBlue,
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black,
                focusedLabelColor = MidBlue,
                unfocusedLabelColor = DarkBlue,
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
            ),
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Password field
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = {
                Text(
                    text = "Mật khẩu",
                    color = DarkBlue,
                    style = MaterialTheme.typography.bodyLarge,
                )
            },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.colors(
                focusedIndicatorColor = MidBlue,
                unfocusedIndicatorColor = DarkBlue,
                cursorColor = MidBlue,
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black,
                focusedLabelColor = MidBlue,
                unfocusedLabelColor = DarkBlue,
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
            ),
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Confirm password field
        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = {
                Text(
                    text = "Xác nhận mật khẩu",
                    color = DarkBlue,
                    style = MaterialTheme.typography.bodyLarge,
                )
            },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.colors(
                focusedIndicatorColor = MidBlue,
                unfocusedIndicatorColor = DarkBlue,
                cursorColor = MidBlue,
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black,
                focusedLabelColor = MidBlue,
                unfocusedLabelColor = DarkBlue,
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
            ),
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { },
            modifier = Modifier
                .width(200.dp)
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = DarkBlue,
                contentColor = Color.White,
            ),
            enabled = fullName.isNotBlank() && email.isNotBlank() &&
                phoneNumber.isNotBlank() &&
                password.isNotBlank() && confirmPassword.isNotBlank() &&
                password == confirmPassword,
        ) {
            Text(
                text = "Đăng ký",
                style = MaterialTheme.typography.titleMedium,
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Đã có tài khoản? ",
                color = DarkBlue,
            )
            TextButton(onClick = { }) {
                Text(
                    text = "Đăng nhập",
                    color = DarkBlue,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}
