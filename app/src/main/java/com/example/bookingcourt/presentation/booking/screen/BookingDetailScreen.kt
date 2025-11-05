package com.example.bookingcourt.presentation.booking.screen

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import com.example.bookingcourt.core.common.Resource
import com.example.bookingcourt.domain.model.BookingDetail
import com.example.bookingcourt.domain.model.BookingStatus
import com.example.bookingcourt.presentation.booking.viewmodel.BookingDetailViewModel
import com.example.bookingcourt.presentation.theme.Primary
import kotlinx.datetime.LocalDateTime
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingDetailScreen(
    bookingId: String,
    onNavigateBack: () -> Unit,
    onNavigateToWaiting: (String) -> Unit,
    viewModel: BookingDetailViewModel = hiltViewModel()
) {
    val bookingDetail by viewModel.bookingDetail.collectAsState()
    val uploadState by viewModel.uploadState.collectAsState()
    val confirmState by viewModel.confirmState.collectAsState()
    val timeRemaining by viewModel.timeRemaining.collectAsState()

    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var uploadedPaymentProofUrl by remember { mutableStateOf<String?>(null) }
    var showConfirmDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    // Image picker launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
        uri?.let {
            // Convert URI to File and upload
            val file = uriToFile(context, it)
            file?.let { imageFile ->
                viewModel.uploadPaymentProof(imageFile)
            }
        }
    }

    // Handle upload state
    LaunchedEffect(uploadState) {
        when (uploadState) {
            is Resource.Success -> {
                uploadedPaymentProofUrl = (uploadState as Resource.Success).data
                snackbarHostState.showSnackbar("Upload ảnh thành công!")
                viewModel.resetUploadState()
            }
            is Resource.Error -> {
                snackbarHostState.showSnackbar(
                    (uploadState as Resource.Error).message ?: "Lỗi upload ảnh"
                )
                viewModel.resetUploadState()
            }
            else -> {}
        }
    }

    // Handle confirm payment state
    LaunchedEffect(confirmState) {
        when (confirmState) {
            is Resource.Success -> {
                snackbarHostState.showSnackbar("Đã gửi xác nhận thanh toán!")
                viewModel.resetConfirmState()
                // Navigate to waiting screen
                onNavigateToWaiting(bookingId)
            }
            is Resource.Error -> {
                snackbarHostState.showSnackbar(
                    (confirmState as Resource.Error).message ?: "Lỗi xác nhận thanh toán"
                )
                viewModel.resetConfirmState()
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chi tiết đặt sân") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Primary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        when (val state = bookingDetail) {
            is Resource.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            is Resource.Error -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = state.message ?: "Lỗi tải dữ liệu",
                        color = Color.Red
                    )
                }
            }
            is Resource.Success -> {
                state.data?.let { booking ->
                    BookingDetailContent(
                        booking = booking,
                        timeRemaining = timeRemaining,
                        selectedImageUri = selectedImageUri,
                        uploadedPaymentProofUrl = uploadedPaymentProofUrl ?: booking.paymentProofUrl,
                        isUploading = uploadState is Resource.Loading,
                        isConfirming = confirmState is Resource.Loading,
                        onSelectImage = { imagePickerLauncher.launch("image/*") },
                        onConfirmPayment = {
                            val proofUrl = uploadedPaymentProofUrl ?: booking.paymentProofUrl
                            if (proofUrl != null) {
                                showConfirmDialog = true
                            }
                        },
                        modifier = Modifier.padding(padding)
                    )

                    if (showConfirmDialog) {
                        AlertDialog(
                            onDismissRequest = { showConfirmDialog = false },
                            title = { Text("Xác nhận thanh toán") },
                            text = { Text("Bạn đã chuyển khoản thành công? Hệ thống sẽ gửi thông báo cho chủ sân xác nhận.") },
                            confirmButton = {
                                TextButton(
                                    onClick = {
                                        showConfirmDialog = false
                                        val proofUrl = uploadedPaymentProofUrl ?: booking.paymentProofUrl
                                        proofUrl?.let { viewModel.confirmPayment(it) }
                                    }
                                ) {
                                    Text("Xác nhận")
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { showConfirmDialog = false }) {
                                    Text("Hủy")
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BookingDetailContent(
    booking: BookingDetail,
    timeRemaining: Long,
    selectedImageUri: Uri?,
    uploadedPaymentProofUrl: String?,
    isUploading: Boolean,
    isConfirming: Boolean,
    onSelectImage: () -> Unit,
    onConfirmPayment: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Countdown timer
        if (booking.status == BookingStatus.PENDING_PAYMENT && timeRemaining > 0) {
            CountdownTimer(timeRemaining)
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Booking info card
        BookingInfoCard(booking)

        Spacer(modifier = Modifier.height(16.dp))

        // Bank info card
        booking.ownerBankInfo?.let { bankInfo ->
            BankInfoCard(bankInfo)
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Payment proof section
        if (booking.status == BookingStatus.PENDING_PAYMENT) {
            PaymentProofSection(
                selectedImageUri = selectedImageUri,
                uploadedPaymentProofUrl = uploadedPaymentProofUrl,
                isUploading = isUploading,
                isConfirming = isConfirming,
                onSelectImage = onSelectImage,
                onConfirmPayment = onConfirmPayment
            )
        }
    }
}

@Composable
private fun CountdownTimer(timeRemaining: Long) {
    val minutes = (timeRemaining / 1000 / 60).toInt()
    val seconds = ((timeRemaining / 1000) % 60).toInt()

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Timer,
                contentDescription = null,
                tint = Color(0xFFFF9800)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Thời gian còn lại: ${minutes}:${seconds.toString().padStart(2, '0')}",
                fontWeight = FontWeight.Bold,
                color = Color(0xFFFF9800)
            )
        }
    }
}

@Composable
private fun BookingInfoCard(booking: BookingDetail) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Thông tin đặt sân",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))

            InfoRow(label = "Sân", value = booking.court.description)
            InfoRow(label = "Địa điểm", value = booking.venue.name)
            booking.venueAddress?.let { InfoRow(label = "Địa chỉ", value = it) }
            InfoRow(
                label = "Thời gian",
                value = formatDateTime(booking.startTime) + " - " + formatTime(booking.endTime)
            )
            InfoRow(
                label = "Tổng tiền",
                value = "${booking.totalPrice.formatPrice()} đ",
                valueColor = Primary
            )
            InfoRow(
                label = "Trạng thái",
                value = booking.status.toVietnamese(),
                valueColor = booking.status.toColor()
            )
        }
    }
}

@Composable
private fun BankInfoCard(bankInfo: com.example.bookingcourt.domain.model.BankInfo) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.AccountBalance,
                    contentDescription = null,
                    tint = Primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Thông tin chuyển khoản",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(12.dp))

            InfoRow(label = "Ngân hàng", value = bankInfo.bankName)
            InfoRow(label = "Số tài khoản", value = bankInfo.bankAccountNumber)
            InfoRow(label = "Chủ tài khoản", value = bankInfo.bankAccountName)
        }
    }
}

