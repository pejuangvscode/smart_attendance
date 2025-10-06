package com.example.smartattendance.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smartattendance.ui.components.AppHeader
import com.example.smartattendance.ui.components.HeaderType
import com.example.smartattendance.ui.theme.AppFontFamily
import com.example.smartattendance.ui.theme.SmartAttendanceTheme

data class PermissionRequest(
    val fromDate: String,
    val toDate: String,
    val status: PermissionStatus
)

enum class PermissionStatus {
    PENDING, APPROVED, REJECTED
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RequestPermissionScreen(
    onBackClick: () -> Unit = {},
    onCreateRequest: () -> Unit = {},
    onNavigate: (String) -> Unit = {}
) {
    var selectedFilter by remember { mutableStateOf("All") }
    var showFilterMenu by remember { mutableStateOf(false) }

    // Sample data with different statuses including REJECTED
    val allPermissionRequests = listOf(
        PermissionRequest("6 Oct, 25", "6 Oct, 25", PermissionStatus.PENDING),
        PermissionRequest("5 Oct, 25", "5 Oct, 25", PermissionStatus.PENDING),
        PermissionRequest("4 Oct, 25", "4 Oct, 25", PermissionStatus.APPROVED),
        PermissionRequest("3 Oct, 25", "3 Oct, 25", PermissionStatus.APPROVED),
        PermissionRequest("2 Oct, 25", "2 Oct, 25", PermissionStatus.PENDING),
        PermissionRequest("1 Oct, 25", "1 Oct, 25", PermissionStatus.REJECTED),
        PermissionRequest("30 Sep, 25", "30 Sep, 25", PermissionStatus.APPROVED),
        PermissionRequest("29 Sep, 25", "29 Sep, 25", PermissionStatus.PENDING),
        PermissionRequest("28 Sep, 25", "28 Sep, 25", PermissionStatus.APPROVED),
        PermissionRequest("27 Sep, 25", "27 Sep, 25", PermissionStatus.REJECTED),
        PermissionRequest("26 Sep, 25", "26 Sep, 25", PermissionStatus.APPROVED),
        PermissionRequest("25 Sep, 25", "25 Sep, 25", PermissionStatus.APPROVED),
        PermissionRequest("24 Sep, 25", "24 Sep, 25", PermissionStatus.REJECTED),
        PermissionRequest("23 Sep, 25", "23 Sep, 25", PermissionStatus.PENDING),
        PermissionRequest("22 Sep, 25", "22 Sep, 25", PermissionStatus.APPROVED),
        PermissionRequest("21 Sep, 25", "21 Sep, 25", PermissionStatus.APPROVED),
        PermissionRequest("20 Sep, 25", "20 Sep, 25", PermissionStatus.REJECTED),
        PermissionRequest("19 Sep, 25", "19 Sep, 25", PermissionStatus.APPROVED),
        PermissionRequest("18 Sep, 25", "18 Sep, 25", PermissionStatus.PENDING),
        PermissionRequest("17 Sep, 25", "17 Sep, 25", PermissionStatus.REJECTED)
    )

    // Filter data based on selected filter
    val filteredRequests = when (selectedFilter) {
        "This week" -> allPermissionRequests.filter {
            // Filter for current week (simplified - you can enhance this logic)
            it.fromDate.contains("29 Sep") || it.fromDate.contains("30 Sep") || it.fromDate.contains("1 Oct") ||
            it.fromDate.contains("2 Oct") || it.fromDate.contains("3 Oct") || it.fromDate.contains("4 Oct")
        }
        "By date" -> allPermissionRequests // For now, show all. You can add date picker logic here
        else -> allPermissionRequests
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF6F6F6))
    ) {
        // Static Header
        AppHeader(
            title = "Request Permission",
            headerType = HeaderType.BACK,
            onBackClick = onBackClick,
            showIcon = false
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            // Static Request Permission Button
            Button(
                onClick = onCreateRequest,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF2C2D32)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "Request Permission",
                    fontSize = 16.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Medium,
                    fontFamily = AppFontFamily
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Static Request Info Section with Filter
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Request Info",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Row {
                    // Filter Icon
                    Box {
                        IconButton(
                            onClick = { showFilterMenu = true },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.FilterList,
                                contentDescription = "Filter",
                                tint = Color.Black,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // Filter Dropdown Menu
                        DropdownMenu(
                            expanded = showFilterMenu,
                            onDismissRequest = { showFilterMenu = false }
                        ) {
                            listOf("All", "This week", "By date").forEach { filter ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = filter,
                                            color = if (filter == selectedFilter) Color(0xFF2C2D32) else Color.Black
                                        )
                                    },
                                    onClick = {
                                        selectedFilter = filter
                                        showFilterMenu = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Add Icon
                    IconButton(
                        onClick = onCreateRequest,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add",
                            tint = Color.Black,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Static Table Header
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp, bottomStart = 0.dp, bottomEnd = 0.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "From",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "To",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "Status",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Static Permission Requests List
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                items(filteredRequests) { request ->
                    PermissionRequestItem(request = request)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun PermissionRequestItem(
    request: PermissionRequest
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(0.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = request.fromDate,
                fontSize = 12.sp,
                color = Color.Black,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = request.toDate,
                fontSize = 12.sp,
                color = Color.Black,
                modifier = Modifier.weight(1f)
            )

            val (statusColor, statusText) = when (request.status) {
                PermissionStatus.PENDING -> Pair(Color(0xFFFFA726), "Pending")
                PermissionStatus.APPROVED -> Pair(Color(0xFF66BB6A), "Approved")
                PermissionStatus.REJECTED -> Pair(Color(0xFFEF5350), "Rejected")
            }

            Text(
                text = statusText,
                fontSize = 12.sp,
                color = statusColor,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RequestPermissionScreenPreview() {
    SmartAttendanceTheme {
        RequestPermissionScreen()
    }
}
