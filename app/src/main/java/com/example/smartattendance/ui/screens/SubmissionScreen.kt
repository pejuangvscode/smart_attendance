package com.example.smartattendance.ui.screens

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.smartattendance.api.AttendanceApi
import com.example.smartattendance.api.AuthApi
import kotlinx.coroutines.launch
import androidx.navigation.NavController
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Error

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

    // Ambil detail kelas
    var courseName by remember { mutableStateOf("") }
    var scheduleInfo by remember { mutableStateOf("") }
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
                scheduleInfo = "${info.day} ${info.start_time} - ${info.end_time}"
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
                // Jika sudah ada row dan status pending serta is_verified false, langsung redirect
                if (record.status == "pending" && !record.is_verified) {
                    val encodedCourseName = java.net.URLEncoder.encode(courseName, "UTF-8")
                    navController.navigate("submission_complete_screen/pending/${encodedCourseName}/${courseId}/${scheduleId}")
                }
                // Jika sudah ada row dan is_verified true dan status bukan pending, langsung ke submission complete dengan logo CheckCircle
                if (record.is_verified && record.status != "pending") {
                    val encodedCourseName = java.net.URLEncoder.encode(courseName, "UTF-8")
                    navController.navigate("submission_complete_screen/${record.status}/${encodedCourseName}/${courseId}/${scheduleId}")
                }
            }
        }
    }

    Column(
        modifier = modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start
    ) {
        Text("Informasi Kelas", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Mata Kuliah: $courseName", fontSize = 16.sp)
        Text("Jadwal: $scheduleInfo", fontSize = 16.sp)
        Text("Ruangan: $roomInfo", fontSize = 16.sp)
        Spacer(modifier = Modifier.height(16.dp))
        Divider()
        Spacer(modifier = Modifier.height(16.dp))
        Text("Status Kehadiran: ${if (!alreadyHasRow) "Belum absen" else attendanceStatus}", fontSize = 16.sp, fontWeight = FontWeight.Medium)
        if (alreadyHasRow) {
            Spacer(modifier = Modifier.height(12.dp))
            Text("Segera masuk kelas agar wajah tertangkap kamera untuk absen dan akhirnya verified.", color = MaterialTheme.colorScheme.error, fontSize = 14.sp)
        }
        Spacer(modifier = Modifier.height(24.dp))
        if (!isReady) {
            Text("Error: Data tidak lengkap atau Supabase belum siap.\nuserId: $userId\nscheduleId: $scheduleId\ncourseId: $courseId\nSupabase: ${if (attendanceApi == null) "null" else "ready"}", color = MaterialTheme.colorScheme.error)
        }
        if (!alreadyHasRow) {
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
                                    message = "Gagal mencatat kehadiran: ${result.exceptionOrNull()?.message ?: "Unknown error"}"
                                }
                            } catch (e: Exception) {
                                message = "Gagal submit: ${e.message ?: e.toString()}"
                            } finally {
                                isSubmitting = false
                                onAttendanceSubmitted()
                            }
                        }
                    } else {
                        message = "User ID, schedule, course, atau Supabase tidak valid."
                    }
                },
                enabled = !isSubmitting && isReady
            ) {
                Text(if (isSubmitting) "Submitting..." else "Submit Attendance")
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        if (message.isNotEmpty()) {
            Text(message, color = if (message.startsWith("Gagal") || message.startsWith("Error")) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onBackground)
        }
        if (alreadyHasRow && attendanceStatus != "pending") {
            Row(verticalAlignment = Alignment.CenterVertically) {
                val icon = when (attendanceStatus) {
                    "present" -> Icons.Filled.CheckCircle
                    "late" -> Icons.Filled.Schedule
                    "absent" -> Icons.Filled.Error
                    else -> Icons.Filled.Error
                }
                Icon(icon, contentDescription = attendanceStatus.capitalize(), tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Status: ${attendanceStatus.capitalize()}", fontSize = 16.sp, fontWeight = FontWeight.Medium)
            }
        }
        if (alreadyHasRow && attendanceStatus == "pending") {
            Spacer(modifier = Modifier.height(12.dp))
            // Tidak perlu warning lagi jika status sudah present
        }
    }
}
