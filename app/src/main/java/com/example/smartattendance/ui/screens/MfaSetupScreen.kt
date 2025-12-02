package com.example.smartattendance.ui.screens

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Setup MFA", fontFamily = AppFontFamily, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color.Black
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier.fillMaxSize().padding(paddingValues).padding(24.dp).verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Step indicator
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                StepIndicator(1, setupStep, "Scan")
                Divider(modifier = Modifier.width(40.dp).padding(horizontal = 8.dp),
                    color = if (setupStep > 1) Color(0xFFE91E63) else Color.LightGray)
                StepIndicator(2, setupStep, "Verify")
                Divider(modifier = Modifier.width(40.dp).padding(horizontal = 8.dp),
                    color = if (setupStep > 2) Color(0xFFE91E63) else Color.LightGray)
                StepIndicator(3, setupStep, "Backup")
            }

            Spacer(modifier = Modifier.height(32.dp))

            when (setupStep) {
                1 -> Step1ScanQR(secret, qrCodeBitmap, showCopiedMessage,
                    { clipboardManager.setText(AnnotatedString(secret)); showCopiedMessage = true },
                    { setupStep = 2 })
                2 -> Step2Verify(verificationCode, errorMessage, isVerifying,
                    { if (it.length <= 6 && it.all { c -> c.isDigit() }) { verificationCode = it; errorMessage = "" } },
                    { setupStep = 1 },
                    {
                        if (verificationCode.length != 6) { errorMessage = "Please enter 6 digits"; return@Step2Verify }
                        isVerifying = true
                        coroutineScope.launch {
                            val valid = MfaApi.verifyTOTP(secret, verificationCode)
                            isVerifying = false
                            if (valid) { setupStep = 3; errorMessage = "" }
                            else { errorMessage = "Invalid code"; verificationCode = "" }
                        }
                    })
                3 -> Step3BackupCodes(backupCodes, showCopiedMessage, errorMessage, isVerifying,
                    { clipboardManager.setText(AnnotatedString(backupCodes.joinToString("\n"))); showCopiedMessage = true },
                    {
                        isVerifying = true
                        coroutineScope.launch {
                            MfaApi.enableMfa(user.user_id ?: "", secret, backupCodes).onSuccess {
                                onSetupComplete()
                            }.onFailure { errorMessage = it.message ?: "Failed" }
                            isVerifying = false
                        }
                    })
            }
        }
    }
}

@Composable
private fun StepIndicator(step: Int, current: Int, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            Modifier.size(40.dp).background(
                if (step <= current) Color(0xFFE91E63) else Color.LightGray,
                RoundedCornerShape(20.dp)
            ),
            contentAlignment = Alignment.Center
        ) {
            Text(if (step < current) "✓" else "$step", color = Color.White, fontSize = 16.sp,
                fontWeight = FontWeight.Bold, fontFamily = AppFontFamily)
        }
        Spacer(Modifier.height(4.dp))
        Text(label, fontSize = 12.sp, fontFamily = AppFontFamily,
            color = if (step <= current) Color.Black else Color.Gray)
    }
}

@Composable
private fun Step1ScanQR(secret: String, qr: Bitmap?, copied: Boolean, onCopy: () -> Unit, onNext: () -> Unit) {
    Text("Scan QR Code", fontSize = 24.sp, fontWeight = FontWeight.Bold, fontFamily = AppFontFamily)
    Spacer(Modifier.height(16.dp))
    Text("Scan with Google Authenticator or Authy", fontSize = 14.sp,
        fontFamily = AppFontFamily, color = Color.Gray, textAlign = TextAlign.Center)
    Spacer(Modifier.height(24.dp))
    qr?.let {
        Card(Modifier.size(250.dp), elevation = CardDefaults.cardElevation(4.dp)) {
            Box(Modifier.fillMaxSize(), Alignment.Center) {
                Image(it.asImageBitmap(), "QR Code", Modifier.fillMaxSize().padding(16.dp))
            }
        }
    }
    Spacer(Modifier.height(24.dp))
    Text("Or enter manually:", fontSize = 14.sp, fontFamily = AppFontFamily, color = Color.Gray)
    Spacer(Modifier.height(8.dp))
    Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(Color(0xFFF5F5F5))) {
        Row(Modifier.fillMaxWidth().padding(16.dp), Arrangement.SpaceBetween, Alignment.CenterVertically) {
            Text(secret.chunked(4).joinToString(" "), fontSize = 16.sp, fontFamily = AppFontFamily,
                fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
            IconButton(onCopy) { Icon(Icons.Default.ContentCopy, "Copy", tint = Color(0xFFE91E63)) }
        }
    }
    if (copied) { Spacer(Modifier.height(8.dp)); Text("✓ Copied", fontSize = 12.sp,
        color = Color(0xFF4CAF50), fontFamily = AppFontFamily) }
    Spacer(Modifier.height(32.dp))
    Button(onNext, Modifier.fillMaxWidth().height(48.dp), shape = RoundedCornerShape(24.dp),
        colors = ButtonDefaults.buttonColors(Color(0xFFE91E63))) {
        Text("Next", fontSize = 16.sp, fontFamily = AppFontFamily)
    }
}

