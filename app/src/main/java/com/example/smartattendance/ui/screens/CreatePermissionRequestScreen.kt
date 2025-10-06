package com.example.smartattendance.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTimeFilled
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smartattendance.ui.components.AppHeader
import com.example.smartattendance.ui.components.HeaderType
import com.example.smartattendance.ui.theme.AppFontFamily
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePermissionRequestScreen(
    onBackClick: () -> Unit = {},
    onNavigateToRequestList: () -> Unit = {}
) {
    val currentDate = remember {
        SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()).format(Date())
    }
    val currentTime = remember {
        SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
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
            // Static Header
            AppHeader(
                title = "Request Permission",
                headerType = HeaderType.BACK,
                onBackClick = onBackClick,
                showIcon = false,
                iconRes = Icons.Default.AccessTimeFilled
            )

            // Content Column
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(color = Color(0xFFF6F6F6))
                    .padding(top = 24.dp, start = 28.dp, end = 28.dp)
            ) {
                // Static Pending Status Section
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(bottom = 24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AccessTimeFilled,
                        contentDescription = "Pending",
                        tint = Color(0xFFFF9D00),
                        modifier = Modifier
                            .padding(bottom = 8.dp, start = 28.dp, end = 28.dp)
                            .size(120.dp)
                    )
                    Text(
                        text = "PENDING",
                        color = Color(0xFFFF9D00),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = AppFontFamily,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.width(188.dp)
                    )
                }

                // Static Detail Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 13.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(vertical = 10.dp)
                    ) {
                        Text(
                            text = "Detail",
                            color = Color(0xFF2C2D32),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = AppFontFamily,
                            modifier = Modifier
                                .padding(bottom = 16.dp, start = 15.dp)
                        )

                        Column(
                            modifier = Modifier
                                .padding(horizontal = 16.dp)
                                .fillMaxWidth()
                                .padding(top = 4.dp, bottom = 22.dp)
                        ) {
                            DetailRow("Name", "TEOFILUS SATRIA RADA INSANI")
                            DetailRow("NIM", "01082230017")
                            DetailRow("Class", "KECERDASAN KOMPUTASIONAL")
                            DetailRow("Requested time", "8:45AM - 11:25AM")
                            DetailRow("Requested date", "29 SEPTEMBER 2025")
                            DetailRow("Submit time", "19:20")
                            DetailRow("Submit date", "28 SEPTEMBER 2025")
                            DetailRow("Reason", "SICK", isReason = true, isLast = true)
                        }
                    }
                }

                // Static Class Information Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 13.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(vertical = 10.dp)
                    ) {
                        Text(
                            text = "Class Information",
                            color = Color(0xFF2C2D32),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = AppFontFamily,
                            modifier = Modifier
                                .padding(bottom = 16.dp, start = 15.dp)
                        )

                        Column(
                            modifier = Modifier
                                .padding(horizontal = 16.dp)
                                .fillMaxWidth()
                        ) {
                            DetailRow("Day", "Monday", hasBottomPadding = true)
                            DetailRow("Schedule", "8:45AM - 11:25AM", hasBottomPadding = true)
                            DetailRow("Date", "29 SEPTEMBER 2025", isLast = true)
                        }
                    }
                }

                // Static Description Card with Medical Certificate
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 13.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(vertical = 10.dp)
                    ) {
                        Text(
                            text = "Description",
                            color = Color(0xFF2C2D32),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = AppFontFamily,
                            modifier = Modifier
                                .padding(bottom = 16.dp, start = 15.dp)
                        )

                        Column(
                            modifier = Modifier
                                .padding(horizontal = 16.dp)
                                .fillMaxWidth()
                        ) {
                            Text(
                                text = "Good morning Sir/Madam, I would like to inform you that I will be arriving late to class today. Thank you for your understanding.",
                                color = Color(0xFF2C2D32),
                                fontSize = 14.sp,
                                fontFamily = AppFontFamily,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )

                            // Medical Certificate Image Placeholder
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(300.dp),
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "Medical Certificate\n(Image attachment)",
                                        textAlign = TextAlign.Center,
                                        color = Color(0xFF666666),
                                        fontSize = 14.sp,
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
}

@Composable
private fun DetailRow(
    label: String,
    value: String,
    isReason: Boolean = false,
    hasBottomPadding: Boolean = false,
    isLast: Boolean = false
) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = if (isLast) 0.dp else if (hasBottomPadding) 10.dp else 15.dp)
    ) {
        Text(
            text = label,
            color = Color(0xFF2C2D32),
            fontSize = 14.sp, // Changed from .dp to .sp
            fontFamily = AppFontFamily,
            modifier = Modifier
                .padding(end = 4.dp)
                .weight(1f)
        )
        Text(
            text = value,
            color = if (isReason) Color(0xFF8A38F5) else if (label == "Class") Color(0xFF000000) else Color(0xFF2C2D32),
            fontSize = 14.sp, // Already correct
            fontWeight = if (isReason) FontWeight.Bold else FontWeight.Normal,
            fontFamily = AppFontFamily,
            textAlign = TextAlign.Right,
            modifier = Modifier.weight(1f)
        )
    }
}


@Preview(showBackground = true)
@Composable
fun CreatePermissionRequestScreenPreview() {
    CreatePermissionRequestScreen()
}
