package com.example.bookingcourt.presentation.filter

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bookingcourt.presentation.theme.Primary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterScreen(
    onBackClick: () -> Unit,
    onApplyFilter: () -> Unit
) {
    var searchByArea by remember { mutableStateOf(true) } // true = khu vực, false = khoảng cách
    var selectedCity by remember { mutableStateOf("") }
    var selectedDistrict by remember { mutableStateOf("") }
    var selectedDistance by remember { mutableStateOf(5f) } // km
    var selectedPrice by remember { mutableStateOf(0f..500f) }
    var expandedCity by remember { mutableStateOf(false) }
    var expandedDistrict by remember { mutableStateOf(false) }

    // Danh sách tỉnh/thành phố
    val cities = listOf(
        "Hồ Chí Minh",
        "Hà Nội",
        "Đà Nẵng",
        "Cần Thơ",
        "Hải Phòng"
    )

    // Danh sách quận/huyện theo thành phố
    val districtsByCity = mapOf(
        "Hồ Chí Minh" to listOf("Quận 1", "Quận 2", "Quận 3", "Quận 5", "Quận 7", "Quận 9", "Gò Vấp", "Bình Thạnh", "Phú Nhuận", "Tân Bình"),
        "Hà Nội" to listOf("Ba Đình", "Hoàn Kiếm", "Hai Bà Trưng", "Đống Đa", "Tây Hồ", "Cầu Giấy", "Thanh Xuân", "Hoàng Mai"),
        "Đà Nẵng" to listOf("Hải Châu", "Thanh Khê", "Sơn Trà", "Ngũ Hành Sơn", "Liên Chiểu", "Cẩm Lệ"),
        "Cần Thơ" to listOf("Ninh Kiều", "Cái Răng", "Bình Thủy", "Ô Môn", "Thốt Nốt"),
        "Hải Phòng" to listOf("Hồng Bàng", "Ngô Quyền", "Lê Chân", "Hải An", "Kiến An", "Đồ Sơn")
    )

    // Reset district when city changes
    LaunchedEffect(selectedCity) {
        selectedDistrict = ""
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Bộ lọc", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Primary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Search Type Selection
            Text(
                text = "Tìm kiếm theo",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = searchByArea,
                    onClick = { searchByArea = true },
                    label = { Text("Khu vực") },
                    modifier = Modifier.weight(1f),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Primary,
                        selectedLabelColor = Color.White
                    )
                )
                FilterChip(
                    selected = !searchByArea,
                    onClick = { searchByArea = false },
                    label = { Text("Khoảng cách") },
                    modifier = Modifier.weight(1f),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Primary,
                        selectedLabelColor = Color.White
                    )
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Area or Distance Filter
            if (searchByArea) {
                // City Selection
                Text(
                    text = "Tỉnh/Thành phố",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(8.dp))

                ExposedDropdownMenuBox(
                    expanded = expandedCity,
                    onExpandedChange = { expandedCity = it }
                ) {
                    OutlinedTextField(
                        value = selectedCity,
                        onValueChange = {},
                        readOnly = true,
                        placeholder = { Text("Chọn tỉnh/thành phố") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCity) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                        colors = OutlinedTextFieldDefaults.colors()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedCity,
                        onDismissRequest = { expandedCity = false }
                    ) {
                        cities.forEach { city ->
                            DropdownMenuItem(
                                text = { Text(city) },
                                onClick = {
                                    selectedCity = city
                                    expandedCity = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // District Selection
                Text(
                    text = "Quận/Huyện",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(8.dp))

                ExposedDropdownMenuBox(
                    expanded = expandedDistrict,
                    onExpandedChange = { expandedDistrict = it && selectedCity.isNotEmpty() }
                ) {
                    OutlinedTextField(
                        value = selectedDistrict,
                        onValueChange = {},
                        readOnly = true,
                        placeholder = { Text(if (selectedCity.isEmpty()) "Vui lòng chọn thành phố trước" else "Chọn quận/huyện") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedDistrict) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                        enabled = selectedCity.isNotEmpty(),
                        colors = OutlinedTextFieldDefaults.colors()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedDistrict,
                        onDismissRequest = { expandedDistrict = false }
                    ) {
                        districtsByCity[selectedCity]?.forEach { district ->
                            DropdownMenuItem(
                                text = { Text(district) },
                                onClick = {
                                    selectedDistrict = district
                                    expandedDistrict = false
                                }
                            )
                        }
                    }
                }
            } else {
                // Distance Filter
                Text(
                    text = "Khoảng cách",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(8.dp))

                Slider(
                    value = selectedDistance,
                    onValueChange = { selectedDistance = it },
                    valueRange = 1f..20f,
                    steps = 18,
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "Trong bán kính ${selectedDistance.toInt()} km",
                    color = Primary,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Price Range Filter - ALWAYS VISIBLE
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = Color.LightGray
            )

            Text(
                text = "Khoảng giá (nghìn VNĐ/giờ)",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(8.dp))

            RangeSlider(
                value = selectedPrice,
                onValueChange = { selectedPrice = it },
                valueRange = 0f..500f,
                steps = 10
            )

            Text(
                text = "${selectedPrice.start.toInt()} - ${selectedPrice.endInclusive.toInt()} nghìn VNĐ",
                color = Primary,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Apply Button
            Button(
                onClick = onApplyFilter,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Primary)
            ) {
                Text("Áp dụng", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Reset Button
            OutlinedButton(
                onClick = {
                    searchByArea = true
                    selectedCity = ""
                    selectedDistrict = ""
                    selectedDistance = 5f
                    selectedPrice = 0f..500f
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text("Đặt lại", fontSize = 16.sp, color = Primary)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun FilterScreenPreview() {
    FilterScreen(
        onBackClick = { },
        onApplyFilter = { }
    )
}
