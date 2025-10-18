package com.example.bookingcourt.presentation.court.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bookingcourt.domain.model.Court
import com.example.bookingcourt.domain.model.SportType
import com.example.bookingcourt.domain.model.CourtType
import com.example.bookingcourt.domain.model.Amenity
import com.example.bookingcourt.presentation.theme.Primary
import kotlinx.datetime.LocalTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    court: Court,
    onBackClick: () -> Unit,
    onBookClick: (Court) -> Unit
) {
    Scaffold(
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(Primary.copy(alpha = 0.3f))
            ) {
                // Action buttons overlay
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 30.dp, start = 20.dp, end = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color.White, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.Black
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        IconButton(
                            onClick = { },
                            modifier = Modifier
                                .size(36.dp)
                                .background(Color.White, CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Directions,
                                contentDescription = "Directions",
                                modifier = Modifier.size(20.dp)
                            )
                        }


                        IconButton(
                            onClick = { },
                            modifier = Modifier
                                .size(36.dp)
                                .background(Color.White, CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.FavoriteBorder,
                                contentDescription = "Favorite",
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Button(
                            onClick = { onBookClick(court) },
                            colors = ButtonDefaults.buttonColors(containerColor = Primary),
                            modifier = Modifier.height(36.dp)
                        ) {
                            Text("Đặt lịch", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Court Name
            Text(
                text = court.name,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Address
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = "Location",
                    tint = Primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = court.address,
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Info Cards
            InfoCard(
                title = "Loại sân",
                value = court.sportType.name
            )

            Spacer(modifier = Modifier.height(8.dp))

            InfoCard(
                title = "Giá",
                value = "${court.pricePerHour / 1000}.000 VNĐ/giờ"
            )

            Spacer(modifier = Modifier.height(8.dp))

            InfoCard(
                title = "Trạng thái",
                value = if (court.isActive) "Đang hoạt động" else "Tạm đóng cửa"
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Description
            Text(
                text = "Mô tả",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = court.description,
                fontSize = 14.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Book Button
            Button(
                onClick = { onBookClick(court) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Primary)
            ) {
                Text("Đặt sân ngay", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun InfoCard(title: String, value: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = title, fontWeight = FontWeight.Medium)
            Text(text = value, color = Primary, fontWeight = FontWeight.Bold)
        }
    }
}

// Preview Provider for Court
class SampleCourtProvider : PreviewParameterProvider<Court> {
    override val values = sequenceOf(
        Court(
            id = "1",
            name = "Sân cầu lông Thống Nhất",
            description = "Sân cầu lông chất lượng cao với đầy đủ tiện nghi, không gian thoáng mát, giá cả phải chăng.",
            address = "123 Nguyễn Văn Linh, Quận 7, TP.HCM",
            latitude = 10.7553411,
            longitude = 106.7423709,
            images = listOf("https://example.com/court1.jpg"),
            sportType = SportType.BADMINTON,
            courtType = CourtType.INDOOR,
            pricePerHour = 150000,
            openTime = LocalTime(7, 0),
            closeTime = LocalTime(22, 0),
            amenities = listOf(
                Amenity(id = "1", name = "Bãi đỗ xe", icon = "parking"),
                Amenity(id = "2", name = "Phòng tắm", icon = "shower")
            ),
            rules = "Vui lòng mang giày đúng quy định",
            ownerId = "owner1",
            rating = 4.5f,
            totalReviews = 120,
            isActive = true,
            maxPlayers = 4
        )
    )
}

@Preview(showBackground = true)
@Composable
fun DetailScreenPreview(
    @PreviewParameter(SampleCourtProvider::class) court: Court
) {
    DetailScreen(
        court = court,
        onBackClick = {},
        onBookClick = {}
    )
}
