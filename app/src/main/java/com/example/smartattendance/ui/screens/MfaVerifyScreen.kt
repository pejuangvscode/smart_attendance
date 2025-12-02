package com.example.smartattendance.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LockPerson
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smartattendance.api.AuthApi
import com.example.smartattendance.api.MfaApi
import com.example.smartattendance.ui.components.AppHeader
import com.example.smartattendance.ui.components.HeaderType
import com.example.smartattendance.ui.theme.AppFontFamily
import kotlinx.coroutines.launch

@Composable
fun MfaVerifyScreen(
    user: AuthApi.User,
    onVerificationSuccess: () -> Unit = {},
    onBackClick: () -> Unit = {}
) {
    val coroutineScope = rememberCoroutineScope()

    var verificationCode by remember { mutableStateOf("") }
    var isVerifying by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var useBackupCode by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF6F6F6))
    ) {
        AppHeader(
            title = "Two-Factor Authentication",
            headerType = HeaderType.BACK,
            onBackClick = onBackClick,
            showIcon = false
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(
                        color = Color(0xFF2C2D32).copy(alpha = 0.1f),
                        shape = RoundedCornerShape(40.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.LockPerson,
                    contentDescription = "Two-Factor Authentication",
                    tint = Color(0xFF2C2D32),
                    modifier = Modifier.size(40.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = if (useBackupCode)
                    "Enter one of your backup codes"
                else
                    "Enter the 6-digit code from your authenticator app",
                fontSize = 14.sp,
                fontFamily = AppFontFamily,
                color = Color(0xFF64748B),
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(32.dp))

            if (errorMessage.isNotEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
                    elevation = CardDefaults.cardElevation(0.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = errorMessage,
                        fontSize = 14.sp,
                        color = Color(0xFFEF4444),
                        modifier = Modifier.padding(16.dp),
                        fontFamily = AppFontFamily
                    )
                }
            }

            OutlinedTextField(
                value = verificationCode,
                onValueChange = {
                    if (useBackupCode) {
                        if (it.length <= 9) {
                            verificationCode = it.uppercase()
                            errorMessage = ""
                        }
                    } else {
                        if (it.length <= 6 && it.all { char -> char.isDigit() }) {
                            verificationCode = it
                            errorMessage = ""
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                label = {
                    Text(
                        if (useBackupCode) "Backup code" else "6-digit code",
                        fontFamily = AppFontFamily,
                        fontSize = 14.sp
                    )
                },
                placeholder = {
                    Text(
                        if (useBackupCode) "XXXX-XXXX" else "000000",
                        fontFamily = AppFontFamily,
                        color = Color(0xFFCBD5E1)
                    )
                },
                singleLine = true,
                enabled = !isVerifying,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF2C2D32),
                    unfocusedBorderColor = Color(0xFFE5E7EB),
                    focusedLabelColor = Color(0xFF2C2D32)
                ),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            TextButton(
                onClick = {
                    useBackupCode = !useBackupCode
                    verificationCode = ""
                    errorMessage = ""
                },
                enabled = !isVerifying
            ) {
                Text(
                    text = if (useBackupCode) "Use authenticator code" else "Use backup code",
                    fontFamily = AppFontFamily,
                    color = Color(0xFF2C2D32),
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (verificationCode.isEmpty()) {
                        errorMessage = "Please enter a code"
                        return@Button
                    }
                    if (!useBackupCode && verificationCode.length != 6) {
                        errorMessage = "Please enter a 6-digit code"
                        return@Button
                    }

                    isVerifying = true
                    coroutineScope.launch {
                        val result = MfaApi.verifyMfaCode(user.user_id ?: "", verificationCode)
                        isVerifying = false

                        result.onSuccess { isValid ->
                            if (isValid) {
                                onVerificationSuccess()
                            } else {
                                errorMessage = "Invalid code. Please try again."
                                verificationCode = ""
                            }
                        }.onFailure {
                            errorMessage = it.message ?: "Verification failed"
                            verificationCode = ""
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2C2D32)),
                enabled = !isVerifying && verificationCode.isNotEmpty(),
                elevation = ButtonDefaults.buttonElevation(0.dp)
            ) {
                if (isVerifying) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "Verify",
                        fontSize = 16.sp,
                        fontFamily = AppFontFamily,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

