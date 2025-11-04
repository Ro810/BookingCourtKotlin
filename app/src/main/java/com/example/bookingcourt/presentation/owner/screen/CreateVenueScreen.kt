package com.example.bookingcourt.presentation.owner.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.bookingcourt.presentation.owner.viewmodel.CreateVenueViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateVenueScreen(
    onNavigateBack: () -> Unit,
    onVenueCreated: (venueId: Long) -> Unit,
    viewModel: CreateVenueViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()

    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var provinceOrCity by remember { mutableStateOf("") }
    var district by remember { mutableStateOf("") }
    var detailAddress by remember { mutableStateOf("") }
    var numberOfCourts by remember { mutableStateOf("") }
    var pricePerHour by remember { mutableStateOf("") }
    var openingTime by remember { mutableStateOf("") }
    var closingTime by remember { mutableStateOf("") }

    // Show success dialog when venue is created
    LaunchedEffect(state.createdVenue) {
        state.createdVenue?.let { venue ->
            onVenueCreated(venue.id)
            viewModel.reset()
        }
    }

    // Show error snackbar
    val snackbarHostState = remember { SnackbarHostState() }
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
        topBar = {
            TopAppBar(
                title = { Text("Tạo sân mới") },
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
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                ),
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
            Text(
                "Thông tin sân",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )

            // Name field
            OutlinedTextField(
                value = name,
                onValueChange = {
                    name = it
                    viewModel.clearValidationErrors()
                },
                label = { Text("Tên sân *") },
                modifier = Modifier.fillMaxWidth(),
                isError = state.validationErrors.nameError != null,
                supportingText = {
                    state.validationErrors.nameError?.let {
                        Text(it, color = MaterialTheme.colorScheme.error)
                    }
                },
                singleLine = true,
            )

            // Description field
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Mô tả") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 5,
            )

            Text(
                "Thông tin liên hệ",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )

            // Phone Number field
            OutlinedTextField(
                value = phoneNumber,
                onValueChange = {
                    phoneNumber = it
                    viewModel.clearValidationErrors()
                },
                label = { Text("Số điện thoại *") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                isError = state.validationErrors.phoneNumberError != null,
                supportingText = {
                    state.validationErrors.phoneNumberError?.let {
                        Text(it, color = MaterialTheme.colorScheme.error)
                    }
                },
                singleLine = true,
            )

            // Email field
            OutlinedTextField(
                value = email,
                onValueChange = {
                    email = it
                    viewModel.clearValidationErrors()
                },
                label = { Text("Email *") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                isError = state.validationErrors.emailError != null,
                supportingText = {
                    state.validationErrors.emailError?.let {
                        Text(it, color = MaterialTheme.colorScheme.error)
                    }
                },
                singleLine = true,
            )

            Text(
                "Địa chỉ",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )

            // Province/City field
            OutlinedTextField(
                value = provinceOrCity,
                onValueChange = {
                    provinceOrCity = it
                    viewModel.clearValidationErrors()
                },
                label = { Text("Tỉnh/Thành phố *") },
                modifier = Modifier.fillMaxWidth(),
                isError = state.validationErrors.provinceError != null,
                supportingText = {
                    state.validationErrors.provinceError?.let {
                        Text(it, color = MaterialTheme.colorScheme.error)
                    }
                },
                singleLine = true,
            )

            // District field
            OutlinedTextField(
                value = district,
                onValueChange = {
                    district = it
                    viewModel.clearValidationErrors()
                },
                label = { Text("Quận/Huyện *") },
                modifier = Modifier.fillMaxWidth(),
                isError = state.validationErrors.districtError != null,
                supportingText = {
                    state.validationErrors.districtError?.let {
                        Text(it, color = MaterialTheme.colorScheme.error)
                    }
                },
                singleLine = true,
            )

            // Detail Address field
            OutlinedTextField(
                value = detailAddress,
                onValueChange = {
                    detailAddress = it
                    viewModel.clearValidationErrors()
                },
                label = { Text("Địa chỉ chi tiết *") },
                modifier = Modifier.fillMaxWidth(),
                isError = state.validationErrors.detailAddressError != null,
                supportingText = {
                    state.validationErrors.detailAddressError?.let {
                        Text(it, color = MaterialTheme.colorScheme.error)
                    }
                },
                singleLine = true,
            )

            Text(
                "Thông tin hoạt động",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )

            // Price per hour field
            OutlinedTextField(
                value = pricePerHour,
                onValueChange = {
                    pricePerHour = it
                    viewModel.clearValidationErrors()
                },
                label = { Text("Giá/1 giờ (VNĐ)") },
                placeholder = { Text("Ví dụ: 100000") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = state.validationErrors.priceError != null,
                supportingText = {
                    state.validationErrors.priceError?.let {
                        Text(it, color = MaterialTheme.colorScheme.error)
                    }
                },
                singleLine = true,
            )

            // Number of courts field
            OutlinedTextField(
                value = numberOfCourts,
                onValueChange = {
                    numberOfCourts = it
                    viewModel.clearValidationErrors()
                },
                label = { Text("Số lượng sân *") },
                placeholder = { Text("Ví dụ: 4") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = state.validationErrors.numberOfCourtsError != null,
                supportingText = {
                    state.validationErrors.numberOfCourtsError?.let {
                        Text(it, color = MaterialTheme.colorScheme.error)
                    }
                },
                singleLine = true,
            )

            // Opening time field
            OutlinedTextField(
                value = openingTime,
                onValueChange = {
                    openingTime = it
                    viewModel.clearValidationErrors()
                },
                label = { Text("Giờ mở cửa") },
                placeholder = { Text("Ví dụ: 06:00 hoặc 06:00:00") },
                modifier = Modifier.fillMaxWidth(),
                isError = state.validationErrors.openingTimeError != null,
                supportingText = {
                    state.validationErrors.openingTimeError?.let {
                        Text(it, color = MaterialTheme.colorScheme.error)
                    }
                },
                singleLine = true,
            )

            // Closing time field
            OutlinedTextField(
                value = closingTime,
                onValueChange = {
                    closingTime = it
                    viewModel.clearValidationErrors()
                },
                label = { Text("Giờ đóng cửa") },
                placeholder = { Text("Ví dụ: 23:00 hoặc 23:00:00") },
                modifier = Modifier.fillMaxWidth(),
                isError = state.validationErrors.closingTimeError != null,
                supportingText = {
                    state.validationErrors.closingTimeError?.let {
                        Text(it, color = MaterialTheme.colorScheme.error)
                    }
                },
                singleLine = true,
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Create/Complete button
            Button(
                onClick = {
                    viewModel.createVenue(
                        name = name,
                        description = description,
                        phoneNumber = phoneNumber,
                        email = email,
                        provinceOrCity = provinceOrCity,
                        district = district,
                        detailAddress = detailAddress,
                        numberOfCourts = numberOfCourts,
                        pricePerHour = pricePerHour,
                        openingTime = openingTime,
                        closingTime = closingTime
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = !state.isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        "Đang tạo sân...",
                        style = MaterialTheme.typography.titleMedium
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Hoàn thành",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }

            Text(
                "* Trường bắt buộc",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}
