package com.example.smartattendance.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smartattendance.ui.theme.SmartAttendanceTheme
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.DayOfWeek
import java.util.Locale
import kotlin.math.abs

data class AttendanceData(
    val present: Int = 60,
    val excused: Int = 5,
    val sick: Int = 3,
    val late: Int = 8,
    val absent: Int = 12
) {
    val total = present + excused + sick + late + absent
    val presentPercentage = if (total > 0) (present * 100f / total) else 0f
    val excusedPercentage = if (total > 0) (excused * 100f / total) else 0f
    val sickPercentage = if (total > 0) (sick * 100f / total) else 0f
    val latePercentage = if (total > 0) (late * 100f / total) else 0f
    val absentPercentage = if (total > 0) (absent * 100f / total) else 0f
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onLogout: () -> Unit = {},
    onSubmitAttendance: () -> Unit = {},
    onDateClick: () -> Unit = {},
    onRequestPermission: () -> Unit = {},
    onBackClick: () -> Unit = {}
) {
    val darkGray = Color(0xFF2C2D32)
    val redColor = Color(0xFFE0697E)
    val attendanceData = remember { AttendanceData() }

    // Get current date
    val currentDate = remember { LocalDate.now() }
    val dayOfMonth = currentDate.dayOfMonth
    val dayOfWeek = currentDate.dayOfWeek
    val month = currentDate.format(DateTimeFormatter.ofPattern("MMMM", Locale.ENGLISH))
    val year = currentDate.year
    val dayName = dayOfWeek.name.lowercase().replaceFirstChar { it.uppercase() }

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
                        text = "Hello, Teo!",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "01082230017/Semester Ganjil",
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }

                IconButton(onClick = onLogout) {
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
                // Take attendance card
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
                            modifier = Modifier.weight(0.8f)
                        ) {
                            Text(
                                text = "KEAMANAN KOMPUTER & JARINGAN",
                                fontSize = 14.sp,
                                color = Color.Black,
                                fontWeight = FontWeight.Medium,
                                maxLines = 2
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = onSubmitAttendance,
                            colors = ButtonDefaults.buttonColors(containerColor = redColor),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Text("Submit", fontSize = 14.sp, color = Color.White)
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
                                text = "26", // Fixed date as shown in image
                                fontSize = 70.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(
                                    text = "Friday",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.Black
                                )
                                Text(
                                    text = "September 2025",
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

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            WeekDayIndicator("M", true, Color(0xFFFF9D00))
                            WeekDayIndicator("T", true, darkGray)
                            WeekDayIndicator("W", true, redColor)
                            WeekDayIndicator("Th", true, darkGray)
                            WeekDayIndicator("Fr", false, Color.LightGray)
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

                        // Donut charts row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            IndividualDonutChart(
                                value = 60,
                                percentage = 68.2f,
                                label = "Present",
                                color = Color(0xFF4CAF50)
                            )
                            IndividualDonutChart(
                                value = 0,
                                percentage = 0f,
                                label = "Excused",
                                color = Color(0xFF2196F3)
                            )
                            IndividualDonutChart(
                                value = 0,
                                percentage = 0f,
                                label = "Sick",
                                color = Color(0xFFFF9800)
                            )
                            IndividualDonutChart(
                                value = 20,
                                percentage = 22.7f,
                                label = "Absent",
                                color = Color(0xFF9E9E9E)
                            )
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
fun AttendanceDonutChart(
    attendanceData: AttendanceData,
    modifier: Modifier = Modifier
) {
    val colors = listOf(
        Color(0xFF4CAF50), // Present - Green
        Color(0xFF2196F3), // Excused - Blue
        Color(0xFFFF9800), // Sick - Orange
        Color(0xFFF44336), // Late - Red
        Color(0xFF9E9E9E)  // Absent - Gray
    )

    val percentages = listOf(
        attendanceData.presentPercentage,
        attendanceData.excusedPercentage,
        attendanceData.sickPercentage,
        attendanceData.latePercentage,
        attendanceData.absentPercentage
    )

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        // Draw individual circular progress indicators
        percentages.forEachIndexed { index, percentage ->
            val strokeWidth = 8.dp
            val baseRadius = 50.dp
            val radiusOffset = (index * 12).dp
            val radius = baseRadius - radiusOffset // Different radius for each ring

            Canvas(
                modifier = Modifier.size(radius * 2 + strokeWidth)
            ) {
                val sweepAngle = (percentage / 100f) * 360f

                drawArc(
                    color = Color.LightGray.copy(alpha = 0.3f),
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    style = Stroke(width = strokeWidth.toPx()),
                    size = androidx.compose.ui.geometry.Size(
                        radius.toPx() * 2,
                        radius.toPx() * 2
                    ),
                    topLeft = androidx.compose.ui.geometry.Offset(
                        (size.width - radius.toPx() * 2) / 2,
                        (size.height - radius.toPx() * 2) / 2
                    )
                )

                // Progress arc
                drawArc(
                    color = Color.Gray,
                    startAngle = -90f, // Start from top
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    style = Stroke(width = strokeWidth.toPx()),
                    size = androidx.compose.ui.geometry.Size(
                        radius.toPx() * 2,
                        radius.toPx() * 2
                    ),
                    topLeft = androidx.compose.ui.geometry.Offset(
                        (size.width - radius.toPx() * 2) / 2,
                        (size.height - radius.toPx() * 2) / 2
                    )
                )
            }
        }

        // Center text showing total
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = attendanceData.total.toString(),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Text(
                text = "Total",
                fontSize = 12.sp,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun IndividualDonutChart(
    value: Int,
    percentage: Float,
    label: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    val strokeWidth = 4.dp
    val radius = 30.dp

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Box(
            modifier = Modifier.size(radius * 2 + strokeWidth),
            contentAlignment = Alignment.Center
        ) {
            Canvas(
                modifier = Modifier.size(radius * 2)
            ) {
                // Background circle (light gray)
                drawArc(
                    color = Color.LightGray.copy(alpha = 0.7f),
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    style = Stroke(width = strokeWidth.toPx()),
                    size = androidx.compose.ui.geometry.Size(
                        radius.toPx() * 2,
                        radius.toPx() * 2
                    ),
                    topLeft = androidx.compose.ui.geometry.Offset(
                        (size.width - radius.toPx() * 2) / 2,
                        (size.height - radius.toPx() * 2) / 2
                    )
                )

                // Progress arc
                val sweepAngle = (percentage / 100f) * 360f
                drawArc(
                    color = Color(0xFF3A3A3A),
                    startAngle = -90f, // Start from top
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    style = Stroke(width = strokeWidth.toPx()),
                    size = androidx.compose.ui.geometry.Size(
                        radius.toPx() * 2,
                        radius.toPx() * 2
                    ),
                    topLeft = androidx.compose.ui.geometry.Offset(
                        (size.width - radius.toPx() * 2) / 2,
                        (size.height - radius.toPx() * 2) / 2
                    )
                )
            }

            // Center text showing value
            Text(
                text = "$value%",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Label below the chart
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color.Black,
            fontWeight = FontWeight.Medium
        )
    }
}
@Composable
fun StatItemWithPercentage(
    value: String,
    percentage: String,
    label: String,
    color: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Row {
                Text(
                    text = value,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "($percentage)",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
            Text(
                text = label,
                fontSize = 12.sp,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun WeekDayIndicator(
    day: String,
    isPresent: Boolean,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = day,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Black,
            modifier = Modifier.padding(bottom = 6.dp)
        )
        Box(
            modifier = Modifier
                .size(14.dp)
                .clip(CircleShape)
                .background(if (isPresent) color else Color.LightGray)
        )
    }
}

@Composable
fun StatItem(
    value: String,
    label: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color.Gray,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    SmartAttendanceTheme {
        HomeScreen()
    }
}
