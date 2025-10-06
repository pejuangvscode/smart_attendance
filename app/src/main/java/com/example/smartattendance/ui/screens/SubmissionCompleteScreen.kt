package com.example.smartattendance.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smartattendance.ui.components.AppHeader
import com.example.smartattendance.ui.components.HeaderType
import com.example.smartattendance.ui.theme.AppFontFamily
import kotlinx.coroutines.delay

data class AttendanceStatusInfo(
    val status: String,
    val icon: ImageVector,
    val color: Color,
    val backgroundColor: Color,
    val title: String,
    val description: String
)

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun SubmissionCompleteScreen(
    status: String,
    courseName: String,
    onBackClick: () -> Unit = {},
    onNavigateHome: () -> Unit = {}
) {
    val attendanceStatus = when (status.lowercase()) {
        "present" -> AttendanceStatusInfo(
            status = "PRESENT",
            icon = Icons.Default.CheckCircle,
            color = Color(0xFF00CC2C),
            backgroundColor = Color(0x1A00CD2C),
            title = "Attendance Recorded",
            description = "Your attendance has been successfully recorded for this class."
        )
        "absent" -> AttendanceStatusInfo(
            status = "ABSENT",
            icon = Icons.Default.Cancel,
            color = Color(0xFFE53E3E),
            backgroundColor = Color(0x1AE53E3E),
            title = "Absence Recorded",
            description = "Your absence has been recorded for this class."
        )
        "late" -> AttendanceStatusInfo(
            status = "LATE",
            icon = Icons.Default.Schedule,
            color = Color(0xFFFF9D00),
            backgroundColor = Color(0x1AFF9D00),
            title = "Late Attendance",
            description = "Your late attendance has been recorded for this class."
        )
        "pending" -> AttendanceStatusInfo(
            status = "PENDING",
            icon = Icons.Default.Schedule,
            color = Color(0xFFFF9D00),
            backgroundColor = Color(0x1AFF9D00),
            title = "Request Pending",
            description = "Your permission request is being reviewed by the instructor."
        )
        "approved" -> AttendanceStatusInfo(
            status = "APPROVED",
            icon = Icons.Default.CheckCircle,
            color = Color(0xFF4285F4),
            backgroundColor = Color(0x1A4285F4),
            title = "Request Approved",
            description = "Your permission request has been approved by the instructor."
        )
        "rejected" -> AttendanceStatusInfo(
            status = "REJECTED",
            icon = Icons.Default.Cancel,
            color = Color(0xFFE53E3E),
            backgroundColor = Color(0x1AE53E3E),
            title = "Request Rejected",
            description = "Your permission request has been rejected by the instructor."
        )
        "not yet" -> AttendanceStatusInfo(
            status = "NOT YET",
            icon = Icons.Default.Warning,
            color = Color(0xFF2C2D32),
            backgroundColor = Color(0x1A2C2D32),
            title = "Not Submitted",
            description = "You haven't submitted your attendance for this class yet."
        )
        else -> AttendanceStatusInfo(
            status = status.uppercase(),
            icon = Icons.Default.CheckCircle,
            color = Color(0xFF2C2D32),
            backgroundColor = Color(0x1A2C2D32),
            title = "Status Updated",
            description = "Your attendance status has been updated."
        )
    }

    var showContent by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(300)
        showContent = true
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // Animated Header
        AnimatedVisibility(
            visible = true,
            enter = slideInVertically(
                initialOffsetY = { -it },
                animationSpec = tween(500, easing = FastOutSlowInEasing)
            ) + fadeIn(animationSpec = tween(500))
        ) {
            AppHeader(
                title = "Attendance Detail",
                headerType = HeaderType.BACK,
                onBackClick = onBackClick
            )
        }

        // Content Area
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(Color(0xFFF6F6F6))
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Animated Status Icon and Text Section
            AnimatedVisibility(
                visible = showContent,
                enter = scaleIn(
                    initialScale = 0f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                ) + fadeIn(animationSpec = tween(800, delayMillis = 300))
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Animated Status Icon with pulse effect
                    var iconScale by remember { mutableStateOf(1f) }
                    val animatedIconScale by animateFloatAsState(
                        targetValue = iconScale,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMedium
                        )
                    )

                    LaunchedEffect(Unit) {
                        delay(800)
                        iconScale = 1.1f
                        delay(300)
                        iconScale = 1f
                    }

                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .scale(animatedIconScale)
                            .clip(CircleShape)
                            .background(attendanceStatus.color),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = attendanceStatus.icon,
                            contentDescription = attendanceStatus.status,
                            tint = Color.White,
                            modifier = Modifier.size(40.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Animated Status Text
                    AnimatedVisibility(
                        visible = showContent,
                        enter = slideInVertically(
                            initialOffsetY = { it / 2 },
                            animationSpec = tween(600, delayMillis = 600, easing = FastOutSlowInEasing)
                        ) + fadeIn(animationSpec = tween(600, delayMillis = 600))
                    ) {
                        Text(
                            text = attendanceStatus.status,
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp,
                            color = attendanceStatus.color,
                            textAlign = TextAlign.Center,
                            fontFamily = AppFontFamily
                        )
                    }
                }
            }

            // Animated Detail Card
            AnimatedVisibility(
                visible = showContent,
                enter = slideInVertically(
                    initialOffsetY = { it },
                    animationSpec = tween(600, delayMillis = 500, easing = FastOutSlowInEasing)
                ) + fadeIn(animationSpec = tween(600, delayMillis = 500))
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(8.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        var showDetailContent by remember { mutableStateOf(false) }

                        LaunchedEffect(Unit) {
                            delay(1000)
                            showDetailContent = true
                        }

                        // Animated Title
                        AnimatedVisibility(
                            visible = showDetailContent,
                            enter = slideInHorizontally(
                                initialOffsetX = { -it },
                                animationSpec = tween(400, easing = FastOutSlowInEasing)
                            ) + fadeIn(animationSpec = tween(400))
                        ) {
                            Text(
                                text = "Detail",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Color.Black,
                                modifier = Modifier.padding(bottom = 16.dp),
                                fontFamily = AppFontFamily
                            )
                        }

                        // Detail Content with staggered animations
                        AnimatedDetailRow(
                            label = "Class",
                            value = courseName,
                            delay = 200,
                            visible = showDetailContent
                        )

                        AnimatedDetailRow(
                            label = "Instructor",
                            value = "KELVIN WIRIYATAMA",
                            delay = 250,
                            visible = showDetailContent
                        )

                        AnimatedDetailRow(
                            label = "Room",
                            value = "B342",
                            delay = 300,
                            visible = showDetailContent
                        )

                        AnimatedDetailRow(
                            label = "Date",
                            value = "25 SEPTEMBER 2025",
                            delay = 350,
                            visible = showDetailContent
                        )

                        AnimatedDetailRow(
                            label = "Submit time",
                            value = "10:07",
                            delay = 400,
                            visible = showDetailContent
                        )

                        // Animated Status Row
                        AnimatedVisibility(
                            visible = showDetailContent,
                            enter = slideInHorizontally(
                                initialOffsetX = { it },
                                animationSpec = tween(400, delayMillis = 450, easing = FastOutSlowInEasing)
                            ) + fadeIn(animationSpec = tween(400, delayMillis = 450))
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Status",
                                    fontSize = 14.sp,
                                    color = Color(0xFF2C2D32),
                                    modifier = Modifier.weight(1f),
                                    fontFamily = AppFontFamily
                                )
                                Text(
                                    text = attendanceStatus.status,
                                    fontSize = 14.sp,
                                    color = attendanceStatus.color,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.End,
                                    modifier = Modifier.weight(1f),
                                    fontFamily = AppFontFamily
                                )
                            }
                        }
                    }
                }
            }

            // Animated Class Information Card
            AnimatedVisibility(
                visible = showContent,
                enter = slideInVertically(
                    initialOffsetY = { it },
                    animationSpec = tween(600, delayMillis = 700, easing = FastOutSlowInEasing)
                ) + fadeIn(animationSpec = tween(600, delayMillis = 700))
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(8.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        var showClassInfoContent by remember { mutableStateOf(false) }

                        LaunchedEffect(Unit) {
                            delay(1200)
                            showClassInfoContent = true
                        }

                        // Animated Title
                        AnimatedVisibility(
                            visible = showClassInfoContent,
                            enter = slideInHorizontally(
                                initialOffsetX = { -it },
                                animationSpec = tween(400, easing = FastOutSlowInEasing)
                            ) + fadeIn(animationSpec = tween(400))
                        ) {
                            Text(
                                text = "Class Information",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Color.Black,
                                modifier = Modifier.padding(bottom = 16.dp),
                                fontFamily = AppFontFamily
                            )
                        }

                        // Day
                        AnimatedDetailRow(
                            label = "Day",
                            value = "Thursday",
                            delay = 200,
                            visible = showClassInfoContent
                        )

                        // Schedule
                        AnimatedVisibility(
                            visible = showClassInfoContent,
                            enter = slideInHorizontally(
                                initialOffsetX = { it },
                                animationSpec = tween(400, delayMillis = 300, easing = FastOutSlowInEasing)
                            ) + fadeIn(animationSpec = tween(400, delayMillis = 300))
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Schedule",
                                    fontSize = 14.sp,
                                    color = Color(0xFF2C2D32),
                                    modifier = Modifier.weight(1f),
                                    fontFamily = AppFontFamily
                                )
                                Text(
                                    text = "10:00AM - 11:40AM",
                                    fontSize = 14.sp,
                                    color = Color.Black,
                                    textAlign = TextAlign.End,
                                    modifier = Modifier.weight(1f),
                                    fontFamily = AppFontFamily
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AnimatedDetailCard(
    title: String,
    courseName: String = "",
    attendanceStatus: AttendanceStatusInfo? = null,
    isClassInfo: Boolean = false,
    animationDelay: Int = 0
) {
    var showCardContent by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(animationDelay.toLong())
        showCardContent = true
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            // Animated Title
            AnimatedVisibility(
                visible = showCardContent,
                enter = slideInHorizontally(
                    initialOffsetX = { -it },
                    animationSpec = tween(400, easing = FastOutSlowInEasing)
                ) + fadeIn(animationSpec = tween(400))
            ) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color.Black,
                    modifier = Modifier.padding(bottom = 16.dp),
                    fontFamily = AppFontFamily
                )
            }

            if (isClassInfo) {
                // Class Information Content
                AnimatedDetailRow(
                    label = "Day",
                    value = "Thursday",
                    delay = 200,
                    visible = showCardContent
                )

                AnimatedDetailRow(
                    label = "Schedule",
                    value = "10:00AM - 11:40AM",
                    delay = 300,
                    visible = showCardContent,
                    isLast = true
                )
            } else {
                // Detail Content
                AnimatedDetailRow(
                    label = "Class",
                    value = courseName,
                    delay = 200,
                    visible = showCardContent
                )

                AnimatedDetailRow(
                    label = "Instructor",
                    value = "KELVIN WIRIYATAMA",
                    delay = 250,
                    visible = showCardContent
                )

                AnimatedDetailRow(
                    label = "Room",
                    value = "B342",
                    delay = 300,
                    visible = showCardContent
                )

                AnimatedDetailRow(
                    label = "Date",
                    value = "25 SEPTEMBER 2025",
                    delay = 350,
                    visible = showCardContent
                )

                AnimatedDetailRow(
                    label = "Submit time",
                    value = "10:07",
                    delay = 400,
                    visible = showCardContent
                )

                // Animated Status Row
                AnimatedVisibility(
                    visible = showCardContent,
                    enter = slideInHorizontally(
                        initialOffsetX = { it },
                        animationSpec = tween(400, delayMillis = 450, easing = FastOutSlowInEasing)
                    ) + fadeIn(animationSpec = tween(400, delayMillis = 450))
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Status",
                            fontSize = 14.sp,
                            color = Color(0xFF2C2D32),
                            modifier = Modifier.weight(1f),
                            fontFamily = AppFontFamily
                        )
                        Text(
                            text = attendanceStatus?.status ?: "",
                            fontSize = 14.sp,
                            color = attendanceStatus?.color ?: Color.Black,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.End,
                            modifier = Modifier.weight(1f),
                            fontFamily = AppFontFamily
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AnimatedDetailRow(
    label: String,
    value: String,
    delay: Int = 0,
    visible: Boolean = true,
    isLast: Boolean = false
) {
    AnimatedVisibility(
        visible = visible,
        enter = slideInHorizontally(
            initialOffsetX = { it },
            animationSpec = tween(400, delayMillis = delay, easing = FastOutSlowInEasing)
        ) + fadeIn(animationSpec = tween(400, delayMillis = delay))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = if (isLast) 0.dp else 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                fontSize = 14.sp,
                color = Color(0xFF2C2D32),
                modifier = Modifier.weight(1f),
                fontFamily = AppFontFamily
            )
            Text(
                text = value,
                fontSize = 14.sp,
                color = Color.Black,
                textAlign = TextAlign.End,
                modifier = Modifier.weight(1f),
                fontFamily = AppFontFamily
            )
        }
    }
}

@Preview
@Composable
fun SubmissionCompleteScreenPreview() {
    SubmissionCompleteScreen(
        status = "Present",
        courseName = "KECERDASAN KOMPUTASIONAL Laboratory",
        onBackClick = {},
        onNavigateHome = {}
    )
}
