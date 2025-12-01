package com.example.smartattendance.ui.screens

import android.app.Activity
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import com.example.smartattendance.api.AuthApi
import com.example.smartattendance.ui.components.AppHeader
import com.example.smartattendance.ui.components.HeaderType
import com.example.smartattendance.ui.theme.AppFontFamily
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import java.text.SimpleDateFormat
import java.util.*

@Serializable
data class ScheduleDetailInfo(
    val schedule_id: Int,
    val course_id: Int,
    val day: String,
    val start_time: String,
    val end_time: String,
    val room: String?,
    val courses: CourseDetailInfo
)

@Serializable
data class CourseDetailInfo(
    val course_name: String,
    val course_code: String,
    val lecturer_id: String?,
    val users: UserInfo?
)

@Serializable
data class UserInfo(
    val full_name: String
)

@Serializable
data class EnrollmentInfo(
    val enrollment_id: Int
)

@Serializable
data class AttendanceHistory(
    val attendance_date: String,
    val status: String,
    val is_verified: Boolean
)

@Serializable
data class EnrollmentCount(
    val count: Int
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleDetailScreen(
    scheduleId: Int,
    courseId: Int,
    userId: String,
    onBackClick: () -> Unit
) {
    var isLoading by remember { mutableStateOf(true) }
    var scheduleDetail by remember { mutableStateOf<ScheduleDetailInfo?>(null) }
    var attendanceHistory by remember { mutableStateOf<List<AttendanceHistory>>(emptyList()) }
    var enrollmentCount by remember { mutableStateOf(0) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()
    val supabase = AuthApi.supabase

    // Set status bar color
    val view = LocalView.current
    LaunchedEffect(Unit) {
        val window = (view.context as? Activity)?.window
        window?.let {
            WindowCompat.setDecorFitsSystemWindows(it, false)
            it.statusBarColor = android.graphics.Color.parseColor("#FF2C2D32")
        }
    }

    // Load data
    LaunchedEffect(scheduleId, courseId, userId) {
        scope.launch {
            try {
                isLoading = true
                errorMessage = null

                // Get schedule detail with course info
                val scheduleResponse = supabase.postgrest["schedules"]
                    .select(
                        columns = Columns.raw(
                            """
                            schedule_id,
                            course_id,
                            day,
                            start_time,
                            end_time,
                            room,
                            courses!inner(
                                course_name,
                                course_code,
                                lecturer_id,
                                users!inner(full_name)
                            )
                            """.trimIndent()
                        )
                    ) {
                        filter {
                            eq("schedule_id", scheduleId)
                        }
                    }
                    .decodeList<ScheduleDetailInfo>()

                scheduleDetail = scheduleResponse.firstOrNull()

                // Get enrollment_id for this user and course
                val enrollmentResponse = supabase.postgrest["enrollments"]
                    .select(columns = Columns.list("enrollment_id", "course_id")) {
                        filter {
                            eq("user_id", userId)
                            eq("course_id", courseId)
                        }
                    }
                    .decodeList<EnrollmentInfo>()

                val enrollmentId = enrollmentResponse.firstOrNull()?.enrollment_id

                // Get attendance history
                if (enrollmentId != null) {
                    val attendanceResponse = supabase.postgrest["attendances"]
                        .select(columns = Columns.list("attendance_date", "status", "is_verified")) {
                            filter {
                                eq("enrollment_id", enrollmentId)
                                eq("schedule_id", scheduleId)
                            }
                        }
                        .decodeList<AttendanceHistory>()

                    attendanceHistory = attendanceResponse.sortedByDescending { it.attendance_date }
                }

                // Get number of students enrolled in this course
                val countResponse = supabase.postgrest["enrollments"]
                    .select(columns = Columns.list("enrollment_id")) {
                        filter {
                            eq("course_id", courseId)
                        }
                    }
                    .decodeList<EnrollmentInfo>()

                enrollmentCount = countResponse.size

            } catch (e: Exception) {
                errorMessage = "Failed to load schedule details: ${e.message}"
                Log.e("ScheduleDetailScreen", "Error loading data", e)
            } finally {
                isLoading = false
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        AppHeader(
            title = "Detail",
            headerType = HeaderType.BACK,
            onBackClick = onBackClick
        )

        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFF6F6F6)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color(0xFF2C2D32))
            }
        } else if (errorMessage != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFF6F6F6)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = errorMessage ?: "Error",
                    color = Color.Red,
                    fontFamily = AppFontFamily
                )
            }
        } else {
            scheduleDetail?.let { detail ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFFF6F6F6))
                        .verticalScroll(rememberScrollState())
                        .padding(24.dp)
                ) {
                    // Detail Card
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 13.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                "Detail",
                                color = Color(0xFF2C2D32),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = AppFontFamily,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )

                            DetailRow("Course", detail.courses.course_name)
                            DetailRow("Dosen", detail.courses.users?.full_name ?: "N/A")
                            DetailRow("Number of attendees", "$enrollmentCount/16")
                            DetailRow("Room", detail.room ?: "N/A", isLast = true)
                        }
                    }

                    // Attendance Status Card
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 13.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                "Attendance status",
                                color = Color(0xFF2C2D32),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = AppFontFamily,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )

                            if (attendanceHistory.isEmpty()) {
                                Text(
                                    "No attendance records yet",
                                    color = Color.Gray,
                                    fontSize = 14.sp,
                                    fontFamily = AppFontFamily,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            } else {
                                attendanceHistory.forEach { attendance ->
                                    AttendanceRow(
                                        date = formatDate(attendance.attendance_date),
                                        status = attendance.status
                                    )
                                }
                            }
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
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = if (isLast) 0.dp else 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Text(
            label,
            color = Color(0xFF2C2D32),
            fontSize = 14.sp,
            fontFamily = AppFontFamily,
            modifier = Modifier.weight(1f)
        )
        Text(
            value,
            color = Color(0xFF2C2D32),
            fontSize = 14.sp,
            fontFamily = AppFontFamily,
            modifier = Modifier.weight(1f),
            textAlign = androidx.compose.ui.text.style.TextAlign.End
        )
    }
}

@Composable
private fun AttendanceRow(
    date: String,
    status: String
) {
    val statusColor = when (status.lowercase()) {
        "present" -> Color(0xFF00CC2C)
        "late" -> Color(0xFFFF9D00)
        "absent" -> Color(0xFFE53E3E)
        "excused" -> Color(0xFF2196F3)
        "sick" -> Color(0xFF9C27B0)
        else -> Color.Gray
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            date,
            color = Color(0xFF2C2D32),
            fontSize = 14.sp,
            fontFamily = AppFontFamily
        )
        Text(
            status.uppercase(),
            color = statusColor,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = AppFontFamily
        )
    }
}

private fun formatDate(dateString: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val outputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val date = inputFormat.parse(dateString)
        if (date != null) outputFormat.format(date) else dateString
    } catch (e: Exception) {
        dateString
    }
}

