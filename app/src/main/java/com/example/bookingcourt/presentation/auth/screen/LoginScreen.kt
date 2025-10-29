package com.example.bookingcourt.presentation.auth.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
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
import com.example.bookingcourt.presentation.auth.viewmodel.LoginViewModel
import com.example.bookingcourt.presentation.theme.DarkBlue
import com.example.bookingcourt.presentation.theme.LightBlue
import com.example.bookingcourt.presentation.theme.MidBlue
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onNavigateToRegister: () -> Unit,
    onNavigateToForgotPassword: () -> Unit,
    onLoginSuccess: () -> Unit,
    viewModel: LoginViewModel = hiltViewModel(),
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var rememberMe by remember { mutableStateOf(false) }

    val loginState by viewModel.loginState.collectAsState()
    val validationErrors by viewModel.validationErrors.collectAsState()
    val scope = rememberCoroutineScope()

    // Show loading dialog
    var showLoadingDialog by remember { mutableStateOf(false) }
    var showErrorDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    // Handle login state changes
    LaunchedEffect(loginState) {
        when (loginState) {
            is LoginViewModel.LoginState.Loading -> {
                showLoadingDialog = true
                showErrorDialog = false
            }
            is LoginViewModel.LoginState.Success -> {
                showLoadingDialog = false
                onLoginSuccess()
                viewModel.resetState()
            }
            is LoginViewModel.LoginState.Error -> {
                showLoadingDialog = false
                errorMessage = (loginState as LoginViewModel.LoginState.Error).message
                showErrorDialog = true
            }
            is LoginViewModel.LoginState.Idle -> {
                showLoadingDialog = false
            }
        }
    }

    // blue gradient background
    val blueGradient = Brush.verticalGradient(
        colors = listOf(
            Color.White,
            LightBlue,
            MidBlue,
        ),
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(brush = blueGradient)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo1),
                contentDescription = "Hình ảnh quả cầu lông",
                modifier = Modifier
                    .size(250.dp)
                    .padding(bottom = 12.dp),
            )
            Text(
                text = "Cầu Lông",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 42.sp,
                ),
                color = DarkBlue,
                modifier = Modifier.padding(bottom = 32.dp),
            )

            // Username field (supports text, email or phone)
            OutlinedTextField(
                value = username,
                onValueChange = {
                    username = it
                    viewModel.validateField("username", it)
                },
                label = {
                    Text(
                        text = "Tên đăng nhập",
                        color = DarkBlue,
                        style = MaterialTheme.typography.bodyLarge,
                    )
                },
                placeholder = {
                    Text(
                        text = "Email hoặc username",
                        color = DarkBlue.copy(alpha = 0.6f),
                    )
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
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
                    errorIndicatorColor = MaterialTheme.colorScheme.error,
                    errorLabelColor = MaterialTheme.colorScheme.error,
                ),
                enabled = loginState !is LoginViewModel.LoginState.Loading,
                isError = validationErrors.usernameError != null,
                supportingText = {
                    validationErrors.usernameError?.let {
                        Text(
                            text = it,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Password field
            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it
                    viewModel.validateField("password", it)
                },
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
                    errorIndicatorColor = MaterialTheme.colorScheme.error,
                    errorLabelColor = MaterialTheme.colorScheme.error,
                ),
                enabled = loginState !is LoginViewModel.LoginState.Loading,
                isError = validationErrors.passwordError != null,
                supportingText = {
                    validationErrors.passwordError?.let {
                        Text(
                            text = it,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = rememberMe,
                        onCheckedChange = { rememberMe = it },
                        colors = CheckboxDefaults.colors(
                            checkedColor = DarkBlue,
                            uncheckedColor = DarkBlue,
                            checkmarkColor = Color.White,
                        ),
                        enabled = loginState !is LoginViewModel.LoginState.Loading,
                    )
                    Text(
                        text = "Ghi nhớ",
                        color = DarkBlue,
                    )
                }
                TextButton(
                    onClick = onNavigateToForgotPassword,
                    enabled = loginState !is LoginViewModel.LoginState.Loading,
                ) {
                    Text(
                        text = "Quên mật khẩu?",
                        color = DarkBlue,
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    scope.launch {
                        viewModel.login(username, password, rememberMe)
                    }
                },
                modifier = Modifier
                    .width(200.dp)
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = DarkBlue,
                    contentColor = Color.White,
                ),
                enabled = loginState !is LoginViewModel.LoginState.Loading && username.isNotBlank() && password.isNotBlank(),
            ) {
                if (loginState is LoginViewModel.LoginState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.dp,
                    )
                } else {
                    Text(
                        text = "Đăng nhập",
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Chưa có tài khoản? ",
                    color = DarkBlue,
                )
                TextButton(
                    onClick = onNavigateToRegister,
                    enabled = loginState !is LoginViewModel.LoginState.Loading,
                ) {
                    Text(
                        text = "Đăng ký",
                        color = DarkBlue,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }

    // Loading Dialog
    if (showLoadingDialog) {
        AlertDialog(
            onDismissRequest = { },
            title = { Text("Đang đăng nhập...") },
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

    // Error Dialog
    if (showErrorDialog) {
        AlertDialog(
            onDismissRequest = {
                showErrorDialog = false
                viewModel.resetState()
            },
            title = { Text("Đăng nhập thất bại") },
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
fun LoginScreenPreview() {
    // Preview không thể dùng ViewModel thực, nên ta tạo UI tương tự
    LoginScreenContent()
}

@Composable
private fun LoginScreenContent() {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var rememberMe by remember { mutableStateOf(false) }

    val blueGradient = Brush.verticalGradient(
        colors = listOf(
            Color.White,
            LightBlue,
            MidBlue,
        ),
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(brush = blueGradient)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo1),
                contentDescription = "Hình ảnh quả cầu lông",
                modifier = Modifier
                    .size(250.dp)
                    .padding(bottom = 12.dp),
            )
            Text(
                text = "Cầu Lông",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 42.sp,
                ),
                color = DarkBlue,
                modifier = Modifier.padding(bottom = 32.dp),
            )

            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = {
                    Text(
                        text = "Tên đăng nhập",
                        color = DarkBlue,
                        style = MaterialTheme.typography.bodyLarge,
                    )
                },
                placeholder = {
                    Text(
                        text = "Email hoặc username",
                        color = DarkBlue.copy(alpha = 0.6f),
                    )
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
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

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = rememberMe,
                        onCheckedChange = { rememberMe = it },
                        colors = CheckboxDefaults.colors(
                            checkedColor = DarkBlue,
                            uncheckedColor = DarkBlue,
                            checkmarkColor = Color.White,
                        ),
                    )
                    Text(
                        text = "Ghi nhớ",
                        color = DarkBlue,
                    )
                }
                TextButton(onClick = { }) {
                    Text(
                        text = "Quên mật khẩu?",
                        color = DarkBlue,
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { },
                modifier = Modifier
                    .width(200.dp)
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = DarkBlue,
                    contentColor = Color.White,
                ),
                enabled = username.isNotBlank() && password.isNotBlank(),
            ) {
                Text(
                    text = "Đăng nhập",
                    style = MaterialTheme.typography.titleMedium,
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Chưa có tài khoản? ",
                    color = DarkBlue,
                )
                TextButton(onClick = { }) {
                    Text(
                        text = "Đăng ký",
                        color = DarkBlue,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }
}
