package com.example.bookingcourt.presentation.review.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bookingcourt.domain.model.Review
import kotlinx.datetime.LocalDateTime

/**
 * Card component to display a single review
 */
@Composable
fun ReviewCard(
    review: Review,
    modifier: Modifier = Modifier,
    showVenueName: Boolean = false,
    canEdit: Boolean = false,
    onEditClick: ((Review) -> Unit)? = null,
    onDeleteClick: ((Review) -> Unit)? = null
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // User info and rating
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // User avatar placeholder
                    Surface(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Box(
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = review.userName.firstOrNull()?.uppercase() ?: "?",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }

                    Column {
                        Text(
                            text = review.userName,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        if (review.isVerifiedBooking) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    modifier = Modifier.size(12.dp),
                                    tint = Color(0xFF4CAF50)
                                )
                                Text(
                                    text = "Đã đặt sân",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF4CAF50),
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }

                // Rating stars
                RatingStars(
                    rating = review.rating,
                    modifier = Modifier
                )
            }

            // Date
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = formatReviewDate(review.createdAt),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Edit and Delete buttons (only show if canEdit is true)
                if (canEdit && (onEditClick != null || onDeleteClick != null)) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        if (onEditClick != null) {
                            IconButton(
                                onClick = { onEditClick(review) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Chỉnh sửa",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        if (onDeleteClick != null) {
                            IconButton(
                                onClick = { onDeleteClick(review) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Xóa",
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Comment
            if (review.comment.isNotBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = review.comment,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // Venue name (for My Reviews screen)
            if (showVenueName) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Sân: ${review.courtId}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

/**
 * Display rating stars
 */
@Composable
fun RatingStars(
    rating: Int,
    modifier: Modifier = Modifier,
    size: Int = 16
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        repeat(5) { index ->
            Icon(
                imageVector = if (index < rating) Icons.Filled.Star else Icons.Outlined.Star,
                contentDescription = null,
                modifier = Modifier.size(size.dp),
                tint = if (index < rating) Color(0xFFFFB300) else Color.Gray
            )
        }
    }
}

/**
 * Format LocalDateTime to readable Vietnamese format
 * Displays in Vietnam timezone (assuming the datetime from API is already in Vietnam time)
 */
private fun formatReviewDate(dateTime: LocalDateTime): String {
    return try {
        // Format: DD/MM/YYYY HH:MM (giờ Việt Nam)
        "${dateTime.dayOfMonth.toString().padStart(2, '0')}/" +
        "${dateTime.monthNumber.toString().padStart(2, '0')}/" +
        "${dateTime.year} " +
        "${dateTime.hour.toString().padStart(2, '0')}:" +
        dateTime.minute.toString().padStart(2, '0')
    } catch (_: Exception) {
        "N/A"
    }
}
