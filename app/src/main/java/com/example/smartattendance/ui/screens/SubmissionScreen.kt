package com.example.smartattendance.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.LocationOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.view.WindowCompat
import androidx.navigation.NavController
import com.example.smartattendance.api.AttendanceApi
import com.example.smartattendance.api.AuthApi
import com.example.smartattendance.ui.components.AppHeader
import com.example.smartattendance.ui.components.HeaderType
import com.example.smartattendance.ui.theme.AppFontFamily
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

// Helper function to calculate distance between two coordinates using Haversine formula
private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Float {
    val earthRadius = 6371000.0 // Earth radius in meters
    val dLat = Math.toRadians(lat2 - lat1)
    val dLon = Math.toRadians(lon2 - lon1)
    val a = sin(dLat / 2) * sin(dLat / 2) +
            cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
            sin(dLon / 2) * sin(dLon / 2)
    val c = 2 * atan2(sqrt(a), sqrt(1 - a))
    return (earthRadius * c).toFloat()
}

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
    val context = LocalContext.current
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

    // Location checking states (only when no row exists yet)
    var isInZone by remember { mutableStateOf(false) }
    var isCheckingLocation by remember { mutableStateOf(false) }
    var locationError by remember { mutableStateOf<String?>(null) }
    var currentDistance by remember { mutableStateOf<Float?>(null) }
    var hasLocationPermission by remember { mutableStateOf(false) }

    // Target location (kampus)
    val targetLatitude = -6.234487925552364
    val targetLongitude = 106.59405922643447
    val maxDistanceMeters = 50f

    // Location permission launcher
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasLocationPermission = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
    }

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

    // Check location permission on load
    LaunchedEffect(Unit) {
        hasLocationPermission = ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasLocationPermission) {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
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

    // Check location only when there's no row yet
    @SuppressLint("MissingPermission")
    LaunchedEffect(hasLocationPermission, alreadyHasRow, attendanceChecked) {
        if (!alreadyHasRow && hasLocationPermission && attendanceChecked) {
            isCheckingLocation = true
            try {
                val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
                val cancellationTokenSource = CancellationTokenSource()

                val location = fusedLocationClient.getCurrentLocation(
                    Priority.PRIORITY_HIGH_ACCURACY,
                    cancellationTokenSource.token
                ).await()

                if (location != null) {
                    val distance = calculateDistance(
                        location.latitude,
                        location.longitude,
                        targetLatitude,
                        targetLongitude
                    )
                    currentDistance = distance
                    isInZone = distance <= maxDistanceMeters
                    locationError = null
                } else {
                    locationError = "Unable to get location"
                    isInZone = false
                }
            } catch (e: SecurityException) {
                locationError = "Location permission denied"
                isInZone = false
            } catch (e: Exception) {
                locationError = "Error getting location: ${e.message}"
                isInZone = false
            } finally {
                isCheckingLocation = false
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
                    // Show location status when no row exists
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(bottom = 24.dp)
                    ) {
                        if (isCheckingLocation) {
                            CircularProgressIndicator(
                                color = Color(0xFF2C2D32),
                                modifier = Modifier
                                    .padding(bottom = 8.dp)
                                    .size(120.dp)
                            )
                            Text(
                                "CHECKING LOCATION...",
                                color = Color(0xFF2C2D32),
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = AppFontFamily,
                                textAlign = TextAlign.Center
                            )
                        } else if (isInZone) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = "In Zone",
                                tint = Color(0xFF00CC2C),
                                modifier = Modifier
                                    .padding(bottom = 8.dp)
                                    .size(120.dp)
                            )
                            Text(
                                "YOU ARE IN THE ZONE",
                                color = Color(0xFF00CC2C),
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = AppFontFamily,
                                textAlign = TextAlign.Center
                            )
                            currentDistance?.let { dist ->
                                Text(
                                    "Distance: ${String.format("%.1f", dist)} meters",
                                    color = Color.Gray,
                                    fontSize = 14.sp,
                                    fontFamily = AppFontFamily,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        } else {
                            Icon(
                                imageVector = Icons.Default.LocationOff,
                                contentDescription = "Not In Zone",
                                tint = Color(0xFFE53E3E),
                                modifier = Modifier
                                    .padding(bottom = 8.dp)
                                    .size(120.dp)
                            )
                            Text(
                                "NOT IN ZONE",
                                color = Color(0xFFE53E3E),
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = AppFontFamily,
                                textAlign = TextAlign.Center
                            )
                            currentDistance?.let { dist ->
                                Text(
                                    "Distance: ${String.format("%.1f", dist)} meters",
                                    color = Color.Gray,
                                    fontSize = 14.sp,
                                    fontFamily = AppFontFamily,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                            locationError?.let { error ->
                                Text(
                                    error,
                                    color = Color(0xFFE53E3E),
                                    fontSize = 12.sp,
                                    fontFamily = AppFontFamily,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }
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

                // Warning message for location when no row exists
                if (!alreadyHasRow && !isInZone && !isCheckingLocation) {
                    Card(
                        modifier = Modifier
                            .padding(bottom = 24.dp)
                            .fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8D7DA)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOff,
                                contentDescription = "Location Warning",
                                tint = Color(0xFF721C24),
                                modifier = Modifier
                                    .size(24.dp)
                                    .padding(end = 8.dp)
                            )
                            Text(
                                "You must be within 50 meters of the campus location to submit attendance.",
                                color = Color(0xFF721C24),
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
                    // For new submissions (no row), user must be in zone
                    // For camera submissions (has row), no location check needed
                    val canSubmit = if (!alreadyHasRow) isInZone else true

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
                        enabled = !isSubmitting && isReady && canSubmit,
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
