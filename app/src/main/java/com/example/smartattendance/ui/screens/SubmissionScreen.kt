package com.example.smartattendance.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smartattendance.api.AttendanceApi
import com.example.smartattendance.api.AuthApi
import com.example.smartattendance.utils.SessionManager
import kotlinx.coroutines.launch
import androidx.compose.ui.platform.LocalContext
import android.util.Log
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.ui.tooling.preview.Preview

import com.example.smartattendance.CurrentScheduleInfo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubmissionScreen(
    user: AuthApi.User?,
    sessionManager: SessionManager,
    scheduleInfo: CurrentScheduleInfo,
    onBackClick: () -> Unit = {},
    onSubmissionComplete: () -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val attendanceApi = remember { AttendanceApi(AuthApi.supabase) }

    var isSubmitting by remember { mutableStateOf(false) }
    var submissionResult by remember { mutableStateOf<Result<String>?>(null) }

    // Extract schedule details from scheduleInfo
    val courseName = scheduleInfo.courseName
    val time = scheduleInfo.time

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Submit Attendance") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Course info card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = courseName.uppercase(),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text(
                        text = time,
                        fontSize = 16.sp,
                        color = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Submit button
            Button(
                onClick = {
                    coroutineScope.launch {
                        isSubmitting = true
                        submissionResult = null

                        try {
                            // Assuming we have scheduleId and courseId from scheduleInfo
                            // For now, we'll need to get them from the schedule data
                            // This might need adjustment based on how scheduleInfo is structured
                            val result = attendanceApi.submitAttendance(
                                userId = user?.user_id ?: "",
                                scheduleId = scheduleInfo.scheduleId ?: 1, // TODO: Get from scheduleInfo
                                courseId = scheduleInfo.courseId ?: 1    // TODO: Get from scheduleInfo
                            )
                            submissionResult = result
                            if (result.isSuccess) {
                                Log.d("SubmissionScreen", "Attendance submitted successfully")
                                // Navigate back after success
                                onSubmissionComplete()
                            }
                        } catch (e: Exception) {
                            submissionResult = Result.failure(e)
                            Log.e("SubmissionScreen", "Error submitting attendance", e)
                        } finally {
                            isSubmitting = false
                        }
                    }
                },
                enabled = !isSubmitting,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE0697E))
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                } else {
                    Text(
                        text = "Submit Attendance",
                        fontSize = 16.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Result message
            submissionResult?.let { result ->
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = if (result.isSuccess) {
                        result.getOrNull() ?: "Success"
                    } else {
                        "Error: ${result.exceptionOrNull()?.message ?: "Unknown error"}"
                    },
                    color = if (result.isSuccess) Color.Green else Color.Red,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }
    }
}