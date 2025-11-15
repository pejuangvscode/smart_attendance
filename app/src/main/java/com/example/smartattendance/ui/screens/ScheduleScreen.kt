package com.example.smartattendance.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import android.app.Activity
import android.util.Log
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smartattendance.api.AuthApi
import com.example.smartattendance.api.ScheduleApi
import com.example.smartattendance.ui.components.AppBottomNavigation
import com.example.smartattendance.ui.components.AppHeader
import com.example.smartattendance.ui.components.HeaderType
import com.example.smartattendance.ui.theme.AppFontFamily
import com.example.smartattendance.utils.SessionManager
import kotlinx.coroutines.launch
import kotlin.math.abs

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun ScheduleScreen(
    user: AuthApi.User?,
    onNavigateBack: () -> Unit,
    onNavigate: (String) -> Unit,
    onSubmitAttendance: () -> Unit = {},
    onScheduleItemClick: (String, String) -> Unit = { _, _ -> }
) {
    val darkGray = Color(0xFF2C2D32)

    // Set status bar color to match header
    val view = LocalView.current
    LaunchedEffect(Unit) {
        val window = (view.context as? Activity)?.window
        window?.let {
            WindowCompat.setDecorFitsSystemWindows(it, false)
            val controller = WindowInsetsControllerCompat(it, view)
            controller.isAppearanceLightStatusBars = false // White icons on dark background
            it.statusBarColor = 0xFF2C2D32.toInt()
        }
    }

    // State for schedules
    var weekSchedule by remember { mutableStateOf<List<DayScheduleData>>(emptyList()) }
    var todaySchedule by remember { mutableStateOf<List<ScheduleItemUI>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var selectedDayIndex by remember { mutableStateOf(getCurrentDayIndex()) }

    // Initialize ScheduleApi
    val scheduleApi = remember { ScheduleApi(AuthApi.supabase) }

    // Get current day info
    val currentDayName = remember { getCurrentDayName() }
    val todayDate = remember {
        val today = java.time.LocalDate.now()
        val formatter = java.time.format.DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy", java.util.Locale.ENGLISH)
        today.format(formatter)
    }

    // Fetch schedules from database
    LaunchedEffect(user?.user_id) {
        user?.user_id?.let { userId ->
            Log.d("ScheduleScreen", "=== LaunchedEffect triggered for user: $userId ===")
            isLoading = true
            errorMessage = null

            try {
                Log.d("ScheduleScreen", "Fetching schedules for user: $userId")

                // Fetch week schedules
                Log.d("ScheduleScreen", "Calling getUserSchedules...")
                val weekResult = scheduleApi.getUserSchedules(userId)
                Log.d("ScheduleScreen", "getUserSchedules returned")

                weekResult.fold(
                    onSuccess = { schedules ->
                        Log.d("ScheduleScreen", "Week schedules SUCCESS: ${schedules.size} days")
                        weekSchedule = schedules.map { daySchedule ->
                            DayScheduleData(
                                dayName = daySchedule.dayName,
                                schedules = daySchedule.schedules.map { apiSchedule ->
                                    ScheduleItemUI(
                                        scheduleId = apiSchedule.scheduleId,
                                        courseId = apiSchedule.courseId,
                                        title = apiSchedule.title,
                                        time = apiSchedule.time,
                                        room = apiSchedule.room
                                    )
                                }
                            )
                        }

                        // Set selectedDayIndex to today's day
                        val todayIndex = weekSchedule.indexOfFirst { it.dayName == currentDayName }
                        if (todayIndex != -1) {
                            selectedDayIndex = todayIndex
                            Log.d("ScheduleScreen", "Set selectedDayIndex to today: $todayIndex ($currentDayName)")
                        }

                        Log.d("ScheduleScreen", "Week schedules mapped successfully")
                    },
                    onFailure = { error ->
                        errorMessage = error.message ?: "Failed to load schedules"
                        Log.e("ScheduleScreen", "Week schedules FAILURE: ${error.message}", error)
                    }
                )

                // Fetch today's schedules
                Log.d("ScheduleScreen", "Calling getTodaySchedules...")
                val todayResult = scheduleApi.getTodaySchedules(userId)
                Log.d("ScheduleScreen", "getTodaySchedules returned")

                todayResult.fold(
                    onSuccess = { schedules ->
                        Log.d("ScheduleScreen", "Today schedules SUCCESS: ${schedules.size} items")
                        todaySchedule = schedules.map { apiSchedule ->
                            ScheduleItemUI(
                                scheduleId = apiSchedule.scheduleId,
                                courseId = apiSchedule.courseId,
                                title = apiSchedule.title,
                                time = apiSchedule.time,
                                room = apiSchedule.room
                            )
                        }
                        Log.d("ScheduleScreen", "Today schedules mapped successfully")
                    },
                    onFailure = { error ->
                        Log.e("ScheduleScreen", "Today schedules FAILURE: ${error.message}", error)
                    }
                )
            } catch (e: Exception) {
                errorMessage = e.message ?: "Unknown error occurred"
                Log.e("ScheduleScreen", "=== EXCEPTION in LaunchedEffect ===", e)
                Log.e("ScheduleScreen", "Exception message: ${e.message}")
            } finally {
                isLoading = false
                Log.d("ScheduleScreen", "=== Loading finished, isLoading set to false ===")
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // Header
        AppHeader(
            title = "Schedule",
            headerType = HeaderType.BACK,
            onBackClick = onNavigateBack
        )

        // Content Area - Make everything scrollable

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(Color(0xFFF6F6F6))
                .padding(start = 24.dp, end = 24.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            item {
                // Schedule Title
                Text(
                    text = "Schedule",
                    fontFamily = AppFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                    color = Color.Black,
                    modifier = Modifier.padding(top = 16.dp, bottom = 16.dp)
                )
            }

            item {
                if (isLoading) {
                    // Loading state
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = Color(0xFF2C2D32)
                        )
                    }
                } else if (errorMessage != null) {
                    // Error state
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = errorMessage ?: "Failed to load schedules",
                            color = Color.Red,
                            fontFamily = AppFontFamily,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                } else if (weekSchedule.isEmpty()) {
                    // Empty state
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No schedules found",
                            color = Color.Gray,
                            fontFamily = AppFontFamily,
                            fontSize = 14.sp
                        )
                    }
                } else {
                    // Swipe gesture detector for day navigation
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .pointerInput(selectedDayIndex) {
                                detectHorizontalDragGestures(
                                    onDragEnd = {
                                        // Reset drag state after gesture ends
                                    }
                                ) { _, dragAmount ->
                                    // Swipe left (negative dragAmount) to show next day
                                    if (dragAmount < -30 && selectedDayIndex < weekSchedule.size - 1) {
                                        selectedDayIndex++
                                    }
                                    // Swipe right (positive dragAmount) to show previous day
                                    else if (dragAmount > 30 && selectedDayIndex > 0) {
                                        selectedDayIndex--
                                    }
                                }
                            }
                    ) {
                        AnimatedContent(
                            targetState = selectedDayIndex,
                            transitionSpec = {
                                val isMovingForward = targetState > initialState
                                slideInHorizontally(
                                    initialOffsetX = { fullWidth ->
                                        if (isMovingForward) fullWidth else -fullWidth
                                    },
                                    animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing)
                                ) + fadeIn() togetherWith
                                        slideOutHorizontally(
                                            targetOffsetX = { fullWidth ->
                                                if (isMovingForward) -fullWidth else fullWidth
                                            },
                                            animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing)
                                        ) + fadeOut()
                            },
                            label = "schedule_animation"
                        ) { dayIndex ->
                            val dayData = weekSchedule[dayIndex]
                            DayScheduleCard(
                                dayName = dayData.dayName,
                                schedules = dayData.schedules,
                                isSelected = true,
                                onCardClick = { },
                                onSubmitAttendance = onSubmitAttendance,
                                onScheduleItemClick = onScheduleItemClick,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }

            item {
                // Dots Indicator - only show if schedules are loaded
                if (!isLoading && weekSchedule.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        repeat(weekSchedule.size) { index ->
                            Box(
                                modifier = Modifier
                                    .size(if (index == selectedDayIndex) 10.dp else 8.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (index == selectedDayIndex) Color(0xFF2C2D32)
                                        else Color.Gray.copy(alpha = 0.3f)
                                    )
                                    .clickable {
                                        selectedDayIndex = index
                                    }
                            )
                            if (index < weekSchedule.size - 1) {
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                        }
                    }
                }
            }

            item {
                // Today Section title with date
                Column(modifier = Modifier.padding(bottom = 16.dp)) {
                    Text(
                        text = "Today",
                        fontFamily = AppFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                        color = Color.Black
                    )
                    Text(
                        text = todayDate,
                        fontFamily = AppFontFamily,
                        fontSize = 14.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            item {
                // Notification Banner - conditional based on whether there are schedules today
                if (todaySchedule.isNotEmpty()) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF2C2D32)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "The attendance submission period is 15 minutes from the start of class.",
                            fontFamily = AppFontFamily,
                            color = Color.White,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                } else {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F0F0)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "No Schedule Today",
                                fontFamily = AppFontFamily,
                                fontWeight = FontWeight.Medium,
                                color = Color.Gray,
                                fontSize = 14.sp,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = "Enjoy your free time!",
                                fontFamily = AppFontFamily,
                                color = Color.Gray.copy(alpha = 0.7f),
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
            }

            // Today's Schedule items - only show if there are schedules
            if (todaySchedule.isNotEmpty()) {
                items(todaySchedule) { item ->
                    TodayScheduleItem(
                        title = item.title,
                        time = item.time,
                        onSubmit = onSubmitAttendance
                    ) {
                        onScheduleItemClick(item.title, item.time)
                    }
                }
            }
        }

    }
}

@Composable
private fun DayScheduleCard(
    dayName: String,
    schedules: List<ScheduleItemUI>,
    isSelected: Boolean,
    onCardClick: () -> Unit,
    onSubmitAttendance: () -> Unit,
    onScheduleItemClick: (String, String) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(Color.White),
        elevation = CardDefaults.cardElevation(8.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = dayName.uppercase(),
                fontFamily = AppFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color.Black,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )

            // Classes for the day with dark background - make them clickable
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                schedules.forEach { schedule ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onScheduleItemClick(schedule.title, schedule.time)
                            },
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF2C2D32)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Text(
                                text = schedule.title,
                                fontFamily = AppFontFamily,
                                fontWeight = FontWeight.Medium,
                                fontSize = 13.sp,
                                color = Color.White,
                                lineHeight = 16.sp
                            )
                            Text(
                                text = schedule.time,
                                fontFamily = AppFontFamily,
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.7f),
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TodayScheduleItem(
    title: String,
    time: String,
    onSubmit: () -> Unit,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 14.dp)
            ) {
                Text(
                    text = title,
                    fontFamily = AppFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    color = Color.Black
                )
                Text(
                    text = time,
                    fontFamily = AppFontFamily,
                    fontSize = 12.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            Button(
                onClick = onSubmit,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFE0697E)
                ),
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier.height(36.dp)
            ) {
                Text(
                    text = "Submit",
                    fontFamily = AppFontFamily,
                    color = Color.White,
                    fontSize = 12.sp
                )
            }
        }
    }
}

