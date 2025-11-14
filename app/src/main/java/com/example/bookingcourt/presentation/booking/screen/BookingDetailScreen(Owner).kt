package com.example.bookingcourt.presentation.booking.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bookingcourt.domain.model.BookingStatus
import com.example.bookingcourt.presentation.theme.BookingCourtTheme
import java.text.NumberFormat
import java.util.*
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import com.example.bookingcourt.core.common.Resource
import com.example.bookingcourt.presentation.booking.viewmodel.BookingDetailViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OwnerBookingDetailScreen(
    bookingId: String,
    onNavigateBack: () -> Unit = {},
    viewModel: BookingDetailViewModel = hiltViewModel(),
) {
    val bookingDetailState by viewModel.bookingDetail.collectAsState()
    val acceptState by viewModel.acceptState.collectAsState()
    val rejectState by viewModel.rejectState.collectAsState()
    val currencyFormat = remember { NumberFormat.getCurrencyInstance(Locale("vi", "VN")) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    var showRejectDialog by remember { mutableStateOf(false) }
    var rejectReason by remember { mutableStateOf("") }

    // Handle accept/reject state
    LaunchedEffect(acceptState) {
        when (acceptState) {
            is Resource.Success -> {
                snackbarHostState.showSnackbar("Đã chấp nhận đặt sân thành công")
                viewModel.resetAcceptState()
            }
            is Resource.Error -> {
                snackbarHostState.showSnackbar(
                    (acceptState as Resource.Error).message ?: "Lỗi khi chấp nhận đặt sân"
                )
                viewModel.resetAcceptState()
            }
            else -> {}
        }
    }

    LaunchedEffect(rejectState) {
        when (rejectState) {
            is Resource.Success -> {
                snackbarHostState.showSnackbar("Đã từ chối đặt sân")
                viewModel.resetRejectState()
                showRejectDialog = false
            }
            is Resource.Error -> {
                snackbarHostState.showSnackbar(
                    (rejectState as Resource.Error).message ?: "Lỗi khi từ chối đặt sân"
                )
                viewModel.resetRejectState()
            }
            else -> {}
        }
    }

    // Reject Dialog
    if (showRejectDialog) {
        AlertDialog(
            onDismissRequest = { showRejectDialog = false },
            title = { Text("Từ chối đặt sân") },
            text = {
                Column {
                    Text("Vui lòng nhập lý do từ chối:")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = rejectReason,
                        onValueChange = { rejectReason = it },
                        placeholder = { Text("Lý do từ chối...") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (rejectReason.isNotBlank()) {
                            viewModel.rejectBooking(rejectReason)
                        }
                    },
                    enabled = rejectReason.isNotBlank()
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chi tiết đặt sân") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                ),
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        when (bookingDetailState) {
            is Resource.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            is Resource.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            (bookingDetailState as Resource.Error).message ?: "Lỗi",
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { viewModel.loadBookingDetail() }) {
                            Text("Thử lại")
                        }
                    }
                }
            }
            is Resource.Success -> {
                val booking = (bookingDetailState as Resource.Success).data!!

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .background(Color(0xFFF5F5F5)),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    // Booking Status Card
                    item {
                        StatusCard(bookingStatus = booking.status)
                    }

                    // Customer Information Card
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                            ) {
                                Text(
                                    "Thông tin khách hàng",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                )
                                Spacer(modifier = Modifier.height(16.dp))

                                InfoRow(
                                    icon = Icons.Default.Person,
                                    label = "Tên khách hàng",
                                    value = booking.user.fullname,
                                )

                                booking.user.phone?.let { phone ->
                                    Spacer(modifier = Modifier.height(12.dp))
                                    InfoRow(
                                        icon = Icons.Default.Phone,
                                        label = "Số điện thoại",
                                        value = phone,
                                    )
                                }
                            }
                        }
                    }

                    // Booking Details Card
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                            ) {
                                Text(
                                    "Thông tin đặt sân",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                )
                                Spacer(modifier = Modifier.height(16.dp))

                                InfoRow(
                                    icon = Icons.Default.LocationOn,
                                    label = "Địa điểm",
                                    value = "${booking.venueAddress}",
                                )
                                Spacer(modifier = Modifier.height(12.dp))

                                InfoRow(
                                    icon = Icons.Default.Place,
                                    label = "Sân",
                                    value = booking.getCourtsDisplayName(),
                                )
                                Spacer(modifier = Modifier.height(12.dp))

                                InfoRow(
                                    icon = Icons.Default.DateRange,
                                    label = "Ngày đặt",
                                    value = "${booking.startTime.dayOfMonth}/${booking.startTime.monthNumber}/${booking.startTime.year}",
                                )
                                Spacer(modifier = Modifier.height(12.dp))

                                InfoRow(
                                    icon = Icons.Default.Info,
                                    label = "Thời gian",
                                    value = "${String.format("%02d:%02d", booking.startTime.hour, booking.startTime.minute)} - ${String.format("%02d:%02d", booking.endTime.hour, booking.endTime.minute)}",
                                )
                            }
                        }
                    }

                    // Payment Information Card
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                            ) {
                                Text(
                                    "Thông tin thanh toán",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                )
                                Spacer(modifier = Modifier.height(16.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Text(
                                        "Tổng tiền",
                                        fontSize = 16.sp,
                                        color = Color.Gray,
                                    )
                                    Text(
                                        currencyFormat.format(booking.totalPrice),
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                    )
                                }
                            }
                        }
                    }

                    // Payment Proof Image (if uploaded)
                    if (booking.paymentProofUploaded && booking.paymentProofUrl != null) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                ) {
                                    Text(
                                        "Chứng từ chuyển khoản",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))

                                    AsyncImage(
                                        model = booking.paymentProofUrl,
                                        contentDescription = "Chứng từ thanh toán",
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(400.dp),
                                        contentScale = ContentScale.Fit
                                    )

                                    booking.paymentProofUploadedAt?.let { uploadedAt ->
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            "Tải lên: $uploadedAt",
                                            fontSize = 12.sp,
                                            color = Color.Gray,
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Rejection Reason (if rejected)
                    booking.rejectionReason?.let { reason ->
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color(0xFFFFEBEE)
                                )
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                ) {
                                    Text(
                                        "Lý do từ chối",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFD32F2F)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        reason,
                                        fontSize = 14.sp,
                                        color = Color(0xFF666666)
                                    )
                                }
                            }
                        }
                    }

                    // Action Buttons for PAYMENT_UPLOADED status
                    if (booking.status == BookingStatus.PAYMENT_UPLOADED) {
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        rejectReason = ""
                                        showRejectDialog = true
                                    },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = Color(0xFFD32F2F)
                                    )
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = null)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Từ chối")
                                }

                                Button(
                                    onClick = { viewModel.acceptBooking() },
                                    modifier = Modifier.weight(1f),
                                    enabled = acceptState !is Resource.Loading
                                ) {
                                    if (acceptState is Resource.Loading) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(20.dp),
                                            color = Color.White,
                                            strokeWidth = 2.dp
                                        )
                                    } else {
                                        Icon(Icons.Default.CheckCircle, contentDescription = null)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Chấp nhận")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatusCard(
    bookingStatus: BookingStatus,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = getBookingStatusColor(bookingStatus),
        ),
        shape = RoundedCornerShape(12.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(
                    "Trạng thái đặt sân",
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 14.sp,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    getBookingStatusName(bookingStatus),
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
            Icon(
                getBookingStatusIcon(bookingStatus),
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(48.dp),
            )
        }
    }
}

