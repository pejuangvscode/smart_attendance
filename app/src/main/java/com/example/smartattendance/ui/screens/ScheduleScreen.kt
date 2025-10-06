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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smartattendance.ui.components.AppBottomNavigation
import com.example.smartattendance.ui.components.AppHeader
import com.example.smartattendance.ui.components.HeaderType
import com.example.smartattendance.ui.theme.AppFontFamily
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun ScheduleScreen(
    onNavigateBack: () -> Unit,
    onNavigate: (String) -> Unit,
    onSubmitAttendance: () -> Unit = {},
    onScheduleItemClick: (String, String) -> Unit = { _, _ -> }
) {
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
        // Single schedule card with navigation - move state outside LazyColumn
        var selectedDayIndex by remember { mutableStateOf(0) }

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
                    // Single day schedule card
                    val currentDayData = weekSchedule[selectedDayIndex]
                    val previousDayIndex = remember { mutableStateOf(selectedDayIndex) }

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
                        }
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

                    LaunchedEffect(selectedDayIndex) {
                        previousDayIndex.value = selectedDayIndex
                    }
                }
            }

            item {
                // Dots Indicator - reflects selected day (no color animation)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    repeat(5) { index ->
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
                        if (index < 4) {
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                    }
                }
            }

            item {
                // Today Section title
                Text(
                    text = "Today",
                    fontFamily = AppFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                    color = Color.Black,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            item {
                // Notification Banner
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
            }

            // Today's Schedule items
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

@Composable
private fun DayScheduleCard(
    dayName: String,
    schedules: List<ScheduleItem>,
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

data class ScheduleItem(
    val title: String,
    val time: String
)
private val mondaySchedule = listOf(
    ScheduleItem(
        title = "KECERDASAN KOMPUTASIONAL - INF 20052 KKKR",
        time = "8:45AM - 11:25AM"
    ),
    ScheduleItem(
        title = "KECERDASAN KOMPUTASIONAL Laboratory - INF 20052 KKLR",
        time = "1:15PM - 2:55PM"
    )
)

private val tuesdaySchedule = listOf(
    ScheduleItem(
        title = "PEMBELAJARAN MESIN LANJUT - INF 20151 PMLR",
        time = "7:15AM - 9:45AM"
    ),
    ScheduleItem(
        title = "PGMB. APLIKASI PLATFORM MOBILE - INF 20054 PAPR",
        time = "10:15AM - 11:55AM"
    ),
    ScheduleItem(
        title = "PGMB. APLIKASI PLATFORM MOBILE Laboratory - INF 20054 PALR",
        time = "1:15PM - 2:55PM"
    )
)

private val wednesdaySchedule = listOf(
    ScheduleItem(
        title = "INFORMATIKA DALAM KOM SELULER - INF 20262 IKSR",
        time = "1:15PM - 3:45PM"
    )
)

private val thursdaySchedule = listOf(
    ScheduleItem(
        title = "PERANCANGAN & PEMROGRAMAN WEB - INF 20053 PWR",
        time = "7:15AM - 9:45AM"
    ),
    ScheduleItem(
        title = "PERANCANGAN & PEMROGRAMAN WEB Laboratory - INF 20053 PPLR",
        time = "10:00AM - 11:40AM"
    )
)

private val fridaySchedule = listOf(
    ScheduleItem(
        title = "KEAMANAN KOMPUTER & JARINGAN - INF 20051 KKJR",
        time = "7:15AM - 9:45AM"
    ),
    ScheduleItem(
        title = "KEAMANAN KOMPUTER & JARINGAN Laboratory - INF 20051 KKLR",
        time = "10:00AM - 11:40AM"
    )
)

private val todaySchedule = fridaySchedule // contoh kalau hari ini Jumat


private val weekSchedule = listOf(
    DayScheduleData("Monday", mondaySchedule),
    DayScheduleData("Tuesday", tuesdaySchedule),
    DayScheduleData("Wednesday", wednesdaySchedule),
    DayScheduleData("Thursday", thursdaySchedule),
    DayScheduleData("Friday", fridaySchedule)
)

data class DayScheduleData(
    val dayName: String,
    val schedules: List<ScheduleItem>
)

@Preview
@Composable
fun ScheduleScreenPreview() {
    ScheduleScreen(
        onNavigateBack = {},
        onNavigate = {},
        onSubmitAttendance = {}
    )
}