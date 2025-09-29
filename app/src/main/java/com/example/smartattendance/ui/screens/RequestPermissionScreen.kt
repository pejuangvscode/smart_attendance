package com.example.smartattendance.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.example.smartattendance.ui.components.AppBottomNavigation
import com.example.smartattendance.ui.theme.SmartAttendanceTheme

data class PermissionRequest(
    val fromDate: String,
    val toDate: String,
    val status: PermissionStatus
)

enum class PermissionStatus {
    PENDING, APPROVED
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RequestPermissionScreen(
    onBackClick: () -> Unit = {},
    onCreateRequest: () -> Unit = {},
    onNavigate: (String) -> Unit = {}
) {
    val darkGray = Color(0xFF3A3A3A)

    // Sample data based on the image
    val permissionRequests = listOf(
        PermissionRequest("29 Sep, 25", "29 Sep, 25", PermissionStatus.PENDING),
        PermissionRequest("29 Sep, 25", "29 Sep, 25", PermissionStatus.PENDING),
        PermissionRequest("19 Sep, 25", "19 Sep, 25", PermissionStatus.APPROVED),
        PermissionRequest("19 Sep, 25", "19 Sep, 25", PermissionStatus.APPROVED),
        PermissionRequest("18 Sep, 25", "18 Sep, 25", PermissionStatus.APPROVED),
        PermissionRequest("18 Sep, 25", "18 Sep, 25", PermissionStatus.APPROVED),
        PermissionRequest("18 Sep, 25", "18 Sep, 25", PermissionStatus.APPROVED),
        PermissionRequest("18 Sep, 25", "18 Sep, 25", PermissionStatus.APPROVED),
        PermissionRequest("18 Sep, 25", "18 Sep, 25", PermissionStatus.APPROVED),
        PermissionRequest("18 Sep, 25", "18 Sep, 25", PermissionStatus.APPROVED),
        PermissionRequest("18 Sep, 25", "18 Sep, 25", PermissionStatus.APPROVED),
        PermissionRequest("18 Sep, 25", "18 Sep, 25", PermissionStatus.APPROVED),
        PermissionRequest("18 Sep, 25", "18 Sep, 25", PermissionStatus.APPROVED),
        PermissionRequest("18 Sep, 25", "18 Sep, 25", PermissionStatus.APPROVED)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // Top bar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = darkGray
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Request Permission",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Request Permission Button
            Button(
                onClick = onCreateRequest,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = darkGray
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "Request Permission",
                    fontSize = 16.sp,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Request Info Section
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
                    Icon(
                        imageVector = Icons.Default.FilterList,
                        contentDescription = "Filter",
                        tint = Color.Black,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add",
                        tint = Color.Black,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Table Header
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp, bottomStart = 0.dp, bottomEnd = 0.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
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

            // Permission Requests List
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                items(permissionRequests) { request ->
                    PermissionRequestItem(request = request)
                }
            }
        }

        // Add Bottom Navigation
        AppBottomNavigation(
            currentRoute = "schedule",
            onNavigate = onNavigate
        )
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
