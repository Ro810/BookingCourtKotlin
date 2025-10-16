package com.example.jatpackcompose.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import com.example.jatpackcompose.R
import androidx.compose.ui.graphics.Color
import com.example.jatpackcompose.ui.theme.DarkBlue

@Composable
fun SelectScreen(
    onCustomerClick: () -> Unit = {},
    onOwnerClick: () -> Unit = {}
) {
    var selectedRole by remember { mutableStateOf<String?>(null) } // "customer" or "owner"

    val blueGradient = Brush.verticalGradient(
        colors = listOf(
            Color.White,
            Color(0xFFD8E2FF), // Light Blue
            Color(0xFF8BB1F6)  // Mid Blue
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = blueGradient)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Spacer(modifier = Modifier.height(56.dp)) // Tăng khoảng cách phía trên để dịch bố cục xuống dưới một chút
        Image(
            painter = painterResource(id = R.drawable.logo1),
            contentDescription = "Hình ảnh quả cầu lông",
            modifier = Modifier
                .size(250.dp)
                .padding(bottom = 12.dp)
        )
        Text(
            text = "Cầu Lông",
            style = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 42.sp
            ),
            color = DarkBlue,
            modifier = Modifier.padding(bottom = 32.dp)
        )
        Spacer(modifier = Modifier.height(20.dp))

        // Nút Khách đặt sân
        Button(
            onClick = {
                selectedRole = "customer"
                onCustomerClick()
            },
            modifier = Modifier
                .width(300.dp)
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (selectedRole == "customer")
                    Color(0xFF123E62)
                else
                    Color.Gray,
                contentColor = Color.White
            )
        ) {
            Text("Tôi là khách đặt sân", fontSize = 18.sp)
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Nút Chủ sân
        OutlinedButton(
            onClick = {
                selectedRole = "owner"
                onOwnerClick()
            },
            modifier = Modifier
                .width(300.dp)
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (selectedRole == "owner")
                    Color(0xFF123E62)
                else
                    Color.Gray,
                contentColor = Color.White
            )
        ) {
            Text("Tôi là chủ sân", fontSize = 18.sp)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SelectScreenPreview() {
    SelectScreen()
}