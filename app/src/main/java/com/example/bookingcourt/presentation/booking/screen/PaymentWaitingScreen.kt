package com.example.bookingcourt.presentation.booking.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import com.example.bookingcourt.core.common.Resource
import com.example.bookingcourt.domain.model.BookingStatus
import com.example.bookingcourt.presentation.booking.viewmodel.PaymentWaitingViewModel
import com.example.bookingcourt.presentation.theme.Primary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentWaitingScreen(
    bookingId: String,
    onNavigateBack: () -> Unit,
    onNavigateToHome: () -> Unit,
    viewModel: PaymentWaitingViewModel = hiltViewModel()
) {
    val bookingDetail by viewModel.bookingDetail.collectAsState()
    val bookingStatus by viewModel.bookingStatus.collectAsState()
    val rejectionReason by viewModel.rejectionReason.collectAsState()

    // Show success or rejection dialog
    LaunchedEffect(bookingStatus) {
        when (bookingStatus) {
            BookingStatus.CONFIRMED -> {
                // Booking confirmed, navigate to success
            }
            BookingStatus.REJECTED -> {
                // Booking rejected, show reason
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chờ xác nhận") },
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
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            when (bookingStatus) {
                BookingStatus.PAYMENT_UPLOADED -> {
                    WaitingContent(
                        bookingDetail = bookingDetail,
                        onRefresh = { viewModel.refreshBookingStatus() }
                    )
                }
                BookingStatus.CONFIRMED -> {
                    SuccessContent(
                        onNavigateToHome = onNavigateToHome
                    )
                }
                BookingStatus.REJECTED -> {
                    RejectedContent(
                        reason = rejectionReason,
                        onNavigateBack = onNavigateBack
                    )
                }
                else -> {
                    CircularProgressIndicator()
                }
            }
        }
    }
}

@Composable
private fun WaitingContent(
    bookingDetail: Resource<com.example.bookingcourt.domain.model.BookingDetail>?,
    onRefresh: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Default.HourglassEmpty,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = Color(0xFFFF9800)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Chờ chủ sân xác nhận",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Chúng tôi đã gửi chứng minh chuyển khoản cho chủ sân. Vui lòng đợi xác nhận.",
            fontSize = 14.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Show payment proof if available
        if (bookingDetail is Resource.Success) {
            bookingDetail.data?.paymentProofUrl?.let { url ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Chứng minh chuyển khoản của bạn",
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Image(
                            painter = rememberAsyncImagePainter(url),
                            contentDescription = "Payment proof",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onRefresh,
            modifier = Modifier.fillMaxWidth(0.6f)
        ) {
            Icon(Icons.Default.Refresh, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Làm mới")
        }

        Spacer(modifier = Modifier.height(16.dp))

        LinearProgressIndicator(
            modifier = Modifier.fillMaxWidth(0.6f)
        )
    }
}

@Composable
private fun SuccessContent(
    onNavigateToHome: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Default.CheckCircle,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = Color(0xFF4CAF50)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Đặt sân thành công!",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF4CAF50)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Chủ sân đã xác nhận thanh toán của bạn. Chúc bạn có trải nghiệm tuyệt vời!",
            fontSize = 14.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onNavigateToHome,
            modifier = Modifier.fillMaxWidth(0.6f)
        ) {
            Text("Về trang chủ")
        }
    }
}

@Composable
private fun RejectedContent(
    reason: String?,
    onNavigateBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Default.Cancel,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = Color(0xFFF44336)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Đặt sân bị từ chối",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFF44336)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Rất tiếc, chủ sân đã từ chối đặt sân của bạn.",
            fontSize = 14.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )

        if (!reason.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Lý do từ chối:",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFF44336)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = reason)
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onNavigateBack,
            modifier = Modifier.fillMaxWidth(0.6f)
        ) {
            Text("Quay lại")
        }
    }
}

