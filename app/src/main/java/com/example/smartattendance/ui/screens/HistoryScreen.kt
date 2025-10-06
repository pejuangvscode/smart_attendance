package com.example.smartattendance.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smartattendance.ui.components.AppHeader
import com.example.smartattendance.ui.components.HeaderType
import com.example.smartattendance.ui.theme.AppFontFamily
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun HistoryScreen(
    onBackClick: () -> Unit = {},
    onCardClick: (String, String) -> Unit = { _, _ -> },
    onNavigateToSubmissionComplete: (String, String) -> Unit = { _, _ -> },
    onNavigateToSchedule: () -> Unit = {}
) {
    val attendanceItems = remember {
        mapOf(
            "Today - Saturday, 6 Oct 2025" to listOf(
                Triple("KECERDASAN KOMPUTASIONAL Laboratory", "Request Permission", "Pending"),
                Triple("KECERDASAN KOMPUTASIONAL", "Request Permission", "Pending"),
                Triple("KEAMANAN KOMPUTER & JARINGAN Laboratory", "10:00AM - 11:40AM", "Not Yet"),
                Triple("KEAMANAN KOMPUTER & JARINGAN", "7:15AM - 9:45AM", "Not Yet")
            ),
            "Friday, 4 Oct 2025" to listOf(
                Triple("ALGORITMA & STRUKTUR DATA Laboratory", "1:15PM - 2:55PM", "Present"),
                Triple("ALGORITMA & STRUKTUR DATA", "10:00AM - 11:40AM", "Present"),
                Triple("MANAJEMEN BASIS DATA", "7:15AM - 9:45AM", "Late")
            ),
            "Thursday, 3 Oct 2025" to listOf(
                Triple("PERANCANGAN & PEMROGRAMAN WEB Laboratory", "10:00AM - 11:40AM", "Present"),
                Triple("PERANCANGAN & PEMROGRAMAN WEB", "7:15AM - 9:45AM", "Present")
            ),
            "Wednesday, 2 Oct 2025" to listOf(
                Triple("INFORMATIKA DALAM KOM SELULER", "1:15PM - 3:45PM", "Absent")
            ),
            "Tuesday, 1 Oct 2025" to listOf(
                Triple("PGMB. APLIKASI PLATFORM MOBILE Laboratory", "1:15PM - 2:55PM", "Present"),
                Triple("PGMB. APLIKASI PLATFORM MOBILE", "10:15AM - 11:55AM", "Present"),
                Triple("PEMBELAJARAN MESIN LANJUT", "7:15AM - 9:45AM", "Present")
            ),
            "Monday, 30 Sep 2025" to listOf(
                Triple("KECERDASAN KOMPUTASIONAL Laboratory", "1:15PM - 2:55PM", "Present"),
                Triple("KECERDASAN KOMPUTASIONAL", "8:45AM - 11:25AM", "Late")
            ),
            "Friday, 27 Sep 2025" to listOf(
                Triple("KEAMANAN KOMPUTER & JARINGAN Laboratory", "10:00AM - 11:40AM", "Sick"),
                Triple("KEAMANAN KOMPUTER & JARINGAN", "7:15AM - 9:45AM", "Sick")
            ),
            "Thursday, 26 Sep 2025" to listOf(
                Triple("KEAMANAN KOMPUTER & JARINGAN Laboratory", "Request Permission", "Approved"),
                Triple("KEAMANAN KOMPUTER & JARINGAN", "Request Permission", "Approved"),
                Triple("PERANCANGAN & PEMROGRAMAN WEB Laboratory", "10:00AM - 11:40AM", "Excused"),
                Triple("PERANCANGAN & PEMROGRAMAN WEB", "7:15AM - 9:45AM", "Excused")
            ),
            "Wednesday, 25 Sep 2025" to listOf(
                Triple("PERANCANGAN & PEMROGRAMAN WEB Laboratory", "Request Permission", "Approved"),
                Triple("PERANCANGAN & PEMROGRAMAN WEB", "Request Permission", "Approved"),
                Triple("INFORMATIKA DALAM KOM SELULER", "1:15PM - 3:45PM", "Late")
            ),
            "Tuesday, 24 Sep 2025" to listOf(
                Triple("INFORMATIKA DALAM KOM SELULER", "Request Permission", "Rejected")
            ),
            "Monday, 23 Sep 2025" to listOf(
                Triple("PGMB. APLIKASI PLATFORM MOBILE Laboratory", "1:15PM - 2:55PM", "Present"),
                Triple("PGMB. APLIKASI PLATFORM MOBILE", "10:15AM - 11:55AM", "Present"),
                Triple("PEMBELAJARAN MESIN LANJUT", "7:15AM - 9:45AM", "Present")
            )
        )
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
        ) {
            // Header without animation interference
            AppHeader(
                title = "History",
                headerType = HeaderType.BACK,
                onBackClick = onBackClick
            )

            // Simple content without complex animations
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(top = 24.dp, start = 24.dp, end = 24.dp, bottom = 20.dp),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                attendanceItems.forEach { (date, items) ->
                    item {
                        Text(
                            date,
                            color = Color(0xFF000000),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = AppFontFamily,
                            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                        )
                    }

                    itemsIndexed(items) { index, (title, subtitle, status) ->
                        SmoothAttendanceItem(
                            title = title,
                            subtitle = subtitle,
                            status = status,
                            onClick = {
                                if (status.lowercase() == "not yet") {
                                    onNavigateToSchedule()
                                } else {
                                    onCardClick(title, status)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SmoothAttendanceItem(
    title: String,
    subtitle: String,
    status: String,
    onClick: () -> Unit = {}
) {
    var isPressed by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    val statusColor = when (status.lowercase()) {
        "present" -> Color(0xFF00CC2C)
        "absent" -> Color(0xFFE53E3E)
        "late" -> Color(0xFFFF9D00)
        "pending" -> Color(0xFFFF9D00)
        "approved" -> Color(0xFF4285F4)
        "rejected" -> Color(0xFFE53E3E)
        "not yet" -> Color(0xFF2C2D32)
        "sick" -> Color(0xFF9C27B0)
        "excused" -> Color(0xFF607D8B)
        else -> Color(0xFF2C2D32)
    }

    val backgroundColor = when (status.lowercase()) {
        "present" -> Color(0x1A00CD2C)
        "absent" -> Color(0x1AE53E3E)
        "late" -> Color(0x1AFF9D00)
        "pending" -> Color(0x1AFF9D00)
        "approved" -> Color(0x1A4285F4)
        "rejected" -> Color(0x1AE53E3E)
        "not yet" -> Color(0x1A2C2D32)
        "sick" -> Color(0x1A9C27B0)
        "excused" -> Color(0x1A607D8B)
        else -> Color(0x1A2C2D32)
    }

    // Simple scale animation for press feedback
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        )
    )

    Card(
        modifier = Modifier
            .padding(bottom = 8.dp)
            .fillMaxWidth()
            .scale(scale)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                isPressed = true
                onClick()
                coroutineScope.launch {
                    delay(100)
                    isPressed = false
                }
            },
        colors = CardDefaults.cardColors(
            containerColor = if (isPressed) Color(0xFFE8E8E8) else Color(0xFFFFFFFF)
        ),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(vertical = 14.dp, horizontal = 16.dp)
                .fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(end = 30.dp)
                    .weight(1f)
            ) {
                Text(
                    title,
                    color = Color(0xFF000000),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    subtitle,
                    color = Color(0xFF888888),
                    fontSize = 14.sp,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            // Simplified Status Button
            Surface(
                modifier = Modifier.clip(shape = RoundedCornerShape(20.dp)),
                color = backgroundColor,
                shape = RoundedCornerShape(20.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 4.dp, horizontal = 10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .padding(end = 6.dp)
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(statusColor)
                    )
                    Text(
                        status,
                        color = statusColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun HistoryScreenPreview() {
    HistoryScreen()
}
