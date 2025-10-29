package com.example.bookingcourt.presentation.owner.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.bookingcourt.presentation.owner.viewmodel.BecomeOwnerEvent
import com.example.bookingcourt.presentation.owner.viewmodel.BecomeOwnerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BecomeOwnerScreen(
    onNavigateBack: () -> Unit,
    onNavigateToLogin: () -> Unit,
    viewModel: BecomeOwnerViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Handle events
    LaunchedEffect(Unit) {
        viewModel.event.collect { event ->
            when (event) {
                is BecomeOwnerEvent.NavigateToLogin -> {
                    onNavigateToLogin()
                }
                is BecomeOwnerEvent.ShowError -> {
                    snackbarHostState.showSnackbar(event.message)
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Trở thành chủ sân") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Quay lại")
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Thông tin hướng dẫn
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                ),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        "📋 Thông tin ngân hàng",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        "Vui lòng cung cấp thông tin ngân hàng để khách hàng có thể chuyển khoản thanh toán đặt sân.",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Form nhập thông tin ngân hàng
            Text(
                "Thông tin ngân hàng",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
            )

            // Tên ngân hàng
            OutlinedTextField(
                value = state.bankName,
                onValueChange = viewModel::onBankNameChange,
                label = { Text("Tên ngân hàng *") },
                placeholder = { Text("VD: Vietcombank, Techcombank, ...") },
                modifier = Modifier.fillMaxWidth(),
                isError = state.bankNameError != null,
                supportingText = state.bankNameError?.let { { Text(it) } },
                enabled = !state.isLoading,
                singleLine = true,
            )

            // Số tài khoản
            OutlinedTextField(
                value = state.accountNumber,
                onValueChange = viewModel::onAccountNumberChange,
                label = { Text("Số tài khoản *") },
                placeholder = { Text("VD: 1234567890") },
                modifier = Modifier.fillMaxWidth(),
                isError = state.accountNumberError != null,
                supportingText = state.accountNumberError?.let { { Text(it) } },
                enabled = !state.isLoading,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
            )

            // Tên chủ tài khoản
            OutlinedTextField(
                value = state.accountHolderName,
                onValueChange = viewModel::onAccountHolderNameChange,
                label = { Text("Tên chủ tài khoản *") },
                placeholder = { Text("VD: NGUYEN VAN A") },
                modifier = Modifier.fillMaxWidth(),
                isError = state.accountHolderNameError != null,
                supportingText = state.accountHolderNameError?.let { { Text(it) } },
                enabled = !state.isLoading,
                singleLine = true,
            )

            // Error message
            state.error?.let { error ->
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                    ),
                ) {
                    Text(
                        error,
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer,
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Submit button
            Button(
                onClick = viewModel::onSubmit,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = !state.isLoading,
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                } else {
                    Text("Đăng ký làm chủ sân")
                }
            }

            // Lưu ý
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                ),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        "⚠️ Lưu ý:",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        "• Thông tin ngân hàng sẽ được hiển thị cho khách hàng khi đặt sân",
                        style = MaterialTheme.typography.bodySmall,
                    )
                    Text(
                        "• Vui lòng kiểm tra kỹ thông tin trước khi gửi",
                        style = MaterialTheme.typography.bodySmall,
                    )
                    Text(
                        "• Bạn có thể cập nhật thông tin này sau trong phần cài đặt",
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        }
    }
}
