package com.example.smartattendance.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smartattendance.api.AttendanceDetailApi
import com.example.smartattendance.api.AuthApi
import com.example.smartattendance.api.AttendanceDetailData
import com.example.smartattendance.ui.components.AppHeader
import com.example.smartattendance.ui.components.HeaderType
import com.example.smartattendance.ui.components.AppBottomNavigation
import com.example.smartattendance.ui.theme.AppFontFamily

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubmitionComplete(
    status: String = "pending",
    courseName: String = "",
    courseId: Int = 0,
    scheduleId: Int = 0,
    onBackClick: () -> Unit = {},
    onNavigateHome: () -> Unit = {}
) {
    var detail by remember { mutableStateOf<AttendanceDetailData?>(null) }
    val supabaseClient = AuthApi.supabase

    LaunchedEffect(courseId, scheduleId) {
        if (supabaseClient != null && courseId != 0 && scheduleId != 0) {
            detail = AttendanceDetailApi.getAttendanceDetail(supabaseClient, courseId, scheduleId)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // Header
        AppHeader(
            title = "Attendance Detail",
            headerType = HeaderType.BACK,
            onBackClick = onBackClick
        )

        // Content
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(Color(0xFFF6F6F6))
                .padding(start = 28.dp, end = 28.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // Pending Icon
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .background(
                        color = Color(0xFFFFD54F),
                        shape = RoundedCornerShape(60.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    repeat(3) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .background(
                                    color = Color.White,
                                    shape = RoundedCornerShape(6.dp)
                                )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = status.uppercase(),
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = AppFontFamily,
                color = Color(0xFFFFD54F),
                letterSpacing = 2.sp
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Warning Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Warning!",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = AppFontFamily,
                        color = Color.Red,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Ensure your face is visible to the external camera so that your presence can be verified.",
                        fontSize = 14.sp,
                        fontFamily = AppFontFamily,
                        color = Color(0xFF666666),
                        lineHeight = 20.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Detail Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Detail",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = AppFontFamily,
                        color = Color(0xFF2C2D32)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Detail Items
                    DetailRow("Class", detail?.courseName ?: courseName)
                    DetailRow("Instructor", detail?.instructorName ?: "")
                    DetailRow("Room", detail?.room ?: "")
                    DetailRow("Date", detail?.attendanceDate ?: "")
                    DetailRow("Time", detail?.attendanceTime ?: "")
                    DetailRow("Status", status.uppercase(), statusColor = Color(0xFFFFD54F))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Class Information Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                shape = RoundedCornerShape(8.dp),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Class Information",
                        fontSize = 18.sp,
                        fontFamily = AppFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2C2D32)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    DetailRow("Instructor", detail?.instructorName ?: "")
                    DetailRow("Room", detail?.room ?: "")
                    DetailRow("Day", detail?.day ?: "")
                    DetailRow("Schedule", "${detail?.startTime ?: ""} - ${detail?.endTime ?: ""}")
                }
            }
        }
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String,
    statusColor: Color? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = Color(0xFF666666),
            modifier = Modifier.weight(1f),
            fontFamily = AppFontFamily
        )

        Text(
            text = value,
            fontSize = 14.sp,
            color = statusColor ?: Color(0xFF2C2D32),
            fontWeight = if (statusColor != null) FontWeight.Bold else FontWeight.Normal,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1f),
            fontFamily = AppFontFamily
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SubmitionCompletePreview() {
    SubmitionComplete()
}
