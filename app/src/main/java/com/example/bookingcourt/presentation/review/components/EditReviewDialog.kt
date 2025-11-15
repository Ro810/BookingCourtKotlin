package com.example.bookingcourt.presentation.review.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.bookingcourt.domain.model.Review

/**
 * Dialog để chỉnh sửa đánh giá
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditReviewDialog(
    review: Review,
    onDismiss: () -> Unit,
    onConfirm: (rating: Int, comment: String?) -> Unit,
    isLoading: Boolean = false
) {
    var rating by remember { mutableIntStateOf(review.rating) }
    var comment by remember { mutableStateOf(review.comment) }

    AlertDialog(
        onDismissRequest = { if (!isLoading) onDismiss() },
        title = {
            Text(
                text = "Chỉnh sửa đánh giá",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Rating selector
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Đánh giá của bạn",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        repeat(5) { index ->
                            IconButton(
                                onClick = { rating = index + 1 },
                                enabled = !isLoading
                            ) {
                                Icon(
                                    imageVector = if (index < rating) Icons.Filled.Star else Icons.Outlined.Star,
                                    contentDescription = "${index + 1} sao",
                                    modifier = Modifier.size(32.dp),
                                    tint = if (index < rating) Color(0xFFFFB300) else Color.Gray
                                )
                            }
                        }
                    }
                    Text(
                        text = when (rating) {
                            1 -> "Rất tệ"
                            2 -> "Tệ"
                            3 -> "Trung bình"
                            4 -> "Tốt"
                            5 -> "Xuất sắc"
                            else -> ""
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Comment input
                OutlinedTextField(
                    value = comment,
                    onValueChange = { comment = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Nhận xét (tùy chọn)") },
                    placeholder = { Text("Chia sẻ trải nghiệm của bạn...") },
                    minLines = 3,
                    maxLines = 5,
                    enabled = !isLoading
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(rating, comment.takeIf { it.isNotBlank() })
                },
                enabled = !isLoading && rating > 0
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text("Cập nhật")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isLoading
            ) {
                Text("Hủy")
            }
        }
    )
}
