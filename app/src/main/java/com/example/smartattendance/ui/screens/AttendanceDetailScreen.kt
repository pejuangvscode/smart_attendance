package com.example.smartattendance.ui.screens

import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTimeFilled
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HdrAuto
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Outbound
import androidx.compose.material.icons.filled.Pending
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import com.example.smartattendance.ui.components.AppHeader
import com.example.smartattendance.ui.components.HeaderType
import com.example.smartattendance.ui.theme.AppFontFamily
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendanceDetailScreen(
    className: String,
    status: String,
    courseCode: String = "",
    lecturerId: String = "",
    room: String = "",
    day: String = "",
    startTime: String = "",
    endTime: String = "",
    attendanceDate: String = "",
    recordedAt: String = "",
    onBackClick: () -> Unit = {},
    onNavigate: (String) -> Unit = {}
) {
    // Format time from recordedAt for submit time
    val submitTime = remember {
        try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val outputFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            val date = inputFormat.parse(recordedAt)
            if (date != null) outputFormat.format(date) else "N/A"
        } catch (e: Exception) {
            "N/A"
        }
    }

    // Format date for display
    val formattedDate = remember {
        try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val outputFormat = SimpleDateFormat("dd MMMM yyyy", Locale.ENGLISH)
            val date = inputFormat.parse(attendanceDate)
            if (date != null) outputFormat.format(date).uppercase() else attendanceDate
        } catch (e: Exception) {
            attendanceDate
        }
    }

    // Format schedule time for display
    val formattedSchedule = remember {
        try {
            val inputFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
            val outputFormat = SimpleDateFormat("h:mma", Locale.getDefault())
            val start = inputFormat.parse(startTime)
            val end = inputFormat.parse(endTime)
            if (start != null && end != null) {
                "${outputFormat.format(start)} - ${outputFormat.format(end)}"
            } else {
                "$startTime - $endTime"
            }
        } catch (e: Exception) {
            "$startTime - $endTime"
        }
    }

    // Set status bar color to match header
    val view = LocalView.current
    LaunchedEffect(Unit) {
        val activity = view.context as ComponentActivity
        WindowCompat.setDecorFitsSystemWindows(activity.window, false)
        WindowCompat.getInsetsController(activity.window, view)?.let { controller ->
            controller.isAppearanceLightStatusBars = false
            activity.window.statusBarColor = android.graphics.Color.parseColor("#FF2C2D32")
        }
    }

    // Determine status color and icon based on status
    val (statusColor, statusIcon) = when (status.lowercase()) {
        "present" -> Color(0xFF00CC2C) to Icons.Default.CheckCircle
        "absent" -> Color(0xFFE53E3E) to Icons.Default.HdrAuto
        "late" -> Color(0xFFFF9D00) to Icons.Default.AccessTimeFilled
        "pending" -> Color(0xFFFF9D00) to Icons.Default.Pending
        "approved" -> Color(0xFF4285F4) to Icons.Default.CheckCircle
        "rejected" -> Color(0xFFE53E3E) to Icons.Default.Cancel
        "not yet" -> Color(0xFF2C2D32) to Icons.Default.Info
        "sick" -> Color(0xFF9C27B0) to Icons.Default.AddCircle
        "excused" -> Color(0xFF607D8B) to Icons.Default.Outbound
        else -> Color(0xFF00CC2C) to Icons.Default.CheckCircle
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .background(color = Color(0xFFFFFFFF))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(color = Color(0xFFF6F6F6))
                .verticalScroll(rememberScrollState())
        ) {
            AppHeader(
                title = "Attendance Detail",
                headerType = HeaderType.BACK,
                onBackClick = onBackClick,
                showIcon = false,
                iconRes = statusIcon
            )

            // Content Column
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = Color(0xFFF6F6F6),
                    )
                    .padding(top = 24.dp, start = 28.dp, end = 28.dp)
            ) {
                // Status Section
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(bottom = 24.dp)
                ) {
                    Icon(
                        imageVector = statusIcon,
                        contentDescription = status,
                        tint = statusColor,
                        modifier = Modifier
                            .padding(bottom = 8.dp, start = 28.dp, end = 28.dp)
                            .size(120.dp)
                    )
                    Text(
                        status.uppercase(),
                        color = statusColor,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = AppFontFamily,
                        textAlign = TextAlign.Center
                    )
                }

                // Detail Card
                Card(
                    modifier = Modifier
                        .padding(bottom = 24.dp)
                        .fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 13.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(vertical = 10.dp)
                    ) {
                        Text(
                            "Detail",
                            color = Color(0xFF2C2D32),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = AppFontFamily,
                            modifier = Modifier
                                .padding(bottom = 16.dp, start = 15.dp)
                        )

                        Column(
                            modifier = Modifier
                                .padding(horizontal = 16.dp)
                                .fillMaxWidth()
                                .padding(top = 4.dp, bottom = 22.dp)
                        ) {
                            DetailRow("Class", className)
                            DetailRow("Course Code", courseCode)
                            DetailRow("Instructor", lecturerId)
                            DetailRow("Room", room.ifEmpty { "N/A" })
                            DetailRow("Date", formattedDate)
                            DetailRow("Submit time", submitTime)
                            DetailRow("Status", status.uppercase(), isStatus = true, statusColor = statusColor, isLast = true)
                        }
                    }
                }

                // Class Information Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 13.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(vertical = 10.dp)
                    ) {
                        Text(
                            "Class Information",
                            color = Color(0xFF2C2D32),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = AppFontFamily,
                            modifier = Modifier
                                .padding(bottom = 16.dp, start = 15.dp)
                        )

                        Column(
                            modifier = Modifier
                                .padding(horizontal = 16.dp)
                                .fillMaxWidth()
                        ) {
                            DetailRow("Day", day, hasBottomPadding = true)
                            DetailRow("Schedule", formattedSchedule, isLast = true)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String,
    isStatus: Boolean = false,
    statusColor: Color = Color.Transparent,
    hasBottomPadding: Boolean = false,
    isLast: Boolean = false
) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .padding(bottom = if (isLast) 0.dp else if (hasBottomPadding) 10.dp else 15.dp)
            .fillMaxWidth()
    ) {
        Text(
            label,
            color = Color(0xFF2C2D32),
            fontSize = 14.sp,
            fontFamily = AppFontFamily,
            modifier = Modifier
                .padding(end = 4.dp)
                .weight(1f)
        )
        Text(
            value,
            color = if (isStatus) statusColor else if (label == "Class") Color(0xFF000000) else Color(0xFF2C2D32),
            fontSize = 14.sp,
            fontWeight = if (isStatus) FontWeight.Bold else FontWeight.Normal,
            fontFamily = AppFontFamily,
            textAlign = TextAlign.Right,
            modifier = Modifier.weight(1f)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun AttendanceDetailScreenPreview() {
    AttendanceDetailScreen(
        className = "Mobile Application Development",
        status = "Excused"
    )
}