@Composable
private fun PaymentProofSection(
    selectedImageUri: Uri?,
    uploadedPaymentProofUrl: String?,
    isUploading: Boolean,
    isConfirming: Boolean,
    onSelectImage: () -> Unit,
    onConfirmPayment: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Chứng minh chuyển khoản",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Image preview
            if (selectedImageUri != null || uploadedPaymentProofUrl != null) {
                Image(
                    painter = rememberAsyncImagePainter(selectedImageUri ?: uploadedPaymentProofUrl),
                    contentDescription = "Payment proof",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .background(Color.LightGray, RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Upload button
            Button(
                onClick = onSelectImage,
                enabled = !isUploading,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isUploading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White
                    )
                } else {
                    Icon(Icons.Default.Upload, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (uploadedPaymentProofUrl != null) "Thay đổi ảnh" else "Chọn ảnh")
                }
            }

            // Confirm payment button
            if (uploadedPaymentProofUrl != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = onConfirmPayment,
                    enabled = !isConfirming,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Primary)
                ) {
                    if (isConfirming) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White
                        )
                    } else {
                        Icon(Icons.Default.CheckCircle, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Xác nhận đã chuyển khoản")
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoRow(
    label: String,
    value: String,
    valueColor: Color = Color.Black
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            color = Color.Gray,
            fontSize = 14.sp
        )
        Text(
            text = value,
            fontWeight = FontWeight.Medium,
            color = valueColor,
            fontSize = 14.sp
        )
    }
}

// Helper functions
private fun formatDateTime(dateTime: LocalDateTime): String {
    return "${dateTime.dayOfMonth}/${dateTime.monthNumber}/${dateTime.year} ${formatTime(dateTime)}"
}

private fun formatTime(dateTime: LocalDateTime): String {
    return "${dateTime.hour.toString().padStart(2, '0')}:${dateTime.minute.toString().padStart(2, '0')}"
}

private fun Long.formatPrice(): String {
    return "%,d".format(this).replace(',', '.')
}

private fun BookingStatus.toVietnamese(): String {
    return when (this) {
        BookingStatus.PENDING_PAYMENT -> "Chờ thanh toán"
        BookingStatus.PAYMENT_UPLOADED -> "Đã gửi xác nhận"
        BookingStatus.CONFIRMED -> "Đã xác nhận"
        BookingStatus.REJECTED -> "Bị từ chối"
        BookingStatus.CANCELLED -> "Đã hủy"
        BookingStatus.COMPLETED -> "Hoàn thành"
        else -> "Đang xử lý"
    }
}

private fun BookingStatus.toColor(): Color {
    return when (this) {
        BookingStatus.CONFIRMED -> Color(0xFF4CAF50)
        BookingStatus.REJECTED, BookingStatus.CANCELLED -> Color(0xFFF44336)
        BookingStatus.PAYMENT_UPLOADED -> Color(0xFFFF9800)
        else -> Color.Gray
    }
}

private fun uriToFile(context: android.content.Context, uri: Uri): File? {
    return try {
        val contentResolver = context.contentResolver
        val file = File(context.cacheDir, "payment_proof_${System.currentTimeMillis()}.jpg")
        val inputStream = contentResolver.openInputStream(uri)
        val outputStream = FileOutputStream(file)
        inputStream?.copyTo(outputStream)
        inputStream?.close()
        outputStream.close()
        file
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

