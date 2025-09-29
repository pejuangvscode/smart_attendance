package com.example.smartattendance.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smartattendance.ui.theme.SmartAttendanceTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onLoginClick: (String, String) -> Unit = { _, _ -> }
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var loginError by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    // Predefined credentials
    val validUsername = "admin"
    val validPassword = "password123"

    fun validateLogin() {
        isLoading = true
        loginError = ""

        // Simulate loading delay
        CoroutineScope(Dispatchers.Main).launch {
            delay(1000) // 1 second delay

            when {
                username.isEmpty() -> {
                    loginError = "Username tidak boleh kosong"
                }
                password.isEmpty() -> {
                    loginError = "Password tidak boleh kosong"
                }
                username != validUsername -> {
                    loginError = "Username salah"
                }
                password != validPassword -> {
                    loginError = "Password salah"
                }
                else -> {
                    loginError = ""
                    onLoginClick(username, password)
                }
            }
            isLoading = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top status bar space
        Spacer(modifier = Modifier.height(60.dp))

        // Profile avatar placeholder
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = Color.LightGray,
                shape = CircleShape
            ) {}
        }

        Spacer(modifier = Modifier.height(40.dp))

        // Login title
        Text(
            text = "Login",
            fontSize = 28.sp,
            fontWeight = FontWeight.Normal,
            color = Color.Black,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Start
        )

//        // Credentials info card
//        Card(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(vertical = 16.dp),
//            colors = CardDefaults.cardColors(
//                containerColor = Color(0xFFE3F2FD)
//            ),
//            shape = RoundedCornerShape(8.dp)
//        ) {
//            Column(
//                modifier = Modifier.padding(12.dp)
//            ) {
//                Text(
//                    text = "Kredensial Login:",
//                    fontSize = 14.sp,
//                    fontWeight = FontWeight.Bold,
//                    color = Color(0xFF1976D2)
//                )
//                Spacer(modifier = Modifier.height(4.dp))
//                Text(
//                    text = "Username: admin",
//                    fontSize = 12.sp,
//                    color = Color(0xFF1976D2)
//                )
//                Text(
//                    text = "Password: password123",
//                    fontSize = 12.sp,
//                    color = Color(0xFF1976D2)
//                )
//            }
//        }

        Spacer(modifier = Modifier.height(16.dp))

        // Error message
        if (loginError.isNotEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFFFEBEE)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = loginError,
                    fontSize = 14.sp,
                    color = Color(0xFFD32F2F),
                    modifier = Modifier.padding(12.dp)
                )
            }
        }

        // Username field
        Text(
            text = "Username",
            fontSize = 16.sp,
            color = Color.Black,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Start
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = username,
            onValueChange = {
                username = it
                loginError = "" // Clear error when user types
            },
            placeholder = {
                Text(
                    text = "Masukkan username",
                    color = Color.Gray
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = if (loginError.contains("Username")) Color.Red else Color.Gray,
                unfocusedBorderColor = if (loginError.contains("Username")) Color.Red else Color.Gray,
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black
            ),
            singleLine = true,
            enabled = !isLoading
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Password field
        Text(
            text = "Password",
            fontSize = 16.sp,
            color = Color.Black,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Start
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
                loginError = "" // Clear error when user types
            },
            placeholder = {
                Text(
                    text = "Masukkan password",
                    color = Color.Gray
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = if (loginError.contains("Password")) Color.Red else Color.Gray,
                unfocusedBorderColor = if (loginError.contains("Password")) Color.Red else Color.Gray,
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black
            ),
            singleLine = true,
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            enabled = !isLoading,
            trailingIcon = {
                TextButton(
                    onClick = { passwordVisible = !passwordVisible },
                    enabled = !isLoading
                ) {
                    Text(
                        text = if (passwordVisible) "Hide" else "Show",
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Forgot Password link
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(
                onClick = { /* Handle forgot password */ },
                enabled = !isLoading
            ) {
                Text(
                    text = "Forgot Password?",
                    color = Color.Black,
                    fontSize = 14.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Login button
        Button(
            onClick = { validateLogin() },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(24.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF424242)
            ),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    text = "Log in",
                    color = Color.White,
                    fontSize = 16.sp
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    SmartAttendanceTheme {
        LoginScreen()
    }
}
