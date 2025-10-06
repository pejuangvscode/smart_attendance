package com.example.smartattendance.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smartattendance.ui.components.AppHeader
import com.example.smartattendance.ui.components.HeaderType
import com.example.smartattendance.ui.components.AppBottomNavigation

@Composable
fun ListScreen(
    onNavigateBack: () -> Unit,
    onNavigate: (String) -> Unit,
    onCardClick: (String, String) -> Unit = { _, _ -> }
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .background(
                color = Color(0xFFFFFFFF),
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(
                    color = Color(0xFFF6F6F6),
                )
                .verticalScroll(rememberScrollState())
        ) {
            // Use AppHeader component
            AppHeader(
                title = "List",
                headerType = HeaderType.BACK_WITH_ACTION,
                onBackClick = onNavigateBack,
                actionIcon = Icons.Default.FilterList,
                onActionClick = { /* Handle filter action */ },
                showIcon = true,
                iconRes = Icons.AutoMirrored.Filled.List
            )

            // Content Column
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = Color(0xFFF6F6F6),
                    )
                    .padding(top = 24.dp, start = 38.dp, end = 38.dp)
            ) {
                // Today Section Header
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .padding(bottom = 8.dp)
                        .fillMaxWidth()
                        .padding(end = 11.dp)
                ) {
                    Text(
                        "Today",
                        color = Color(0xFF000000),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.width(74.dp)
                    )
                    Icon(
                        imageVector = Icons.Default.FilterList,
                        contentDescription = "Filter",
                        tint = Color.Black,
                        modifier = Modifier.size(26.dp)
                    )
                }

                // Today's Records
                AttendanceItem(
                    title = "KECERDASAN KOMPUTASIONAL Laboratory\nRequest Permission",
                    status = "Pending",
                    statusColor = Color(0xFFFF9D00),
                    backgroundColor = Color(0x1AFF9D00),
                    hasElevation = false,
                    onClick = { onCardClick("KECERDASAN KOMPUTASIONAL Laboratory", "Pending") }
                )

                AttendanceItem(
                    title = "KECERDASAN KOMPUTASIONAL\nRequest Permission",
                    status = "Pending",
                    statusColor = Color(0xFFFF9D00),
                    backgroundColor = Color(0x1AFF9D00),
                    hasElevation = false,
                    onClick = { onCardClick("KECERDASAN KOMPUTASIONAL", "Pending") }
                )

                AttendanceItem(
                    title = "KEAMANAN KOMPUTER & JARINGAN Laboratory\n10:00AM - 11:40AM",
                    status = "Not Yet",
                    statusColor = Color(0xFF2C2D32),
                    backgroundColor = Color(0x1A2C2D32),
                    hasElevation = true,
                    onClick = { onCardClick("KEAMANAN KOMPUTER & JARINGAN Laboratory", "Not Yet") }
                )

                AttendanceItem(
                    title = "KEAMANAN KOMPUTER & JARINGAN\n7:15AM - 9:45AM",
                    status = "Not Yet",
                    statusColor = Color(0xFF2C2D32),
                    backgroundColor = Color(0x1A2C2D32),
                    hasElevation = true,
                    onClick = { onCardClick("KEAMANAN KOMPUTER & JARINGAN", "Not Yet") }
                )

                // Previous Day Section
                Text(
                    "Thursday, 25 Sep 2025",
                    color = Color(0xFF000000),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .padding(bottom = 8.dp)
                        .width(268.dp)
                )

                AttendanceItem(
                    title = "PERANCANGAN & PEMROGRAMAN WEB Laboratory\n10:00AM - 11:40AM",
                    status = "Present",
                    statusColor = Color(0xFF00CC2C),
                    backgroundColor = Color(0x1A00CD2C),
                    hasElevation = false,
                    onClick = { onCardClick("PERANCANGAN & PEMROGRAMAN WEB Laboratory", "Present") }
                )

                AttendanceItem(
                    title = "PERANCANGAN & PEMROGRAMAN WEB\n7:15AM - 9:45AM",
                    status = "Present",
                    statusColor = Color(0xFF00CC2C),
                    backgroundColor = Color(0x1A00CD2C),
                    hasElevation = false,
                    isLast = true,
                    onClick = { onCardClick("PERANCANGAN & PEMROGRAMAN WEB", "Present") }
                )

                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        // Bottom Navigation
        AppBottomNavigation(
            currentRoute = "list",
            onNavigate = onNavigate
        )
    }
}

@Composable
private fun AttendanceItem(
    title: String,
    status: String,
    statusColor: Color,
    backgroundColor: Color,
    hasElevation: Boolean,
    isLast: Boolean = false,
    onClick: () -> Unit = {}
) {
    val modifier = if (hasElevation) {
        Modifier
            .padding(bottom = 8.dp)
            .clip(shape = RoundedCornerShape(4.dp))
            .fillMaxWidth()
            .background(
                color = Color(0xFFFFFFFF),
                shape = RoundedCornerShape(4.dp)
            )
            .shadow(
                elevation = 13.dp,
                spotColor = Color(0x409C9C9C),
            )
    } else {
        Modifier
            .padding(bottom = if (isLast) 0.dp else 8.dp)
            .fillMaxWidth()
            .background(
                color = Color(0xFFFFFFFF),
            )
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .padding(vertical = 8.dp, horizontal = 12.dp)
            .clickable(onClick = onClick)
    ) {
        Text(
            title,
            color = Color(0xFF000000),
            fontSize = 16.sp,
            modifier = Modifier
                .padding(end = 30.dp)
                .weight(1f)
        )

        // Status Button
        Surface(
            modifier = Modifier
                .clip(shape = RoundedCornerShape(20.dp)),
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