@Composable
fun InfoRow(
    icon: ImageVector,
    label: String,
    value: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp),
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                label,
                fontSize = 14.sp,
                color = Color.Gray,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                value,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}

fun getBookingStatusName(status: BookingStatus): String {
    return when (status) {
        BookingStatus.PENDING -> "Chờ xác nhận"
        BookingStatus.PENDING_PAYMENT -> "Chờ thanh toán"
        BookingStatus.PAYMENT_UPLOADED -> "Đã gửi chứng minh"
        BookingStatus.CONFIRMED -> "Đã xác nhận"
        BookingStatus.REJECTED -> "Bị từ chối"
        BookingStatus.CANCELLED -> "Đã hủy"
        BookingStatus.EXPIRED -> "Hết hạn"
        BookingStatus.COMPLETED -> "Hoàn thành"
        BookingStatus.NO_SHOW -> "Không đến"
    }
}

fun getBookingStatusColor(status: BookingStatus): Color {
    return when (status) {
        BookingStatus.PENDING -> Color(0xFFFFA500)
        BookingStatus.PENDING_PAYMENT -> Color(0xFFFFA500)
        BookingStatus.PAYMENT_UPLOADED -> Color(0xFFFF9800)
        BookingStatus.CONFIRMED -> Color(0xFF4CAF50)
        BookingStatus.REJECTED -> Color(0xFFFF5252)
        BookingStatus.CANCELLED -> Color(0xFF9E9E9E)
        BookingStatus.EXPIRED -> Color(0xFFFF5252)
        BookingStatus.COMPLETED -> Color(0xFF2196F3)
        BookingStatus.NO_SHOW -> Color(0xFF9E9E9E)
    }
}

fun getBookingStatusIcon(status: BookingStatus): ImageVector {
    return when (status) {
        BookingStatus.PENDING -> Icons.Default.DateRange
        BookingStatus.PENDING_PAYMENT -> Icons.Default.DateRange
        BookingStatus.PAYMENT_UPLOADED -> Icons.Default.Info
        BookingStatus.CONFIRMED -> Icons.Default.CheckCircle
        BookingStatus.REJECTED -> Icons.Default.Close
        BookingStatus.CANCELLED -> Icons.Default.Close
        BookingStatus.EXPIRED -> Icons.Default.Clear
        BookingStatus.COMPLETED -> Icons.Default.Check
        BookingStatus.NO_SHOW -> Icons.Default.Clear
    }
}
