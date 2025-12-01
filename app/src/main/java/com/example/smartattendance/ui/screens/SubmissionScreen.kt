package com.example.smartattendance.ui.screens

import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Pending
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.navigation.NavController
import com.example.smartattendance.api.AttendanceApi
import com.example.smartattendance.api.AuthApi
import com.example.smartattendance.ui.components.AppHeader
import com.example.smartattendance.ui.components.HeaderType
import com.example.smartattendance.ui.theme.AppFontFamily
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubmissionScreen(
    modifier: Modifier = Modifier,
    user: AuthApi.User?,
    navController: NavController,
    scheduleId: Int?,
    courseId: Int?,
    onAttendanceSubmitted: () -> Unit = {}
) {
    val scope = rememberCoroutineScope()
    var isSubmitting by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("") }

    // Validasi Supabase
    val supabaseClient = AuthApi.supabase
    val attendanceApi = supabaseClient?.let { AttendanceApi(it) }
    var attendanceChecked by remember { mutableStateOf(false) }
    var alreadyHasRow by remember { mutableStateOf(false) }
    var attendanceStatus by remember { mutableStateOf("") }
    var fromCamera by remember { mutableStateOf(false) }
    var isVerified by remember { mutableStateOf(false) }

    // Ambil detail kelas
    var courseName by remember { mutableStateOf("") }
    var day by remember { mutableStateOf("") }
    var startTime by remember { mutableStateOf("") }
    var endTime by remember { mutableStateOf("") }
    var roomInfo by remember { mutableStateOf("") }

    val userId = user?.user_id ?: ""
    val isReady = userId.isNotEmpty() && scheduleId != null && courseId != null && attendanceApi != null

    LaunchedEffect(courseId) {
        if (courseId != null && attendanceApi != null) {
            courseName = attendanceApi.getCourseName(courseId) ?: "Unknown Course"
        }
    }
    LaunchedEffect(scheduleId) {
        if (scheduleId != null && attendanceApi != null) {
            val info = attendanceApi.getScheduleInfo(scheduleId)
            if (info != null) {
                day = info.day ?: ""
                startTime = info.start_time ?: ""
                endTime = info.end_time ?: ""
                roomInfo = info.room ?: "-"
            }
        }
    }

    // Check today's attendance before showing submit button
    LaunchedEffect(userId, scheduleId, courseId) {
        if (isReady && !attendanceChecked) {
            val record = attendanceApi?.getTodayAttendance(userId, scheduleId!!, courseId!!)?.getOrNull()
            attendanceChecked = true
            if (record != null) {
                alreadyHasRow = true
                attendanceStatus = record.status
                fromCamera = record.from_camera
                isVerified = record.is_verified
                // Jika from_camera = false (user submitted manually) dan status pending dan belum verified
                // maka redirect ke pending screen
                if (!record.from_camera && record.status == "pending" && !record.is_verified) {
                    val encodedCourseName = java.net.URLEncoder.encode(courseName, "UTF-8")
                    navController.navigate("submission_complete_screen/pending/${encodedCourseName}/${courseId}/${scheduleId}")
                }
                // Jika sudah verified dan status bukan pending (present/late/absent/etc)
                // maka redirect ke complete screen dengan status final
                else if (record.is_verified && record.status != "pending") {
                    val encodedCourseName = java.net.URLEncoder.encode(courseName, "UTF-8")
                    navController.navigate("submission_complete_screen/${record.status}/${encodedCourseName}/${courseId}/${scheduleId}")
                }
                // Jika from_camera = true, is_verified = false, status = pending
                // maka TIDAK redirect, tetap di submission screen dan tampilkan tombol submit
                // karena user masih perlu submit manual attendance
            }
        }
    }

    // Format schedule time for display
    val formattedSchedule = remember(startTime, endTime) {
        try {
            val inputFormat = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault())
            val outputFormat = java.text.SimpleDateFormat("h:mma", java.util.Locale.getDefault())
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

    // Determine status color and icon
    val (statusColor, statusIcon) = when (attendanceStatus.lowercase()) {
        "present" -> Color(0xFF00CC2C) to Icons.Default.CheckCircle
        "late" -> Color(0xFFFF9D00) to Icons.Default.Schedule
        "pending" -> Color(0xFFFF9D00) to Icons.Default.Pending
        "absent" -> Color(0xFFE53E3E) to Icons.Default.Error
        else -> Color(0xFF2C2D32) to Icons.Default.Send
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
                title = "Submit Attendance",
                headerType = HeaderType.BACK,
                onBackClick = { navController.popBackStack() },
                showIcon = false
            )

            // Content Column
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(color = Color(0xFFF6F6F6))
                    .padding(top = 24.dp, start = 28.dp, end = 28.dp)
            ) {
                // Status Section
                if (alreadyHasRow) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(bottom = 24.dp)
                    ) {
                        Icon(
                            imageVector = statusIcon,
                            contentDescription = attendanceStatus,
                            tint = statusColor,
                            modifier = Modifier
                                .padding(bottom = 8.dp)
                                .size(120.dp)
                        )
                        Text(
                            attendanceStatus.uppercase(),
                            color = statusColor,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = AppFontFamily,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(bottom = 24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Submit",
                            tint = Color(0xFF2C2D32),
                            modifier = Modifier
                                .padding(bottom = 8.dp)
                                .size(120.dp)
                        )
                        Text(
                            "READY TO SUBMIT",
                            color = Color(0xFF2C2D32),
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = AppFontFamily,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                // Class Information Card
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
                            "Class Information",
                            color = Color(0xFF2C2D32),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = AppFontFamily,
                            modifier = Modifier.padding(bottom = 16.dp, start = 15.dp)
                        )

                        Column(
                            modifier = Modifier
                                .padding(horizontal = 16.dp)
                                .fillMaxWidth()
                                .padding(top = 4.dp, bottom = 22.dp)
                        ) {
                            DetailRow("Class", courseName)
                            DetailRow("Room", roomInfo)
                            DetailRow("Day", day)
                            DetailRow("Schedule", formattedSchedule, isLast = true)
                        }
                    }
                }

                // Warning message if camera detected but not verified
                if (alreadyHasRow && fromCamera && !isVerified && attendanceStatus == "pending") {
                    Card(
                        modifier = Modifier
                            .padding(bottom = 24.dp)
                            .fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3CD)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = "Warning",
                                tint = Color(0xFF856404),
                                modifier = Modifier
                                    .size(24.dp)
                                    .padding(end = 8.dp)
                            )
                            Text(
                                "Camera detected your presence. Please submit to confirm your attendance.",
                                color = Color(0xFF856404),
                                fontSize = 14.sp,
                                fontFamily = AppFontFamily,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }
                }

                // Show submit button if:
                // 1. No attendance record exists yet, OR
                // 2. Record exists but from_camera=true, is_verified=false, status=pending
                val shouldShowSubmitButton = !alreadyHasRow || (alreadyHasRow && fromCamera && !isVerified && attendanceStatus == "pending")

                if (shouldShowSubmitButton) {
                    Button(
                        onClick = {
                            if (isReady) {
                                isSubmitting = true
                                scope.launch {
                                    try {
                                        val result = attendanceApi.submitAttendance(userId, scheduleId, courseId)
                                        if (result.isSuccess) {
                                            val status = result.getOrNull()?.second ?: "pending"
                                            val encodedCourseName = java.net.URLEncoder.encode(courseName, "UTF-8")
                                            navController.navigate("submission_complete_screen/${status}/${encodedCourseName}/${courseId}/${scheduleId}")
                                        } else {
                                            message = "Failed to submit attendance: ${result.exceptionOrNull()?.message ?: "Unknown error"}"
                                        }
                                    } catch (e: Exception) {
                                        message = "Failed to submit: ${e.message ?: e.toString()}"
                                    } finally {
                                        isSubmitting = false
                                        onAttendanceSubmitted()
                                    }
                                }
                            } else {
                                message = "Invalid user, schedule, or course data."
                            }
                        },
                        enabled = !isSubmitting && isReady,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .padding(bottom = 16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFE0697E),
                            disabledContainerColor = Color(0xFFCCCCCC)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            if (isSubmitting) "Submitting..." else "Submit Attendance",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = AppFontFamily,
                            color = Color.White
                        )
                    }
                }

                // Error message
                if (message.isNotEmpty()) {
                    Card(
                        modifier = Modifier
                            .padding(bottom = 24.dp)
                            .fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = if (message.startsWith("Failed") || message.startsWith("Invalid"))
                                Color(0xFFF8D7DA) else Color(0xFFD4EDDA)
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (message.startsWith("Failed") || message.startsWith("Invalid"))
                                    Icons.Default.Error else Icons.Default.CheckCircle,
                                contentDescription = "Message",
                                tint = if (message.startsWith("Failed") || message.startsWith("Invalid"))
                                    Color(0xFF721C24) else Color(0xFF155724),
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                message,
                                color = if (message.startsWith("Failed") || message.startsWith("Invalid"))
                                    Color(0xFF721C24) else Color(0xFF155724),
                                fontSize = 14.sp,
                                fontFamily = AppFontFamily,
                                modifier = Modifier.padding(start = 12.dp)
                            )
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
    isLast: Boolean = false
) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .padding(bottom = if (isLast) 0.dp else 15.dp)
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
            color = if (label == "Class") Color(0xFF000000) else Color(0xFF2C2D32),
            fontSize = 14.sp,
            fontWeight = FontWeight.Normal,
            fontFamily = AppFontFamily,
            textAlign = TextAlign.Right,
            modifier = Modifier.weight(1f)
        )
    }
}
