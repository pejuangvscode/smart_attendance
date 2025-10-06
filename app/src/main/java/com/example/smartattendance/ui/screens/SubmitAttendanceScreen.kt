package com.example.smartattendance.ui.screens

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smartattendance.ui.components.AppHeader
import com.example.smartattendance.ui.components.HeaderType
import com.example.smartattendance.ui.components.AppBottomNavigation
import com.example.smartattendance.ui.theme.AppFontFamily

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubmitAttendanceScreen(
    capturedPhoto: Bitmap? = null,
    isLateAttendance: Boolean = false,
    onBackClick: () -> Unit = {},
    onTakePhotoClick: () -> Unit = {},
    onSubmitSuccess: () -> Unit = {},
    onNavigateHome: () -> Unit = {},
    onNavigateSchedule: () -> Unit = {},
    onNavigateHistory: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // Header
        AppHeader(
            title = "Submit Attendance",
            headerType = HeaderType.BACK,
            onBackClick = onBackClick
        )

        // Content
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(Color(0xFFF6F6F6))
                .padding(bottom = 8.dp, top = 44.dp)
        ) {
            // Photo display
            Box(
                modifier = Modifier
                    .padding(start = 44.dp, end= 44.dp, bottom = 16.dp)
                    .height(400.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.Gray.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                if (capturedPhoto != null) {
                    Image(
                        bitmap = capturedPhoto.asImageBitmap(),
                        contentDescription = "Captured photo",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    // Placeholder image
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = "Camera placeholder",
                        modifier = Modifier.size(64.dp),
                        tint = Color.Gray
                    )
                }
            }

            // Take another photo button
            OutlinedButton(
                onClick = onTakePhotoClick,
                border = ButtonDefaults.outlinedButtonBorder,
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color(0xFF2C2D32)
                ),
                modifier = Modifier
                    .padding(horizontal = 44.dp)
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(4.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = "Camera icon",
                        modifier = Modifier.size(24.dp),
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Take another photo",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontFamily = AppFontFamily
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Submit button
            Button(
                onClick = onSubmitSuccess,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFE0697E)
                ),
                modifier = Modifier
                    .padding(horizontal = 44.dp)
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(4.dp)
            ) {
                Text(
                    text = "Submit",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontFamily = AppFontFamily
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SubmitAttendanceScreenPreview() {
    SubmitAttendanceScreen()
}
