package com.example.bookingcourt.presentation.select

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bookingcourt.R
import com.example.bookingcourt.presentation.theme.BookingCourtTheme
import com.example.bookingcourt.presentation.theme.DarkBlue

@Composable
fun SelectScreen(
    onCustomerClick: () -> Unit = {},
    onOwnerClick: () -> Unit = {},
) {
    var selectedRole by remember { mutableStateOf<String?>(null) }

    val blueGradient = Brush.verticalGradient(
        colors = listOf(
            Color.White,
            Color(0xFFD8E2FF),
            Color(0xFF8BB1F6),
        ),
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = blueGradient)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top,
    ) {
        Spacer(modifier = Modifier.height(56.dp))

        Image(
            painter = painterResource(id = R.drawable.logo1),
            contentDescription = "Hình ảnh quả cầu lông",
            modifier = Modifier
                .size(250.dp)
                .padding(bottom = 12.dp),
        )

        Text(
            text = "Cầu Lông",
            style = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 42.sp,
            ),
            color = DarkBlue,
            modifier = Modifier.padding(bottom = 32.dp),
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {
                selectedRole = "customer"
                onCustomerClick()
            },
            modifier = Modifier
                .width(300.dp)
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (selectedRole == "customer") {
                    Color(0xFF123E62)
                } else {
                    Color.Gray
                },
                contentColor = Color.White,
            ),
        ) {
            Text("Tôi là khách đặt sân", fontSize = 18.sp)
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = {
                selectedRole = "owner"
                onOwnerClick()
            },
            modifier = Modifier
                .width(300.dp)
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (selectedRole == "owner") {
                    Color(0xFF123E62)
                } else {
                    Color.Gray
                },
                contentColor = Color.White,
            ),
        ) {
            Text("Tôi là chủ sân", fontSize = 18.sp)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SelectScreenPreview() {
    BookingCourtTheme {
        SelectScreen()
    }
}
