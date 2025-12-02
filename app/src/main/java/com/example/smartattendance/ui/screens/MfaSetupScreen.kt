package com.example.smartattendance.ui.screens

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smartattendance.api.AuthApi
import com.example.smartattendance.api.MfaApi
import com.example.smartattendance.ui.components.AppHeader
import com.example.smartattendance.ui.components.HeaderType
import com.example.smartattendance.ui.theme.AppFontFamily
import com.example.smartattendance.utils.QRCodeGenerator
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MfaSetupScreen(
    user: AuthApi.User,
    onBackClick: () -> Unit = {},
    onSetupComplete: () -> Unit = {}
) {
    val coroutineScope = rememberCoroutineScope()
    val clipboardManager = LocalClipboardManager.current

    var secret by remember { mutableStateOf(MfaApi.generateSecret()) }
    var backupCodes by remember { mutableStateOf(MfaApi.generateBackupCodes()) }
    var qrCodeBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var verificationCode by remember { mutableStateOf("") }
    var isVerifying by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var setupStep by remember { mutableStateOf(1) }
    var showCopiedMessage by remember { mutableStateOf(false) }

    LaunchedEffect(secret) {
        val uri = MfaApi.getTOTPUri(secret, user.email, "SmartAttendance")
        qrCodeBitmap = QRCodeGenerator.generateQRCode(uri)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF6F6F6))
    ) {
        AppHeader(
            title = "Setup MFA",
            headerType = HeaderType.BACK,
            onBackClick = onBackClick,
            showIcon = false
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Step indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                StepIndicator(1, setupStep, "Scan QR")
                HorizontalDivider(
                    modifier = Modifier.width(40.dp).padding(horizontal = 8.dp),
                    color = if (setupStep > 1) Color(0xFF2C2D32) else Color.LightGray
                )
                StepIndicator(2, setupStep, "Verify")
                HorizontalDivider(
                    modifier = Modifier.width(40.dp).padding(horizontal = 8.dp),
                    color = if (setupStep > 2) Color(0xFF2C2D32) else Color.LightGray
                )
                StepIndicator(3, setupStep, "Backup")
            }

            Spacer(modifier = Modifier.height(32.dp))

            when (setupStep) {
                1 -> Step1ScanQR(
                    secret = secret,
                    qrCodeBitmap = qrCodeBitmap,
                    copied = showCopiedMessage,
                    onCopy = {
                        clipboardManager.setText(AnnotatedString(secret))
                        showCopiedMessage = true
                    },
                    onNext = { setupStep = 2 }
                )
                2 -> Step2Verify(
                    code = verificationCode,
                    error = errorMessage,
                    loading = isVerifying,
                    onChange = {
                        if (it.length <= 6 && it.all { c -> c.isDigit() }) {
                            verificationCode = it
                            errorMessage = ""
                        }
                    },
                    onBack = { setupStep = 1 },
                    onVerify = {
                        if (verificationCode.length != 6) {
                            errorMessage = "Please enter 6 digits"
                            return@Step2Verify
                        }
                        isVerifying = true
                        coroutineScope.launch {
                            val valid = MfaApi.verifyTOTP(secret, verificationCode)
                            isVerifying = false
                            if (valid) {
                                setupStep = 3
                                errorMessage = ""
                            } else {
                                errorMessage = "Invalid code"
                                verificationCode = ""
                            }
                        }
                    }
                )
                3 -> Step3BackupCodes(
                    codes = backupCodes,
                    copied = showCopiedMessage,
                    error = errorMessage,
                    loading = isVerifying,
                    onCopy = {
                        clipboardManager.setText(AnnotatedString(backupCodes.joinToString("\n")))
                        showCopiedMessage = true
                    },
                    onComplete = {
                        isVerifying = true
                        coroutineScope.launch {
                            MfaApi.enableMfa(user.user_id ?: "", secret, backupCodes).onSuccess {
                                onSetupComplete()
                            }.onFailure {
                                errorMessage = it.message ?: "Failed"
                                isVerifying = false
                            }
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun StepIndicator(step: Int, current: Int, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(
                    if (step <= current) Color(0xFF2C2D32) else Color(0xFFE5E7EB),
                    RoundedCornerShape(22.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                if (step < current) "✓" else "$step",
                color = if (step <= current) Color.White else Color(0xFF94A3B8),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = AppFontFamily
            )
        }
        Spacer(Modifier.height(8.dp))
        Text(
            label,
            fontSize = 12.sp,
            fontFamily = AppFontFamily,
            color = if (step <= current) Color(0xFF2C2D32) else Color(0xFF94A3B8)
        )
    }
}

@Composable
private fun Step1ScanQR(
    secret: String,
    qrCodeBitmap: Bitmap?,
    copied: Boolean,
    onCopy: () -> Unit,
    onNext: () -> Unit
) {
    Text(
        "Scan QR Code",
        fontSize = 24.sp,
        fontWeight = FontWeight.Bold,
        fontFamily = AppFontFamily,
        color = Color(0xFF1E293B)
    )
    Spacer(Modifier.height(8.dp))
    Text(
        "Scan this QR code with your authenticator app",
        fontSize = 14.sp,
        fontFamily = AppFontFamily,
        color = Color(0xFF64748B),
        textAlign = TextAlign.Center
    )
    Spacer(Modifier.height(24.dp))

    qrCodeBitmap?.let {
        Card(
            modifier = Modifier.size(260.dp),
            elevation = CardDefaults.cardElevation(4.dp),
            colors = CardDefaults.cardColors(Color.White),
            shape = RoundedCornerShape(16.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxSize().padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    bitmap = it.asImageBitmap(),
                    contentDescription = "QR Code",
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }

    Spacer(Modifier.height(24.dp))

    Text(
        "Or enter this code manually:",
        fontSize = 14.sp,
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.Medium,
        color = Color(0xFF1E293B)
    )
    Spacer(Modifier.height(12.dp))
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(Color(0xFFF8F9FA)),
        elevation = CardDefaults.cardElevation(0.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = secret.chunked(4).joinToString(" "),
                fontSize = 16.sp,
                fontFamily = AppFontFamily,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF1E293B),
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onCopy) {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = "Copy",
                    tint = Color(0xFF2C2D32)
                )
            }
        }
    }

    if (copied) {
        Spacer(Modifier.height(8.dp))
        Text(
            "✓ Copied to clipboard",
            fontSize = 13.sp,
            color = Color(0xFF10B981),
            fontFamily = AppFontFamily,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
    }

    Spacer(Modifier.height(32.dp))

    Button(
        onClick = onNext,
        modifier = Modifier.fillMaxWidth().height(56.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(Color(0xFF2C2D32)),
        elevation = ButtonDefaults.buttonElevation(0.dp)
    ) {
        Text(
            "Next",
            fontSize = 16.sp,
            fontFamily = AppFontFamily,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun Step2Verify(
    code: String,
    error: String,
    loading: Boolean,
    onChange: (String) -> Unit,
    onBack: () -> Unit,
    onVerify: () -> Unit
) {
    Text(
        "Verify Code",
        fontSize = 24.sp,
        fontWeight = FontWeight.Bold,
        fontFamily = AppFontFamily,
        color = Color(0xFF1E293B)
    )
    Spacer(Modifier.height(8.dp))
    Text(
        "Enter the 6-digit code from your authenticator app",
        fontSize = 14.sp,
        fontFamily = AppFontFamily,
        color = Color(0xFF64748B),
        textAlign = TextAlign.Center
    )
    Spacer(Modifier.height(32.dp))

    if (error.isNotEmpty()) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(Color(0xFFFEF2F2)),
            elevation = CardDefaults.cardElevation(0.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                error,
                fontSize = 14.sp,
                color = Color(0xFFEF4444),
                modifier = Modifier.padding(16.dp),
                fontFamily = AppFontFamily
            )
        }
    }

    OutlinedTextField(
        value = code,
        onValueChange = onChange,
        modifier = Modifier.fillMaxWidth(),
        label = {
            Text(
                "6-digit code",
                fontFamily = AppFontFamily,
                fontSize = 14.sp
            )
        },
        singleLine = true,
        enabled = !loading,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color(0xFF2C2D32),
            unfocusedBorderColor = Color(0xFFE5E7EB),
            focusedLabelColor = Color(0xFF2C2D32)
        ),
        shape = RoundedCornerShape(12.dp)
    )

    Spacer(Modifier.height(32.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedButton(
            onClick = onBack,
            modifier = Modifier.weight(1f).height(56.dp),
            shape = RoundedCornerShape(16.dp),
            enabled = !loading,
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = Color(0xFF2C2D32)
            )
        ) {
            Text(
                "Back",
                fontFamily = AppFontFamily,
                fontWeight = FontWeight.SemiBold
            )
        }

        Button(
            onClick = onVerify,
            modifier = Modifier.weight(1f).height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(Color(0xFF2C2D32)),
            enabled = !loading && code.length == 6,
            elevation = ButtonDefaults.buttonElevation(0.dp)
        ) {
            if (loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    "Verify",
                    fontSize = 16.sp,
                    fontFamily = AppFontFamily,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun Step3BackupCodes(
    codes: List<String>,
    copied: Boolean,
    error: String,
    loading: Boolean,
    onCopy: () -> Unit,
    onComplete: () -> Unit
) {
    Text(
        "Save Backup Codes",
        fontSize = 24.sp,
        fontWeight = FontWeight.Bold,
        fontFamily = AppFontFamily,
        color = Color(0xFF1E293B)
    )
    Spacer(Modifier.height(8.dp))
    Text(
        "Save these codes in a safe place for emergency access",
        fontSize = 14.sp,
        fontFamily = AppFontFamily,
        color = Color(0xFF64748B),
        textAlign = TextAlign.Center
    )
    Spacer(Modifier.height(24.dp))

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(Color(0xFFFEF3C7)),
        elevation = CardDefaults.cardElevation(0.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("⚠️", fontSize = 24.sp, modifier = Modifier.padding(end = 12.dp))
            Text(
                "Save these codes in a safe place! You can use them if you lose your phone.",
                fontSize = 13.sp,
                fontFamily = AppFontFamily,
                color = Color(0xFF92400E),
                lineHeight = 18.sp
            )
        }
    }

    Spacer(Modifier.height(20.dp))

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(Color(0xFFF8F9FA)),
        elevation = CardDefaults.cardElevation(0.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            codes.chunked(2).forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    row.forEach {
                        Text(
                            it,
                            fontSize = 15.sp,
                            fontFamily = AppFontFamily,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF1E293B),
                            modifier = Modifier.padding(vertical = 6.dp)
                        )
                    }
                }
            }
        }
    }

    Spacer(Modifier.height(16.dp))

    OutlinedButton(
        onClick = onCopy,
        modifier = Modifier.fillMaxWidth().height(48.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = Color(0xFF2C2D32)
        )
    ) {
        Icon(
            imageVector = Icons.Default.ContentCopy,
            contentDescription = "Copy",
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            "Copy All Codes",
            fontFamily = AppFontFamily,
            fontWeight = FontWeight.SemiBold
        )
    }

    if (copied) {
        Spacer(Modifier.height(8.dp))
        Text(
            "✓ Copied to clipboard",
            fontSize = 13.sp,
            color = Color(0xFF10B981),
            fontFamily = AppFontFamily,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
    }

    Spacer(Modifier.height(32.dp))

    Button(
        onClick = onComplete,
        modifier = Modifier.fillMaxWidth().height(56.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(Color(0xFF2C2D32)),
        enabled = !loading,
        elevation = ButtonDefaults.buttonElevation(0.dp)
    ) {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = Color.White,
                strokeWidth = 2.dp
            )
        } else {
            Text(
                "Complete Setup",
                fontSize = 16.sp,
                fontFamily = AppFontFamily,
                fontWeight = FontWeight.SemiBold
            )
        }
    }

    if (error.isNotEmpty()) {
        Spacer(Modifier.height(16.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(Color(0xFFFEF2F2)),
            elevation = CardDefaults.cardElevation(0.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                error,
                fontSize = 14.sp,
                color = Color(0xFFEF4444),
                modifier = Modifier.padding(16.dp),
                fontFamily = AppFontFamily
            )
        }
    }
}

