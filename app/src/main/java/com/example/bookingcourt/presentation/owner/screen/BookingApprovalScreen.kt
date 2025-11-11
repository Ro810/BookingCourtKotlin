package com.example.bookingcourt.presentation.owner.screen

import androidx.compose.foundation.Image
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import com.example.bookingcourt.core.common.Resource
import com.example.bookingcourt.domain.model.BookingDetail
import com.example.bookingcourt.presentation.owner.viewmodel.BookingApprovalViewModel
import com.example.bookingcourt.presentation.theme.Primary
import kotlinx.datetime.LocalDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingApprovalScreen(
    bookingId: String,
    onNavigateBack: () -> Unit,
    viewModel: BookingApprovalViewModel = hiltViewModel()
) {
    val bookingDetail by viewModel.bookingDetail.collectAsState()
    val acceptState by viewModel.acceptState.collectAsState()
    val rejectState by viewModel.rejectState.collectAsState()

    var showRejectDialog by remember { mutableStateOf(false) }
    var showAcceptDialog by remember { mutableStateOf(false) }
    var rejectionReason by remember { mutableStateOf("") }

    val snackbarHostState = remember { SnackbarHostState() }

    // Handle accept state
    LaunchedEffect(acceptState) {
        when (acceptState) {
            is Resource.Success -> {
                snackbarHostState.showSnackbar("Đã xác nhận booking thành công!")
                viewModel.resetAcceptState()
                kotlinx.coroutines.delay(1000)
                onNavigateBack()
            }
            is Resource.Error -> {
                snackbarHostState.showSnackbar(
                    (acceptState as Resource.Error).message ?: "Lỗi xác nhận booking"
                )
                viewModel.resetAcceptState()
            }
            else -> {}
        }
    }

    // Handle reject state
    LaunchedEffect(rejectState) {
        when (rejectState) {
            is Resource.Success -> {
                snackbarHostState.showSnackbar("Đã từ chối booking")
                viewModel.resetRejectState()
                kotlinx.coroutines.delay(1000)
                onNavigateBack()
            }
            is Resource.Error -> {
                snackbarHostState.showSnackbar(
                    (rejectState as Resource.Error).message ?: "Lỗi từ chối booking"
                )
                viewModel.resetRejectState()
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Xác nhận booking") },
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
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            is Resource.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
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
                    BookingApprovalContent(
                        booking = booking,
                        isAccepting = acceptState is Resource.Loading,
                        isRejecting = rejectState is Resource.Loading,
                        onAccept = { showAcceptDialog = true },
                        onReject = { showRejectDialog = true },
                        modifier = Modifier.padding(padding)
                    )

                    // Accept confirmation dialog
                    if (showAcceptDialog) {
                        AlertDialog(
                            onDismissRequest = { showAcceptDialog = false },
                            title = { Text("Xác nhận chấp nhận") },
                            text = { Text("Bạn đã nhận được tiền chuyển khoản từ khách hàng?") },
                            confirmButton = {
                                TextButton(
                                    onClick = {
                                        showAcceptDialog = false
                                        viewModel.acceptBooking()
                                    }
                                ) {
                                    Text("Xác nhận")
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { showAcceptDialog = false }) {
                                    Text("Hủy")
                                }
                            }
                        )
                    }

                    // Reject dialog
                    if (showRejectDialog) {
                        AlertDialog(
                            onDismissRequest = { showRejectDialog = false },
                            title = { Text("Từ chối booking") },
                            text = {
                                Column {
                                    Text("Vui lòng nhập lý do từ chối:")
                                    Spacer(modifier = Modifier.height(8.dp))
                                    OutlinedTextField(
                                        value = rejectionReason,
                                        onValueChange = { rejectionReason = it },
                                        placeholder = { Text("VD: Chưa nhận được tiền") },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            },
                            confirmButton = {
                                TextButton(
                                    onClick = {
                                        if (rejectionReason.isNotBlank()) {
                                            showRejectDialog = false
                                            viewModel.rejectBooking(rejectionReason)
                                        }
                                    },
                                    enabled = rejectionReason.isNotBlank()
                                ) {
                                    Text("Từ chối")
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { showRejectDialog = false }) {
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
private fun BookingApprovalContent(
    booking: BookingDetail,
    isAccepting: Boolean,
    isRejecting: Boolean,
    onAccept: () -> Unit,
    onReject: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Customer info card
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Thông tin khách hàng",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))

                InfoRow(label = "Tên", value = booking.user.fullname)
                InfoRow(label = "Số điện thoại", value = booking.user.phone ?: "N/A")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Booking info card
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

                InfoRow(label = "Địa điểm", value = booking.venue.name)
                booking.venueAddress?.let { InfoRow(label = "Địa chỉ", value = it) }

                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(thickness = 1.dp, color = Color.LightGray)
                Spacer(modifier = Modifier.height(8.dp))

                // ✅ Court info - Hiển thị tất cả các sân đã đặt
                if (!booking.bookingItems.isNullOrEmpty()) {
                    Text(
                        text = "Sân đã đặt:",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    booking.bookingItems.forEach { item ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFF5F5F5)
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = item.courtName,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Primary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "${formatDateTime(item.startTime)} - ${formatTime(item.endTime)}",
                                    fontSize = 13.sp,
                                    color = Color.DarkGray
                                )
                                Text(
                                    text = "${item.price.formatPrice()} đ",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Primary
                                )
                            }
                        }
                    }
                } else {
                    // Legacy: 1 sân duy nhất
                    booking.court?.let { court ->
                        InfoRow(label = "Sân", value = court.description)
                        InfoRow(
                            label = "Thời gian",
                            value = formatDateTime(booking.startTime) + " - " + formatTime(booking.endTime)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(thickness = 1.dp, color = Color.LightGray)
                Spacer(modifier = Modifier.height(8.dp))

                // ✅ Total price - Hiển thị tổng tiền từ API (đã bao gồm tất cả sân)
                InfoRow(
                    label = "Tổng tiền",
                    value = "${booking.totalPrice.formatPrice()} đ",
                    valueColor = Primary
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Payment proof card
        if (booking.paymentProofUrl != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Image,
                            contentDescription = null,
                            tint = Primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Chứng minh chuyển khoản",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    Image(
                        painter = rememberAsyncImagePainter(booking.paymentProofUrl),
                        contentDescription = "Payment proof",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp),
                        contentScale = ContentScale.Fit
                    )

                    booking.paymentProofUploadedAt?.let {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Upload lúc: $it",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Reject button
                OutlinedButton(
                    onClick = onReject,
                    enabled = !isAccepting && !isRejecting,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFFF44336)
                    )
                ) {
                    if (isRejecting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color(0xFFF44336)
                        )
                    } else {
                        Icon(Icons.Default.Close, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Từ chối")
                    }
                }

                // Accept button
                Button(
                    onClick = onAccept,
                    enabled = !isAccepting && !isRejecting,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4CAF50)
                    )
                ) {
                    if (isAccepting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White
                        )
                    } else {
                        Icon(Icons.Default.Check, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Chấp nhận")
                    }
                }
            }
        } else {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = null,
                        tint = Color(0xFFFF9800)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Khách hàng chưa upload chứng minh chuyển khoản",
                        color = Color(0xFFFF9800)
                    )
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
