package com.example.smartattendance.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
        modifier = Modifier.fillMaxSize().padding(24.dp).verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier.size(80.dp).background(
                color = Color(0xFFE91E63).copy(alpha = 0.1f),
                shape = RoundedCornerShape(40.dp)
            ),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "🔐", fontSize = 40.sp)
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Two-Factor Authentication",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = AppFontFamily,
            color = Color.Black
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = if (useBackupCode) "Enter backup code" else "Enter 6-digit code from authenticator",
            fontSize = 14.sp,
            fontFamily = AppFontFamily,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        if (errorMessage.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE))
            ) {
                Text(
                    text = errorMessage,
                    fontSize = 14.sp,
                    color = Color(0xFFD32F2F),
                    modifier = Modifier.padding(12.dp),
                    fontFamily = AppFontFamily
                )
            }
        }

        OutlinedTextField(
            value = verificationCode,
            onValueChange = {
                if (useBackupCode) {
                    if (it.length <= 9) { verificationCode = it.uppercase(); errorMessage = "" }
                } else {
                    if (it.length <= 6 && it.all { char -> char.isDigit() }) {
                        verificationCode = it; errorMessage = ""
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(if (useBackupCode) "Backup code" else "6-digit code", fontFamily = AppFontFamily) },
            placeholder = { Text(if (useBackupCode) "XXXX-XXXX" else "000000", fontFamily = AppFontFamily) },
            singleLine = true,
            enabled = !isVerifying,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFFE91E63),
                unfocusedBorderColor = Color.LightGray
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(
            onClick = { useBackupCode = !useBackupCode; verificationCode = ""; errorMessage = "" },
            enabled = !isVerifying
        ) {
            Text(
                text = if (useBackupCode) "Use authenticator code" else "Use backup code",
                fontFamily = AppFontFamily,
                color = Color(0xFFE91E63)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                if (verificationCode.isEmpty()) { errorMessage = "Please enter a code"; return@Button }
                if (!useBackupCode && verificationCode.length != 6) {
                    errorMessage = "Please enter a 6-digit code"; return@Button
                }

                isVerifying = true
                coroutineScope.launch {
                    val result = MfaApi.verifyMfaCode(user.user_id ?: "", verificationCode)
                    isVerifying = false

                    result.onSuccess { isValid ->
                        if (isValid) onVerificationSuccess()
                        else { errorMessage = "Invalid code. Please try again."; verificationCode = "" }
                    }.onFailure {
                        errorMessage = it.message ?: "Verification failed"; verificationCode = ""
                    }
                }
            },
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(24.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE91E63)),
            enabled = !isVerifying && verificationCode.isNotEmpty()
        ) {
            if (isVerifying) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
            } else {
                Text(text = "Verify", fontSize = 16.sp, fontFamily = AppFontFamily)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = onBackClick, enabled = !isVerifying) {
            Text(text = "Cancel", fontFamily = AppFontFamily, color = Color.Gray)
        }
    }
}

