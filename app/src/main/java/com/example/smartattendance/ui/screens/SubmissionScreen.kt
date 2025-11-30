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
import androidx.navigation.compose.rememberNavController

@Composable
fun SubmissionScreen(
    user: AuthApi.User?,
    navController: NavController,
    scheduleId: Int?,
    courseId: Int?,
    onAttendanceSubmitted: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    var isSubmitting by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("") }

    // Validasi Supabase
    val supabaseClient = AuthApi.supabase
    val attendanceApi = remember { if (supabaseClient != null) AttendanceApi(supabaseClient) else null }

    // Validasi userId
    val userId = user?.user_id ?: ""
    val isReady = userId.isNotEmpty() && scheduleId != null && courseId != null && attendanceApi != null

    Column(
        modifier = modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Submit Attendance", fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(24.dp))
        if (!isReady) {
            Text("Error: Data tidak lengkap atau Supabase belum siap.\nuserId: $userId\nscheduleId: $scheduleId\ncourseId: $courseId\nSupabase: ${if (attendanceApi == null) "null" else "ready"}", color = MaterialTheme.colorScheme.error)
        }
        Button(
            onClick = {
                if (isReady) {
                    isSubmitting = true
                    scope.launch {
                        try {
                            val result = attendanceApi!!.submitAttendance(userId, scheduleId!!, courseId!!)
                            val (msg, status) = result.getOrElse { Pair(it.message ?: "Error", "pending") }
                            message = msg
                            val courseName = attendanceApi.getCourseName(courseId!!) ?: "Unknown Course"
                            val encodedCourseName = java.net.URLEncoder.encode(courseName, "UTF-8")
                            navController.navigate("submission_complete_screen/${status}/${encodedCourseName}/${courseId!!}/${scheduleId!!}")
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
        Spacer(modifier = Modifier.height(16.dp))
        if (message.isNotEmpty()) {
            Text(message, color = if (message.startsWith("Gagal") || message.startsWith("Error")) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onBackground)
        }
    }
}