data class ScheduleItemUI(
    val scheduleId: Int = 0,
    val courseId: Int = 0,
    val title: String,
    val time: String,
    val room: String? = null
)

data class DayScheduleData(
    val dayName: String,
    val schedules: List<ScheduleItemUI>
)

// Helper functions for current day
private fun getCurrentDayName(): String {
    val dayOfWeek = java.time.LocalDate.now().dayOfWeek
    return when (dayOfWeek) {
        java.time.DayOfWeek.MONDAY -> "Monday"
        java.time.DayOfWeek.TUESDAY -> "Tuesday"
        java.time.DayOfWeek.WEDNESDAY -> "Wednesday"
        java.time.DayOfWeek.THURSDAY -> "Thursday"
        java.time.DayOfWeek.FRIDAY -> "Friday"
        java.time.DayOfWeek.SATURDAY -> "Monday" // Default to Monday for weekend
        java.time.DayOfWeek.SUNDAY -> "Monday" // Default to Monday for weekend
    }
}

private fun getCurrentDayIndex(): Int {
    val dayOfWeek = java.time.LocalDate.now().dayOfWeek
    return when (dayOfWeek) {
        java.time.DayOfWeek.MONDAY -> 0
        java.time.DayOfWeek.TUESDAY -> 1
        java.time.DayOfWeek.WEDNESDAY -> 2
        java.time.DayOfWeek.THURSDAY -> 3
        java.time.DayOfWeek.FRIDAY -> 4
        else -> 0 // Default to Monday (index 0) for weekend
    }
}

@Preview
@Composable
fun ScheduleScreenPreview() {
    ScheduleScreen(
        user = null,
        onNavigateBack = {},
        onNavigate = {},
        onSubmitAttendance = {}
    )
}