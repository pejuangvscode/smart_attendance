package com.example.smartattendance.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smartattendance.ui.theme.SmartAttendanceTheme

data class AttendanceRecord(
    val subject: String,
    val type: String,
    val status: AttendanceStatus,
    val time: String? = null,
    val date: String? = null
)

enum class AttendanceStatus {
    PENDING, NOT_YET, PRESENT
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    onBackClick: () -> Unit = {}
) {
    val darkGray = Color(0xFF3A3A3A)

    // Sample data
    val attendanceRecords = listOf(
        AttendanceRecord("KECERDASAN KOMPUTASIONAL", "Laboratory Request Permission", AttendanceStatus.PENDING),
        AttendanceRecord("KECERDASAN KOMPUTASIONAL", "Request Permission", AttendanceStatus.PENDING),
        AttendanceRecord("KEAMANAN KOMPUTER & JARINGAN", "Laboratory", AttendanceStatus.NOT_YET, "10:00AM - 11:40AM"),
        AttendanceRecord("KEAMANAN KOMPUTER & JARINGAN", "", AttendanceStatus.NOT_YET, "7:15AM - 9:45AM"),
        AttendanceRecord("PERANCANGAN & PEMROGRAMAN WEB", "Laboratory", AttendanceStatus.PRESENT, "10:00AM - 11:40AM", "Thursday, 25 Sep 2025"),
        AttendanceRecord("PERANCANGAN & PEMROGRAMAN WEB", "", AttendanceStatus.PRESENT, "", "Thursday, 25 Sep 2025")
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
                    text = "History",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White
                )
            }
        }

        // Content
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                // Today section header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Today",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Icon(
                        imageVector = Icons.Default.FilterList,
                        contentDescription = "Filter",
                        tint = Color.Black,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // Today's records
            items(attendanceRecords.take(4)) { record ->
                AttendanceRecordItem(record = record)
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Thursday, 25 Sep 2025",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }

            // Previous day records
            items(attendanceRecords.drop(4)) { record ->
                AttendanceRecordItem(record = record)
            }
        }
    }
}

@Composable
fun AttendanceRecordItem(
    record: AttendanceRecord
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = record.subject,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            if (record.type.isNotEmpty()) {
                Text(
                    text = record.type,
                    fontSize = 12.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (record.time?.isNotEmpty() == true) {
                    Text(
                        text = record.time,
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }

                StatusIndicator(status = record.status)
            }
        }
    }
}

@Composable
fun StatusIndicator(
    status: AttendanceStatus
) {
    val (color, text) = when (status) {
        AttendanceStatus.PENDING -> Pair(Color(0xFFFFA726), "Pending")
        AttendanceStatus.NOT_YET -> Pair(Color.Gray, "Not Yet")
        AttendanceStatus.PRESENT -> Pair(Color(0xFF66BB6A), "Present")
    }

    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = text,
            fontSize = 12.sp,
            color = color,
            fontWeight = FontWeight.Medium
        )
    }
}

@Preview(showBackground = true)
@Composable
fun HistoryScreenPreview() {
    SmartAttendanceTheme {
        HistoryScreen()
    }
}
