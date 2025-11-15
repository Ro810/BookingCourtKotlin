package com.example.bookingcourt.presentation.review.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.bookingcourt.domain.model.Review
import com.example.bookingcourt.presentation.review.components.EditReviewDialog
import com.example.bookingcourt.presentation.review.components.ReviewCard
import com.example.bookingcourt.presentation.review.viewmodel.ReviewViewModel

/**
 * Screen to display all reviews created by the current user
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyReviewsScreen(
    onNavigateBack: () -> Unit = {},
    viewModel: ReviewViewModel = hiltViewModel()
) {
    val state by viewModel.myReviewsState.collectAsState()
    val updateState by viewModel.updateReviewState.collectAsState()

    var showDeleteDialog by remember { mutableStateOf(false) }
    var reviewToDelete by remember { mutableStateOf<Review?>(null) }
    var showEditDialog by remember { mutableStateOf(false) }
    var reviewToEdit by remember { mutableStateOf<Review?>(null) }

    // Handle update success
    LaunchedEffect(updateState.success) {
        if (updateState.success) {
            showEditDialog = false
            reviewToEdit = null
            viewModel.resetUpdateReviewState()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.loadMyReviews()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Đánh giá của tôi") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Quay lại"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                state.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                state.error != null -> {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Text(
                            text = state.error ?: "Đã có lỗi xảy ra",
                            modifier = Modifier.padding(16.dp),
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }

                state.reviews.isEmpty() -> {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .align(Alignment.Center),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Chưa có đánh giá",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Bạn chưa đánh giá sân nào",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(state.reviews) { review ->
                            ReviewCard(
                                review = review,
                                showVenueName = true,
                                canEdit = true,
                                onEditClick = {
                                    reviewToEdit = it
                                    showEditDialog = true
                                },
                                onDeleteClick = {
                                    reviewToDelete = it
                                    showDeleteDialog = true
                                }
                            )
                        }
                    }
                }
            }

            // Show update error as snackbar
            if (updateState.error != null) {
                Snackbar(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp),
                    action = {
                        TextButton(onClick = { viewModel.resetUpdateReviewState() }) {
                            Text("Đóng")
                        }
                    }
                ) {
                    Text(updateState.error ?: "Có lỗi xảy ra")
                }
            }
        }
    }

    // Edit review dialog
    if (showEditDialog && reviewToEdit != null) {
        EditReviewDialog(
            review = reviewToEdit!!,
            onDismiss = {
                showEditDialog = false
                reviewToEdit = null
                viewModel.resetUpdateReviewState()
            },
            onConfirm = { rating, comment ->
                viewModel.updateReview(
                    reviewId = reviewToEdit!!.id.toLong(),
                    rating = rating,
                    comment = comment
                )
            },
            isLoading = updateState.isLoading
        )
    }

    // Delete confirmation dialog
    if (showDeleteDialog && reviewToDelete != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Xóa đánh giá?") },
            text = { Text("Bạn có chắc chắn muốn xóa đánh giá này không?") },
            confirmButton = {
                Button(
                    onClick = {
                        reviewToDelete?.let { review ->
                            viewModel.deleteReview(review.id.toLong()) {
                                showDeleteDialog = false
                                reviewToDelete = null
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Xóa")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    reviewToDelete = null
                }) {
                    Text("Hủy")
                }
            }
        )
    }
}
