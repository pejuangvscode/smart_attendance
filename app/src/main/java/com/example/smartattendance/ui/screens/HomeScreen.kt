package com.example.smartattendance.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ExitToApp
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smartattendance.api.AuthApi
import com.example.smartattendance.api.StatisticsApi
import com.example.smartattendance.api.ScheduleApi
import com.example.smartattendance.data.AttendanceStatistics
import java.time.LocalTime
import com.example.smartattendance.ui.theme.SmartAttendanceTheme
import com.example.smartattendance.utils.SessionManager
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.abs
import kotlinx.coroutines.launch
import androidx.compose.ui.platform.LocalContext
import android.util.Log
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.compose.ui.platform.LocalView
import android.app.Activity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    user: AuthApi.User?,
    sessionManager: SessionManager,
    onLogout: () -> Unit = {},
    onSubmitAttendance: () -> Unit = {},
    onDateClick: () -> Unit = {},
    onRequestPermission: () -> Unit = {},
    onBackClick: () -> Unit = {}
) {
    val darkGray = Color(0xFF2C2D32)
    val redColor = Color(0xFFE0697E)
    val coroutineScope = rememberCoroutineScope()

    // Set status bar color to match header
    val view = LocalView.current
    val window = (view.context as? Activity)?.window
    window?.let {
        WindowCompat.setDecorFitsSystemWindows(it, false)
        val controller = WindowInsetsControllerCompat(it, view)
        controller.isAppearanceLightStatusBars = true // Light icons on dark background
        it.statusBarColor = 0xFF2C2D32.toInt()
    }

    // State untuk statistik presensi
    var attendanceStatistics by remember { mutableStateOf(AttendanceStatistics.empty()) }
    var isLoadingStatistics by remember { mutableStateOf(false) }
    var statisticsError by remember { mutableStateOf<String?>(null) }

    // State untuk weekly status
    var weeklyStatus by remember { mutableStateOf<Map<String, String?>>(emptyMap()) }
    var isLoadingWeekly by remember { mutableStateOf(false) }

    // State untuk current schedule
    var currentSchedule by remember { mutableStateOf<CurrentScheduleInfo?>(null) }
    var isLoadingSchedule by remember { mutableStateOf(false) }

    // Initialize StatisticsApi and ScheduleApi
    val statisticsApi = remember { StatisticsApi(AuthApi.supabase) }
    val scheduleApi = remember { ScheduleApi(AuthApi.supabase) }

    // Fetch statistics saat pertama kali load
    LaunchedEffect(user?.user_id) {
        user?.user_id?.let { userId ->
            isLoadingStatistics = true
            isLoadingWeekly = true
            isLoadingSchedule = true
            statisticsError = null

            try {
                // Fetch overall statistics
                val statsResult = statisticsApi.getUserStatistics(userId)
                statsResult.fold(
                    onSuccess = { statistics ->
                        attendanceStatistics = AttendanceStatistics.fromOverallStatistic(statistics)
                        Log.d("HomeScreen", "Statistics loaded: $attendanceStatistics")
                    },
                    onFailure = { error ->
                        statisticsError = error.message ?: "Failed to load statistics"
                        Log.e("HomeScreen", "Error loading statistics", error)
                    }
                )

                // Fetch weekly status
                val weeklyResult = statisticsApi.getWeeklyStatus(userId)
                weeklyResult.fold(
                    onSuccess = { weekly ->
                        weeklyStatus = weekly
                        Log.d("HomeScreen", "Weekly status loaded: $weeklyStatus")
                    },
                    onFailure = { error ->
                        Log.e("HomeScreen", "Error loading weekly status", error)
                    }
                )

                // Fetch today's schedules
                val todayResult = scheduleApi.getTodaySchedules(userId)
                todayResult.fold(
                    onSuccess = { schedules ->
                        Log.d("HomeScreen", "Today schedules loaded: ${schedules.size} items")
                        // Determine current or next class
                        currentSchedule = determineCurrentSchedule(schedules)
                        Log.d("HomeScreen", "Current schedule: $currentSchedule")
                    },
                    onFailure = { error ->
                        Log.e("HomeScreen", "Error loading today schedules", error)
                    }
                )
            } catch (e: Exception) {
                statisticsError = e.message ?: "Unknown error occurred"
                Log.e("HomeScreen", "Exception loading statistics", e)
            } finally {
                isLoadingStatistics = false
                isLoadingWeekly = false
                isLoadingSchedule = false
            }
        }
    }

    // Get current date
    val currentDate = remember { LocalDate.now() }
    val dayOfMonth = currentDate.dayOfMonth
    val dayOfWeek = currentDate.dayOfWeek
    val month = currentDate.format(DateTimeFormatter.ofPattern("MMMM", Locale.ENGLISH))
    val year = currentDate.year
    val dayName = dayOfWeek.getDisplayName(java.time.format.TextStyle.FULL, Locale.ENGLISH)


    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Background split - dark gray top, white bottom
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.4f)
                    .background(color = darkGray)
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.6f)
                    .background(color = Color.White)
            )
        }

        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Header section
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 25.dp, vertical = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Hello, ${user?.full_name?.split(" ")?.firstOrNull() ?: "User"}!",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "${user?.nim ?: "Unknown"}/Semester Ganjil",
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }

                IconButton(onClick = {
                    coroutineScope.launch {
                        sessionManager.clearSession()
                        onLogout()
                    }
                }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ExitToApp,
                        contentDescription = "Logout",
                        tint = Color.White,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Cards section with proper padding
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 25.dp)
            ) {
                // Take attendance card - dynamic based on schedule
                if (isLoadingSchedule) {
                    // Loading state
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                color = darkGray,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                } else {
                    // Show current or upcoming schedule
                    currentSchedule?.let { schedule ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.CalendarToday,
                                    contentDescription = "Calendar",
                                    tint = Color.Black,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column(
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = schedule.courseName.uppercase(),
                                        fontSize = 14.sp,
                                        color = Color.Black,
                                        fontWeight = FontWeight.Medium,
                                        maxLines = 2
                                    )
                                    Text(
                                        text = schedule.time,
                                        fontSize = 12.sp,
                                        color = Color.Gray,
                                        modifier = Modifier.padding(top = 2.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                if (schedule.isActive) {
                                    // Show Submit button for active class
                                    Button(
                                        onClick = onSubmitAttendance,
                                        colors = ButtonDefaults.buttonColors(containerColor = redColor),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.height(32.dp)
                                    ) {
                                        Text("Submit", fontSize = 14.sp, color = Color.White)
                                    }
                                } else {
                                    // Show "Not Yet" indicator for upcoming class
                                    Surface(
                                        color = Color(0xFFE0E0E0),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.height(32.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier.padding(horizontal = 12.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "Not Yet",
                                                fontSize = 14.sp,
                                                color = Color.Gray,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Date card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onDateClick() },
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = dayOfMonth.toString(),
                                fontSize = 70.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(
                                    text = dayName,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.Black
                                )
                                Text(
                                    text = "$month $year",
                                    fontSize = 14.sp,
                                    color = Color.Gray
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Text(
                            text = "This week status",
                            fontSize = 14.sp,
                            color = Color.Gray,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        if (isLoadingWeekly) {
                            // Loading state for weekly status
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    color = darkGray,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        } else {
                            // Display weekly status from database
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                WeekDayIndicator(
                                    day = "M",
                                    status = weeklyStatus["monday"],
                                    darkGray = darkGray,
                                    redColor = redColor
                                )
                                WeekDayIndicator(
                                    day = "T",
                                    status = weeklyStatus["tuesday"],
                                    darkGray = darkGray,
                                    redColor = redColor
                                )
                                WeekDayIndicator(
                                    day = "W",
                                    status = weeklyStatus["wednesday"],
                                    darkGray = darkGray,
                                    redColor = redColor
                                )
                                WeekDayIndicator(
                                    day = "Th",
                                    status = weeklyStatus["thursday"],
                                    darkGray = darkGray,
                                    redColor = redColor
                                )
                                WeekDayIndicator(
                                    day = "F",
                                    status = weeklyStatus["friday"],
                                    darkGray = darkGray,
                                    redColor = redColor
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Statistics Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Text(
                            text = "This Semester",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        if (isLoadingStatistics) {
                            // Loading state
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(100.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    color = darkGray,
                                    modifier = Modifier.size(40.dp)
                                )
                            }
                        } else if (statisticsError != null) {
                            // Error state
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(100.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Failed to load statistics",
                                    color = Color.Red,
                                    fontSize = 14.sp
                                )
                            }
                        } else {
                            // Donut charts - Scrollable horizontal dengan 5 charts
                            LazyRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(20.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp)
                            ) {
                                item {
                                    IndividualDonutChart(
                                        value = attendanceStatistics.present,
                                        percentage = attendanceStatistics.presentPercentage,
                                        label = "Present",
                                        color = Color(0xFF4CAF50)
                                    )
                                }
                                item {
                                    IndividualDonutChart(
                                        value = attendanceStatistics.late,
                                        percentage = attendanceStatistics.latePercentage,
                                        label = "Late",
                                        color = Color(0xFFFF9800)
                                    )
                                }
                                item {
                                    IndividualDonutChart(
                                        value = attendanceStatistics.excused,
                                        percentage = attendanceStatistics.excusedPercentage,
                                        label = "Excused",
                                        color = Color(0xFF2196F3)
                                    )
                                }
                                item {
                                    IndividualDonutChart(
                                        value = attendanceStatistics.sick,
                                        percentage = attendanceStatistics.sickPercentage,
                                        label = "Sick",
                                        color = Color(0xFFFFC107)
                                    )
                                }
                                item {
                                    IndividualDonutChart(
                                        value = attendanceStatistics.absent,
                                        percentage = attendanceStatistics.absentPercentage,
                                        label = "Absent",
                                        color = Color(0xFF9E9E9E)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Request Permission card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onRequestPermission() },
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Description,
                            contentDescription = "Request Permission",
                            tint = Color.Black,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Ask Permission",
                            fontSize = 14.sp,
                            color = Color.Black
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@Composable
fun WeekDayIndicator(
    day: String,
    status: String?,
    darkGray: Color,
    redColor: Color
) {
    // Determine color based on attendance status
    val color = when (status) {
        "present" -> Color(0xFF4CAF50)  // Green for present
        "late" -> Color(0xFFFF9800)      // Orange for late
        "absent" -> redColor              // Red for absent
        "excused" -> Color(0xFF2196F3)   // Blue for excused
        "sick" -> Color(0xFFFFC107)      // Yellow for sick
        null -> Color.LightGray           // Light gray for no data
        else -> Color.LightGray
    }

    val attended = status != null

    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(if (attended) color else Color.White)
            .border(2.dp, color, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = day,
            color = if (attended) Color.White else color,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp
        )
    }
}

@Composable
fun IndividualDonutChart(
    value: Int,
    percentage: Float,
    label: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier.size(60.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokeWidth = 8.dp.toPx()
                // Background circle
                drawCircle(
                    color = Color.LightGray.copy(alpha = 0.3f),
                    style = Stroke(width = strokeWidth)
                )
                // Foreground arc
                drawArc(
                    color = color,
                    startAngle = -90f,
                    sweepAngle = percentage * 360 / 100, // Convert percentage to angle
                    useCenter = false,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }
            Text(
                text = value.toString(),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color.Gray
        )
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    SmartAttendanceTheme {
        HomeScreen(user = AuthApi.User(full_name = "Teo", email = "", password_hash = ""), sessionManager = SessionManager(LocalContext.current))
    }
}

// Data class for current schedule info
data class CurrentScheduleInfo(
    val courseName: String,
    val time: String,
    val isActive: Boolean  // true if class is currently in session (within 15 min window)
)

// Local data class to match ScheduleApi.ScheduleItem structure
private data class ScheduleItemData(
    val title: String,
    val time: String
)

// Helper function to determine current or next schedule
private fun determineCurrentSchedule(schedules: List<Any>): CurrentScheduleInfo? {
    if (schedules.isEmpty()) return null

    val now = LocalTime.now()
    val formatter = DateTimeFormatter.ofPattern("HH:mm:ss")

    // Find if there's a class currently active (within 15 minutes of start time)
    for (schedule in schedules) {
        try {
            // Access properties using reflection
            val titleField = schedule::class.java.getDeclaredField("title")
            val timeField = schedule::class.java.getDeclaredField("time")
            titleField.isAccessible = true
            timeField.isAccessible = true

            val title = titleField.get(schedule) as String
            val time = timeField.get(schedule) as String

            // Extract course name from title (format: "Course Name - CODE - ROOM")
            val courseName = title.split(" - ").getOrNull(0) ?: title
            val timeRange = time.split(" - ")
            if (timeRange.size != 2) continue

            val startTimeStr = convertTo24Hour(timeRange[0].trim())
            val endTimeStr = convertTo24Hour(timeRange[1].trim())

            val startTime = LocalTime.parse(startTimeStr, formatter)
            val endTime = LocalTime.parse(endTimeStr, formatter)

            // Check if class is in progress or within 15 minutes before start
            val fifteenMinutesBefore = startTime.minusMinutes(15)

            if (now.isAfter(fifteenMinutesBefore) && now.isBefore(endTime)) {
                // Class is active or about to start
                return CurrentScheduleInfo(
                    courseName = courseName,
                    time = time,
                    isActive = true
                )
            }
        } catch (e: Exception) {
            Log.e("HomeScreen", "Error parsing schedule", e)
        }
    }

    // No active class, find the next upcoming class
    for (schedule in schedules) {
        try {
            val titleField = schedule::class.java.getDeclaredField("title")
            val timeField = schedule::class.java.getDeclaredField("time")
            titleField.isAccessible = true
            timeField.isAccessible = true

            val title = titleField.get(schedule) as String
            val time = timeField.get(schedule) as String

            val courseName = title.split(" - ").getOrNull(0) ?: title
            val timeRange = time.split(" - ")
            if (timeRange.size != 2) continue

            val startTimeStr = convertTo24Hour(timeRange[0].trim())
            val startTime = LocalTime.parse(startTimeStr, formatter)

            if (now.isBefore(startTime)) {
                // This is the next class
                return CurrentScheduleInfo(
                    courseName = courseName,
                    time = time,
                    isActive = false
                )
            }
        } catch (e: Exception) {
            Log.e("HomeScreen", "Error parsing schedule", e)
        }
    }

    // All classes have passed for today
    return null
}

// Helper function to convert 12-hour format to 24-hour format
private fun convertTo24Hour(time12h: String): String {
    return try {
        val parts = time12h.split(" ")
        if (parts.size != 2) return "00:00:00"

        val timePart = parts[0]
        val period = parts[1].uppercase()

        val timeParts = timePart.split(":")
        if (timeParts.size != 2) return "00:00:00"

        var hour = timeParts[0].toInt()
        val minute = timeParts[1]

        // Convert to 24-hour format
        if (period == "PM" && hour != 12) {
            hour += 12
        } else if (period == "AM" && hour == 12) {
            hour = 0
        }

        String.format("%02d:%s:00", hour, minute)
    } catch (e: Exception) {
        Log.e("HomeScreen", "Error converting time: $time12h", e)
        "00:00:00"
    }
}



