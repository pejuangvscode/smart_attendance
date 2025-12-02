package com.example.smartattendance.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smartattendance.api.AuthApi
import com.example.smartattendance.api.MfaApi
import com.example.smartattendance.ui.components.AppHeader
import com.example.smartattendance.ui.components.HeaderType
import com.example.smartattendance.ui.theme.AppFontFamily
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MfaSettingsScreen(
    user: AuthApi.User,
    onBackClick: () -> Unit = {},
    onSetupMfa: () -> Unit = {}
) {
    val coroutineScope = rememberCoroutineScope()

    var mfaSettings by remember { mutableStateOf<MfaApi.MfaSettings?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var showDisableDialog by remember { mutableStateOf(false) }
    var isDisabling by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    // Load MFA settings
    LaunchedEffect(Unit) {
        val result = MfaApi.getMfaSettings(user.user_id ?: "")
        isLoading = false
        result.onSuccess { settings ->
            mfaSettings = settings
        }.onFailure {
            errorMessage = it.message ?: "Failed to load MFA settings"
        }
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF6F6F6))
    ) {
        // Use AppHeader component
        AppHeader(
            title = "MFA Settings",
            headerType = HeaderType.BACK,
            onBackClick = onBackClick,
            showIcon = false
        )

        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color(0xFF2C2D32))
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Status Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (mfaSettings?.mfa_enabled == true)
                            Color(0xFFE8F5E9)
                        else
                            Color(0xFFFFF3E0)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(
                                    color = if (mfaSettings?.mfa_enabled == true)
                                        Color(0xFF4CAF50).copy(alpha = 0.2f)
                                    else
                                        Color(0xFFFF9800).copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(24.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = "Security",
                                tint = if (mfaSettings?.mfa_enabled == true)
                                    Color(0xFF4CAF50)
                                else
                                    Color(0xFFFF9800),
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (mfaSettings?.mfa_enabled == true)
                                    "MFA Enabled"
                                else
                                    "MFA Disabled",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = AppFontFamily,
                                color = Color.Black
                            )
                            Text(
                                text = if (mfaSettings?.mfa_enabled == true)
                                    "Your account is protected"
                                else
                                    "Enable for extra security",
                                fontSize = 14.sp,
                                fontFamily = AppFontFamily,
                                color = Color.Gray
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Information
                Text(
                    text = "About Two-Factor Authentication",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = AppFontFamily,
                    color = Color.Black
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Two-factor authentication (2FA) adds an extra layer of security to your account. " +
                            "In addition to your password, you'll need to enter a code from your authenticator app when logging in.",
                    fontSize = 14.sp,
                    fontFamily = AppFontFamily,
                    color = Color.Gray,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Benefits
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Benefits:",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = AppFontFamily,
                            color = Color.Black
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        BenefitItem("- Enhanced account security")
                        BenefitItem("- Protection against unauthorized access")
                        BenefitItem("- Works with Google Authenticator, Authy, etc.")
                        BenefitItem("- Backup codes for emergency access")
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                if (errorMessage.isNotEmpty()) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
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

                // Action Button
                if (mfaSettings?.mfa_enabled == true) {
                    OutlinedButton(
                        onClick = { showDisableDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFFD32F2F)
                        ),
                        enabled = !isDisabling
                    ) {
                        if (isDisabling) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color(0xFFD32F2F),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = "Disable Two-Factor Authentication",
                                fontSize = 16.sp,
                                fontFamily = AppFontFamily
                            )
                        }
                    }

                    // Show remaining backup codes count
                    mfaSettings?.backup_codes?.let { codes ->
                        if (codes.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "You have ${codes.size} backup code${if (codes.size != 1) "s" else ""} remaining",
                                fontSize = 14.sp,
                                fontFamily = AppFontFamily,
                                color = Color.Gray,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                } else {
                    Button(
                        onClick = onSetupMfa,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2C2D32)),
                        elevation = ButtonDefaults.buttonElevation(0.dp)
                    ) {
                        Text(
                            text = "Enable Two-Factor Authentication",
                            fontSize = 16.sp,
                            fontFamily = AppFontFamily,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        // Disable MFA Confirmation Dialog
        if (showDisableDialog) {
            AlertDialog(
                onDismissRequest = { if (!isDisabling) showDisableDialog = false },
                title = {
                    Text(
                        text = "Disable Two-Factor Authentication?",
                        fontFamily = AppFontFamily,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Text(
                        text = "Your account will be less secure without two-factor authentication. Are you sure you want to disable it?",
                        fontFamily = AppFontFamily
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            isDisabling = true
                            coroutineScope.launch {
                                val result = MfaApi.disableMfa(user.user_id ?: "")
                                isDisabling = false
                                showDisableDialog = false

                                result.onSuccess {
                                    mfaSettings = mfaSettings?.copy(mfa_enabled = false)
                                }.onFailure {
                                    errorMessage = it.message ?: "Failed to disable MFA"
                                }
                            }
                        },
                        enabled = !isDisabling
                    ) {
                        if (isDisabling) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Disable", fontFamily = AppFontFamily, color = Color(0xFFD32F2F))
                        }
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showDisableDialog = false },
                        enabled = !isDisabling
                    ) {
                        Text("Cancel", fontFamily = AppFontFamily)
                    }
                }
            )
        }
    }
}

@Composable
fun BenefitItem(text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = text,
            fontSize = 14.sp,
            fontFamily = AppFontFamily,
            color = Color.Black.copy(alpha = 0.8f)
        )
    }
}