@Composable
private fun Step2Verify(code: String, error: String, loading: Boolean, onChange: (String) -> Unit,
    onBack: () -> Unit, onVerify: () -> Unit) {
    Text("Verify Code", fontSize = 24.sp, fontWeight = FontWeight.Bold, fontFamily = AppFontFamily)
    Spacer(Modifier.height(16.dp))
    Text("Enter 6-digit code from authenticator", fontSize = 14.sp,
        fontFamily = AppFontFamily, color = Color.Gray, textAlign = TextAlign.Center)
    Spacer(Modifier.height(32.dp))
    if (error.isNotEmpty()) {
        Card(Modifier.fillMaxWidth().padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(Color(0xFFFFEBEE))) {
            Text(error, fontSize = 14.sp, color = Color(0xFFD32F2F),
                modifier = Modifier.padding(12.dp), fontFamily = AppFontFamily)
        }
    }
    OutlinedTextField(code, onChange, Modifier.fillMaxWidth(), label = { Text("6-digit code", fontFamily = AppFontFamily) },
        singleLine = true, enabled = !loading, colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color(0xFFE91E63), unfocusedBorderColor = Color.LightGray))
    Spacer(Modifier.height(32.dp))
    Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(16.dp)) {
        OutlinedButton(onBack, Modifier.weight(1f).height(48.dp),
            shape = RoundedCornerShape(24.dp), enabled = !loading) { Text("Back", fontFamily = AppFontFamily) }
        Button(onVerify, Modifier.weight(1f).height(48.dp), shape = RoundedCornerShape(24.dp),
            colors = ButtonDefaults.buttonColors(Color(0xFFE91E63)), enabled = !loading && code.length == 6) {
            if (loading) CircularProgressIndicator(Modifier.size(20.dp), Color.White, 2.dp)
            else Text("Verify", fontSize = 16.sp, fontFamily = AppFontFamily)
        }
    }
}

@Composable
private fun Step3BackupCodes(codes: List<String>, copied: Boolean, error: String, loading: Boolean,
    onCopy: () -> Unit, onComplete: () -> Unit) {
    Text("Save Backup Codes", fontSize = 24.sp, fontWeight = FontWeight.Bold, fontFamily = AppFontFamily)
    Spacer(Modifier.height(16.dp))
    Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(Color(0xFFFFF3E0))) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("⚠️", fontSize = 24.sp, modifier = Modifier.padding(end = 12.dp))
            Text("Save these codes!", fontSize = 14.sp, fontFamily = AppFontFamily, color = Color(0xFFE65100))
        }
    }
    Spacer(Modifier.height(24.dp))
    Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(Color(0xFFF5F5F5))) {
        Column(Modifier.padding(16.dp)) {
            codes.chunked(2).forEach { row ->
                Row(Modifier.fillMaxWidth(), Arrangement.SpaceEvenly) {
                    row.forEach { Text(it, fontSize = 16.sp, fontFamily = AppFontFamily,
                        fontWeight = FontWeight.Medium, modifier = Modifier.padding(vertical = 8.dp)) }
                }
            }
        }
    }
    Spacer(Modifier.height(16.dp))
    OutlinedButton(onCopy, Modifier.fillMaxWidth()) {
        Icon(Icons.Default.ContentCopy, "Copy", Modifier.size(20.dp))
        Spacer(Modifier.width(8.dp))
        Text("Copy All", fontFamily = AppFontFamily)
    }
    if (copied) { Spacer(Modifier.height(8.dp)); Text("✓ Copied", fontSize = 12.sp,
        color = Color(0xFF4CAF50), fontFamily = AppFontFamily,
        modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center) }
    Spacer(Modifier.height(32.dp))
    Button(onComplete, Modifier.fillMaxWidth().height(48.dp), shape = RoundedCornerShape(24.dp),
        colors = ButtonDefaults.buttonColors(Color(0xFFE91E63)), enabled = !loading) {
        if (loading) CircularProgressIndicator(Modifier.size(20.dp), Color.White, 2.dp)
        else Text("Complete Setup", fontSize = 16.sp, fontFamily = AppFontFamily)
    }
    if (error.isNotEmpty()) {
        Spacer(Modifier.height(16.dp))
        Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(Color(0xFFFFEBEE))) {
            Text(error, fontSize = 14.sp, color = Color(0xFFD32F2F),
                modifier = Modifier.padding(12.dp), fontFamily = AppFontFamily)
        }
    }
}

