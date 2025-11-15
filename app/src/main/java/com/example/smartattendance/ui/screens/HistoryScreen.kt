package com.example.smartattendance.ui.screens

import android.util.Log
import android.app.Activity
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.smartattendance.api.AuthApi
import com.example.smartattendance.api.HistoryApi
import com.example.smartattendance.api.HistoryGroupedItem
import com.example.smartattendance.ui.components.AppHeader
import com.example.smartattendance.ui.components.HeaderType
import com.example.smartattendance.ui.theme.AppFontFamily
import com.example.smartattendance.utils.SessionManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun HistoryScreen(
    onBackClick: () -> Unit = {},
    onCardClick: (String, String) -> Unit = { _, _ -> },
    onNavigateToSchedule: () -> Unit = {}
) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }

    var attendanceItems by remember { mutableStateOf<List<HistoryGroupedItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

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

    // Load data when screen is first composed - similar to HomeScreen pattern
    LaunchedEffect(Unit) {
        isLoading = true
        errorMessage = null

        try {
            val userId = sessionManager.getUserId()
            Log.d("HistoryScreen", "Fetching history for user: $userId")

            if (userId != null && userId.isNotEmpty()) {
                val result = HistoryApi.getAttendanceHistory(
                    supabase = AuthApi.supabase,
                    userId = userId
                )

                result.onSuccess { data ->
                    attendanceItems = data
                    Log.d("HistoryScreen", "Successfully loaded ${data.size} date groups")
                }.onFailure { error ->
                    errorMessage = error.message ?: "Failed to load history"
                    Log.e("HistoryScreen", "Error loading history", error)
                }
            } else {
                errorMessage = "User not logged in"
            }
        } catch (e: Exception) {
            errorMessage = e.message ?: "An error occurred"
            Log.e("HistoryScreen", "Exception loading history", e)
        } finally {
            isLoading = false
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
        ) {
            // Header without animation interference
            AppHeader(
                title = "History",
                headerType = HeaderType.BACK,
                onBackClick = onBackClick
            )

            // Content with loading and error states
            when {
                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color(0xFF2C2D32))
                    }
                }

                errorMessage != null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(24.dp)
                        ) {
                            Text(
                                text = "Failed to load history",
                                fontFamily = AppFontFamily,
                                fontWeight = FontWeight.Medium,
                                color = Color.Gray,
                                fontSize = 16.sp
                            )
                            Text(
                                text = errorMessage ?: "",
                                fontFamily = AppFontFamily,
                                color = Color.Gray,
                                fontSize = 14.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                    }
                }

                attendanceItems.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(24.dp)
                        ) {
                            Text(
                                text = "No History Yet",
                                fontFamily = AppFontFamily,
                                fontWeight = FontWeight.Medium,
                                color = Color.Gray,
                                fontSize = 16.sp
                            )
                            Text(
                                text = "Your attendance history will appear here",
                                fontFamily = AppFontFamily,
                                color = Color.Gray,
                                fontSize = 14.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(top = 24.dp, start = 24.dp, end = 24.dp, bottom = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(0.dp)
                    ) {
                        attendanceItems.forEach { dateGroup ->
                            item {
                                Text(
                                    dateGroup.date,
                                    color = Color(0xFF000000),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = AppFontFamily,
                                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                                )
                            }

                            itemsIndexed(dateGroup.items) { index, item ->
                                SmoothAttendanceItem(
                                    title = item.title,
                                    subtitle = item.subtitle,
                                    status = item.status,
                                    onClick = {
                                        onCardClick(item.title, item.status)
                                    }
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
