package com.example.smartattendance.ui.screens

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smartattendance.data.AttendanceReport
import com.example.smartattendance.data.AttendanceStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendanceDetailScreen(
    attendanceReport: AttendanceReport,
    onBackClick: () -> Unit = {},
    onEditClick: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // Top Bar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color(0xFF6366F1),
            shadowElevation = 4.dp
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
                        tint = Color.White
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Attendance Detail",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }

        // Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            // Status Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = when {
                        attendanceReport.isLate -> Color(0xFFFEF2F2)
                        attendanceReport.status == AttendanceStatus.APPROVED -> Color(0xFFF0FDF4)
                        else -> Color(0xFFFEF3C7)
                    }
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = when {
                                attendanceReport.isLate -> Icons.Default.Warning
                                attendanceReport.status == AttendanceStatus.APPROVED -> Icons.Default.CheckCircle
                                else -> Icons.Default.AccessTime
                            },
                            contentDescription = null,
                            tint = when {
                                attendanceReport.isLate -> Color(0xFFDC2626)
                                attendanceReport.status == AttendanceStatus.APPROVED -> Color(0xFF16A34A)
                                else -> Color(0xFFD97706)
                            },
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = if (attendanceReport.isLate) "Late Attendance" else "Regular Attendance",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = when {
                                attendanceReport.isLate -> Color(0xFFDC2626)
                                attendanceReport.status == AttendanceStatus.APPROVED -> Color(0xFF16A34A)
                                else -> Color(0xFFD97706)
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = attendanceReport.date,
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccessTime,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Time: ${attendanceReport.timestamp}",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Status Badge
                    Surface(
                        color = when (attendanceReport.status) {
                            AttendanceStatus.APPROVED -> Color(0xFF16A34A)
                            AttendanceStatus.REJECTED -> Color(0xFFDC2626)
                            AttendanceStatus.PENDING -> Color(0xFFD97706)
                            AttendanceStatus.NOT_YET -> Color(0xFF6B7280)
                            AttendanceStatus.PRESENT -> Color(0xFF16A34A)
                        },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = attendanceReport.status.name,
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Photo Evidence
            if (attendanceReport.photo != null) {
                Text(
                    text = "Photo Evidence",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF374151)
                )
                Spacer(modifier = Modifier.height(8.dp))

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Image(
                        bitmap = attendanceReport.photo.asImageBitmap(),
                        contentDescription = "Attendance Photo",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            // Late Reason (if applicable)
            if (attendanceReport.isLate && !attendanceReport.reason.isNullOrEmpty()) {
                Text(
                    text = "Reason for Late Attendance",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF374151)
                )
                Spacer(modifier = Modifier.height(8.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FAFB)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Text(
                        text = attendanceReport.reason,
                        modifier = Modifier.padding(16.dp),
                        fontSize = 14.sp,
                        color = Color(0xFF374151),
                        lineHeight = 20.sp
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Action Buttons (if status is pending)
            if (attendanceReport.status == AttendanceStatus.PENDING) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onEditClick,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Edit Report")
                    }

                    Button(
                        onClick = { /* Handle resubmit */ },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF6366F1)
                        )
                    ) {
                        Text("Resubmit")
                    }
                }
            }
        }
    }
}
